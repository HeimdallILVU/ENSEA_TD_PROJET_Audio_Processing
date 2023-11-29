package ui;

import audio.AudioIO;
import audio.AudioProcessor;
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
import javafx.scene.layout.VBox;
import javafx.stage.Stage;
import javax.sound.sampled.*;


public class Main extends Application {
    private AudioProcessor audioProcessor;
    private AudioFormat audioFormat;
    private ToolBar toolBar;
    private Node statusBar;
    private Node mainContent;
    private BorderPane root;

    /**
     * Start function of the application
     */
    @Override
    public void start(Stage primaryStage) {

        try {
            root = new BorderPane();

            toolBar = createToolbar();
            root.setTop(toolBar);

        } catch(Exception e) {
            e.printStackTrace();
        }

        // TODO - Change HardCoded Format to Interface-defined
        this.audioFormat = new AudioFormat(44000.0f, 16, 1, true, true);

        TargetDataLine audioInput = TargetDataLineFromToolBar(this.toolBar);
        SourceDataLine audioOutput = SourceDataLineFromToolBar(this.toolBar);
        int FrameSize = FrameSizeFromToolBar(this.toolBar);

        this.audioProcessor = new AudioProcessor(audioInput, audioOutput, FrameSize);


        try {
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
    }

    /**
     * Update the audioProcessor using the parameter inputted by the user in the toolbar
     */
    private void updateProcessor() {
        TargetDataLine audioInput = TargetDataLineFromToolBar(this.toolBar);
        SourceDataLine audioOutput = SourceDataLineFromToolBar(this.toolBar);
        int FrameSize = FrameSizeFromToolBar(this.toolBar);

        this.audioProcessor.setAudioInput(audioInput);
        this.audioProcessor.setAudioOutput(audioOutput);
        this.audioProcessor.setFrameSize(FrameSize);
    }

    /**
     * Create the toolbar that will later on be display at the top of the window
     */
    private ToolBar createToolbar(){

        // Create the start / stop button
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

        ToolBar tb = new ToolBar(button);

        ComboBox<String> cbInputs = new ComboBox<>();
        ComboBox<String> cbOutputs = new ComboBox<>();
        TextField frameSizeTextField = new TextField("1024");

        Label Input = new Label("Input Device : ");
        Label Output = new Label("Output Device : ");
        Label FrameSize = new Label("FrameSize : ");

        // Adding our Label, ComboBox, Separator to the toolbar for each variable the user can change
        tb.getItems().addAll(new Separator(), Input, cbInputs);
        tb.getItems().addAll(new Separator(), Output, cbOutputs);
        tb.getItems().addAll(new Separator(), FrameSize, frameSizeTextField);

        // Gets all the mixer that are able to Capture or Play Audio
        AudioIO.getAudioMixers().stream().filter(e -> e.getDescription().contains("Capture")).forEach(e -> cbInputs.getItems().add(e.getName()));
        AudioIO.getAudioMixers().stream().filter(e -> e.getDescription().contains("Playback")).forEach(e -> cbOutputs.getItems().add(e.getName()));

        // Automatically define the firsts mixers in the lists as the used mixers
        cbInputs.setValue(cbInputs.getItems().stream().findFirst().orElse(null));
        cbOutputs.setValue(cbOutputs.getItems().stream().findFirst().orElse(null));

        return tb;
    }

    // Vestige
    /**
     * Create the statusbar that is later on displayed at the bottom of the window
     */
    private Node createStatusbar(){
        HBox statusbar = new HBox();
        statusbar.getChildren().addAll(new Label("Name:"), new TextField(" Input your desire !"));
        return statusbar;
    }

    /**
     * Create the main content that is later on displayed in the middle of the window
     */
    private Node createMainContent(){
        SignalView inputSignalView = new SignalView(audioProcessor.getInputSignal(), "Input Signal");
        SignalView outputSignalView = new SignalView(audioProcessor.getOutputSignal(), "Output Signal");
        VuMeter vuMeter = new VuMeter(50, 200, audioProcessor.getInputSignal());
        Spectrogram spectrogram = new Spectrogram(400, 200, audioProcessor.getInputSignal(), audioProcessor.getAudioInput().getFormat().getSampleRate());
        Spectrogram spectrogramZoom = new Spectrogram(400, 200, audioProcessor.getInputSignal(), audioProcessor.getAudioInput().getFormat().getSampleRate());
        spectrogramZoom.setFrequencyRange(20, 4000);


        HBox hbox1 = new HBox(inputSignalView, outputSignalView, vuMeter); // First line of widget
        HBox hbox2 = new HBox(spectrogram, spectrogramZoom); // Second line
        VBox vBox = new VBox(hbox1, hbox2); // Organizing both line in a vertical Box

        Group g = new Group(vBox);
        return g;
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


}
