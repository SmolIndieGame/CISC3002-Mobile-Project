package com.example.toedy_project;

import java.util.ArrayList;
import java.util.List;

public class QuestionObj {
    public int version;
    public List<Question> questions = new ArrayList<>();

    static class Question {
        public int id;
        public String question;
        public List<String> options;
        public String answer;
        public String hint_url;
        public String sound_url;

        public Question copy() {
            Question q = new Question();
            q.id = id;
            q.question = question;
            q.options = new ArrayList<>(options);
            q.answer = answer;
            q.hint_url = hint_url;
            q.sound_url = sound_url;
            return q;
        }
    }
}
