package com.example.toedy_project;

import android.app.Activity;

import androidx.annotation.NonNull;

import com.google.gson.Gson;

import java.io.IOException;

import okhttp3.Call;
import okhttp3.MediaType;
import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.RequestBody;
import okhttp3.Response;

public class Fetches {
    public static void questions(Activity activity, String jsonBinAccessKey, Callback2<QuestionObj, String> successCallBack, Callback<IOException> failureCallback) {
        OkHttpClient client = new OkHttpClient();
        Request request = new Request.Builder()
                .url("https://api.jsonbin.io/v3/b/663de6a9acd3cb34a845a7f9")
                .header("X-Access-Key", jsonBinAccessKey)
                .header("X-Bin-Meta", "false")
                .get()
                .build();
        client.newCall(request).enqueue(new MyCallback<>(activity, QuestionObj.class, successCallBack, failureCallback));
    }

    public static void scores(Activity activity, String jsonBinAccessKey, Callback2<ScoreObj, String> successCallBack, Callback<IOException> failureCallback) {
        OkHttpClient client = new OkHttpClient();
        Request request = new Request.Builder()
                .url("https://api.jsonbin.io/v3/b/663dfc05acd3cb34a845b7a8")
                .header("X-Access-Key", jsonBinAccessKey)
                .header("X-Bin-Meta", "false")
                .get()
                .build();
        client.newCall(request).enqueue(new MyCallback<>(activity, ScoreObj.class, successCallBack, failureCallback));
    }

    public static void putScores(Activity activity, String jsonBinAccessKey, ScoreObj scores, Callback2<Object, String> successCallBack, Callback<IOException> failureCallback) {
        Gson gson = new Gson();
        OkHttpClient client = new OkHttpClient();
        RequestBody body = RequestBody.create(gson.toJson(scores), MediaType.parse("application/json"));
        Request request = new Request.Builder()
                .url("https://api.jsonbin.io/v3/b/663dfc05acd3cb34a845b7a8")
                .header("Content-Type", "application/json")
                .header("X-Access-Key", jsonBinAccessKey)
                .put(body)
                .build();
        client.newCall(request).enqueue(new MyCallback<>(activity, Object.class, successCallBack, failureCallback));
    }

    @FunctionalInterface
    public interface Callback<T> {
        void call(T t);
    }

    @FunctionalInterface
    public interface Callback2<T1, T2> {
        void call(T1 t1, T2 t2);
    }

    static class MyCallback<TJson> implements okhttp3.Callback {
        private final Activity activity;
        private final Class<TJson> jsonClass;
        private final Callback2<TJson, String> successCallBack;
        private final Callback<IOException> failureCallback;

        public MyCallback(Activity activity, Class<TJson> jsonClass, Callback2<TJson, String> successCallBack, Callback<IOException> failureCallback) {
            this.activity = activity;
            this.jsonClass = jsonClass;
            this.successCallBack = successCallBack;
            this.failureCallback = failureCallback;
        }

        @Override
        public void onFailure(@NonNull Call call, @NonNull IOException e) {
            e.printStackTrace();
            if (failureCallback != null)
                activity.runOnUiThread(() -> failureCallback.call(e));
        }

        @Override
        public void onResponse(@NonNull Call call, @NonNull Response response) throws IOException {
            if (!response.isSuccessful()) {
                IOException exception = new IOException(response.message());
                exception.printStackTrace();
                activity.runOnUiThread(() -> failureCallback.call(exception));
            }

            String res = response.body().string();
            Gson gson = new Gson();
            TJson json = gson.fromJson(res, jsonClass);
            if (successCallBack != null)
                activity.runOnUiThread(() -> successCallBack.call(json, res));
        }
    }
}
