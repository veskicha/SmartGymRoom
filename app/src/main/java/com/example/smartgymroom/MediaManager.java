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


    public void startSong(int activity) {
        if (activity == 1){
            mediaPlayer = MediaPlayer.create(context, R.raw.cardio) ;
        }else if(activity == 2){
            mediaPlayer = MediaPlayer.create(context, R.raw.strength) ;
        }else if (activity == 3 ){
            mediaPlayer = MediaPlayer.create(context, R.raw.stretching) ;
        }else{
            mediaPlayer = MediaPlayer.create(context, R.raw.song1) ;
        }


        mediaPlayer.start();
    }

    public void stopSong() {
        //mediaPlayer.pause();
        mediaPlayer.stop();
        mediaPlayer.release();
    }
}

