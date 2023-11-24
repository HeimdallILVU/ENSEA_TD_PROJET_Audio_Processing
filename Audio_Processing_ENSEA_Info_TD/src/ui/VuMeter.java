package ui;

import audio.AudioSignal;
import javafx.animation.AnimationTimer;
import javafx.scene.canvas.Canvas;
import javafx.scene.canvas.GraphicsContext;
import javafx.scene.paint.Color;

public class VuMeter extends Canvas {
    private double signalLevel; // Should be between 0 and 1
    private AudioSignal audioSignal;
    private double factor;
    private double maxWidth;
    private double maxHeight;

    // Define threshold values
    private static final double GREEN_THRESHOLD = 0.5;
    private static final double ORANGE_THRESHOLD = 0.8;

    public VuMeter(double width, double height, AudioSignal audioSignal) {
        super(width, height);
        this.maxWidth = width;
        this.maxHeight = height;

        this.audioSignal = audioSignal;

        this.factor = 0.9;
        this.signalLevel = 0.5; // Default signal level



        // Call update method in an AnimationTimer to continuously update the VuMeter
        new AnimationTimer() {
            @Override
            public void handle(long now) {
                update();
            }
        }.start();
    }

    public void update() {
        GraphicsContext gc = getGraphicsContext2D();


        this.signalLevel = Math.max(0, Math.min(1, Math.abs((this.audioSignal.getdBlevel() + 45) / 40)  * factor));
        double rectHeight = signalLevel * maxHeight;
        System.out.println(this.audioSignal.getdBlevel() + " : " + this.signalLevel);


        // Clear the canvas
        gc.clearRect(0, 0, getWidth(), getHeight());

        // Draw the background contour
        drawContour(gc);

        // Draw the threshold lines
        drawThresholdLine(gc, GREEN_THRESHOLD, Color.GREEN);
        drawThresholdLine(gc, ORANGE_THRESHOLD, Color.ORANGE);

        // Set color based on the signal level
        if (signalLevel < GREEN_THRESHOLD) {
            gc.setFill(Color.GREEN);
        } else if (signalLevel < ORANGE_THRESHOLD) {
            gc.setFill(Color.ORANGE);
        } else {
            gc.setFill(Color.RED);
        }

        // Draw the stylized VuMeter rectangle
        gc.fillRect(10, maxHeight - rectHeight, maxWidth - 20, rectHeight - 10);
    }

    private void drawThresholdLine(GraphicsContext gc, double threshold, Color color) {
        double thresholdY = threshold * maxHeight;
        gc.setStroke(color);
        gc.setLineWidth(2.0);
        gc.strokeLine(10, maxHeight - thresholdY, maxWidth - 10, maxHeight - thresholdY);
    }

    private void drawContour(GraphicsContext gc) {
        gc.setStroke(Color.BLACK);
        gc.setLineWidth(2.0);

        // Draw the outer rectangle
        gc.strokeRect(5, 5, maxWidth - 10, maxHeight - 10);

        // Draw the inner lines for the stylized appearance
        for (int i = 1; i <= 4; i++) {
            double y = i * (maxHeight - 20) / 4;
            gc.strokeLine(15, y, maxWidth - 15, y);
        }
    }
}
