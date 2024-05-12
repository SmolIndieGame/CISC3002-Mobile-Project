package com.example.toedy_project;

import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.RadioButton;
import android.widget.RadioGroup;
import android.widget.TextView;

import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;

import com.bumptech.glide.Glide;
import com.bumptech.glide.load.engine.DiskCacheStrategy;

import java.io.File;
import java.util.List;
import java.util.Objects;

public class QuizFragment extends Fragment {
    int position;
    boolean hinted;

    public QuizFragment() {
    }

    public static QuizFragment newInstance(int position, boolean hinted) {
        QuizFragment fragment = new QuizFragment();
        Bundle args = new Bundle();
        args.putInt("position", position);
        args.putBoolean("hinted", hinted);
        fragment.setArguments(args);
        return fragment;
    }

    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        if (getArguments() != null) {
            position = getArguments().getInt("position");
            hinted = getArguments().getBoolean("hinted");
        }
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        View root = inflater.inflate(R.layout.fragment_quiz, container, false);

        QuizActivity activity = (QuizActivity) requireActivity();

        assert activity.states.currentQuestions != null;
        assert activity.states.currentAnswers != null;
        QuestionObj.Question question = activity.states.currentQuestions.get(position);
        List<String> answers = activity.states.currentAnswers;

        ImageView image = root.findViewById(R.id.quiz_image_hint);
        if (hinted)
            Glide.with(this)
                    .load(new File(activity.getFilesDir(), "hint/" + question.id))
                    .diskCacheStrategy(DiskCacheStrategy.NONE)
                    .into(image);
        image.setOnClickListener(v -> {
            Log.d("myapp", "onCreateView: " + question.id);
            Glide.with(this)
                    .load(new File(activity.getFilesDir(), "hint/" + question.id))
                    .diskCacheStrategy(DiskCacheStrategy.NONE)
                    .into(image);
            activity.viewHint(position);
        });

        TextView questionText = root.findViewById(R.id.quiz_text_question);
        questionText.setText(question.question);

        RadioGroup group = root.findViewById(R.id.quiz_options);
        group.removeAllViews();
        for (String option : question.options) {
            RadioButton radioButton = new RadioButton(activity);
            group.addView(radioButton);
            radioButton.setLayoutParams(new RadioGroup.LayoutParams(
                    RadioGroup.LayoutParams.MATCH_PARENT,
                    RadioGroup.LayoutParams.WRAP_CONTENT));

            radioButton.setText(option);
            if (Objects.equals(answers.get(position), option))
                radioButton.performClick();
            radioButton.setOnCheckedChangeListener((v, isChecked) -> {
                if (!isChecked)
                    return;
                answers.set(position, option);
            });

            radioButton.setChecked(Objects.equals(answers.get(position), option));
        }

        return root;
    }
}