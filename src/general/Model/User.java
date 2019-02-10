package general.Model;

import java.io.Serializable;
import java.sql.Date;
import java.sql.Time;


public class User implements Serializable {
    private String name;
    private int score;
    private Date resultDate;
    private Time resultTime;


    /**
     *
     * @param name
     * @param score
     * @param resultDate
     * @param resultTime
     */
    public User(String name, int score, Date resultDate, Time resultTime) {
        this.name = name;
        this.score = score;
        this.resultDate = resultDate;
        this.resultTime = resultTime;
    }


    /**
     * Default Construtcor for User
     */
    public User() {
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public int getScore() {
        return score;
    }

    public void setScore(int score) {
        this.score = score;
    }

    public Date getResultDate() {
        return resultDate;
    }

    public void setResultDate(Date resultDate) {
        this.resultDate = resultDate;
    }

    public Time getResultTime() {
        return resultTime;
    }

    public void setResultTime(Time resultTime) {
        this.resultTime = resultTime;
    }
}
