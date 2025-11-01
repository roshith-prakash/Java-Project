package javaproject;

import javafx.application.Application;
import javafx.stage.Stage;

public class Main extends Application {
    
    @Override
    public void start(Stage primaryStage) {
        // Initialize database schema
        SchemaInitializer.initialize();
        
        // Show login screen
        LoginScreen loginScreen = new LoginScreen(primaryStage);
        loginScreen.show();
    }
    
    public static void main(String[] args) {
        launch(args);
    }
}