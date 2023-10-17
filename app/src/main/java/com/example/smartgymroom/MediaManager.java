package com.example.smartgymroom;

import android.content.Context;
import android.media.MediaPlayer;

public class MediaManager {

    public MediaManager(Context context) {
        this.context = context;
        //mediaPlayer = MediaPlayer.create(context, R.raw.song);
    }


    private Context context;
    private MediaPlayer mediaPlayer;


    public void startSong() {
        mediaPlayer.start();
    }

    public void stopSong() {
        mediaPlayer.pause();
    }
}

