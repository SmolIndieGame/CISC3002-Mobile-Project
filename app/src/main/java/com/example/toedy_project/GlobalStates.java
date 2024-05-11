package com.example.toedy_project;

import android.app.Application;

import androidx.annotation.Nullable;

public class GlobalStates extends Application {
    @Nullable public String auth0AccessToken;
    @Nullable public String userId;
    @Nullable public String userName;

    @Nullable public QuestionObj questions;
    @Nullable public ScoreObj scores;
}

