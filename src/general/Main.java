package general;

import general.Controller.IntroCtrl;
import javafx.application.Application;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.image.Image;
import javafx.stage.Stage;


/**
 * Main Class + Main View
 */
public class Main extends Application {

    @Override
    public void start(Stage primaryStage) throws Exception{

        FXMLLoader fxmlLoader = new FXMLLoader(getClass().getResource("View/IntroView.fxml"));
        Parent root = fxmlLoader.load();
        root.setStyle("-fx-background-color: #2D393C;");

        Scene scene = new Scene(root, 530, 304);
        scene.getStylesheets().add(getClass().getResource("/fontstyle.css").toExternalForm());

        primaryStage.setTitle("PacMan");
        primaryStage.setScene(scene);
        primaryStage.setResizable(false);


        Image icon = new Image(getClass().getResourceAsStream("/images/icon.svg"));
        primaryStage.getIcons().add(icon);

        //Set primaryStage to the controller of the View
        IntroCtrl ctrl = fxmlLoader.getController();
        ctrl.setPrimaryStage(primaryStage);

        primaryStage.show();
    }

    public static void main(String[] args) {
        launch(args);
    }
}
