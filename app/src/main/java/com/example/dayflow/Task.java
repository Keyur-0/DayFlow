package com.example.dayflow;

import java.util.ArrayList;
import java.util.List;
public class Task {
    String name;
    List<Exercise> exercises;
    public Task(String name) {
        this.name = name;
        exercises = new ArrayList<>();
    }
}