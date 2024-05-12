package com.example.toedy_project;

import android.content.Intent;
import android.os.Bundle;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;

import java.util.Comparator;
import java.util.Locale;
import java.util.Objects;

public class ScoreActivity extends AppCompatActivity {
    public static final double CORRECT_ANSWER_SCORE = 10;
    public static final double CORRECT_ANSWER_HINT_SCORE = 5;
    public static final double LEFT_TIME_SCORE_BONUS = 1;

    GlobalStates states;

    boolean[] hintUsed;
    int timeLeft;
    boolean postScoreAttempted;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_score);

        if (savedInstanceState != null) {
            hintUsed = savedInstanceState.getBooleanArray("hint_used");
            timeLeft = savedInstanceState.getInt("time_left");
            postScoreAttempted = savedInstanceState.getBoolean("post_score");
        }
        Intent intent = getIntent();
        if (intent.hasExtra("hint_used"))
            hintUsed = intent.getBooleanArrayExtra("hint_used");
        if (intent.hasExtra("time_left"))
            timeLeft = intent.getIntExtra("time_left", 0);

        findViewById(R.id.score_button_start).setOnClickListener(v -> {
            Utils.startQuiz(this);
            finish();
        });
        findViewById(R.id.score_button_back).setOnClickListener(v -> finish());

        states = (GlobalStates) getApplication();
        assert states.currentQuestions != null;
        assert states.currentAnswers != null;

        double score = 0;
        int correctAnswers = 0;
        for (int i = 0; i < states.currentQuestions.size(); i++) {
            if (!Objects.equals(states.currentQuestions.get(i).answer, states.currentAnswers.get(i)))
                continue;
            correctAnswers++;
            score += !hintUsed[i] ? CORRECT_ANSWER_SCORE : CORRECT_ANSWER_HINT_SCORE;
        }

        score *= 1 + (double) timeLeft / QuizActivity.QUIZ_LENGTH_SECONDS * LEFT_TIME_SCORE_BONUS;

        int hints = 0;
        for (boolean hint : hintUsed)
            if (hint)
                hints++;

        ScoreObj.Score scoreStore = new ScoreObj.Score();
        scoreStore.userId = states.userId;
        scoreStore.name = states.userName;
        scoreStore.score = score;
        scoreStore.correct_answers = correctAnswers;
        scoreStore.hint_used = hints;
        scoreStore.time_left = timeLeft;

        TextView scoreText = findViewById(R.id.score_text_score);
        scoreText.setText(String.format(Locale.ENGLISH, "%.2f", scoreStore.score));
        TextView correctText = findViewById(R.id.score_text_correct);
        correctText.setText(String.valueOf(scoreStore.correct_answers));
        TextView hintText = findViewById(R.id.score_text_hint);
        hintText.setText(String.valueOf(scoreStore.hint_used));
        TextView timeText = findViewById(R.id.score_text_time);
        int seconds = scoreStore.time_left % 60;
        int minutes = scoreStore.time_left / 60;
        timeText.setText(String.format(Locale.ENGLISH, "%02d:%02d", minutes, seconds));

        if (states.userId == null || states.userId.isEmpty()) {
            Toast.makeText(this, "Login to post your score", Toast.LENGTH_SHORT).show();
        }

        if (states.scores == null || postScoreAttempted)
            return;

        for (ScoreObj.Score item : states.scores.scores) {
            if (!Objects.equals(item.userId, states.userId))
                continue;
            TextView maxScoreText = findViewById(R.id.score_text_max_score);
            maxScoreText.setText(String.format(Locale.ENGLISH, "%.2f", item.score));
            TextView maxCorrectText = findViewById(R.id.score_text_max_correct);
            maxCorrectText.setText(String.valueOf(item.correct_answers));
            TextView maxHintText = findViewById(R.id.score_text_max_hint);
            maxHintText.setText(String.valueOf(item.hint_used));
            TextView maxTimeText = findViewById(R.id.score_text_max_time);
            int maxSeconds = scoreStore.time_left % 60;
            int maxMinutes = scoreStore.time_left / 60;
            maxTimeText.setText(String.format(Locale.ENGLISH, "%02d:%02d", maxMinutes, maxSeconds));
            break;
        }

        boolean scoreInserted = false;
        for (int i = 0; i < states.scores.scores.size(); i++) {
            if (!Objects.equals(states.scores.scores.get(i).userId, states.userId))
                continue;
            if (states.scores.scores.get(i).score < scoreStore.score)
                states.scores.scores.set(i, scoreStore);
            scoreInserted = true;
        }
        if (!scoreInserted)
            states.scores.scores.add(scoreStore);

        states.scores.scores.sort(Comparator.comparingDouble(o -> -o.score));
        Fetches.putScores(this, getString(R.string.jsonbin_access_key), states.scores,
                (o, s) -> Toast.makeText(this, "Score posted successfully", Toast.LENGTH_SHORT).show(),
                e -> Toast.makeText(this, "Posting score failed", Toast.LENGTH_SHORT).show());

        postScoreAttempted = true;
    }

    @Override
    protected void onSaveInstanceState(@NonNull Bundle outState) {
        outState.putBooleanArray("hint_used", hintUsed);
        outState.putInt("time_left", timeLeft);
        outState.putBoolean("post_score", postScoreAttempted);
        super.onSaveInstanceState(outState);
    }
}