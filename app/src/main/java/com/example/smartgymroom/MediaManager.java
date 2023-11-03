package com.example.smartgymroom;

import android.content.Context;
import android.media.MediaPlayer;

public class MediaManager {

    public MediaManager(Context context) {
        this.context = context;
        currentSong = -1;
    }


    private Context context;
    private MediaPlayer mediaPlayer;
    private int currentSong;


    public void startSong(int activity) {
        if (currentSong == activity) {
            return;
        }
        currentSong = activity;

        stopSong();
        int song = activity == 1 ? R.raw.cardio : activity == 2 ? R.raw.strength : R.raw.stretching;
        mediaPlayer = MediaPlayer.create(context, song);

        mediaPlayer.start();
    }

    public void stopSong() {
        if (mediaPlayer != null) {
            mediaPlayer.stop();
            mediaPlayer.release();
        }
    }
}

