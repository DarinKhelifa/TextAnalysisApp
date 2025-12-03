module com.example.textanalysisapp {
    requires javafx.controls;
    requires javafx.fxml;
    requires java.desktop;

    opens com.example.textanalysisapp.view to javafx.fxml;
    exports com.example.textanalysisapp.view;
}