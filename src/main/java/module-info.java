module com.pomodoroapp.pomodoroapp {
    requires javafx.controls;
    requires javafx.fxml;
    requires com.google.gson;
    requires java.desktop;


    opens com.pomodoroapp.pomodoroapp to javafx.fxml;
    exports com.pomodoroapp.pomodoroapp;
}