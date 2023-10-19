package com.example.smartgymroom;

import android.animation.AnimatorSet;
import android.animation.ObjectAnimator;
import android.animation.PropertyValuesHolder;
import android.animation.ValueAnimator;
import android.annotation.SuppressLint;
import android.content.Intent;
import android.os.Bundle;
import android.os.Handler;
import android.view.MotionEvent;
import android.view.animation.AlphaAnimation;
import android.view.animation.Animation;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.appcompat.app.AppCompatActivity;

public class LoadingPage extends AppCompatActivity {
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.loading_page);


        //Animations start for app
        ImageView imageViewlogo = findViewById(R.id.logoImageView);
        TextView textViewSmart = findViewById(R.id.textViewSmart);
        TextView textViewSwipe = findViewById(R.id.swiperight);
        @SuppressLint("MissingInflatedId") TextView textViewActiveSync = findViewById(R.id.activesync);
        TextView textViewGym = findViewById(R.id.textViewGym);
        TextView textViewRoom = findViewById(R.id.textViewRoom);

        new Handler().postDelayed(() -> logoAnimation(imageViewlogo), 250);
        new Handler().postDelayed(() -> startFadeInAnimation(textViewActiveSync), 350);


        //Scaling down the name and the logo
        ObjectAnimator scaleXAnimator = ObjectAnimator.ofFloat(imageViewlogo, "scaleX", 1.0f, 0.25f);
        ObjectAnimator scaleYAnimator = ObjectAnimator.ofFloat(imageViewlogo, "scaleY", 1.0f, 0.25f);
        ObjectAnimator scaleXName = ObjectAnimator.ofFloat(textViewActiveSync, "scaleX", 1.0f, 0.6f);
        ObjectAnimator scaleYName = ObjectAnimator.ofFloat(textViewActiveSync, "scaleY", 1.0f, 0.6f);

        AnimatorSet animatorSet = new AnimatorSet();
        AnimatorSet animatorSetName = new AnimatorSet();

        animatorSet.setDuration(1000);
        animatorSetName.setDuration(1000);

        animatorSet.setStartDelay(2000);
        animatorSetName.setStartDelay(2250);

        animatorSet.playTogether(scaleXAnimator, scaleYAnimator);  // Play both animations at the same time
        animatorSetName.playTogether(scaleXName, scaleYName);

        animatorSet.start();
        animatorSetName.start();

        //Moving the name and the logo

        PropertyValuesHolder NameX = PropertyValuesHolder.ofFloat("translationX", 0, 300);
        PropertyValuesHolder NameY = PropertyValuesHolder.ofFloat("translationY", 0, -1530);
        PropertyValuesHolder pvhX = PropertyValuesHolder.ofFloat("translationX", 0, -380);
        PropertyValuesHolder pvhY = PropertyValuesHolder.ofFloat("translationY", 0, -810);

        ObjectAnimator animator = ObjectAnimator.ofPropertyValuesHolder(imageViewlogo, pvhX, pvhY);
        ObjectAnimator animatorName = ObjectAnimator.ofPropertyValuesHolder(textViewActiveSync, NameX, NameY);

        animator.setDuration(500);
        animatorName.setDuration(500);

        animator.setStartDelay(2500);
        animatorName.setStartDelay(2600);

        animatorName.start();
        animator.start();

        //Fade in animations for the big texts
        new Handler().postDelayed(() -> startFadeInAnimation(textViewSmart), 3000);
        new Handler().postDelayed(() -> startFadeInAnimation(textViewGym), 3250);
        new Handler().postDelayed(() -> startFadeInAnimation(textViewRoom), 3500);
        new Handler().postDelayed(() -> startFadeInAnimation(textViewSwipe), 4250);

        //Blinking animation for the swipe right text
        ValueAnimator blinkAnim = ValueAnimator.ofFloat(0f, 1f);
        blinkAnim.setDuration(1500);
        blinkAnim.setStartDelay(4000);
        blinkAnim.setRepeatCount(Animation.INFINITE);
        blinkAnim.setRepeatMode(ValueAnimator.REVERSE);
        blinkAnim.addUpdateListener(new ValueAnimator.AnimatorUpdateListener() {
            @Override
            public void onAnimationUpdate(ValueAnimator animation) {
                float alpha = (float) animation.getAnimatedValue();
                textViewSwipe.setAlpha(alpha);
            }
        });
        blinkAnim.start();

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
