package com.example.toedy_project;

import android.content.Intent;
import android.graphics.Color;
import android.os.Bundle;
import android.os.CountDownTimer;
import android.widget.Button;
import android.widget.ProgressBar;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.fragment.app.Fragment;
import androidx.viewpager2.adapter.FragmentStateAdapter;
import androidx.viewpager2.widget.ViewPager2;

import java.io.File;
import java.util.Locale;

public class QuizActivity extends AppCompatActivity {
    public static final int QUIZ_LENGTH_SECONDS = 180;
    GlobalStates states;
    MyMediaPlayer myMediaPlayer;
    CountDownTimer timer;

    ViewPager2 pager;
    Button prevButton;
    Button nextButton;
    TextView timerText;
    ProgressBar timerProgress;

    private boolean[] hintUsed;
    private int leftTime;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_quiz);

        states = (GlobalStates) getApplication();
        assert states.currentQuestions != null;
        hintUsed = new boolean[states.currentQuestions.size()];

        myMediaPlayer = new MyMediaPlayer(this,
                findViewById(R.id.quiz_button_start),
                findViewById(R.id.quiz_button_pause),
                findViewById(R.id.quiz_button_stop),
                findViewById(R.id.quiz_text_sound),
                findViewById(R.id.quiz_seek_sound));

        prevButton = findViewById(R.id.quiz_button_prev);
        nextButton = findViewById(R.id.quiz_button_next);
        findViewById(R.id.quiz_button_submit).setOnClickListener(v -> gotoScores());

        timerText = findViewById(R.id.quiz_text_timer);
        timerProgress = findViewById(R.id.quiz_progress_timer);
        timerProgress.setMax(QUIZ_LENGTH_SECONDS);
        timerProgress.getProgressDrawable().setColorFilter(
                Color.RED, android.graphics.PorterDuff.Mode.SRC_IN);

        pager = findViewById(R.id.quiz_pager);
        pager.setAdapter(new FragmentStateAdapter(this) {
            @NonNull
            @Override
            public Fragment createFragment(int position) {
                return QuizFragment.newInstance(position, hintUsed[position]);
            }

            @Override
            public int getItemCount() {
                assert states.currentQuestions != null;
                return states.currentQuestions.size();
            }
        });

        pager.registerOnPageChangeCallback(new ViewPager2.OnPageChangeCallback() {
            @Override
            public void onPageSelected(int position) {
                File sound = new File(
                        getFilesDir(), "sound/" + states.currentQuestions.get(position).id);
                myMediaPlayer.setSource(QuizActivity.this, sound);

                assert states.currentQuestions != null;
                prevButton.setEnabled(position > 0);
                nextButton.setEnabled(position < states.currentQuestions.size() - 1);
            }
        });

        prevButton.setOnClickListener(v -> {
            pager.setCurrentItem(pager.getCurrentItem() - 1);
        });

        nextButton.setOnClickListener(v -> {
            pager.setCurrentItem(pager.getCurrentItem() + 1);
        });

        timer = new CountDownTimer(QUIZ_LENGTH_SECONDS * 1000, 1_000) {
            @Override
            public void onTick(long millisUntilFinished) {
                long seconds = millisUntilFinished / 1000 % 60;
                long minutes = millisUntilFinished / 1000 / 60;
                timerText.setText(String.format(Locale.ENGLISH, "%02d:%02d", minutes, seconds));
                timerProgress.setProgress((int) (millisUntilFinished / 1000), false);
                leftTime = (int) (millisUntilFinished / 1000);
            }

            @Override
            public void onFinish() {
                gotoScores();
            }
        }.start();
    }

    private void gotoScores() {
        Intent intent = new Intent(QuizActivity.this, ScoreActivity.class);
        intent.putExtra("hint_used", hintUsed);
        intent.putExtra("time_left", leftTime);
        startActivity(intent);
        finish();
    }

    public void viewHint(int position) {
        hintUsed[position] = true;
    }

    @Override
    public void onResume() {
        super.onResume();
        myMediaPlayer.startUpdate();
    }

    @Override
    public void onPause() {
        myMediaPlayer.stopUpdate();
        super.onPause();
    }

    @Override
    protected void onDestroy() {
        myMediaPlayer.dispose();
        timer.cancel();
        super.onDestroy();
    }
}