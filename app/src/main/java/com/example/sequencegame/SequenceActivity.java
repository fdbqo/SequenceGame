package com.example.sequencegame;

import android.content.Context;
import android.content.Intent;
import android.hardware.Sensor;
import android.hardware.SensorEvent;
import android.hardware.SensorEventListener;
import android.hardware.SensorManager;
import android.os.Bundle;
import android.os.Handler;
import android.view.Display;
import android.view.Surface;
import android.view.WindowManager;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;

import com.google.android.material.button.MaterialButton;

import java.util.ArrayList;
import java.util.List;
import java.util.Random;

/**
 * SequenceActivity is the main game screen where the sequence game is played.
 * It handles sequence generation, display, user input using device tilt, and game logic.
 */
public class SequenceActivity extends AppCompatActivity implements SensorEventListener {

    private List<Integer> sequence;
    private MaterialButton[] buttons;
    private TextView debugTextView;
    private TextView scoreTextView;
    private int sequenceLength = 4;
    private int currentScore = 0;
    private int playerStep = 0;
    private boolean isDisplaying = true;
    private boolean canAcceptInput = false;

    private SensorManager sensorManager;
    private Sensor accelerometer;
    private Display display;

    private static final int LEFT = 0;
    private static final int TOP = 1;
    private static final int RIGHT = 2;
    private static final int BOTTOM = 3;

    private static final long SEQUENCE_DISPLAY_DELAY = 1000; // 1 second between each button flash
    private static final long PLAYER_START_DELAY = 3000; // 3 seconds before player can start
    private static final long INPUT_DELAY = 500; // 0.5 seconds between inputs
    private static final long DIRECTION_CHANGE_DELAY = 1000; // 1 second to allow direction change

    private long lastInputTime = 0;
    private float[] initialPosition;
    private boolean isCalibrated = false;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_sequence);

        // Initialise UI components
        buttons = new MaterialButton[]{
                findViewById(R.id.leftButton),
                findViewById(R.id.topButton),
                findViewById(R.id.rightButton),
                findViewById(R.id.bottomButton)
        };

        debugTextView = findViewById(R.id.debugTextView);
        scoreTextView = findViewById(R.id.scoreTextView);

        // Set up sensor manager and accelerometer
        sensorManager = (SensorManager) getSystemService(Context.SENSOR_SERVICE);
        accelerometer = sensorManager.getDefaultSensor(Sensor.TYPE_ACCELEROMETER);

        WindowManager windowManager = (WindowManager) getSystemService(WINDOW_SERVICE);
        display = windowManager.getDefaultDisplay();

        // Get initial sequence length and score from intent
        sequenceLength = getIntent().getIntExtra("sequenceLength", 4);
        currentScore = getIntent().getIntExtra("score", 0);
        updateScoreDisplay();

        generateSequence();
        calibrateInitialPosition();
    }

    @Override
    protected void onResume() {
        super.onResume();
        sensorManager.registerListener(this, accelerometer, SensorManager.SENSOR_DELAY_GAME);
    }

    @Override
    protected void onPause() {
        super.onPause();
        sensorManager.unregisterListener(this);
    }

    /**
     * Calibrates the initial position of the device for accurate tilt detection.
     */
    private void calibrateInitialPosition() {
        Toast.makeText(this, "Hold the device in your preferred position", Toast.LENGTH_LONG).show();
        new Handler().postDelayed(new Runnable() {
            @Override
            public void run() {
                isCalibrated = true;
                Toast.makeText(SequenceActivity.this, "Calibration complete. Get ready!", Toast.LENGTH_SHORT).show();
                new Handler().postDelayed(new Runnable() {
                    @Override
                    public void run() {
                        displaySequence();
                    }
                }, 2000);
            }
        }, 3000);
    }

    /**
     * Generates a random sequence of colors for the game.
     */
    private void generateSequence() {
        sequence = new ArrayList<>();
        Random random = new Random();
        for (int i = 0; i < sequenceLength; i++) {
            sequence.add(random.nextInt(4));
        }
    }

    /**
     * Displays the generated sequence to the player.
     */
    private void displaySequence() {
        isDisplaying = true;
        canAcceptInput = false;
        for (int i = 0; i < sequence.size(); i++) {
            final int index = i;
            new Handler().postDelayed(new Runnable() {
                @Override
                public void run() {
                    flashButton(sequence.get(index));
                    if (index == sequence.size() - 1) {
                        new Handler().postDelayed(new Runnable() {
                            @Override
                            public void run() {
                                isDisplaying = false;
                                canAcceptInput = true;
                                Toast.makeText(SequenceActivity.this, "Your turn! Tilt the device.", Toast.LENGTH_LONG).show();
                            }
                        }, PLAYER_START_DELAY);
                    }
                }
            }, SEQUENCE_DISPLAY_DELAY * i);
        }
    }

    /**
     * Flashes a button to indicate it as part of the sequence.
     * @param buttonIndex The index of the button to flash.
     */
    private void flashButton(int buttonIndex) {
        buttons[buttonIndex].setAlpha(0.3f);
        buttons[buttonIndex].animate()
                .scaleX(1.2f)
                .scaleY(1.2f)
                .setDuration(250)
                .withEndAction(() -> {
                    buttons[buttonIndex].animate()
                            .scaleX(1f)
                            .scaleY(1f)
                            .alpha(1f)
                            .setDuration(250)
                            .start();
                })
                .start();
    }

    @Override
    public void onSensorChanged(SensorEvent event) {
        if (event.sensor.getType() == Sensor.TYPE_ACCELEROMETER) {
            if (!isCalibrated) {
                initialPosition = event.values.clone();
                return;
            }

            if (canAcceptInput) {
                long currentTime = System.currentTimeMillis();
                if (currentTime - lastInputTime < INPUT_DELAY) {
                    return; // Ignore input if it's too soon after the last input
                }

                float x = event.values[0] - initialPosition[0];
                float y = event.values[1] - initialPosition[1];
                float z = event.values[2] - initialPosition[2];

                int direction = getDirection(x, y, z);
                if (direction != -1) {
                    lastInputTime = currentTime;
                    checkInput(direction);
                }

                // Debug information
                String debugInfo = String.format("X: %.2f, Y: %.2f, Z: %.2f, Dir: %s", x, y, z, directionToString(direction));
                debugTextView.setText(debugInfo);
            }
        }
    }

    @Override
    public void onAccuracyChanged(Sensor sensor, int accuracy) {
        // Not used in this example
    }

    /**
     * Determines the tilt direction based on accelerometer values.
     * @param x X-axis acceleration
     * @param y Y-axis acceleration
     * @param z Z-axis acceleration
     * @return The detected direction (LEFT, TOP, RIGHT, BOTTOM) or -1 if no direction is detected
     */
    private int getDirection(float x, float y, float z) {
        float threshold = 2.5f; // Reduced threshold for more sensitivity

        int rotation = display.getRotation();
        switch (rotation) {
            case Surface.ROTATION_0:
                if (Math.abs(x) > Math.abs(y)) {
                    return x > threshold ? LEFT : x < -threshold ? RIGHT : -1;
                } else {
                    return y > threshold ? TOP : y < -threshold ? BOTTOM : -1;
                }
            case Surface.ROTATION_90:
                if (Math.abs(y) > Math.abs(x)) {
                    return y > threshold ? RIGHT : y < -threshold ? LEFT : -1;
                } else {
                    return x < -threshold ? TOP : x > threshold ? BOTTOM : -1;
                }
            case Surface.ROTATION_180:
                if (Math.abs(x) > Math.abs(y)) {
                    return x > threshold ? RIGHT : x < -threshold ? LEFT : -1;
                } else {
                    return y < -threshold ? TOP : y > threshold ? BOTTOM : -1;
                }
            case Surface.ROTATION_270:
                if (Math.abs(y) > Math.abs(x)) {
                    return y < -threshold ? RIGHT : y > threshold ? LEFT : -1;
                } else {
                    return x > threshold ? TOP : x < -threshold ? BOTTOM : -1;
                }
            default:
                return -1;
        }
    }

    private String directionToString(int direction) {
        switch (direction) {
            case LEFT: return "LEFT";
            case TOP: return "TOP";
            case RIGHT: return "RIGHT";
            case BOTTOM: return "BOTTOM";
            default: return "NONE";
        }
    }

    private String directionToColor(int direction) {
        switch (direction) {
            case LEFT: return "Red";
            case TOP: return "Blue";
            case RIGHT: return "Green";
            case BOTTOM: return "Orange";
            default: return "Unknown";
        }
    }

    /**
     * Checks if the player's input matches the current step in the sequence.
     * @param direction The direction of the player's tilt
     */
    private void checkInput(int direction) {
//        String directionName = directionToString(direction);
//        String color = directionToColor(direction);
//        Toast.makeText(this, "Tilted: " + directionName + " (" + color + ")", Toast.LENGTH_SHORT).show();

        if (direction == sequence.get(playerStep)) {
            flashButton(direction);
            currentScore++; // Increment score for each correct guess
            updateScoreDisplay();
            playerStep++;
            if (playerStep == sequence.size()) {
                new Handler().postDelayed(new Runnable() {
                    @Override
                    public void run() {
                        Toast.makeText(SequenceActivity.this, "Correct! Next sequence.", Toast.LENGTH_SHORT).show();
                        nextRound();
                    }
                }, DIRECTION_CHANGE_DELAY);
            }
        } else {
            new Handler().postDelayed(new Runnable() {
                @Override
                public void run() {
                    gameOver();
                }
            }, DIRECTION_CHANGE_DELAY);
        }

        // Disable input temporarily to allow for direction change
        canAcceptInput = false;
        new Handler().postDelayed(new Runnable() {
            @Override
            public void run() {
                canAcceptInput = true;
            }
        }, DIRECTION_CHANGE_DELAY);
    }

    /**
     * Updates the score display on the screen.
     */
    private void updateScoreDisplay() {
        scoreTextView.setText("Score: " + currentScore);
    }

    /**
     * Starts the next round by increasing sequence length and generating a new sequence.
     */
    private void nextRound() {
        sequenceLength += 2;
        playerStep = 0;
        generateSequence();
        displaySequence();
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

