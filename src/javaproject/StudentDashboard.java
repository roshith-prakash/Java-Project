package javaproject;

import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.Scene;
import javafx.scene.control.*;
import javafx.scene.layout.*;
import javafx.stage.Stage;
import java.sql.*;
import java.util.*;

public class StudentDashboard {
    private Stage stage;
    private User student;
    private BorderPane mainLayout;
    
    public StudentDashboard(Stage stage, User student) {
        this.stage = stage;
        this.student = student;
    }
    
    public void show() {
        mainLayout = new BorderPane();
        
        // Top bar
        HBox topBar = new HBox(20);
        topBar.setPadding(new Insets(15));
        topBar.setStyle("-fx-background-color: #2c3e50;");
        topBar.setAlignment(Pos.CENTER_LEFT);
        
        Label titleLabel = new Label("Student Dashboard - " + student.getName());
        titleLabel.setStyle("-fx-text-fill: white; -fx-font-size: 18px; -fx-font-weight: bold;");
        
        Region spacer = new Region();
        HBox.setHgrow(spacer, Priority.ALWAYS);
        
        Button logoutBtn = new Button("Logout");
        logoutBtn.setStyle("-fx-background-color: #e74c3c; -fx-text-fill: white; -fx-cursor: hand;");
        logoutBtn.setOnAction(e -> logout());
        
        topBar.getChildren().addAll(titleLabel, spacer, logoutBtn);
        
        // Left menu
        VBox menu = new VBox(10);
        menu.setPadding(new Insets(20));
        menu.setStyle("-fx-background-color: #34495e;");
        menu.setPrefWidth(200);
        
        Button[] menuButtons = {
            createMenuButton("My Attendance"),
            createMenuButton("Lecture Count"),
            createMenuButton("Defaulter Status")
        };
        
        menuButtons[0].setOnAction(e -> showMyAttendance());
        menuButtons[1].setOnAction(e -> showLectureCount());
        menuButtons[2].setOnAction(e -> showDefaulterStatus());
        
        menu.getChildren().addAll(menuButtons);
        
        mainLayout.setTop(topBar);
        mainLayout.setLeft(menu);
        
        showMyAttendance();
        
        Scene scene = new Scene(mainLayout, 1000, 600);
        stage.setScene(scene);
        stage.setTitle("Student Dashboard");
    }
    
    private Button createMenuButton(String text) {
        Button btn = new Button(text);
        btn.setMaxWidth(Double.MAX_VALUE);
        btn.setStyle("-fx-background-color: transparent; -fx-text-fill: white; -fx-alignment: CENTER-LEFT; -fx-padding: 12; -fx-cursor: hand;");
        btn.setOnMouseEntered(e -> btn.setStyle("-fx-background-color: #3498db; -fx-text-fill: white; -fx-alignment: CENTER-LEFT; -fx-padding: 12; -fx-cursor: hand;"));
        btn.setOnMouseExited(e -> btn.setStyle("-fx-background-color: transparent; -fx-text-fill: white; -fx-alignment: CENTER-LEFT; -fx-padding: 12; -fx-cursor: hand;"));
        return btn;
    }
    
    private void showMyAttendance() {
        VBox content = new VBox(20);
        content.setPadding(new Insets(30));
        
        Label title = new Label("My Attendance");
        title.setStyle("-fx-font-size: 24px; -fx-font-weight: bold;");
        
        TableView<Map<String, Object>> table = new TableView<>();
        TableColumn<Map<String, Object>, String> subjectCol = new TableColumn<>("Subject");
        subjectCol.setCellValueFactory(data -> new javafx.beans.property.SimpleStringProperty(data.getValue().get("subject").toString()));
        subjectCol.setPrefWidth(200);
        
        TableColumn<Map<String, Object>, String> classCol = new TableColumn<>("Class");
        classCol.setCellValueFactory(data -> new javafx.beans.property.SimpleStringProperty(data.getValue().get("class").toString()));
        classCol.setPrefWidth(150);
        
        TableColumn<Map<String, Object>, String> totalCol = new TableColumn<>("Total Lectures");
        totalCol.setCellValueFactory(data -> new javafx.beans.property.SimpleStringProperty(data.getValue().get("total").toString()));
        
        TableColumn<Map<String, Object>, String> presentCol = new TableColumn<>("Present");
        presentCol.setCellValueFactory(data -> new javafx.beans.property.SimpleStringProperty(data.getValue().get("present").toString()));
        
        TableColumn<Map<String, Object>, String> absentCol = new TableColumn<>("Absent");
        absentCol.setCellValueFactory(data -> new javafx.beans.property.SimpleStringProperty(data.getValue().get("absent").toString()));
        
        TableColumn<Map<String, Object>, String> percentCol = new TableColumn<>("Attendance %");
        percentCol.setCellValueFactory(data -> {
            String percentage = data.getValue().get("percentage").toString();
            return new javafx.beans.property.SimpleStringProperty(percentage);
        });
        percentCol.setCellFactory(col -> new TableCell<Map<String, Object>, String>() {
            @Override
            protected void updateItem(String item, boolean empty) {
                super.updateItem(item, empty);
                if (empty || item == null) {
                    setText(null);
                    setStyle("");
                } else {
                    setText(item);
                    double percent = Double.parseDouble(item.replace("%", ""));
                    if (percent < 75) {
                        setStyle("-fx-background-color: #e74c3c; -fx-text-fill: white;");
                    } else {
                        setStyle("-fx-background-color: #27ae60; -fx-text-fill: white;");
                    }
                }
            }
        });
        
        table.getColumns().addAll(subjectCol, classCol, totalCol, presentCol, absentCol, percentCol);
        
        try (Connection conn = DatabaseConfig.getConnection();
             PreparedStatement ps = conn.prepareStatement(
                 "SELECT s.name as subject, cl.name as class, " +
                 "COUNT(*) as total, " +
                 "SUM(CASE WHEN a.status = 'PRESENT' THEN 1 ELSE 0 END) as present, " +
                 "SUM(CASE WHEN a.status = 'ABSENT' THEN 1 ELSE 0 END) as absent " +
                 "FROM class_students cs " +
                 "JOIN classes cl ON cs.class_id = cl.id " +
                 "JOIN subjects s ON s.course_id = cl.course_id " +
                 "LEFT JOIN attendance a ON a.student_id = ? AND a.subject_id = s.id AND a.class_id = cl.id " +
                 "WHERE cs.student_id = ? " +
                 "GROUP BY s.id, cl.id " +
                 "ORDER BY s.name")) {
            ps.setInt(1, student.getId());
            ps.setInt(2, student.getId());
            ResultSet rs = ps.executeQuery();
            
            while (rs.next()) {
                int total = rs.getInt("total");
                int present = rs.getInt("present");
                int absent = rs.getInt("absent");
                double percentage = total > 0 ? (present * 100.0) / total : 0;
                
                Map<String, Object> row = new HashMap<>();
                row.put("subject", rs.getString("subject"));
                row.put("class", rs.getString("class"));
                row.put("total", String.valueOf(total));
                row.put("present", String.valueOf(present));
                row.put("absent", String.valueOf(absent));
                row.put("percentage", String.format("%.2f%%", percentage));
                table.getItems().add(row);
            }
        } catch (Exception ex) {
            ex.printStackTrace();
        }
        
        Label infoLabel = new Label("Green = Above 75% | Red = Below 75% (Defaulter)");
        infoLabel.setStyle("-fx-font-size: 12px; -fx-text-fill: #7f8c8d;");
        
        content.getChildren().addAll(title, table, infoLabel);
        mainLayout.setCenter(content);
    }
    
    private void showLectureCount() {
        VBox content = new VBox(20);
        content.setPadding(new Insets(30));
        
        Label title = new Label("Lecture Count by Subject");
        title.setStyle("-fx-font-size: 24px; -fx-font-weight: bold;");
        
        TableView<Map<String, Object>> table = new TableView<>();
        TableColumn<Map<String, Object>, String> subjectCol = new TableColumn<>("Subject");
        subjectCol.setCellValueFactory(data -> new javafx.beans.property.SimpleStringProperty(data.getValue().get("subject").toString()));
        subjectCol.setPrefWidth(250);
        
        TableColumn<Map<String, Object>, String> classCol = new TableColumn<>("Class");
        classCol.setCellValueFactory(data -> new javafx.beans.property.SimpleStringProperty(data.getValue().get("class").toString()));
        classCol.setPrefWidth(200);
        
        TableColumn<Map<String, Object>, String> lecturesCol = new TableColumn<>("Total Lectures");
        lecturesCol.setCellValueFactory(data -> new javafx.beans.property.SimpleStringProperty(data.getValue().get("lectures").toString()));
        lecturesCol.setPrefWidth(150);
        
        table.getColumns().addAll(subjectCol, classCol, lecturesCol);
        
        try (Connection conn = DatabaseConfig.getConnection();
             PreparedStatement ps = conn.prepareStatement(
                 "SELECT s.name as subject, cl.name as class, COUNT(DISTINCT a.date) as lectures " +
                 "FROM class_students cs " +
                 "JOIN classes cl ON cs.class_id = cl.id " +
                 "JOIN subjects s ON s.course_id = cl.course_id " +
                 "LEFT JOIN attendance a ON a.subject_id = s.id AND a.class_id = cl.id " +
                 "WHERE cs.student_id = ? " +
                 "GROUP BY s.id, cl.id " +
                 "ORDER BY s.name")) {
            ps.setInt(1, student.getId());
            ResultSet rs = ps.executeQuery();
            
            while (rs.next()) {
                Map<String, Object> row = new HashMap<>();
                row.put("subject", rs.getString("subject"));
                row.put("class", rs.getString("class"));
                row.put("lectures", String.valueOf(rs.getInt("lectures")));
                table.getItems().add(row);
            }
        } catch (Exception ex) {
            ex.printStackTrace();
        }
        
        content.getChildren().addAll(title, table);
        mainLayout.setCenter(content);
    }
    
    private void showDefaulterStatus() {
        VBox content = new VBox(20);
        content.setPadding(new Insets(30));
        
        Label title = new Label("Defaulter Status");
        title.setStyle("-fx-font-size: 24px; -fx-font-weight: bold;");
        
        VBox statusBox = new VBox(15);
        statusBox.setStyle("-fx-background-color: white; -fx-padding: 20; -fx-background-radius: 10;");
        
        try (Connection conn = DatabaseConfig.getConnection();
             PreparedStatement ps = conn.prepareStatement(
                 "SELECT s.name as subject, cl.name as class, " +
                 "COUNT(*) as total, " +
                 "SUM(CASE WHEN a.status = 'PRESENT' THEN 1 ELSE 0 END) as present " +
                 "FROM class_students cs " +
                 "JOIN classes cl ON cs.class_id = cl.id " +
                 "JOIN subjects s ON s.course_id = cl.course_id " +
                 "LEFT JOIN attendance a ON a.student_id = ? AND a.subject_id = s.id AND a.class_id = cl.id " +
                 "WHERE cs.student_id = ? " +
                 "GROUP BY s.id, cl.id " +
                 "HAVING total > 0 " +
                 "ORDER BY s.name")) {
            ps.setInt(1, student.getId());
            ps.setInt(2, student.getId());
            ResultSet rs = ps.executeQuery();
            
            boolean hasDefaulter = false;
            List<String> defaulterSubjects = new ArrayList<>();
            List<String> safeSubjects = new ArrayList<>();
            
            while (rs.next()) {
                int total = rs.getInt("total");
                int present = rs.getInt("present");
                double percentage = (present * 100.0) / total;
                String subject = rs.getString("subject");
                String className = rs.getString("class");
                
                if (percentage < 75) {
                    hasDefaulter = true;
                    defaulterSubjects.add(String.format("%s (%s): %.2f%%", subject, className, percentage));
                } else {
                    safeSubjects.add(String.format("%s (%s): %.2f%%", subject, className, percentage));
                }
            }
            
            if (hasDefaulter) {
                Label warningLabel = new Label("⚠ WARNING: You are a DEFAULTER in the following subjects:");
                warningLabel.setStyle("-fx-font-size: 18px; -fx-font-weight: bold; -fx-text-fill: #e74c3c;");
                statusBox.getChildren().add(warningLabel);
                
                VBox defaulterList = new VBox(5);
                defaulterList.setStyle("-fx-padding: 10; -fx-background-color: #fadbd8; -fx-background-radius: 5;");
                for (String subject : defaulterSubjects) {
                    Label subjectLabel = new Label("• " + subject);
                    subjectLabel.setStyle("-fx-font-size: 14px; -fx-text-fill: #c0392b;");
                    defaulterList.getChildren().add(subjectLabel);
                }
                statusBox.getChildren().add(defaulterList);
                
                Label actionLabel = new Label("Action Required: Your attendance is below 75%. Please attend classes regularly!");
                actionLabel.setStyle("-fx-font-size: 14px; -fx-text-fill: #e67e22; -fx-padding: 10 0 0 0;");
                statusBox.getChildren().add(actionLabel);
            } else {
                Label successLabel = new Label("✓ Good News! You are NOT a defaulter in any subject.");
                successLabel.setStyle("-fx-font-size: 18px; -fx-font-weight: bold; -fx-text-fill: #27ae60;");
                statusBox.getChildren().add(successLabel);
            }
            
            if (!safeSubjects.isEmpty()) {
                Label safeLabel = new Label("\nSubjects with Good Attendance (≥75%):");
                safeLabel.setStyle("-fx-font-size: 16px; -fx-font-weight: bold; -fx-text-fill: #2c3e50; -fx-padding: 10 0 0 0;");
                statusBox.getChildren().add(safeLabel);
                
                VBox safeList = new VBox(5);
                safeList.setStyle("-fx-padding: 10; -fx-background-color: #d5f4e6; -fx-background-radius: 5;");
                for (String subject : safeSubjects) {
                    Label subjectLabel = new Label("• " + subject);
                    subjectLabel.setStyle("-fx-font-size: 14px; -fx-text-fill: #27ae60;");
                    safeList.getChildren().add(subjectLabel);
                }
                statusBox.getChildren().add(safeList);
            }
            
        } catch (Exception ex) {
            ex.printStackTrace();
            Label errorLabel = new Label("Error loading defaulter status");
            errorLabel.setStyle("-fx-text-fill: red;");
            statusBox.getChildren().add(errorLabel);
        }
        
        Label noteLabel = new Label("\nNote: Defaulter threshold is 75%. Maintain at least 75% attendance to avoid being marked as defaulter.");
        noteLabel.setStyle("-fx-font-size: 12px; -fx-text-fill: #7f8c8d; -fx-wrap-text: true;");
        
        content.getChildren().addAll(title, statusBox, noteLabel);
        ScrollPane scrollPane = new ScrollPane(content);
        scrollPane.setFitToWidth(true);
        mainLayout.setCenter(scrollPane);
    }
    
    private void logout() {
        new LoginScreen(stage).show();
    }
}