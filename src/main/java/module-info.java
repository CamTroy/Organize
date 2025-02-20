module stellar.organize {
    requires javafx.fxml;
    requires java.desktop;
    requires com.dlsc.gemsfx;
    requires org.controlsfx.controls;
    requires com.fasterxml.jackson.datatype.jsr310;
    requires com.fasterxml.jackson.databind;


    opens stellar.organize to javafx.fxml;
    exports stellar.organize;
}