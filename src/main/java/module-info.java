module com.example.textanalysisapp {
    requires javafx.controls;
    requires javafx.fxml;
    requires javafx.graphics; // Add this if needed

    opens com.example.textanalysisapp to javafx.fxml;
    exports com.example.textanalysisapp;
    exports com.example.textanalysisapp.view;
    opens com.example.textanalysisapp.view to javafx.fxml;
}