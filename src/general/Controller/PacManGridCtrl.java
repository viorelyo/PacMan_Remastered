package general.Controller;

import general.Model.*;
import general.Repository.MapRepository;
import general.Repository.UserRepoTasks.SaveScoreTask;
import general.Repository.UserRepository;
import javafx.animation.Animation;
import javafx.animation.FadeTransition;
import javafx.application.Platform;
import javafx.event.EventHandler;
import javafx.geometry.Insets;
import javafx.scene.Group;
import javafx.scene.Scene;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.control.ProgressIndicator;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.scene.input.KeyEvent;
import javafx.scene.layout.*;
import javafx.scene.paint.Color;
import javafx.scene.shape.Rectangle;
import javafx.scene.text.Font;
import javafx.scene.text.TextAlignment;
import javafx.stage.Modality;
import javafx.stage.Stage;
import javafx.geometry.Bounds;
import javafx.util.Duration;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.*;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.stream.IntStream;


/**
 * Generates the 10x10 Grid (depending on values from MapRepository)
 */
public class PacManGridCtrl implements StopListener {
    private static final int NUM_CELLS = 10;
    private static final int CELL_SIZE = 50;
    private static final int SIZE = 45;

    private Group board;
    private Stage primaryStage;

    private List<Ghost> allGhosts;
    private PacMan pacMan;
    private Goodie goodie;

    private Stage mainAppWindow;

    private int settingsNrGhosts;
    private int settingsGhostSpeed;
    private int levelNr;

    private Label levelLbl;
    private Label scoreLbl;
    private String userName;
    private int score;

    private UserRepository usrRepo;
    private ExecutorService databaseExecutor;
    private Future databaseSetupFuture;
    private SaveScoreTask saverTask;


    /**
     * Constructor for PacManGrid Controller
     * @param mainAppWindow
     * @param usrName
     * @param usrRepo
     */
    public PacManGridCtrl(Stage mainAppWindow, String usrName, UserRepository usrRepo) {
        this.mainAppWindow = mainAppWindow;
        this.userName = usrName;
        this.score = 0;
        this.usrRepo = usrRepo;
        this.settingsGhostSpeed = 800;
        this.levelNr = 1;
    }


    /**
     * setter for nr of Ghosts
     * @param settingsNrGhosts
     */
    public void setSettingsNrGhosts(int settingsNrGhosts) {
        this.settingsNrGhosts = settingsNrGhosts;
    }


    /**
     * Create Score Pane in the RIGHT Part
     * @param gamingBounds
     * @return
     */
    private Group createScorePane(Bounds gamingBounds) {
        Group scorePane = new Group();

        //Background color of the RIGHT PART
        Rectangle bg = new Rectangle(gamingBounds.getWidth()- 5, 0, 200, gamingBounds.getHeight() - 1.6);
        bg.setFill(Color.web("#2D393C"));
        bg.setStroke(Color.BLACK);
        bg.setStrokeWidth(1.5);

        scorePane.getChildren().add(bg);

        Label userLbl = new Label(userName);
        userLbl.setLayoutX(530);
        userLbl.setLayoutY(50);
        userLbl.setTextFill(Color.web("#D99951"));
        userLbl.setStyle("-fx-font-weight: bold; -fx-font-size: 25pt; -fx-font-family: \"SF Pixelate\";");

        scorePane.getChildren().add(userLbl);

        //Label with logo of Goodies
        Label label = new Label();
        label.setLayoutX(530);
        label.setLayoutY(150);
        Image image = new Image(getClass().getResourceAsStream("/images/bitg2.png"), 50, 50, true, true);
        label.setGraphic(new ImageView(image));

        scorePane.getChildren().add(label);

        //Label with score (auto updated)
        scoreLbl = new Label(String.valueOf(score));
        scoreLbl.setLayoutX(610);
        scoreLbl.setLayoutY(150);
        scoreLbl.setTextFill(Color.web("#BC4F56"));
        scoreLbl.setStyle("-fx-font-weight: bold; -fx-font-size: 30pt; -fx-font-family: \"SF Pixelate\";");

        scorePane.getChildren().add(scoreLbl);

        //Label with levelNr (auto updated)
        Label label1 = new Label("Level: " );
        label1.setLayoutX(550);
        label1.setLayoutY(400);
        label1.setTextFill(Color.web("#cab592"));
        label1.setStyle("-fx-font-weight: bold; -fx-font-size: 18pt; -fx-font-family: \"SF Pixelate\";");

        scorePane.getChildren().add(label1);

        levelLbl = new Label(String.valueOf(levelNr));
        levelLbl.setLayoutX(645);
        levelLbl.setLayoutY(400);
        levelLbl.setTextFill(Color.web("#cab592"));
        levelLbl.setStyle("-fx-font-weight: bold; -fx-font-size: 18pt; -fx-font-family: \"SF Pixelate\";");

        scorePane.getChildren().add(levelLbl);

        return scorePane;
    }


    /**
     * start function of the View (Grid Scene)
     * @throws IOException
     */
    public void start() throws IOException {
        AtomicInteger pacManX = new AtomicInteger();
        AtomicInteger pacManY = new AtomicInteger();

        Group gridGroup = new Group();
        IntStream.range(0,NUM_CELLS).boxed().forEach(i->
            IntStream.range(0,NUM_CELLS).boxed().forEach(j->{
                Rectangle cell = new Rectangle(j * CELL_SIZE, i * CELL_SIZE, CELL_SIZE, CELL_SIZE);
                if (MapRepository.labyrinth.get(i)[j] == 1)
                    cell.setFill(Color.GREY);
                else {
                    cell.setFill(Color.AQUA);
                    //save Pacman's coordinates from CSV File
                    if (MapRepository.labyrinth.get(i)[j] == 2) {
                        pacManX.set(j);
                        pacManY.set(i);
                    }
                }
                cell.setStroke(Color.BLACK);
                cell.setStrokeWidth(1.5);
                gridGroup.getChildren().add(cell);
            })
        );


        board = new Group();
        Bounds gameBounds = gridGroup.getLayoutBounds();
        board.getChildren().add(createScorePane(gameBounds));
        board.getChildren().add(gridGroup);


        //Create the personages
        pacMan = new PacMan(pacManX.get(), pacManY.get());
        this.createGoodie();
        this.setPacMan();
        this.ghostCreation();


        //Main Container(StackPane)
        StackPane root = new StackPane();
        root.getChildren().add(board);


        //Create Scene
        Scene scene = new Scene(root, gameBounds.getWidth() + 200 - 16, gameBounds.getHeight() - 11);
        scene.setOnKeyPressed(keyListener);
        scene.getStylesheets().add(getClass().getResource("/fontstyle.css").toExternalForm());

        //Create the window
        primaryStage = new Stage();
        primaryStage.setTitle("PacMan");
        primaryStage.setScene(scene);
        primaryStage.setResizable(false);

        Image icon = new Image(getClass().getResourceAsStream("/images/icon.svg"));
        primaryStage.getIcons().add(icon);

        primaryStage.show();
        //Override Shutdown Application
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
            //Stop all Threads (Ghosts)
            allGhosts.forEach(x -> x.stop());

            Platform.exit();
            System.exit(0);
        });
    }


    /**
     * set PacMan on the board
     */
    private void setPacMan() {
        this.board.getChildren().add(pacMan.getLabel());
    }


    /**
     * KeyPressed event Handler
     * Move the pacMan on key pressed
     * Check if there is no goodie on same position
     */
    private EventHandler<KeyEvent> keyListener = new EventHandler<KeyEvent>() {
        @Override
        public void handle(KeyEvent event) {
            switch (event.getCode()) {
                case UP:
                    pacMan.moveMe(Directions.UP);
                    break;
                case DOWN:
                    pacMan.moveMe(Directions.DOWN);
                    break;
                case LEFT:
                    pacMan.moveMe(Directions.LEFT);
                    break;
                case RIGHT:
                    pacMan.moveMe(Directions.RIGHT);
                    break;
            }
            checkForGoodie();

            event.consume();
        }
    };


    /**
     * Check if there is no goodie on same position with PacMan
     * If true -> relocate the Goodie and increase the score
     */
    private void checkForGoodie() {
        if (pacMan.X == goodie.X && pacMan.Y == goodie.Y) {
            int[] result = randomizePosition();
            goodie.setPosition(result[0], result[1]);
            this.score++;

            Platform.runLater(() -> {
                scoreLbl.setText(String.valueOf(score));
            });

            if (this.score % 5 == 0) {
                lvlUP();
            }
        }
    }


    /**
     * Filter the free positions of the labyirinth
     * Choose randomly a free position
     */
    private int[] randomizePosition() {
        List<int[]> free = new ArrayList<>();

        IntStream.range(0,NUM_CELLS).boxed().forEach(i->
            IntStream.range(0,NUM_CELLS).boxed().forEach(j -> {
                if (MapRepository.labyrinth.get(i)[j] != 1) {
                            int[] xy = new int[]{j, i};
                            free.add(xy);
                }
            })
        );

        //eliminate those coordinates from "free" that are in the neighbourhood of pacMan
        free.removeIf(v -> ((v[0] == pacMan.X && v[1] == pacMan.Y) || (v[0] == pacMan.X + 1 && v[1] == pacMan.Y) || (v[0] == pacMan.X + 2 && v[1] == pacMan.Y)
                            || (v[0] == pacMan.X - 1 && v[1] == pacMan.Y) || (v[0] == pacMan.X - 2 && v[1] == pacMan.Y)
                            || (v[0] == pacMan.X && v[1] == pacMan.Y + 1) || (v[0] == pacMan.X && v[1] == pacMan.Y + 2)
                            || (v[0] == pacMan.X && v[1] == pacMan.Y - 1) || (v[0] == pacMan.X && v[1] == pacMan.Y - 2)));

        //Choose randomly a free position
        int index = ThreadLocalRandom.current().nextInt(0, free.size());
        return free.get(index);
    }


    /**
     * Create ghosts
     */
    private void ghostCreation() {
        allGhosts = new ArrayList<>();
        for (int i = 0; i < settingsNrGhosts; i++) {
            int[] result = randomizePosition();

            Ghost gh = new Ghost(this, settingsGhostSpeed, result[0], result[1], i);
            this.board.getChildren().add(gh.getLabel());
            gh.start();
            allGhosts.add(gh);
        }
    }


    /**
     * Update score or add new user to DB Asynchronous
     */
    private void modifyDBAsync() {
        databaseExecutor = Executors.newFixedThreadPool(
                1,
                new UserRepository.DatabaseThreadFactory()
        );
        saverTask = new SaveScoreTask(usrRepo, userName, score);
        databaseSetupFuture = databaseExecutor.submit(saverTask);
    }


    /**
     * show modal Window with label game over
     * button restart closes all windows and opens the IntroView
     */
    public void showGameOver() {
        //Create Label
        Label gameOverLbl = new Label("GAME OVER");
        gameOverLbl.setTextFill(Color.web("#CA3449"));
        gameOverLbl.setStyle("-fx-font-weight: bold; -fx-font-size: 30pt; -fx-font-family: \"SF Pixelate\";");
        gameOverLbl.setLayoutX(25);
        gameOverLbl.setLayoutY(30);

        //Create Backgrounds
        Background backgroundLayout = new Background( new BackgroundFill( Color.web("#c1babb"), CornerRadii.EMPTY, Insets.EMPTY ) );
        Background backgroundBtn = new Background( new BackgroundFill( Color.AQUA, CornerRadii.EMPTY, Insets.EMPTY ) );

        //Create Button
        Button restartBtn = new Button("Restart");
        restartBtn.setFont(new Font("Consolas", 18.0));
        restartBtn.setBackground(backgroundBtn);
        restartBtn.setLayoutX(25);
        restartBtn.setLayoutY(110);

        Button showScoreBoardBtn = new Button("Show Scores");
        showScoreBoardBtn.setFont(new Font("Consolas", 18.0));
        showScoreBoardBtn.setBackground(backgroundBtn);
        showScoreBoardBtn.setLayoutX(150);
        showScoreBoardBtn.setLayoutY(110);

        //ProgressIndicator
        ProgressIndicator databaseActivityIndicator = new ProgressIndicator();
        databaseActivityIndicator.setLayoutX(195);
        databaseActivityIndicator.setLayoutY(100);
        databaseActivityIndicator.setPrefHeight(55);
        databaseActivityIndicator.setPrefWidth(55);

        databaseActivityIndicator.visibleProperty().bind(saverTask.runningProperty());
        databaseActivityIndicator.progressProperty().bind(saverTask.progressProperty());
        showScoreBoardBtn.disableProperty().bind(saverTask.runningProperty());

        //Create Layout
        Pane secondaryLayout = new Pane();
        secondaryLayout.setBackground(backgroundLayout);

        //add Controls to Window
        secondaryLayout.getChildren().add(gameOverLbl);
        secondaryLayout.getChildren().add(restartBtn);
        secondaryLayout.getChildren().add(showScoreBoardBtn);
        secondaryLayout.getChildren().add(databaseActivityIndicator);

        // New window (Stage)
        Scene scene = new Scene(secondaryLayout, 300, 150);
        scene.getStylesheets().add(getClass().getResource("/fontstyle.css").toExternalForm());

        Stage newWindow = new Stage();
        newWindow.setTitle("END");
        newWindow.setScene(scene);
        Image icon = new Image(getClass().getResourceAsStream("/images/icon.svg"));
        newWindow.getIcons().add(icon);

        // Specifies the modality for new window.
        newWindow.initModality(Modality.WINDOW_MODAL);

        // Specifies the owner Window (parent) for new window
        newWindow.initOwner(primaryStage);

        // Set position of second window, related to primary window.
        newWindow.setX(primaryStage.getX() + 100);
        newWindow.setY(primaryStage.getY() + 200);
        newWindow.setResizable(false);

        newWindow.show();

        //Button Events
        restartBtn.setOnAction(e -> {
            newWindow.close();
            primaryStage.close();

            //show the start Page
            mainAppWindow.show();
        });


        showScoreBoardBtn.setOnAction(e -> Platform.runLater(
            () -> {
                //wait until saverTask operates with database
                try {
                    databaseSetupFuture.get();
                } catch (InterruptedException | ExecutionException exc) {
                    exc.printStackTrace();
                }

                showScoreBoardWindow();
            }
        ));
    }


    @Override
    public void someoneStopped() {
        //stop all running ghosts(threads)
        allGhosts.forEach(x -> x.stop());

        //Update the score of the user
        modifyDBAsync();

        //user interface cannot be directly updated from a non-application thread. Platform.runLater solves this
        Platform.runLater(
            () -> {
               showGameOver();
            }
        );
    }


    @Override
    public synchronized void setLblPosition(int x, int y, Label label) {
        label.setLayoutX((x+0.5)*CELL_SIZE-SIZE/2);
        label.setLayoutY((y+0.5)*CELL_SIZE-SIZE/2);
    }


    /**
     * Create Goodie
     * It will appear random on the board (one per board)
     */
    private void createGoodie() {
        int[] result = randomizePosition();
        goodie = new Goodie(result[0], result[1]);
        this.board.getChildren().add(goodie.getLabel());
    }


    /**
     * Increase Level. All Ghosts increase their speed
     * Show a label with level nr.
     */
    private void lvlUP() {
        if (settingsGhostSpeed - 50 >= 200) {
            settingsGhostSpeed -= 50;
            levelNr++;
        }
        for(Ghost g : allGhosts) {
            g.setSpeed(settingsGhostSpeed);
        }

        //Show level Label
        Platform.runLater(() -> {
            levelLbl.setText(String.valueOf(levelNr));
        });
    }


    private Group bestPlayers() {
        Group bestPlayers = new Group();
        int positionY = 110;
        int rank = 1;

        Group thisPlayer = new Group();

        for (User u : usrRepo.getUsers()) {
            Group player = new Group();

            Label rankLbl = new Label(String.valueOf(rank));
            rankLbl.setTextAlignment(TextAlignment.CENTER);
            rankLbl.setLayoutX(60);
            rankLbl.setLayoutY(positionY);
            rankLbl.setTextFill(Color.web("#D99951"));
            rankLbl.setStyle("-fx-font-weight: bold; -fx-font-size: 16pt; -fx-font-family: \"SF Pixelate\";");
            player.getChildren().add(rankLbl);

            Label userNameLbl = new Label(u.getName());
            userNameLbl.setLayoutX(140);
            userNameLbl.setLayoutY(positionY);
            userNameLbl.setTextFill(Color.web("#D99951"));
            userNameLbl.setStyle("-fx-font-weight: bold; -fx-font-size: 16pt; -fx-font-family: \"SF Pixelate\";");
            player.getChildren().add(userNameLbl);

            Label scoreLbl = new Label(String.valueOf(u.getScore()));
            scoreLbl.setLayoutX(300);
            scoreLbl.setLayoutY(positionY);
            scoreLbl.setTextFill(Color.web("#BC4F56"));
            scoreLbl.setStyle("-fx-font-weight: bold; -fx-font-size: 16pt; -fx-font-family: \"SF Pixelate\";");
            player.getChildren().add(scoreLbl);

            Label dateLbl = new Label(u.getResultDate().toString());
            dateLbl.setLayoutX(380);
            dateLbl.setLayoutY(positionY);
            dateLbl.setTextFill(Color.web("#D99951"));
            dateLbl.setStyle("-fx-font-weight: bold; -fx-font-size: 16pt; -fx-font-family: \"SF Pixelate\";");
            player.getChildren().add(dateLbl);

            Label timeLbl = new Label(u.getResultTime().toString());
            timeLbl.setLayoutX(535);
            timeLbl.setLayoutY(positionY);
            timeLbl.setTextFill(Color.web("#D99951"));
            timeLbl.setStyle("-fx-font-weight: bold; -fx-font-size: 16pt; -fx-font-family: \"SF Pixelate\";");
            player.getChildren().add(timeLbl);

            bestPlayers.getChildren().add(player);

            //save the current user (To highlight him)
            if ((u.getName()).equals(userName)) {
                thisPlayer = player;
            }

            positionY += 30;
            rank++;
            if (rank == 11)
                break;
        }

        //Highlight the current user
        FadeTransition fadeTransition = new FadeTransition(Duration.seconds(0.7), thisPlayer);
        fadeTransition.setFromValue(1.0);
        fadeTransition.setToValue(0.0);
        fadeTransition.setAutoReverse(true);
        fadeTransition.setCycleCount(Animation.INDEFINITE);
        fadeTransition.play();


        return bestPlayers;
    }


    private void showScoreBoardWindow() {
        //Title
        Label titleLbl = new Label("THE 10 LEGENDS");
        titleLbl.setLayoutX(140);
        titleLbl.setLayoutY(10);
        titleLbl.setTextFill(Color.web("#cab592"));
        titleLbl.setStyle("-fx-font-weight: bold; -fx-font-size: 28pt; -fx-font-family: \"SF Pixelate\";");

        //Column Names
        int positionY = 65;
        Group columns = new Group();

        Label rankLbl = new Label("RANK");
        rankLbl.setTextAlignment(TextAlignment.CENTER);
        rankLbl.setLayoutX(30);
        rankLbl.setLayoutY(positionY);
        rankLbl.setTextFill(Color.web("#D99951"));
        rankLbl.setStyle("-fx-font-weight: bold; -fx-font-size: 20pt; -fx-font-family: \"SF Pixelate\";");
        columns.getChildren().add(rankLbl);

        Label userNameLbl = new Label("USER");
        userNameLbl.setLayoutX(150);
        userNameLbl.setLayoutY(positionY);
        userNameLbl.setTextFill(Color.web("#D99951"));
        userNameLbl.setStyle("-fx-font-weight: bold; -fx-font-size: 18pt; -fx-font-family: \"SF Pixelate\";");
        columns.getChildren().add(userNameLbl);

        Label scoreLbl = new Label("SCORE");
        scoreLbl.setLayoutX(270);
        scoreLbl.setLayoutY(positionY);
        scoreLbl.setTextFill(Color.web("#D99951"));
        scoreLbl.setStyle("-fx-font-weight: bold; -fx-font-size: 18pt; -fx-font-family: \"SF Pixelate\";");
        columns.getChildren().add(scoreLbl);

        Label dateLbl = new Label("DATE");
        dateLbl.setLayoutX(405);
        dateLbl.setLayoutY(positionY);
        dateLbl.setTextFill(Color.web("#D99951"));
        dateLbl.setStyle("-fx-font-weight: bold; -fx-font-size: 18pt; -fx-font-family: \"SF Pixelate\";");
        columns.getChildren().add(dateLbl);

        Label timeLbl = new Label("TIME");
        timeLbl.setLayoutX(540);
        timeLbl.setLayoutY(positionY);
        timeLbl.setTextFill(Color.web("#D99951"));
        timeLbl.setStyle("-fx-font-weight: bold; -fx-font-size: 18pt; -fx-font-family: \"SF Pixelate\";");
        columns.getChildren().add(timeLbl);


        //Bottom Copyright Developer
        Label devLbl = new Label("© GV");
        devLbl.setLayoutX(300);
        devLbl.setLayoutY(450);
        devLbl.setTextFill(Color.web("#A99CBC"));
        devLbl.setStyle("-fx-font-weight: bold; -fx-font-size: 14pt; -fx-font-family: \"SF Pixelate\";");


        //Create Backgrounds
        Background backgroundLayout = new Background( new BackgroundFill( Color.web("#2D393C"), CornerRadii.EMPTY, Insets.EMPTY ) );

        Pane secondaryLayout = new Pane();
        secondaryLayout.setBackground(backgroundLayout);

        //Add elements to layout
        secondaryLayout.getChildren().add(titleLbl);
        secondaryLayout.getChildren().add(bestPlayers());
        secondaryLayout.getChildren().add(columns);
        secondaryLayout.getChildren().add(devLbl);


        // New window (Stage)
        Scene scene = new Scene(secondaryLayout, 660, 490);
        scene.getStylesheets().add(getClass().getResource("/fontstyle.css").toExternalForm());

        Stage newWindow = new Stage();
        newWindow.setTitle("Scores");
        newWindow.setScene(scene);
        Image icon = new Image(getClass().getResourceAsStream("/images/icon.svg"));
        newWindow.getIcons().add(icon);

        // Specifies the modality for new window.
        newWindow.initModality(Modality.WINDOW_MODAL);

        // Specifies the owner Window (parent) for new window
        newWindow.initOwner(primaryStage);
        newWindow.setResizable(false);

        newWindow.show();
    }
}



