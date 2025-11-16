module com.example.textanalysisapp {
    requires javafx.controls;
    requires javafx.fxml;

    requires com.dlsc.formsfx;

    opens com.example.textanalysisapp to javafx.fxml;
    exports com.example.textanalysisapp;
}