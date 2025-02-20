package stellar.organize;

import javafx.event.Event;
import javafx.fxml.FXML;
import javafx.scene.control.Button;
import javafx.scene.layout.HBox;
import javafx.scene.layout.VBox;
import javafx.scene.text.Text;

import java.net.URL;
import java.time.LocalDate;
import java.util.List;
import java.util.ResourceBundle;

public class ActivityViewerController extends MainCalendarController {

    @FXML
    private VBox list_vbox;

    private MainCalendarController main_calendar_controller;

    @Override
    public void initialize(URL url, ResourceBundle bundle) {

    }

    public ActivityViewerController() { }

    public void set_MainCalendarController(MainCalendarController main_calendar_controller) {
        this.main_calendar_controller = main_calendar_controller;
    }

    public void list_activities_in_vbox(LocalDate date, List<CalendarActivity> activity_list) {
        list_vbox.getChildren().clear();

        for (CalendarActivity activity : activity_list) {

            HBox hbox = new HBox();

            Button delete_button = new Button();
            delete_button.setOnMouseClicked((event) -> {
                delete_event(event, activity);
                list_vbox.getChildren().remove(hbox);
            });

            hbox.getChildren().add(new Text(activity.get_title()));
            hbox.getChildren().add(new Text(activity.get_description()));
            hbox.getChildren().add(new Text(activity.get_start_time().toString()));
            hbox.getChildren().add(new Text(activity.get_end_time().toString()));
            hbox.getChildren().add(new Text(activity.get_start_date().toString()));
            hbox.getChildren().add(new Text(activity.get_end_date().toString()));
            hbox.getChildren().add(new Text(activity.get_repeating()));
            hbox.getChildren().add(delete_button);

            list_vbox.getChildren().add(hbox);
        }
    }

    public void delete_event(Event event, CalendarActivity activity) {

        activity_list.remove(activity);
        main_calendar_controller.make_month();
    }
}
