module stellar.organize {
    requires javafx.controls;
    requires javafx.fxml;
    requires java.desktop;


    opens stellar.organize to javafx.fxml;
    exports stellar.organize;
}