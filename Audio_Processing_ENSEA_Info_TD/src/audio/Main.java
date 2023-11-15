package audio;

import javax.sound.sampled.*;

public class Main {
    /**
     * This main function is used to test the functionnality of AudioSignal
     * @param none
     */
    public static void main(String[] args) {
        // Audio format with 8000Hz sample rate and 16 Bits format
        AudioFormat audioFormat = new AudioFormat(8000.0f, 16, 1, true, true);

        // Creating two AudioSignal Instances
        AudioSignal audioSignal = new AudioSignal(32000, audioFormat);
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

        // Playing to audioSignal.sourceDataLine
        System.out.println("Start of Playing to audioSignal.sourceDataLine !");
        audioSignal.play(); System.out.println("End of Playing to audioSignal.sourceDataLine !");

        // Playing to external sourceDataLine
        System.out.println("Start of Playing to external sourceDataLine !");
        audioSignal.playTo(sourceDataLine); System.out.println("End of Playing to external sourceDataLine !");

        // Copying of signal
        audioSignal2.setFrom(audioSignal);

        // Playing to external sourceDataLine to test correct copy of audioSignal
        System.out.println("Start of Playing to external sourceDataLine of audioSignal2 !");
        audioSignal2.playTo(sourceDataLine); System.out.println("End of Playing to external sourceDataLine of audioSignal2 !");

    }
}