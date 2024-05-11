package com.example.toedy_project;

import java.util.ArrayList;
import java.util.List;

public class QuestionObj {
    public int version;
    public List<Question> questions = new ArrayList<>();

    static class Question {
        public String question;
        public List<String> options;
        public String answer;
        public String hint_url;
        public String sound_url;
    }
}
