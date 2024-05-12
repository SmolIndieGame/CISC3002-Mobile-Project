package com.example.toedy_project;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.os.Handler;
import android.os.Looper;
import android.widget.Toast;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStreamReader;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.List;
import java.util.Random;
import java.util.stream.IntStream;

public class Utils {
    public static LoopHandle loop(Runnable runnable, int delayMs) {
        return new LoopHandle(runnable, delayMs);
    }

    public static String readAllText(Context context, String filePath) throws IOException {
        try (FileInputStream is = context.openFileInput(filePath)) {
            InputStreamReader sr = new InputStreamReader(is, StandardCharsets.UTF_8);
            BufferedReader reader = new BufferedReader(sr);
            StringBuilder sb = new StringBuilder();
            String line;
            while ((line = reader.readLine()) != null) {
                sb.append(line).append('\n');
            }
            return sb.toString();
        }
    }

    public static void writeAllText(Context context, String filePath, String text) throws IOException {
        boolean ignored = new File(context.getFilesDir(), filePath).createNewFile();
        try (FileOutputStream os = context.openFileOutput(filePath, Context.MODE_PRIVATE)) {
            os.write(text.getBytes(StandardCharsets.UTF_8));
        }
    }

    public static <T> void shuffle(List<T> list) {
        Random random = new Random();
        for (int i = list.size() - 1; i >= 1; i--) {
            int chosen = random.nextInt(i + 1);

            T tmp = list.get(chosen);
            list.set(chosen, list.get(i));
            list.set(i, tmp);
        }
    }

    public static void shuffle(int[] array) {
        Random random = new Random();
        for (int i = array.length - 1; i >= 1; i--) {
            int chosen = random.nextInt(i + 1);

            int tmp = array[chosen];
            array[chosen] = array[i];
            array[i] = tmp;
        }
    }

    static void startQuiz(Activity activity) {
        GlobalStates states = (GlobalStates) activity.getApplication();
        if (states.questions == null) {
            Toast.makeText(activity, "Questions not downloaded, cannot start", Toast.LENGTH_LONG).show();
            return;
        }

        int[] indices = IntStream.range(0, states.questions.questions.size())
                .toArray();
        shuffle(indices);
        states.currentQuestions = new ArrayList<>();
        for (int i = 0; i < indices.length && i < MainActivity.QUIZ_QUESTION_COUNT; i++) {
            QuestionObj.Question question = states.questions.questions.get(indices[i]).copy();
            shuffle(question.options);
            states.currentQuestions.add(question);
        }

        states.currentAnswers = new ArrayList<>(states.currentQuestions.size());
        for (QuestionObj.Question ignored : states.currentQuestions)
            states.currentAnswers.add("");

        Intent intent = new Intent(activity, QuizActivity.class);
        activity.startActivity(intent);
    }

    public static class LoopHandle {
        private final Handler handler;
        private final Runnable wrappedRunnable;

        public LoopHandle(Runnable runnable, int delayMs) {
            handler = new Handler(Looper.myLooper());
            wrappedRunnable = new Runnable() {
                @Override
                public void run() {
                    runnable.run();
                    handler.postDelayed(this, delayMs);
                }
            };
            handler.postDelayed(wrappedRunnable, delayMs);
        }

        public void dispose() {
            handler.removeCallbacks(wrappedRunnable);
        }
    }
}
