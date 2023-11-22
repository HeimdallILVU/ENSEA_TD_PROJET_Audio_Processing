package audio;

import javax.sound.sampled.AudioFormat;
import javax.sound.sampled.LineUnavailableException;
import javax.sound.sampled.SourceDataLine;
import javax.sound.sampled.TargetDataLine;

/** The main audio processing class, implemented as a Runnable so
    * as to be run in a separated execution Thread. */
public class AudioProcessor implements Runnable {
    private AudioSignal inputSignal, outputSignal;
    private TargetDataLine audioInput;
    private SourceDataLine audioOutput;
    private boolean isThreadRunning; // makes it possible to "terminate" thread

    /**
     * Creates an AudioProcessor that takes input from the given TargetDataLine, and plays back
     * to the given SourceDataLine.
     *
     * @param audioInput TargetDataLine (Microphone line)
     * @param audioOutput OuputDataLine (Speaker line)
     * @param frameSize the size of the audio buffer. The shorter, the lower the latency.
     */
    public AudioProcessor(TargetDataLine audioInput, SourceDataLine audioOutput, int frameSize) {
        inputSignal = new AudioSignal(frameSize);
        outputSignal = new AudioSignal(inputSignal);
        this.audioInput = audioInput;
        this.audioOutput = audioOutput;
    }


    /**
     * Audio processing thread code. Basically an infinite loop that continuously fills the sample
     * buffer with audio data fed by a TargetDataLine and then applies some audio effect, if any,
     * and finally copies data back to a SourceDataLine.
     */
    @Override
    public void run() {
        isThreadRunning = true;
        while (isThreadRunning) {
            inputSignal.recordFrom(audioInput);
            // your job: copy inputSignal to outputSignal with some audio effect

            outputSignal.playTo(audioOutput);
        }
    }

    /**
     * Tells the thread loop to break as soon as possible. This is an asynchronous process.
     */
    public void terminateAudioThread() {
        this.isThreadRunning = false;
    }

    // todo here: all getters and setters


    /* an example of a possible test code */
    public static void main(String[] args) {
        TargetDataLine inLine = AudioIO.obtainAudioInput("Default Audio Device", 16000);
        SourceDataLine outLine = AudioIO.obtainAudioOutput("Default Audio Device", 16000);
        AudioProcessor as = new AudioProcessor(inLine, outLine, 1024);

        try {
            inLine.open();
            inLine.start();
            outLine.open();
            outLine.start();
        } catch (LineUnavailableException e) {
            throw new RuntimeException(e);
        }

        new Thread(as).start();

        System.out.println("A new thread has been created!");
    }
}
