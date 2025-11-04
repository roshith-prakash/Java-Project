package javaproject;

import javafx.application.Application;
import javafx.stage.Stage;

public class Main extends Application {
    
    @Override
    public void start(Stage primaryStage) {
        try {
            // Initialize database schema first
            SchemaInitializer.initialize();
            
            // Show login screen
            new LoginScreen(primaryStage).show();
        } catch (Exception e) {
            e.printStackTrace();
            System.err.println("Error starting application: " + e.getMessage());
        }
    }
    
    public static void main(String[] args) {
        launch(args);
    }
}