import javafx.application.Application;
import javafx.application.Platform;
import javafx.stage.Stage;
import javafx.scene.Scene;

public class Main extends Application {
    public static final String TITLE = "Countdown Timer";
    private MainWindow window;

    @Override
    public void start(Stage stage) {
        stage.setTitle(TITLE);
        window = new MainWindow();
        // focus on the root node instead of first text field
        stage.setScene(new Scene(window));
        Platform.runLater(() -> window.requestFocus());

        stage.setResizable(false);

        stage.show();

        stage.setOnCloseRequest(e -> window.interruptTimer());
    }

    public static void main(String args[]) {
        Application.launch(args);
    }
}
