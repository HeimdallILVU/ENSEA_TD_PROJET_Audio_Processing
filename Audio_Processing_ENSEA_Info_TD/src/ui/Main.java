package ui;

import audio.AudioIO;
import audio.AudioProcessor;
import audio.AudioSignal;
import javafx.application.Application;
import javafx.scene.Group;
import javafx.scene.Node;
import javafx.scene.Scene;
import javafx.scene.control.*;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.control.TextField;
import javafx.scene.layout.BorderPane;
import javafx.scene.layout.HBox;
import javafx.stage.Stage;
import org.w3c.dom.Text;

import javax.sound.sampled.*;
import javax.tools.Tool;
import java.awt.*;
import java.util.ArrayList;
import java.util.Arrays;

public class Main extends Application {
    private AudioProcessor audioProcessor;
    private AudioFormat audioFormat;
    private ToolBar toolBar;
    private Node statusBar;
    private Node mainContent;
    @Override
    public void start(Stage primaryStage) {
        try {
            BorderPane root = new BorderPane();

            toolBar = createToolbar();
            root.setTop(toolBar);

            statusBar = createStatusbar();
            root.setBottom(statusBar);

            mainContent = createMainContent();
            root.setCenter(mainContent);

            Scene scene = new Scene(root,1500,800);

            primaryStage.setScene(scene);
            primaryStage.setTitle("The JavaFX audio processor");
            primaryStage.show();

        } catch(Exception e) {
            e.printStackTrace();
        }

        // TODO - Change HardCoded Format to Interface-defined
        this.audioFormat = new AudioFormat(16000.0f, 16, 1, true, true);
        // TODO - Create the audioProcessor

        TargetDataLine audioInput = TargetDataLineFromToolBar(this.toolBar);
        SourceDataLine audioOutput = SourceDataLineFromToolBar(this.toolBar);
        int FrameSize = FrameSizeFromToolBar(this.toolBar);

        this.audioProcessor = new AudioProcessor(audioInput, audioOutput, FrameSize);
    }

    private TargetDataLine TargetDataLineFromToolBar(ToolBar toolBar) {
        ComboBox comboBox = getComboBoxFromToolBar(toolBar, "Input Device : ");
        TargetDataLine targetDataLine;
        try {
            targetDataLine = AudioSystem.getTargetDataLine(this.audioFormat, AudioIO.getMixerInfo((String) comboBox.getValue()));
        } catch (LineUnavailableException e) {
            targetDataLine = null;
            System.out.println("TargetDataLine Unavailable !");
        }
        return targetDataLine;
    }
    private SourceDataLine SourceDataLineFromToolBar(ToolBar toolBar) {
        ComboBox comboBox = getComboBoxFromToolBar(toolBar, "Output Device : ");
        SourceDataLine sourceDataLine;
        try {
            sourceDataLine = AudioSystem.getSourceDataLine(this.audioFormat, AudioIO.getMixerInfo((String) comboBox.getValue()));
        } catch (LineUnavailableException e) {
            sourceDataLine = null;
            System.out.println("SourceDataLine Unavailable !");
        }
        return sourceDataLine;
    }
    private int FrameSizeFromToolBar(ToolBar toolBar) {
        return Integer.valueOf(getTextFieldFromToolBar(toolBar, "FrameSize : ").getText());
    }

    private ComboBox getComboBoxFromToolBar(ToolBar toolBar, String labelText) {
        boolean comboBoxFound = false;
        for (Node item : toolBar.getItems()) {
            if (comboBoxFound && item instanceof ComboBox) {
                return (ComboBox) item;
            }

            if (item instanceof Label && ((Label) item).getText() == labelText) {
                comboBoxFound = true;
            }
        }
        return null;
    }

    private TextField getTextFieldFromToolBar(ToolBar toolBar, String labelText) {
        boolean textFieldFound = false;
        for (Node item : toolBar.getItems()) {
            if (textFieldFound && item instanceof TextField) {
                return (TextField) item;
            }

            if (item instanceof Label && ((Label) item).getText() == labelText) {
                textFieldFound = true;
            }
        }
        return null;
    }

    private void updateProcessor() {
        TargetDataLine audioInput = TargetDataLineFromToolBar(this.toolBar);
        SourceDataLine audioOutput = SourceDataLineFromToolBar(this.toolBar);
        int FrameSize = FrameSizeFromToolBar(this.toolBar);

        this.audioProcessor.setAudioInput(audioInput);
        this.audioProcessor.setAudioOutput(audioOutput);
        this.audioProcessor.setFrameSize(FrameSize);
    }

    private ToolBar createToolbar(){

        Button button = new Button("Start");

        button.setOnAction(event -> {
            if(button.getText() == "Start") {
                updateProcessor();

                try {
                    this.audioProcessor.getAudioOutput().open(this.audioFormat);
                    this.audioProcessor.getAudioOutput().start();
                    this.audioProcessor.getAudioInput().open(this.audioFormat);
                    this.audioProcessor.getAudioInput().start();
                } catch (LineUnavailableException e) {
                    throw new RuntimeException(e);
                }

                System.out.println("Starting the audioProcessor !");
                button.setText("Stop");
                new Thread(audioProcessor).start();

            } else if(button.getText() == "Stop") {

                System.out.println("Stopping the audioProcessor !");
                button.setText("Start");
                audioProcessor.terminateAudioThread();

            }
        });

        ToolBar tb = new ToolBar(button); //, new Label("ceci est un label"), new Separator()

        ComboBox<String> cbInputs = new ComboBox<>();
        ComboBox<String> cbOutputs = new ComboBox<>();
        TextField frameSizeTextField = new TextField("1024");

        Label Input = new Label("Input Device : ");
        Label Output = new Label("Output Device : ");
        Label FrameSize = new Label("FrameSize : ");

        tb.getItems().addAll(new Separator(), Input, cbInputs);
        tb.getItems().addAll(new Separator(), Output, cbOutputs);
        tb.getItems().addAll(new Separator(), FrameSize, frameSizeTextField);

        AudioIO.getAudioMixers().stream().filter(e -> e.getDescription().contains("Capture")).forEach(e -> cbInputs.getItems().add(e.getName()));
        AudioIO.getAudioMixers().stream().filter(e -> e.getDescription().contains("Playback")).forEach(e -> cbOutputs.getItems().add(e.getName()));

        cbInputs.setValue(cbInputs.getItems().stream().findFirst().orElse(null));
        cbOutputs.setValue(cbOutputs.getItems().stream().findFirst().orElse(null));

        return tb;
    }

    private Node createStatusbar(){
        HBox statusbar = new HBox();
        statusbar.getChildren().addAll(new Label("Name:"), new TextField(" Input your desire !"));
        return statusbar;
    }

    private Node createMainContent(){
        Group g = new Group();
        // ici en utilisant g.getChildren().add(...) vous pouvez ajouter tout ´el´ement graphique souhait´e de type Node
        return g;
    }

}
