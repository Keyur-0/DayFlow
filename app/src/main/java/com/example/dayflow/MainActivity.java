package com.example.dayflow;

import android.media.AudioManager;
import android.media.ToneGenerator;
import android.os.Bundle;
import android.os.CountDownTimer;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.AutoCompleteTextView;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.LinearLayout;
import android.widget.TextView;

import androidx.activity.EdgeToEdge;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;

import java.util.ArrayList;
import java.util.List;
import java.util.Locale;

import android.text.TextWatcher;
import android.text.Editable;

import android.app.DatePickerDialog;
import java.util.Calendar;
import android.widget.ScrollView;

public class MainActivity extends AppCompatActivity {
    Button playButton;
    Button addDateButton;
    ImageButton resetButton;
    CountDownTimer countDownTimer;
    Task currentTask;
    List<Exercise> exercises = new ArrayList<>();
    LinearLayout dayContainer;
    LinearLayout exerciseContainer;
    ScrollView scrollView;
    View todayDayView;

    DayManager dayManager;
    Day currentDay;
    int currentExercise = 0;
    int currentSet = 1;
    int currentRep = 1;
    boolean isRunning = false;
    ToneGenerator toneGenerator;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        EdgeToEdge.enable(this);
        setContentView(R.layout.activity_main);
        ViewCompat.setOnApplyWindowInsetsListener(
                findViewById(R.id.main),
                (v, insets) -> {
                    Insets systemBars =
                            insets.getInsets(
                                    WindowInsetsCompat.Type.systemBars()
                            );
                    v.setPadding(
                            systemBars.left,
                            systemBars.top,
                            systemBars.right,
                            systemBars.bottom
                    );
                    return insets;
                }
        );
        playButton = findViewById(R.id.playButton);
        resetButton = findViewById(R.id.resetButton);
        dayContainer = findViewById(R.id.dayContainer);
        addDateButton = findViewById(R.id.addDateButton);
        scrollView = findViewById(R.id.scrollView2);

        dayManager = new DayManager();
        String todayDate = DayUtils.getTodayDate();
        if (!dayManager.hasDay(todayDate)) {
            Day today = new Day(todayDate);
            dayManager.addDay(today);
        }
        currentDay = dayManager.getDay(todayDate);
        if (currentDay.tasks.isEmpty()) {
            Task workout = new Task("Workout 1");
            currentDay.tasks.add(workout);
        }
        currentTask = currentDay.tasks.get(0);
        displayAllDays();
//        displayDay(currentDay);
        toneGenerator =
                new ToneGenerator(
                        AudioManager.STREAM_ALARM,
                        100
                );
        playButton.setOnClickListener(v -> {
            if (!isRunning) {
                startWorkout();
            } else {
                pauseTimer();
            }
        });
        resetButton.setOnClickListener(v -> {
            resetWorkout();
        });
        addDateButton.setOnClickListener(v -> {
            openDatePicker();
        });
    }
    private void displayDay(Day day) {
        LayoutInflater inflater = LayoutInflater.from(this);

        View dayView = inflater.inflate(
                R.layout.day_item,
                dayContainer,
                false
        );

        TextView dayTitle = dayView.findViewById(R.id.dayTitle);
        TextView dayDate = dayView.findViewById(R.id.dayDate);
        LinearLayout dayExerciseContainer = dayView.findViewById(R.id.dayExerciseContainer);
        Button addExercise = dayView.findViewById(R.id.dayAddExercise);
        String todayDate = DayUtils.getTodayDate();
        boolean isPastDay = day.date.compareTo(todayDate) < 0;
        if (isPastDay) {
            addExercise.setEnabled(false);
            addExercise.setAlpha(0.4f);
        }
        Task task = day.tasks.get(0);

        if (day.date.equals(DayUtils.getTodayDate())) {
            exerciseContainer = dayExerciseContainer;
        }

        for (Exercise exercise : task.exercises) {
            addNewExercise(
                    dayExerciseContainer,
                    exercise
            );
        }
        if (isPastDay) {
            for (int i = 0; i < dayExerciseContainer.getChildCount(); i++) {
                View exerciseView =
                        dayExerciseContainer.getChildAt(i);
                setExerciseEditable(exerciseView, false);
            }
        }

        addExercise.setOnClickListener(v -> {

            if (isPastDay) {
                return;
            }

            Exercise exercise = new Exercise(
                    "New Exercise",
                    1,
                    10,
                    5,
                    30
            );
            task.exercises.add(exercise);

            addNewExercise(
                    dayExerciseContainer,
                    exercise
            );
        });

        Calendar calendar = Calendar.getInstance();


        calendar.add(Calendar.DAY_OF_YEAR, -1);

        String yesterdayDate = String.format(
                Locale.getDefault(),
                "%04d-%02d-%02d",
                calendar.get(Calendar.YEAR),
                calendar.get(Calendar.MONTH) + 1,
                calendar.get(Calendar.DAY_OF_MONTH)
        );

        calendar.add(Calendar.DAY_OF_YEAR, 2);
        String tomorrowDate = String.format(
                Locale.getDefault(),
                "%04d-%02d-%02d",
                calendar.get(Calendar.YEAR),
                calendar.get(Calendar.MONTH) + 1,
                calendar.get(Calendar.DAY_OF_MONTH)
        );
        if (day.date.equals(todayDate)) {
            todayDayView = dayView;
            dayTitle.setText("Today");
            dayView.setAlpha(1.0f);
        } else if (day.date.equals(yesterdayDate)) {
            dayTitle.setText("Yesterday");
            dayView.setAlpha(0.5f);
        } else if (day.date.equals(tomorrowDate)) {
            dayTitle.setText("Tomorrow");
            dayView.setAlpha(0.5f);

        } else {
            dayTitle.setText("");
            dayView.setAlpha(0.40f);
        }

        dayDate.setText(
                DayUtils.formatDateForDisplay(day.date)
        );

        dayContainer.addView(dayView);
    }

    private void addNewExercise(
            LinearLayout exerciseContainer,
            Exercise exercise
    ) {

        LayoutInflater inflater = LayoutInflater.from(this);

        View exerciseView = inflater.inflate(
                R.layout.exercise_item,
                exerciseContainer,
                false
        );

        EditText exerciseName = exerciseView.findViewById(R.id.exerciseName);

        AutoCompleteTextView setInput = exerciseView.findViewById(R.id.setInput);

        AutoCompleteTextView repsInput = exerciseView.findViewById(R.id.repsInput);

        EditText timerValue = exerciseView.findViewById(R.id.timerValue);

        EditText restInput = exerciseView.findViewById(R.id.restInput);

        TextView deleteTask = exerciseView.findViewById(R.id.deleteTask);
        exerciseName.setText(exercise.name);
        setInput.setText(
                String.valueOf(exercise.sets)
        );
        repsInput.setText(
                String.valueOf(exercise.reps)
        );
        timerValue.setText(
                String.format(
                        Locale.getDefault(),
                        "%02d:%02d",
                        exercise.timerSeconds / 60,
                        exercise.timerSeconds % 60
                )
        );
        restInput.setText(
                String.format(
                        Locale.getDefault(),
                        "%02d:%02d",
                        exercise.restSeconds / 60,
                        exercise.restSeconds % 60
                )
        );
        exerciseName.addTextChangedListener(
                new TextWatcher() {
                    @Override
                    public void beforeTextChanged(
                            CharSequence s,
                            int start,
                            int count,
                            int after
                    ) {
                    }
                    @Override
                    public void onTextChanged(
                            CharSequence s,
                            int start,
                            int before,
                            int count
                    ) {
                        exercise.name = s.toString();
                    }
                    @Override
                    public void afterTextChanged(
                            Editable s
                    ) {
                    }
                }
        );
        deleteTask.setOnClickListener(v -> {
            deleteExercise(
                    exerciseContainer,
                    exerciseView,
                    exercise
            );
        });
        exerciseContainer.addView(exerciseView);
    }
    private void deleteExercise(
            LinearLayout exerciseContainer,
            View exerciseView,
            Exercise exercise
    ) {

        currentTask.exercises.remove(exercise);
        exerciseContainer.removeView(exerciseView);
    }
    private void loadExercises() {
        Task task = currentTask;

        for (int i = 0; i < task.exercises.size(); i++) {
            Exercise exercise = task.exercises.get(i);

            View exerciseView = exerciseContainer.getChildAt(i);

            exercise.sets = getSets(exerciseView);
            exercise.reps = getReps(exerciseView);
            exercise.timerSeconds = getTimerValue(exerciseView);
            exercise.restSeconds = getRestValue(exerciseView);
        }
        exercises = task.exercises;
    }
    private void startWorkout() {

        String todayDate = DayUtils.getTodayDate();
        currentDay = dayManager.getDay(todayDate);

        if (currentDay == null || currentDay.tasks.isEmpty()) {
            return;
        }
        scrollToToday();

        currentTask = currentDay.tasks.get(0);
        loadExercises();

        if (exercises.isEmpty()) {
            return;
        }

        currentExercise = 0;
        currentSet = 1;
        currentRep = 1;
        isRunning = true;

        startCurrentExercise();
    }
    private void startCurrentExercise() {
        currentSet = 1;
        currentRep = 1;

        startRepTimer();
    }
    private void startRepTimer() {
        Exercise exercise = exercises.get(currentExercise);
        EditText timerView = getCurrentTimerView();

        timerView.setText(
                String.format(
                        "%02d:%02d",
                        exercise.timerSeconds / 60,
                        exercise.timerSeconds % 60
                )
        );

        countDownTimer =
                new CountDownTimer(
                        exercise.timerSeconds * 1000L,
                        1000
                ) {
                    @Override
                    public void onTick(
                            long millisUntilFinished
                    ) {
                        int seconds =
                                (int) (
                                        millisUntilFinished
                                                / 1000
                                );
                        timerView.setText(
                                String.format(
                                        "%02d:%02d",
                                        seconds / 60,
                                        seconds % 60
                                )
                        );
                    }
                    @Override
                    public void onFinish() {
                        beep();
                        currentRep++;
                        if (currentRep <= exercise.reps) {
                            startRepTimer();
                        } else {
                            finishSet();
                        }
                    }
                }.start();
    }
    private void finishSet() {
        Exercise exercise = exercises.get(currentExercise);

        if (currentSet < exercise.sets) {
            startBreakTimer();
        } else {
            finishExercise();
        }
    }
    private void startBreakTimer() {
        Exercise exercise = exercises.get(currentExercise);
        EditText timerView = getCurrentTimerView();

        timerView.setText(
                String.format(
                        "%02d:%02d",
                        exercise.restSeconds / 60,
                        exercise.restSeconds % 60
                )
        );
        countDownTimer =
                new CountDownTimer(
                        exercise.restSeconds * 1000L,
                        1000
                ) {

                    @Override
                    public void onTick(
                            long millisUntilFinished
                    ) {
                        int seconds = (int) (millisUntilFinished / 1000);

                        timerView.setText(
                                String.format(
                                        "%02d:%02d",
                                        seconds / 60,
                                        seconds % 60
                                )
                        );
                    }

                    @Override
                    public void onFinish() {
                        beep();
                        currentSet++;
                        currentRep = 1;
                        startRepTimer();
                    }

                }.start();
    }

    private void finishExercise() {
        View finishedExerciseView =
                exerciseContainer.getChildAt(
                        currentExercise
                );

        EditText finishedTimer =
                finishedExerciseView.findViewById(
                        R.id.timerValue
                );

        finishedTimer.setText("DONE");

        currentExercise++;

        if (currentExercise < exercises.size()) {
            startCurrentExercise();
        } else {
            workoutFinished();
        }
    }
    private int getTimeInSeconds(String time) {

        try {
            String[] parts = time.split(":");

            int minutes = Integer.parseInt(parts[0]);
            int seconds = Integer.parseInt(parts[1]);

            return minutes * 60 + seconds;

        } catch (Exception e) {
            return 0;
        }
    }
    private int getTimerValue(View exerciseView) {

        EditText timerValue =
                exerciseView.findViewById(R.id.timerValue);

        return getTimeInSeconds(
                timerValue.getText().toString()
        );
    }
    private int getRestValue(View exerciseView) {

        EditText restInput =
                exerciseView.findViewById(R.id.restInput);

        return getTimeInSeconds(
                restInput.getText().toString()
        );
    }
    private int getSets(View exerciseView) {

        AutoCompleteTextView setInput =
                exerciseView.findViewById(R.id.setInput);

        try {
            return Integer.parseInt(setInput.getText().toString());
        } catch (NumberFormatException e) {
            return 0;
        }
    }
    private int getReps(View exerciseView) {

        AutoCompleteTextView repsInput =
                exerciseView.findViewById(R.id.repsInput);

        try {
            return Integer.parseInt(repsInput.getText().toString());
        } catch (NumberFormatException e) {
            return 0;
        }
    }
    private EditText getCurrentTimerView() {

        View exerciseView =
                exerciseContainer.getChildAt(
                        currentExercise
                );

        return exerciseView.findViewById(
                R.id.timerValue
        );
    }


    private void pauseTimer() {
        if (countDownTimer != null) {
            countDownTimer.cancel();
        }
        isRunning = false;
    }
    private void resetWorkout() {
        if (countDownTimer != null) {
            countDownTimer.cancel();
        }

        currentExercise = 0;
        currentSet = 1;
        currentRep = 1;
        isRunning = false;

        if (
                exerciseContainer != null
                        && exerciseContainer.getChildCount() > 0
        ) {
            View exerciseView =
                    exerciseContainer.getChildAt(0);
            EditText timerView =
                    exerciseView.findViewById(
                            R.id.timerValue
                    );

            timerView.setText("00:05");
        }
    }
    private void workoutFinished() {
        isRunning = false;

        if (
                exerciseContainer != null
                        && exerciseContainer.getChildCount() > 0
        ) {

            View exerciseView =
                    exerciseContainer.getChildAt(
                            currentExercise - 1
                    );

            EditText timerView =
                    exerciseView.findViewById(
                            R.id.timerValue
                    );

            timerView.setText("DONE");
        }
    }
    private void beep() {
        toneGenerator.startTone(
                ToneGenerator.TONE_PROP_BEEP,
                200
        );
    }

    @Override
    protected void onDestroy() {
        if (countDownTimer != null) {
            countDownTimer.cancel();
        }
        if (toneGenerator != null) {
            toneGenerator.release();
        }
        super.onDestroy();
    }
    private void openDatePicker() {

        Calendar calendar = Calendar.getInstance();

        int year = calendar.get(Calendar.YEAR);
        int month = calendar.get(Calendar.MONTH);
        int day = calendar.get(Calendar.DAY_OF_MONTH);

        DatePickerDialog datePickerDialog =
                new DatePickerDialog(
                        MainActivity.this,
                        (view, selectedYear, selectedMonth, selectedDay) -> {

                            String selectedDate =
                                    String.format(
                                            Locale.getDefault(),
                                            "%04d-%02d-%02d",
                                            selectedYear,
                                            selectedMonth + 1,
                                            selectedDay
                                    );

                            addNewDay(selectedDate);
                        },
                        year,
                        month,
                        day
                );

        datePickerDialog.show();
    }
    private void addNewDay(String selectedDate) {
        String todayDate = DayUtils.getTodayDate();

        if (selectedDate.compareTo(todayDate) < 0) {
            return;
        }
        if (dayManager.hasDay(selectedDate)) {
            return;
        }

        Day newDay = new Day(selectedDate);
        Task workout = new Task("Workout 1");
        newDay.tasks.add(workout);

        dayManager.addDay(newDay);

        displayAllDays();
    }
    private void displayAllDays() {

        dayContainer.removeAllViews();
        todayDayView = null;

        for (Day day : dayManager.days) {
            displayDay(day);
        }
        scrollToToday();
    }
    private void setExerciseEditable(
            View exerciseView,
            boolean editable
    ) {

        EditText exerciseName =
                exerciseView.findViewById(R.id.exerciseName);

        AutoCompleteTextView setInput =
                exerciseView.findViewById(R.id.setInput);

        AutoCompleteTextView repsInput =
                exerciseView.findViewById(R.id.repsInput);

        EditText timerValue =
                exerciseView.findViewById(R.id.timerValue);

        EditText restInput =
                exerciseView.findViewById(R.id.restInput);

        TextView deleteExercise =
                exerciseView.findViewById(R.id.deleteTask);


        exerciseName.setEnabled(editable);
        setInput.setEnabled(editable);
        repsInput.setEnabled(editable);
        timerValue.setEnabled(editable);
        restInput.setEnabled(editable);

        deleteExercise.setEnabled(editable);
        if (!editable) {
            deleteExercise.setAlpha(0.4f);
        } else {
            deleteExercise.setAlpha(1.0f);
        }
    }
    private void scrollToToday() {

        if (todayDayView == null) {
            return;
        }

        scrollView.post(() -> {
            scrollView.smoothScrollTo(
                    0,
                    todayDayView.getTop()
            );
        });
    }
}
