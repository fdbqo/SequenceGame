package com.example.sequencegame;

import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;

import com.example.sequencegame.DatabaseHandler;
import com.example.sequencegame.Score;
import com.google.android.material.button.MaterialButton;
import com.google.android.material.textfield.TextInputEditText;
import com.google.android.material.textfield.TextInputLayout;

/**
 * GameOverActivity is displayed when the player fails to match the sequence correctly.
 * It shows the final score and allows the player to submit their score if it's a high score.
 */
public class GameOverActivity extends AppCompatActivity {

    private int finalScore;
    private TextInputLayout nameInputLayout;
    private TextInputEditText nameInput;
    private MaterialButton submitScoreButton;
    private MaterialButton viewHighScoresButton;
    private DatabaseHandler dbHandler;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_game_over);

        dbHandler = new DatabaseHandler(this);
        finalScore = getIntent().getIntExtra("score", 0);

        TextView scoreTextView = findViewById(R.id.scoreTextView);
        scoreTextView.setText("Final Score: " + finalScore);

        nameInputLayout = findViewById(R.id.nameInputLayout);
        nameInput = findViewById(R.id.nameInput);
        submitScoreButton = findViewById(R.id.submitScoreButton);
        viewHighScoresButton = findViewById(R.id.viewHighScoresButton);

        submitScoreButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                submitScore();
            }
        });

        viewHighScoresButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                viewHighScores();
            }
        });

        if (dbHandler.isHighScore(finalScore)) {
            nameInputLayout.setVisibility(View.VISIBLE);
            submitScoreButton.setVisibility(View.VISIBLE);
        } else {
            nameInputLayout.setVisibility(View.GONE);
            submitScoreButton.setVisibility(View.GONE);
        }
    }

    /**
     * Submits the player's score to the database if it's a high score.
     */
    private void submitScore() {
        String playerName = nameInput.getText().toString().trim();
        if (!playerName.isEmpty()) {
            Score newScore = new Score(playerName, finalScore);
            dbHandler.addScore(newScore);
            Toast.makeText(this, "Score submitted successfully!", Toast.LENGTH_SHORT).show();
            viewHighScores();
        } else {
            Toast.makeText(this, "Please enter your name", Toast.LENGTH_SHORT).show();
        }
    }

    /**
     * Transitions to the HighScoreActivity to view high scores.
     */
    private void viewHighScores() {
        startActivity(new Intent(this, HighScoreActivity.class));
        finish();
    }
}

