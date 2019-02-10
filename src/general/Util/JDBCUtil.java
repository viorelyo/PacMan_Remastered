package general.Util;

import java.io.InputStream;
import java.sql.*;
import java.util.Properties;
import java.util.concurrent.ThreadFactory;
import java.util.concurrent.atomic.AtomicInteger;


public class JDBCUtil {
    private String className;
    private String url;
    private String user;
    private String password;
    private Connection con;


    /**
     * Constructor for JDBCUtil
     */
    public JDBCUtil() { getPropertyValues(); }


    /**
     * Get connection with DB2
     * @return statement
     */
    public Connection getConnection() {
        try {
            Class.forName(className);
        } catch (ClassNotFoundException ex) {
            System.out.println("Unable to load the class. Terminating the program");
            System.exit(-1);
        }
        //get the connection
        try {
            // Create the connection using the IBM Data Server Driver for JDBC and SQLJ
            con = DriverManager.getConnection (url, user, password);
            // Commit changes manually
            con.setAutoCommit(false);

            return con;
        } catch (SQLException ex) {
            System.err.println("SQLException information");
            while(ex!=null) {
                System.err.println("Error msg: " + ex.getMessage());
                System.err.println("SQLSTATE: " + ex.getSQLState());
                System.err.println("Error code: " + ex.getErrorCode());
                ex.printStackTrace();
                ex = ex.getNextException(); // For drivers that support chained exceptions
            }
            System.exit(-1);
        }
        return null;
    }


    /**
     * Close the connection and Statement with DB2
     */
    public void closeConnection() {
        try {
            // Close the connection
            con.commit();
            con.close();
        }
        catch(SQLException ex)
        {
            System.err.println("SQLException information");
            while(ex!=null) {
                System.err.println ("Error msg: " + ex.getMessage());
                System.err.println ("SQLSTATE: " + ex.getSQLState());
                System.err.println ("Error code: " + ex.getErrorCode());
                ex.printStackTrace();
                ex = ex.getNextException(); // For drivers that support chained exceptions
            }
        }
    }


    /**
     * Get Database properties from app.properties file
     */
    private void getPropertyValues() {
        InputStream inputStream;
        try {
            Properties prop = new Properties();
            String propFileName = "app.properties";
            inputStream = getClass().getClassLoader().getResourceAsStream(propFileName);

            if (inputStream != null) {
                prop.load(inputStream);
            } else {
                System.out.println("property file '" + propFileName + "' not found in the classpath");
            }

            className = prop.getProperty("classname");
            user = prop.getProperty("user");
            password = prop.getProperty("password");
            url = prop.getProperty("url");
        } catch (Exception e) {
            e.printStackTrace();
        }
    }
}
