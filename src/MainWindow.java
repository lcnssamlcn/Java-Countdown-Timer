import javafx.scene.layout.VBox;
import javafx.scene.layout.HBox;
import javafx.scene.control.Label;
import javafx.scene.control.TextField;
import javafx.scene.control.TextFormatter;
import javafx.scene.control.Button;
import javafx.scene.control.Alert;
import javafx.scene.control.ButtonType;
import javafx.scene.text.Font;
import javafx.geometry.Pos;
import javafx.geometry.Insets;

public class MainWindow extends VBox {
    // GUI
    private HBox timerMainThreadContainer;
    private Label timeSeparator[];
    private TextField hrField;
    private TextField minField;
    private TextField secField;

    private HBox buttonContainer;
    private Button startPauseButton;
    private Button stopButton;

    // internal
    private Timer timer;
    private Thread timerMainThread;
    private Thread timerPauseThread;
    private Thread timerResumeThread;

    public MainWindow() {
        this.setPadding(new Insets(15, 15, 15, 15));
        this.setSpacing(10);
        this.init();
        this.getChildren().addAll(timerMainThreadContainer, buttonContainer);
        this.setAlignment(Pos.CENTER);

        this.timer = new Timer(this.hrField, this.minField, this.secField);
    }

    // create a TextFormatter only accepting integer from [start, end) with specified number of digits
    private TextFormatter numericFilter(int start, int end, int numOfDigits) {
        return new TextFormatter<>(c -> {
            if (c.getText().matches("[^0-9]") || c.getControlNewText().length() > numOfDigits) {
                c.setText("");  // remain unchanged
                return c;
            }

            if (c.isContentChange()) {
                try {
                    Integer integer = new Integer(c.getControlNewText());
                    if (integer < start || integer >= end)
                        c.setText("");
                }
                catch (NumberFormatException e) {
                    c.setText("");
                }
            }

            return c;
        });
    }

    // init main GUI window
    private void init() {
        timerMainThreadContainer = new HBox(10);

        timeSeparator = new Label[] { new Label(":"), new Label(":") };
        timeSeparator[0].setFont(new Font(16)); 
        timeSeparator[1].setFont(new Font(16));
        hrField = new TextField("00");
        hrField.setFont(new Font(20));
        hrField.setAlignment(Pos.CENTER);
        hrField.setPrefColumnCount(6);
        hrField.setTextFormatter(numericFilter(0, 1000, 3));  // only accept [0, 999] and three or less digits
        minField = new TextField("00");
        minField.setFont(new Font(20));
        minField.setAlignment(Pos.CENTER);
        minField.setPrefColumnCount(6);
        minField.setTextFormatter(numericFilter(0, 60, 2));
        secField = new TextField("00");
        secField.setFont(new Font(20));
        secField.setAlignment(Pos.CENTER);
        secField.setPrefColumnCount(6);
        secField.setTextFormatter(numericFilter(0, 60, 2));

        timerMainThreadContainer.getChildren().addAll(hrField, timeSeparator[0], minField, timeSeparator[1], secField);

        timerMainThreadContainer.setAlignment(Pos.CENTER);


        buttonContainer = new HBox(40);

        startPauseButton = new Button("Start");
        startPauseButton.setFont(new Font(18));
        startPauseButton.setOnAction(e -> handleStartPause());
        stopButton = new Button("Stop");
        stopButton.setFont(new Font(18));
        stopButton.setOnAction(e -> handleStop());

        buttonContainer.getChildren().addAll(startPauseButton, stopButton);

        buttonContainer.setAlignment(Pos.CENTER);
    }

    // Start/Resume/Pause Button
    private void handleStartPause() {
        // System.out.println("paused: " + timer.wasPaused() + ", stopped: " + timer.isStopped());

        if (timer.isStopped()) {
            this.handleStart();
        }
        else if (timer.wasPaused()) {
            this.handleResume();
        }
        else {
            if (timer.timesUp())
                this.handleStop();
            else
                this.handlePause();
        }

        // System.out.println("paused: " + timer.wasPaused() + ", stopped: " + timer.isStopped());
    }

    // Start Button
    private void handleStart() {
        if (hrField.getText().length() == 0 || minField.getText().length() == 0 || secField.getText().length() == 0) {
            Alert alertBox = new Alert(Alert.AlertType.ERROR, "hour/minute/second field cannot leave blank", ButtonType.OK);
            alertBox.showAndWait();
            return;
        }

        // should not throw NumberFormatException since it is filtered by numericFilter()
        timer.setTime(new Integer(hrField.getText()), new Integer(minField.getText()), new Integer(secField.getText()));

        hrField.setEditable(false); minField.setEditable(false); secField.setEditable(false);

        // if (timerMainThread.getState() == Thread.State.TERMINATED)
        timerMainThread = new Thread(timer);  // renew the dead thread

        timer.start();
        timerMainThread.start();

        // switch to Pause
        startPauseButton.setText("Pause");
    }

    // Resume Button
    private void handleResume() {
        // if (timerResumeThread.getState() == Thread.State.TERMINATED)
        timerResumeThread = new Thread(() -> timer.resume());
        timerResumeThread.start();

        // switch to Pause
        startPauseButton.setText("Pause");
    }

    // Pause Button
    private void handlePause() {
        // if (timerPauseThread.getState() == Thread.State.TERMINATED)
        timerPauseThread = new Thread(() -> timer.pause());
        timerPauseThread.start();

        // wait for Resume
        startPauseButton.setText("Resume");
    }

    // Stop Button: reset the timerMainThread
    private void handleStop() {
        this.interruptTimer();
        timer.reset();

        startPauseButton.setText("Start");
    }

    // send interrupt signal (InterruptedException) to all threads
    public void interruptTimer() {
        timer.stop();

        if (timerMainThread != null && timerMainThread.isAlive())
            timerMainThread.interrupt();
    }
}
