package com.example.toedy_project;

import android.os.AsyncTask;
import android.view.View;
import android.widget.Toast;

import com.google.gson.Gson;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.lang.ref.WeakReference;
import java.util.List;
import java.util.concurrent.TimeUnit;

import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.Response;

public class DownloadQuestionsTask extends AsyncTask<QuestionObj, Integer, Boolean> {
    private final WeakReference<MainActivity> activityRef;

    public DownloadQuestionsTask(MainActivity activity) {
        super();
        this.activityRef = new WeakReference<>(activity);
    }

    @Override
    protected void onPostExecute(Boolean isSuccessful) {
        super.onPostExecute(isSuccessful);

        MainActivity act = activityRef.get();
        if (act == null) return;

        if (!isSuccessful) {
            Toast.makeText(act, "Download questions failed", Toast.LENGTH_SHORT).show();
        }

        act.startButton.setVisibility(View.VISIBLE);
        act.startProgress.setVisibility(View.GONE);
    }

    @Override
    protected void onProgressUpdate(Integer... values) {
        super.onProgressUpdate(values);

        MainActivity act = activityRef.get();
        if (act == null) return;

        act.startProgress.setProgress(values[0], false);
    }

    @Override
    protected void onCancelled() {
        super.onCancelled();

        MainActivity act = activityRef.get();
        if (act == null) return;

        Toast.makeText(act, "Question downloads cancelled", Toast.LENGTH_SHORT).show();
    }

    @Override
    protected Boolean doInBackground(QuestionObj... objects) {
        MainActivity act = activityRef.get();
        if (act == null) return null;

        QuestionObj questions = objects[0];
        OkHttpClient client = new OkHttpClient.Builder()
                .callTimeout(60, TimeUnit.SECONDS)
                .build();
        List<QuestionObj.Question> questionList = questions.questions;


        try {
            for (int i = 0; i < questionList.size(); i++) {
                QuestionObj.Question question = questionList.get(i);
                File dir = new File(act.getFilesDir(), "hint");
                boolean ignored = dir.mkdirs();
                File file = new File(dir, String.valueOf(i));
                downloadFile(client, question.hint_url, file);
                publishProgress(i + 1);

                if (isCancelled())
                    return false;
            }

            for (int i = 0; i < questionList.size(); i++) {
                QuestionObj.Question question = questionList.get(i);
                File dir = new File(act.getFilesDir(), "sound");
                boolean ignored = dir.mkdirs();
                File file = new File(dir, String.valueOf(i));
                downloadFile(client, question.sound_url, file);
                publishProgress(questionList.size() + i + 1);

                if (isCancelled())
                    return false;
            }

            Gson gson = new Gson();
            Utils.writeAllText(act, "questions.json", gson.toJson(questions));
            act.states.questions = questions;
        } catch (IOException e) {
            e.printStackTrace();
            return false;
        }
        return true;
    }

    private void downloadFile(OkHttpClient client, String url, File saveFile) throws IOException {
        Request request = new Request.Builder()
                .url(url)
                .get()
                .build();

        try (Response response = client.newCall(request).execute()) {
            InputStream is = response.body().byteStream();
            try (FileOutputStream os = new FileOutputStream(saveFile, false)) {
                byte[] buffer = new byte[8192];
                int read;
                while ((read = is.read(buffer, 0, 8192)) >= 0) {
                    os.write(buffer, 0, read);
                }
            }
        }
    }
}
