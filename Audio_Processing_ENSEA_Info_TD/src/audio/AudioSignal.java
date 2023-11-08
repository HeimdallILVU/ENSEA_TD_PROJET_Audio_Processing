package audio;

import javax.sound.sampled.*;

public class AudioSignal {

    private double[] sampleBuffer; // floating point representation of audio samples
    private double dBlevel; // current signal level

    /** Construct an AudioSignal that may contain up to "frameSize" samples.
        * @param frameSize the number of samples in one audio frame */
    public AudioSignal(int frameSize) {
        AudioFormat audioFormat = new AudioFormat(8000.0f, 8, 1, true, true);

        try {
            SourceDataLine sourceDataLine = AudioSystem.getSourceDataLine(audioFormat);
        } catch (LineUnavailableException e) {
            throw new RuntimeException(e);
        }

        sampleBuffer = new double[frameSize];
    }

    /** Sets the content of this signal from another signal.
        * @param other other.length must not be lower than the length of this signal. */
    public void setFrom(AudioSignal other) {
        this.dBlevel = other.dBlevel;
        this.sampleBuffer = other.sampleBuffer;
    }

    /** Fills the buffer content from the given input. Byte's are converted on the fly to double's.
        * @return false if at end of stream */
    public boolean recordFrom(TargetDataLine audioInput) {
        byte[] byteBuffer = new byte[sampleBuffer.length*2]; // 16 bit samples
        if (audioInput.read(byteBuffer, 0, byteBuffer.length)==-1) return false;
        for (int i=0; i<sampleBuffer.length; i++)
            sampleBuffer[i] = ((byteBuffer[2*i]<<8)+byteBuffer[2*i+1]) / 32768.0; // big endian

        // ... TODO-DONE : dBlevel = update signal level in dB here ...

        // Convert byte values to double values between -1.0 and 1.0
        double[] samples = new double[sampleBuffer.length];
        for (int i = 0; i < sampleBuffer.length; i++) {
            samples[i] = (sampleBuffer[i] / 127.0);
        }

        // Calculate the root mean square (RMS) value
        double sum = 0.0;
        for (double sample : samples) {
            sum += sample * sample;
        }
        double rms = Math.sqrt(sum / sampleBuffer.length);

        // Convert the RMS value to dBFS (dB relative to full scale)
        double db = 20 * Math.log10(rms);
        this.dBlevel = db;

        return true;
    }

    public static byte[] convertDoublesToBytes(double[] doubles) {
        byte[] bytes = new byte[doubles.length];
        for (int i = 0; i < doubles.length; i++) {
            // Scale the double value to the range of bytes (-128 to 127)
            double scaledValue = (doubles[i] + 1.0) * 127.0;
            // Quantize (round) the scaled value to the nearest integer
            bytes[i] = (byte) Math.round(scaledValue);
        }
        return bytes;
    }

    /** Plays the buffer content to the given output.
        * @return false if at end of stream */
    public boolean playTo(SourceDataLine audioOutput) {

        // Write the audio data to the SourceDataLine
        audioOutput.write(convertDoublesToBytes(this.sampleBuffer), 0, this.sampleBuffer.length);

        // Block until all data is played
        audioOutput.drain();

        // Close the SourceDataLine
        audioOutput.close();

        return true;
    }
            // your job: add getters and setters ...
            // double getSample(int i)
            // void setSample(int i, double value)
            // double getdBLevel()
            // int getFrameSize()
            // Can be implemented much later: Complex[] computeFFT()

}