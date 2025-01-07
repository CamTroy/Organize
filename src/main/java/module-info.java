module stellar.organize {
    requires javafx.controls;
    requires javafx.fxml;


    opens stellar.organize to javafx.fxml;
    exports stellar.organize;
}