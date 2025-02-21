package stellar.organize;

import javafx.application.Application;
import javafx.application.Platform;
import javafx.fxml.FXMLLoader;
import javafx.scene.Scene;
import javafx.scene.image.Image;
import javafx.stage.Stage;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

public class OrganizeApplication extends Application {

    private MainCalendarController calendar_controller;

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

        calendar_controller = fxmlLoader.getController();

        stage.setOnCloseRequest(event -> {
            if (calendar_controller != null) {
                try {

                    String activities_path = "activities.json";
                    String config_path = "config.json";
                    List<Boolean> config_options = Collections.singletonList(calendar_controller.do_not_disturb);

                    calendar_controller.write_activities_to_file(activities_path, calendar_controller.activity_list);
                    calendar_controller.write_config_to_file(config_path, config_options);
                } catch (IOException e) {
                    e.printStackTrace();
                }
            }
            Platform.exit();
            System.exit(0);
        });
    }

    public static void main(String[] args) {
        launch(args);
    }
}