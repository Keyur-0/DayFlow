package com.example.dayflow;

import java.util.ArrayList;
import java.util.List;

public class DayManager {
    List<Day> days;
    public DayManager() {
        days = new ArrayList<>();
    }
    public void addDay(Day day) {
        days.add(day);
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