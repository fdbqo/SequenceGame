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

/**
 * PlayActivity is responsible for the gameplay phase of the Sequence Game.
 * It handles user input via device tilt and checks if the input matches the displayed sequence.
 */
public class PlayActivity extends AppCompatActivity implements SensorEventListener {

    private SensorManager sensorManager;
    private Sensor accelerometer;
    private List<Integer> sequence;
    private int currentStep = 0;
    private int currentScore;
    private long lastUpdateTime = 0;
    private static final long SHAKE_THRESHOLD = 600; // Time threshold for registering a shake

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_play);

        // Initialise the sensor manager and accelerometer
        sensorManager = (SensorManager) getSystemService(Context.SENSOR_SERVICE);
        accelerometer = sensorManager.getDefaultSensor(Sensor.TYPE_ACCELEROMETER);

        // Retrieve the sequence and current score from the intent
        sequence = getIntent().getIntegerArrayListExtra("sequence");
        currentScore = getIntent().getIntExtra("score", 0);

        // Display instructions to the player
        Toast.makeText(this, "Tilt to match the sequence!", Toast.LENGTH_LONG).show();
    }

    @Override
    protected void onResume() {
        super.onResume();
        // Register the accelerometer listener when the activity resumes
        sensorManager.registerListener(this, accelerometer, SensorManager.SENSOR_DELAY_NORMAL);
    }

    @Override
    protected void onPause() {
        super.onPause();
        // Unregister the accelerometer listener when the activity is paused
        sensorManager.unregisterListener(this);
    }

    @Override
    public void onSensorChanged(SensorEvent event) {
        if (event.sensor.getType() == Sensor.TYPE_ACCELEROMETER) {
            long currentTime = System.currentTimeMillis();
            // Check if enough time has passed since the last update to avoid overly sensitive input
            if ((currentTime - lastUpdateTime) > SHAKE_THRESHOLD) {
                lastUpdateTime = currentTime;
                float x = event.values[0];
                float y = event.values[1];

                int direction = getDirection(x, y);
                if (direction != -1) {
                    checkInput(direction);
                }
            }
        }
    }

    @Override
    public void onAccuracyChanged(Sensor sensor, int accuracy) {
        // This method is required by the SensorEventListener interface,
        // but we don't need to use it for this game.
    }

    /**
     * Determines the tilt direction based on accelerometer values.
     * @param x X-axis acceleration
     * @param y Y-axis acceleration
     * @return The detected direction (0: Left, 1: Right, 2: Up, 3: Down) or -1 if no direction is detected
     */
    private int getDirection(float x, float y) {
        if (Math.abs(x) > Math.abs(y)) {
            return x < -2 ? 0 : x > 2 ? 1 : -1; // 0: Left (Red), 1: Right (Blue)
        } else {
            return y < -2 ? 2 : y > 2 ? 3 : -1; // 2: Up (Green), 3: Down (Yellow)
        }
    }

    /**
     * Checks if the player's input matches the current step in the sequence.
     * @param input The direction of the player's tilt
     */
    private void checkInput(int input) {
        if (input == sequence.get(currentStep)) {
            currentStep++;
            if (currentStep == sequence.size()) {
                // Player has completed the sequence correctly
                currentScore += sequence.size();
                goToNextSequence();
            }
        } else {
            // Player made a mistake, end the game
            gameOver();
        }
    }

    /**
     * Transitions to the next sequence when the player completes the current one correctly.
     */
    private void goToNextSequence() {
        Intent intent = new Intent(this, SequenceActivity.class);
        intent.putExtra("sequenceLength", sequence.size() + 2); // Increase sequence length
        intent.putExtra("score", currentScore);
        startActivity(intent);
        finish();
    }

    /**
     * Ends the game and transitions to the GameOverActivity.
     */
    private void gameOver() {
        Intent intent = new Intent(this, GameOverActivity.class);
        intent.putExtra("score", currentScore);
        startActivity(intent);
        finish();
    }
}

