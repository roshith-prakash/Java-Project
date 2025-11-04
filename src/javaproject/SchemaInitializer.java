package javaproject;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;

public class SchemaInitializer {
    
    public static void initialize() {
        try (Connection conn = DatabaseConfig.getConnectionForInitialization();
             Statement stmt = conn.createStatement()) {
            
            // Create database if not exists and use it
            stmt.executeUpdate("CREATE DATABASE IF NOT EXISTS javaproject");
            stmt.executeUpdate("USE javaproject");
            
            // Create users table
            stmt.executeUpdate(
                "CREATE TABLE IF NOT EXISTS users (" +
                "id INT AUTO_INCREMENT PRIMARY KEY," +
                "username VARCHAR(50) UNIQUE NOT NULL," +
                "password VARCHAR(100) NOT NULL," +
                "role ENUM('ADMIN', 'TEACHER', 'STUDENT') NOT NULL," +
                "name VARCHAR(100) NOT NULL," +
                "created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP)"
            );
            
            // Create courses table
            stmt.executeUpdate(
                "CREATE TABLE IF NOT EXISTS courses (" +
                "id INT AUTO_INCREMENT PRIMARY KEY," +
                "name VARCHAR(100) UNIQUE NOT NULL," +
                "description TEXT," +
                "created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP)"
            );
            
            // Create subjects table
            stmt.executeUpdate(
                "CREATE TABLE IF NOT EXISTS subjects (" +
                "id INT AUTO_INCREMENT PRIMARY KEY," +
                "name VARCHAR(100) NOT NULL," +
                "course_id INT NOT NULL," +
                "FOREIGN KEY (course_id) REFERENCES courses(id) ON DELETE CASCADE," +
                "UNIQUE KEY unique_subject_course (name, course_id))"
            );
            
            // Create classes table
            stmt.executeUpdate(
                "CREATE TABLE IF NOT EXISTS classes (" +
                "id INT AUTO_INCREMENT PRIMARY KEY," +
                "name VARCHAR(100) NOT NULL," +
                "course_id INT NOT NULL," +
                "FOREIGN KEY (course_id) REFERENCES courses(id) ON DELETE CASCADE," +
                "UNIQUE KEY unique_class_course (name, course_id))"
            );
            
            // Create class_students table
            stmt.executeUpdate(
                "CREATE TABLE IF NOT EXISTS class_students (" +
                "class_id INT NOT NULL," +
                "student_id INT NOT NULL," +
                "PRIMARY KEY (class_id, student_id)," +
                "FOREIGN KEY (class_id) REFERENCES classes(id) ON DELETE CASCADE," +
                "FOREIGN KEY (student_id) REFERENCES users(id) ON DELETE CASCADE)"
            );
            
            // Create subject_assignments table
            stmt.executeUpdate(
                "CREATE TABLE IF NOT EXISTS subject_assignments (" +
                "id INT AUTO_INCREMENT PRIMARY KEY," +
                "subject_id INT NOT NULL," +
                "class_id INT NOT NULL," +
                "teacher_id INT NOT NULL," +
                "FOREIGN KEY (subject_id) REFERENCES subjects(id) ON DELETE CASCADE," +
                "FOREIGN KEY (class_id) REFERENCES classes(id) ON DELETE CASCADE," +
                "FOREIGN KEY (teacher_id) REFERENCES users(id) ON DELETE CASCADE," +
                "UNIQUE KEY unique_subject_class (subject_id, class_id))"
            );
            
            // Create attendance table with time slot support
            stmt.executeUpdate(
                "CREATE TABLE IF NOT EXISTS attendance (" +
                "id INT AUTO_INCREMENT PRIMARY KEY," +
                "student_id INT NOT NULL," +
                "subject_id INT NOT NULL," +
                "class_id INT NOT NULL," +
                "date DATE NOT NULL," +
                "time_slot TIME NOT NULL," +
                "status ENUM('PRESENT', 'ABSENT') NOT NULL," +
                "marked_by INT NOT NULL," +
                "marked_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP," +
                "FOREIGN KEY (student_id) REFERENCES users(id) ON DELETE CASCADE," +
                "FOREIGN KEY (subject_id) REFERENCES subjects(id) ON DELETE CASCADE," +
                "FOREIGN KEY (class_id) REFERENCES classes(id) ON DELETE CASCADE," +
                "FOREIGN KEY (marked_by) REFERENCES users(id)," +
                "UNIQUE KEY unique_attendance (student_id, subject_id, class_id, date, time_slot))"
            );
            
            // Add time_slot column to existing attendance table if it doesn't exist
            try {
                stmt.executeUpdate("ALTER TABLE attendance ADD COLUMN time_slot TIME NOT NULL DEFAULT '09:00:00'");
                System.out.println("Added time_slot column to existing attendance table");
            } catch (Exception e) {
                // Column might already exist, ignore error
                if (!e.getMessage().contains("Duplicate column name")) {
                    System.out.println("Note: time_slot column may already exist");
                }
            }
            
            // Update unique constraint to include time_slot
            try {
                stmt.executeUpdate("ALTER TABLE attendance DROP INDEX unique_attendance");
                stmt.executeUpdate("ALTER TABLE attendance ADD UNIQUE KEY unique_attendance (student_id, subject_id, class_id, date, time_slot)");
                System.out.println("Updated unique constraint to include time_slot");
            } catch (Exception e) {
                // Constraint might already be updated, ignore error
                System.out.println("Note: unique constraint may already be updated");
            }
            
            // Check if default admin exists
            try (PreparedStatement ps = conn.prepareStatement(
                "SELECT COUNT(*) FROM javaproject.users WHERE username = ?")) {
                ps.setString(1, "admin");
                ResultSet rs = ps.executeQuery();
                rs.next();
                if (rs.getInt(1) == 0) {
                    // Insert default admin
                    try (PreparedStatement insert = conn.prepareStatement(
                        "INSERT INTO javaproject.users (username, password, role, name) VALUES (?, ?, ?, ?)")) {
                        insert.setString(1, "admin");
                        insert.setString(2, "admin123"); // Plain text for demo
                        insert.setString(3, "ADMIN");
                        insert.setString(4, "System Administrator");
                        insert.executeUpdate();
                    }
                }
            }
            
            System.out.println("Database initialized successfully!");
            System.out.println("Default Admin Account:");
            System.out.println("  Username: admin");
            System.out.println("  Password: admin123");
            
        } catch (Exception e) {
            System.err.println("Error initializing database: " + e.getMessage());
            e.printStackTrace();
        }
    }
}