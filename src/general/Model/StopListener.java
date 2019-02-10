package general.Model;

import javafx.scene.control.Label;

/**
 * Event Handler for stop the game (Game Over) and label movement
 */
public interface StopListener {
    void someoneStopped();
    void setLblPosition(int x, int y, Label label);
}
