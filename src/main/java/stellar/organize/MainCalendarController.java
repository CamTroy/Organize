package stellar.organize;

import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import javafx.scene.control.CheckBox;
import javafx.scene.layout.FlowPane;
import javafx.scene.layout.StackPane;
import javafx.scene.paint.Color;
import javafx.scene.shape.Rectangle;
import javafx.scene.text.Text;

import java.net.URL;
import java.time.ZonedDateTime;
import java.util.ResourceBundle;

public class MainCalendarController implements Initializable {

    @FXML
    private CheckBox dnd_checkbox;

    @FXML
    private Text year_text, month_text;

    @FXML
    private FlowPane month_flowpane;

    ZonedDateTime today;

    boolean do_not_disturb;

    @Override
    public void initialize(URL url, ResourceBundle bundle) {

        today = ZonedDateTime.now();
        make_month();
    }

    private void make_month() {

        year_text.setText(String.valueOf(today.getYear()));
        month_text.setText(today.getMonth().toString());

        double month_width = month_flowpane.getPrefWidth();
        double month_height = month_flowpane.getPrefHeight();
        double stroke_width = 1;
        double spacing_h = month_flowpane.getHgap();
        double spacing_v = month_flowpane.getVgap();

        int amount_of_days = today.getMonth().maxLength();

        int day_of_week_start = ZonedDateTime.of(today.getYear(), today.getMonthValue(), 1, 0, 0, 0, 0, today.getZone()).getDayOfWeek().getValue();

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

                int butt_date = (j + 1) + (7 * i);
                if (butt_date > day_of_week_start) {

                    int true_date = butt_date - day_of_week_start;
                    if (true_date <= amount_of_days) {

                        Text day = new Text(String.valueOf(true_date));
                        pane.getChildren().add(day);
                    }
                }
                month_flowpane.getChildren().add(pane);
            }
        }
    }

    public void toggle_dnd(ActionEvent event) {
        if (dnd_checkbox.isSelected()) {
            do_not_disturb = true;
        } else {
            do_not_disturb = false;
        }
    }
}