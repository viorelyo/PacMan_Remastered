package general.Model;

import general.Repository.MapRepository;
import javafx.scene.control.Label;

import java.util.concurrent.ThreadLocalRandom;

/**
 * Class Ghost (Will be created as Thread
 */
public class Ghost implements Runnable {
    private static final int SIZE = 45;
    private static final int CELL_SIZE = 50;

    private Thread thread;
    private Label label;

    private int speed;
    private int X;
    private int Y;
    private Directions direct;

    private int id;
    private StopListener listener;

    /**
     * Moves the Ghost Label to the given x, y coordinates
     * @param x
     * @param y
     */
    private synchronized void setPosition(int x, int y) {
        this.X = x;
        this.Y = y;

        //Controller is responsible for updating Label Position
        listener.setLblPosition(x, y, label);

    }

    /**
     * Getter for label
     * @return label
     */
    public Label getLabel() {
        return label;
    }

    /**
     * Constructor for Ghost
     * @param listener
     * @param speed
     * @param x
     * @param y
     * @param id
     */
    public Ghost(StopListener listener, int speed, int x, int y, int id) {
        this.id = id;
        this.listener = listener;
        this.speed = speed;

        label = new Label();
        label.setMinSize(SIZE, SIZE);
        label.setPrefSize(SIZE, SIZE);
        label.setMaxSize(SIZE, SIZE);
        label.setStyle("-fx-background-color: #f0814c; -fx-font-weight: bold; -fx-font-size: 15pt;");
        label.setText(" ^ ^");
        this.setPosition(x, y);

        moveRandomly();     //set initial Direction
    }


    /**
     * Setter for speed of the Ghost
     * @param speed
     */
    public void setSpeed(int speed) {
        this.speed = speed;
    }


    @Override
    public void run() {
        while (true) {
            try {
                if (this.thread != null) {
                    Thread.sleep(this.speed);
                }
            } catch (InterruptedException e) {
                break;
            }
            tryChangeDirection();
            setDirection(this.direct);

            if (this.X == PacMan.X && this.Y == PacMan.Y) {
                listener.someoneStopped();
                break;
            }
        }
    }


    /**
     * Set a random Direction for the Ghost
     */
    private void moveRandomly(){

    rnd:while (true) {
            //Get a random Direction of move
            int index = ThreadLocalRandom.current().nextInt(0, 4);
            Directions dir = Directions.values()[index];
            int tmp;
            switch (dir) {
                case UP:
                    tmp = this.Y - 1;
                    if (MapRepository.labyrinth.get(tmp)[X] != 1) {
                        this.direct = dir;
                        break rnd;
                    }
                    break;
                case DOWN:
                    tmp = this.Y + 1;
                    if (MapRepository.labyrinth.get(tmp)[X] != 1) {
                        this.direct = dir;
                        break rnd;
                    }
                    break;
                case LEFT:
                    tmp = this.X - 1;
                    if (MapRepository.labyrinth.get(Y)[tmp] != 1) {
                        this.direct = dir;
                        break rnd;
                    }
                    break;
                case RIGHT:
                    tmp = this.X + 1;
                    if (MapRepository.labyrinth.get(Y)[tmp] != 1) {
                        this.direct = dir;
                        break rnd;
                    }
                    break;
            }
        }
        setDirection(this.direct);
    }


    private void tryChangeDirection() {
        //Random choose to change or no direction
        boolean[] changeORno = {true, false};
        int index = ThreadLocalRandom.current().nextInt(0, 2);

        if ((MapRepository.labyrinth.get(this.Y + 1)[this.X] != 1) && (this.direct != Directions.UP && this.direct != Directions.DOWN)) {
            if (changeORno[index] == true) {
                this.direct = Directions.DOWN;
                return;
            }
        }
        if ((MapRepository.labyrinth.get(this.Y - 1)[this.X] != 1) && (this.direct != Directions.UP && this.direct != Directions.DOWN)){
            if (changeORno[index] == true) {
                this.direct = Directions.UP;
                return;
            }
        }
        if ((MapRepository.labyrinth.get(this.Y)[this.X + 1] != 1) && (this.direct != Directions.LEFT && this.direct != Directions.RIGHT)) {
            if (changeORno[index] == true) {
                this.direct = Directions.RIGHT;
                return;
            }
        }
        if ((MapRepository.labyrinth.get(this.Y)[this.X - 1] != 1) && (this.direct != Directions.LEFT && this.direct != Directions.RIGHT)){
            if (changeORno[index] == true) {
                this.direct = Directions.LEFT;
                return;
            }
        }
    }

    /**
     * Set a direction for Ghost
     * It moves until a wall
     * @param dir
     */
    private void setDirection(Directions dir) {
        int tmp;
        switch (dir) {
            case UP:
                tmp = this.Y - 1;
                if (MapRepository.labyrinth.get(tmp)[X] != 1) {
                    setPosition(X, tmp);
                }
                else {
                    moveRandomly();

                }
                break;
            case DOWN:
                tmp = this.Y + 1;
                if (MapRepository.labyrinth.get(tmp)[X] != 1) {
                    setPosition(X, tmp);
                }
                else {
                    moveRandomly();
                }
                break;
            case LEFT:
                tmp = this.X - 1;
                if (MapRepository.labyrinth.get(Y)[tmp] != 1) {
                    setPosition(tmp, Y);
                }
                else {
                    moveRandomly();
                }
                break;
            case RIGHT:
                tmp = this.X + 1;
                if (MapRepository.labyrinth.get(Y)[tmp] != 1) {
                    setPosition(tmp, Y);
                }
                else {
                    moveRandomly();
                }
                break;
        }
    }


    /**
     * starts a thread with ghost(this) as instance
     */
    public void start() {
        if (thread == null) {
            thread = new Thread(this);
            thread.start();
        }
    }

    /**
     * stops the thread
     */
    public void stop() {
        if (thread != null ) {
            thread.interrupt();
        }
    }
}
