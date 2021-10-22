package nobility.tools;

import java.util.Calendar;

public class Tools {

    public static String getDateForFolder() {
        Calendar c = Calendar.getInstance();
        int day = c.get(Calendar.DAY_OF_MONTH);
        int year = c.get(Calendar.YEAR);
        int month = c.get(Calendar.MONTH) + 1;
        return month + "-" + day + "-" + year;
    }

    public static String getCurrentTimeForFolder() {
        Calendar c = Calendar.getInstance();
        int hour = c.get(Calendar.HOUR);
        int minute = c.get(Calendar.MINUTE);
        return hour + ";" + minute;
    }

    public static int ordinalIndexOf(String str, String substr, int n) {
        int pos = -1;
        do {
            pos = str.indexOf(substr, pos + 1);
        } while (n-- > 0 && pos != -1);
        return pos;
    }
}
