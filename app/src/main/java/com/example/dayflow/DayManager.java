package com.example.dayflow;

import java.util.ArrayList;
import java.util.List;
import java.util.Collections;
import java.util.Comparator;

public class DayManager {
    List<Day> days;
    public DayManager() {
        days = new ArrayList<>();
    }
    public void addDay(Day day) {

        days.add(day);

        Collections.sort(
                days,
                Comparator.comparing(d -> d.date)
        );
    }
    public Day getDay(String date) {
        for (Day day : days) {
            if (day.date.equals(date)) {
                return day;
            }
        }
        return null;
    }

    public boolean hasDay(String date) {
        return getDay(date) != null;
    }
}