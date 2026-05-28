package com.example.ridesharefiji;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Date;
import java.util.Locale;

public class DateUtils {
    private static final String DATE_FORMAT = "dd/MM/yyyy";

    public static boolean isExpired(String dateStr) {
        if (dateStr == null || dateStr.isEmpty()) return false;

        SimpleDateFormat sdf = new SimpleDateFormat(DATE_FORMAT, Locale.getDefault());
        try {
            Date rideDate = sdf.parse(dateStr);
            if (rideDate == null) return false;

            // Get current date at midnight (to allow rides for today)
            Calendar cal = Calendar.getInstance();
            cal.set(Calendar.HOUR_OF_DAY, 0);
            cal.set(Calendar.MINUTE, 0);
            cal.set(Calendar.SECOND, 0);
            cal.set(Calendar.MILLISECOND, 0);
            Date today = cal.getTime();

            return rideDate.before(today);
        } catch (ParseException e) {
            return false;
        }
    }
}
