package com.example.toedy_project;

import android.app.Activity;
import android.media.MediaPlayer;
import android.net.Uri;
import android.widget.Button;
import android.widget.SeekBar;
import android.widget.TextView;

import androidx.annotation.Nullable;

import java.io.File;
import java.util.Locale;

public class MyMediaPlayer {
    private final Button play;
    private final Button pause;
    private final Button stop;
    private final TextView time;
    private final SeekBar seek;
    @Nullable
    private MediaPlayer mediaPlayer;

    private boolean isTouchingSeek;
    @Nullable
    private Utils.LoopHandle seekUpdater;

    public MyMediaPlayer(Activity activity, Button play, Button pause, Button stop, TextView time, SeekBar seek) {
        this.play = play;
        this.pause = pause;
        this.stop = stop;
        this.time = time;
        this.seek = seek;

        play.setEnabled(false);
        pause.setEnabled(false);
        stop.setEnabled(false);
        play.setOnClickListener(v -> {
            if (mediaPlayer == null)
                return;
            mediaPlayer.start();
            play.setEnabled(false);
            pause.setEnabled(true);
            stop.setEnabled(true);
        });
        pause.setOnClickListener(v -> {
            if (mediaPlayer == null)
                return;
            mediaPlayer.pause();
            play.setEnabled(true);
            pause.setEnabled(false);
            stop.setEnabled(true);
        });
        stop.setOnClickListener(v -> {
            if (mediaPlayer == null)
                return;
            mediaPlayer.pause();
            mediaPlayer.seekTo(0);
            play.setEnabled(true);
            pause.setEnabled(false);
            stop.setEnabled(false);
        });

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
                activity.runOnUiThread(() -> {
                    if (mediaPlayer == null)
                        return;
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
    }

    public void startUpdate() {
        seekUpdater = new Utils.LoopHandle(() -> {
            if (mediaPlayer == null)
                return;
            if (!isTouchingSeek)
                seek.setProgress(mediaPlayer.getCurrentPosition(), false);
            final int position = mediaPlayer.getCurrentPosition();
            final int seconds = position / 1000 % 60;
            final int minutes = position / 1000 / 60;
            time.setText(String.format(Locale.ENGLISH, "%02d:%02d", minutes, seconds));
        }, 100);
    }

    public void stopUpdate() {
        if (seekUpdater != null)
            seekUpdater.dispose();
    }

    public void dispose() {
        if (mediaPlayer != null)
            mediaPlayer.release();
        if (seekUpdater != null)
            seekUpdater.dispose();
    }

    public void setSource(Activity activity, File source) {
        if (mediaPlayer != null)
            mediaPlayer.release();
        mediaPlayer = MediaPlayer.create(activity, Uri.fromFile(source));
        mediaPlayer.setOnCompletionListener(player -> {
            play.setEnabled(true);
            pause.setEnabled(false);
            stop.setEnabled(false);
        });

        play.setEnabled(true);
        pause.setEnabled(false);
        stop.setEnabled(false);
        seek.setMax(mediaPlayer.getDuration());
    }
}
