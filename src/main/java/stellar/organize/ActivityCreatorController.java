package stellar.organize;

import com.dlsc.gemsfx.TimePicker;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.scene.control.*;

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
                activity_description_field.getText() == null || activity_description_field.getText().trim().isEmpty() ||
                start_time_picker.getTime() == null ||
                end_time_picker.getTime() == null ||
                repeating_choice_box.getSelectionModel().getSelectedItem() == null) {
            return;
        }

        LocalDate activity_start_localdate = LocalDate.parse(start_date_picker.getValue().toString());
        ZonedDateTime activity_start_date = activity_start_localdate.atStartOfDay(ZoneId.systemDefault());

        LocalDate activity_end_localdate = LocalDate.parse(end_date_picker.getValue().toString());
        ZonedDateTime activity_end_date = activity_end_localdate.atStartOfDay(ZoneId.systemDefault());

        String activity_name = activity_name_field.getText();
        String activity_description = activity_description_field.getText();

        LocalTime activity_start_time = start_time_picker.getTime();
        LocalTime activity_end_time = end_time_picker.getTime();

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

        if (activity_end_time.isBefore(activity_start_time)) {
            activity_end_time = activity_start_time.plusMinutes(15);
        }

        if (activity_end_date.isBefore(activity_start_date)) {
            activity_end_date = activity_start_date;
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
            schedule_task(target_date_time, () -> {
                main_calendar_controller.send_notification(activity);
            });
        }

        main_calendar_controller.make_month();
    }

    public void set_all_day(ActionEvent event) {

        boolean all_day = !(all_day_checkbox.isSelected());
        start_time_picker.setVisible(all_day);
        end_time_picker.setVisible(all_day);
    }
}
