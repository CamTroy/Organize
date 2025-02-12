module stellar.organize {
    requires javafx.fxml;
    requires java.desktop;
    requires com.dlsc.gemsfx;
    requires org.controlsfx.controls;


    opens stellar.organize to javafx.fxml;
    exports stellar.organize;
}