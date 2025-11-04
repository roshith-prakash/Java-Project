package javaproject;

import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.Scene;
import javafx.scene.control.*;
import javafx.scene.layout.*;
import javafx.stage.Stage;
import java.sql.*;
import java.time.LocalDate;
import java.util.*;

public class TeacherDashboard {
    private Stage stage;
    private User teacher;
    private BorderPane mainLayout;
    
    // Store assignment IDs separately
    private Map<String, Assignment> assignmentMap = new HashMap<>();
    
    private static class Assignment {
        int classId;
        int subjectId;
        String display;
        
        Assignment(int classId, int subjectId, String display) {
            this.classId = classId;
            this.subjectId = subjectId;
            this.display = display;
        }
    }
    
    public TeacherDashboard(Stage stage, User teacher) {
        this.stage = stage;
        this.teacher = teacher;
    }
    
    public void show() {
        mainLayout = new BorderPane();
        
        // Top bar
        HBox topBar = new HBox(20);
        topBar.setPadding(new Insets(15));
        topBar.setStyle("-fx-background-color: #2c3e50;");
        topBar.setAlignment(Pos.CENTER_LEFT);
        
        Label titleLabel = new Label("Teacher Dashboard - " + teacher.getName());
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
            createMenuButton("Take Attendance"),
            createMenuButton("Quick Actions"),
            createMenuButton("View Attendance"),
            createMenuButton("Modify Attendance"),
            createMenuButton("Check Defaulters"),
            createMenuButton("Quick Search")
        };
        
        menuButtons[0].setOnAction(e -> showTakeAttendance());
        menuButtons[1].setOnAction(e -> showQuickActions());
        menuButtons[2].setOnAction(e -> showViewAttendance());
        menuButtons[3].setOnAction(e -> showModifyAttendance());
        menuButtons[4].setOnAction(e -> showCheckDefaulters());
        menuButtons[5].setOnAction(e -> showQuickSearch());
        
        menu.getChildren().addAll(menuButtons);
        
        mainLayout.setTop(topBar);
        mainLayout.setLeft(menu);
        
        showTakeAttendance();
        
        Scene scene = new Scene(mainLayout, 1200, 700);
        stage.setScene(scene);
        stage.setTitle("Teacher Dashboard");
    }
    
    private Button createMenuButton(String text) {
        Button btn = new Button(text);
        btn.setMaxWidth(Double.MAX_VALUE);
        btn.setStyle("-fx-background-color: transparent; -fx-text-fill: white; -fx-alignment: CENTER-LEFT; -fx-padding: 12; -fx-cursor: hand;");
        btn.setOnMouseEntered(e -> btn.setStyle("-fx-background-color: #3498db; -fx-text-fill: white; -fx-alignment: CENTER-LEFT; -fx-padding: 12; -fx-cursor: hand;"));
        btn.setOnMouseExited(e -> btn.setStyle("-fx-background-color: transparent; -fx-text-fill: white; -fx-alignment: CENTER-LEFT; -fx-padding: 12; -fx-cursor: hand;"));
        return btn;
    }
    
    private void showQuickActions() {
        VBox content = new VBox(20);
        content.setPadding(new Insets(30));
        
        Label title = new Label("Quick Actions");
        title.setStyle("-fx-font-size: 24px; -fx-font-weight: bold;");
        
        // Quick action cards
        GridPane actionsGrid = new GridPane();
        actionsGrid.setHgap(20);
        actionsGrid.setVgap(20);
        
        // Mark All Present Card
        VBox markAllPresentCard = createQuickActionCard(
            "Mark All Present", 
            "Mark all students present for today's lecture",
            "#27ae60",
            e -> showMarkAllPresentDialog()
        );
        
        // Mark All Absent Card
        VBox markAllAbsentCard = createQuickActionCard(
            "Mark All Absent", 
            "Mark all students absent for today's lecture",
            "#e74c3c",
            e -> showMarkAllAbsentDialog()
        );
        
        // Copy Previous Day Card
        VBox copyPreviousCard = createQuickActionCard(
            "Copy Previous Day", 
            "Copy attendance from previous lecture",
            "#3498db",
            e -> showCopyPreviousDialog()
        );
        
        // Bulk Update Card
        VBox bulkUpdateCard = createQuickActionCard(
            "Bulk Update", 
            "Update multiple students at once",
            "#9b59b6",
            e -> showBulkUpdateDialog()
        );
        
        actionsGrid.add(markAllPresentCard, 0, 0);
        actionsGrid.add(markAllAbsentCard, 1, 0);
        actionsGrid.add(copyPreviousCard, 0, 1);
        actionsGrid.add(bulkUpdateCard, 1, 1);
        
        Label infoLabel = new Label("Quick actions help you manage attendance efficiently for entire classes.");
        infoLabel.setStyle("-fx-font-size: 14px; -fx-text-fill: #7f8c8d; -fx-wrap-text: true;");
        
        content.getChildren().addAll(title, actionsGrid, infoLabel);
        
        // Wrap content in ScrollPane to make it scrollable
        ScrollPane scrollPane = new ScrollPane(content);
        scrollPane.setFitToWidth(true);
        scrollPane.setFitToHeight(true);
        scrollPane.setVbarPolicy(ScrollPane.ScrollBarPolicy.AS_NEEDED);
        scrollPane.setHbarPolicy(ScrollPane.ScrollBarPolicy.NEVER);
        scrollPane.setStyle("-fx-background-color: transparent;");
        
        mainLayout.setCenter(scrollPane);
    }
    
    private VBox createQuickActionCard(String title, String description, String color, javafx.event.EventHandler<javafx.event.ActionEvent> action) {
        VBox card = new VBox(15);
        card.setAlignment(Pos.CENTER);
        card.setPadding(new Insets(25));
        card.setStyle("-fx-background-color: white; -fx-border-color: " + color + "; -fx-border-width: 2; -fx-background-radius: 10; -fx-border-radius: 10; -fx-effect: dropshadow(three-pass-box, rgba(0,0,0,0.1), 5, 0, 0, 2);");
        card.setPrefWidth(250);
        card.setPrefHeight(150);
        card.setCursor(javafx.scene.Cursor.HAND);
        
        Label titleLabel = new Label(title);
        titleLabel.setStyle("-fx-text-fill: " + color + "; -fx-font-size: 16px; -fx-font-weight: bold;");
        titleLabel.setAlignment(Pos.CENTER);
        
        Label descLabel = new Label(description);
        descLabel.setStyle("-fx-text-fill: #7f8c8d; -fx-font-size: 12px;");
        descLabel.setWrapText(true);
        descLabel.setAlignment(Pos.CENTER);
        
        Button actionBtn = new Button("Execute");
        actionBtn.setStyle("-fx-background-color: " + color + "; -fx-text-fill: white; -fx-cursor: hand; -fx-padding: 8 16;");
        actionBtn.setOnAction(action);
        
        card.getChildren().addAll(titleLabel, descLabel, actionBtn);
        
        // Hover effects
        card.setOnMouseEntered(e -> card.setStyle("-fx-background-color: #f8f9fa; -fx-border-color: " + color + "; -fx-border-width: 2; -fx-background-radius: 10; -fx-border-radius: 10; -fx-effect: dropshadow(three-pass-box, rgba(0,0,0,0.2), 8, 0, 0, 3);"));
        card.setOnMouseExited(e -> card.setStyle("-fx-background-color: white; -fx-border-color: " + color + "; -fx-border-width: 2; -fx-background-radius: 10; -fx-border-radius: 10; -fx-effect: dropshadow(three-pass-box, rgba(0,0,0,0.1), 5, 0, 0, 2);"));
        
        return card;
    }
    
    private void showMarkAllPresentDialog() {
        Stage dialog = new Stage();
        dialog.setTitle("Mark All Present");
        
        VBox form = new VBox(15);
        form.setPadding(new Insets(20));
        
        Label titleLabel = new Label("Mark All Students Present");
        titleLabel.setStyle("-fx-font-size: 16px; -fx-font-weight: bold;");
        
        ComboBox<String> classSubjectCombo = new ComboBox<>();
        classSubjectCombo.setPromptText("Select Class & Subject");
        loadTeacherAssignments(classSubjectCombo);
        
        DatePicker datePicker = new DatePicker(java.time.LocalDate.now());
        
        ComboBox<String> timeSlotCombo = new ComboBox<>();
        timeSlotCombo.setPromptText("Select Time Slot");
        timeSlotCombo.getItems().addAll(
            "09:00 - 10:00", "10:00 - 11:00", "11:00 - 12:00", "12:00 - 13:00",
            "13:00 - 14:00", "14:00 - 15:00", "15:00 - 16:00", "16:00 - 17:00"
        );
        timeSlotCombo.setValue("09:00 - 10:00"); // Default selection
        
        Button executeBtn = new Button("Mark All Present");
        executeBtn.setStyle("-fx-background-color: #27ae60; -fx-text-fill: white; -fx-cursor: hand;");
        
        Label msgLabel = new Label();
        
        executeBtn.setOnAction(e -> {
            String selection = classSubjectCombo.getValue();
            java.time.LocalDate date = datePicker.getValue();
            String timeSlot = timeSlotCombo.getValue();
            
            if (selection == null) {
                msgLabel.setText("Please select a class & subject");
                msgLabel.setStyle("-fx-text-fill: red;");
                return;
            }
            
            if (date == null) {
                msgLabel.setText("Please select a date");
                msgLabel.setStyle("-fx-text-fill: red;");
                return;
            }
            
            if (timeSlot == null) {
                msgLabel.setText("Please select a time slot");
                msgLabel.setStyle("-fx-text-fill: red;");
                return;
            }
            
            Assignment assignment = assignmentMap.get(selection);
            if (assignment == null) {
                msgLabel.setText("Error: Assignment not found for selection: " + selection);
                msgLabel.setStyle("-fx-text-fill: red;");
                return;
            }
            
            int classId = assignment.classId;
            int subjectId = assignment.subjectId;
            String timeValue = timeSlot.split(" - ")[0] + ":00"; // Convert "09:00 - 10:00" to "09:00:00"
            
            System.out.println("Marking all present for ClassID: " + classId + ", SubjectID: " + subjectId + ", Date: " + date + ", Time: " + timeValue);
            
            try (Connection conn = DatabaseConfig.getConnection()) {
                // First check if there are students in this class
                try (PreparedStatement checkPs = conn.prepareStatement(
                    "SELECT COUNT(*) FROM class_students WHERE class_id = ?")) {
                    checkPs.setInt(1, classId);
                    ResultSet rs = checkPs.executeQuery();
                    rs.next();
                    int studentCount = rs.getInt(1);
                    
                    if (studentCount == 0) {
                        msgLabel.setText("No students found in this class");
                        msgLabel.setStyle("-fx-text-fill: orange;");
                        return;
                    }
                    
                    System.out.println("Found " + studentCount + " students in class");
                }
                
                // Now mark attendance with time slot
                try (PreparedStatement ps = conn.prepareStatement(
                     "INSERT INTO attendance (student_id, subject_id, class_id, date, time_slot, status, marked_by) " +
                     "SELECT cs.student_id, ?, ?, ?, ?, 'PRESENT', ? " +
                     "FROM class_students cs " +
                     "WHERE cs.class_id = ? " +
                     "ON DUPLICATE KEY UPDATE status = 'PRESENT', marked_by = ?, marked_at = CURRENT_TIMESTAMP")) {
                    
                    ps.setInt(1, subjectId);
                    ps.setInt(2, classId);
                    ps.setDate(3, java.sql.Date.valueOf(date));
                    ps.setString(4, timeValue);
                    ps.setInt(5, teacher.getId());
                    ps.setInt(6, classId);
                    ps.setInt(7, teacher.getId());
                    
                    int count = ps.executeUpdate();
                    
                    // Record lecture session and update conducted lectures count
                    try (PreparedStatement sessionPs = conn.prepareStatement(
                        "INSERT INTO lecture_sessions (subject_id, class_id, date, time_slot, conducted_by) " +
                        "VALUES (?, ?, ?, ?, ?) " +
                        "ON DUPLICATE KEY UPDATE conducted_by = ?")) {
                        
                        sessionPs.setInt(1, subjectId);
                        sessionPs.setInt(2, classId);
                        sessionPs.setDate(3, java.sql.Date.valueOf(date));
                        sessionPs.setString(4, timeValue);
                        sessionPs.setInt(5, teacher.getId());
                        sessionPs.setInt(6, teacher.getId());
                        sessionPs.executeUpdate();
                    }
                    
                    // Update conducted lectures count
                    try (PreparedStatement updatePs = conn.prepareStatement(
                        "UPDATE subjects SET conducted_lectures = (" +
                        "SELECT COUNT(DISTINCT ls.date, ls.time_slot) FROM lecture_sessions ls " +
                        "WHERE ls.subject_id = ?" +
                        ") WHERE id = ?")) {
                        
                        updatePs.setInt(1, subjectId);
                        updatePs.setInt(2, subjectId);
                        updatePs.executeUpdate();
                    }
                    
                    msgLabel.setText("Successfully marked " + count + " students as present for " + timeSlot + "!");
                    msgLabel.setStyle("-fx-text-fill: green;");
                    System.out.println("Updated " + count + " attendance records");
                }
                
            } catch (Exception ex) {
                msgLabel.setText("Error: " + ex.getMessage());
                msgLabel.setStyle("-fx-text-fill: red;");
                ex.printStackTrace();
            }
        });
        
        Button cancelBtn = new Button("Cancel");
        cancelBtn.setOnAction(e -> dialog.close());
        
        HBox buttons = new HBox(10, executeBtn, cancelBtn);
        
        form.getChildren().addAll(
            titleLabel,
            new Label("Class & Subject:"), classSubjectCombo,
            new Label("Date:"), datePicker,
            new Label("Time Slot:"), timeSlotCombo,
            buttons,
            msgLabel
        );
        
        Scene scene = new Scene(form, 350, 350);
        dialog.setScene(scene);
        dialog.show();
    }
    
    private void showMarkAllAbsentDialog() {
        Stage dialog = new Stage();
        dialog.setTitle("Mark All Absent");
        
        VBox form = new VBox(15);
        form.setPadding(new Insets(20));
        
        Label titleLabel = new Label("Mark All Students Absent");
        titleLabel.setStyle("-fx-font-size: 16px; -fx-font-weight: bold;");
        
        ComboBox<String> classSubjectCombo = new ComboBox<>();
        classSubjectCombo.setPromptText("Select Class & Subject");
        loadTeacherAssignments(classSubjectCombo);
        
        DatePicker datePicker = new DatePicker(java.time.LocalDate.now());
        
        ComboBox<String> timeSlotCombo = new ComboBox<>();
        timeSlotCombo.setPromptText("Select Time Slot");
        timeSlotCombo.getItems().addAll(
            "09:00 - 10:00", "10:00 - 11:00", "11:00 - 12:00", "12:00 - 13:00",
            "13:00 - 14:00", "14:00 - 15:00", "15:00 - 16:00", "16:00 - 17:00"
        );
        timeSlotCombo.setValue("09:00 - 10:00"); // Default selection
        
        Button executeBtn = new Button("Mark All Absent");
        executeBtn.setStyle("-fx-background-color: #e74c3c; -fx-text-fill: white; -fx-cursor: hand;");
        
        Label msgLabel = new Label();
        
        executeBtn.setOnAction(e -> {
            String selection = classSubjectCombo.getValue();
            java.time.LocalDate date = datePicker.getValue();
            String timeSlot = timeSlotCombo.getValue();
            
            if (selection == null) {
                msgLabel.setText("Please select a class & subject");
                msgLabel.setStyle("-fx-text-fill: red;");
                return;
            }
            
            if (date == null) {
                msgLabel.setText("Please select a date");
                msgLabel.setStyle("-fx-text-fill: red;");
                return;
            }
            
            if (timeSlot == null) {
                msgLabel.setText("Please select a time slot");
                msgLabel.setStyle("-fx-text-fill: red;");
                return;
            }
            
            Assignment assignment = assignmentMap.get(selection);
            if (assignment == null) {
                msgLabel.setText("Error: Assignment not found");
                msgLabel.setStyle("-fx-text-fill: red;");
                return;
            }
            
            int classId = assignment.classId;
            int subjectId = assignment.subjectId;
            String timeValue = timeSlot.split(" - ")[0] + ":00"; // Convert "09:00 - 10:00" to "09:00:00"
            
            try (Connection conn = DatabaseConfig.getConnection();
                 PreparedStatement ps = conn.prepareStatement(
                     "INSERT INTO attendance (student_id, subject_id, class_id, date, time_slot, status, marked_by) " +
                     "SELECT cs.student_id, ?, ?, ?, ?, 'ABSENT', ? " +
                     "FROM class_students cs " +
                     "WHERE cs.class_id = ? " +
                     "ON DUPLICATE KEY UPDATE status = 'ABSENT', marked_by = ?, marked_at = CURRENT_TIMESTAMP")) {
                
                ps.setInt(1, subjectId);
                ps.setInt(2, classId);
                ps.setDate(3, java.sql.Date.valueOf(date));
                ps.setString(4, timeValue);
                ps.setInt(5, teacher.getId());
                ps.setInt(6, classId);
                ps.setInt(7, teacher.getId());
                
                int count = ps.executeUpdate();
                
                // Record lecture session and update conducted lectures count
                try (PreparedStatement sessionPs = conn.prepareStatement(
                    "INSERT INTO lecture_sessions (subject_id, class_id, date, time_slot, conducted_by) " +
                    "VALUES (?, ?, ?, ?, ?) " +
                    "ON DUPLICATE KEY UPDATE conducted_by = ?")) {
                    
                    sessionPs.setInt(1, subjectId);
                    sessionPs.setInt(2, classId);
                    sessionPs.setDate(3, java.sql.Date.valueOf(date));
                    sessionPs.setString(4, timeValue);
                    sessionPs.setInt(5, teacher.getId());
                    sessionPs.setInt(6, teacher.getId());
                    sessionPs.executeUpdate();
                }
                
                // Update conducted lectures count
                try (PreparedStatement updatePs = conn.prepareStatement(
                    "UPDATE subjects SET conducted_lectures = (" +
                    "SELECT COUNT(DISTINCT ls.date, ls.time_slot) FROM lecture_sessions ls " +
                    "WHERE ls.subject_id = ?" +
                    ") WHERE id = ?")) {
                    
                    updatePs.setInt(1, subjectId);
                    updatePs.setInt(2, subjectId);
                    updatePs.executeUpdate();
                }
                
                msgLabel.setText("Successfully marked " + count + " students as absent for " + timeSlot + "!");
                msgLabel.setStyle("-fx-text-fill: green;");
                
            } catch (Exception ex) {
                msgLabel.setText("Error: " + ex.getMessage());
                msgLabel.setStyle("-fx-text-fill: red;");
                ex.printStackTrace();
            }
        });
        
        Button cancelBtn = new Button("Cancel");
        cancelBtn.setOnAction(e -> dialog.close());
        
        HBox buttons = new HBox(10, executeBtn, cancelBtn);
        
        form.getChildren().addAll(
            titleLabel,
            new Label("Class & Subject:"), classSubjectCombo,
            new Label("Date:"), datePicker,
            new Label("Time Slot:"), timeSlotCombo,
            buttons,
            msgLabel
        );
        
        Scene scene = new Scene(form, 350, 300);
        dialog.setScene(scene);
        dialog.show();
    }
    
    private void showCopyPreviousDialog() {
        Stage dialog = new Stage();
        dialog.setTitle("Copy Previous Day Attendance");
        
        VBox form = new VBox(15);
        form.setPadding(new Insets(20));
        
        Label titleLabel = new Label("Copy Previous Day Attendance");
        titleLabel.setStyle("-fx-font-size: 16px; -fx-font-weight: bold;");
        
        ComboBox<String> classSubjectCombo = new ComboBox<>();
        classSubjectCombo.setPromptText("Select Class & Subject");
        loadTeacherAssignments(classSubjectCombo);
        
        DatePicker fromDatePicker = new DatePicker();
        DatePicker toDatePicker = new DatePicker(java.time.LocalDate.now());
        
        Button executeBtn = new Button("Copy Attendance");
        executeBtn.setStyle("-fx-background-color: #3498db; -fx-text-fill: white; -fx-cursor: hand;");
        
        Label msgLabel = new Label();
        
        executeBtn.setOnAction(e -> {
            String selection = classSubjectCombo.getValue();
            java.time.LocalDate fromDate = fromDatePicker.getValue();
            java.time.LocalDate toDate = toDatePicker.getValue();
            
            if (selection != null && fromDate != null && toDate != null) {
                Assignment assignment = assignmentMap.get(selection);
                int classId = assignment.classId;
                int subjectId = assignment.subjectId;
                
                try (Connection conn = DatabaseConfig.getConnection();
                     PreparedStatement ps = conn.prepareStatement(
                         "INSERT INTO attendance (student_id, subject_id, class_id, date, time_slot, status, marked_by) " +
                         "SELECT student_id, subject_id, class_id, ?, time_slot, status, ? " +
                         "FROM attendance " +
                         "WHERE class_id = ? AND subject_id = ? AND date = ? " +
                         "ON DUPLICATE KEY UPDATE status = VALUES(status), marked_by = VALUES(marked_by), marked_at = CURRENT_TIMESTAMP")) {
                    
                    ps.setDate(1, java.sql.Date.valueOf(toDate));
                    ps.setInt(2, teacher.getId());
                    ps.setInt(3, classId);
                    ps.setInt(4, subjectId);
                    ps.setDate(5, java.sql.Date.valueOf(fromDate));
                    
                    int count = ps.executeUpdate();
                    msgLabel.setText("Successfully copied attendance for " + count + " students!");
                    msgLabel.setStyle("-fx-text-fill: green;");
                    
                } catch (Exception ex) {
                    msgLabel.setText("Error: " + ex.getMessage());
                    msgLabel.setStyle("-fx-text-fill: red;");
                    ex.printStackTrace();
                }
            }
        });
        
        Button cancelBtn = new Button("Cancel");
        cancelBtn.setOnAction(e -> dialog.close());
        
        HBox buttons = new HBox(10, executeBtn, cancelBtn);
        
        form.getChildren().addAll(
            titleLabel,
            new Label("Class & Subject:"), classSubjectCombo,
            new Label("Copy From Date:"), fromDatePicker,
            new Label("Copy To Date:"), toDatePicker,
            buttons,
            msgLabel
        );
        
        Scene scene = new Scene(form, 350, 350);
        dialog.setScene(scene);
        dialog.show();
    }
    
    private void showBulkUpdateDialog() {
        Stage dialog = new Stage();
        dialog.setTitle("Bulk Update Attendance");
        dialog.setWidth(600);
        dialog.setHeight(500);
        
        VBox form = new VBox(15);
        form.setPadding(new Insets(20));
        
        Label titleLabel = new Label("Bulk Update Attendance");
        titleLabel.setStyle("-fx-font-size: 16px; -fx-font-weight: bold;");
        
        ComboBox<String> classSubjectCombo = new ComboBox<>();
        classSubjectCombo.setPromptText("Select Class & Subject");
        loadTeacherAssignments(classSubjectCombo);
        
        DatePicker datePicker = new DatePicker(java.time.LocalDate.now());
        
        ComboBox<String> timeSlotCombo = new ComboBox<>();
        timeSlotCombo.setPromptText("Select Time Slot");
        timeSlotCombo.getItems().addAll(
            "09:00 - 10:00", "10:00 - 11:00", "11:00 - 12:00", "12:00 - 13:00",
            "13:00 - 14:00", "14:00 - 15:00", "15:00 - 16:00", "16:00 - 17:00"
        );
        timeSlotCombo.setValue("09:00 - 10:00"); // Default selection
        
        TableView<Map<String, Object>> studentsTable = new TableView<>();
        studentsTable.setPrefHeight(250);
        
        TableColumn<Map<String, Object>, String> nameCol = new TableColumn<>("Student Name");
        nameCol.setCellValueFactory(data -> new javafx.beans.property.SimpleStringProperty(data.getValue().get("name").toString()));
        nameCol.setPrefWidth(200);
        
        TableColumn<Map<String, Object>, String> statusCol = new TableColumn<>("Status");
        statusCol.setCellFactory(col -> new TableCell<Map<String, Object>, String>() {
            private final ComboBox<String> statusCombo = new ComboBox<>();
            {
                statusCombo.getItems().addAll("PRESENT", "ABSENT");
                statusCombo.setOnAction(e -> {
                    Map<String, Object> row = getTableView().getItems().get(getIndex());
                    row.put("status", statusCombo.getValue());
                });
            }
            
            @Override
            protected void updateItem(String item, boolean empty) {
                super.updateItem(item, empty);
                if (empty) {
                    setGraphic(null);
                } else {
                    Map<String, Object> row = getTableView().getItems().get(getIndex());
                    statusCombo.setValue(row.get("status").toString());
                    setGraphic(statusCombo);
                }
            }
        });
        statusCol.setPrefWidth(150);
        
        studentsTable.getColumns().addAll(nameCol, statusCol);
        
        // Update students list when any selection changes
        Runnable updateStudentsList = () -> {
            String selection = classSubjectCombo.getValue();
            java.time.LocalDate date = datePicker.getValue();
            String timeSlot = timeSlotCombo.getValue();
            
            if (selection != null && date != null && timeSlot != null) {
                studentsTable.getItems().clear();
                Assignment assignment = assignmentMap.get(selection);
                int classId = assignment.classId;
                int subjectId = assignment.subjectId;
                String timeValue = timeSlot.split(" - ")[0] + ":00";
                
                try (Connection conn = DatabaseConfig.getConnection();
                     PreparedStatement ps = conn.prepareStatement(
                         "SELECT u.id, u.name, COALESCE(a.status, 'PRESENT') as status " +
                         "FROM class_students cs " +
                         "JOIN users u ON cs.student_id = u.id " +
                         "LEFT JOIN attendance a ON u.id = a.student_id AND a.class_id = ? AND a.subject_id = ? AND a.date = ? AND a.time_slot = ? " +
                         "WHERE cs.class_id = ? ORDER BY u.name")) {
                    
                    ps.setInt(1, classId);
                    ps.setInt(2, subjectId);
                    ps.setDate(3, java.sql.Date.valueOf(date));
                    ps.setString(4, timeValue);
                    ps.setInt(5, classId);
                    
                    ResultSet rs = ps.executeQuery();
                    while (rs.next()) {
                        Map<String, Object> row = new HashMap<>();
                        row.put("id", rs.getInt("id"));
                        row.put("name", rs.getString("name"));
                        row.put("status", rs.getString("status"));
                        studentsTable.getItems().add(row);
                    }
                } catch (Exception ex) {
                    ex.printStackTrace();
                }
            }
        };
        
        classSubjectCombo.setOnAction(e -> updateStudentsList.run());
        datePicker.setOnAction(e -> updateStudentsList.run());
        timeSlotCombo.setOnAction(e -> updateStudentsList.run());
        
        Button saveBtn = new Button("Save All Changes");
        saveBtn.setStyle("-fx-background-color: #27ae60; -fx-text-fill: white; -fx-cursor: hand;");
        
        Label msgLabel = new Label();
        
        saveBtn.setOnAction(e -> {
            String selection = classSubjectCombo.getValue();
            java.time.LocalDate date = datePicker.getValue();
            String timeSlot = timeSlotCombo.getValue();
            
            if (selection != null && date != null && timeSlot != null && !studentsTable.getItems().isEmpty()) {
                Assignment assignment = assignmentMap.get(selection);
                int classId = assignment.classId;
                int subjectId = assignment.subjectId;
                String timeValue = timeSlot.split(" - ")[0] + ":00";
                
                try (Connection conn = DatabaseConfig.getConnection();
                     PreparedStatement ps = conn.prepareStatement(
                         "INSERT INTO attendance (student_id, subject_id, class_id, date, time_slot, status, marked_by) " +
                         "VALUES (?, ?, ?, ?, ?, ?, ?) " +
                         "ON DUPLICATE KEY UPDATE status = ?, marked_by = ?, marked_at = CURRENT_TIMESTAMP")) {
                    
                    for (Map<String, Object> row : studentsTable.getItems()) {
                        int studentId = (int) row.get("id");
                        String status = row.get("status").toString();
                        
                        ps.setInt(1, studentId);
                        ps.setInt(2, subjectId);
                        ps.setInt(3, classId);
                        ps.setDate(4, java.sql.Date.valueOf(date));
                        ps.setString(5, timeValue);
                        ps.setString(6, status);
                        ps.setInt(7, teacher.getId());
                        ps.setString(8, status);
                        ps.setInt(9, teacher.getId());
                        ps.addBatch();
                    }
                    
                    ps.executeBatch();
                    
                    // Record lecture session and update conducted lectures count
                    try (PreparedStatement sessionPs = conn.prepareStatement(
                        "INSERT INTO lecture_sessions (subject_id, class_id, date, time_slot, conducted_by) " +
                        "VALUES (?, ?, ?, ?, ?) " +
                        "ON DUPLICATE KEY UPDATE conducted_by = ?")) {
                        
                        sessionPs.setInt(1, subjectId);
                        sessionPs.setInt(2, classId);
                        sessionPs.setDate(3, java.sql.Date.valueOf(date));
                        sessionPs.setString(4, timeValue);
                        sessionPs.setInt(5, teacher.getId());
                        sessionPs.setInt(6, teacher.getId());
                        sessionPs.executeUpdate();
                    }
                    
                    // Update conducted lectures count
                    try (PreparedStatement updatePs = conn.prepareStatement(
                        "UPDATE subjects SET conducted_lectures = (" +
                        "SELECT COUNT(DISTINCT ls.date, ls.time_slot) FROM lecture_sessions ls " +
                        "WHERE ls.subject_id = ?" +
                        ") WHERE id = ?")) {
                        
                        updatePs.setInt(1, subjectId);
                        updatePs.setInt(2, subjectId);
                        updatePs.executeUpdate();
                    }
                    
                    msgLabel.setText("Successfully updated attendance for " + studentsTable.getItems().size() + " students!");
                    msgLabel.setStyle("-fx-text-fill: green;");
                    
                } catch (Exception ex) {
                    msgLabel.setText("Error: " + ex.getMessage());
                    msgLabel.setStyle("-fx-text-fill: red;");
                    ex.printStackTrace();
                }
            }
        });
        
        Button cancelBtn = new Button("Cancel");
        cancelBtn.setOnAction(e -> dialog.close());
        
        HBox buttons = new HBox(10, saveBtn, cancelBtn);
        
        form.getChildren().addAll(
            titleLabel,
            new Label("Class & Subject:"), classSubjectCombo,
            new Label("Date:"), datePicker,
            new Label("Time Slot:"), timeSlotCombo,
            new Label("Students:"), studentsTable,
            buttons,
            msgLabel
        );
        
        Scene scene = new Scene(form);
        dialog.setScene(scene);
        dialog.show();
    }

    private void showTakeAttendance() {
        VBox content = new VBox(15); // Reduced spacing to give more room to student list
        content.setPadding(new Insets(20)); // Reduced padding to maximize space
        
        Label title = new Label("Take Attendance");
        title.setStyle("-fx-font-size: 24px; -fx-font-weight: bold;");
        
        // Create lecture slots overview section
        VBox lectureSlotSection = createLectureSlotSection();
        
        // Separator
        Label separator = new Label("Manual Attendance Entry");
        separator.setStyle("-fx-font-size: 18px; -fx-font-weight: bold; -fx-text-fill: #34495e;");
        
        ComboBox<String> classSubjectCombo = new ComboBox<>();
        classSubjectCombo.setPromptText("Select Class & Subject");
        loadTeacherAssignments(classSubjectCombo);
        
        DatePicker datePicker = new DatePicker(LocalDate.now());
        
        ComboBox<String> timeSlotCombo = new ComboBox<>();
        timeSlotCombo.setPromptText("Select Time Slot");
        timeSlotCombo.getItems().addAll(
            "09:00 - 10:00", "10:00 - 11:00", "11:00 - 12:00", "12:00 - 13:00",
            "13:00 - 14:00", "14:00 - 15:00", "15:00 - 16:00", "16:00 - 17:00"
        );
        timeSlotCombo.setValue("09:00 - 10:00"); // Default selection
        
        VBox studentsList = new VBox(5); // Reduced spacing to fit more students
        studentsList.setPadding(new Insets(8)); // Reduced padding to maximize space
        ScrollPane studentsScroll = new ScrollPane(studentsList);
        studentsScroll.setFitToWidth(true);
        studentsScroll.setPrefHeight(800); // Increased to 800px for better visibility
        studentsScroll.setMinHeight(600); // Set minimum height to ensure it's always large enough
        studentsScroll.setStyle("-fx-background-color: #f8f9fa; -fx-border-color: #dee2e6; -fx-border-width: 1; -fx-border-radius: 5;");
        studentsScroll.setVbarPolicy(ScrollPane.ScrollBarPolicy.AS_NEEDED);
        studentsScroll.setHbarPolicy(ScrollPane.ScrollBarPolicy.NEVER);
        
        // Add Select All / Deselect All buttons
        HBox selectionButtons = new HBox(10);
        selectionButtons.setAlignment(Pos.CENTER);
        
        Button selectAllBtn = new Button("Select All (Present)");
        selectAllBtn.setStyle("-fx-background-color: #3498db; -fx-text-fill: white; -fx-font-size: 12px; -fx-cursor: hand;");
        
        Button deselectAllBtn = new Button("Deselect All (Absent)");
        deselectAllBtn.setStyle("-fx-background-color: #e74c3c; -fx-text-fill: white; -fx-font-size: 12px; -fx-cursor: hand;");
        
        selectionButtons.getChildren().addAll(selectAllBtn, deselectAllBtn);
        
        Button submitBtn = new Button("Submit Attendance");
        submitBtn.setStyle("-fx-background-color: #27ae60; -fx-text-fill: white; -fx-font-size: 14px; -fx-cursor: hand;");
        submitBtn.setDisable(true);
        
        Label msgLabel = new Label();
        
        Map<Integer, CheckBox> attendanceMap = new HashMap<>();
        
        classSubjectCombo.setOnAction(comboEvent -> {
            String selection = classSubjectCombo.getValue();
            if (selection != null) {
                studentsList.getChildren().clear();
                attendanceMap.clear();
                msgLabel.setText("");
                
                Assignment assignment = assignmentMap.get(selection);
                if (assignment == null) {
                    msgLabel.setText("Error: Could not load assignment details");
                    msgLabel.setStyle("-fx-text-fill: red;");
                    return;
                }
                
                int classId = assignment.classId;
                int subjectId = assignment.subjectId;
                
                System.out.println("Loading students for classId=" + classId + ", subjectId=" + subjectId);
                
                try (Connection conn = DatabaseConfig.getConnection();
                     PreparedStatement ps = conn.prepareStatement(
                         "SELECT u.id, u.name FROM class_students cs " +
                         "JOIN users u ON cs.student_id = u.id " +
                         "WHERE cs.class_id = ? ORDER BY u.name")) {
                    ps.setInt(1, classId);
                    ResultSet rs = ps.executeQuery();
                    
                    int count = 0;
                    while (rs.next()) {
                        int studentId = rs.getInt("id");
                        String studentName = rs.getString("name");
                        
                        CheckBox cb = new CheckBox(studentName + " (ID: " + studentId + ")");
                        cb.setSelected(true); // Default to present
                        cb.setStyle("-fx-font-size: 14px; -fx-padding: 6; -fx-background-color: white; " +
                                   "-fx-border-color: #e9ecef; -fx-border-width: 1; -fx-border-radius: 3; " +
                                   "-fx-background-radius: 3;"); // Reduced padding slightly
                        
                        // Add hover effect
                        cb.setOnMouseEntered(mouseEvent -> cb.setStyle("-fx-font-size: 14px; -fx-padding: 6; " +
                                                             "-fx-background-color: #f8f9fa; -fx-border-color: #3498db; " +
                                                             "-fx-border-width: 1; -fx-border-radius: 3; -fx-background-radius: 3;"));
                        cb.setOnMouseExited(mouseEvent -> cb.setStyle("-fx-font-size: 14px; -fx-padding: 6; " +
                                                            "-fx-background-color: white; -fx-border-color: #e9ecef; " +
                                                            "-fx-border-width: 1; -fx-border-radius: 3; -fx-background-radius: 3;"));
                        
                        attendanceMap.put(studentId, cb);
                        studentsList.getChildren().add(cb);
                        count++;
                    }
                    
                    System.out.println("Found " + count + " students");
                    
                    if (count == 0) {
                        Label noStudentsLabel = new Label("No students found in this class.");
                        noStudentsLabel.setStyle("-fx-text-fill: orange; -fx-font-size: 14px;");
                        studentsList.getChildren().add(noStudentsLabel);
                        submitBtn.setDisable(true);
                    } else {
                        submitBtn.setDisable(false);
                    }
                } catch (Exception ex) {
                    msgLabel.setText("Error loading students: " + ex.getMessage());
                    msgLabel.setStyle("-fx-text-fill: red;");
                    ex.printStackTrace();
                }
            }
        });
        
        // Event handlers for selection buttons
        selectAllBtn.setOnAction(selectEvent -> {
            for (CheckBox cb : attendanceMap.values()) {
                cb.setSelected(true);
            }
        });
        
        deselectAllBtn.setOnAction(deselectEvent -> {
            for (CheckBox cb : attendanceMap.values()) {
                cb.setSelected(false);
            }
        });
        
        submitBtn.setOnAction(submitEvent -> {
            String selection = classSubjectCombo.getValue();
            LocalDate date = datePicker.getValue();
            String timeSlot = timeSlotCombo.getValue();
            
            if (selection == null) {
                msgLabel.setText("Please select a class & subject");
                msgLabel.setStyle("-fx-text-fill: red;");
                return;
            }
            
            if (date == null) {
                msgLabel.setText("Please select a date");
                msgLabel.setStyle("-fx-text-fill: red;");
                return;
            }
            
            if (timeSlot == null) {
                msgLabel.setText("Please select a time slot");
                msgLabel.setStyle("-fx-text-fill: red;");
                return;
            }
            
            if (attendanceMap.isEmpty()) {
                msgLabel.setText("No students to mark attendance for");
                msgLabel.setStyle("-fx-text-fill: red;");
                return;
            }
            
            Assignment assignment = assignmentMap.get(selection);
            int classId = assignment.classId;
            int subjectId = assignment.subjectId;
            String timeValue = timeSlot.split(" - ")[0] + ":00"; // Convert "09:00 - 10:00" to "09:00:00"
            
            try (Connection conn = DatabaseConfig.getConnection()) {
                conn.setAutoCommit(false);
                
                try (PreparedStatement ps = conn.prepareStatement(
                    "INSERT INTO attendance (student_id, subject_id, class_id, date, time_slot, status, marked_by) " +
                    "VALUES (?, ?, ?, ?, ?, ?, ?) " +
                    "ON DUPLICATE KEY UPDATE status = ?, marked_by = ?, marked_at = CURRENT_TIMESTAMP")) {
                    
                    for (Map.Entry<Integer, CheckBox> entry : attendanceMap.entrySet()) {
                        int studentId = entry.getKey();
                        String status = entry.getValue().isSelected() ? "PRESENT" : "ABSENT";
                        
                        ps.setInt(1, studentId);
                        ps.setInt(2, subjectId);
                        ps.setInt(3, classId);
                        ps.setDate(4, java.sql.Date.valueOf(date));
                        ps.setString(5, timeValue);
                        ps.setString(6, status);
                        ps.setInt(7, teacher.getId());
                        ps.setString(8, status);
                        ps.setInt(9, teacher.getId());
                        ps.addBatch();
                    }
                    
                    ps.executeBatch();
                    
                    // Record lecture session and update conducted lectures count
                    try (PreparedStatement sessionPs = conn.prepareStatement(
                        "INSERT INTO lecture_sessions (subject_id, class_id, date, time_slot, conducted_by) " +
                        "VALUES (?, ?, ?, ?, ?) " +
                        "ON DUPLICATE KEY UPDATE conducted_by = ?")) {
                        
                        sessionPs.setInt(1, subjectId);
                        sessionPs.setInt(2, classId);
                        sessionPs.setDate(3, java.sql.Date.valueOf(date));
                        sessionPs.setString(4, timeValue);
                        sessionPs.setInt(5, teacher.getId());
                        sessionPs.setInt(6, teacher.getId());
                        sessionPs.executeUpdate();
                    }
                    
                    // Update conducted lectures count
                    try (PreparedStatement updatePs = conn.prepareStatement(
                        "UPDATE subjects SET conducted_lectures = (" +
                        "SELECT COUNT(DISTINCT ls.date, ls.time_slot) FROM lecture_sessions ls " +
                        "WHERE ls.subject_id = ?" +
                        ") WHERE id = ?")) {
                        
                        updatePs.setInt(1, subjectId);
                        updatePs.setInt(2, subjectId);
                        updatePs.executeUpdate();
                    }
                    
                    conn.commit();
                    
                    msgLabel.setText("Attendance submitted successfully for " + attendanceMap.size() + " students!");
                    msgLabel.setStyle("-fx-text-fill: green; -fx-font-size: 14px;");
                    
                    // Clear form
                    studentsList.getChildren().clear();
                    attendanceMap.clear();
                    submitBtn.setDisable(true);
                    
                } catch (Exception ex) {
                    try {
                        conn.rollback();
                    } catch (SQLException rollbackEx) {
                        rollbackEx.printStackTrace();
                    }
                    msgLabel.setText("Error: " + ex.getMessage());
                    msgLabel.setStyle("-fx-text-fill: red; -fx-font-size: 14px;");
                    ex.printStackTrace();
                }
            } catch (Exception ex) {
                msgLabel.setText("Database connection error: " + ex.getMessage());
                msgLabel.setStyle("-fx-text-fill: red; -fx-font-size: 14px;");
                ex.printStackTrace();
            }
        });
        
        Label infoLabel = new Label("Instructions: Select class, date & time slot, then mark attendance. Use Select All/Deselect All buttons for quick marking.");
        infoLabel.setStyle("-fx-font-size: 12px; -fx-text-fill: #7f8c8d; -fx-wrap-text: true;");
        
        content.getChildren().addAll(
            title,
            lectureSlotSection,
            new javafx.scene.control.Separator(),
            separator,
            infoLabel,
            new Label("Select Class & Subject:"),
            classSubjectCombo,
            new Label("Date:"),
            datePicker,
            new Label("Time Slot:"),
            timeSlotCombo,
            new Label("Students (Checked = Present):"),
            studentsScroll,
            selectionButtons,
            submitBtn,
            msgLabel
        );
        
        // Wrap content in ScrollPane to make it scrollable
        ScrollPane mainScrollPane = new ScrollPane(content);
        mainScrollPane.setFitToWidth(true);
        mainScrollPane.setFitToHeight(true);
        mainScrollPane.setVbarPolicy(ScrollPane.ScrollBarPolicy.AS_NEEDED);
        mainScrollPane.setHbarPolicy(ScrollPane.ScrollBarPolicy.NEVER);
        mainScrollPane.setStyle("-fx-background-color: transparent;");
        
        mainLayout.setCenter(mainScrollPane);
    }
    
    private VBox createLectureSlotSection() {
        VBox slotSection = new VBox(10); // Reduced spacing
        slotSection.setStyle("-fx-background-color: #f8f9fa; -fx-padding: 15; -fx-background-radius: 10;"); // Reduced padding
        
        Label sectionTitle = new Label("Today's Lecture Slots");
        sectionTitle.setStyle("-fx-font-size: 18px; -fx-font-weight: bold; -fx-text-fill: #2c3e50;");
        
        // Create grid for lecture slots
        GridPane slotsGrid = new GridPane();
        slotsGrid.setHgap(10); // Reduced gap
        slotsGrid.setVgap(10); // Reduced gap
        slotsGrid.setPadding(new Insets(5)); // Reduced padding
        
        String[] timeSlots = {
            "09:00 - 10:00", "10:00 - 11:00", "11:00 - 12:00", "12:00 - 13:00",
            "13:00 - 14:00", "14:00 - 15:00", "15:00 - 16:00", "16:00 - 17:00"
        };
        
        int col = 0, row = 0;
        for (String slot : timeSlots) {
            VBox slotCard = createLectureSlotCard(slot);
            slotsGrid.add(slotCard, col, row);
            
            col++;
            if (col > 3) { // 4 slots per row
                col = 0;
                row++;
            }
        }
        
        Label instructionLabel = new Label("Click on a time slot to quickly take attendance for that period");
        instructionLabel.setStyle("-fx-font-size: 12px; -fx-text-fill: #7f8c8d; -fx-font-style: italic;");
        
        slotSection.getChildren().addAll(sectionTitle, slotsGrid, instructionLabel);
        return slotSection;
    }
    
    private VBox createLectureSlotCard(String timeSlot) {
        VBox card = new VBox(5); // Reduced spacing
        card.setAlignment(Pos.CENTER);
        card.setPadding(new Insets(10)); // Reduced padding
        card.setStyle("-fx-background-color: white; -fx-border-color: #3498db; -fx-border-width: 1; " +
                     "-fx-background-radius: 8; -fx-border-radius: 8; -fx-cursor: hand; " +
                     "-fx-effect: dropshadow(three-pass-box, rgba(0,0,0,0.1), 3, 0, 0, 1);");
        card.setPrefWidth(130); // Reduced width
        card.setPrefHeight(80); // Reduced height
        
        Label timeLabel = new Label(timeSlot);
        timeLabel.setStyle("-fx-font-size: 12px; -fx-font-weight: bold; -fx-text-fill: #2c3e50;"); // Reduced font size
        
        Label statusLabel = new Label("Available");
        statusLabel.setStyle("-fx-font-size: 10px; -fx-text-fill: #27ae60;"); // Reduced font size
        
        // Check if attendance is already taken for this slot today
        updateSlotStatus(card, timeLabel, statusLabel, timeSlot);
        
        // Add click handler
        card.setOnMouseClicked(e -> openQuickAttendanceDialog(timeSlot));
        
        // Hover effects
        card.setOnMouseEntered(e -> card.setStyle(
            "-fx-background-color: #ecf0f1; -fx-border-color: #3498db; -fx-border-width: 2; " +
            "-fx-background-radius: 8; -fx-border-radius: 8; -fx-cursor: hand; " +
            "-fx-effect: dropshadow(three-pass-box, rgba(0,0,0,0.2), 5, 0, 0, 2);"
        ));
        
        card.setOnMouseExited(e -> card.setStyle(
            "-fx-background-color: white; -fx-border-color: #3498db; -fx-border-width: 1; " +
            "-fx-background-radius: 8; -fx-border-radius: 8; -fx-cursor: hand; " +
            "-fx-effect: dropshadow(three-pass-box, rgba(0,0,0,0.1), 3, 0, 0, 1);"
        ));
        
        card.getChildren().addAll(timeLabel, statusLabel);
        return card;
    }
    
    private void updateSlotStatus(VBox card, Label timeLabel, Label statusLabel, String timeSlot) {
        // Check if there's any attendance recorded for this time slot today
        String timeValue = timeSlot.split(" - ")[0] + ":00";
        LocalDate today = LocalDate.now();
        
        try (Connection conn = DatabaseConfig.getConnection();
             PreparedStatement ps = conn.prepareStatement(
                 "SELECT COUNT(DISTINCT sa.class_id, sa.subject_id) as lecture_count, " +
                 "COUNT(a.id) as attendance_count " +
                 "FROM subject_assignments sa " +
                 "LEFT JOIN attendance a ON sa.class_id = a.class_id AND sa.subject_id = a.subject_id " +
                 "AND a.date = ? AND a.time_slot = ? " +
                 "WHERE sa.teacher_id = ?")) {
            
            ps.setDate(1, java.sql.Date.valueOf(today));
            ps.setString(2, timeValue);
            ps.setInt(3, teacher.getId());
            
            ResultSet rs = ps.executeQuery();
            if (rs.next()) {
                int lectureCount = rs.getInt("lecture_count");
                int attendanceCount = rs.getInt("attendance_count");
                
                if (attendanceCount > 0) {
                    statusLabel.setText("Completed (" + attendanceCount + " records)");
                    statusLabel.setStyle("-fx-font-size: 11px; -fx-text-fill: #27ae60;");
                    card.setStyle("-fx-background-color: #d5f4e6; -fx-border-color: #27ae60; -fx-border-width: 1; " +
                                 "-fx-background-radius: 8; -fx-border-radius: 8; -fx-cursor: hand; " +
                                 "-fx-effect: dropshadow(three-pass-box, rgba(0,0,0,0.1), 3, 0, 0, 1);");
                } else if (lectureCount > 0) {
                    statusLabel.setText("Pending (" + lectureCount + " classes)");
                    statusLabel.setStyle("-fx-font-size: 11px; -fx-text-fill: #f39c12;");
                    card.setStyle("-fx-background-color: #fef9e7; -fx-border-color: #f39c12; -fx-border-width: 1; " +
                                 "-fx-background-radius: 8; -fx-border-radius: 8; -fx-cursor: hand; " +
                                 "-fx-effect: dropshadow(three-pass-box, rgba(0,0,0,0.1), 3, 0, 0, 1);");
                } else {
                    statusLabel.setText("No classes");
                    statusLabel.setStyle("-fx-font-size: 11px; -fx-text-fill: #95a5a6;");
                }
            }
        } catch (Exception ex) {
            ex.printStackTrace();
        }
    }
    
    private void openQuickAttendanceDialog(String timeSlot) {
        Stage dialog = new Stage();
        dialog.setTitle("Quick Attendance - " + timeSlot);
        dialog.setWidth(500);
        dialog.setHeight(400);
        
        VBox form = new VBox(15);
        form.setPadding(new Insets(20));
        
        Label titleLabel = new Label("Quick Attendance for " + timeSlot);
        titleLabel.setStyle("-fx-font-size: 16px; -fx-font-weight: bold;");
        
        ComboBox<String> classSubjectCombo = new ComboBox<>();
        classSubjectCombo.setPromptText("Select Class & Subject");
        loadTeacherAssignments(classSubjectCombo);
        
        DatePicker datePicker = new DatePicker(LocalDate.now());
        
        HBox buttonBox = new HBox(10);
        buttonBox.setAlignment(Pos.CENTER);
        
        Button markAllPresentBtn = new Button("Mark All Present");
        markAllPresentBtn.setStyle("-fx-background-color: #27ae60; -fx-text-fill: white; -fx-cursor: hand;");
        
        Button markAllAbsentBtn = new Button("Mark All Absent");
        markAllAbsentBtn.setStyle("-fx-background-color: #e74c3c; -fx-text-fill: white; -fx-cursor: hand;");
        
        Button takeAttendanceBtn = new Button("Take Individual Attendance");
        takeAttendanceBtn.setStyle("-fx-background-color: #3498db; -fx-text-fill: white; -fx-cursor: hand;");
        
        buttonBox.getChildren().addAll(markAllPresentBtn, markAllAbsentBtn, takeAttendanceBtn);
        
        Label msgLabel = new Label();
        
        // Event handlers
        markAllPresentBtn.setOnAction(e -> {
            String selection = classSubjectCombo.getValue();
            LocalDate date = datePicker.getValue();
            
            if (selection != null && date != null) {
                executeQuickAttendance(selection, date, timeSlot, "PRESENT", msgLabel);
            } else {
                msgLabel.setText("Please select class & subject and date");
                msgLabel.setStyle("-fx-text-fill: red;");
            }
        });
        
        markAllAbsentBtn.setOnAction(e -> {
            String selection = classSubjectCombo.getValue();
            LocalDate date = datePicker.getValue();
            
            if (selection != null && date != null) {
                executeQuickAttendance(selection, date, timeSlot, "ABSENT", msgLabel);
            } else {
                msgLabel.setText("Please select class & subject and date");
                msgLabel.setStyle("-fx-text-fill: red;");
            }
        });
        
        takeAttendanceBtn.setOnAction(e -> {
            dialog.close();
            // Set the time slot in the main form and switch to manual attendance
            // This would require updating the main form's time slot combo
            showTakeAttendance();
        });
        
        Button cancelBtn = new Button("Cancel");
        cancelBtn.setOnAction(e -> dialog.close());
        
        HBox bottomButtons = new HBox(10, cancelBtn);
        bottomButtons.setAlignment(Pos.CENTER_RIGHT);
        
        form.getChildren().addAll(
            titleLabel,
            new Label("Class & Subject:"), classSubjectCombo,
            new Label("Date:"), datePicker,
            new Label("Quick Actions:"), buttonBox,
            msgLabel,
            bottomButtons
        );
        
        Scene scene = new Scene(form);
        dialog.setScene(scene);
        dialog.show();
    }
    
    private void executeQuickAttendance(String selection, LocalDate date, String timeSlot, String status, Label msgLabel) {
        Assignment assignment = assignmentMap.get(selection);
        if (assignment == null) {
            msgLabel.setText("Error: Assignment not found");
            msgLabel.setStyle("-fx-text-fill: red;");
            return;
        }
        
        int classId = assignment.classId;
        int subjectId = assignment.subjectId;
        String timeValue = timeSlot.split(" - ")[0] + ":00";
        
        try (Connection conn = DatabaseConfig.getConnection()) {
            // First check if there are students in this class
            try (PreparedStatement checkPs = conn.prepareStatement(
                "SELECT COUNT(*) FROM class_students WHERE class_id = ?")) {
                checkPs.setInt(1, classId);
                ResultSet rs = checkPs.executeQuery();
                rs.next();
                int studentCount = rs.getInt(1);
                
                if (studentCount == 0) {
                    msgLabel.setText("No students found in this class");
                    msgLabel.setStyle("-fx-text-fill: orange;");
                    return;
                }
            }
            
            // Mark attendance
            try (PreparedStatement ps = conn.prepareStatement(
                 "INSERT INTO attendance (student_id, subject_id, class_id, date, time_slot, status, marked_by) " +
                 "SELECT cs.student_id, ?, ?, ?, ?, ?, ? " +
                 "FROM class_students cs " +
                 "WHERE cs.class_id = ? " +
                 "ON DUPLICATE KEY UPDATE status = ?, marked_by = ?, marked_at = CURRENT_TIMESTAMP")) {
                
                ps.setInt(1, subjectId);
                ps.setInt(2, classId);
                ps.setDate(3, java.sql.Date.valueOf(date));
                ps.setString(4, timeValue);
                ps.setString(5, status);
                ps.setInt(6, teacher.getId());
                ps.setInt(7, classId);
                ps.setString(8, status);
                ps.setInt(9, teacher.getId());
                
                int count = ps.executeUpdate();
                
                // Record lecture session and update conducted lectures count
                try (PreparedStatement sessionPs = conn.prepareStatement(
                    "INSERT INTO lecture_sessions (subject_id, class_id, date, time_slot, conducted_by) " +
                    "VALUES (?, ?, ?, ?, ?) " +
                    "ON DUPLICATE KEY UPDATE conducted_by = ?")) {
                    
                    sessionPs.setInt(1, subjectId);
                    sessionPs.setInt(2, classId);
                    sessionPs.setDate(3, java.sql.Date.valueOf(date));
                    sessionPs.setString(4, timeValue);
                    sessionPs.setInt(5, teacher.getId());
                    sessionPs.setInt(6, teacher.getId());
                    sessionPs.executeUpdate();
                }
                
                // Update conducted lectures count
                try (PreparedStatement updatePs = conn.prepareStatement(
                    "UPDATE subjects SET conducted_lectures = (" +
                    "SELECT COUNT(DISTINCT ls.date, ls.time_slot) FROM lecture_sessions ls " +
                    "WHERE ls.subject_id = ?" +
                    ") WHERE id = ?")) {
                    
                    updatePs.setInt(1, subjectId);
                    updatePs.setInt(2, subjectId);
                    updatePs.executeUpdate();
                }
                
                msgLabel.setText("Successfully marked " + count + " students as " + status.toLowerCase() + " for " + timeSlot + "!");
                msgLabel.setStyle("-fx-text-fill: green;");
            }
            
        } catch (Exception ex) {
            msgLabel.setText("Error: " + ex.getMessage());
            msgLabel.setStyle("-fx-text-fill: red;");
            ex.printStackTrace();
        }
    }
    
    private void showViewAttendance() {
        VBox content = new VBox(20);
        content.setPadding(new Insets(30));
        
        Label title = new Label("View Attendance");
        title.setStyle("-fx-font-size: 24px; -fx-font-weight: bold;");
        
        ComboBox<String> classSubjectCombo = new ComboBox<>();
        classSubjectCombo.setPromptText("Select Class & Subject");
        loadTeacherAssignments(classSubjectCombo);
        
        TableView<Map<String, Object>> table = new TableView<>();
        TableColumn<Map<String, Object>, String> studentCol = new TableColumn<>("Student");
        studentCol.setCellValueFactory(data -> new javafx.beans.property.SimpleStringProperty(data.getValue().get("student").toString()));
        studentCol.setPrefWidth(200);
        
        TableColumn<Map<String, Object>, String> totalCol = new TableColumn<>("Total Lectures");
        totalCol.setCellValueFactory(data -> new javafx.beans.property.SimpleStringProperty(data.getValue().get("total").toString()));
        
        TableColumn<Map<String, Object>, String> presentCol = new TableColumn<>("Present");
        presentCol.setCellValueFactory(data -> new javafx.beans.property.SimpleStringProperty(data.getValue().get("present").toString()));
        
        TableColumn<Map<String, Object>, String> absentCol = new TableColumn<>("Absent");
        absentCol.setCellValueFactory(data -> new javafx.beans.property.SimpleStringProperty(data.getValue().get("absent").toString()));
        
        TableColumn<Map<String, Object>, String> percentCol = new TableColumn<>("Attendance %");
        percentCol.setCellValueFactory(data -> new javafx.beans.property.SimpleStringProperty(data.getValue().get("percentage").toString()));
        
        table.getColumns().addAll(studentCol, totalCol, presentCol, absentCol, percentCol);
        
        classSubjectCombo.setOnAction(e -> {
            String selection = classSubjectCombo.getValue();
            if (selection != null) {
                table.getItems().clear();
                
                Assignment assignment = assignmentMap.get(selection);
                int classId = assignment.classId;
                int subjectId = assignment.subjectId;
                
                try (Connection conn = DatabaseConfig.getConnection();
                     PreparedStatement ps = conn.prepareStatement(
                         "SELECT u.name as student, " +
                         "COUNT(*) as total, " +
                         "SUM(CASE WHEN a.status = 'PRESENT' THEN 1 ELSE 0 END) as present, " +
                         "SUM(CASE WHEN a.status = 'ABSENT' THEN 1 ELSE 0 END) as absent " +
                         "FROM class_students cs " +
                         "JOIN users u ON cs.student_id = u.id " +
                         "LEFT JOIN attendance a ON a.student_id = u.id AND a.subject_id = ? AND a.class_id = ? " +
                         "WHERE cs.class_id = ? " +
                         "GROUP BY u.id ORDER BY u.name")) {
                    ps.setInt(1, subjectId);
                    ps.setInt(2, classId);
                    ps.setInt(3, classId);
                    ResultSet rs = ps.executeQuery();
                    
                    while (rs.next()) {
                        int total = rs.getInt("total");
                        int present = rs.getInt("present");
                        int absent = rs.getInt("absent");
                        double percentage = total > 0 ? (present * 100.0) / total : 0;
                        
                        Map<String, Object> row = new HashMap<>();
                        row.put("student", rs.getString("student"));
                        row.put("total", String.valueOf(total));
                        row.put("present", String.valueOf(present));
                        row.put("absent", String.valueOf(absent));
                        row.put("percentage", String.format("%.2f%%", percentage));
                        table.getItems().add(row);
                    }
                } catch (Exception ex) {
                    ex.printStackTrace();
                }
            }
        });
        
        content.getChildren().addAll(title, classSubjectCombo, table);
        
        // Wrap content in ScrollPane to make it scrollable
        ScrollPane scrollPane = new ScrollPane(content);
        scrollPane.setFitToWidth(true);
        scrollPane.setFitToHeight(true);
        scrollPane.setVbarPolicy(ScrollPane.ScrollBarPolicy.AS_NEEDED);
        scrollPane.setHbarPolicy(ScrollPane.ScrollBarPolicy.NEVER);
        scrollPane.setStyle("-fx-background-color: transparent;");
        
        mainLayout.setCenter(scrollPane);
    }
    
    private void showModifyAttendance() {
        VBox content = new VBox(20);
        content.setPadding(new Insets(30));
        
        Label title = new Label("Modify Attendance");
        title.setStyle("-fx-font-size: 24px; -fx-font-weight: bold;");
        
        ComboBox<String> classSubjectCombo = new ComboBox<>();
        classSubjectCombo.setPromptText("Select Class & Subject");
        loadTeacherAssignments(classSubjectCombo);
        
        DatePicker datePicker = new DatePicker();
        
        TableView<Map<String, Object>> table = new TableView<>();
        TableColumn<Map<String, Object>, String> studentCol = new TableColumn<>("Student");
        studentCol.setCellValueFactory(data -> new javafx.beans.property.SimpleStringProperty(data.getValue().get("student").toString()));
        studentCol.setPrefWidth(250);
        
        TableColumn<Map<String, Object>, String> statusCol = new TableColumn<>("Status");
        statusCol.setCellValueFactory(data -> new javafx.beans.property.SimpleStringProperty(data.getValue().get("status").toString()));
        
        TableColumn<Map<String, Object>, Void> actionCol = new TableColumn<>("Action");
        actionCol.setCellFactory(col -> new TableCell<Map<String, Object>, Void>() {
            private final Button toggleBtn = new Button("Toggle");
            {
                toggleBtn.setStyle("-fx-background-color: #3498db; -fx-text-fill: white; -fx-cursor: hand;");
                toggleBtn.setOnAction(e -> {
                    Map<String, Object> row = getTableView().getItems().get(getIndex());
                    int attendanceId = (int) row.get("id");
                    String currentStatus = row.get("status").toString();
                    String newStatus = currentStatus.equals("PRESENT") ? "ABSENT" : "PRESENT";
                    
                    try (Connection conn = DatabaseConfig.getConnection();
                         PreparedStatement ps = conn.prepareStatement(
                             "UPDATE attendance SET status = ?, marked_by = ?, marked_at = CURRENT_TIMESTAMP WHERE id = ?")) {
                        ps.setString(1, newStatus);
                        ps.setInt(2, teacher.getId());
                        ps.setInt(3, attendanceId);
                        ps.executeUpdate();
                        
                        row.put("status", newStatus);
                        getTableView().refresh();
                    } catch (Exception ex) {
                        ex.printStackTrace();
                    }
                });
            }
            
            @Override
            protected void updateItem(Void item, boolean empty) {
                super.updateItem(item, empty);
                setGraphic(empty ? null : toggleBtn);
            }
        });
        
        table.getColumns().addAll(studentCol, statusCol, actionCol);
        
        Button loadBtn = new Button("Load Attendance");
        loadBtn.setStyle("-fx-background-color: #3498db; -fx-text-fill: white; -fx-cursor: hand;");
        
        Label msgLabel = new Label();
        
        loadBtn.setOnAction(e -> {
            String selection = classSubjectCombo.getValue();
            LocalDate date = datePicker.getValue();
            
            if (selection != null && date != null) {
                table.getItems().clear();
                
                Assignment assignment = assignmentMap.get(selection);
                int classId = assignment.classId;
                int subjectId = assignment.subjectId;
                
                try (Connection conn = DatabaseConfig.getConnection();
                     PreparedStatement ps = conn.prepareStatement(
                         "SELECT a.id, u.name as student, a.status " +
                         "FROM attendance a " +
                         "JOIN users u ON a.student_id = u.id " +
                         "WHERE a.class_id = ? AND a.subject_id = ? AND a.date = ? " +
                         "ORDER BY u.name")) {
                    ps.setInt(1, classId);
                    ps.setInt(2, subjectId);
                    ps.setDate(3, java.sql.Date.valueOf(date));
                    ResultSet rs = ps.executeQuery();
                    
                    while (rs.next()) {
                        Map<String, Object> row = new HashMap<>();
                        row.put("id", rs.getInt("id"));
                        row.put("student", rs.getString("student"));
                        row.put("status", rs.getString("status"));
                        table.getItems().add(row);
                    }
                    
                    if (table.getItems().isEmpty()) {
                        msgLabel.setText("No attendance records found for this date.");
                        msgLabel.setStyle("-fx-text-fill: orange;");
                    } else {
                        msgLabel.setText("");
                    }
                } catch (Exception ex) {
                    msgLabel.setText("Error: " + ex.getMessage());
                    msgLabel.setStyle("-fx-text-fill: red;");
                    ex.printStackTrace();
                }
            }
        });
        
        content.getChildren().addAll(
            title,
            new Label("Select Class & Subject:"),
            classSubjectCombo,
            new Label("Date:"),
            datePicker,
            loadBtn,
            msgLabel,
            table
        );
        
        // Wrap content in ScrollPane to make it scrollable
        ScrollPane scrollPane = new ScrollPane(content);
        scrollPane.setFitToWidth(true);
        scrollPane.setFitToHeight(true);
        scrollPane.setVbarPolicy(ScrollPane.ScrollBarPolicy.AS_NEEDED);
        scrollPane.setHbarPolicy(ScrollPane.ScrollBarPolicy.NEVER);
        scrollPane.setStyle("-fx-background-color: transparent;");
        
        mainLayout.setCenter(scrollPane);
    }
    
    private void showCheckDefaulters() {
        VBox content = new VBox(20);
        content.setPadding(new Insets(30));
        
        Label title = new Label("Check Defaulters (Below 75%)");
        title.setStyle("-fx-font-size: 24px; -fx-font-weight: bold;");
        
        ComboBox<String> classSubjectCombo = new ComboBox<>();
        classSubjectCombo.setPromptText("Select Class & Subject");
        loadTeacherAssignments(classSubjectCombo);
        
        TableView<Map<String, Object>> table = new TableView<>();
        TableColumn<Map<String, Object>, String> studentCol = new TableColumn<>("Student");
        studentCol.setCellValueFactory(data -> new javafx.beans.property.SimpleStringProperty(data.getValue().get("student").toString()));
        studentCol.setPrefWidth(250);
        
        TableColumn<Map<String, Object>, String> percentCol = new TableColumn<>("Attendance %");
        percentCol.setCellValueFactory(data -> new javafx.beans.property.SimpleStringProperty(data.getValue().get("percentage").toString()));
        
        TableColumn<Map<String, Object>, String> statusCol = new TableColumn<>("Status");
        statusCol.setCellValueFactory(data -> new javafx.beans.property.SimpleStringProperty(data.getValue().get("status").toString()));
        
        table.getColumns().addAll(studentCol, percentCol, statusCol);
        
        classSubjectCombo.setOnAction(e -> {
            String selection = classSubjectCombo.getValue();
            if (selection != null) {
                table.getItems().clear();
                
                Assignment assignment = assignmentMap.get(selection);
                int classId = assignment.classId;
                int subjectId = assignment.subjectId;
                
                try (Connection conn = DatabaseConfig.getConnection();
                     PreparedStatement ps = conn.prepareStatement(
                         "SELECT u.name as student, " +
                         "COUNT(*) as total, " +
                         "SUM(CASE WHEN a.status = 'PRESENT' THEN 1 ELSE 0 END) as present " +
                         "FROM class_students cs " +
                         "JOIN users u ON cs.student_id = u.id " +
                         "LEFT JOIN attendance a ON a.student_id = u.id AND a.subject_id = ? AND a.class_id = ? " +
                         "WHERE cs.class_id = ? " +
                         "GROUP BY u.id " +
                         "HAVING total > 0")) {
                    ps.setInt(1, subjectId);
                    ps.setInt(2, classId);
                    ps.setInt(3, classId);
                    ResultSet rs = ps.executeQuery();
                    
                    while (rs.next()) {
                        int total = rs.getInt("total");
                        int present = rs.getInt("present");
                        double percentage = (present * 100.0) / total;
                        
                        if (percentage < 75) {
                            Map<String, Object> row = new HashMap<>();
                            row.put("student", rs.getString("student"));
                            row.put("percentage", String.format("%.2f%%", percentage));
                            row.put("status", "DEFAULTER");
                            table.getItems().add(row);
                        }
                    }
                } catch (Exception ex) {
                    ex.printStackTrace();
                }
            }
        });
        
        content.getChildren().addAll(title, classSubjectCombo, table);
        
        // Wrap content in ScrollPane to make it scrollable
        ScrollPane scrollPane = new ScrollPane(content);
        scrollPane.setFitToWidth(true);
        scrollPane.setFitToHeight(true);
        scrollPane.setVbarPolicy(ScrollPane.ScrollBarPolicy.AS_NEEDED);
        scrollPane.setHbarPolicy(ScrollPane.ScrollBarPolicy.NEVER);
        scrollPane.setStyle("-fx-background-color: transparent;");
        
        mainLayout.setCenter(scrollPane);
    }
    
    private void showQuickSearch() {
        VBox content = new VBox(20);
        content.setPadding(new Insets(30));
        
        Label title = new Label("Quick Student Search");
        title.setStyle("-fx-font-size: 24px; -fx-font-weight: bold;");
        
        // Search input section
        VBox searchSection = new VBox(15);
        searchSection.setStyle("-fx-background-color: #f8f9fa; -fx-padding: 20; -fx-background-radius: 10;");
        
        Label searchTitle = new Label("Search Students");
        searchTitle.setStyle("-fx-font-size: 18px; -fx-font-weight: bold; -fx-text-fill: #2c3e50;");
        
        TextField searchField = new TextField();
        searchField.setPromptText("Enter student name, roll number, or class name...");
        searchField.setStyle("-fx-font-size: 14px; -fx-padding: 10;");
        searchField.setPrefWidth(400);
        
        ComboBox<String> searchTypeCombo = new ComboBox<>();
        searchTypeCombo.getItems().addAll("All Fields", "Student Name", "Roll Number", "Class Name");
        searchTypeCombo.setValue("All Fields");
        searchTypeCombo.setStyle("-fx-font-size: 14px;");
        
        HBox searchControls = new HBox(15);
        searchControls.setAlignment(Pos.CENTER_LEFT);
        
        Button searchBtn = new Button("Search");
        searchBtn.setStyle("-fx-background-color: #3498db; -fx-text-fill: white; -fx-font-size: 14px; -fx-cursor: hand; -fx-padding: 10 20;");
        
        Button clearBtn = new Button("Clear");
        clearBtn.setStyle("-fx-background-color: #95a5a6; -fx-text-fill: white; -fx-font-size: 14px; -fx-cursor: hand; -fx-padding: 10 20;");
        
        searchControls.getChildren().addAll(new Label("Search in:"), searchTypeCombo, searchBtn, clearBtn);
        
        searchSection.getChildren().addAll(searchTitle, searchField, searchControls);
        
        // Results table
        TableView<Map<String, Object>> resultsTable = new TableView<>();
        resultsTable.setPrefHeight(500);
        
        TableColumn<Map<String, Object>, String> nameCol = new TableColumn<>("Student Name");
        nameCol.setCellValueFactory(data -> new javafx.beans.property.SimpleStringProperty(data.getValue().get("name").toString()));
        nameCol.setPrefWidth(200);
        
        TableColumn<Map<String, Object>, String> rollCol = new TableColumn<>("Roll Number");
        rollCol.setCellValueFactory(data -> new javafx.beans.property.SimpleStringProperty(data.getValue().get("roll").toString()));
        rollCol.setPrefWidth(120);
        
        TableColumn<Map<String, Object>, String> classCol = new TableColumn<>("Class");
        classCol.setCellValueFactory(data -> new javafx.beans.property.SimpleStringProperty(data.getValue().get("class").toString()));
        classCol.setPrefWidth(150);
        
        TableColumn<Map<String, Object>, String> courseCol = new TableColumn<>("Course");
        courseCol.setCellValueFactory(data -> new javafx.beans.property.SimpleStringProperty(data.getValue().get("course").toString()));
        courseCol.setPrefWidth(150);
        
        TableColumn<Map<String, Object>, String> attendanceCol = new TableColumn<>("Overall Attendance");
        attendanceCol.setCellValueFactory(data -> new javafx.beans.property.SimpleStringProperty(data.getValue().get("attendance").toString()));
        attendanceCol.setPrefWidth(150);
        
        TableColumn<Map<String, Object>, Void> actionCol = new TableColumn<>("Actions");
        actionCol.setCellFactory(col -> new TableCell<Map<String, Object>, Void>() {
            private final Button viewBtn = new Button("View Details");
            {
                viewBtn.setStyle("-fx-background-color: #27ae60; -fx-text-fill: white; -fx-cursor: hand; -fx-font-size: 12px;");
                viewBtn.setOnAction(e -> {
                    Map<String, Object> student = getTableView().getItems().get(getIndex());
                    showStudentDetails(student);
                });
            }
            
            @Override
            protected void updateItem(Void item, boolean empty) {
                super.updateItem(item, empty);
                setGraphic(empty ? null : viewBtn);
            }
        });
        actionCol.setPrefWidth(120);
        
        resultsTable.getColumns().addAll(nameCol, rollCol, classCol, courseCol, attendanceCol, actionCol);
        
        Label resultsLabel = new Label("Search Results");
        resultsLabel.setStyle("-fx-font-size: 16px; -fx-font-weight: bold; -fx-text-fill: #2c3e50;");
        
        Label countLabel = new Label("No results found");
        countLabel.setStyle("-fx-font-size: 12px; -fx-text-fill: #7f8c8d;");
        
        // Search functionality
        Runnable performSearch = () -> {
            String searchText = searchField.getText().trim();
            String searchType = searchTypeCombo.getValue();
            
            if (searchText.isEmpty()) {
                resultsTable.getItems().clear();
                countLabel.setText("Enter search text to find students");
                return;
            }
            
            resultsTable.getItems().clear();
            
            try (Connection conn = DatabaseConfig.getConnection()) {
                StringBuilder query = new StringBuilder(
                    "SELECT DISTINCT u.id, u.name, u.username as roll, cl.name as class_name, c.name as course_name, " +
                    "COALESCE(AVG(CASE WHEN a.status = 'PRESENT' THEN 100.0 ELSE 0.0 END), 0) as avg_attendance " +
                    "FROM users u " +
                    "JOIN class_students cs ON u.id = cs.student_id " +
                    "JOIN classes cl ON cs.class_id = cl.id " +
                    "JOIN courses c ON cl.course_id = c.id " +
                    "LEFT JOIN attendance a ON u.id = a.student_id " +
                    "WHERE u.role = 'STUDENT' "
                );
                
                // Add search conditions based on search type
                if (searchType.equals("Student Name")) {
                    query.append("AND u.name LIKE ? ");
                } else if (searchType.equals("Roll Number")) {
                    query.append("AND u.username LIKE ? ");
                } else if (searchType.equals("Class Name")) {
                    query.append("AND cl.name LIKE ? ");
                } else { // All Fields
                    query.append("AND (u.name LIKE ? OR u.username LIKE ? OR cl.name LIKE ?) ");
                }
                
                query.append("GROUP BY u.id, u.name, u.username, cl.name, c.name ORDER BY u.name");
                
                try (PreparedStatement ps = conn.prepareStatement(query.toString())) {
                    String searchPattern = "%" + searchText + "%";
                    
                    if (searchType.equals("All Fields")) {
                        ps.setString(1, searchPattern);
                        ps.setString(2, searchPattern);
                        ps.setString(3, searchPattern);
                    } else {
                        ps.setString(1, searchPattern);
                    }
                    
                    ResultSet rs = ps.executeQuery();
                    int count = 0;
                    
                    while (rs.next()) {
                        Map<String, Object> row = new HashMap<>();
                        row.put("id", rs.getInt("id"));
                        row.put("name", rs.getString("name"));
                        row.put("roll", rs.getString("roll"));
                        row.put("class", rs.getString("class_name"));
                        row.put("course", rs.getString("course_name"));
                        row.put("attendance", String.format("%.1f%%", rs.getDouble("avg_attendance")));
                        resultsTable.getItems().add(row);
                        count++;
                    }
                    
                    countLabel.setText(count + " student(s) found");
                }
            } catch (Exception ex) {
                countLabel.setText("Error: " + ex.getMessage());
                ex.printStackTrace();
            }
        };
        
        // Event handlers
        searchBtn.setOnAction(e -> performSearch.run());
        searchField.setOnAction(e -> performSearch.run()); // Search on Enter key
        
        clearBtn.setOnAction(e -> {
            searchField.clear();
            resultsTable.getItems().clear();
            countLabel.setText("Enter search text to find students");
        });
        
        // Real-time search as user types (with delay)
        searchField.textProperty().addListener((observable, oldValue, newValue) -> {
            if (newValue.length() >= 2) { // Start searching after 2 characters
                performSearch.run();
            } else if (newValue.isEmpty()) {
                resultsTable.getItems().clear();
                countLabel.setText("Enter search text to find students");
            }
        });
        
        content.getChildren().addAll(title, searchSection, resultsLabel, countLabel, resultsTable);
        
        // Wrap content in ScrollPane to make it scrollable
        ScrollPane scrollPane = new ScrollPane(content);
        scrollPane.setFitToWidth(true);
        scrollPane.setFitToHeight(true);
        scrollPane.setVbarPolicy(ScrollPane.ScrollBarPolicy.AS_NEEDED);
        scrollPane.setHbarPolicy(ScrollPane.ScrollBarPolicy.NEVER);
        scrollPane.setStyle("-fx-background-color: transparent;");
        
        mainLayout.setCenter(scrollPane);
    }
    
    private void showStudentDetails(Map<String, Object> student) {
        Stage dialog = new Stage();
        dialog.setTitle("Student Details - " + student.get("name"));
        dialog.setWidth(700);
        dialog.setHeight(600);
        
        VBox content = new VBox(20);
        content.setPadding(new Insets(30));
        
        // Student info section
        VBox infoSection = new VBox(10);
        infoSection.setStyle("-fx-background-color: #f8f9fa; -fx-padding: 20; -fx-background-radius: 10;");
        
        Label infoTitle = new Label("Student Information");
        infoTitle.setStyle("-fx-font-size: 18px; -fx-font-weight: bold; -fx-text-fill: #2c3e50;");
        
        GridPane infoGrid = new GridPane();
        infoGrid.setHgap(20);
        infoGrid.setVgap(10);
        
        infoGrid.add(new Label("Name:"), 0, 0);
        infoGrid.add(new Label(student.get("name").toString()), 1, 0);
        infoGrid.add(new Label("Roll Number:"), 0, 1);
        infoGrid.add(new Label(student.get("roll").toString()), 1, 1);
        infoGrid.add(new Label("Class:"), 0, 2);
        infoGrid.add(new Label(student.get("class").toString()), 1, 2);
        infoGrid.add(new Label("Course:"), 0, 3);
        infoGrid.add(new Label(student.get("course").toString()), 1, 3);
        infoGrid.add(new Label("Overall Attendance:"), 0, 4);
        infoGrid.add(new Label(student.get("attendance").toString()), 1, 4);
        
        // Style the info labels
        for (int i = 0; i < 5; i++) {
            ((Label) infoGrid.getChildren().get(i * 2)).setStyle("-fx-font-weight: bold; -fx-text-fill: #34495e;");
            ((Label) infoGrid.getChildren().get(i * 2 + 1)).setStyle("-fx-text-fill: #2c3e50;");
        }
        
        infoSection.getChildren().addAll(infoTitle, infoGrid);
        
        // Attendance details table
        Label attendanceTitle = new Label("Subject-wise Attendance Details");
        attendanceTitle.setStyle("-fx-font-size: 16px; -fx-font-weight: bold; -fx-text-fill: #2c3e50;");
        
        TableView<Map<String, Object>> attendanceTable = new TableView<>();
        attendanceTable.setPrefHeight(300);
        
        TableColumn<Map<String, Object>, String> subjectCol = new TableColumn<>("Subject");
        subjectCol.setCellValueFactory(data -> new javafx.beans.property.SimpleStringProperty(data.getValue().get("subject").toString()));
        subjectCol.setPrefWidth(200);
        
        TableColumn<Map<String, Object>, String> totalCol = new TableColumn<>("Total Classes");
        totalCol.setCellValueFactory(data -> new javafx.beans.property.SimpleStringProperty(data.getValue().get("total").toString()));
        totalCol.setPrefWidth(120);
        
        TableColumn<Map<String, Object>, String> presentCol = new TableColumn<>("Present");
        presentCol.setCellValueFactory(data -> new javafx.beans.property.SimpleStringProperty(data.getValue().get("present").toString()));
        presentCol.setPrefWidth(100);
        
        TableColumn<Map<String, Object>, String> percentCol = new TableColumn<>("Percentage");
        percentCol.setCellValueFactory(data -> new javafx.beans.property.SimpleStringProperty(data.getValue().get("percentage").toString()));
        percentCol.setPrefWidth(120);
        
        attendanceTable.getColumns().addAll(subjectCol, totalCol, presentCol, percentCol);
        
        // Load attendance details
        try (Connection conn = DatabaseConfig.getConnection();
             PreparedStatement ps = conn.prepareStatement(
                 "SELECT s.name as subject, " +
                 "COUNT(a.id) as total, " +
                 "SUM(CASE WHEN a.status = 'PRESENT' THEN 1 ELSE 0 END) as present " +
                 "FROM subjects s " +
                 "JOIN attendance a ON s.id = a.subject_id " +
                 "WHERE a.student_id = ? " +
                 "GROUP BY s.id, s.name " +
                 "ORDER BY s.name")) {
            
            ps.setInt(1, (Integer) student.get("id"));
            ResultSet rs = ps.executeQuery();
            
            while (rs.next()) {
                int total = rs.getInt("total");
                int present = rs.getInt("present");
                double percentage = total > 0 ? (present * 100.0) / total : 0;
                
                Map<String, Object> row = new HashMap<>();
                row.put("subject", rs.getString("subject"));
                row.put("total", String.valueOf(total));
                row.put("present", String.valueOf(present));
                row.put("percentage", String.format("%.1f%%", percentage));
                attendanceTable.getItems().add(row);
            }
        } catch (Exception ex) {
            ex.printStackTrace();
        }
        
        Button closeBtn = new Button("Close");
        closeBtn.setStyle("-fx-background-color: #95a5a6; -fx-text-fill: white; -fx-cursor: hand; -fx-padding: 10 20;");
        closeBtn.setOnAction(e -> dialog.close());
        
        HBox buttonBox = new HBox(closeBtn);
        buttonBox.setAlignment(Pos.CENTER);
        
        content.getChildren().addAll(infoSection, attendanceTitle, attendanceTable, buttonBox);
        
        ScrollPane scrollPane = new ScrollPane(content);
        scrollPane.setFitToWidth(true);
        scrollPane.setFitToHeight(true);
        
        Scene scene = new Scene(scrollPane);
        dialog.setScene(scene);
        dialog.show();
    }
    
    private void loadTeacherAssignments(ComboBox<String> combo) {
        assignmentMap.clear();
        combo.getItems().clear();
        
        System.out.println("Loading assignments for teacher ID: " + teacher.getId());
        
        try (Connection conn = DatabaseConfig.getConnection();
             PreparedStatement ps = conn.prepareStatement(
                 "SELECT sa.class_id, sa.subject_id, cl.name as class_name, s.name as subject_name " +
                 "FROM subject_assignments sa " +
                 "JOIN classes cl ON sa.class_id = cl.id " +
                 "JOIN subjects s ON sa.subject_id = s.id " +
                 "WHERE sa.teacher_id = ?")) {
            ps.setInt(1, teacher.getId());
            ResultSet rs = ps.executeQuery();
            
            int count = 0;
            while (rs.next()) {
                int classId = rs.getInt("class_id");
                int subjectId = rs.getInt("subject_id");
                String className = rs.getString("class_name");
                String subjectName = rs.getString("subject_name");
                
                String display = className + " - " + subjectName;
                Assignment assignment = new Assignment(classId, subjectId, display);
                assignmentMap.put(display, assignment);
                combo.getItems().add(display);
                
                System.out.println("Loaded assignment: " + display + " (ClassID: " + classId + ", SubjectID: " + subjectId + ")");
                count++;
            }
            
            if (count == 0) {
                System.out.println("No assignments found for teacher ID: " + teacher.getId());
                combo.getItems().add("No assignments found");
            } else {
                System.out.println("Total assignments loaded: " + count);
            }
            
        } catch (Exception e) {
            System.err.println("Error loading teacher assignments: " + e.getMessage());
            e.printStackTrace();
        }
    }
    
    private void logout() {
        new LoginScreen(stage).show();
    }
}