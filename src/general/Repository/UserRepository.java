package general.Repository;

import general.Model.User;
import general.Util.JDBCUtil;

import java.sql.*;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.ThreadFactory;
import java.util.concurrent.atomic.AtomicInteger;


public class UserRepository  {
    private JDBCUtil jdbcUtil;
    private List<User> users = new ArrayList<>();



    public UserRepository() {
        jdbcUtil = new JDBCUtil();
    }


    /**
     * Getter for list of users
     * @return
     */
    public List<User> getUsers() {
        return users;
    }


    /**
     * Find all users from database.
     * Order them descending by their score
     * @return the list with all users
     */
    public List<User> findAll() {
        try {
            Connection con = jdbcUtil.getConnection();
            PreparedStatement stmt = con.prepareStatement("SELECT * FROM USER ORDER BY SCORE DESC");

            ResultSet resultSet = stmt.executeQuery();

            while(resultSet.next()) {
                User user = new User();
                user.setName(resultSet.getString("USERNAME"));
                user.setScore(resultSet.getInt("SCORE"));
                user.setResultDate(resultSet.getDate("RESULTDATE"));
                user.setResultTime(resultSet.getTime("RESULTTIME"));

                users.add(user);
            }

            jdbcUtil.closeConnection();

        } catch (SQLException ex) {
            System.err.println("Error msg: " + ex.getMessage());
            System.err.println("SQLSTATE: " + ex.getSQLState());
            System.err.println("Error code: " + ex.getErrorCode());

            users = null;
        }
        return users;
    }


    /**
     * Insert new user to database (sort by score)
     * @param user
     */
    public void addUser(User user) {
        //add new user and keep the list of users sorted descending by score
        if (users.size() == 0) {
            users.add(user);
        }
        else {
            int i;
            for (i = 0; i < users.size(); i++) {
                if (user.getScore() >= users.get(i).getScore()) {
                    users.add(i, user);
                    break;
                }
            }
            if (i == users.size()) {
                users.add(user);
            }
        }

        try {
            Connection con = jdbcUtil.getConnection();

            PreparedStatement pstmt = con.prepareStatement("INSERT INTO User VALUES(?, ?, ?, ?)");

            pstmt.setString(1, user.getName());
            pstmt.setInt(2, user.getScore());
            pstmt.setDate(3, user.getResultDate());
            pstmt.setTime(4, user.getResultTime());

            pstmt.executeUpdate();

            jdbcUtil.closeConnection();

        } catch (SQLException ex) {
            System.err.println("Error msg: " + ex.getMessage());
            System.err.println("SQLSTATE: " + ex.getSQLState());
            System.err.println("Error code: " + ex.getErrorCode());
        }
    }


    /**
     * Update the user in the DB
     * @param user
     */
    public void updateUser(User user) {
        //keep the list of users sorted
        if (users.size() >= 2) {
            int index = users.indexOf(user);
            users.remove(index);
            int i;
            for (i = 0; i < users.size(); i++) {
                if (user.getScore() >= users.get(i).getScore()) {
                    users.add(i, user);
                    break;
                }
            }
            if (i == users.size()) {
                users.add(user);
            }
        }

        try {
            Connection con = jdbcUtil.getConnection();

            PreparedStatement pstmt = con.prepareStatement("UPDATE User SET User.SCORE = ?, User.RESULTDATE = ?, User.RESULTTIME = ? WHERE User.USERNAME = ?");

            pstmt.setInt(1, user.getScore());
            pstmt.setDate(2, user.getResultDate());
            pstmt.setTime(3, user.getResultTime());
            pstmt.setString(4, user.getName());

            pstmt.executeUpdate();

            jdbcUtil.closeConnection();

        } catch (SQLException ex) {
            System.err.println("Error msg: " + ex.getMessage());
            System.err.println("SQLSTATE: " + ex.getSQLState());
            System.err.println("Error code: " + ex.getErrorCode());
        }
    }


    /**
     * Check if there is an user with given name
     * @param name
     * @return Found User or null otherwise
     */
    public User findUserByName(String name) {
       return users.stream().filter(x -> name.equals(x.getName())).findAny().orElse(null);
    }


    /**
     * ThreadFactory
     */
    public static class DatabaseThreadFactory implements ThreadFactory {
        static final AtomicInteger poolNumber = new AtomicInteger(1);

        @Override public Thread newThread(Runnable runnable) {
            Thread thread = new Thread(runnable, "Database-Connection-" + poolNumber.getAndIncrement() + "-thread");
            thread.setDaemon(true);

            return thread;
        }
    }
}
