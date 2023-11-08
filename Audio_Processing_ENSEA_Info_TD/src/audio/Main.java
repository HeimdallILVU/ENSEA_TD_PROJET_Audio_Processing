package audio;


import javax.sound.sampled.*;

public class Main {
    public static void main(String[] args) throws LineUnavailableException {
        // Audio format with 8000Hz sample rate
        AudioFormat audioFormat = new AudioFormat(8000.0f, 8, 1, true, true);

        SourceDataLine sourceDataLine = AudioSystem.getSourceDataLine(audioFormat);

        // Get the Line.Info for the SourceDataLine
        Line.Info lineInfo = sourceDataLine.getLineInfo();

        /*
        if (lineInfo instanceof DataLine.Info) {
            DataLine.Info dataLineInfo = (DataLine.Info) lineInfo;
            System.out.println("Line Info: " + dataLineInfo.toString());
            System.out.println("Supported SourceDataLine formats:");
            AudioFormat[] formats = dataLineInfo.getFormats();
            for (AudioFormat format : formats) {
                System.out.println(format.toString());
            }
        } else {
            System.out.println("Not a DataLine.Info");
        }*/


        sourceDataLine.open(audioFormat);
        sourceDataLine.start();

        // Create a buffer for the audio data
        int bufferSize = 8000; // 1 second of audio
        byte[] buffer = new byte[bufferSize];

        // Generate a simple 8000Hz sine wave
        for (int i = 0; i < bufferSize; i++) {
            double angle = 2.0 * Math.PI * i / 8000.0;
            byte sample = (byte) (Math.sin(angle) * 127.0);
            buffer[i] = sample;
        }

        // Write the audio data to the SourceDataLine
        sourceDataLine.write(buffer, 0, bufferSize);

        // Block until all data is played
        sourceDataLine.drain();

        // Close the SourceDataLine
        sourceDataLine.close();
    }
}
