package com.tooglamtogivedamn.earnwquizspin;

import android.animation.ValueAnimator;
import android.content.Intent;
import android.os.Bundle;
import android.os.Handler;
import android.os.Looper;
import android.widget.TextView;

import androidx.appcompat.app.AppCompatActivity;

public class SplashScreen extends AppCompatActivity {

   private TextView textAnim;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_splash_screen);


        textAnim=findViewById(R.id.textAnim);
        textAnim.setText("");

        // Start the text writing animation
        animateText("EarnWFun");
        // for splash screen
        new Handler(Looper.getMainLooper()).postDelayed(new Runnable() {
            @Override
            public void run() {
                Intent intent = new Intent(SplashScreen.this, LoginActivity.class);
                startActivity(intent);
                finish();
            }
        }, 3000);

    }
    private void animateText(String text) {
        ValueAnimator valueAnimator = ValueAnimator.ofInt(0, text.length());
        valueAnimator.setDuration(2500); // Set the duration of animation in milliseconds

        valueAnimator.addUpdateListener(animation -> {
            int animatedValue = (int) animation.getAnimatedValue();
            textAnim.setText(text.substring(0, animatedValue));
        });

        valueAnimator.start();
    }
}