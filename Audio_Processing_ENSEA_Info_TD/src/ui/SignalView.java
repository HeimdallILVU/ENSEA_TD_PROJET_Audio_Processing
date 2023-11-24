package ui;

import audio.AudioSignal;
import javafx.animation.AnimationTimer;
import javafx.scene.chart.LineChart;
import javafx.scene.chart.NumberAxis;
import javafx.scene.chart.XYChart;

import java.util.Arrays;
import java.util.concurrent.atomic.AtomicInteger;

public class SignalView extends LineChart<Number, Number>{
    private XYChart.Series<Number, Number> series;
    private AudioSignal audioSignal;
    public SignalView(AudioSignal audioSignal, String title) {
        super(new NumberAxis(), new NumberAxis());
        this.audioSignal = audioSignal;

        // Set chart properties
        setTitle(title);
        getXAxis().setLabel("Index");
        getYAxis().setLabel("Value");

        // Disable symbol creation for the LineChart
        this.setCreateSymbols(false);

        getYAxis().setAutoRanging(false); // Turn off auto-ranging
        ((NumberAxis) getYAxis()).setLowerBound(-1);
        ((NumberAxis) getYAxis()).setUpperBound(1);

        // Create data series
        series = new XYChart.Series<>();
        series.setName("Data Series");

        // Add data points to the series
        {
            AtomicInteger i = new AtomicInteger(0);
            Arrays.stream(audioSignal.getSampleBuffer()).forEach(e -> {
                series.getData().add(new XYChart.Data<>(i, e));
                i.getAndIncrement();
            });
        }

        // Add the series to the chart
        getData().add(series);


        // Set up an AnimationTimer to periodically update the chart
        new AnimationTimer() {
            @Override
            public void handle(long now) {
                updateData(audioSignal);
            }
        }.start();
    }

    public void updateData(AudioSignal audioSignal) {
        // Clear existing data
        series.getData().clear();

        // Add data points to the series
        {
            double[] sampleBuffer = audioSignal.getSampleBuffer();
            for (int i = 0; i < sampleBuffer.length; i++) {
                series.getData().add(new XYChart.Data<>(i, sampleBuffer[i]));
            }
        }
    }


}
