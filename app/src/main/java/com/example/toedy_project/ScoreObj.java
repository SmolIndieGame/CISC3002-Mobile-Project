package com.example.toedy_project;

import java.util.List;

public class ScoreObj {
    public List<Score> scores;

    static class Score {
        public String username;
        public double score;
        public int correct_answers;
        public int hint_used;
        public double time_left;
    }
}
