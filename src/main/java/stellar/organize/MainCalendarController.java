package stellar.organize;

import com.dlsc.gemsfx.TimePicker;
import javafx.application.Platform;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import javafx.geometry.Pos;
import javafx.scene.control.*;
import javafx.scene.layout.FlowPane;
import javafx.scene.layout.StackPane;
import javafx.scene.layout.VBox;
import javafx.scene.paint.Color;
import javafx.scene.shape.Rectangle;
import javafx.scene.text.Text;
import javafx.util.Duration;
import org.controlsfx.control.Notifications;

import java.net.URL;
import java.time.*;
import java.util.*;
import java.util.concurrent.*;

public class MainCalendarController implements Initializable {

    @FXML
    private Button create_event_button, prev_month_button, next_month_button;

    @FXML
    private DatePicker event_start_datepicker, event_end_datepicker;

    @FXML
    private CheckBox dnd_checkbox;

    @FXML
    private Text year_text, month_text;

    @FXML
    private FlowPane month_flowpane;

    @FXML
    private TextField event_name_field, event_description_field;

    @FXML
    private TimePicker start_time_picker, end_time_picker;

    ZonedDateTime date_focus, today;

    boolean do_not_disturb = false;

    List<CalendarActivity> activities_list = new ArrayList<>();
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

        if (date_focus.getYear() % 4 != 0 && amount_of_days == 29) {
            amount_of_days = 28;
        }

        int day_of_week_start = ZonedDateTime.of(date_focus.getYear(), date_focus.getMonthValue(), 1, 0, 0, 0, 0, date_focus.getZone()).getDayOfWeek().getValue();
        ZonedDateTime date_for_getting = ZonedDateTime.of(2025, date_focus.getMonthValue(), 1, 0, 0, 0, 0, date_focus.getZone());

        for (int i = 0; i < 6; i++) {

            for (int j = 0; j < 7; j++) {

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
                            activity_list = events.get(date_for_getting.toLocalDate().minusDays(day_of_week_start));
                        } catch (Exception e) {
                            e.printStackTrace();
                        }

                        if (activity_list != null) {
                            create_event_on_calendar(activity_list, rectangle_height, rectangle_width, pane);
                        }
                    }
                }
                date_for_getting = date_for_getting.plusDays(1);
                month_flowpane.getChildren().add(pane);
            }
        }

        for (CalendarActivity activity : activities_list) {

            LocalDate local_date_bruh = LocalDate.from(activity.get_start_date());
            LocalTime local_time_bruh = LocalTime.from(activity.get_start_time());

            LocalDateTime target_date_time = LocalDateTime.of(local_date_bruh, local_time_bruh);

            schedule_task(target_date_time, () -> {
                send_notification(activity);
            });
        }
    }

    private void create_event_on_calendar(List<CalendarActivity> activity_list, double rectangle_height, double rectangle_width, StackPane pane) {

        System.out.println(activity_list.size());
        VBox activity_vbox = new VBox();
        for (int i = 0; i < activity_list.size(); i++) {
            if (i >= 1) {
                Text moreActivities = new Text("...");
                activity_vbox.getChildren().add(moreActivities);
                moreActivities.setOnMouseClicked(mouseEvent -> {
                    System.out.println(activity_list);
                });
                break;
            }
            String title = activity_list.get(i).get_title();
            String description = activity_list.get(i).get_description();
            Text text = new Text(title + "\n"
                    + description + "\n"
                    + activity_list.get(i).get_start_time() + "\n"
                    + activity_list.get(i).get_end_time() + "\n");
            activity_vbox.getChildren().add(text);
            text.setOnMouseClicked(mouseEvent -> {
                System.out.println(text.getText());

                for (int j = 0; j < activity_list.size(); j++) {
                    // && activities_list.get(j).get_start_date().equals(activity_list.get(j).get_start_date())
                    // Gonna work on this later.
                    if (activities_list.get(j).get_title().equals(title)) {
                        System.out.println("Found!");
                        activities_list.remove(activities_list.get(j));
                        events = create_map(activities_list);
                        make_month();
                    }
                }
            });
        }
        activity_vbox.setTranslateY((rectangle_height / 2) * 0.20);
        activity_vbox.setMaxWidth(rectangle_width * 0.8);
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

        LocalDate event_start_localdate = LocalDate.parse(event_start_datepicker.getValue().toString());
        ZonedDateTime event_start_date = event_start_localdate.atStartOfDay(ZoneId.systemDefault());

        LocalDate event_end_localdate = LocalDate.parse(event_end_datepicker.getValue().toString());
        ZonedDateTime event_end_date = event_end_localdate.atStartOfDay(ZoneId.systemDefault());

        String event_name = event_name_field.getText();
        String event_description = event_description_field.getText();

        LocalTime event_start_time = start_time_picker.getTime();
        LocalTime event_end_time = end_time_picker.getTime();

        if (event_name.isEmpty()) {
            Random rand = new Random();
            int random_number = rand.nextInt(1000) + 1;
            event_name = String.valueOf(random_number);
        }
        ;

        if ((end_time_picker.getTime() == null && start_time_picker.getTime() != null)) {
            event_end_time = event_start_time;
        }

        if (event_end_date.isBefore(event_start_date) || end_time_picker.getValue() == null) {
            event_end_date = event_start_date;
        }

        CalendarActivity activity;

        activity = new CalendarActivity(event_name, event_description, event_start_date, event_end_date, event_start_time, event_end_time);

        activities_list.add(activity);
        events = create_map(activities_list);

        System.out.println(events);
        make_month();
    }

    private Map<LocalDate, List<CalendarActivity>> create_map(List<CalendarActivity> activities) {

        Map<LocalDate, List<CalendarActivity>> event_map = new HashMap<>();

        // This is an ugly function, I'm sorry to anyone who has to look at this 🫥
        // Also thanks to https://www.baeldung.com/java-between-dates
        for (CalendarActivity activity : activities) {

            List<ZonedDateTime> event_dates = new ArrayList<>();
            ZonedDateTime iterative_date = activity.get_start_date();

            while (iterative_date.isBefore(activity.get_end_date()) || iterative_date.isEqual(activity.get_end_date())) {

                if (!(event_map.containsKey(iterative_date.toLocalDate()))) {
                    event_map.put(iterative_date.toLocalDate(), new ArrayList<>());
                }

                event_map.get(iterative_date.toLocalDate()).add(activity);
                iterative_date = iterative_date.plusDays(1);
            }
        }

        return event_map;
    }

    private void schedule_task(LocalDateTime target_date_time, Runnable task) {

        LocalDateTime now = LocalDateTime.now();

        long delay = java.time.Duration.between(now, target_date_time).toMillis();
        System.out.println(delay);
        if (delay < 0) return;

        ScheduledExecutorService scheduler = Executors.newSingleThreadScheduledExecutor();

        scheduler.schedule(() -> {
            try {
                task.run();
            } finally {
                scheduler.shutdown();
            }
        }, delay, TimeUnit.MILLISECONDS);
    }

    private void send_notification(CalendarActivity activity) {

        if (do_not_disturb) return;

        System.out.println("Sending notification!");
        Platform.runLater(() -> {
                Notifications notifications = Notifications.create()
                .title(activity.get_title())
                .text(activity.get_description())
                .graphic(null)
                .hideAfter(Duration.seconds(5))
                .position(Pos.TOP_CENTER);
        notifications.show();
        });
    }
}