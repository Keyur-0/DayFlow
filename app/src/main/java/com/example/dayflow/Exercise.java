package com.example.dayflow;

public class Exercise {

    String name;
    int sets;
    int reps;
    int timerSeconds;
    int restSeconds;

    public Exercise(
            String name,
            int sets,
            int reps,
            int timerSeconds,
            int restSeconds
    ) {

        this.name = name;
        this.sets = sets;
        this.reps = reps;
        this.timerSeconds = timerSeconds;
        this.restSeconds = restSeconds;
    }
}