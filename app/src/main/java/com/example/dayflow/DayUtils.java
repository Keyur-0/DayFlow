package com.example.dayflow;

import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Locale;

public class DayUtils {

    public static String getTodayDate() {
        SimpleDateFormat formatter =
                new SimpleDateFormat("yyyy-MM-dd", Locale.getDefault());
        return formatter.format(new Date());
    }
    public static String formatDateForDisplay(String date) {
        try {
            SimpleDateFormat inputFormat =
                    new SimpleDateFormat("yyyy-MM-dd", Locale.getDefault());

            SimpleDateFormat outputFormat =
                    new SimpleDateFormat("dd / MM / yyyy", Locale.getDefault());

            Date parsedDate = inputFormat.parse(date);

            return outputFormat.format(parsedDate);

        } catch (Exception e) {
            return date;
        }
    }
}