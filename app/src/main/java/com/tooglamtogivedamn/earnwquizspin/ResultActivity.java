package com.tooglamtogivedamn.earnwquizspin;

import static android.content.ContentValues.TAG;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;

import android.app.Activity;
import android.app.Dialog;
import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.Toast;

import com.google.android.gms.ads.AdRequest;
import com.google.android.gms.ads.LoadAdError;
import com.google.android.gms.ads.MobileAds;
import com.google.android.gms.ads.OnUserEarnedRewardListener;
import com.google.android.gms.ads.initialization.InitializationStatus;
import com.google.android.gms.ads.initialization.OnInitializationCompleteListener;
import com.google.android.gms.ads.rewarded.RewardItem;
import com.google.android.gms.ads.rewarded.RewardedAd;
import com.google.android.gms.ads.rewarded.RewardedAdLoadCallback;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.firestore.FieldValue;
import com.google.firebase.firestore.FirebaseFirestore;
import com.tooglamtogivedamn.earnwquizspin.databinding.ActivityResultBinding;

public class ResultActivity extends AppCompatActivity {
    ActivityResultBinding binding;
    private RewardedAd rewardedAd;
    String appPackageName = "com.tooglamtogivedamn.earnwquizspin";
    String playStoreLink = "https://play.google.com/store/apps/details?id=" + appPackageName;

    int POINTS = 10;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        binding = ActivityResultBinding.inflate(getLayoutInflater());
        View view = binding.getRoot();
        setContentView(view);

        adLoader();

        int correctAns = getIntent().getIntExtra("correct", 0);
        int totalQue = getIntent().getIntExtra("total", 0);

        int points = correctAns * POINTS;
        binding.score.setText(String.format("%d/%d", correctAns, totalQue));
        binding.earnedCoins.setText(String.valueOf(points));
        FirebaseFirestore database = FirebaseFirestore.getInstance();
        database.collection("users")
                .document(FirebaseAuth.getInstance().getUid())
                .update("coins", FieldValue.increment(points));

        binding.restartBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent1 = new Intent(ResultActivity.this, MainActivity.class);
                startActivity(intent1);
                finish();
            }

        });

        binding.shareBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(Intent.ACTION_SEND);
                intent.setType("text/plain");
                intent.putExtra(Intent.EXTRA_TEXT, "Hey, Checkout my score in:"+playStoreLink);
                Intent chooser = Intent.createChooser(intent, "Share this app using...");
                startActivity(chooser);
            }
        });

    }

    private void adLoader(){
        Dialog dialog = new Dialog(ResultActivity.this);
        dialog.setContentView(R.layout.activity_dialog_box_rewardthird);
        dialog.setCancelable(false);
        Button adBtn=dialog.findViewById(R.id.adBtn);
        ImageView closePopUp=dialog.findViewById(R.id.closPopup);

        MobileAds.initialize(this, new OnInitializationCompleteListener() {
            @Override
            public void onInitializationComplete(@NonNull InitializationStatus initializationStatus) {

            }
        });
        loadAd();

        adBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {

                if (rewardedAd != null) {

                    Activity activityContext = ResultActivity.this;
                    rewardedAd.show(activityContext, new OnUserEarnedRewardListener() {
                        @Override
                        public void onUserEarnedReward(@NonNull RewardItem rewardItem) {
                            int correctAns=getIntent().getIntExtra("correct",0);
                            int points=correctAns*POINTS*POINTS;
                            FirebaseFirestore database=FirebaseFirestore.getInstance();
                            database.collection("users")
                                    .document(FirebaseAuth.getInstance().getUid())
                                    .update("coins", FieldValue.increment(points));

                            Toast.makeText(activityContext, "x10 coins added in your wallet", Toast.LENGTH_SHORT).show();
                        }
                    });
                    rewardedAd=null;
                    dialog.dismiss();
                } else {
                    Toast.makeText(ResultActivity.this, "try again after few seconds!", Toast.LENGTH_SHORT).show();
                    Log.d(TAG, "The rewarded ad wasn't ready yet.");
                }
            }
        });

        closePopUp.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                dialog.dismiss();
            }
        });

        dialog.show();
    }
    private void loadAd() {
        AdRequest adRequest = new AdRequest.Builder().build();
        RewardedAd.load(this, "ca-app-pub-1551156846680522/7673582365",
                adRequest, new RewardedAdLoadCallback() {
                    @Override
                    public void onAdFailedToLoad(@NonNull LoadAdError loadAdError) {
                        // Handle the error.
                        Log.d(TAG, loadAdError.toString());
                        rewardedAd = null;
                    }
                    @Override
                    public void onAdLoaded(@NonNull RewardedAd ad) {
                        rewardedAd = ad;
                        Log.d(TAG, "Ad was loaded.");
                    }
                });
    }
}