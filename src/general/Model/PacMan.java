package general.Model;

import general.Repository.MapRepository;
import javafx.scene.control.ContentDisplay;
import javafx.scene.control.Label;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;

/**
 * PacMan Model
 */
public class PacMan {
    private static final int SIZE = 42;
    private static final int CELL_SIZE = 50;

    public static int X;
    public static int Y;

    private Label label;
    private Image imageL;
    private Image imageR;

    /**
     * Moves the PacMan Label to the given x, y coordinates
     * @param x
     * @param y
     */
    private void setPosition(int x, int y) {
        this.X = x;
        this.Y = y;

        label.setLayoutX((x+0.5)*CELL_SIZE-SIZE/2);
        label.setLayoutY((y+0.5)*CELL_SIZE-SIZE/2);
    }

    /**
     * Constructor for PacMan
     * @param x
     * @param y
     * x, y - position of the PacMan
     */
    public PacMan(int x, int y) {
        label = new Label();
//        label.setMinSize(SIZE, SIZE);
//        label.setPrefSize(SIZE, SIZE);
//        label.setMaxSize(SIZE, SIZE);
        imageL = new Image(getClass().getResourceAsStream("/images/pacmanL.png"), 40, 40, true, true);
        imageR = new Image(getClass().getResourceAsStream("/images/pacmanR.png"), 40, 40, true, true);
        label.setContentDisplay(ContentDisplay.GRAPHIC_ONLY);
        label.setGraphic(new ImageView(imageL));
        setPosition(x, y);
    }

    /**
     * Getter for label
     * @return label
     */
    public Label getLabel() {
        return label;
    }

    /**
     * moves the PacMan to the given Direction
     * Checks for availability to move
     * changes the label.Layout to x, y coordinates
     * @param dir
     */
    public void moveMe(Directions dir) {
        switch (dir) {
            case UP:
                this.moveMeUp();
                break;
            case DOWN:
                this.moveMeDown();
                break;
            case LEFT:
                this.moveMeLeft();
                break;
            case RIGHT:
                this.moveMeRight();
                break;
        }
    }


    /**
     * Moves the PacMan up if it's possible
     */
    private void moveMeUp() {
        int tmp = Y - 1;
        if (MapRepository.labyrinth.get(tmp)[X] != 1)
            this.setPosition(X, tmp);
    }

    /**
     * Moves the PacMan Down if it's possible
     */
    private void moveMeDown() {
        int tmp = Y + 1;
        if (MapRepository.labyrinth.get(tmp)[X] != 1)
            this.setPosition(X, tmp);
    }

    /**
     * Moves the PacMan LEFT if it's possible
     */
    private void moveMeLeft() {
        label.setGraphic(new ImageView(imageL));
        int tmp = X - 1;
        if (MapRepository.labyrinth.get(Y)[tmp] != 1)
            this.setPosition(tmp, Y);
    }

    /**
     * Moves the PacMan RIGHT if it's possible
     */
    private void moveMeRight() {
        label.setGraphic(new ImageView(imageR));
        int tmp = X + 1;
        if (MapRepository.labyrinth.get(Y)[tmp] != 1)
            this.setPosition(tmp, Y);
    }
}
