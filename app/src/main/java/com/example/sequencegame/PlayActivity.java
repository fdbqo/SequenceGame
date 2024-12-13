package com.example.sequencegame;

import android.content.Context;
import android.content.Intent;
import android.hardware.Sensor;
import android.hardware.SensorEvent;
import android.hardware.SensorEventListener;
import android.hardware.SensorManager;
import android.os.Bundle;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;

import java.util.ArrayList;
import java.util.List;

public class PlayActivity extends AppCompatActivity implements SensorEventListener {

    private SensorManager sensorManager;
    private Sensor accelerometer;
    private List<Integer> sequence;
    private int currentStep = 0;
    private int currentScore;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_play);

        sensorManager = (SensorManager) getSystemService(Context.SENSOR_SERVICE);
        accelerometer = sensorManager.getDefaultSensor(Sensor.TYPE_ACCELEROMETER);

        sequence = getIntent().getIntegerArrayListExtra("sequence");
        currentScore = getIntent().getIntExtra("score", 0);
    }

    @Override
    protected void onResume() {
        super.onResume();
        sensorManager.registerListener(this, accelerometer, SensorManager.SENSOR_DELAY_NORMAL);
    }

    @Override
    protected void onPause() {
        super.onPause();
        sensorManager.unregisterListener(this);
    }

    @Override
    public void onSensorChanged(SensorEvent event) {
        if (event.sensor.getType() == Sensor.TYPE_ACCELEROMETER) {
            float x = event.values[0];
            float y = event.values[1];

            int direction = getDirection(x, y);
            if (direction != -1) {
                checkInput(direction);
            }
        }
    }

    @Override
    public void onAccuracyChanged(Sensor sensor, int accuracy) {
        // Not used in this example
    }

    private int getDirection(float x, float y) {
        if (Math.abs(x) > Math.abs(y)) {
            return x < -2 ? 0 : x > 2 ? 1 : -1; // 0: Left , 1: Right
        } else {
            return y < -2 ? 2 : y > 2 ? 3 : -1; // 2: Up , 3: Down
        }
    }

    private void checkInput(int input) {
        if (input == sequence.get(currentStep)) {
            currentStep++;
            if (currentStep == sequence.size()) {
                currentScore += sequence.size();
                goToNextSequence();
            }
        } else {
            gameOver();
        }
    }

    private void goToNextSequence() {
        Intent intent = new Intent(this, SequenceActivity.class);
        intent.putExtra("sequenceLength", sequence.size() + 2);
        intent.putExtra("score", currentScore);
        startActivity(intent);
        finish();
    }

    private void gameOver() {
        Intent intent = new Intent(this, GameOverActivity.class);
        intent.putExtra("score", currentScore);
        startActivity(intent);
        finish();
    }
}

