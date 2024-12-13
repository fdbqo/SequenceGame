package com.example.sequencegame;

import android.content.Intent;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.example.sequencegame.DatabaseHandler;
import com.example.sequencegame.Score;
import com.google.android.material.button.MaterialButton;

import java.util.List;

/**
 * HighScoreActivity displays the top scores of the Sequence Game.
 * It retrieves scores from the database and shows them in a RecyclerView.
 */
public class HighScoreActivity extends AppCompatActivity {

    private RecyclerView highScoreRecyclerView;
    private MaterialButton backToMainButton;
    private DatabaseHandler dbHandler;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_high_score);


        // Initialise database handler and UI components
        dbHandler = new DatabaseHandler(this);
        highScoreRecyclerView = findViewById(R.id.highScoreRecyclerView);
        backToMainButton = findViewById(R.id.backToMainButton);

        // Set up RecyclerView
        highScoreRecyclerView.setLayoutManager(new LinearLayoutManager(this));
        displayHighScores();

        // Set up button to return to main menu
        backToMainButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                startActivity(new Intent(HighScoreActivity.this, MainActivity.class));
                finish();
            }
        });
    }

    /**
     * Retrieves top scores from the database and displays them in the RecyclerView.
     */
    private void displayHighScores() {
        List<Score> highScores = dbHandler.getTopScores(10);
        HighScoreAdapter adapter = new HighScoreAdapter(highScores);
        highScoreRecyclerView.setAdapter(adapter);
    }

    /**
     * Custom RecyclerView Adapter for displaying high scores.
     */
    private class HighScoreAdapter extends RecyclerView.Adapter<HighScoreAdapter.ViewHolder> {

        private List<Score> scores;

        HighScoreAdapter(List<Score> scores) {
            this.scores = scores;
        }

        @NonNull
        @Override
        public ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
            View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.item_high_score, parent, false);
            return new ViewHolder(view);
        }

        @Override
        public void onBindViewHolder(@NonNull ViewHolder holder, int position) {
            Score score = scores.get(position);
            holder.rankTextView.setText(String.valueOf(position + 1));
            holder.nameTextView.setText(score.getName());
            holder.scoreTextView.setText(String.valueOf(score.getScore()));
        }

        @Override
        public int getItemCount() {
            return scores.size();
        }

        /**
         * ViewHolder class for the HighScoreAdapter.
         * Holds references to the views for each item in the RecyclerView.
         */
        class ViewHolder extends RecyclerView.ViewHolder {
            TextView rankTextView;
            TextView nameTextView;
            TextView scoreTextView;

            ViewHolder(View itemView) {
                super(itemView);
                rankTextView = itemView.findViewById(R.id.rankTextView);
                nameTextView = itemView.findViewById(R.id.nameTextView);
                scoreTextView = itemView.findViewById(R.id.scoreTextView);
            }
        }
    }
}

