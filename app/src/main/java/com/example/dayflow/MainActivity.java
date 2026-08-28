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

import androidx.activity.EdgeToEdge;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;

import java.util.ArrayList;
import java.util.List;

public class MainActivity extends AppCompatActivity {

    Button playButton;
    ImageButton resetButton;
    Button addExercise;
    LinearLayout exerciseContainer;
    CountDownTimer countDownTimer;
    List<Exercise> exercises = new ArrayList<>();

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
        exerciseContainer = findViewById(R.id.exerciseContainer);
        addExercise = findViewById(R.id.addExercise);

        addNewExercise();

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

        addExercise.setOnClickListener(v -> {
            addNewExercise();
        });
    }
    private void addNewExercise() {
        LayoutInflater inflater = LayoutInflater.from(this);

        View exerciseView =
                inflater.inflate(
                        R.layout.exercise_item,
                        exerciseContainer,
                        false
                );

        exerciseContainer.addView(exerciseView);
    }

    private void loadExercises() {
        exercises.clear();
        for (int i = 0; i < exerciseContainer.getChildCount(); i++) {
            View exerciseView = exerciseContainer.getChildAt(i);

            EditText nameInput =
                    exerciseView.findViewById(
                            R.id.exerciseName
                    );

            AutoCompleteTextView setsInput =
                    exerciseView.findViewById(
                            R.id.setInput
                    );

            AutoCompleteTextView repsInput =
                    exerciseView.findViewById(
                            R.id.repsInput
                    );

            EditText timerInput =
                    exerciseView.findViewById(
                            R.id.timerValue
                    );

            EditText restInput =
                    exerciseView.findViewById(
                            R.id.restInput
                    );

            String name = nameInput.getText().toString().trim();

            int sets = parseNumber(setsInput.getText().toString());
            int reps = parseNumber(repsInput.getText().toString());
            int timer = parseTime(timerInput.getText().toString());
            int rest = parseTime(restInput.getText().toString());

            Exercise exercise =
                    new Exercise(
                            name,
                            sets,
                            reps,
                            timer,
                            rest
                    );
            exercises.add(exercise);
        }
    }

    private int parseNumber(String value) {
        try {
            return Integer.parseInt(
                    value.trim()
            );
        } catch (NumberFormatException e) {
            return 1;
        }
    }

    private int parseTime(String value) {
        try {
            String[] parts = value.trim().split(":");
            int minutes = Integer.parseInt(parts[0]);
            int seconds = Integer.parseInt(parts[1]);
            return (minutes * 60) + seconds;

        } catch (Exception e) {
            return 0;
        }
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
                        currentRep++;

                        if (currentRep <= exercise.reps) {

                            startRepTimer();
                        }
                        else {
                            finishSet();
                        }
                    }

                }.start();
    }

    private void finishSet() {
        Exercise exercise = exercises.get(currentExercise);

        if (currentSet < exercise.sets) {
            startBreakTimer();
        }
        else {
            finishExercise();
        }
    }
    private void startBreakTimer() {

        Exercise exercise = exercises.get(currentExercise);

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
        View finishedExerciseView = exerciseContainer.getChildAt(currentExercise);

        EditText finishedTimer =
                finishedExerciseView.findViewById(
                        R.id.timerValue
                );
        finishedTimer.setText("DONE");

        currentExercise++;
        if (currentExercise < exercises.size()) {
            startCurrentExercise();
        }
        else {

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

        if (exerciseContainer.getChildCount() > 0) {
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

        if (exerciseContainer.getChildCount() > 0) {
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