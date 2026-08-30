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

public class MainActivity extends AppCompatActivity {

    Button playButton;
    ImageButton resetButton;

    CountDownTimer countDownTimer;

    Task currentTask;
    List<Exercise> exercises = new ArrayList<>();

    LinearLayout dayContainer;

    // Used by the timer for the currently displayed workout
    LinearLayout exerciseContainer;

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

        displayDay(currentDay);

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
    }


    private void displayDay(Day day) {

        LayoutInflater inflater =
                LayoutInflater.from(this);

        View dayView = inflater.inflate(
                R.layout.day_item,
                dayContainer,
                false
        );

        TextView dayTitle =
                dayView.findViewById(R.id.dayTitle);

        TextView dayDate =
                dayView.findViewById(R.id.dayDate);

        LinearLayout dayExerciseContainer =
                dayView.findViewById(
                        R.id.dayExerciseContainer
                );

        Button addExercise =
                dayView.findViewById(
                        R.id.dayAddExercise
                );

        Task task = day.tasks.get(0);

        // Today's exercise container is used by the timer
        if (day.date.equals(DayUtils.getTodayDate())) {

            exerciseContainer = dayExerciseContainer;
        }

        // Display existing exercises
        for (Exercise exercise : task.exercises) {

            addNewExercise(
                    dayExerciseContainer,
                    exercise
            );
        }

        // Add new exercise
        addExercise.setOnClickListener(v -> {

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

        String todayDate =
                DayUtils.getTodayDate();

        if (day.date.equals(todayDate)) {

            dayTitle.setText("Today");

        } else {

            dayTitle.setText("");
        }

        dayDate.setText(
                DayUtils.formatDateForDisplay(
                        day.date
                )
        );

        dayContainer.addView(dayView);
    }


    private void addNewExercise(
            LinearLayout exerciseContainer,
            Exercise exercise
    ) {

        LayoutInflater inflater =
                LayoutInflater.from(this);

        View exerciseView = inflater.inflate(
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


        // Set initial values

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


        // Exercise name listener

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
                    }
                }
        );


        // Sets listener

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

                        } catch (
                                NumberFormatException e
                        ) {

                            exercise.sets = 0;
                        }
                    }

                    @Override
                    public void afterTextChanged(
                            Editable s
                    ) {
                    }
                }
        );


        // Reps listener

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

                        } catch (
                                NumberFormatException e
                        ) {

                            exercise.reps = 0;
                        }
                    }

                    @Override
                    public void afterTextChanged(
                            Editable s
                    ) {
                    }
                }
        );


        exerciseContainer.addView(
                exerciseView
        );
    }


    private void loadExercises() {

        exercises = currentTask.exercises;
    }


    private void startWorkout() {

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

        Exercise exercise =
                exercises.get(currentExercise);

        EditText timerView =
                getCurrentTimerView();

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

                        if (
                                currentRep
                                        <= exercise.reps
                        ) {

                            startRepTimer();

                        } else {

                            finishSet();
                        }
                    }

                }.start();
    }


    private void finishSet() {

        Exercise exercise =
                exercises.get(currentExercise);

        if (currentSet < exercise.sets) {

            startBreakTimer();

        } else {

            finishExercise();
        }
    }


    private void startBreakTimer() {

        Exercise exercise =
                exercises.get(currentExercise);

        EditText timerView =
                getCurrentTimerView();

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

        if (
                currentExercise
                        < exercises.size()
        ) {

            startCurrentExercise();

        } else {

            workoutFinished();
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
}