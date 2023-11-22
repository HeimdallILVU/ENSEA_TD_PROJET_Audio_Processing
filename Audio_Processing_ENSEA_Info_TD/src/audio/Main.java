package audio;

import javax.sound.sampled.*;

public class Main {


    public static void main(String[] args) {
        try {
            AudioSignal.main(args);
        } catch (LineUnavailableException e) {
            throw new RuntimeException(e);
        }
    }
}