package com.example.toedy_project;

import java.util.List;

public class ScoreObj {
    public List<Score> scores;

    static class Score {
        public String userId;
        public String name;
        public double score;
        public int correct_answers;
        public int hint_used;
        public int time_left;
    }
}
