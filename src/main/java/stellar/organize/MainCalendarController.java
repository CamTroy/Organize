package stellar.organize;

import com.dlsc.gemsfx.TimePicker;
import javafx.application.Platform;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.control.*;
import javafx.scene.layout.FlowPane;
import javafx.scene.layout.Region;
import javafx.scene.layout.StackPane;
import javafx.scene.layout.VBox;
import javafx.scene.paint.Color;
import javafx.scene.shape.Rectangle;
import javafx.scene.text.Text;
import javafx.util.Duration;
import org.controlsfx.control.Notifications;

import java.net.URL;
import java.time.*;
import java.time.temporal.ChronoUnit;
import java.util.*;
import java.util.concurrent.*;

public class MainCalendarController implements Initializable {

    @FXML
    private Button create_event_button, prev_month_button, next_month_button;

    @FXML
    private DatePicker event_start_datepicker, event_end_datepicker;

    @FXML
    private CheckBox dnd_checkbox, all_day_checkbox;

    @FXML
    private Text year_text, month_text;

    @FXML
    private FlowPane month_flowpane;

    @FXML
    private TextField event_name_field, event_description_field, location_text_field;

    @FXML
    private TimePicker start_time_picker, end_time_picker;

    @FXML
    private ChoiceBox<String> repeating_choice_box;

    ZonedDateTime date_focus, today;

    boolean do_not_disturb = false;

    List<CalendarActivity> activities_list = new ArrayList<>();
    Map<LocalDate, List<CalendarActivity>> events = new HashMap<>();

    @Override
    public void initialize(URL url, ResourceBundle bundle) {

        String[] choice_box_choices = {"Daily", "Weekly", "Monthly", "Yearly", "None"};
        repeating_choice_box.getItems().addAll(choice_box_choices);
        repeating_choice_box.setValue(choice_box_choices[4]);

        date_focus = ZonedDateTime.now();
        today = ZonedDateTime.now();
        make_month();
    }

    // A lot of this code was created with the help of this video: https://www.youtube.com/watch?v=tlomjP5uvqo
    private void make_month() {

        events = create_map(activities_list, date_focus.toLocalDate());
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

                        /*for (CalendarActivity activity : activities_list) {
                            if (activity.get_repeating() != null) {

                            }
                        }*/
                    }
                }
                date_for_getting = date_for_getting.plusDays(1);
                month_flowpane.getChildren().add(pane);
            }
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
//            String description = activity_list.get(i).get_description();
//            Text text = new Text(title + "\n"
//                    + description + "\n"
//                    + activity_list.get(i).get_start_time() + "\n"
//                    + activity_list.get(i).get_end_time() + "\n");
            Text text = new Text(title);
            activity_vbox.getChildren().add(text);
            text.setOnMouseClicked(mouseEvent -> {
                System.out.println(text.getText());

                for (int j = 0; j < activity_list.size(); j++) {
                    // && activities_list.get(j).get_start_date().equals(activity_list.get(j).get_start_date())
                    // Gonna work on this later.
                    if (activities_list.get(j).get_title().equals(title)) {
                        System.out.println("Found!");
                        activities_list.remove(activities_list.get(j));
                        events = create_map(activities_list, date_focus.toLocalDate());
                        make_month();
                    }
                }
            });
        }

        StackPane.setAlignment(activity_vbox, Pos.TOP_CENTER);
        activity_vbox.setTranslateY(rectangle_height / 10);
        activity_vbox.setMaxWidth(rectangle_width * 0.8);
        activity_vbox.setMaxHeight(rectangle_height * 0.1);
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

        if (event_start_datepicker.getValue() == null ||
                event_end_datepicker.getValue() == null ||
                event_name_field.getText() == null || event_name_field.getText().trim().isEmpty() ||
                event_description_field.getText() == null || event_description_field.getText().trim().isEmpty() ||
                start_time_picker.getTime() == null ||
                end_time_picker.getTime() == null ||
                repeating_choice_box.getSelectionModel().getSelectedItem() == null) {
            return;
        }

        LocalDate event_start_localdate = LocalDate.parse(event_start_datepicker.getValue().toString());
        ZonedDateTime event_start_date = event_start_localdate.atStartOfDay(ZoneId.systemDefault());

        LocalDate event_end_localdate = LocalDate.parse(event_end_datepicker.getValue().toString());
        ZonedDateTime event_end_date = event_end_localdate.atStartOfDay(ZoneId.systemDefault());

        String event_name = event_name_field.getText();
        String event_description = event_description_field.getText();

        LocalTime event_start_time = start_time_picker.getTime();
        LocalTime event_end_time = end_time_picker.getTime();

        String location = "";

        if (!(location_text_field.getText() == null || location_text_field.getText().trim().isEmpty())) {
            location = location_text_field.getText();
        }

        String repeating = repeating_choice_box.getSelectionModel().getSelectedItem();

        if (event_name.isEmpty()) {

            Random rand = new Random();
            int random_number = rand.nextInt(100000) + 1;
            event_name = String.valueOf(random_number);
        }

        if (event_end_time.isBefore(event_start_time)) {
            event_end_time = event_start_time.plusMinutes(15);
        }

        if (event_end_date.isBefore(event_start_date)) {
            event_end_date = event_start_date;
        }

        CalendarActivity activity;

        activity = new CalendarActivity(event_name, event_description, event_start_date, event_end_date, event_start_time, event_end_time, repeating, location);

        activities_list.add(activity);
        events = create_map(activities_list, date_focus.toLocalDate());

        System.out.println(events);

        LocalDate local_date_bruh = LocalDate.from(activity.get_start_date());
        LocalTime local_time_bruh = LocalTime.from(activity.get_start_time());

        LocalDateTime target_date_time = LocalDateTime.of(local_date_bruh, local_time_bruh);

        if (!(activity.get_repeating().equals("None"))) {
            schedule_repeating_task(activity, target_date_time, () -> {
                send_notification(activity);
            });
        } else {
            schedule_task(target_date_time, () -> {
                send_notification(activity);
            });
        }

        make_month();
    }

    private Map<LocalDate, List<CalendarActivity>> create_map(List<CalendarActivity> activities, LocalDate date) {

        Map<LocalDate, List<CalendarActivity>> event_map = new HashMap<>();

        // This is an ugly function, I'm sorry to anyone who has to look at this 🫥
        // Also thanks to https://www.baeldung.com/java-between-dates
        for (CalendarActivity activity : activities) {

            List<ZonedDateTime> event_dates = new ArrayList<>();
            LocalDate iterative_date = activity.get_start_date().toLocalDate();

            while (iterative_date.isBefore(activity.get_end_date().toLocalDate()) || iterative_date.isEqual(activity.get_end_date().toLocalDate())) {

                // Create the Key if it doesn't already exist.
                if (!(event_map.containsKey(iterative_date))) {
                    event_map.put(iterative_date, new ArrayList<>());
                }

                event_map.get(iterative_date).add(activity);

                iterative_date = iterative_date.plusDays(1);
            }

            event_map = setup_repeating_events(event_map, activity, date);
        }

        return event_map;
    }

    private Map<LocalDate, List<CalendarActivity>> setup_repeating_events(Map<LocalDate, List<CalendarActivity>> event_map, CalendarActivity activity, LocalDate date) {

        if (event_map == null) {
            event_map = new HashMap<>();
        }

        LocalDate current_date = date.withDayOfMonth(1);
        int days_in_month = current_date.getMonth().length(current_date.isLeapYear());

        for (int i = 1; i <= days_in_month; i++) {
            long days_between = ChronoUnit.DAYS.between(activity.get_start_date().toLocalDate(), current_date);

            if (activity.get_repeating().equals("Daily")
                    && activity.get_start_date().toLocalDate().isBefore(current_date)
                    && days_between >= 1) {

                event_map.computeIfAbsent(current_date, k -> new ArrayList<>()).add(activity);
            }

            if (activity.get_repeating().equals("Weekly")
                    && days_between >= 1
                    && days_between % 7 == 0) {

                event_map.computeIfAbsent(current_date, k -> new ArrayList<>()).add(activity);
            }

            if (activity.get_repeating().equals("Monthly")
                    && activity.get_start_date().toLocalDate().isBefore(current_date)
                    && activity.get_start_date().getDayOfMonth() == current_date.getDayOfMonth()) {
                event_map.computeIfAbsent(current_date, k -> new ArrayList<>()).add(activity);
            }

            if (activity.get_repeating().equals("Yearly")
                    && activity.get_start_date().toLocalDate().isBefore(current_date)
                    && (days_between == 365 || days_between == 366 && Year.isLeap(current_date.getYear()))) {
                event_map.computeIfAbsent(current_date, k -> new ArrayList<>()).add(activity);
            }

            current_date = current_date.plusDays(1);
        }

        return event_map;
    }

    private void schedule_task(LocalDateTime target_date_time, Runnable task) {

        // I don't think this is very optimized, but I honestly don't care very much.
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

    private void schedule_repeating_task(CalendarActivity activity, LocalDateTime target_date_time, Runnable task) {
        ScheduledExecutorService scheduler = Executors.newScheduledThreadPool(1);

        long initialDelay = calculate_initial_delay(target_date_time);

        long period = TimeUnit.DAYS.toSeconds(1);

        scheduler.scheduleAtFixedRate(() -> {
            LocalDateTime currentDate = LocalDateTime.now();

            if (is_valid_event_day(target_date_time, currentDate)) {
                task.run();
            }
        }, initialDelay, period, TimeUnit.SECONDS);
    }

    private boolean is_valid_event_day(LocalDateTime target_date_time, LocalDateTime current_date) {
        int target_day = target_date_time.getDayOfMonth();
        int target_month = target_date_time.getMonthValue();

        if (current_date.getMonthValue() != target_month) {
            return false;
        }

        YearMonth currentYearMonth = YearMonth.of(current_date.getYear(), current_date.getMonthValue());
        if (target_day > currentYearMonth.lengthOfMonth()) {
            return false;
        }

        if (target_date_time.getMonthValue() == 2 && target_date_time.getDayOfMonth() == 29) {
            return Year.isLeap(current_date.getYear());
        }

        return current_date.getDayOfMonth() == target_day;
    }

    private long calculate_initial_delay(LocalDateTime target_date_time) {
        LocalDateTime now = LocalDateTime.now();
        return java.time.Duration.between(now, target_date_time).getSeconds();
    }


    private void send_notification(CalendarActivity activity) {

        if (do_not_disturb) return;

        System.out.println("Sending notification!");
        Platform.runLater(() -> {
            Notifications notifications = Notifications.create().title(activity.get_title()).text(activity.get_description()).graphic(null).hideAfter(Duration.seconds(5)).position(Pos.TOP_CENTER);
            notifications.show();
        });
    }

    public void set_all_day(ActionEvent event) {

        boolean all_day = !(all_day_checkbox.isSelected());
        start_time_picker.setVisible(all_day);
        end_time_picker.setVisible(all_day);
    }
}