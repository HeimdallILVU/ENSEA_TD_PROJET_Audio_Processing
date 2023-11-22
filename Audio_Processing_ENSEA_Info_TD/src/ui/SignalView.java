package ui;

import audio.AudioSignal;
import javafx.scene.chart.Axis;
import javafx.scene.chart.LineChart;

public class SignalView extends LineChart<Number, Number> {
    public SignalView(Axis<Number> axis, Axis<Number> axis1) {
        super(axis, axis1);
    }

    public void updateData(AudioSignal audioSignal) {

    }



}
