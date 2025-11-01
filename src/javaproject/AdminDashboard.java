package javaproject;

import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.Scene;
import javafx.scene.control.*;
import javafx.scene.layout.*;
import javafx.stage.Stage;
import java.sql.*;
import java.util.*;

public class AdminDashboard {
    private Stage stage;
    private User admin;
    private BorderPane mainLayout;
    
    public AdminDashboard(Stage stage, User admin) {
        this.stage = stage;
        this.admin = admin;
    }
    
    public void show() {
        mainLayout = new BorderPane();
        
        // Top bar
        HBox topBar = new HBox(20);
        topBar.setPadding(new Insets(15));
        topBar.setStyle("-fx-background-color: #2c3e50;");
        topBar.setAlignment(Pos.CENTER_LEFT);
        
        Label titleLabel = new Label("Admin Dashboard - " + admin.getName());
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
            createMenuButton("Dashboard Statistics"),
            createMenuButton("Manage Courses"),
            createMenuButton("Manage Subjects"),
            createMenuButton("Manage Classes"),
            createMenuButton("Manage Students"),
            createMenuButton("Assign Teachers"),
            createMenuButton("Defaulter List")
        };
        
        menuButtons[0].setOnAction(e -> showDashboardStatistics());
        menuButtons[1].setOnAction(e -> showManageCourses());
        menuButtons[2].setOnAction(e -> showManageSubjects());
        menuButtons[3].setOnAction(e -> showManageClasses());
        menuButtons[4].setOnAction(e -> showManageStudents());
        menuButtons[5].setOnAction(e -> showAssignTeachers());
        menuButtons[6].setOnAction(e -> showDefaulterList());
        
        menu.getChildren().addAll(menuButtons);
        
        mainLayout.setTop(topBar);
        mainLayout.setLeft(menu);
        
        showDashboardStatistics();
        
        Scene scene = new Scene(mainLayout, 1200, 700);
        stage.setScene(scene);
        stage.setTitle("Admin Dashboard");
    }
    
    private Button createMenuButton(String text) {
        Button btn = new Button(text);
        btn.setMaxWidth(Double.MAX_VALUE);
        btn.setStyle("-fx-background-color: transparent; -fx-text-fill: white; -fx-alignment: CENTER-LEFT; -fx-padding: 12; -fx-cursor: hand;");
        btn.setOnMouseEntered(e -> btn.setStyle("-fx-background-color: #3498db; -fx-text-fill: white; -fx-alignment: CENTER-LEFT; -fx-padding: 12; -fx-cursor: hand;"));
        btn.setOnMouseExited(e -> btn.setStyle("-fx-background-color: transparent; -fx-text-fill: white; -fx-alignment: CENTER-LEFT; -fx-padding: 12; -fx-cursor: hand;"));
        return btn;
    }
    
    private void showDashboardStatistics() {
        VBox content = new VBox(20);
        content.setPadding(new Insets(30));
        
        Label title = new Label("Dashboard Statistics");
        title.setStyle("-fx-font-size: 24px; -fx-font-weight: bold;");
        
        // Statistics cards container
        GridPane statsGrid = new GridPane();
        statsGrid.setHgap(20);
        statsGrid.setVgap(20);
        
        try (Connection conn = DatabaseConfig.getConnection()) {
            // Total Courses
            VBox courseCard = createStatCard("Total Courses", getCount(conn, "SELECT COUNT(*) FROM courses"), "#3498db");
            
            // Total Subjects
            VBox subjectCard = createStatCard("Total Subjects", getCount(conn, "SELECT COUNT(*) FROM subjects"), "#9b59b6");
            
            // Total Classes
            VBox classCard = createStatCard("Total Classes", getCount(conn, "SELECT COUNT(*) FROM classes"), "#e67e22");
            
            // Total Students
            VBox studentCard = createStatCard("Total Students", getCount(conn, "SELECT COUNT(*) FROM users WHERE role = 'STUDENT'"), "#27ae60");
            
            // Total Teachers
            VBox teacherCard = createStatCard("Total Teachers", getCount(conn, "SELECT COUNT(*) FROM users WHERE role = 'TEACHER'"), "#f39c12");
            
            // Total Defaulters
            int defaulterCount = getDefaulterCount(conn);
            VBox defaulterCard = createStatCard("Total Defaulters", String.valueOf(defaulterCount), "#e74c3c");
            
            // Average Attendance
            double avgAttendance = getAverageAttendance(conn);
            VBox attendanceCard = createStatCard("Average Attendance", String.format("%.1f%%", avgAttendance), "#1abc9c");
            
            // Recent Activity
            VBox activityCard = createStatCard("Today's Attendance", getTodayAttendanceCount(conn), "#34495e");
            
            statsGrid.add(courseCard, 0, 0);
            statsGrid.add(subjectCard, 1, 0);
            statsGrid.add(classCard, 2, 0);
            statsGrid.add(studentCard, 3, 0);
            statsGrid.add(teacherCard, 0, 1);
            statsGrid.add(defaulterCard, 1, 1);
            statsGrid.add(attendanceCard, 2, 1);
            statsGrid.add(activityCard, 3, 1);
            
        } catch (Exception ex) {
            Label errorLabel = new Label("Error loading statistics: " + ex.getMessage());
            errorLabel.setStyle("-fx-text-fill: red;");
            content.getChildren().addAll(title, errorLabel);
            mainLayout.setCenter(content);
            return;
        }
        
        // Recent Defaulters Table
        Label defaultersTitle = new Label("Recent Defaulters (Below 75%)");
        defaultersTitle.setStyle("-fx-font-size: 18px; -fx-font-weight: bold; -fx-padding: 20 0 10 0;");
        
        TableView<Map<String, Object>> defaultersTable = new TableView<>();
        defaultersTable.setPrefHeight(200);
        
        TableColumn<Map<String, Object>, String> studentCol = new TableColumn<>("Student");
        studentCol.setCellValueFactory(data -> new javafx.beans.property.SimpleStringProperty(data.getValue().get("student").toString()));
        studentCol.setPrefWidth(200);
        
        TableColumn<Map<String, Object>, String> classCol = new TableColumn<>("Class");
        classCol.setCellValueFactory(data -> new javafx.beans.property.SimpleStringProperty(data.getValue().get("class").toString()));
        classCol.setPrefWidth(150);
        
        TableColumn<Map<String, Object>, String> subjectCol = new TableColumn<>("Subject");
        subjectCol.setCellValueFactory(data -> new javafx.beans.property.SimpleStringProperty(data.getValue().get("subject").toString()));
        subjectCol.setPrefWidth(150);
        
        TableColumn<Map<String, Object>, String> percentCol = new TableColumn<>("Attendance %");
        percentCol.setCellValueFactory(data -> new javafx.beans.property.SimpleStringProperty(data.getValue().get("percentage").toString()));
        percentCol.setPrefWidth(120);
        
        defaultersTable.getColumns().addAll(studentCol, classCol, subjectCol, percentCol);
        
        loadDefaultersData(defaultersTable);
        
        content.getChildren().addAll(title, statsGrid, defaultersTitle, defaultersTable);
        
        ScrollPane scrollPane = new ScrollPane(content);
        scrollPane.setFitToWidth(true);
        mainLayout.setCenter(scrollPane);
    }
    
    private VBox createStatCard(String title, String value, String color) {
        VBox card = new VBox(10);
        card.setAlignment(Pos.CENTER);
        card.setPadding(new Insets(20));
        card.setStyle("-fx-background-color: " + color + "; -fx-background-radius: 10; -fx-effect: dropshadow(three-pass-box, rgba(0,0,0,0.1), 5, 0, 0, 2);");
        card.setPrefWidth(150);
        card.setPrefHeight(100);
        
        Label titleLabel = new Label(title);
        titleLabel.setStyle("-fx-text-fill: white; -fx-font-size: 12px; -fx-font-weight: bold;");
        titleLabel.setWrapText(true);
        titleLabel.setAlignment(Pos.CENTER);
        
        Label valueLabel = new Label(value);
        valueLabel.setStyle("-fx-text-fill: white; -fx-font-size: 24px; -fx-font-weight: bold;");
        
        card.getChildren().addAll(titleLabel, valueLabel);
        return card;
    }
    
    private String getCount(Connection conn, String query) throws SQLException {
        try (Statement stmt = conn.createStatement();
             ResultSet rs = stmt.executeQuery(query)) {
            if (rs.next()) {
                return String.valueOf(rs.getInt(1));
            }
        }
        return "0";
    }
    
    private int getDefaulterCount(Connection conn) throws SQLException {
        String query = "SELECT COUNT(DISTINCT u.id) FROM users u " +
                      "JOIN class_students cs ON u.id = cs.student_id " +
                      "JOIN attendance a ON u.id = a.student_id " +
                      "WHERE u.role = 'STUDENT' " +
                      "GROUP BY u.id, a.subject_id, a.class_id " +
                      "HAVING (SUM(CASE WHEN a.status = 'PRESENT' THEN 1 ELSE 0 END) * 100.0 / COUNT(*)) < 75";
        
        try (Statement stmt = conn.createStatement();
             ResultSet rs = stmt.executeQuery("SELECT COUNT(*) FROM (" + query + ") as defaulters")) {
            if (rs.next()) {
                return rs.getInt(1);
            }
        }
        return 0;
    }
    
    private double getAverageAttendance(Connection conn) throws SQLException {
        String query = "SELECT AVG(attendance_percentage) FROM (" +
                      "SELECT (SUM(CASE WHEN a.status = 'PRESENT' THEN 1 ELSE 0 END) * 100.0 / COUNT(*)) as attendance_percentage " +
                      "FROM attendance a " +
                      "GROUP BY a.student_id, a.subject_id, a.class_id" +
                      ") as student_attendance";
        
        try (Statement stmt = conn.createStatement();
             ResultSet rs = stmt.executeQuery(query)) {
            if (rs.next()) {
                return rs.getDouble(1);
            }
        }
        return 0.0;
    }
    
    private String getTodayAttendanceCount(Connection conn) throws SQLException {
        String query = "SELECT COUNT(*) FROM attendance WHERE DATE(marked_at) = CURDATE()";
        
        try (Statement stmt = conn.createStatement();
             ResultSet rs = stmt.executeQuery(query)) {
            if (rs.next()) {
                return String.valueOf(rs.getInt(1));
            }
        }
        return "0";
    }
    
    private void loadDefaultersData(TableView<Map<String, Object>> table) {
        try (Connection conn = DatabaseConfig.getConnection();
             PreparedStatement ps = conn.prepareStatement(
                 "SELECT u.name as student, cl.name as class, s.name as subject, " +
                 "(SUM(CASE WHEN a.status = 'PRESENT' THEN 1 ELSE 0 END) * 100.0 / COUNT(*)) as percentage " +
                 "FROM users u " +
                 "JOIN class_students cs ON u.id = cs.student_id " +
                 "JOIN classes cl ON cs.class_id = cl.id " +
                 "JOIN attendance a ON u.id = a.student_id AND a.class_id = cl.id " +
                 "JOIN subjects s ON a.subject_id = s.id " +
                 "WHERE u.role = 'STUDENT' " +
                 "GROUP BY u.id, a.subject_id, a.class_id " +
                 "HAVING percentage < 75 " +
                 "ORDER BY percentage ASC " +
                 "LIMIT 10")) {
            
            ResultSet rs = ps.executeQuery();
            while (rs.next()) {
                Map<String, Object> row = new HashMap<>();
                row.put("student", rs.getString("student"));
                row.put("class", rs.getString("class"));
                row.put("subject", rs.getString("subject"));
                row.put("percentage", String.format("%.1f%%", rs.getDouble("percentage")));
                table.getItems().add(row);
            }
        } catch (Exception ex) {
            ex.printStackTrace();
        }
    }
    
    private void showManageCourses() {
        VBox content = new VBox(20);
        content.setPadding(new Insets(30));
        
        Label title = new Label("Manage Courses");
        title.setStyle("-fx-font-size: 24px; -fx-font-weight: bold;");
        
        HBox addForm = new HBox(10);
        TextField nameField = new TextField();
        nameField.setPromptText("Course Name");
        TextField descField = new TextField();
        descField.setPromptText("Description");
        Button addBtn = new Button("Add Course");
        addBtn.setStyle("-fx-background-color: #27ae60; -fx-text-fill: white; -fx-cursor: hand;");
        
        Label msgLabel = new Label();
        msgLabel.setStyle("-fx-text-fill: green;");
        
        addBtn.setOnAction(e -> {
            String name = nameField.getText().trim();
            String desc = descField.getText().trim();
            if (!name.isEmpty()) {
                try (Connection conn = DatabaseConfig.getConnection();
                     PreparedStatement ps = conn.prepareStatement(
                         "INSERT INTO courses (name, description) VALUES (?, ?)")) {
                    ps.setString(1, name);
                    ps.setString(2, desc);
                    ps.executeUpdate();
                    msgLabel.setText("Course added successfully!");
                    msgLabel.setStyle("-fx-text-fill: green;");
                    nameField.clear();
                    descField.clear();
                    showManageCourses();
                } catch (Exception ex) {
                    msgLabel.setText("Error: " + ex.getMessage());
                    msgLabel.setStyle("-fx-text-fill: red;");
                }
            }
        });
        
        addForm.getChildren().addAll(nameField, descField, addBtn, msgLabel);
        
        TableView<Map<String, Object>> table = new TableView<>();
        TableColumn<Map<String, Object>, String> idCol = new TableColumn<>("ID");
        idCol.setCellValueFactory(data -> new javafx.beans.property.SimpleStringProperty(data.getValue().get("id").toString()));
        
        TableColumn<Map<String, Object>, String> nameCol = new TableColumn<>("Name");
        nameCol.setCellValueFactory(data -> new javafx.beans.property.SimpleStringProperty(data.getValue().get("name").toString()));
        
        TableColumn<Map<String, Object>, String> descCol = new TableColumn<>("Description");
        descCol.setCellValueFactory(data -> new javafx.beans.property.SimpleStringProperty(data.getValue().get("description").toString()));
        
        TableColumn<Map<String, Object>, Void> actionsCol = new TableColumn<>("Actions");
        actionsCol.setCellFactory(param -> new TableCell<>() {
            private final Button editBtn = new Button("Edit");
            private final Button deleteBtn = new Button("Delete");
            private final HBox pane = new HBox(5, editBtn, deleteBtn);
            
            {
                editBtn.setStyle("-fx-background-color: #3498db; -fx-text-fill: white; -fx-cursor: hand;");
                deleteBtn.setStyle("-fx-background-color: #e74c3c; -fx-text-fill: white; -fx-cursor: hand;");
                
                editBtn.setOnAction(e -> {
                    Map<String, Object> course = getTableView().getItems().get(getIndex());
                    showEditCourseDialog(course);
                });
                
                deleteBtn.setOnAction(e -> {
                    Map<String, Object> course = getTableView().getItems().get(getIndex());
                    deleteCourse((Integer) course.get("id"));
                });
            }
            
            @Override
            protected void updateItem(Void item, boolean empty) {
                super.updateItem(item, empty);
                setGraphic(empty ? null : pane);
            }
        });
        
        table.getColumns().addAll(idCol, nameCol, descCol, actionsCol);
        
        try (Connection conn = DatabaseConfig.getConnection();
             Statement stmt = conn.createStatement();
             ResultSet rs = stmt.executeQuery("SELECT * FROM courses")) {
            while (rs.next()) {
                Map<String, Object> row = new HashMap<>();
                row.put("id", rs.getInt("id"));
                row.put("name", rs.getString("name"));
                row.put("description", rs.getString("description"));
                table.getItems().add(row);
            }
        } catch (Exception ex) {
            ex.printStackTrace();
        }
        
        content.getChildren().addAll(title, addForm, table);
        mainLayout.setCenter(content);
    }
    
    private void showEditCourseDialog(Map<String, Object> course) {
        Stage dialog = new Stage();
        dialog.setTitle("Edit Course");
        
        VBox form = new VBox(10);
        form.setPadding(new Insets(20));
        
        TextField nameField = new TextField((String) course.get("name"));
        TextField descField = new TextField((String) course.get("description"));
        
        Button saveBtn = new Button("Save");
        saveBtn.setStyle("-fx-background-color: #27ae60; -fx-text-fill: white;");
        
        Label msgLabel = new Label();
        
        saveBtn.setOnAction(e -> {
            String name = nameField.getText().trim();
            String desc = descField.getText().trim();
            
            if (!name.isEmpty()) {
                try (Connection conn = DatabaseConfig.getConnection();
                     PreparedStatement ps = conn.prepareStatement(
                         "UPDATE courses SET name = ?, description = ? WHERE id = ?")) {
                    ps.setString(1, name);
                    ps.setString(2, desc);
                    ps.setInt(3, (Integer) course.get("id"));
                    ps.executeUpdate();
                    dialog.close();
                    showManageCourses();
                } catch (Exception ex) {
                    msgLabel.setText("Error: " + ex.getMessage());
                    msgLabel.setStyle("-fx-text-fill: red;");
                }
            }
        });
        
        form.getChildren().addAll(
            new Label("Course Name:"), nameField,
            new Label("Description:"), descField,
            saveBtn, msgLabel
        );
        
        Scene scene = new Scene(form, 300, 250);
        dialog.setScene(scene);
        dialog.show();
    }
    
    private void deleteCourse(int courseId) {
        Alert alert = new Alert(Alert.AlertType.CONFIRMATION);
        alert.setTitle("Confirm Delete");
        alert.setHeaderText("Delete Course");
        alert.setContentText("Are you sure you want to delete this course? This will also delete all related subjects, classes, and assignments.");
        
        alert.showAndWait().ifPresent(response -> {
            if (response == ButtonType.OK) {
                try (Connection conn = DatabaseConfig.getConnection();
                     PreparedStatement ps = conn.prepareStatement("DELETE FROM courses WHERE id = ?")) {
                    ps.setInt(1, courseId);
                    ps.executeUpdate();
                    showManageCourses();
                } catch (Exception ex) {
                    Alert errorAlert = new Alert(Alert.AlertType.ERROR);
                    errorAlert.setTitle("Error");
                    errorAlert.setContentText("Error deleting course: " + ex.getMessage());
                    errorAlert.showAndWait();
                }
            }
        });
    }
    
    private void showManageSubjects() {
        VBox content = new VBox(20);
        content.setPadding(new Insets(30));
        
        Label title = new Label("Manage Subjects");
        title.setStyle("-fx-font-size: 24px; -fx-font-weight: bold;");
        
        HBox addForm = new HBox(10);
        TextField nameField = new TextField();
        nameField.setPromptText("Subject Name");
        
        ComboBox<String> courseCombo = new ComboBox<>();
        loadCoursesIntoCombo(courseCombo);
        courseCombo.setPromptText("Select Course");
        
        Button addBtn = new Button("Add Subject");
        addBtn.setStyle("-fx-background-color: #27ae60; -fx-text-fill: white; -fx-cursor: hand;");
        
        Label msgLabel = new Label();
        
        addBtn.setOnAction(e -> {
            String name = nameField.getText().trim();
            String course = courseCombo.getValue();
            if (!name.isEmpty() && course != null) {
                int courseId = Integer.parseInt(course.split(" - ")[0]);
                try (Connection conn = DatabaseConfig.getConnection();
                     PreparedStatement ps = conn.prepareStatement(
                         "INSERT INTO subjects (name, course_id) VALUES (?, ?)")) {
                    ps.setString(1, name);
                    ps.setInt(2, courseId);
                    ps.executeUpdate();
                    msgLabel.setText("Subject added successfully!");
                    msgLabel.setStyle("-fx-text-fill: green;");
                    nameField.clear();
                    showManageSubjects();
                } catch (Exception ex) {
                    msgLabel.setText("Error: " + ex.getMessage());
                    msgLabel.setStyle("-fx-text-fill: red;");
                }
            }
        });
        
        addForm.getChildren().addAll(nameField, courseCombo, addBtn, msgLabel);
        
        TableView<Map<String, Object>> table = new TableView<>();
        TableColumn<Map<String, Object>, String> idCol = new TableColumn<>("ID");
        idCol.setCellValueFactory(data -> new javafx.beans.property.SimpleStringProperty(data.getValue().get("id").toString()));
        
        TableColumn<Map<String, Object>, String> nameCol = new TableColumn<>("Subject");
        nameCol.setCellValueFactory(data -> new javafx.beans.property.SimpleStringProperty(data.getValue().get("name").toString()));
        
        TableColumn<Map<String, Object>, String> courseCol = new TableColumn<>("Course");
        courseCol.setCellValueFactory(data -> new javafx.beans.property.SimpleStringProperty(data.getValue().get("course").toString()));
        
        TableColumn<Map<String, Object>, Void> actionsCol = new TableColumn<>("Actions");
        actionsCol.setCellFactory(param -> new TableCell<>() {
            private final Button editBtn = new Button("Edit");
            private final Button deleteBtn = new Button("Delete");
            private final HBox pane = new HBox(5, editBtn, deleteBtn);
            
            {
                editBtn.setStyle("-fx-background-color: #3498db; -fx-text-fill: white; -fx-cursor: hand;");
                deleteBtn.setStyle("-fx-background-color: #e74c3c; -fx-text-fill: white; -fx-cursor: hand;");
                
                editBtn.setOnAction(e -> {
                    Map<String, Object> subject = getTableView().getItems().get(getIndex());
                    showEditSubjectDialog(subject);
                });
                
                deleteBtn.setOnAction(e -> {
                    Map<String, Object> subject = getTableView().getItems().get(getIndex());
                    deleteSubject((Integer) subject.get("id"));
                });
            }
            
            @Override
            protected void updateItem(Void item, boolean empty) {
                super.updateItem(item, empty);
                setGraphic(empty ? null : pane);
            }
        });
        
        table.getColumns().addAll(idCol, nameCol, courseCol, actionsCol);
        
        try (Connection conn = DatabaseConfig.getConnection();
             Statement stmt = conn.createStatement();
             ResultSet rs = stmt.executeQuery(
                 "SELECT s.id, s.name, s.course_id, c.name as course FROM subjects s JOIN courses c ON s.course_id = c.id")) {
            while (rs.next()) {
                Map<String, Object> row = new HashMap<>();
                row.put("id", rs.getInt("id"));
                row.put("name", rs.getString("name"));
                row.put("course_id", rs.getInt("course_id"));
                row.put("course", rs.getString("course"));
                table.getItems().add(row);
            }
        } catch (Exception ex) {
            ex.printStackTrace();
        }
        
        content.getChildren().addAll(title, addForm, table);
        mainLayout.setCenter(content);
    }
    
    private void showEditSubjectDialog(Map<String, Object> subject) {
        Stage dialog = new Stage();
        dialog.setTitle("Edit Subject");
        
        VBox form = new VBox(10);
        form.setPadding(new Insets(20));
        
        TextField nameField = new TextField((String) subject.get("name"));
        
        ComboBox<String> courseCombo = new ComboBox<>();
        loadCoursesIntoCombo(courseCombo);
        
        // Set current course
        int currentCourseId = (Integer) subject.get("course_id");
        for (String item : courseCombo.getItems()) {
            if (item.startsWith(currentCourseId + " - ")) {
                courseCombo.setValue(item);
                break;
            }
        }
        
        Button saveBtn = new Button("Save");
        saveBtn.setStyle("-fx-background-color: #27ae60; -fx-text-fill: white;");
        
        Label msgLabel = new Label();
        
        saveBtn.setOnAction(e -> {
            String name = nameField.getText().trim();
            String course = courseCombo.getValue();
            
            if (!name.isEmpty() && course != null) {
                int courseId = Integer.parseInt(course.split(" - ")[0]);
                try (Connection conn = DatabaseConfig.getConnection();
                     PreparedStatement ps = conn.prepareStatement(
                         "UPDATE subjects SET name = ?, course_id = ? WHERE id = ?")) {
                    ps.setString(1, name);
                    ps.setInt(2, courseId);
                    ps.setInt(3, (Integer) subject.get("id"));
                    ps.executeUpdate();
                    dialog.close();
                    showManageSubjects();
                } catch (Exception ex) {
                    msgLabel.setText("Error: " + ex.getMessage());
                    msgLabel.setStyle("-fx-text-fill: red;");
                }
            }
        });
        
        form.getChildren().addAll(
            new Label("Subject Name:"), nameField,
            new Label("Course:"), courseCombo,
            saveBtn, msgLabel
        );
        
        Scene scene = new Scene(form, 300, 250);
        dialog.setScene(scene);
        dialog.show();
    }
    
    private void deleteSubject(int subjectId) {
        Alert alert = new Alert(Alert.AlertType.CONFIRMATION);
        alert.setTitle("Confirm Delete");
        alert.setHeaderText("Delete Subject");
        alert.setContentText("Are you sure you want to delete this subject? This will also delete all related assignments and attendance records.");
        
        alert.showAndWait().ifPresent(response -> {
            if (response == ButtonType.OK) {
                try (Connection conn = DatabaseConfig.getConnection();
                     PreparedStatement ps = conn.prepareStatement("DELETE FROM subjects WHERE id = ?")) {
                    ps.setInt(1, subjectId);
                    ps.executeUpdate();
                    showManageSubjects();
                } catch (Exception ex) {
                    Alert errorAlert = new Alert(Alert.AlertType.ERROR);
                    errorAlert.setTitle("Error");
                    errorAlert.setContentText("Error deleting subject: " + ex.getMessage());
                    errorAlert.showAndWait();
                }
            }
        });
    }
    
    private void showManageClasses() {
        VBox content = new VBox(20);
        content.setPadding(new Insets(30));
        
        Label title = new Label("Manage Classes");
        title.setStyle("-fx-font-size: 24px; -fx-font-weight: bold;");
        
        HBox addForm = new HBox(10);
        TextField nameField = new TextField();
        nameField.setPromptText("Class Name");
        
        ComboBox<String> courseCombo = new ComboBox<>();
        loadCoursesIntoCombo(courseCombo);
        courseCombo.setPromptText("Select Course");
        
        Button addBtn = new Button("Add Class");
        addBtn.setStyle("-fx-background-color: #27ae60; -fx-text-fill: white; -fx-cursor: hand;");
        
        Label msgLabel = new Label();
        
        addBtn.setOnAction(e -> {
            String name = nameField.getText().trim();
            String course = courseCombo.getValue();
            if (!name.isEmpty() && course != null) {
                int courseId = Integer.parseInt(course.split(" - ")[0]);
                try (Connection conn = DatabaseConfig.getConnection();
                     PreparedStatement ps = conn.prepareStatement(
                         "INSERT INTO classes (name, course_id) VALUES (?, ?)")) {
                    ps.setString(1, name);
                    ps.setInt(2, courseId);
                    ps.executeUpdate();
                    msgLabel.setText("Class added successfully!");
                    msgLabel.setStyle("-fx-text-fill: green;");
                    nameField.clear();
                    showManageClasses();
                } catch (Exception ex) {
                    msgLabel.setText("Error: " + ex.getMessage());
                    msgLabel.setStyle("-fx-text-fill: red;");
                }
            }
        });
        
        addForm.getChildren().addAll(nameField, courseCombo, addBtn, msgLabel);
        
        TableView<Map<String, Object>> table = new TableView<>();
        TableColumn<Map<String, Object>, String> idCol = new TableColumn<>("ID");
        idCol.setCellValueFactory(data -> new javafx.beans.property.SimpleStringProperty(data.getValue().get("id").toString()));
        
        TableColumn<Map<String, Object>, String> nameCol = new TableColumn<>("Class");
        nameCol.setCellValueFactory(data -> new javafx.beans.property.SimpleStringProperty(data.getValue().get("name").toString()));
        
        TableColumn<Map<String, Object>, String> courseCol = new TableColumn<>("Course");
        courseCol.setCellValueFactory(data -> new javafx.beans.property.SimpleStringProperty(data.getValue().get("course").toString()));
        
        TableColumn<Map<String, Object>, Void> actionsCol = new TableColumn<>("Actions");
        actionsCol.setCellFactory(param -> new TableCell<>() {
            private final Button editBtn = new Button("Edit");
            private final Button deleteBtn = new Button("Delete");
            private final HBox pane = new HBox(5, editBtn, deleteBtn);
            
            {
                editBtn.setStyle("-fx-background-color: #3498db; -fx-text-fill: white; -fx-cursor: hand;");
                deleteBtn.setStyle("-fx-background-color: #e74c3c; -fx-text-fill: white; -fx-cursor: hand;");
                
                editBtn.setOnAction(e -> {
                    Map<String, Object> classData = getTableView().getItems().get(getIndex());
                    showEditClassDialog(classData);
                });
                
                deleteBtn.setOnAction(e -> {
                    Map<String, Object> classData = getTableView().getItems().get(getIndex());
                    deleteClass((Integer) classData.get("id"));
                });
            }
            
            @Override
            protected void updateItem(Void item, boolean empty) {
                super.updateItem(item, empty);
                setGraphic(empty ? null : pane);
            }
        });
        
        table.getColumns().addAll(idCol, nameCol, courseCol, actionsCol);
        
        try (Connection conn = DatabaseConfig.getConnection();
             Statement stmt = conn.createStatement();
             ResultSet rs = stmt.executeQuery(
                 "SELECT cl.id, cl.name, cl.course_id, c.name as course FROM classes cl JOIN courses c ON cl.course_id = c.id")) {
            while (rs.next()) {
                Map<String, Object> row = new HashMap<>();
                row.put("id", rs.getInt("id"));
                row.put("name", rs.getString("name"));
                row.put("course_id", rs.getInt("course_id"));
                row.put("course", rs.getString("course"));
                table.getItems().add(row);
            }
        } catch (Exception ex) {
            ex.printStackTrace();
        }
        
        content.getChildren().addAll(title, addForm, table);
        mainLayout.setCenter(content);
    }
    
    private void showEditClassDialog(Map<String, Object> classData) {
        Stage dialog = new Stage();
        dialog.setTitle("Edit Class");
        
        VBox form = new VBox(10);
        form.setPadding(new Insets(20));
        
        TextField nameField = new TextField((String) classData.get("name"));
        
        ComboBox<String> courseCombo = new ComboBox<>();
        loadCoursesIntoCombo(courseCombo);
        
        // Set current course
        int currentCourseId = (Integer) classData.get("course_id");
        for (String item : courseCombo.getItems()) {
            if (item.startsWith(currentCourseId + " - ")) {
                courseCombo.setValue(item);
                break;
            }
        }
        
        Button saveBtn = new Button("Save");
        saveBtn.setStyle("-fx-background-color: #27ae60; -fx-text-fill: white;");
        
        Label msgLabel = new Label();
        
        saveBtn.setOnAction(e -> {
            String name = nameField.getText().trim();
            String course = courseCombo.getValue();
            
            if (!name.isEmpty() && course != null) {
                int courseId = Integer.parseInt(course.split(" - ")[0]);
                try (Connection conn = DatabaseConfig.getConnection();
                     PreparedStatement ps = conn.prepareStatement(
                         "UPDATE classes SET name = ?, course_id = ? WHERE id = ?")) {
                    ps.setString(1, name);
                    ps.setInt(2, courseId);
                    ps.setInt(3, (Integer) classData.get("id"));
                    ps.executeUpdate();
                    dialog.close();
                    showManageClasses();
                } catch (Exception ex) {
                    msgLabel.setText("Error: " + ex.getMessage());
                    msgLabel.setStyle("-fx-text-fill: red;");
                }
            }
        });
        
        form.getChildren().addAll(
            new Label("Class Name:"), nameField,
            new Label("Course:"), courseCombo,
            saveBtn, msgLabel
        );
        
        Scene scene = new Scene(form, 300, 250);
        dialog.setScene(scene);
        dialog.show();
    }
    
    private void deleteClass(int classId) {
        Alert alert = new Alert(Alert.AlertType.CONFIRMATION);
        alert.setTitle("Confirm Delete");
        alert.setHeaderText("Delete Class");
        alert.setContentText("Are you sure you want to delete this class? This will also delete all related student enrollments and attendance records.");
        
        alert.showAndWait().ifPresent(response -> {
            if (response == ButtonType.OK) {
                try (Connection conn = DatabaseConfig.getConnection();
                     PreparedStatement ps = conn.prepareStatement("DELETE FROM classes WHERE id = ?")) {
                    ps.setInt(1, classId);
                    ps.executeUpdate();
                    showManageClasses();
                } catch (Exception ex) {
                    Alert errorAlert = new Alert(Alert.AlertType.ERROR);
                    errorAlert.setTitle("Error");
                    errorAlert.setContentText("Error deleting class: " + ex.getMessage());
                    errorAlert.showAndWait();
                }
            }
        });
    }
    
    private void showManageStudents() {
        VBox content = new VBox(20);
        content.setPadding(new Insets(30));
        
        Label title = new Label("Manage Students");
        title.setStyle("-fx-font-size: 24px; -fx-font-weight: bold;");
        
        // Add student form
        VBox addForm = new VBox(10);
        TextField usernameField = new TextField();
        usernameField.setPromptText("Username");
        TextField passwordField = new TextField();
        passwordField.setPromptText("Password");
        TextField nameField = new TextField();
        nameField.setPromptText("Full Name");
        
        ComboBox<String> classCombo = new ComboBox<>();
        loadClassesIntoCombo(classCombo);
        classCombo.setPromptText("Select Class");
        
        Button addBtn = new Button("Add Student");
        addBtn.setStyle("-fx-background-color: #27ae60; -fx-text-fill: white; -fx-cursor: hand;");
        
        Label msgLabel = new Label();
        
        addBtn.setOnAction(e -> {
            String username = usernameField.getText().trim();
            String password = passwordField.getText().trim();
            String name = nameField.getText().trim();
            String classStr = classCombo.getValue();
            
            if (!username.isEmpty() && !password.isEmpty() && !name.isEmpty() && classStr != null) {
                int classId = Integer.parseInt(classStr.split(" - ")[0]);
                try (Connection conn = DatabaseConfig.getConnection()) {
                    conn.setAutoCommit(false);
                    
                    // Insert user
                    try (PreparedStatement ps = conn.prepareStatement(
                        "INSERT INTO users (username, password, role, name) VALUES (?, ?, 'STUDENT', ?)",
                        Statement.RETURN_GENERATED_KEYS)) {
                        ps.setString(1, username);
                        ps.setString(2, password);
                        ps.setString(3, name);
                        ps.executeUpdate();
                        
                        ResultSet rs = ps.getGeneratedKeys();
                        rs.next();
                        int studentId = rs.getInt(1);
                        
                        // Add to class
                        try (PreparedStatement ps2 = conn.prepareStatement(
                            "INSERT INTO class_students (class_id, student_id) VALUES (?, ?)")) {
                            ps2.setInt(1, classId);
                            ps2.setInt(2, studentId);
                            ps2.executeUpdate();
                        }
                    }
                    
                    conn.commit();
                    msgLabel.setText("Student added successfully!");
                    msgLabel.setStyle("-fx-text-fill: green;");
                    usernameField.clear();
                    passwordField.clear();
                    nameField.clear();
                    showManageStudents();
                } catch (Exception ex) {
                    msgLabel.setText("Error: " + ex.getMessage());
                    msgLabel.setStyle("-fx-text-fill: red;");
                }
            }
        });
        
        addForm.getChildren().addAll(usernameField, passwordField, nameField, classCombo, addBtn, msgLabel);
        
        TableView<Map<String, Object>> table = new TableView<>();
        TableColumn<Map<String, Object>, String> idCol = new TableColumn<>("ID");
        idCol.setCellValueFactory(data -> new javafx.beans.property.SimpleStringProperty(data.getValue().get("id").toString()));
        
        TableColumn<Map<String, Object>, String> usernameCol = new TableColumn<>("Username");
        usernameCol.setCellValueFactory(data -> new javafx.beans.property.SimpleStringProperty(data.getValue().get("username").toString()));
        
        TableColumn<Map<String, Object>, String> nameCol = new TableColumn<>("Name");
        nameCol.setCellValueFactory(data -> new javafx.beans.property.SimpleStringProperty(data.getValue().get("name").toString()));
        
        TableColumn<Map<String, Object>, String> classCol = new TableColumn<>("Class");
        classCol.setCellValueFactory(data -> new javafx.beans.property.SimpleStringProperty(data.getValue().get("class").toString()));
        
        TableColumn<Map<String, Object>, Void> actionsCol = new TableColumn<>("Actions");
        actionsCol.setCellFactory(param -> new TableCell<>() {
            private final Button editBtn = new Button("Edit");
            private final Button deleteBtn = new Button("Delete");
            private final HBox pane = new HBox(5, editBtn, deleteBtn);
            
            {
                editBtn.setStyle("-fx-background-color: #3498db; -fx-text-fill: white; -fx-cursor: hand;");
                deleteBtn.setStyle("-fx-background-color: #e74c3c; -fx-text-fill: white; -fx-cursor: hand;");
                
                editBtn.setOnAction(e -> {
                    Map<String, Object> student = getTableView().getItems().get(getIndex());
                    showEditStudentDialog(student);
                });
                
                deleteBtn.setOnAction(e -> {
                    Map<String, Object> student = getTableView().getItems().get(getIndex());
                    deleteStudent((Integer) student.get("id"));
                });
            }
            
            @Override
            protected void updateItem(Void item, boolean empty) {
                super.updateItem(item, empty);
                setGraphic(empty ? null : pane);
            }
        });
        
        table.getColumns().addAll(idCol, usernameCol, nameCol, classCol, actionsCol);
        
        try (Connection conn = DatabaseConfig.getConnection();
             Statement stmt = conn.createStatement();
             ResultSet rs = stmt.executeQuery(
                 "SELECT u.id, u.username, u.name, GROUP_CONCAT(cl.name) as class " +
                 "FROM users u " +
                 "LEFT JOIN class_students cs ON u.id = cs.student_id " +
                 "LEFT JOIN classes cl ON cs.class_id = cl.id " +
                 "WHERE u.role = 'STUDENT' " +
                 "GROUP BY u.id")) {
            while (rs.next()) {
                Map<String, Object> row = new HashMap<>();
                row.put("id", rs.getInt("id"));
                row.put("username", rs.getString("username"));
                row.put("name", rs.getString("name"));
                row.put("class", rs.getString("class") != null ? rs.getString("class") : "Not assigned");
                table.getItems().add(row);
            }
        } catch (Exception ex) {
            ex.printStackTrace();
        }
        
        content.getChildren().addAll(title, addForm, table);
        ScrollPane scrollPane = new ScrollPane(content);
        scrollPane.setFitToWidth(true);
        mainLayout.setCenter(scrollPane);
    }
    
    private void showEditStudentDialog(Map<String, Object> student) {
        Stage dialog = new Stage();
        dialog.setTitle("Edit Student");
        
        VBox form = new VBox(10);
        form.setPadding(new Insets(20));
        
        TextField usernameField = new TextField((String) student.get("username"));
        TextField passwordField = new TextField();
        passwordField.setPromptText("New Password (leave empty to keep current)");
        TextField nameField = new TextField((String) student.get("name"));
        
        ComboBox<String> classCombo = new ComboBox<>();
        loadClassesIntoCombo(classCombo);
        
        Button saveBtn = new Button("Save");
        saveBtn.setStyle("-fx-background-color: #27ae60; -fx-text-fill: white;");
        
        Label msgLabel = new Label();
        
        saveBtn.setOnAction(e -> {
            String username = usernameField.getText().trim();
            String password = passwordField.getText().trim();
            String name = nameField.getText().trim();
            String classStr = classCombo.getValue();
            
            if (!username.isEmpty() && !name.isEmpty()) {
                try (Connection conn = DatabaseConfig.getConnection()) {
                    conn.setAutoCommit(false);
                    
                    // Update user
                    String updateSql = password.isEmpty() 
                        ? "UPDATE users SET username = ?, name = ? WHERE id = ?"
                        : "UPDATE users SET username = ?, password = ?, name = ? WHERE id = ?";
                    
                    try (PreparedStatement ps = conn.prepareStatement(updateSql)) {
                        ps.setString(1, username);
                        if (password.isEmpty()) {
                            ps.setString(2, name);
                            ps.setInt(3, (Integer) student.get("id"));
                        } else {
                            ps.setString(2, password);
                            ps.setString(3, name);
                            ps.setInt(4, (Integer) student.get("id"));
                        }
                        ps.executeUpdate();
                    }
                    
                    // Update class if selected
                    if (classStr != null) {
                        int classId = Integer.parseInt(classStr.split(" - ")[0]);
                        
                        // Delete existing class assignments
                        try (PreparedStatement ps = conn.prepareStatement(
                            "DELETE FROM class_students WHERE student_id = ?")) {
                            ps.setInt(1, (Integer) student.get("id"));
                            ps.executeUpdate();
                        }
                        
                        // Insert new class assignment
                        try (PreparedStatement ps = conn.prepareStatement(
                            "INSERT INTO class_students (class_id, student_id) VALUES (?, ?)")) {
                            ps.setInt(1, classId);
                            ps.setInt(2, (Integer) student.get("id"));
                            ps.executeUpdate();
                        }
                    }
                    
                    conn.commit();
                    dialog.close();
                    showManageStudents();
                } catch (Exception ex) {
                    msgLabel.setText("Error: " + ex.getMessage());
                    msgLabel.setStyle("-fx-text-fill: red;");
                }
            }
        });
        
        form.getChildren().addAll(
            new Label("Username:"), usernameField,
            new Label("Password:"), passwordField,
            new Label("Full Name:"), nameField,
            new Label("Class:"), classCombo,
            saveBtn, msgLabel
        );
        
        Scene scene = new Scene(form, 300, 350);
        dialog.setScene(scene);
        dialog.show();
    }
    
    private void deleteStudent(int studentId) {
        Alert alert = new Alert(Alert.AlertType.CONFIRMATION);
        alert.setTitle("Confirm Delete");
        alert.setHeaderText("Delete Student");
        alert.setContentText("Are you sure you want to delete this student? This will also delete all related attendance records.");
        
        alert.showAndWait().ifPresent(response -> {
            if (response == ButtonType.OK) {
                try (Connection conn = DatabaseConfig.getConnection();
                     PreparedStatement ps = conn.prepareStatement("DELETE FROM users WHERE id = ?")) {
                    ps.setInt(1, studentId);
                    ps.executeUpdate();
                    showManageStudents();
                } catch (Exception ex) {
                    Alert errorAlert = new Alert(Alert.AlertType.ERROR);
                    errorAlert.setTitle("Error");
                    errorAlert.setContentText("Error deleting student: " + ex.getMessage());
                    errorAlert.showAndWait();
                }
            }
        });
    }
    
    private void showAssignTeachers() {
        VBox content = new VBox(20);
        content.setPadding(new Insets(30));
        
        Label title = new Label("Assign Teachers to Subjects");
        title.setStyle("-fx-font-size: 24px; -fx-font-weight: bold;");
        
        // First create teachers if none exist
        Button createTeacherBtn = new Button("Create Teacher Account");
        createTeacherBtn.setStyle("-fx-background-color: #9b59b6; -fx-text-fill: white; -fx-cursor: hand;");
        createTeacherBtn.setOnAction(e -> showCreateTeacherDialog());
        
        VBox assignForm = new VBox(10);
        
        ComboBox<String> teacherCombo = new ComboBox<>();
        loadTeachersIntoCombo(teacherCombo);
        teacherCombo.setPromptText("Select Teacher");
        
        ComboBox<String> classCombo = new ComboBox<>();
        loadClassesIntoCombo(classCombo);
        classCombo.setPromptText("Select Class");
        
        ComboBox<String> subjectCombo = new ComboBox<>();
        subjectCombo.setPromptText("Select Subject");
        
        classCombo.setOnAction(e -> {
            String classStr = classCombo.getValue();
            if (classStr != null) {
                int classId = Integer.parseInt(classStr.split(" - ")[0]);
                loadSubjectsForClass(subjectCombo, classId);
            }
        });
        
        Button assignBtn = new Button("Assign Teacher");
        assignBtn.setStyle("-fx-background-color: #27ae60; -fx-text-fill: white; -fx-cursor: hand;");
        
        Label msgLabel = new Label();
        
        assignBtn.setOnAction(e -> {
            String teacher = teacherCombo.getValue();
            String classStr = classCombo.getValue();
            String subject = subjectCombo.getValue();
            
            if (teacher != null && classStr != null && subject != null) {
                int teacherId = Integer.parseInt(teacher.split(" - ")[0]);
                int classId = Integer.parseInt(classStr.split(" - ")[0]);
                int subjectId = Integer.parseInt(subject.split(" - ")[0]);
                
                try (Connection conn = DatabaseConfig.getConnection();
                     PreparedStatement ps = conn.prepareStatement(
                         "INSERT INTO subject_assignments (subject_id, class_id, teacher_id) VALUES (?, ?, ?)")) {
                    ps.setInt(1, subjectId);
                    ps.setInt(2, classId);
                    ps.setInt(3, teacherId);
                    ps.executeUpdate();
                    msgLabel.setText("Teacher assigned successfully!");
                    msgLabel.setStyle("-fx-text-fill: green;");
                    showAssignTeachers();
                } catch (Exception ex) {
                    msgLabel.setText("Error: " + ex.getMessage());
                    msgLabel.setStyle("-fx-text-fill: red;");
                }
            }
        });
        
        assignForm.getChildren().addAll(teacherCombo, classCombo, subjectCombo, assignBtn, msgLabel);
        
        TableView<Map<String, Object>> table = new TableView<>();
        TableColumn<Map<String, Object>, String> teacherCol = new TableColumn<>("Teacher");
        teacherCol.setCellValueFactory(data -> new javafx.beans.property.SimpleStringProperty(data.getValue().get("teacher").toString()));
        
        TableColumn<Map<String, Object>, String> subjectCol = new TableColumn<>("Subject");
        subjectCol.setCellValueFactory(data -> new javafx.beans.property.SimpleStringProperty(data.getValue().get("subject").toString()));
        
        TableColumn<Map<String, Object>, String> classCol = new TableColumn<>("Class");
        classCol.setCellValueFactory(data -> new javafx.beans.property.SimpleStringProperty(data.getValue().get("class").toString()));
        
        TableColumn<Map<String, Object>, Void> actionsCol = new TableColumn<>("Actions");
        actionsCol.setCellFactory(param -> new TableCell<>() {
            private final Button deleteBtn = new Button("Remove");
            
            {
                deleteBtn.setStyle("-fx-background-color: #e74c3c; -fx-text-fill: white; -fx-cursor: hand;");
                
                deleteBtn.setOnAction(e -> {
                    Map<String, Object> assignment = getTableView().getItems().get(getIndex());
                    deleteAssignment((Integer) assignment.get("id"));
                });
            }
            
            @Override
            protected void updateItem(Void item, boolean empty) {
                super.updateItem(item, empty);
                setGraphic(empty ? null : deleteBtn);
            }
        });
        
        table.getColumns().addAll(teacherCol, subjectCol, classCol, actionsCol);
        
        try (Connection conn = DatabaseConfig.getConnection();
             Statement stmt = conn.createStatement();
             ResultSet rs = stmt.executeQuery(
                 "SELECT sa.id, u.name as teacher, s.name as subject, cl.name as class " +
                 "FROM subject_assignments sa " +
                 "JOIN users u ON sa.teacher_id = u.id " +
                 "JOIN subjects s ON sa.subject_id = s.id " +
                 "JOIN classes cl ON sa.class_id = cl.id")) {
            while (rs.next()) {
                Map<String, Object> row = new HashMap<>();
                row.put("id", rs.getInt("id"));
                row.put("teacher", rs.getString("teacher"));
                row.put("subject", rs.getString("subject"));
                row.put("class", rs.getString("class"));
                table.getItems().add(row);
            }
        } catch (Exception ex) {
            ex.printStackTrace();
        }
        
        content.getChildren().addAll(title, createTeacherBtn, assignForm, table);
        mainLayout.setCenter(content);
    }
    
    private void deleteAssignment(int assignmentId) {
        Alert alert = new Alert(Alert.AlertType.CONFIRMATION);
        alert.setTitle("Confirm Remove");
        alert.setHeaderText("Remove Teacher Assignment");
        alert.setContentText("Are you sure you want to remove this teacher assignment?");
        
        alert.showAndWait().ifPresent(response -> {
            if (response == ButtonType.OK) {
                try (Connection conn = DatabaseConfig.getConnection();
                     PreparedStatement ps = conn.prepareStatement("DELETE FROM subject_assignments WHERE id = ?")) {
                    ps.setInt(1, assignmentId);
                    ps.executeUpdate();
                    showAssignTeachers();
                } catch (Exception ex) {
                    Alert errorAlert = new Alert(Alert.AlertType.ERROR);
                    errorAlert.setTitle("Error");
                    errorAlert.setContentText("Error removing assignment: " + ex.getMessage());
                    errorAlert.showAndWait();
                }
            }
        });
    }
    
    private void showCreateTeacherDialog() {
        Stage dialog = new Stage();
        dialog.setTitle("Create Teacher");
        
        VBox form = new VBox(10);
        form.setPadding(new Insets(20));
        
        TextField usernameField = new TextField();
        usernameField.setPromptText("Username");
        TextField passwordField = new TextField();
        passwordField.setPromptText("Password");
        TextField nameField = new TextField();
        nameField.setPromptText("Full Name");
        
        Button createBtn = new Button("Create");
        createBtn.setStyle("-fx-background-color: #27ae60; -fx-text-fill: white;");
        
        Label msgLabel = new Label();
        
        createBtn.setOnAction(e -> {
            String username = usernameField.getText().trim();
            String password = passwordField.getText().trim();
            String name = nameField.getText().trim();
            
            if (!username.isEmpty() && !password.isEmpty() && !name.isEmpty()) {
                try (Connection conn = DatabaseConfig.getConnection();
                     PreparedStatement ps = conn.prepareStatement(
                         "INSERT INTO users (username, password, role, name) VALUES (?, ?, 'TEACHER', ?)")) {
                    ps.setString(1, username);
                    ps.setString(2, password);
                    ps.setString(3, name);
                    ps.executeUpdate();
                    msgLabel.setText("Teacher created!");
                    msgLabel.setStyle("-fx-text-fill: green;");
                    dialog.close();
                    showAssignTeachers();
                } catch (Exception ex) {
                    msgLabel.setText("Error: " + ex.getMessage());
                    msgLabel.setStyle("-fx-text-fill: red;");
                }
            }
        });
        
        form.getChildren().addAll(
            new Label("Username:"), usernameField,
            new Label("Password:"), passwordField,
            new Label("Full Name:"), nameField,
            createBtn, msgLabel
        );
        
        Scene scene = new Scene(form, 300, 300);
        dialog.setScene(scene);
        dialog.show();
    }
    
    private void showDefaulterList() {
        VBox content = new VBox(20);
        content.setPadding(new Insets(30));
        
        Label title = new Label("Defaulter List (Below 75% Attendance)");
        title.setStyle("-fx-font-size: 24px; -fx-font-weight: bold;");
        
        ComboBox<String> classCombo = new ComboBox<>();
        loadClassesIntoCombo(classCombo);
        classCombo.setPromptText("Select Class");
        
        TableView<Map<String, Object>> table = new TableView<>();
        TableColumn<Map<String, Object>, String> studentCol = new TableColumn<>("Student");
        studentCol.setCellValueFactory(data -> new javafx.beans.property.SimpleStringProperty(data.getValue().get("student").toString()));
        
        TableColumn<Map<String, Object>, String> subjectCol = new TableColumn<>("Subject");
        subjectCol.setCellValueFactory(data -> new javafx.beans.property.SimpleStringProperty(data.getValue().get("subject").toString()));
        
        TableColumn<Map<String, Object>, String> attendanceCol = new TableColumn<>("Attendance %");
        attendanceCol.setCellValueFactory(data -> new javafx.beans.property.SimpleStringProperty(data.getValue().get("attendance").toString()));
        
        TableColumn<Map<String, Object>, String> statusCol = new TableColumn<>("Status");
        statusCol.setCellValueFactory(data -> new javafx.beans.property.SimpleStringProperty(data.getValue().get("status").toString()));
        
        table.getColumns().addAll(studentCol, subjectCol, attendanceCol, statusCol);
        
        classCombo.setOnAction(e -> {
            String classStr = classCombo.getValue();
            if (classStr != null) {
                int classId = Integer.parseInt(classStr.split(" - ")[0]);
                table.getItems().clear();
                
                try (Connection conn = DatabaseConfig.getConnection();
                     PreparedStatement ps = conn.prepareStatement(
                         "SELECT u.name as student, s.name as subject, " +
                         "COUNT(CASE WHEN a.status = 'PRESENT' THEN 1 END) as present, " +
                         "COUNT(*) as total " +
                         "FROM class_students cs " +
                         "JOIN users u ON cs.student_id = u.id " +
                         "JOIN subjects s ON s.course_id = (SELECT course_id FROM classes WHERE id = ?) " +
                         "LEFT JOIN attendance a ON a.student_id = u.id AND a.subject_id = s.id AND a.class_id = ? " +
                         "WHERE cs.class_id = ? " +
                         "GROUP BY u.id, s.id " +
                         "HAVING total > 0")) {
                    ps.setInt(1, classId);
                    ps.setInt(2, classId);
                    ps.setInt(3, classId);
                    ResultSet rs = ps.executeQuery();
                    
                    while (rs.next()) {
                        int present = rs.getInt("present");
                        int total = rs.getInt("total");
                        double percentage = (present * 100.0) / total;
                        
                        if (percentage < 75) {
                            Map<String, Object> row = new HashMap<>();
                            row.put("student", rs.getString("student"));
                            row.put("subject", rs.getString("subject"));
                            row.put("attendance", String.format("%.2f%%", percentage));
                            row.put("status", "DEFAULTER");
                            table.getItems().add(row);
                        }
                    }
                } catch (Exception ex) {
                    ex.printStackTrace();
                }
            }
        });
        
        content.getChildren().addAll(title, classCombo, table);
        mainLayout.setCenter(content);
    }
    
    private void loadCoursesIntoCombo(ComboBox<String> combo) {
        try (Connection conn = DatabaseConfig.getConnection();
             Statement stmt = conn.createStatement();
             ResultSet rs = stmt.executeQuery("SELECT id, name FROM courses")) {
            while (rs.next()) {
                combo.getItems().add(rs.getInt("id") + " - " + rs.getString("name"));
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
    }
    
    private void loadClassesIntoCombo(ComboBox<String> combo) {
        try (Connection conn = DatabaseConfig.getConnection();
             Statement stmt = conn.createStatement();
             ResultSet rs = stmt.executeQuery("SELECT id, name FROM classes")) {
            while (rs.next()) {
                combo.getItems().add(rs.getInt("id") + " - " + rs.getString("name"));
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
    }
    
    private void loadTeachersIntoCombo(ComboBox<String> combo) {
        try (Connection conn = DatabaseConfig.getConnection();
             Statement stmt = conn.createStatement();
             ResultSet rs = stmt.executeQuery("SELECT id, name FROM users WHERE role = 'TEACHER'")) {
            while (rs.next()) {
                combo.getItems().add(rs.getInt("id") + " - " + rs.getString("name"));
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
    }
    
    private void loadSubjectsForClass(ComboBox<String> combo, int classId) {
        combo.getItems().clear();
        try (Connection conn = DatabaseConfig.getConnection();
             PreparedStatement ps = conn.prepareStatement(
                 "SELECT s.id, s.name FROM subjects s " +
                 "JOIN classes cl ON s.course_id = cl.course_id " +
                 "WHERE cl.id = ?")) {
            ps.setInt(1, classId);
            ResultSet rs = ps.executeQuery();
            while (rs.next()) {
                combo.getItems().add(rs.getInt("id") + " - " + rs.getString("name"));
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
    }
    
    private void logout() {
        new LoginScreen(stage).show();
    }
}