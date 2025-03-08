package stellar.organize;

import com.dlsc.gemsfx.TimePicker;
import javafx.event.ActionEvent;
import javafx.event.Event;
import javafx.fxml.FXML;
import javafx.scene.control.*;
import javafx.scene.layout.AnchorPane;
import javafx.stage.Stage;

import java.net.URL;
import java.time.*;
import java.util.Random;
import java.util.ResourceBundle;

public class ActivityCreatorController extends MainCalendarController {

    @FXML
    private ChoiceBox<String> repeating_choice_box;

    @FXML
    private DatePicker start_date_picker, end_date_picker;

    @FXML
    private TextField activity_name_field, location_text_field;

    @FXML
    private TextArea activity_description_field;

    @FXML
    private TimePicker start_time_picker, end_time_picker;

    @FXML
    private CheckBox all_day_checkbox;

    @FXML
    private Button create_activity_button;

    @FXML
    private AnchorPane anchor_pane;
    
    private MainCalendarController main_calendar_controller;

    public ActivityCreatorController() {}

    @Override
    public void initialize(URL url, ResourceBundle bundle) {

        String[] choice_box_choices = {"Daily", "Weekly", "Monthly", "Yearly", "None"};
        repeating_choice_box.getItems().addAll(choice_box_choices);
        repeating_choice_box.setValue(choice_box_choices[4]);
    }

    public void set_MainCalendarController(MainCalendarController main_calendar_controller) {
        this.main_calendar_controller = main_calendar_controller;
    }

    public void create_activity(ActionEvent event) {

        System.out.println("Creating activity");
        if (start_date_picker.getValue() == null ||
                end_date_picker.getValue() == null ||
                activity_name_field.getText() == null || activity_name_field.getText().trim().isEmpty() ||
                repeating_choice_box.getSelectionModel().getSelectedItem() == null) {
            return;
        }

        if (start_time_picker.getTime().equals(end_time_picker.getTime())) {
            end_time_picker.setTime(start_time_picker.getTime().plusMinutes(15));
        }

        String activity_description;
        if (activity_description_field.getText() == null || activity_description_field.getText().trim().isEmpty()) {

            activity_description = "";
        } else {
           activity_description = activity_description_field.getText();
        }

        LocalTime activity_start_time = null;
        LocalTime activity_end_time = null;
        if ((all_day_checkbox.isSelected())) {

            activity_start_time = LocalTime.of(0, 0);
            activity_end_time = LocalTime.of(23, 59);
        } else if (start_time_picker.getTime() == null || end_time_picker.getTime() == null) {
            return;
        } else {

            activity_start_time = start_time_picker.getTime();
            activity_end_time = end_time_picker.getTime();
        }

        LocalDate activity_start_date = start_date_picker.getValue();
        LocalDate activity_end_date = end_date_picker.getValue();

        String activity_name = activity_name_field.getText();

        String location = "";

        if (!(location_text_field.getText() == null || location_text_field.getText().trim().isEmpty())) {
            location = location_text_field.getText();
        }

        String repeating = repeating_choice_box.getSelectionModel().getSelectedItem();

        if (activity_name.isEmpty()) {

            Random rand = new Random();
            int random_number = rand.nextInt(100000) + 1;
            activity_name = String.valueOf(random_number);
        }

        CalendarActivity activity;

        activity = new CalendarActivity(activity_name, activity_description, activity_start_date, activity_end_date, activity_start_time, activity_end_time, repeating, location);

        main_calendar_controller.activity_list.add(activity);
        main_calendar_controller.activity_map = main_calendar_controller.create_map(main_calendar_controller.activity_list, main_calendar_controller.date_focus.toLocalDate());

        System.out.println("Activity map: " + main_calendar_controller.activity_map);

        LocalDate local_date_bruh = LocalDate.from(activity.get_start_date());
        LocalTime local_time_bruh = LocalTime.from(activity.get_start_time());

        LocalDateTime target_date_time = LocalDateTime.of(local_date_bruh, local_time_bruh);

        if (!(activity.get_repeating().equals("None"))) {
            main_calendar_controller.schedule_repeating_task(activity, target_date_time, () -> {
                main_calendar_controller.send_notification(activity);
            });
        } else {
            System.out.println("Schedule non-repeating.");
            schedule_task(target_date_time, () -> {
                main_calendar_controller.send_notification(activity);
            });
        }

        main_calendar_controller.create_week_activities_stuff();
        main_calendar_controller.make_month();
        Stage stage = (Stage) create_activity_button.getScene().getWindow();
        stage.close();
    }

    public void set_all_day(ActionEvent event) {

        boolean all_day = !(all_day_checkbox.isSelected());
        start_time_picker.setVisible(all_day);
        end_time_picker.setVisible(all_day);
    }

    public void correct_end_time(Event event) {

        if (end_time_picker.getTime() == null || start_time_picker.getTime().isAfter(end_time_picker.getTime())) {
            end_time_picker.setTime(start_time_picker.getTime());
        }
    }

    public void correct_end_date(Event event) {

        if (end_date_picker.getValue() == null || start_date_picker.getValue().isAfter(end_date_picker.getValue())) {
            end_date_picker.setValue(start_date_picker.getValue());
        }
    }

    // public void correct_start_time(Event event) {
    //
    //     if (start_time_picker.getTime() == null || end_time_picker.getTime().isBefore(start_time_picker.getTime())) {
    //         start_time_picker.setTime(end_time_picker.getTime().minusMinutes(15));
    //     }
    // }
    //
    // public void correct_start_date(Event event) {
    //
    //     if (start_date_picker.getValue() == null || end_date_picker.getValue().isBefore(start_date_picker.getValue())) {
    //         start_date_picker.setValue(end_date_picker.getValue());
    //     }
    // }
}
