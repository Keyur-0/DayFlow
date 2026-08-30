package com.example.dayflow;

import java.util.ArrayList;
import java.util.List;

public class Day {
    String date;
    List<Task> tasks;
    public Day(String date) {
        this.date = date;
        tasks = new ArrayList<>();
    }
}
