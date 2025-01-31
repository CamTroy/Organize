package stellar.organize;

import java.time.ZonedDateTime;

public class CalendarActivity {
    private String title;
    private String description;
    private ZonedDateTime start_date;
    private ZonedDateTime end_date;

    public CalendarActivity(String title, String description, ZonedDateTime start_date) {
        this.title = title;
        this.description = description;
        this.start_date = start_date;
    }

    public CalendarActivity(String title, String description, ZonedDateTime start_date, ZonedDateTime end_date) {
        this.title = title;
        this.description = description;
        this.start_date = start_date;
        this.end_date = end_date;
    }

    public CalendarActivity() {
    }

    public String get_title() {
        return title;
    }

    public String get_description() {
        return description;
    }

    public ZonedDateTime get_start_date() {
        return start_date;
    }

    public ZonedDateTime get_end_date() {
        return end_date;
    }

    public void set_title(String title) {
        this.title = title;
    }

    public void set_description(String description) {
        this.description = description;
    }

    public void set_start_date(ZonedDateTime start_date) {
        this.start_date = start_date;
    }

    public void set_end_date(ZonedDateTime end_date) {
        this.end_date = end_date;
    }

    @Override
    public String toString() {
        return "title = '" + title + '\n' +
                "description = " + description + '\n' +
                "start_date = " + start_date;
    }
}
