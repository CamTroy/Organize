package stellar.organize;

import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import javafx.scene.control.Button;
import javafx.scene.control.CheckBox;
import javafx.scene.control.DatePicker;
import javafx.scene.layout.FlowPane;
import javafx.scene.layout.StackPane;
import javafx.scene.layout.VBox;
import javafx.scene.paint.Color;
import javafx.scene.shape.Rectangle;
import javafx.scene.text.Text;

import javax.swing.*;
import java.net.URL;
import java.time.LocalDate;
import java.time.ZonedDateTime;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.ResourceBundle;

public class MainCalendarController implements Initializable {

    @FXML
    private Button create_event_button, prev_month_button, next_month_button;

    @FXML
    private DatePicker event_datepicker;

    @FXML
    private CheckBox dnd_checkbox;

    @FXML
    private Text year_text, month_text;

    @FXML
    private FlowPane month_flowpane;

    ZonedDateTime date_focus, today;

    boolean do_not_disturb;

    Map<LocalDate, List<CalendarActivity>> events = new HashMap<>();

    @Override
    public void initialize(URL url, ResourceBundle bundle) {

        date_focus = ZonedDateTime.now();
        today = ZonedDateTime.now();
        make_month();
    }

    // A lot of this code was created with the help of this video: https://www.youtube.com/watch?v=tlomjP5uvqo
    private void make_month() {

        month_flowpane.getChildren().clear();
        year_text.setText(String.valueOf(date_focus.getYear()));
        month_text.setText(date_focus.getMonth().toString());

        double month_width = month_flowpane.getPrefWidth();
        double month_height = month_flowpane.getPrefHeight();
        double stroke_width = 1;
        double spacing_h = month_flowpane.getHgap();
        double spacing_v = month_flowpane.getVgap();

        int amount_of_days = date_focus.getMonth().maxLength();

        int day_of_week_start = ZonedDateTime.of(date_focus.getYear(), date_focus.getMonthValue(), 1, 0, 0, 0, 0, date_focus.getZone()).getDayOfWeek().getValue();
        ZonedDateTime date_for_getting = ZonedDateTime.of(2025, date_focus.getMonthValue(), 1, 0, 0, 0, 0, date_focus.getZone());

        for (int i = 0; i < 6; i++) {

            for (int j = 1; j < 7; j++) {

                StackPane pane = new StackPane();

                Rectangle rectangle = new Rectangle();
                rectangle.setFill(Color.TRANSPARENT);
                rectangle.setStroke(Color.BLACK);
                rectangle.setStrokeWidth(stroke_width);
                double rectangle_width = (month_width / 7) - stroke_width - spacing_h;
                rectangle.setWidth(rectangle_width);
                double rectangle_height = (month_height / 6) - stroke_width - spacing_v;
                rectangle.setHeight(rectangle_height);
                pane.getChildren().add(rectangle);

                int calculated_date = (j + 1) + (7 * i);
                if (calculated_date > day_of_week_start) {

                    int true_date = calculated_date - day_of_week_start;
                    if (true_date <= amount_of_days) {

                        Text day = new Text(String.valueOf(true_date));
                        pane.getChildren().add(day);

                        List<CalendarActivity> activity_list = null;
                        try {
                            activity_list = events.get(date_for_getting.toLocalDate());
                            System.out.println("The activity list is here!" + activity_list);
                        } catch (Exception e) {
                            e.printStackTrace();
                        }
                        
                        if (activity_list != null) {
                            System.out.println("activity list is NOT null");
                            create_event_on_calendar(activity_list, rectangle_height, rectangle_width, pane);
                        }
                    }
                }
                System.out.println(date_for_getting);
                date_for_getting = date_for_getting.plusDays(1);
                month_flowpane.getChildren().add(pane);
            }
        }
    }

    private void create_event_on_calendar(List<CalendarActivity> activity_list, double rectangle_height, double rectangle_width, StackPane pane) {
        VBox activity_vbox = new VBox();
        for (int i = 0; i < activity_list.size(); i++) {
            if(i >= 2) {
                Text moreActivities = new Text("...");
                activity_vbox.getChildren().add(moreActivities);
                moreActivities.setOnMouseClicked(mouseEvent -> {
                    System.out.println(activity_list);
                });
                break;
            }
            Text text = new Text(activity_list.get(i).get_title() + ", " + activity_list.get(i).get_description());
            activity_vbox.getChildren().add(text);
            text.setOnMouseClicked(mouseEvent -> {
                System.out.println(text.getText());
            });
        }
        activity_vbox.setTranslateY((rectangle_height / 2) * 0.20);
        activity_vbox.setMaxWidth(rectangle_width* 0.8);
        activity_vbox.setMaxHeight(rectangle_height * 0.65);
        activity_vbox.setStyle("-fx-background-color:#a6a6ff");
        pane.getChildren().add(activity_vbox);
    }

    public void go_to_next_month(ActionEvent event) {
        date_focus = date_focus.plusMonths(1);
        make_month();
    }

    public void go_to_prev_month(ActionEvent event) {
        date_focus = date_focus.minusMonths(1);
        make_month();
    }

    public void display_events() {

    }

    public void toggle_dnd(ActionEvent event) {
        do_not_disturb = dnd_checkbox.isSelected();
    }

    public void create_event(ActionEvent event) {
        //TODO

        // Sample code
        CalendarActivity activity = new CalendarActivity("Bruh", "Bruh", today.plusDays(3));
        events.put(activity.get_start_date().toLocalDate(), List.of(activity));
        System.out.println(events);
        make_month();
    }
}