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
            createMenuButton("Check Defaulters")
        };
        
        menuButtons[0].setOnAction(e -> showTakeAttendance());
        menuButtons[1].setOnAction(e -> showQuickActions());
        menuButtons[2].setOnAction(e -> showViewAttendance());
        menuButtons[3].setOnAction(e -> showModifyAttendance());
        menuButtons[4].setOnAction(e -> showCheckDefaulters());
        
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
        mainLayout.setCenter(content);
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
        
        Button executeBtn = new Button("Mark All Present");
        executeBtn.setStyle("-fx-background-color: #27ae60; -fx-text-fill: white; -fx-cursor: hand;");
        
        Label msgLabel = new Label();
        
        executeBtn.setOnAction(e -> {
            String selection = classSubjectCombo.getValue();
            java.time.LocalDate date = datePicker.getValue();
            
            if (selection != null && date != null) {
                Assignment assignment = assignmentMap.get(selection);
                int classId = assignment.classId;
                int subjectId = assignment.subjectId;
                
                try (Connection conn = DatabaseConfig.getConnection();
                     PreparedStatement ps = conn.prepareStatement(
                         "INSERT INTO attendance (student_id, subject_id, class_id, date, status, marked_by) " +
                         "SELECT cs.student_id, ?, ?, ?, 'PRESENT', ? " +
                         "FROM class_students cs " +
                         "WHERE cs.class_id = ? " +
                         "ON DUPLICATE KEY UPDATE status = 'PRESENT', marked_by = ?, marked_at = CURRENT_TIMESTAMP")) {
                    
                    ps.setInt(1, subjectId);
                    ps.setInt(2, classId);
                    ps.setDate(3, java.sql.Date.valueOf(date));
                    ps.setInt(4, teacher.getId());
                    ps.setInt(5, classId);
                    ps.setInt(6, teacher.getId());
                    
                    int count = ps.executeUpdate();
                    msgLabel.setText("Successfully marked " + count + " students as present!");
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
            new Label("Date:"), datePicker,
            buttons,
            msgLabel
        );
        
        Scene scene = new Scene(form, 350, 300);
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
        
        Button executeBtn = new Button("Mark All Absent");
        executeBtn.setStyle("-fx-background-color: #e74c3c; -fx-text-fill: white; -fx-cursor: hand;");
        
        Label msgLabel = new Label();
        
        executeBtn.setOnAction(e -> {
            String selection = classSubjectCombo.getValue();
            java.time.LocalDate date = datePicker.getValue();
            
            if (selection != null && date != null) {
                Assignment assignment = assignmentMap.get(selection);
                int classId = assignment.classId;
                int subjectId = assignment.subjectId;
                
                try (Connection conn = DatabaseConfig.getConnection();
                     PreparedStatement ps = conn.prepareStatement(
                         "INSERT INTO attendance (student_id, subject_id, class_id, date, status, marked_by) " +
                         "SELECT cs.student_id, ?, ?, ?, 'ABSENT', ? " +
                         "FROM class_students cs " +
                         "WHERE cs.class_id = ? " +
                         "ON DUPLICATE KEY UPDATE status = 'ABSENT', marked_by = ?, marked_at = CURRENT_TIMESTAMP")) {
                    
                    ps.setInt(1, subjectId);
                    ps.setInt(2, classId);
                    ps.setDate(3, java.sql.Date.valueOf(date));
                    ps.setInt(4, teacher.getId());
                    ps.setInt(5, classId);
                    ps.setInt(6, teacher.getId());
                    
                    int count = ps.executeUpdate();
                    msgLabel.setText("Successfully marked " + count + " students as absent!");
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
            new Label("Date:"), datePicker,
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
                         "INSERT INTO attendance (student_id, subject_id, class_id, date, status, marked_by) " +
                         "SELECT student_id, subject_id, class_id, ?, status, ? " +
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
        
        classSubjectCombo.setOnAction(e -> {
            String selection = classSubjectCombo.getValue();
            java.time.LocalDate date = datePicker.getValue();
            
            if (selection != null && date != null) {
                studentsTable.getItems().clear();
                Assignment assignment = assignmentMap.get(selection);
                int classId = assignment.classId;
                int subjectId = assignment.subjectId;
                
                try (Connection conn = DatabaseConfig.getConnection();
                     PreparedStatement ps = conn.prepareStatement(
                         "SELECT u.id, u.name, COALESCE(a.status, 'PRESENT') as status " +
                         "FROM class_students cs " +
                         "JOIN users u ON cs.student_id = u.id " +
                         "LEFT JOIN attendance a ON u.id = a.student_id AND a.class_id = ? AND a.subject_id = ? AND a.date = ? " +
                         "WHERE cs.class_id = ? ORDER BY u.name")) {
                    
                    ps.setInt(1, classId);
                    ps.setInt(2, subjectId);
                    ps.setDate(3, java.sql.Date.valueOf(date));
                    ps.setInt(4, classId);
                    
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
        });
        
        Button saveBtn = new Button("Save All Changes");
        saveBtn.setStyle("-fx-background-color: #27ae60; -fx-text-fill: white; -fx-cursor: hand;");
        
        Label msgLabel = new Label();
        
        saveBtn.setOnAction(e -> {
            String selection = classSubjectCombo.getValue();
            java.time.LocalDate date = datePicker.getValue();
            
            if (selection != null && date != null && !studentsTable.getItems().isEmpty()) {
                Assignment assignment = assignmentMap.get(selection);
                int classId = assignment.classId;
                int subjectId = assignment.subjectId;
                
                try (Connection conn = DatabaseConfig.getConnection();
                     PreparedStatement ps = conn.prepareStatement(
                         "INSERT INTO attendance (student_id, subject_id, class_id, date, status, marked_by) " +
                         "VALUES (?, ?, ?, ?, ?, ?) " +
                         "ON DUPLICATE KEY UPDATE status = ?, marked_by = ?, marked_at = CURRENT_TIMESTAMP")) {
                    
                    for (Map<String, Object> row : studentsTable.getItems()) {
                        int studentId = (int) row.get("id");
                        String status = row.get("status").toString();
                        
                        ps.setInt(1, studentId);
                        ps.setInt(2, subjectId);
                        ps.setInt(3, classId);
                        ps.setDate(4, java.sql.Date.valueOf(date));
                        ps.setString(5, status);
                        ps.setInt(6, teacher.getId());
                        ps.setString(7, status);
                        ps.setInt(8, teacher.getId());
                        ps.addBatch();
                    }
                    
                    ps.executeBatch();
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
            new Label("Students:"), studentsTable,
            buttons,
            msgLabel
        );
        
        Scene scene = new Scene(form);
        dialog.setScene(scene);
        dialog.show();
    }

    private void showTakeAttendance() {
        VBox content = new VBox(20);
        content.setPadding(new Insets(30));
        
        Label title = new Label("Take Attendance");
        title.setStyle("-fx-font-size: 24px; -fx-font-weight: bold;");
        
        ComboBox<String> classSubjectCombo = new ComboBox<>();
        classSubjectCombo.setPromptText("Select Class & Subject");
        loadTeacherAssignments(classSubjectCombo);
        
        DatePicker datePicker = new DatePicker(LocalDate.now());
        
        VBox studentsList = new VBox(10);
        ScrollPane studentsScroll = new ScrollPane(studentsList);
        studentsScroll.setFitToWidth(true);
        studentsScroll.setPrefHeight(400);
        
        Button submitBtn = new Button("Submit Attendance");
        submitBtn.setStyle("-fx-background-color: #27ae60; -fx-text-fill: white; -fx-font-size: 14px; -fx-cursor: hand;");
        submitBtn.setDisable(true);
        
        Label msgLabel = new Label();
        
        Map<Integer, CheckBox> attendanceMap = new HashMap<>();
        
        classSubjectCombo.setOnAction(e -> {
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
                        cb.setStyle("-fx-font-size: 14px;");
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
        
        submitBtn.setOnAction(e -> {
            String selection = classSubjectCombo.getValue();
            LocalDate date = datePicker.getValue();
            
            if (selection != null && date != null && !attendanceMap.isEmpty()) {
                Assignment assignment = assignmentMap.get(selection);
                int classId = assignment.classId;
                int subjectId = assignment.subjectId;
                
                try (Connection conn = DatabaseConfig.getConnection()) {
                    conn.setAutoCommit(false);
                    
                    try (PreparedStatement ps = conn.prepareStatement(
                        "INSERT INTO attendance (student_id, subject_id, class_id, date, status, marked_by) " +
                        "VALUES (?, ?, ?, ?, ?, ?) " +
                        "ON DUPLICATE KEY UPDATE status = ?, marked_by = ?, marked_at = CURRENT_TIMESTAMP")) {
                        
                        for (Map.Entry<Integer, CheckBox> entry : attendanceMap.entrySet()) {
                            int studentId = entry.getKey();
                            String status = entry.getValue().isSelected() ? "PRESENT" : "ABSENT";
                            
                            ps.setInt(1, studentId);
                            ps.setInt(2, subjectId);
                            ps.setInt(3, classId);
                            ps.setDate(4, java.sql.Date.valueOf(date));
                            ps.setString(5, status);
                            ps.setInt(6, teacher.getId());
                            ps.setString(7, status);
                            ps.setInt(8, teacher.getId());
                            ps.addBatch();
                        }
                        
                        ps.executeBatch();
                    }
                    
                    conn.commit();
                    msgLabel.setText("Attendance submitted successfully for " + attendanceMap.size() + " students!");
                    msgLabel.setStyle("-fx-text-fill: green; -fx-font-size: 14px;");
                    
                    // Clear form
                    studentsList.getChildren().clear();
                    attendanceMap.clear();
                    submitBtn.setDisable(true);
                    
                } catch (Exception ex) {
                    msgLabel.setText("Error: " + ex.getMessage());
                    msgLabel.setStyle("-fx-text-fill: red; -fx-font-size: 14px;");
                    ex.printStackTrace();
                }
            }
        });
        
        Label infoLabel = new Label("Instructions: Select class & subject, then check/uncheck students (checked = present)");
        infoLabel.setStyle("-fx-font-size: 12px; -fx-text-fill: #7f8c8d;");
        
        content.getChildren().addAll(
            title,
            infoLabel,
            new Label("Select Class & Subject:"),
            classSubjectCombo,
            new Label("Date:"),
            datePicker,
            new Label("Students (Checked = Present):"),
            studentsScroll,
            submitBtn,
            msgLabel
        );
        
        mainLayout.setCenter(content);
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
        mainLayout.setCenter(content);
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
        
        mainLayout.setCenter(content);
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
        mainLayout.setCenter(content);
    }
    
    private void loadTeacherAssignments(ComboBox<String> combo) {
        assignmentMap.clear();
        combo.getItems().clear();
        
        try (Connection conn = DatabaseConfig.getConnection();
             PreparedStatement ps = conn.prepareStatement(
                 "SELECT sa.class_id, sa.subject_id, cl.name as class_name, s.name as subject_name " +
                 "FROM subject_assignments sa " +
                 "JOIN classes cl ON sa.class_id = cl.id " +
                 "JOIN subjects s ON sa.subject_id = s.id " +
                 "WHERE sa.teacher_id = ?")) {
            ps.setInt(1, teacher.getId());
            ResultSet rs = ps.executeQuery();
            
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
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
    }
    
    private void logout() {
        new LoginScreen(stage).show();
    }
}