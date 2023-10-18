package com.example.smartgymroom;

import android.animation.AnimatorSet;
import android.animation.ObjectAnimator;
import android.animation.PropertyValuesHolder;
import android.content.Intent;
import android.os.Bundle;
import android.os.Handler;
import android.view.MotionEvent;
import android.view.animation.AlphaAnimation;
import android.view.animation.Animation;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toolbar;

import androidx.appcompat.app.AppCompatActivity;

public class LoadingPage extends AppCompatActivity {
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.loading_page);


        //Animations start for app
        ImageView imageViewlogo = findViewById(R.id.logoImageView);
        TextView textViewSmart = findViewById(R.id.textViewSmart);
        TextView textViewGym = findViewById(R.id.textViewGym);
        TextView textViewRoom = findViewById(R.id.textViewRoom);

        new Handler().postDelayed(() -> logoAnimation(imageViewlogo), 250);

        ObjectAnimator scaleXAnimator = ObjectAnimator.ofFloat(imageViewlogo, "scaleX", 1.0f, 0.25f);
        ObjectAnimator scaleYAnimator = ObjectAnimator.ofFloat(imageViewlogo, "scaleY", 1.0f, 0.25f);

        AnimatorSet animatorSet = new AnimatorSet();
        animatorSet.setDuration(1000);
        animatorSet.setStartDelay(2000);// Duration in milliseconds
        animatorSet.playTogether(scaleXAnimator, scaleYAnimator);  // Play both animations at the same time
        animatorSet.start();

        PropertyValuesHolder pvhX = PropertyValuesHolder.ofFloat("translationX", 0, -410);
        PropertyValuesHolder pvhY = PropertyValuesHolder.ofFloat("translationY", 0, -800);
        ObjectAnimator animator = ObjectAnimator.ofPropertyValuesHolder(imageViewlogo, pvhX, pvhY);
        animator.setDuration(500);
        animator.setStartDelay(2500);// Duration in milliseconds

        animator.start();
        new Handler().postDelayed(() -> startFadeInAnimation(textViewSmart), 2750);  // Delay 1 second
        new Handler().postDelayed(() -> startFadeInAnimation(textViewGym), 3000);   // Delay 3 seconds
        new Handler().postDelayed(() -> startFadeInAnimation(textViewRoom), 3250);  // Delay 5 seconds
        // End of animations
    }

    //Change of page
    float x1,x2,y1,y2;
    public boolean onTouchEvent(MotionEvent touchEvent){
        switch(touchEvent.getAction()){
            case MotionEvent.ACTION_DOWN:
                x1 = touchEvent.getX();
                y1 = touchEvent.getY();
                break;
            case MotionEvent.ACTION_UP:
                x2 = touchEvent.getX();
                y2 = touchEvent.getY();
                if(x1 < x2){
                Intent i = new Intent(LoadingPage.this, MainActivity.class);
                startActivity(i);
                overridePendingTransition(R.anim.slide_in_left,R.anim.slide_out_right);
            }else if(x1 > x2){
                Intent i = new Intent(LoadingPage.this, MainActivity.class);
                startActivity(i);
                overridePendingTransition(R.anim.slide_in_right,R.anim.slide_out_left);

            }
            break;
        }
        return false;
    }

    private void logoAnimation(final ImageView imageView) {
        AlphaAnimation fadeIn = new AlphaAnimation(0.0f, 1.0f);
        fadeIn.setDuration(1000);
        fadeIn.setAnimationListener(new Animation.AnimationListener() {
            @Override
            public void onAnimationStart(Animation animation) {
                imageView.setAlpha(1.0f);
            }@Override
            public void onAnimationEnd(Animation animation) {
                // Ensure the view remains visible after the animation
            }
            @Override
            public void onAnimationRepeat(Animation animation) {

            }
        });

        imageView.startAnimation(fadeIn);
    }
    private void startFadeInAnimation(final TextView textView) {
        AlphaAnimation fadeIn = new AlphaAnimation(0.0f, 1.0f);
        fadeIn.setDuration(1000);

        fadeIn.setAnimationListener(new Animation.AnimationListener() {
            @Override
            public void onAnimationStart(Animation animation) {
                textView.setAlpha(1.0f);
            }

            @Override
            public void onAnimationEnd(Animation animation) {
                // Ensure the view remains visible after the animation
            }

            @Override
            public void onAnimationRepeat(Animation animation) {
                // Nothing needed here
            }
        });

        textView.startAnimation(fadeIn);
    }
    
}
