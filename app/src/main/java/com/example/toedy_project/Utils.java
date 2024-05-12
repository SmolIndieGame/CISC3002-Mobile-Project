package com.example.toedy_project;

import android.content.Context;
import android.os.Handler;
import android.os.Looper;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStreamReader;
import java.nio.charset.StandardCharsets;

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
