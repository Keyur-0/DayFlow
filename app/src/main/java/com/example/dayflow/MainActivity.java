package com.example.dayflow;

import android.media.AudioManager;
import android.media.ToneGenerator;
import android.os.Bundle;
import android.os.CountDownTimer;
import android.widget.AutoCompleteTextView;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageButton;

import androidx.activity.EdgeToEdge;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;

public class MainActivity extends AppCompatActivity {

    //Ui
    Button playButton;
    ImageButton resetButton;
    EditText timerValue;
    EditText restInput;
    AutoCompleteTextView setInput;
    AutoCompleteTextView repsInput;

    CountDownTimer countDownTimer;

    int currentSet = 1;
    int currentRep = 1;
    int repTimeSeconds;

//    final int countDownValue = 5;
    int breakValue;
//    final int countDownValue = 5;

    boolean isRunning = false;

    ToneGenerator toneGenerator;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        EdgeToEdge.enable(this);
        setContentView(R.layout.activity_main);
        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main), (v, insets) -> {
            Insets systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars());
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom);
            return insets;
        });

        playButton = findViewById(R.id.playButton);
        resetButton = findViewById(R.id.resetButton);
        restInput = findViewById(R.id.restInput);
        timerValue = findViewById(R.id.timerValue);
        setInput = findViewById(R.id.setInput);
        repsInput = findViewById(R.id.repsInput);

        toneGenerator = new ToneGenerator(AudioManager.STREAM_ALARM,100);

        resetButton.setOnClickListener(v -> {
            resetWorkout();
        });
        playButton.setOnClickListener(v ->{
            if(!isRunning){
                startWorkout();
            }
            else{
                pauseTimer();
            }
        });
    }
    private int getTimerSeconds() {
        String value = timerValue.getText().toString().trim();

        String[] parts = value.split(":");

        int minutes = Integer.parseInt(parts[0]);
        int seconds = Integer.parseInt(parts[1]);

        return (minutes * 60) + seconds;
    }
    private void resetWorkout() {

        if (countDownTimer != null) {
            countDownTimer.cancel();
        }

        currentSet = 1;
        currentRep = 1;
        isRunning = false;

        timerValue.setText(
                String.format("%02d:%02d",
                        repTimeSeconds / 60,
                        repTimeSeconds % 60)
        );
    }
    private void startWorkout(){
        int totalReps = getReps();
        int totalSets = getSets();
        repTimeSeconds = getTimerSeconds();
        breakValue = getBreakSeconds();

        if(totalSets <= 0 || totalReps <= 0 || repTimeSeconds <= 0){
            return;
        }
        currentRep=1;
        currentSet=1;
        isRunning= true;

        startRepTimer(totalReps);
    }

    private void startRepTimer(int totalReps){
//        int countDownValue = getTimerSeconds();
//        timerValue.setText(String.valueOf(countDownValue));
        timerValue.setText(
                String.format("%02d:%02d",
                        repTimeSeconds / 60,
                        repTimeSeconds % 60)
        );
        countDownTimer = new CountDownTimer(
                repTimeSeconds * 1000L,1000
        ) {
            @Override
            public void onTick(long millisUntilFinished) {
                int seconds =
                        (int) (millisUntilFinished / 1000);

                timerValue.setText(
                        String.format("00:%02d", seconds)
                );
            }
            @Override
            public void onFinish() {
                beep();

                currentRep++;

                if(currentRep<=totalReps){
                    startRepTimer(totalReps);
                }
                else{
                    finishSet(totalReps);
                }
            }
        }.start();
    }
    private void finishSet(int totalReps) {

        if (currentSet < getSets()) {
            startBreakTimer(totalReps);

        } else {
            workoutFinished();
        }
    }
    private void startBreakTimer(int totalReps) {

        timerValue.setText(
                String.format("%02d:%02d",
                        breakValue / 60,
                        breakValue % 60)
        );

        countDownTimer = new CountDownTimer(
                breakValue * 1000L,
                1000
        ) {

            @Override
            public void onTick(long millisUntilFinished) {

                int seconds =
                        (int) (millisUntilFinished / 1000);

//                timerValue.setText(
//                        String.format("00:%02d", seconds)
//                );
                timerValue.setText(
                        String.format("%02d:%02d",
                                seconds / 60,
                                seconds % 60)
                );
            }

            @Override
            public void onFinish() {

                beep();

                currentSet++;
                currentRep = 1;

                startRepTimer(totalReps);
            }

        }.start();
    }

    private void pauseTimer(){
        if(countDownTimer != null){
            countDownTimer.cancel();
        }
        isRunning = false;
    }

    private void workoutFinished(){
        timerValue.setText("DONE");
        isRunning = false;
    }

    private int getSets(){
        String value = setInput.getText().toString().trim();
        try {
            return Integer.parseInt(value);
        }catch (NumberFormatException e){
            return 1;
        }
    }

    private int getReps(){
        String value = repsInput.getText().toString().trim();
        try {
            return Integer.parseInt(value);
        }catch (NumberFormatException e){
            return 1;
        }
    }
    private int getBreakSeconds() {
        String value = restInput.getText().toString().trim();

        String[] parts = value.split(":");

        int minutes = Integer.parseInt(parts[0]);
        int seconds = Integer.parseInt(parts[1]);

        return (minutes * 60) + seconds;
    }
    private void beep(){
        toneGenerator.startTone(
                ToneGenerator.TONE_PROP_BEEP,200
        );
    }

    @Override
    protected void onDestroy(){
        if (countDownTimer != null){
            countDownTimer.cancel();
        }
        if (toneGenerator !=null){
            toneGenerator.release();
        }
        super.onDestroy();
    }
}