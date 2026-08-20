package com.example.dayflow;

import android.media.AudioManager;
import android.media.ToneGenerator;
import android.os.Bundle;
import android.os.CountDownTimer;
import android.widget.AutoCompleteTextView;
import android.widget.Button;
import android.widget.EditText;

import androidx.activity.EdgeToEdge;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;

public class MainActivity extends AppCompatActivity {

    //Ui
    Button playButton;
    EditText timerValue;
    AutoCompleteTextView setInput;
    AutoCompleteTextView repsInput;

    CountDownTimer countDownTimer;

    int currentSet = 1;
    int currentRep = 1;

    final int countDownValue = 5;
    final int breakValue = 120;

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
        timerValue = findViewById(R.id.timerValue);
        setInput = findViewById(R.id.setInput);
        repsInput = findViewById(R.id.repsInput);

        toneGenerator = new ToneGenerator(AudioManager.STREAM_ALARM,100);
    }
}