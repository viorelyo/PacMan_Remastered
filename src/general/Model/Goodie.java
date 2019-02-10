package general.Model;

import javafx.scene.control.Label;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;


public class Goodie {
    private static final int SIZE = 40;
    private static final int CELL_SIZE = 50;

    public static int X;
    public static int Y;

    private Label label;

    /**
     * Moves the Goodie Label to the given x, y coordinates
     * @param x
     * @param y
     */
    public void setPosition(int x, int y) {
        this.X = x;
        this.Y = y;

        label.setLayoutX((x+0.5)*CELL_SIZE-SIZE/2);
        label.setLayoutY((y+0.5)*CELL_SIZE-SIZE/2);
    }


    /**
     * Constructor for Goodie
     * @param x
     * @param y
     * x, y - position of Goodie
     */
    public Goodie(int x, int y) {
        label = new Label();
        Image image = new Image(getClass().getResourceAsStream("/images/bitg.png"), 40, 40, true, true);
        label.setGraphic(new ImageView(image));

        setPosition(x, y);
    }


    /**
     * Getter for label
     * @return label
     */
    public Label getLabel() {
        return label;
    }
}
