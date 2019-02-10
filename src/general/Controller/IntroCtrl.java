package general.Controller;

import general.Repository.MapRepository;
import general.Repository.UserRepoTasks.FindAllTask;
import general.Repository.UserRepository;
import javafx.application.Platform;
import javafx.beans.binding.BooleanBinding;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.concurrent.Task;
import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import javafx.scene.control.*;
import javafx.scene.image.ImageView;
import javafx.scene.text.Font;
import javafx.stage.Stage;

import java.io.IOException;
import java.net.URL;
import java.util.ResourceBundle;
import java.util.concurrent.*;

/**
 * Controller for Intro View
 */
public class IntroCtrl implements Initializable {
    private PacManGridCtrl window;

    private Stage primaryStage;

    private ExecutorService databaseExecutor;
    private Future databaseSetupFuture;
    private UserRepository usrRepo;

    @FXML
    private ImageView warnImg;

    @FXML
    private Label nrGhostsLbl;

    @FXML
    private Button startBtn;

    @FXML
    private Label usernameLbl;


    @FXML
    private ProgressIndicator databaseActivityIndicator;

    @FXML
    private TextField usrNameTextField;

    @FXML
    private ComboBox<String> nrGhostCombo;


    @Override
    public void initialize(URL location, ResourceBundle resources) {
        //Connect to Database and store all users to local List
        usrRepo = new UserRepository();
        databaseExecutor = Executors.newFixedThreadPool(
                1,
                new UserRepository.DatabaseThreadFactory()
        );
        FindAllTask findAllTask = new FindAllTask(usrRepo);
        awaitLoadingDB(findAllTask);
        findAllTask.setOnSucceeded(t -> showUIControls());
        databaseSetupFuture = databaseExecutor.submit(findAllTask);

        //Get Labyrinth Data
        MapRepository repo = new MapRepository();


        //Set Controls Initial Values
        warnImg.setVisible(false);
        startBtn.setStyle("-fx-background-color: #1b7d7e; -fx-text-fill: #2D393C; -fx-font-weight: bold; -fx-font-size: 16pt; -fx-font-family: \"SF Pixelate\";");
        usrNameTextField.setStyle("-fx-font-size: 14pt; -fx-font-family: \"SF Pixelate\";");
        ObservableList<String> items1 = FXCollections.observableArrayList("1", "2", "3");
        nrGhostCombo.setItems(items1);
        nrGhostCombo.getSelectionModel().selectFirst();

        //validate username input
        usrNameTextField.textProperty().addListener((observable) -> {
            if (usrNameTextField.getText().trim().isEmpty()) {
                warnImg.setVisible(true);
                final Tooltip tooltip = new Tooltip();
                tooltip.setText("Username must not be empty!");
                usrNameTextField.setTooltip(tooltip);
            }
            else if (usrNameTextField.getText().length() > 9) {
                warnImg.setVisible(true);
                final Tooltip tooltip = new Tooltip();
                tooltip.setText("Username must not be longer than 9 chars!");
                usrNameTextField.setTooltip(tooltip);
            }
            else {
                warnImg.setVisible(false);
            }
        });

        //Bind start button with text field (if username isn't valid -> disable start button)
        BooleanBinding booleanBind = new BooleanBinding() {
            {
                super.bind(usrNameTextField.textProperty());
            }
            @Override
            protected boolean computeValue() {
                return (usrNameTextField.getText().isEmpty() || usrNameTextField.getText().length() > 9);
            }
        };
        startBtn.disableProperty().bind(booleanBind);
    }


    /**
     * Setter for Primary Stage of the controller
     * @param primaryStage
     */
    public void setPrimaryStage(Stage primaryStage) {
        this.primaryStage = primaryStage;

        //Override Application Shutdown
        this.primaryStage.setOnCloseRequest(t -> {
            //Handle the Database connection
            if (databaseExecutor != null) {
                databaseExecutor.shutdown();
                try {
                    if (!databaseExecutor.awaitTermination(3, TimeUnit.SECONDS)) {
                        System.out.println("Database execution thread timed out after 3 seconds rather than shutting down cleanly.");
                    }
                } catch (InterruptedException e) {
                    System.err.println(e.getMessage());
                }
            }

            Platform.exit();
            System.exit(0);
        });
    }


    /**
     * Reactivate UI Controls after connection
     */
    private void showUIControls() {
        usernameLbl.setVisible(true);
        usrNameTextField.setVisible(true);
        nrGhostsLbl.setVisible(true);
        nrGhostCombo.setVisible(true);
    }


    /**
     * Hide UI controls while getting connected to DB
     * Show ProgressIndicator
     * @param runningTask
     */
    private void awaitLoadingDB(Task runningTask) {
        usernameLbl.setVisible(false);
        usrNameTextField.setVisible(false);
        nrGhostsLbl.setVisible(false);
        nrGhostCombo.setVisible(false);
        startBtn.disableProperty().bind(runningTask.runningProperty());

        databaseActivityIndicator.visibleProperty().bind(runningTask.runningProperty());
        databaseActivityIndicator.progressProperty().bind(runningTask.progressProperty());
    }

    /**
     * On Start Button Click:
     * Hide the current Window
     * Show the new Window
     */
    public void onStartClicked() {
        //Await for FindAll Task From Database
        try {
            databaseSetupFuture.get();
        } catch (InterruptedException | ExecutionException e) {
            e.printStackTrace();
        }

        try {
            window = new PacManGridCtrl(primaryStage, usrNameTextField.getText(), this.usrRepo);

            window.setSettingsNrGhosts(Integer.parseInt(nrGhostCombo.getSelectionModel().getSelectedItem()));

            window.start();
            primaryStage.close();
        }
        catch (IOException e) {
            e.printStackTrace();
        }
    }
}
