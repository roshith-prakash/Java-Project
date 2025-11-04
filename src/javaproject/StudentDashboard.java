package javaproject;

import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.print.PrinterJob;
import javafx.scene.Scene;
import javafx.scene.control.*;
import javafx.scene.layout.*;
import javafx.scene.text.Font;
import javafx.scene.text.FontWeight;
import javafx.scene.text.Text;
import javafx.scene.text.TextFlow;
import javafx.stage.Stage;
import java.sql.*;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
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
            createMenuButton("Defaulter Status"),
            createMenuButton("Leave Request"),
            createMenuButton("Export PDF")
        };
        
        menuButtons[0].setOnAction(e -> showMyAttendance());
        menuButtons[1].setOnAction(e -> showLectureCount());
        menuButtons[2].setOnAction(e -> showDefaulterStatus());
        menuButtons[3].setOnAction(e -> showLeaveRequest());
        menuButtons[4].setOnAction(e -> showExportPDF());
        
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
                 "FROM attendance a " +
                 "JOIN subjects s ON a.subject_id = s.id " +
                 "JOIN classes cl ON a.class_id = cl.id " +
                 "WHERE a.student_id = ? " +
                 "GROUP BY s.id, cl.id " +
                 "ORDER BY s.name")) {
            ps.setInt(1, student.getId());
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
        
        // Wrap content in ScrollPane to make it scrollable
        ScrollPane scrollPane = new ScrollPane(content);
        scrollPane.setFitToWidth(true);
        scrollPane.setFitToHeight(true);
        scrollPane.setVbarPolicy(ScrollPane.ScrollBarPolicy.AS_NEEDED);
        scrollPane.setHbarPolicy(ScrollPane.ScrollBarPolicy.NEVER);
        scrollPane.setStyle("-fx-background-color: transparent;");
        
        mainLayout.setCenter(scrollPane);
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
                 "SELECT s.name as subject, cl.name as class, COUNT(DISTINCT a.date, a.time_slot) as lectures " +
                 "FROM attendance a " +
                 "JOIN subjects s ON a.subject_id = s.id " +
                 "JOIN classes cl ON a.class_id = cl.id " +
                 "WHERE a.student_id = ? " +
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
        
        // Wrap content in ScrollPane to make it scrollable
        ScrollPane scrollPane = new ScrollPane(content);
        scrollPane.setFitToWidth(true);
        scrollPane.setFitToHeight(true);
        scrollPane.setVbarPolicy(ScrollPane.ScrollBarPolicy.AS_NEEDED);
        scrollPane.setHbarPolicy(ScrollPane.ScrollBarPolicy.NEVER);
        scrollPane.setStyle("-fx-background-color: transparent;");
        
        mainLayout.setCenter(scrollPane);
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
                 "FROM attendance a " +
                 "JOIN subjects s ON a.subject_id = s.id " +
                 "JOIN classes cl ON a.class_id = cl.id " +
                 "WHERE a.student_id = ? " +
                 "GROUP BY s.id, cl.id " +
                 "HAVING total > 0 " +
                 "ORDER BY s.name")) {
            ps.setInt(1, student.getId());
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
    
    private void showLeaveRequest() {
        VBox content = new VBox(20);
        content.setPadding(new Insets(30));
        
        Label title = new Label("Leave Request Management");
        title.setStyle("-fx-font-size: 24px; -fx-font-weight: bold;");
        
        // Create new leave request section
        VBox newRequestSection = new VBox(15);
        newRequestSection.setStyle("-fx-background-color: #f8f9fa; -fx-padding: 20; -fx-background-radius: 10;");
        
        Label newRequestTitle = new Label("Submit New Leave Request");
        newRequestTitle.setStyle("-fx-font-size: 18px; -fx-font-weight: bold; -fx-text-fill: #2c3e50;");
        
        // Form for new leave request
        GridPane requestForm = new GridPane();
        requestForm.setHgap(15);
        requestForm.setVgap(15);
        
        Label subjectLabel = new Label("Subject:");
        subjectLabel.setStyle("-fx-font-weight: bold;");
        ComboBox<String> subjectCombo = new ComboBox<>();
        subjectCombo.setPromptText("Select Subject");
        loadSubjectsForLeaveRequest(subjectCombo);
        
        Label dateLabel = new Label("Date:");
        dateLabel.setStyle("-fx-font-weight: bold;");
        DatePicker datePicker = new DatePicker();
        datePicker.setPromptText("Select date of absence");
        
        Label timeSlotLabel = new Label("Time Slot:");
        timeSlotLabel.setStyle("-fx-font-weight: bold;");
        ComboBox<String> timeSlotCombo = new ComboBox<>();
        timeSlotCombo.setPromptText("Select Time Slot");
        timeSlotCombo.getItems().addAll(
            "09:00 - 10:00", "10:00 - 11:00", "11:00 - 12:00", "12:00 - 13:00",
            "13:00 - 14:00", "14:00 - 15:00", "15:00 - 16:00", "16:00 - 17:00"
        );
        
        Label reasonLabel = new Label("Reason:");
        reasonLabel.setStyle("-fx-font-weight: bold;");
        TextArea reasonArea = new TextArea();
        reasonArea.setPromptText("Please provide a detailed reason for your absence...");
        reasonArea.setPrefRowCount(4);
        reasonArea.setWrapText(true);
        
        requestForm.add(subjectLabel, 0, 0);
        requestForm.add(subjectCombo, 1, 0);
        requestForm.add(dateLabel, 0, 1);
        requestForm.add(datePicker, 1, 1);
        requestForm.add(timeSlotLabel, 0, 2);
        requestForm.add(timeSlotCombo, 1, 2);
        requestForm.add(reasonLabel, 0, 3);
        requestForm.add(reasonArea, 1, 3);
        
        Button submitRequestBtn = new Button("Submit Leave Request");
        submitRequestBtn.setStyle("-fx-background-color: #3498db; -fx-text-fill: white; -fx-font-size: 14px; -fx-cursor: hand; -fx-padding: 10 20;");
        
        Label submitMsgLabel = new Label();
        
        newRequestSection.getChildren().addAll(newRequestTitle, requestForm, submitRequestBtn, submitMsgLabel);
        
        // Existing leave requests section
        VBox existingRequestsSection = new VBox(15);
        
        Label existingRequestsTitle = new Label("My Leave Requests");
        existingRequestsTitle.setStyle("-fx-font-size: 18px; -fx-font-weight: bold; -fx-text-fill: #2c3e50;");
        
        TableView<Map<String, Object>> requestsTable = new TableView<>();
        requestsTable.setPrefHeight(400);
        
        TableColumn<Map<String, Object>, String> subjectCol = new TableColumn<>("Subject");
        subjectCol.setCellValueFactory(data -> new javafx.beans.property.SimpleStringProperty(data.getValue().get("subject").toString()));
        subjectCol.setPrefWidth(150);
        
        TableColumn<Map<String, Object>, String> dateCol = new TableColumn<>("Date");
        dateCol.setCellValueFactory(data -> new javafx.beans.property.SimpleStringProperty(data.getValue().get("date").toString()));
        dateCol.setPrefWidth(100);
        
        TableColumn<Map<String, Object>, String> timeCol = new TableColumn<>("Time Slot");
        timeCol.setCellValueFactory(data -> new javafx.beans.property.SimpleStringProperty(data.getValue().get("time_slot").toString()));
        timeCol.setPrefWidth(120);
        
        TableColumn<Map<String, Object>, String> reasonCol = new TableColumn<>("Reason");
        reasonCol.setCellValueFactory(data -> new javafx.beans.property.SimpleStringProperty(data.getValue().get("reason").toString()));
        reasonCol.setPrefWidth(200);
        
        TableColumn<Map<String, Object>, String> statusCol = new TableColumn<>("Status");
        statusCol.setCellFactory(col -> new TableCell<Map<String, Object>, String>() {
            @Override
            protected void updateItem(String item, boolean empty) {
                super.updateItem(item, empty);
                if (empty || getTableRow() == null || getTableRow().getItem() == null) {
                    setText(null);
                    setStyle("");
                } else {
                    Map<String, Object> row = getTableRow().getItem();
                    String status = row.get("status").toString();
                    setText(status);
                    
                    switch (status) {
                        case "PENDING":
                            setStyle("-fx-background-color: #f39c12; -fx-text-fill: white; -fx-alignment: center;");
                            break;
                        case "APPROVED":
                            setStyle("-fx-background-color: #27ae60; -fx-text-fill: white; -fx-alignment: center;");
                            break;
                        case "REJECTED":
                            setStyle("-fx-background-color: #e74c3c; -fx-text-fill: white; -fx-alignment: center;");
                            break;
                        default:
                            setStyle("");
                    }
                }
            }
        });
        statusCol.setPrefWidth(100);
        
        TableColumn<Map<String, Object>, String> submittedCol = new TableColumn<>("Submitted");
        submittedCol.setCellValueFactory(data -> new javafx.beans.property.SimpleStringProperty(data.getValue().get("submitted_at").toString()));
        submittedCol.setPrefWidth(120);
        
        TableColumn<Map<String, Object>, String> commentsCol = new TableColumn<>("Admin Comments");
        commentsCol.setCellValueFactory(data -> new javafx.beans.property.SimpleStringProperty(
            data.getValue().get("admin_comments") != null ? data.getValue().get("admin_comments").toString() : ""));
        commentsCol.setPrefWidth(150);
        
        requestsTable.getColumns().addAll(subjectCol, dateCol, timeCol, reasonCol, statusCol, submittedCol, commentsCol);
        
        existingRequestsSection.getChildren().addAll(existingRequestsTitle, requestsTable);
        
        // Load existing requests
        loadLeaveRequests(requestsTable);
        
        // Submit request event handler
        submitRequestBtn.setOnAction(e -> {
            String subject = subjectCombo.getValue();
            LocalDate date = datePicker.getValue();
            String timeSlot = timeSlotCombo.getValue();
            String reason = reasonArea.getText().trim();
            
            if (subject == null || subject.isEmpty()) {
                submitMsgLabel.setText("Please select a subject");
                submitMsgLabel.setStyle("-fx-text-fill: red;");
                return;
            }
            
            if (date == null) {
                submitMsgLabel.setText("Please select a date");
                submitMsgLabel.setStyle("-fx-text-fill: red;");
                return;
            }
            
            if (timeSlot == null || timeSlot.isEmpty()) {
                submitMsgLabel.setText("Please select a time slot");
                submitMsgLabel.setStyle("-fx-text-fill: red;");
                return;
            }
            
            if (reason.isEmpty()) {
                submitMsgLabel.setText("Please provide a reason for your absence");
                submitMsgLabel.setStyle("-fx-text-fill: red;");
                return;
            }
            
            // Check if date is not in the future
            if (date.isAfter(LocalDate.now())) {
                submitMsgLabel.setText("Cannot submit leave request for future dates");
                submitMsgLabel.setStyle("-fx-text-fill: red;");
                return;
            }
            
            // Submit the leave request
            submitLeaveRequest(subject, date, timeSlot, reason, submitMsgLabel, requestsTable, 
                             subjectCombo, datePicker, timeSlotCombo, reasonArea);
        });
        
        content.getChildren().addAll(title, newRequestSection, existingRequestsSection);
        
        // Wrap content in ScrollPane to make it scrollable
        ScrollPane scrollPane = new ScrollPane(content);
        scrollPane.setFitToWidth(true);
        scrollPane.setFitToHeight(true);
        scrollPane.setVbarPolicy(ScrollPane.ScrollBarPolicy.AS_NEEDED);
        scrollPane.setHbarPolicy(ScrollPane.ScrollBarPolicy.NEVER);
        scrollPane.setStyle("-fx-background-color: transparent;");
        
        mainLayout.setCenter(scrollPane);
    }
    
    private void loadLeaveRequests(TableView<Map<String, Object>> table) {
        table.getItems().clear();
        
        try (Connection conn = DatabaseConfig.getConnection();
             PreparedStatement ps = conn.prepareStatement(
                 "SELECT lr.*, s.name as subject_name " +
                 "FROM leave_requests lr " +
                 "JOIN subjects s ON lr.subject_id = s.id " +
                 "WHERE lr.student_id = ? " +
                 "ORDER BY lr.submitted_at DESC")) {
            
            ps.setInt(1, student.getId());
            ResultSet rs = ps.executeQuery();
            
            DateTimeFormatter dateFormatter = DateTimeFormatter.ofPattern("dd/MM/yyyy");
            DateTimeFormatter timeFormatter = DateTimeFormatter.ofPattern("dd/MM/yyyy HH:mm");
            
            while (rs.next()) {
                Map<String, Object> row = new HashMap<>();
                row.put("id", rs.getInt("id"));
                row.put("subject", rs.getString("subject_name"));
                row.put("date", rs.getDate("date").toLocalDate().format(dateFormatter));
                row.put("time_slot", formatTimeSlot(rs.getTime("time_slot").toString()));
                row.put("reason", rs.getString("reason"));
                row.put("status", rs.getString("status"));
                row.put("submitted_at", rs.getTimestamp("submitted_at").toLocalDateTime().format(timeFormatter));
                row.put("admin_comments", rs.getString("admin_comments"));
                table.getItems().add(row);
            }
        } catch (Exception ex) {
            ex.printStackTrace();
        }
    }
    
    private void submitLeaveRequest(String subject, LocalDate date, String timeSlot, String reason,
                                  Label msgLabel, TableView<Map<String, Object>> table,
                                  ComboBox<String> subjectCombo, DatePicker datePicker,
                                  ComboBox<String> timeSlotCombo, TextArea reasonArea) {
        try (Connection conn = DatabaseConfig.getConnection()) {
            // Get subject and class IDs
            int subjectId = Integer.parseInt(subject.split(" - ")[0]);
            String timeValue = timeSlot.split(" - ")[0] + ":00";
            
            // Get student's class for this subject
            try (PreparedStatement classPs = conn.prepareStatement(
                "SELECT cs.class_id FROM class_students cs " +
                "JOIN classes cl ON cs.class_id = cl.id " +
                "JOIN subjects s ON s.course_id = cl.course_id " +
                "WHERE cs.student_id = ? AND s.id = ? LIMIT 1")) {
                
                classPs.setInt(1, student.getId());
                classPs.setInt(2, subjectId);
                ResultSet classRs = classPs.executeQuery();
                
                if (!classRs.next()) {
                    msgLabel.setText("Error: Could not find your class for this subject");
                    msgLabel.setStyle("-fx-text-fill: red;");
                    return;
                }
                
                int classId = classRs.getInt("class_id");
                
                // Check if attendance record exists for this date/time
                try (PreparedStatement attendancePs = conn.prepareStatement(
                    "SELECT status FROM attendance WHERE student_id = ? AND subject_id = ? AND class_id = ? AND date = ? AND time_slot = ?")) {
                    
                    attendancePs.setInt(1, student.getId());
                    attendancePs.setInt(2, subjectId);
                    attendancePs.setInt(3, classId);
                    attendancePs.setDate(4, java.sql.Date.valueOf(date));
                    attendancePs.setString(5, timeValue);
                    
                    ResultSet attendanceRs = attendancePs.executeQuery();
                    
                    if (!attendanceRs.next()) {
                        msgLabel.setText("No attendance record found for the selected date and time slot");
                        msgLabel.setStyle("-fx-text-fill: red;");
                        return;
                    }
                    
                    String currentStatus = attendanceRs.getString("status");
                    if ("PRESENT".equals(currentStatus)) {
                        msgLabel.setText("You are already marked present for this lecture");
                        msgLabel.setStyle("-fx-text-fill: red;");
                        return;
                    }
                }
                
                // Insert leave request
                try (PreparedStatement ps = conn.prepareStatement(
                    "INSERT INTO leave_requests (student_id, subject_id, class_id, date, time_slot, reason) " +
                    "VALUES (?, ?, ?, ?, ?, ?)")) {
                    
                    ps.setInt(1, student.getId());
                    ps.setInt(2, subjectId);
                    ps.setInt(3, classId);
                    ps.setDate(4, java.sql.Date.valueOf(date));
                    ps.setString(5, timeValue);
                    ps.setString(6, reason);
                    
                    ps.executeUpdate();
                    
                    msgLabel.setText("Leave request submitted successfully! Awaiting admin approval.");
                    msgLabel.setStyle("-fx-text-fill: green;");
                    
                    // Clear form
                    subjectCombo.setValue(null);
                    datePicker.setValue(null);
                    timeSlotCombo.setValue(null);
                    reasonArea.clear();
                    
                    // Refresh the table
                    loadLeaveRequests(table);
                }
            }
        } catch (Exception ex) {
            msgLabel.setText("Error: " + ex.getMessage());
            msgLabel.setStyle("-fx-text-fill: red;");
            ex.printStackTrace();
        }
    }
    
    private void logout() {
        new LoginScreen(stage).show();
    }
    
    private void showExportPDF() {
        VBox content = new VBox(20);
        content.setPadding(new Insets(30));
        
        Label title = new Label("Export Attendance as PDF");
        title.setStyle("-fx-font-size: 24px; -fx-font-weight: bold;");
        
        // Export options
        VBox exportOptions = new VBox(15);
        exportOptions.setPadding(new Insets(20));
        exportOptions.setStyle("-fx-background-color: #f8f9fa; -fx-border-color: #dee2e6; -fx-border-radius: 5; -fx-background-radius: 5;");
        
        Label optionsTitle = new Label("Export Options");
        optionsTitle.setStyle("-fx-font-size: 16px; -fx-font-weight: bold;");
        
        ComboBox<String> subjectCombo = new ComboBox<>();
        subjectCombo.setPromptText("Select Subject (All Subjects)");
        loadStudentSubjects(subjectCombo);
        
        DatePicker fromDate = new DatePicker();
        fromDate.setPromptText("From Date (Optional)");
        
        DatePicker toDate = new DatePicker();
        toDate.setPromptText("To Date (Optional)");
        
        CheckBox includeStats = new CheckBox("Include Statistics Summary");
        includeStats.setSelected(true);
        
        Button exportBtn = new Button("Generate & Print PDF");
        exportBtn.setStyle("-fx-background-color: #007bff; -fx-text-fill: white; -fx-font-size: 14px; -fx-cursor: hand; -fx-padding: 10 20;");
        
        Label msgLabel = new Label();
        
        exportBtn.setOnAction(e -> {
            generateAndPrintPDF(
                subjectCombo.getValue(),
                fromDate.getValue(),
                toDate.getValue(),
                includeStats.isSelected(),
                msgLabel
            );
        });
        
        exportOptions.getChildren().addAll(
            optionsTitle,
            new Label("Subject:"), subjectCombo,
            new Label("From Date:"), fromDate,
            new Label("To Date:"), toDate,
            includeStats,
            exportBtn,
            msgLabel
        );
        
        // Preview area
        Label previewTitle = new Label("Preview");
        previewTitle.setStyle("-fx-font-size: 16px; -fx-font-weight: bold;");
        
        TextArea previewArea = new TextArea();
        previewArea.setPrefHeight(300);
        previewArea.setEditable(false);
        previewArea.setStyle("-fx-font-family: 'Courier New'; -fx-font-size: 12px;");
        
        Button previewBtn = new Button("Generate Preview");
        previewBtn.setStyle("-fx-background-color: #28a745; -fx-text-fill: white; -fx-cursor: hand;");
        
        previewBtn.setOnAction(e -> {
            String preview = generateAttendanceReport(
                subjectCombo.getValue(),
                fromDate.getValue(),
                toDate.getValue(),
                includeStats.isSelected()
            );
            previewArea.setText(preview);
        });
        
        content.getChildren().addAll(
            title,
            exportOptions,
            previewTitle,
            previewBtn,
            previewArea
        );
        
        ScrollPane scrollPane = new ScrollPane(content);
        scrollPane.setFitToWidth(true);
        mainLayout.setCenter(scrollPane);
    }
    
    private void loadStudentSubjects(ComboBox<String> combo) {
        combo.getItems().add("All Subjects");
        
        try (Connection conn = DatabaseConfig.getConnection();
             PreparedStatement ps = conn.prepareStatement(
                 "SELECT DISTINCT s.id, s.name FROM subjects s " +
                 "JOIN classes cl ON s.course_id = cl.course_id " +
                 "JOIN class_students cs ON cl.id = cs.class_id " +
                 "WHERE cs.student_id = ? ORDER BY s.name")) {
            
            ps.setInt(1, student.getId());
            ResultSet rs = ps.executeQuery();
            
            while (rs.next()) {
                combo.getItems().add(rs.getInt("id") + " - " + rs.getString("name"));
            }
        } catch (Exception ex) {
            ex.printStackTrace();
        }
    }
    
    private void loadSubjectsForLeaveRequest(ComboBox<String> combo) {
        try (Connection conn = DatabaseConfig.getConnection();
             PreparedStatement ps = conn.prepareStatement(
                 "SELECT DISTINCT s.id, s.name FROM subjects s " +
                 "JOIN classes cl ON s.course_id = cl.course_id " +
                 "JOIN class_students cs ON cl.id = cs.class_id " +
                 "WHERE cs.student_id = ? ORDER BY s.name")) {
            
            ps.setInt(1, student.getId());
            ResultSet rs = ps.executeQuery();
            
            while (rs.next()) {
                combo.getItems().add(rs.getInt("id") + " - " + rs.getString("name"));
            }
        } catch (Exception ex) {
            ex.printStackTrace();
        }
    }
    
    private String generateAttendanceReport(String subject, LocalDate fromDate, LocalDate toDate, boolean includeStats) {
        StringBuilder report = new StringBuilder();
        DateTimeFormatter formatter = DateTimeFormatter.ofPattern("dd/MM/yyyy");
        
        // Header
        report.append("ATTENDANCE REPORT\n");
        report.append("=".repeat(50)).append("\n\n");
        report.append("Student: ").append(student.getName()).append("\n");
        report.append("Student ID: ").append(student.getId()).append("\n");
        report.append("Generated: ").append(LocalDate.now().format(formatter)).append("\n");
        
        if (fromDate != null) {
            report.append("From Date: ").append(fromDate.format(formatter)).append("\n");
        }
        if (toDate != null) {
            report.append("To Date: ").append(toDate.format(formatter)).append("\n");
        }
        
        report.append("\n").append("-".repeat(50)).append("\n\n");
        
        try (Connection conn = DatabaseConfig.getConnection()) {
            String query = buildAttendanceQuery(subject, fromDate, toDate);
            
            try (PreparedStatement ps = conn.prepareStatement(query)) {
                setQueryParameters(ps, subject, fromDate, toDate);
                ResultSet rs = ps.executeQuery();
                
                String currentSubject = "";
                int totalPresent = 0, totalLectures = 0;
                Map<String, int[]> subjectStats = new HashMap<>();
                
                while (rs.next()) {
                    String subjectName = rs.getString("subject_name");
                    String date = rs.getDate("date").toLocalDate().format(formatter);
                    String timeSlot = rs.getString("time_slot");
                    String status = rs.getString("status");
                    
                    if (!currentSubject.equals(subjectName)) {
                        if (!currentSubject.isEmpty()) {
                            report.append("\n");
                        }
                        currentSubject = subjectName;
                        report.append("Subject: ").append(subjectName).append("\n");
                        report.append("-".repeat(40)).append("\n");
                    }
                    
                    // Format time slot for display (e.g., "09:00:00" -> "09:00-10:00")
                    String displayTime = formatTimeSlot(timeSlot);
                    report.append(String.format("%-12s %-12s : %s\n", date, displayTime, status));
                    
                    // Update statistics
                    subjectStats.putIfAbsent(subjectName, new int[2]);
                    int[] stats = subjectStats.get(subjectName);
                    stats[1]++; // Total lectures
                    if ("PRESENT".equals(status)) {
                        stats[0]++; // Present count
                        totalPresent++;
                    }
                    totalLectures++;
                }
                
                // Add statistics if requested
                if (includeStats && !subjectStats.isEmpty()) {
                    report.append("\n").append("=".repeat(50)).append("\n");
                    report.append("ATTENDANCE STATISTICS\n");
                    report.append("=".repeat(50)).append("\n\n");
                    
                    for (Map.Entry<String, int[]> entry : subjectStats.entrySet()) {
                        String subjectName = entry.getKey();
                        int[] stats = entry.getValue();
                        double percentage = (stats[0] * 100.0) / stats[1];
                        
                        report.append(String.format("%-20s: %d/%d (%.1f%%)\n", 
                            subjectName, stats[0], stats[1], percentage));
                    }
                    
                    double overallPercentage = totalLectures > 0 ? (totalPresent * 100.0) / totalLectures : 0;
                    report.append("\n");
                    report.append(String.format("Overall Attendance: %d/%d (%.1f%%)\n", 
                        totalPresent, totalLectures, overallPercentage));
                    
                    if (overallPercentage < 75) {
                        report.append("\n*** WARNING: Attendance below 75% threshold ***\n");
                    }
                }
                
            }
        } catch (Exception ex) {
            report.append("Error generating report: ").append(ex.getMessage());
            ex.printStackTrace();
        }
        
        return report.toString();
    }
    
    private String buildAttendanceQuery(String subject, LocalDate fromDate, LocalDate toDate) {
        StringBuilder query = new StringBuilder(
            "SELECT s.name as subject_name, a.date, a.time_slot, a.status " +
            "FROM attendance a " +
            "JOIN subjects s ON a.subject_id = s.id " +
            "WHERE a.student_id = ?"
        );
        
        if (subject != null && !subject.equals("All Subjects")) {
            query.append(" AND s.id = ?");
        }
        
        if (fromDate != null) {
            query.append(" AND a.date >= ?");
        }
        
        if (toDate != null) {
            query.append(" AND a.date <= ?");
        }
        
        query.append(" ORDER BY s.name, a.date, a.time_slot");
        
        return query.toString();
    }
    
    private void setQueryParameters(PreparedStatement ps, String subject, LocalDate fromDate, LocalDate toDate) throws SQLException {
        int paramIndex = 1;
        
        ps.setInt(paramIndex++, student.getId());
        
        if (subject != null && !subject.equals("All Subjects")) {
            int subjectId = Integer.parseInt(subject.split(" - ")[0]);
            ps.setInt(paramIndex++, subjectId);
        }
        
        if (fromDate != null) {
            ps.setDate(paramIndex++, java.sql.Date.valueOf(fromDate));
        }
        
        if (toDate != null) {
            ps.setDate(paramIndex++, java.sql.Date.valueOf(toDate));
        }
    }
    
    private String formatTimeSlot(String timeSlot) {
        if (timeSlot == null) return "N/A";
        
        try {
            // Convert "09:00:00" to "09:00-10:00"
            String[] parts = timeSlot.split(":");
            int hour = Integer.parseInt(parts[0]);
            int nextHour = hour + 1;
            return String.format("%02d:00-%02d:00", hour, nextHour);
        } catch (Exception e) {
            return timeSlot; // Return original if parsing fails
        }
    }
    
    private void generateAndPrintPDF(String subject, LocalDate fromDate, LocalDate toDate, boolean includeStats, Label msgLabel) {
        try {
            // Generate the report content
            String reportContent = generateAttendanceReport(subject, fromDate, toDate, includeStats);
            
            // Create a TextFlow for printing
            TextFlow textFlow = new TextFlow();
            textFlow.setPrefWidth(550); // A4 width minus margins
            textFlow.setPadding(new Insets(20));
            
            // Split content into lines and create Text nodes
            String[] lines = reportContent.split("\n");
            for (String line : lines) {
                Text text = new Text(line + "\n");
                
                // Style different types of content
                if (line.startsWith("ATTENDANCE REPORT") || line.startsWith("ATTENDANCE STATISTICS")) {
                    text.setFont(Font.font("Arial", FontWeight.BOLD, 16));
                } else if (line.startsWith("Subject:") || line.startsWith("Student:")) {
                    text.setFont(Font.font("Arial", FontWeight.BOLD, 12));
                } else if (line.startsWith("=") || line.startsWith("-")) {
                    text.setFont(Font.font("Courier New", 10));
                } else {
                    text.setFont(Font.font("Arial", 11));
                }
                
                textFlow.getChildren().add(text);
            }
            
            // Create and configure printer job
            PrinterJob printerJob = PrinterJob.createPrinterJob();
            
            if (printerJob != null) {
                // Show print dialog
                boolean proceed = printerJob.showPrintDialog(stage);
                
                if (proceed) {
                    // Print the content
                    boolean success = printerJob.printPage(textFlow);
                    
                    if (success) {
                        printerJob.endJob();
                        msgLabel.setText("Attendance report sent to printer successfully!");
                        msgLabel.setStyle("-fx-text-fill: green; -fx-font-weight: bold;");
                    } else {
                        msgLabel.setText("Failed to print the report.");
                        msgLabel.setStyle("-fx-text-fill: red; -fx-font-weight: bold;");
                    }
                } else {
                    msgLabel.setText("Print job cancelled by user.");
                    msgLabel.setStyle("-fx-text-fill: orange; -fx-font-weight: bold;");
                }
            } else {
                msgLabel.setText("No printer available. Please check your printer setup.");
                msgLabel.setStyle("-fx-text-fill: red; -fx-font-weight: bold;");
            }
            
        } catch (Exception ex) {
            msgLabel.setText("Error generating PDF: " + ex.getMessage());
            msgLabel.setStyle("-fx-text-fill: red; -fx-font-weight: bold;");
            ex.printStackTrace();
        }
    }
}