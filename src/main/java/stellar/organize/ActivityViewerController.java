package stellar.organize;

import com.dlsc.gemsfx.Spacer;
import javafx.event.Event;
import javafx.fxml.FXML;
import javafx.geometry.Insets;
import javafx.scene.Node;
import javafx.scene.control.Button;
import javafx.scene.control.ListView;
import javafx.scene.control.ScrollPane;
import javafx.scene.layout.VBox;
import javafx.scene.text.Text;

import java.net.URL;
import java.time.LocalDate;
import java.util.List;
import java.util.ResourceBundle;

public class ActivityViewerController extends MainCalendarController {

    @FXML
    private ListView<VBox> list_view;

    @FXML
    private ScrollPane scroll_pane;

    private MainCalendarController main_calendar_controller;

    @Override
    public void initialize(URL url, ResourceBundle bundle) {

    }

    public ActivityViewerController() { }

    public void set_MainCalendarController(MainCalendarController main_calendar_controller) {
        this.main_calendar_controller = main_calendar_controller;
    }

    public void list_activities_in_vbox(LocalDate date, List<CalendarActivity> activity_list) throws NullPointerException{
        list_view.getItems().clear();

        for (CalendarActivity activity : activity_list) {

            VBox vbox = new VBox();
            vbox.setSpacing(5);

            Button delete_button = new Button();
            delete_button.setOnMouseClicked((event) -> {
                delete_event(event, activity);
                list_view.getItems().remove(vbox);
                main_calendar_controller.create_week_activities_stuff();
            });
            delete_button.setText("Delete Activity");

            String style = "-fx-fill: #e2e2e3; -fx-text-fill: #e2e2e3;";

            Text title_text = new Text(activity.get_title());
            Text description_text = new Text(activity.get_description());
            Text time_text = new Text(activity.get_start_time().toString() + " - " + activity.get_end_time().toString());
            Text date_text;

            if (activity.get_start_date().isEqual(activity.get_end_date())) {
                date_text = new Text(activity.get_start_date().toString());
            } else {
                date_text = new Text(activity.get_start_date().toString() + " - " + activity.get_end_date().toString());
            }
            Text repeating_text = new Text("Repeats: " + activity.get_repeating());

            title_text.getStyleClass().add("title-text");
            description_text.getStyleClass().add("description-text");
            time_text.getStyleClass().add("time-text");
            date_text.getStyleClass().add("date-text");
            repeating_text.getStyleClass().add("repeating-text");

            vbox.getChildren().add(title_text);
            Spacer spacer = new Spacer();
            spacer.setPrefHeight(10);
            vbox.getChildren().add(spacer);
            vbox.getChildren().add(description_text);
            vbox.getChildren().add(time_text);
            vbox.getChildren().add(date_text);
            vbox.getChildren().add(repeating_text);
            vbox.getChildren().add(delete_button);

            description_text.setWrappingWidth(350);

            for (Node text: vbox.getChildren()) {
                text.setStyle(style);
                text.maxWidth(50);
            }

            list_view.getItems().add(vbox);
            vbox.setPadding(new Insets(10, 10, 10, 10));
            vbox.getStyleClass().add("vbox");
        }
        list_view.getStyleClass().add("list-view");
        scroll_pane.getStylesheets().add(getClass().getResource("viewer.css").toExternalForm());
    }

    public void delete_event(Event event, CalendarActivity activity) {

        activity_list.remove(activity);
        main_calendar_controller.make_month();
    }
}
