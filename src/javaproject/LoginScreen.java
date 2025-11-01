package javaproject;

import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.Scene;
import javafx.scene.control.*;
import javafx.scene.layout.*;
import javafx.stage.Stage;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;

public class LoginScreen {
    private Stage stage;
    
    public LoginScreen(Stage stage) {
        this.stage = stage;
    }
    
    public void show() {
        VBox root = new VBox(20);
        root.setPadding(new Insets(40));
        root.setAlignment(Pos.CENTER);
        root.setStyle("-fx-background-color: #f5f5f5;");
        
        Label titleLabel = new Label("Attendance Management System");
        titleLabel.setStyle("-fx-font-size: 24px; -fx-font-weight: bold; -fx-text-fill: #2c3e50;");
        
        Label subtitleLabel = new Label("Login");
        subtitleLabel.setStyle("-fx-font-size: 18px; -fx-text-fill: #34495e;");
        
        VBox formBox = new VBox(15);
        formBox.setMaxWidth(400);
        formBox.setStyle("-fx-background-color: white; -fx-padding: 30; -fx-background-radius: 10;");
        
        TextField usernameField = new TextField();
        usernameField.setPromptText("Username");
        usernameField.setStyle("-fx-font-size: 14px; -fx-padding: 10;");
        
        PasswordField passwordField = new PasswordField();
        passwordField.setPromptText("Password");
        passwordField.setStyle("-fx-font-size: 14px; -fx-padding: 10;");
        
        ComboBox<String> roleCombo = new ComboBox<>();
        roleCombo.getItems().addAll("ADMIN", "TEACHER", "STUDENT");
        roleCombo.setPromptText("Select Role");
        roleCombo.setMaxWidth(Double.MAX_VALUE);
        roleCombo.setStyle("-fx-font-size: 14px;");
        
        Button loginButton = new Button("Login");
        loginButton.setMaxWidth(Double.MAX_VALUE);
        loginButton.setStyle("-fx-background-color: #3498db; -fx-text-fill: white; -fx-font-size: 16px; -fx-padding: 12; -fx-cursor: hand;");
        
        Label messageLabel = new Label();
        messageLabel.setStyle("-fx-text-fill: red; -fx-font-size: 12px;");
        
        loginButton.setOnAction(e -> {
            String username = usernameField.getText().trim();
            String password = passwordField.getText();
            String role = roleCombo.getValue();
            
            if (username.isEmpty() || password.isEmpty() || role == null) {
                messageLabel.setText("Please fill all fields");
                return;
            }
            
            User user = authenticate(username, password, role);
            if (user != null) {
                openDashboard(user);
            } else {
                messageLabel.setText("Invalid credentials");
            }
        });
        
        formBox.getChildren().addAll(
            new Label("Username:"),
            usernameField,
            new Label("Password:"),
            passwordField,
            new Label("Role:"),
            roleCombo,
            loginButton,
            messageLabel
        );
        
        Label infoLabel = new Label("Default Admin: username=admin, password=admin123");
        infoLabel.setStyle("-fx-font-size: 11px; -fx-text-fill: #7f8c8d;");
        
        root.getChildren().addAll(titleLabel, subtitleLabel, formBox, infoLabel);
        
        Scene scene = new Scene(root, 600, 600);
        stage.setScene(scene);
        stage.setTitle("Login - Attendance System");
        stage.show();
    }
    
    private User authenticate(String username, String password, String role) {
        try (Connection conn = DatabaseConfig.getConnection();
             PreparedStatement ps = conn.prepareStatement(
                 "SELECT * FROM users WHERE username = ? AND password = ? AND role = ?")) {
            ps.setString(1, username);
            ps.setString(2, password);
            ps.setString(3, role);
            ResultSet rs = ps.executeQuery();
            
            if (rs.next()) {
                return new User(
                    rs.getInt("id"),
                    rs.getString("username"),
                    rs.getString("password"),
                    rs.getString("role"),
                    rs.getString("name")
                );
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
        return null;
    }
    
    private void openDashboard(User user) {
        switch (user.getRole()) {
            case "ADMIN":
                new AdminDashboard(stage, user).show();
                break;
            case "TEACHER":
                new TeacherDashboard(stage, user).show();
                break;
            case "STUDENT":
                new StudentDashboard(stage, user).show();
                break;
        }
    }
}