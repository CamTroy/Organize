package stellar.organize;

import javafx.application.Application;
import javafx.fxml.FXMLLoader;
import javafx.scene.Scene;
import javafx.scene.image.Image;
import javafx.stage.Stage;

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Paths;

public class OrganizeApplication extends Application {
    @Override
    public void start(Stage stage) throws IOException {
        FXMLLoader fxmlLoader = new FXMLLoader(OrganizeApplication.class.getResource("calendar.fxml"));
        Scene scene = new Scene(fxmlLoader.load(), 320, 240);

        Image icon = new Image("icon.jpg");
        stage.getIcons().add(icon);
        stage.setTitle("Organize");
        stage.setHeight(800);
        stage.setWidth(1280);
        stage.setScene(scene);
        stage.show();
    }

    public static void main(String[] args) {
        launch(args);
    }
}