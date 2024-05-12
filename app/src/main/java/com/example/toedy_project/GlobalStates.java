package com.example.toedy_project;

import android.app.Application;

import androidx.annotation.Nullable;

import java.util.List;

public class GlobalStates extends Application {
    @Nullable
    public String auth0AccessToken;
    @Nullable
    public String userId;
    @Nullable
    public String userName;

    @Nullable
    public QuestionObj questions;
    @Nullable
    public ScoreObj scores;

    @Nullable
    public List<QuestionObj.Question> currentQuestions;
    @Nullable
    public List<String> currentAnswers;
}

