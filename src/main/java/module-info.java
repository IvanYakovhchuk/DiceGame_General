module com.algorithms.lab6 {
    requires javafx.controls;
    requires javafx.fxml;


    opens com.algorithms.lab6 to javafx.fxml;
    exports com.algorithms.lab6;
}