package com.example.toedy_project;

import android.app.Application;

public class GlobalStates extends Application {
    public String auth0AccessToken;
    public String userId;
    public String userName;

    public QuestionObj questions;
    public ScoreObj scores;
}

