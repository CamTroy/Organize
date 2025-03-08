package stellar.organize;

import com.dlsc.gemsfx.Spacer;
import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule;
import javafx.application.Platform;
import javafx.event.ActionEvent;
import javafx.event.Event;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.fxml.Initializable;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.control.*;
import javafx.scene.layout.*;
import javafx.scene.paint.Color;
import javafx.scene.paint.Paint;
import javafx.scene.shape.Rectangle;
import javafx.scene.text.Font;
import javafx.scene.text.Text;
import javafx.stage.Stage;
import javafx.util.Duration;
import org.controlsfx.control.Notifications;

import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.net.URL;
import java.time.*;
import java.time.temporal.ChronoUnit;
import java.util.*;
import java.util.concurrent.*;

public class MainCalendarController implements Initializable {

    @FXML
    private BorderPane border_pane;

    @FXML
    private Button open_creator_button, prev_month_button, next_month_button, today_button;

    @FXML
    private CheckBox dnd_checkbox;

    @FXML
    private Text year_text, month_text;

    @FXML
    private FlowPane month_flowpane;

    @FXML
    private VBox week_activity_vbox, calendar_vbox;

    @FXML
    private HBox main_hbox;

    ZonedDateTime date_focus, today;

    boolean do_not_disturb;

    public static List<CalendarActivity> activity_list = new ArrayList<>();
    Map<LocalDate, List<CalendarActivity>> activity_map = new HashMap<>();
    List<Boolean> config_options = new ArrayList<>();

    String activities_path = "activities.json";
    String config_path = "config.json";

    @Override
    public void initialize(URL url, ResourceBundle bundle) {

        date_focus = ZonedDateTime.now();
        today = ZonedDateTime.now();

        try {
            activity_list = get_activities_from_file(activities_path);
        } catch (IOException e) {
            System.out.println("Activities.json does not exist or the file is empty! Ignoring...");
        }

        System.out.println(activity_list);

        try {
            config_options = get_config_from_file(config_path);
        } catch (IOException e) {
            System.out.println("Config.json does not exist or the file is empty! Ignoring...");
        }

        if (!config_options.isEmpty()) {
            do_not_disturb = config_options.get(0);
        }

        create_week_activities_stuff();
        dnd_checkbox.setSelected(do_not_disturb);
        make_month();
        VBox.setVgrow(month_flowpane, Priority.ALWAYS);
        HBox.setHgrow(calendar_vbox, Priority.ALWAYS);
        main_hbox.maxWidthProperty().bind(border_pane.widthProperty());
        main_hbox.maxHeightProperty().bind(border_pane.heightProperty());
        setup_notifications_on_start(activity_list);
        // border_pane.widthProperty().addListener((obs, old_val, new_val) -> {
        //
        // });
    }

    // A lot of this code was created with the help of this video: https://www.youtube.com/watch?v=tlomjP5uvqo
    public void make_month() {

        activity_map = create_map(activity_list, date_focus.toLocalDate());
        month_flowpane.getChildren().clear();
        year_text.setText(String.valueOf(date_focus.getYear()));
        month_text.setText(date_focus.getMonth().toString());

        double stroke_width = 1;
        double spacing_h = month_flowpane.getHgap();
        double spacing_v = month_flowpane.getVgap();

        int amount_of_days = date_focus.getMonth().maxLength();

        if (date_focus.getYear() % 4 != 0 && amount_of_days == 29) {
            amount_of_days = 28;
        }

        int day_of_week_start = ZonedDateTime.of(date_focus.getYear(), date_focus.getMonthValue(), 1, 0, 0, 0, 0, date_focus.getZone()).getDayOfWeek().getValue();
        ZonedDateTime date_for_getting = ZonedDateTime.of(date_focus.getYear(), date_focus.getMonthValue(), 1, 0, 0, 0, 0, date_focus.getZone());

        for (int i = 0; i < 6; i++) {

            for (int j = 0; j < 7; j++) {

                StackPane pane = new StackPane();

                Rectangle rectangle = new Rectangle();
                rectangle.setFill(Color.TRANSPARENT);
                rectangle.setStroke(Paint.valueOf("#594f6f"));
                rectangle.setStrokeWidth(stroke_width);
                rectangle.widthProperty().bind(month_flowpane.widthProperty().divide(7).subtract(stroke_width).subtract(spacing_h));
                rectangle.heightProperty().bind(month_flowpane.heightProperty().divide(6).subtract(stroke_width).subtract(spacing_v));
                pane.getChildren().add(rectangle);

                int calculated_date = (j + 1) + (7 * i);
                if (calculated_date > day_of_week_start) {

                    int true_date = calculated_date - day_of_week_start;
                    if (true_date <= amount_of_days) {

                        Text day = new Text(String.valueOf(true_date));
                        pane.getChildren().add(day);
                        day.setFill(Paint.valueOf("#7f8490"));
                        StackPane.setAlignment(day, Pos.TOP_LEFT);
                        StackPane.setMargin(day, new Insets(10, 0, 0, 10));

                        List<CalendarActivity> activity_list = null;
                        try {
                            activity_list = activity_map.get(date_for_getting.toLocalDate().minusDays(day_of_week_start));
                        } catch (Exception e) {
                            e.printStackTrace();
                        }

                        if (activity_list != null) {
                            create_activity_on_calendar(activity_list, rectangle.heightProperty().get(), rectangle.widthProperty().get(), pane);
                        }
                    }

                    pane.setOnMouseClicked(event -> {

                        open_activity_viewer(event, true_date, date_focus.toLocalDate());
                    });
                }
                date_for_getting = date_for_getting.plusDays(1);
                month_flowpane.getChildren().add(pane);
            }
        }
    }

    public void create_activity_on_calendar(List<CalendarActivity> activity_list, double rectangle_height, double rectangle_width, StackPane pane) {

        System.out.println(activity_list.size());
        VBox activity_vbox = new VBox();
        for (int i = 0; i < activity_list.size(); i++) {

            if (i >= 2) {
                Text moreActivities = new Text("...");
                activity_vbox.getChildren().add(moreActivities);
                moreActivities.setOnMouseClicked(mouseEvent -> {
                    System.out.println(activity_list);
                });
                break;
            }
            String title = activity_list.get(i).get_title();
            Text text = new Text(title);
            if (text.getText().length() >= 14) {
                text.setText(text.getText().substring(0, 11) + "...");
            }
            activity_vbox.getChildren().add(text);
            text.setOnMouseClicked(mouseEvent -> {
                System.out.println(text.getText());
            });
        }

        StackPane.setAlignment(activity_vbox, Pos.CENTER);
        activity_vbox.alignmentProperty().set(Pos.CENTER_LEFT);
        activity_vbox.maxWidthProperty().bind(pane.widthProperty().multiply(0.8));
        activity_vbox.maxHeightProperty().bind(pane.heightProperty().multiply(0.1));
        activity_vbox.setTranslateY(rectangle_height / 10);
        // activity_vbox.setMaxWidth(rectangle_width * 0.8);
        // activity_vbox.setMaxHeight(rectangle_height * 0.1);
        activity_vbox.setStyle("-fx-background-color:#b39df3");
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

    public void toggle_dnd(ActionEvent event) {
        do_not_disturb = dnd_checkbox.isSelected();
    }

    public Map<LocalDate, List<CalendarActivity>> create_map(List<CalendarActivity> activities, LocalDate date) {

        Map<LocalDate, List<CalendarActivity>> activity_map = new HashMap<>();

        // This is an ugly function, I'm sorry to anyone who has to look at this 🫥
        // Also thanks to https://www.baeldung.com/java-between-dates
        for (CalendarActivity activity : activities) {

            List<LocalDate> activity_dates = new ArrayList<>();
            LocalDate iterative_date = activity.get_start_date();

            while (iterative_date.isBefore(activity.get_end_date()) || iterative_date.isEqual(activity.get_end_date())) {

                // Create the Key if it doesn't already exist.
                if (!(activity_map.containsKey(iterative_date))) {
                    activity_map.put(iterative_date, new ArrayList<>());
                }

                activity_map.get(iterative_date).add(activity);

                iterative_date = iterative_date.plusDays(1);
            }

            activity_map = setup_repeating_activities(activity_map, activity, date);
        }

        return activity_map;
    }

    public Map<LocalDate, List<CalendarActivity>> setup_repeating_activities(Map<LocalDate, List<CalendarActivity>> activity_map, CalendarActivity activity, LocalDate date) {

        if (activity_map == null) {
            activity_map = new HashMap<>();
        }

        LocalDate current_date = date.withDayOfMonth(1);
        int days_in_month = current_date.getMonth().length(current_date.isLeapYear());

        for (int i = 1; i <= days_in_month; i++) {
            long days_between = ChronoUnit.DAYS.between(activity.get_start_date(), current_date);

            if (activity.get_repeating().equals("None")) {
                return activity_map;
            }

            if (activity.get_repeating().equals("Daily")
                    && activity.get_start_date().isBefore(current_date)
                    && days_between >= 1) {

                activity_map.computeIfAbsent(current_date, k -> new ArrayList<>()).add(activity);
            }

            if (activity.get_repeating().equals("Weekly")
                    && days_between >= 1
                    && days_between % 7 == 0) {

                activity_map.computeIfAbsent(current_date, k -> new ArrayList<>()).add(activity);
            }

            if (activity.get_repeating().equals("Monthly")
                    && activity.get_start_date().isBefore(current_date)
                    && activity.get_start_date().getDayOfMonth() == current_date.getDayOfMonth()) {
                activity_map.computeIfAbsent(current_date, k -> new ArrayList<>()).add(activity);
            }

            if (activity.get_repeating().equals("Yearly")
                    && activity.get_start_date().isBefore(current_date)
                    && (days_between == 365 || days_between == 366 && Year.isLeap(current_date.getYear()))) {
                activity_map.computeIfAbsent(current_date, k -> new ArrayList<>()).add(activity);
            }

            current_date = current_date.plusDays(1);
        }

        return activity_map;
    }

    public void schedule_task(LocalDateTime target_date_time, Runnable task) {

        // I don't think this is very optimized, but I honestly don't care very much.
        LocalDateTime now = LocalDateTime.now();

        long delay = java.time.Duration.between(now, target_date_time).toMillis();

        if (delay < 0) return;

        ScheduledExecutorService scheduler = Executors.newSingleThreadScheduledExecutor();
        System.out.println("Scheduling non-repeating...");

        scheduler.schedule(() -> {
            try {
                task.run();
            } finally {
                scheduler.shutdown();
            }
        }, delay, TimeUnit.MILLISECONDS);
    }

    protected void schedule_repeating_task(CalendarActivity activity, LocalDateTime target_date_time, Runnable task) {
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

    protected long calculate_initial_delay(LocalDateTime target_date_time) {
        LocalDateTime now = LocalDateTime.now();
        return java.time.Duration.between(now, target_date_time).getSeconds();
    }


    protected void send_notification(CalendarActivity activity) {

        if (do_not_disturb) return;

        System.out.println("Sending notification!");
        Platform.runLater(() -> {
            Notifications notifications = Notifications.create().title(activity.get_title()).text(activity.get_description()).graphic(null).hideAfter(Duration.seconds(5)).position(Pos.TOP_CENTER);
            notifications.show();
        });
    }

    public void open_activity_viewer(Event event, int day_of_month, LocalDate date_focus) {

        try {

            FXMLLoader fxml_loader = new FXMLLoader(getClass().getResource("activity_viewer.fxml"));
            Parent root1 = fxml_loader.load();
            Stage stage = new Stage();
            stage.setScene(new Scene(root1));
            stage.show();

            ActivityViewerController activity_viewer_controller = fxml_loader.getController();
            activity_viewer_controller.set_MainCalendarController(this);

            LocalDate date = date_focus.withDayOfMonth(day_of_month);
            List<CalendarActivity> activity_list = activity_map.get(date);

            activity_viewer_controller.list_activities_in_vbox(date, activity_list);
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    public void bring_to_today(Event event) {

        date_focus = today;
        make_month();
    }

    public void write_activities_to_file(String path, List<CalendarActivity> activity_list) throws IOException {

        System.out.println("Writing to: " + path);
        ObjectMapper mapper = new ObjectMapper();
        mapper.registerModule(new JavaTimeModule());

        new FileWriter(path, false).close();
        File file = new File(path);
        String JSON_string = "";

        mapper.writeValue(file, activity_list);

        System.out.println("Done writing to: " + path);
    }

    public void write_config_to_file(String path, List<Boolean> configurations) throws IOException {

        ObjectMapper mapper = new ObjectMapper();

        new FileWriter(path, false).close();
        File file = new File(path);
        String JSON_string = "";

        mapper.writeValue(file, configurations);
    }

    private List<CalendarActivity> get_activities_from_file(String path) throws IOException {

        if (!(new File(path).exists())) {
            return new ArrayList<>();
        }

        ObjectMapper mapper = new ObjectMapper();
        mapper.registerModule(new JavaTimeModule());

        return mapper.readValue(new File(path), new TypeReference<List<CalendarActivity>>() {
        });
    }

    private List<Boolean> get_config_from_file(String path) throws IOException {

        if (!(new File(path).exists())) {
            return new ArrayList<>();
        }

        ObjectMapper mapper = new ObjectMapper();

        return mapper.readValue(new File(path), List.class);
    }

    public void open_activity_creator(ActionEvent event) {

        try {

            FXMLLoader loader = new FXMLLoader(getClass().getResource("activity_creator.fxml"));
            Parent root2 = loader.load();
            Stage stage = new Stage();
            stage.setScene(new Scene(root2));
            stage.show();

            ActivityCreatorController creator_controller = loader.getController();
            creator_controller.set_MainCalendarController(this);
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    public void create_week_activities_stuff() {

        LocalDate iterative_date = today.toLocalDate();

        List<List<CalendarActivity>> this_weeks_stuff = new ArrayList<>();

        for (int i = 0; i < 7; i++) {
            this_weeks_stuff.add(new ArrayList<>());
        }

        for (int i = 0; i < this_weeks_stuff.size(); i++) {

            activity_map = create_map(activity_list, iterative_date);
            if (activity_map.get(iterative_date) != null) {
                this_weeks_stuff.set(i, activity_map.get(iterative_date));
            }
            iterative_date = iterative_date.plusDays(1);
        }

        week_activity_vbox.getChildren().clear();
        int index = 0;
        for (List<CalendarActivity> days_stuff : this_weeks_stuff) {
            for (CalendarActivity activity : days_stuff) {

                show_week_activity(activity, get_day_addition(this_weeks_stuff, activity, index));
            }
            index++;
        }
    }

    private int get_day_addition(List<List<CalendarActivity>> this_weeks_stuff, CalendarActivity activity, int index) {

        int day_addition = 0;
        System.out.println("Index: " + index);
        for (int i = 0; i < index; i++) {

            if (this_weeks_stuff.get(i).contains(activity)) {
                day_addition++;
            }
        }

        System.out.println("day_addition for " + activity.get_title() + ": " + day_addition);
        return day_addition;
    }

    public void show_week_activity(CalendarActivity activity, int day_addition) {

        if (activity == null) return;

        Text name = new Text(activity.get_title());
        Text description = new Text(activity.get_description());
        Text date = new Text(activity.get_start_date().plusDays(day_addition).getDayOfWeek().toString());
        Spacer spacer1 = new Spacer();
        Spacer spacer2 = new Spacer();
        Spacer spacer3 = new Spacer();
        spacer1.setMaxHeight(10);
        spacer2.setMaxHeight(5);
        spacer3.setMaxHeight(20);

        name.setFill(Color.valueOf("e2e2e3"));
        name.setFont(Font.font(20));
        name.wrappingWidthProperty().bind(week_activity_vbox.widthProperty().multiply(.9));;
        description.setFill(Color.valueOf("e2e2e3"));
        description.wrappingWidthProperty().bind(week_activity_vbox.widthProperty().multiply(.9));
        date.setFill(Color.valueOf("e2e2e3"));

        week_activity_vbox.getChildren().add(name);
        week_activity_vbox.getChildren().add(spacer2);
        week_activity_vbox.getChildren().add(date);
        week_activity_vbox.getChildren().add(spacer1);
        week_activity_vbox.getChildren().add(description);
        week_activity_vbox.getChildren().add(spacer3);
    }

    public void setup_notifications_on_start(List<CalendarActivity> activity_list) {

        for (CalendarActivity activity : activity_list) {
            if (!(activity.get_repeating().equals("None"))) {
        schedule_repeating_task(activity, LocalDateTime.of(activity.get_start_date(), activity.get_start_time()), () -> {
        send_notification(activity);
                });
            } else {
                schedule_task(LocalDateTime.of(activity.get_start_date(), activity.get_start_time()), () -> {
        send_notification(activity);
                });
            }
        }
    }
}