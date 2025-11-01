package javaproject;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.SQLException;

public class DatabaseConfig {
    private static final String URL = "jdbc:mysql://localhost:3306/javaproject";
    private static final String USER = "root";
    private static final String PASSWORD = "roshith_123"; // Change this to your MySQL password
    
    public static Connection getConnection() throws SQLException {
        try {
            Class.forName("com.mysql.cj.jdbc.Driver");
            return DriverManager.getConnection(URL, USER, PASSWORD);
        } catch (ClassNotFoundException e) {
            throw new SQLException("MySQL Driver not found", e);
        }
    }
    
    public static Connection getConnectionForInitialization() throws SQLException {
        try {
            Class.forName("com.mysql.cj.jdbc.Driver");
            // Connect without specifying database for initial setup
            return DriverManager.getConnection("jdbc:mysql://localhost:3306/", USER, PASSWORD);
        } catch (ClassNotFoundException e) {
            throw new SQLException("MySQL Driver not found", e);
        }
    }
}