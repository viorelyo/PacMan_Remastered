package general.Repository.UserRepoTasks;

import general.Model.User;
import general.Repository.UserRepository;
import javafx.concurrent.Task;

import java.sql.Date;
import java.sql.Time;
import java.time.LocalDate;
import java.time.LocalTime;

public class SaveScoreTask extends Task {
    private UserRepository repo;
    private String userName;
    private int score;

    /**
     * Constructor for User Controller
     * @param repo
     */
    public SaveScoreTask(UserRepository repo, String userName, int score) {
        this.repo = repo;
        this.userName = userName;
        this.score = score;
    }


    @Override
    protected Object call() throws Exception {
        saveScore(this.userName, this.score);
        return null;
    }


    /**
     * Update the user's information
     * if there is a user with given name update his score, if the new one is greater
     * else create new user with given name
     * @param userName
     * @param score
     */
    public void saveScore(String userName, int score) {
        User foundUser = repo.findUserByName(userName);

        if (foundUser == null) {
            User newUser = new User(userName, score, Date.valueOf(LocalDate.now()), Time.valueOf(LocalTime.now()));
            repo.addUser(newUser);
        }
        else {
            //Check if the new score is greater than user's old one
            if (score > foundUser.getScore()) {
                foundUser.setScore(score);
                foundUser.setResultDate(Date.valueOf(LocalDate.now()));
                foundUser.setResultTime(Time.valueOf(LocalTime.now()));
                repo.updateUser(foundUser);
            }
        }
    }

}
