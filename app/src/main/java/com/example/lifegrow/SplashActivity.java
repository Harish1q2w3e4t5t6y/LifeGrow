package com.example.lifegrow;

import android.animation.AnimatorSet;
import android.animation.ObjectAnimator;
import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;
import android.os.Handler;
import android.view.View;
import android.view.animation.AccelerateDecelerateInterpolator;
import android.widget.ImageView;
import android.widget.RelativeLayout;
import android.widget.TextView;

import com.bumptech.glide.Glide;

public class SplashActivity extends Activity {

    private final Handler handler = new Handler();
    private boolean toggleOn = false;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_splash);
        View toggleCircle = findViewById(R.id.toggleCircle);
        View toggleBackground = findViewById(R.id.toggleBackground);
        TextView productivityText = findViewById(R.id.productivityText);
        RelativeLayout distractionIcons = findViewById(R.id.distractionIcons);


        // Load GIFs for all distraction icons
        loadGif(R.id.iconYouTube, R.drawable.youtube);
        loadGif(R.id.iconGaming, R.drawable.insta);
        loadGif(R.id.iconBingeWatching, R.drawable.fackbook);
        loadGif(R.id.iconProcrastination, R.drawable.fire);
        loadGif(R.id.iconNotifications, R.drawable.shopping);
        loadGif(R.id.iconShopping, R.drawable.message);
        loadGif(R.id.iconMemes, R.drawable.snapchat);
        loadGif(R.id.iconPhone, R.drawable.tictoc);
        loadGif(R.id.iconMusic, R.drawable.scrolling);
        loadGif(R.id.iconLaziness, R.drawable.twitter);
        loadGif(R.id.iconScrolling, R.drawable.videogame);

// Video game & digital addiction icons
        loadGif(R.id.iconReddit, R.drawable.reddit);
        loadGif(R.id.iconAddiction, R.drawable.addiction);
        loadGif(R.id.iconMessages, R.drawable.message2);
        loadGif(R.id.iconMovies, R.drawable.movie);
        loadGif(R.id.iconOrdering, R.drawable.order);
        loadGif(R.id.iconWebsiteBrowsing, R.drawable.website);



        productivityText.setAlpha(0);

        toggleCircle.setOnClickListener(view -> {
            if (!toggleOn) {
                toggleOn = true;
                activateToggle(toggleCircle, toggleBackground, distractionIcons, productivityText);

            }
        });

        handler.postDelayed(() -> {
            if (!toggleOn) {
                toggleOn = true;
                activateToggle(toggleCircle, toggleBackground, distractionIcons, productivityText);

            }
        }, 2500);

        handler.postDelayed(() -> {
            startActivity(new Intent(SplashActivity.this, Login.class));
            finish();
        }, 4500);
    }

    private void loadGif(int imageViewId, int gifResource) {
        ImageView imageView = findViewById(imageViewId);
        Glide.with(this)
                .asGif()
                .load(gifResource)
                .into(imageView);
    }

    private void activateToggle(View toggleCircle, View toggleBackground, RelativeLayout distractionIcons, TextView productivityText) {
        // Ensure toggle starts at the leftmost position (relative to its parent)
        toggleCircle.setTranslationX(0);

        // Animate toggle movement from left (0) to right (50dp)
        ObjectAnimator moveToggle = ObjectAnimator.ofFloat(toggleCircle, "translationX", 0, 0);
        moveToggle.setDuration(0);
        moveToggle.setInterpolator(new AccelerateDecelerateInterpolator());

        // Bounce back effect: move slightly from 50dp to 45dp
        ObjectAnimator bounceBack = ObjectAnimator.ofFloat(toggleCircle, "translationX", 0, 70);
        bounceBack.setDuration(600);

        AnimatorSet toggleMoveSet = new AnimatorSet();
        toggleMoveSet.playSequentially(moveToggle, bounceBack);

        // Background pulse effect remains unchanged
        ObjectAnimator pulseUp = ObjectAnimator.ofFloat(toggleBackground, "scaleX", 1f, 1.05f);
        ObjectAnimator pulseDown = ObjectAnimator.ofFloat(toggleBackground, "scaleX", 1.05f, 1f);
        pulseUp.setDuration(300);
        pulseDown.setDuration(300);

        AnimatorSet backgroundPulse = new AnimatorSet();
        backgroundPulse.playSequentially(pulseUp, pulseDown);

        // Play both animations together
        AnimatorSet fullAnimation = new AnimatorSet();
        fullAnimation.playTogether(toggleMoveSet, backgroundPulse);
        fullAnimation.start();


        new Handler().postDelayed(() -> {
            // Smooth fade-in effect (removed scaling)
            ObjectAnimator fadeInText = ObjectAnimator.ofFloat(productivityText, "alpha", 0f, 1f);
            fadeInText.setDuration(2500); // Increased duration for a smoother fade-in

            fadeInText.start();
        }, 100);



        for (int i = 0; i < distractionIcons.getChildCount(); i++) {
            View icon = distractionIcons.getChildAt(i);
            ObjectAnimator fadeOutIcon = ObjectAnimator.ofFloat(icon, "alpha", 1f, 0f);
            ObjectAnimator scaleDownX = ObjectAnimator.ofFloat(icon, "scaleX", 1f, 0.6f);
            ObjectAnimator scaleDownY = ObjectAnimator.ofFloat(icon, "scaleY", 1f, 0.6f);

            fadeOutIcon.setDuration(2200);
            scaleDownX.setDuration(2200);
            scaleDownY.setDuration(2200);

            AnimatorSet iconAnim = new AnimatorSet();
            iconAnim.playTogether(fadeOutIcon, scaleDownX, scaleDownY);
            iconAnim.start();
        }


        AnimatorSet finalSet = new AnimatorSet();
        finalSet.playTogether(toggleMoveSet, backgroundPulse);
        finalSet.start();
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        handler.removeCallbacksAndMessages(null);
    }
}


