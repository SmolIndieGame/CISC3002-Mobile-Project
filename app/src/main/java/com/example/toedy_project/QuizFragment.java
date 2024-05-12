package com.example.toedy_project;

import android.content.Context;
import android.media.MediaPlayer;
import android.net.Uri;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.SeekBar;
import android.widget.TextView;

import androidx.fragment.app.Fragment;

import com.bumptech.glide.Glide;

import java.io.File;
import java.util.Locale;

public class QuizFragment extends Fragment {
    MediaPlayer mediaPlayer;
    Button play;
    Button pause;
    Button stop;
    TextView time;
    SeekBar seek;
    boolean isTouchingSeek;
    boolean suppressSeekCompleteEvent;
    Utils.LoopHandle seekUpdater;

    public QuizFragment() {
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        View root = inflater.inflate(R.layout.fragment_quiz, container, false);

        Context context = requireContext();
        File hint = new File(context.getFilesDir(), "hint/0");
        File sound = new File(context.getFilesDir(), "sound/0");

        ImageView image = root.findViewById(R.id.quiz_image_hint);
        Glide
                .with(this)
                .load(hint).into(image);

        play = root.findViewById(R.id.quiz_button_start);
        pause = root.findViewById(R.id.quiz_button_pause);
        stop = root.findViewById(R.id.quiz_button_stop);
        time = root.findViewById(R.id.quiz_text_sound);
        seek = root.findViewById(R.id.quiz_seek_sound);

        mediaPlayer = MediaPlayer.create(context, Uri.fromFile(sound));
        mediaPlayer.setOnCompletionListener(player -> {
            play.setEnabled(true);
            pause.setEnabled(false);
            stop.setEnabled(false);
        });

        play.setEnabled(true);
        pause.setEnabled(false);
        stop.setEnabled(false);
        play.setOnClickListener(v -> {
            mediaPlayer.start();
            play.setEnabled(false);
            pause.setEnabled(true);
            stop.setEnabled(true);
        });
        pause.setOnClickListener(v -> {
            mediaPlayer.pause();
            play.setEnabled(true);
            pause.setEnabled(false);
            stop.setEnabled(true);
        });
        stop.setOnClickListener(v -> {
            mediaPlayer.pause();
            suppressSeekCompleteEvent = true;
            mediaPlayer.seekTo(0);
            play.setEnabled(true);
            pause.setEnabled(false);
            stop.setEnabled(false);
        });

        seek.setMax(mediaPlayer.getDuration());
        seek.setOnSeekBarChangeListener(new SeekBar.OnSeekBarChangeListener() {
            @Override
            public void onProgressChanged(SeekBar seekBar, int progress, boolean fromUser) {
            }

            @Override
            public void onStartTrackingTouch(SeekBar seekBar) {
                isTouchingSeek = true;
            }

            @Override
            public void onStopTrackingTouch(SeekBar seekBar) {
                isTouchingSeek = false;
                requireActivity().runOnUiThread(() -> {
                    if (!mediaPlayer.isPlaying()) {
                        mediaPlayer.start();
                        play.setEnabled(false);
                        pause.setEnabled(true);
                        stop.setEnabled(true);
                    }
                    mediaPlayer.seekTo(seekBar.getProgress());
                });
            }
        });

        return root;
    }

    @Override
    public void onResume() {
        super.onResume();
        seekUpdater = new Utils.LoopHandle(() -> {
            if (!isTouchingSeek)
                seek.setProgress(mediaPlayer.getCurrentPosition(), false);
            final int position = mediaPlayer.getCurrentPosition();
            final int seconds = position / 1000 % 60;
            final int minutes = position / 1000 / 60;
            time.setText(String.format(Locale.ENGLISH, "%02d:%02d", minutes, seconds));
        }, 100);
    }

    @Override
    public void onPause() {
        seekUpdater.dispose();
        super.onPause();
    }
}