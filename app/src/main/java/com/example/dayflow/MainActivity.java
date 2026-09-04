package com.example.dayflow;

import android.app.DatePickerDialog;
import android.media.AudioManager;
import android.media.ToneGenerator;
import android.os.Bundle;
import android.os.CountDownTimer;
import android.text.Editable;
import android.text.TextWatcher;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.AutoCompleteTextView;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.LinearLayout;
import android.widget.ScrollView;
import android.widget.TextView;

import androidx.activity.EdgeToEdge;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;

import java.util.ArrayList;
import java.util.Calendar;
import java.util.List;
import java.util.Locale;

public class MainActivity extends AppCompatActivity {

    Button playButton;
    Button addDateButton;
    ImageButton resetButton;

    CountDownTimer countDownTimer;

    Task currentTask;
    JsonManager jsonManager;

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

    boolean isUpdatingTimerView = false;
    long remainingMillis = 0;
    boolean isBreakTimer = false;

    int workoutRunId = 0;
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


        jsonManager = new JsonManager(this);

        List<Day> savedDays =
                jsonManager.loadDays();

        if (savedDays != null) {
            dayManager = new DayManager();
            dayManager.days.addAll(savedDays);
        }
        else {
            dayManager = new DayManager();
        }

        String todayDate = DayUtils.getTodayDate();

        boolean dataChanged = false;
        if (!dayManager.hasDay(todayDate)) {
            Day today = new Day(todayDate);
            dayManager.addDay(today);

            dataChanged = true;
        }

        currentDay = dayManager.getDay(todayDate);

        if (currentDay.tasks.isEmpty()) {
            Task workout = new Task("Workout 1");
            currentDay.tasks.add(workout);
            dataChanged = true;
        }
        if (dataChanged) {
            jsonManager.saveDays(
                    dayManager.days
            );
        }
        currentTask = currentDay.tasks.get(0);

        displayAllDays();
        toneGenerator =
                new ToneGenerator(
                        AudioManager.STREAM_ALARM,
                        100
                );

        playButton.setOnClickListener(v -> {
            if (isRunning) {
                pauseTimer();
            } else {
                if (remainingMillis > 0) {
                    resumeWorkout();
                } else {
                    startWorkout();
                }
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

        View dayView =
                inflater.inflate(
                        R.layout.day_item,
                        dayContainer,
                        false
                );

        TextView dayTitle =
                dayView.findViewById(
                        R.id.dayTitle
                );

        TextView dayDate =
                dayView.findViewById(
                        R.id.dayDate
                );

        LinearLayout dayExerciseContainer =
                dayView.findViewById(
                        R.id.dayExerciseContainer
                );

        Button addExercise =
                dayView.findViewById(
                        R.id.dayAddExercise
                );

        String todayDate = DayUtils.getTodayDate();


        boolean isPastDay = day.date.compareTo(todayDate) < 0;


        if (isPastDay) {
            addExercise.setEnabled(false);
            addExercise.setAlpha(0.4f);
        }
        Task task = day.tasks.get(0);

        if (day.date.equals(todayDate)) {
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

                View exerciseView = dayExerciseContainer.getChildAt(i);

                setExerciseEditable(exerciseView, false);
            }
        }

        addExercise.setOnClickListener(v -> {
            if (isPastDay) {
                return;
            }

            Exercise exercise =
                    new Exercise(
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

        String yesterdayDate =
                String.format(
                        Locale.getDefault(),
                        "%04d-%02d-%02d",
                        calendar.get(Calendar.YEAR),
                        calendar.get(Calendar.MONTH) + 1,
                        calendar.get(Calendar.DAY_OF_MONTH)
                );

        calendar.add(Calendar.DAY_OF_YEAR, 2);

        String tomorrowDate =
                String.format(
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
        }
        else if (day.date.equals(yesterdayDate)) {
            dayTitle.setText("Yesterday");
            dayView.setAlpha(0.5f);
        }
        else if (day.date.equals(tomorrowDate)) {

            dayTitle.setText("Tomorrow");

            dayView.setAlpha(0.5f);

        }
        else {
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
        LayoutInflater inflater =
                LayoutInflater.from(this);

        View exerciseView =
                inflater.inflate(
                        R.layout.exercise_item,
                        exerciseContainer,
                        false
                );

        EditText exerciseName =
                exerciseView.findViewById(
                        R.id.exerciseName
                );

        AutoCompleteTextView setInput =
                exerciseView.findViewById(
                        R.id.setInput
                );

        AutoCompleteTextView repsInput =
                exerciseView.findViewById(
                        R.id.repsInput
                );

        EditText timerValue =
                exerciseView.findViewById(
                        R.id.timerValue
                );

        EditText restInput =
                exerciseView.findViewById(
                        R.id.restInput
                );

        TextView deleteTask =
                exerciseView.findViewById(
                        R.id.deleteTask
                );

        exerciseName.setText(
                exercise.name
        );

        setInput.setText(
                String.valueOf(
                        exercise.sets
                )
        );
        repsInput.setText(
                String.valueOf(
                        exercise.reps
                )
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

                        exercise.name =
                                s.toString();
                    }

                    @Override
                    public void afterTextChanged(
                            Editable s
                    ) {

                        jsonManager.saveDays(
                                dayManager.days
                        );
                    }
                }
        );

        setInput.addTextChangedListener(
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

                        try {

                            exercise.sets =
                                    Integer.parseInt(
                                            s.toString()
                                    );
                        } catch (NumberFormatException e) {
                            exercise.sets = 0;
                        }
                    }

                    @Override
                    public void afterTextChanged(
                            Editable s
                    ) {
                        jsonManager.saveDays(
                                dayManager.days
                        );
                    }
                }
        );

        repsInput.addTextChangedListener(
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
                        try {
                            exercise.reps =
                                    Integer.parseInt(
                                            s.toString()
                                    );
                        } catch (NumberFormatException e) {
                            exercise.reps = 0;
                        }
                    }
                    @Override
                    public void afterTextChanged(
                            Editable s
                    ) {
                        jsonManager.saveDays(dayManager.days);
                    }
                }
        );

        timerValue.addTextChangedListener(
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
                        if (isUpdatingTimerView) {
                            return;
                        }

                        exercise.timerSeconds =
                                getTimeInSeconds(
                                        s.toString()
                                );
                    }
                    @Override
                    public void afterTextChanged(
                            Editable s
                    ) {
                        if (isUpdatingTimerView) {
                            return;
                        }
                        jsonManager.saveDays(
                                dayManager.days
                        );
                    }
                }
        );

        restInput.addTextChangedListener(
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
                        exercise.restSeconds =
                                getTimeInSeconds(
                                        s.toString()
                                );
                    }

                    @Override
                    public void afterTextChanged(
                            Editable s
                    ) {
                        jsonManager.saveDays(
                                dayManager.days
                        );
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
        jsonManager.saveDays(dayManager.days);
    }

    private void updateTimerDisplay(
            EditText timerView,
            String text
    ) {

        isUpdatingTimerView = true;
        timerView.setText(text);
        isUpdatingTimerView = false;
    }

    private void startRepTimer() {
        startRepTimer(0);
    }

    private void startRepTimer(
            long startingMillis
    ) {
        if (!isRunning) {
            return;
        }
        if (currentExercise >= exercises.size()) {
            return;
        }
        Exercise exercise = exercises.get(currentExercise);
        EditText timerView = getCurrentTimerView();

        isBreakTimer = false;
        long durationMillis;

        if (startingMillis > 0) {
            durationMillis = startingMillis;
        }
        else {
            durationMillis = exercise.timerSeconds * 1000L;
        }

        if (durationMillis <= 0) {
            return;
        }
        remainingMillis = durationMillis;

        if (startingMillis <= 0) {
            updateTimerDisplay(
                    timerView,
                    String.format(
                            Locale.getDefault(),
                            "%02d:%02d",
                            exercise.timerSeconds / 60,
                            exercise.timerSeconds % 60
                    )
            );
        }
        if (countDownTimer != null) {
            countDownTimer.cancel();
            countDownTimer = null;
        }
        final int thisWorkoutRun = workoutRunId;
        countDownTimer =
                new CountDownTimer(
                        durationMillis,
                        1000
                ) {
                    @Override
                    public void onTick(
                            long millisUntilFinished
                    ) {
                        if (!isRunning) {
                            return;
                        }
                        if (thisWorkoutRun != workoutRunId) {
                            return;
                        }
                        remainingMillis = millisUntilFinished;
                        int seconds = (int) (millisUntilFinished / 1000);

                        updateTimerDisplay(
                                timerView,
                                String.format(
                                        Locale.getDefault(),
                                        "%02d:%02d",
                                        seconds / 60,
                                        seconds % 60
                                )
                        );
                    }
                    @Override
                    public void onFinish() {
                        if (!isRunning) {
                            return;
                        }
                        if (thisWorkoutRun != workoutRunId) {
                            return;
                        }
                        remainingMillis = 0;
                        beep();
                        if (currentRep < exercise.reps) {
                            currentRep++;
                            startRepTimer();
                        }
                        else {
                            finishSet();
                        }
                    }
                }.start();
    }
    private void finishSet() {
        if (!isRunning) {
            return;
        }
        if (currentExercise >= exercises.size()) {
            return;
        }

        Exercise exercise = exercises.get(currentExercise);

        if (currentSet < exercise.sets) {
            startBreakTimer();
        } else {
            finishExercise();
        }
    }
    private void startBreakTimer() {
        startBreakTimer(0);
    }
    private void startBreakTimer(long startingMillis) {
        if (!isRunning) {
            return;
        }

        if (currentExercise >= exercises.size()) {
            return;
        }
        Exercise exercise = exercises.get(currentExercise);
        EditText timerView = getCurrentTimerView();

        isBreakTimer = true;
        long durationMillis;

        if (startingMillis > 0) {
            durationMillis = startingMillis;
        }
        else {
            durationMillis = exercise.restSeconds * 1000L;
        }
        if (durationMillis <= 0) {
            currentSet++;
            currentRep = 1;

            startRepTimer();
            return;
        }
        remainingMillis = durationMillis;

        if (startingMillis <= 0) {
            updateTimerDisplay(
                    timerView,
                    String.format(
                            Locale.getDefault(),
                            "%02d:%02d",
                            exercise.restSeconds / 60,
                            exercise.restSeconds % 60
                    )
            );
        }
        if (countDownTimer != null) {
            countDownTimer.cancel();
            countDownTimer = null;
        }
        final int thisWorkoutRun = workoutRunId;

        countDownTimer =
                new CountDownTimer(durationMillis, 1000) {
                    @Override
                    public void onTick(
                            long millisUntilFinished
                    ) {
                        if (!isRunning) {
                            return;
                        }
                        if (thisWorkoutRun != workoutRunId) {
                            return;
                        }
                        remainingMillis = millisUntilFinished;
                        int seconds = (int) (millisUntilFinished / 1000);

                        updateTimerDisplay(
                                timerView,
                                String.format(
                                        Locale.getDefault(),
                                        "%02d:%02d",
                                        seconds / 60,
                                        seconds % 60
                                )
                        );
                    }
                    @Override
                    public void onFinish() {
                        if (!isRunning) {
                            return;
                        }
                        if (thisWorkoutRun != workoutRunId) {
                            return;
                        }
                        remainingMillis = 0;

                        beep();
                        currentSet++;
                        currentRep = 1;
                        startRepTimer();
                    }
                }.start();
    }
    private void finishExercise() {
        if (countDownTimer != null) {
            countDownTimer.cancel();
            countDownTimer = null;
        }
        remainingMillis = 0;

        if (currentExercise >= exerciseContainer.getChildCount()) {
            workoutFinished();
            return;
        }

        View finishedExerciseView = exerciseContainer.getChildAt(currentExercise);
        EditText finishedTimer = finishedExerciseView.findViewById(R.id.timerValue);

        updateTimerDisplay(finishedTimer, "DONE");
        setExerciseEditable(finishedExerciseView, false);
        currentExercise++;

        if (currentExercise < exercises.size()) {
            startCurrentExercise();
        }
        else {
            workoutFinished();
        }
    }
    private void startCurrentExercise() {
        currentSet = 1;
        currentRep = 1;
        remainingMillis = 0;
        startRepTimer();
    }
    private void pauseTimer() {
        if (countDownTimer != null) {
            countDownTimer.cancel();
            countDownTimer = null;
        }
        isRunning = false;
    }

    private void resumeWorkout() {
        if (exercises.isEmpty()) {
            return;
        }
        if (currentExercise >= exercises.size()) {
            return;
        }
        isRunning = true;
        if (remainingMillis > 0) {
            if (isBreakTimer) {
                startBreakTimer(remainingMillis);
            }
            else {
                startRepTimer(remainingMillis);
            }
        }
        else {
            startRepTimer();
        }
    }
    private void resetWorkout() {
        workoutRunId++;

        if (countDownTimer != null) {
            countDownTimer.cancel();
            countDownTimer = null;
        }

        isRunning = false;
        remainingMillis = 0;
        isBreakTimer = false;

        currentExercise = 0;
        currentSet = 1;
        currentRep = 1;

        for (int i = 0; i < exerciseContainer.getChildCount(); i++) {
            View exerciseView = exerciseContainer.getChildAt(i);

            EditText timerView = exerciseView.findViewById(R.id.timerValue);
            Exercise exercise = exercises.get(i);

            updateTimerDisplay(
                    timerView,
                    String.format(
                            Locale.getDefault(),
                            "%02d:%02d",
                            exercise.timerSeconds / 60,
                            exercise.timerSeconds % 60
                    )
            );

            setExerciseEditable(exerciseView, true);
        }
    }
    private void workoutFinished() {
        if (countDownTimer != null) {
            countDownTimer.cancel();
            countDownTimer = null;
        }
        remainingMillis = 0;
        isRunning = false;

        if (currentExercise > 0 &&
                currentExercise <=
                exerciseContainer.getChildCount()) {

            View finishedExerciseView =
                    exerciseContainer.getChildAt(
                            currentExercise - 1
                    );

            EditText timerView =
                    finishedExerciseView.findViewById(
                            R.id.timerValue
                    );

            updateTimerDisplay(timerView, "DONE");
            setExerciseEditable(finishedExerciseView, false);
        }
    }
    private void deleteExercise(
            LinearLayout exerciseContainer,
            View exerciseView,
            Exercise exercise
    ) {
        currentTask.exercises.remove(exercise);
        exerciseContainer.removeView(exerciseView);
        jsonManager.saveDays(dayManager.days);
    }
    private void loadExercises() {
        exercises = currentTask.exercises;
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
        workoutRunId++;

        if (countDownTimer != null) {
            countDownTimer.cancel();
            countDownTimer = null;
        }
        remainingMillis = 0;
        isBreakTimer = false;

        currentExercise = 0;
        currentSet = 1;
        currentRep = 1;

        isRunning = true;

        startCurrentExercise();
    }
    private int getTimeInSeconds(
            String time
    ) {
        try {
            String[] parts = time.split(":");

            int minutes = Integer.parseInt(parts[0]);
            int seconds = Integer.parseInt(parts[1]);

            return minutes * 60 + seconds;
        } catch (Exception e) {
            return 0;
        }
    }
    private EditText getCurrentTimerView() {
        View exerciseView = exerciseContainer.getChildAt(currentExercise);

        return exerciseView.findViewById(R.id.timerValue);
    }
    private void beep() {
        toneGenerator.startTone(ToneGenerator.TONE_PROP_BEEP, 200);
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
                        (view,
                         selectedYear,
                         selectedMonth,
                         selectedDay) -> {

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
        jsonManager.saveDays(dayManager.days);

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
        EditText exerciseName = exerciseView.findViewById(R.id.exerciseName);

        AutoCompleteTextView setInput = exerciseView.findViewById(R.id.setInput);

        AutoCompleteTextView repsInput = exerciseView.findViewById(R.id.repsInput);

        EditText timerValue = exerciseView.findViewById(R.id.timerValue);

        EditText restInput = exerciseView.findViewById(R.id.restInput);

        TextView deleteExercise = exerciseView.findViewById(R.id.deleteTask);

        exerciseName.setEnabled(editable);
        setInput.setEnabled(editable);
        repsInput.setEnabled(editable);
        timerValue.setEnabled(editable);
        restInput.setEnabled(editable);
        deleteExercise.setEnabled(editable);

        if (!editable) {
            deleteExercise.setAlpha(0.4f);
        }
        else {
            deleteExercise.setAlpha(1.0f);
        }
    }
    private void scrollToToday() {
        if (todayDayView == null) {
            return;
        }
        scrollView.post(() -> {
            scrollView.smoothScrollTo(0, todayDayView.getTop());
        });
    }
}