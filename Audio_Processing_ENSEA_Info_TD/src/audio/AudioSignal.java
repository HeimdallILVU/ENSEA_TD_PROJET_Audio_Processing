package audio;

import javax.sound.sampled.*;

public class AudioSignal {

    private double[] sampleBuffer; // floating point representation of audio samples
    private double dBlevel; // current signal level

    /** Construct an AudioSignal that may contain up to "frameSize" samples.
        * @param frameSize the number of samples in one audio frame */
    public AudioSignal(int frameSize) {
        sampleBuffer = new double[frameSize];
    }

    public AudioSignal(AudioSignal other) {
        this.sampleBuffer = other.sampleBuffer;
        this.dBlevel = other.dBlevel;
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

        // Calculate the root-mean-square (RMS) value
        double sum = 0.0;
        for (double sample : sampleBuffer) {
            sum += sample * sample;
        }
        double rms = Math.sqrt(sum / sampleBuffer.length);

        // Convert the RMS value to dBFS (dB relative to full scale)
        this.dBlevel = 20 * Math.log10(rms);

        return true;
    }

    /** Convert doubles into bytes for 16 and 8 bits audioFormats
     * @return byte array of a double array*/
    public byte[] convertDoublesToBytes(double[] doubles, int SizeInBits) {
        if (SizeInBits == 16) {
            byte[] bytes = new byte[doubles.length * 2];
            for (int i = 0; i < doubles.length; i++) {
                // Scale the double value to the range of bytes (-32768 to 32767)
                short scaledValue = (short) (doubles[i] * Short.MAX_VALUE);

                // 16 bit audio format handling
                bytes[2 * i] = (byte) ((scaledValue >> 8) & 0xFF);
                bytes[2 * i + 1] = (byte) (scaledValue & 0xFF);

            }
            return bytes;
        }

        if (SizeInBits == 8) {
            byte[] bytes = new byte[doubles.length];
            for (int i = 0; i < doubles.length; i++) {
                // Scale the double value to the range of bytes (-32768 to 32767)
                short scaledValue = (short) (doubles[i] * 127);

                // 8 bit audio format handling
                bytes[i] = (byte) scaledValue;

            }
            return bytes;
        }

        throw new RuntimeException("Sample Size in Bits not supported");
    }

    /** Plays the buffer content to the given output.
        * @return false if at end of stream */
    public boolean playTo(SourceDataLine audioOutput) {

        // Start the sourceDataLine
        audioOutput.start();

        // Write the audio data to the SourceDataLine
        audioOutput.write(convertDoublesToBytes(this.sampleBuffer, audioOutput.getFormat().getSampleSizeInBits()), 0, this.sampleBuffer.length * 2);

        // Block until all data is played
        audioOutput.drain();

        // Close the SourceDataLine
        audioOutput.close();

        return true;
    }

    /** Plays the buffer content to the given output.
     * @param continuous if set to True, will not close the audioOutput DataLine
     * @return false if at end of stream */
    public boolean playTo(SourceDataLine audioOutput, boolean continuous) {

        // Start the sourceDataLine
        if(!continuous) audioOutput.start();

        // Write the audio data to the SourceDataLine
        audioOutput.write(convertDoublesToBytes(this.sampleBuffer, audioOutput.getFormat().getSampleSizeInBits()), 0, this.sampleBuffer.length * 2);

        // Block until all data is played
        if(!continuous) audioOutput.drain();

        // Close the SourceDataLine
        if(!continuous) audioOutput.close();

        return true;
    }

    // TODO Can be implemented much later: Complex[] computeFFT()


    public double getSample(int i) {
        return sampleBuffer[i];
    }

    public void setSample(int i, double value) {
        sampleBuffer[i] = value;
    }

    public double getdBlevel() {
        return dBlevel;
    }

    public int getFrameSize() {
        return sampleBuffer.length;
    }

    public void setFrameSize(int FrameSize) {
        this.sampleBuffer = new double[FrameSize];
    }

    public double[] getSampleBuffer() {
        return sampleBuffer;
    }


    public void playTestSin(SourceDataLine sourceDataLine) {
        if(sourceDataLine.getFormat().getSampleSizeInBits() == 16) {
            // Create a buffer for the audio data
            int bufferSize = this.getFrameSize();
            double[] buffer = new double[bufferSize];


            // Generate a simple 1000Hz sine wave
            for (int i = 0; i < bufferSize/2; i++) {
                double angle = 2.0 * Math.PI * 1000 * i / sourceDataLine.getFormat().getSampleRate();
                double sampleValue = Math.sin(angle);

                buffer[i] = sampleValue;
            }

            this.sampleBuffer = buffer;

            this.playTo(sourceDataLine);
            sourceDataLine.drain();
            sourceDataLine.close();

        } else {
            throw new RuntimeException("Change Audio format to 16 Bits");
        }
    }

    public void playTestSin(SourceDataLine sourceDataLine, boolean continuous) {
        if(sourceDataLine.getFormat().getSampleSizeInBits() == 16) {
            // Create a buffer for the audio data
            int bufferSize = this.getFrameSize();
            double[] buffer = new double[bufferSize];


            // Generate a simple 1000Hz sine wave
            for (int i = 0; i < bufferSize/2; i++) {
                double angle = 2.0 * Math.PI * 1000 * i / sourceDataLine.getFormat().getSampleRate();
                double sampleValue = Math.sin(angle);

                buffer[i] = sampleValue;
            }

            this.sampleBuffer = buffer;

            this.playTo(sourceDataLine, continuous);

        } else {
            throw new RuntimeException("Change Audio format to 16 Bits");
        }
    }

    /**
     * This function is used to test the functionality of AudioSignal
     * @param args (Empty)
     */
    public static void main(String[] args) throws LineUnavailableException {
        // Audio format with 8000Hz sample rate and 16 Bits format
        AudioFormat audioFormat = new AudioFormat(8000.0f, 16, 1, true, true);

        // Creating two AudioSignal Instances
        AudioSignal audioSignal = new AudioSignal(32000);
        AudioSignal audioSignal2 = new AudioSignal(audioSignal);

        // Defining Input and Output for the Audio
        SourceDataLine sourceDataLine = null;
        TargetDataLine targetDataLine = null;

        // Try and catch of the getDataLines
        try {
            sourceDataLine = AudioSystem.getSourceDataLine(audioFormat);
            targetDataLine = AudioSystem.getTargetDataLine(audioFormat);
        } catch (LineUnavailableException e) {
            throw new RuntimeException(e);
        }

        //Checking the gotten lines

        System.out.println(sourceDataLine.getLineInfo());
        System.out.println(targetDataLine.getLineInfo());

        // open and start targetDataLine
        try {
            targetDataLine.open(audioFormat);
            targetDataLine.start();
        } catch (LineUnavailableException e) {
            throw new RuntimeException(e);
        }


        // Recording 4 seconds of audio
        System.out.println("Start of Recording !");
        audioSignal.recordFrom(targetDataLine); System.out.println("End of Recording !");

        // Playing to sourceDataLine
        System.out.println("Start of Playing to sourceDataLine !"); sourceDataLine.open(audioFormat); sourceDataLine.start();
        audioSignal.playTo(sourceDataLine); System.out.println("End of Playing to external sourceDataLine !");

        // Copying of signal
        audioSignal2.setFrom(audioSignal);

        // Playing to external sourceDataLine to test correct copy of audioSignal
        System.out.println("Start of Playing to sourceDataLine of audioSignal2 !"); sourceDataLine.open(audioFormat); sourceDataLine.start();
        audioSignal2.playTo(sourceDataLine); System.out.println("End of Playing to external sourceDataLine of audioSignal2 !");

        // Playing to external sourceDataLine to test correct copy of audioSignal
        System.out.println("Start of Playing test Sin fun !"); sourceDataLine.open(audioFormat); sourceDataLine.start();
        audioSignal.playTestSin(sourceDataLine); System.out.println("End of Playing test Sin fun !");

    }

}