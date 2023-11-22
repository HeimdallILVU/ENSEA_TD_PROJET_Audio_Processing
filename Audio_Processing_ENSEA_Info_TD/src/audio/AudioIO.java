package audio;

import javax.sound.sampled.*;
import java.util.Arrays;

public class AudioIO {
    /**
     * Displays every audio mixer available on the current system.
     */
    public static void printAudioMixers() {
        System.out.println("Mixers:");
        Arrays.stream(AudioSystem.getMixerInfo())
                .forEach(e -> System.out.println("- name=\"" + e.getName() + "\" description=\"" + e.getDescription() + " by " + e.getVendor() + "\""));
    }

    /**
     * @return a Mixer.Info whose name matches the given string.
     * Example of use: getMixerInfo("Macbook default output")
     */
    public static Mixer.Info getMixerInfo(String mixerName) {
        // see how the use of streams is much more compact than for() loops!
        return Arrays.stream(AudioSystem.getMixerInfo())
                .filter(e -> e.getName().equalsIgnoreCase(mixerName)).findFirst().get();
    }

    /**
     * Return a line that's appropriate for recording sound from a microphone.
     * Example of use:
     * TargetDataLine line = obtainInputLine("USB Audio Device", 8000);
     *
     * @param mixerName a string that matches one of the available mixers.
     * @see .AudioSystem.getMixerInfo() which provides a list of all mixers on your system.
     */
    public static TargetDataLine obtainAudioInput(String mixerName, int sampleRate) {
        // Create the Object that will be returned, return null if failed.
        TargetDataLine targetDataLine;

        // Audio format with sampleRate as sample rate and 16 Bits format
        AudioFormat audioFormat = new AudioFormat((float) sampleRate, 16, 1, true, true);

        Mixer.Info mixerInfo = getMixerInfo(mixerName);

        try {
            targetDataLine = AudioSystem.getTargetDataLine(audioFormat, mixerInfo);
        } catch (LineUnavailableException e) {
            throw new RuntimeException(e);
        }

        return targetDataLine;
    }

    /**
     * Return a line that's appropriate for playing sound to a loudspeaker.
     */
    public static SourceDataLine obtainAudioOutput(String mixerName, int sampleRate) {
        // Create the Object that will be returned, return null if failed.
        SourceDataLine sourceDataLine;

        // Audio format with sampleRate as sample rate and 16 Bits format
        AudioFormat audioFormat = new AudioFormat((float) sampleRate, 16, 1, true, true);

        Mixer.Info mixerInfo = getMixerInfo(mixerName);

        try {
            sourceDataLine = AudioSystem.getSourceDataLine(audioFormat, mixerInfo);
        } catch (LineUnavailableException e) {
            throw new RuntimeException(e);
        }

        return sourceDataLine;
    }

    public static void main(String[] args) {

        int samplingRate = 8000;

        // List of Audio Mixers
        printAudioMixers();

        // Trying to get my Microphone
        System.out.println(getMixerInfo("Headset Microphone (Realtek(R) "));
        System.out.println(obtainAudioInput("Headset Microphone (Realtek(R) ", samplingRate));

        // Trying to get my Headset speaker
        System.out.println(getMixerInfo("Headphone (Realtek(R) Audio)"));
        System.out.println(obtainAudioOutput("Headphone (Realtek(R) Audio)", samplingRate));
    }
}
