package com.example.toedy_project;

import android.content.DialogInterface;
import android.content.Intent;
import android.graphics.Typeface;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.DividerItemDecoration;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.google.gson.Gson;

import java.io.FileNotFoundException;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.Locale;
import java.util.Objects;
import java.util.stream.Collectors;

public class MainActivity extends AppCompatActivity {
    GlobalStates states;
    Utils.LoopHandle scoreLoop;

    Button startButton;
    ProgressBar startLoading;
    ProgressBar startProgress;
    RecyclerView scoreList;
    ScoreAdapter scoreAdapter;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        states = (GlobalStates) getApplicationContext();
        if (states.userId == null) {
            Intent intent = new Intent(this, LoginActivity.class);
            startActivity(intent);
            finish();
            return;
        }

        ImageView loginButton = findViewById(R.id.main_img_logout);
        loginButton.setOnClickListener(v -> logout());

        TextView textView = findViewById(R.id.main_text_user);
        textView.setText(states.userName);

        startButton = findViewById(R.id.main_button_start);
        startButton.setVisibility(View.GONE);
        startButton.setOnClickListener(view -> {
            Intent intent = new Intent(this, QuizActivity.class);
            startActivity(intent);
            finish();

//            if (states.scores == null)
//                return;
//            ScoreObj.Score score = new ScoreObj.Score();
//            score.username = states.userName;
//            score.score = new Random().nextDouble();
//            states.scores.scores.add(score);
//            states.scores.scores.sort(Comparator.comparingDouble(o -> -o.score));
//            Fetches.putScores(this, getString(R.string.jsonbin_access_key), states.scores,
//                    null, null);
        });

        startLoading = findViewById(R.id.main_loading_questions);
        startProgress = findViewById(R.id.main_progress_questions);
        startProgress.setVisibility(View.GONE);

        scoreList = findViewById(R.id.main_list_score);
        scoreAdapter = new ScoreAdapter();
        scoreList.setAdapter(scoreAdapter);
        scoreList.setLayoutManager(new LinearLayoutManager(this));
        DividerItemDecoration dividerItemDecoration = new DividerItemDecoration(
                scoreList.getContext(), DividerItemDecoration.HORIZONTAL);
        scoreList.addItemDecoration(dividerItemDecoration);

        if (states.questions == null) {
            try {
                String oldQuestionStr = Utils.readAllText(this, "questions.json");
                Gson gson = new Gson();
                states.questions = gson.fromJson(oldQuestionStr, QuestionObj.class);
            } catch (FileNotFoundException ignored) {
            } catch (IOException e) {
                Toast.makeText(this, "Load questions from local failed", Toast.LENGTH_SHORT).show();
            }
        }

        Fetches.questions(this, getString(R.string.jsonbin_access_key),
                (questions, text) -> {
                    if (states.questions == null) {
                        useNewQuestions(questions);
                        return;
                    }

                    if (states.questions.version >= questions.version) {
                        useOldQuestions();
                        return;
                    }

                    AlertDialog dialog = new AlertDialog.Builder(this).create();
                    dialog.setTitle("New questions found!");
                    dialog.setMessage("New questions found, do you want to download them?");
                    dialog.setIcon(R.drawable.ic_user);
                    dialog.setCancelable(true);
                    dialog.setButton(DialogInterface.BUTTON_POSITIVE, "Yes",
                            (v, w) -> useNewQuestions(questions));
                    dialog.setButton(DialogInterface.BUTTON_NEGATIVE, "No",
                            (v, w) -> useOldQuestions());
                    dialog.setOnCancelListener(v -> useOldQuestions());
                    dialog.show();
                },
                e -> {
                    Toast.makeText(this, "Failed to fetch questions", Toast.LENGTH_SHORT).show();
                    useOldQuestions();
                });
    }

    private void useOldQuestions() {
        startButton.setVisibility(View.VISIBLE);
        startLoading.setVisibility(View.GONE);
    }

    private void useNewQuestions(QuestionObj questions) {
        startProgress.setMax(questions.questions.size() * 2);
        startProgress.setVisibility(View.VISIBLE);
        startLoading.setVisibility(View.GONE);

        DownloadQuestionsTask task = new DownloadQuestionsTask(this);
        task.execute(questions);
    }

    @Override
    protected void onResume() {
        super.onResume();
        Runnable runnable = () -> Fetches.scores(this, getString(R.string.jsonbin_access_key),
                (scores, text) -> {
                    states.scores = scores;
                    scoreAdapter.setScores(
                            scores.scores.stream().limit(10).collect(Collectors.toList()));
                    for (ScoreObj.Score score : scores.scores) {
                        if (!Objects.equals(score.username, states.userName))
                            continue;
                        TextView scoreText = findViewById(R.id.main_text_score);
                        scoreText.setText(String.format(Locale.ENGLISH, "%.2f", score.score));
                        TextView correctText = findViewById(R.id.main_text_correct);
                        correctText.setText(String.valueOf(score.correct_answers));
                        TextView hintText = findViewById(R.id.main_text_hint);
                        hintText.setText(String.valueOf(score.hint_used));
                        TextView timeText = findViewById(R.id.main_text_time);
                        timeText.setText(String.format(Locale.ENGLISH, "%.2f", score.time_left));
                        break;
                    }
                }, null);
        runnable.run();
        scoreLoop = new Utils.LoopHandle(runnable, 10_000);
    }

    @Override
    protected void onPause() {
        if (scoreLoop != null)
            scoreLoop.dispose();
        super.onPause();
    }

    private void logout() {
        Intent intent = new Intent(this, LoginActivity.class);
        intent.putExtra(LoginActivity.EXTRA_CLEAR_CREDENTIALS, true);
        startActivity(intent);
        finish();
    }

    private class ScoreAdapter extends RecyclerView.Adapter<RecyclerView.ViewHolder> {
        private List<ScoreObj.Score> scores = new ArrayList<>();

        @NonNull
        @Override
        public RecyclerView.ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
            View view = LayoutInflater.from(parent.getContext())
                    .inflate(R.layout.main_list_scores, parent, false);
            return new RecyclerView.ViewHolder(view) {
            };
        }

        @Override
        public void onBindViewHolder(@NonNull RecyclerView.ViewHolder holder, int position) {
            ScoreObj.Score score = scores.get(position);
            View root = holder.itemView;
            TextView rankText = root.findViewById(R.id.scores_rank);
            TextView nameText = root.findViewById(R.id.scores_name);
            TextView scoreText = root.findViewById(R.id.scores_score);

            rankText.setText(String.valueOf(position + 1));
            rankText.setTypeface(null, Typeface.BOLD_ITALIC);
            if (score.username.length() < 12)
                nameText.setText(score.username);
            else
                nameText.setText(score.username.substring(0, 7)
                        + "..." + score.username.substring(score.username.length() - 4));
            scoreText.setText(String.format(Locale.ENGLISH, "%.2f", score.score));

            if (Objects.equals(score.username, states.userName)) {
                nameText.setTypeface(null, Typeface.BOLD);
                scoreText.setTypeface(null, Typeface.BOLD);
                return;
            }
            nameText.setTypeface(null, Typeface.NORMAL);
            scoreText.setTypeface(null, Typeface.NORMAL);
        }

        @Override
        public int getItemCount() {
            return scores.size();
        }

        public void setScores(List<ScoreObj.Score> scores) {
            this.scores = scores;
            notifyItemRangeChanged(0, scores.size());
            notifyDataSetChanged();
        }
    }
}