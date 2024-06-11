package com.tooglamtogivedamn.earnwquizspin;

import androidx.appcompat.app.AppCompatActivity;

import android.content.Intent;
import android.os.Bundle;
import android.os.CountDownTimer;
import android.view.View;
import android.widget.TextView;
import android.widget.Toast;

import com.google.android.gms.tasks.OnSuccessListener;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.QuerySnapshot;
import com.tooglamtogivedamn.earnwquizspin.databinding.ActivityQuizBinding;

import java.util.ArrayList;
import java.util.Random;

public class QuizActivity extends AppCompatActivity {
    ActivityQuizBinding binding;
    ArrayList<Questions> questions;
    FirebaseFirestore database;
    int index=0;
    Questions question;
    CountDownTimer timer;
    int correctAns=0;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        binding=ActivityQuizBinding.inflate(getLayoutInflater());
        setContentView(binding.getRoot());

        binding.progressBar.setVisibility(View.VISIBLE);

   questions=new ArrayList<>();
   database =FirebaseFirestore.getInstance();
final String catId=getIntent().getStringExtra("catId");
final Random random=new Random();
int rand=random.nextInt(12);
   database.collection("categories")
           .document(catId)
           .collection("questions").whereGreaterThanOrEqualTo( "index",rand)
           .orderBy("index")
           .limit(5).get().addOnSuccessListener(new OnSuccessListener<QuerySnapshot>() {
               @Override
               public void onSuccess(QuerySnapshot queryDocumentSnapshots) {
                    if(queryDocumentSnapshots.getDocuments().size()<5){
                        binding.progressBar.setVisibility(View.GONE);
                        database.collection("categories")
            .document(catId)
            .collection("questions").whereLessThanOrEqualTo( "index",rand)
            .orderBy("index")
            .limit(5).get().addOnSuccessListener(new OnSuccessListener<QuerySnapshot>() {
                            @Override
                            public void onSuccess(QuerySnapshot queryDocumentSnapshots) {
                                for (DocumentSnapshot snapshot:queryDocumentSnapshots){
                                    Questions question=snapshot.toObject(Questions.class);
                                    questions.add(question);
                                }
                                setNextQuestion();
                                binding.progressBar.setVisibility(View.GONE);

                            }

                    });
               }else{
for (DocumentSnapshot snapshot:queryDocumentSnapshots){
    Questions question=snapshot.toObject(Questions.class);
    questions.add(question);
}
                        setNextQuestion();
                        binding.progressBar.setVisibility(View.GONE);

               }
           }
           });

resetTimer();


        binding.quitBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                startActivity(new Intent(QuizActivity.this,MainActivity.class));
            }
        });
    }

    void resetTimer(){
        timer=new CountDownTimer(30000,1000) {
            @Override
            public void onTick(long millisUntilFinished) {
                binding.timer.setText(String.valueOf(millisUntilFinished / 1000));
            }

            @Override
            public void onFinish() {
                moveToNextQuestionOrEnd();

            }
        }.start();
    }
    void moveToNextQuestionOrEnd() {
        // You can add any logic here if needed
        // For now, we'll simply move to the next question or end the quiz
        reset();

        index++; // Move to the next question
        if (index < questions.size()) {
            // There are more questions, move to the next one
            setNextQuestion();
            resetTimer(); // Restart the timer for the next question
        } else {
            // No more questions, end the quiz and show the result
            Intent intent = new Intent(QuizActivity.this, ResultActivity.class);
            intent.putExtra("correct", correctAns);
            intent.putExtra("total", questions.size());
            startActivity(intent);
            finish(); // Optional: You may want to finish the quiz activity after showing the result
        }
    }

    // ... (existing code)

    @Override
    protected void onDestroy() {
        // Cancel the timer to prevent any memory leaks
        if (timer != null) {
            timer.cancel();
            timer = null;
        }
        super.onDestroy();
    }


    void shoeAns(){
        if (question.getAnswer().equals(binding.option1.getText().toString()))
            binding.option1.setBackground(getResources().getDrawable(R.drawable.option_right));
      if (question.getAnswer().equals(binding.option2.getText().toString()))
            binding.option2.setBackground(getResources().getDrawable(R.drawable.option_right));
      if (question.getAnswer().equals(binding.option3.getText().toString()))
            binding.option3.setBackground(getResources().getDrawable(R.drawable.option_right));
      if (question.getAnswer().equals(binding.option4.getText().toString()))
            binding.option4.setBackground(getResources().getDrawable(R.drawable.option_right));
    }

    void setNextQuestion(){
        if (timer!=null)
            timer.cancel();
        resetTimer();
        timer.start();
        if (index<questions.size()){
            binding.questionCounter.setText(String.format("%d/%d",(index+1),questions.size()));
            question= questions.get(index);
            binding.question.setText(question.getQuestion());
            binding.option1.setText(question.getOpt1());
            binding.option2.setText(question.getOpt2());
            binding.option3.setText(question.getOpt3());
            binding.option4.setText(question.getOpt4());

        } else {
            // If there are no more questions, you may want to handle this scenario.
            // For example, show a message or end the quiz activity.
            Toast.makeText(this, "Quiz end", Toast.LENGTH_SHORT).show();

        }
    }
    void checkAns(TextView textView) {
        String selectedAns = textView.getText().toString();

        if (question != null) {
            if (selectedAns.equals(question.getAnswer())) {
                correctAns++;
                textView.setBackground(getResources().getDrawable(R.drawable.option_right));
            } else {
                shoeAns();
                textView.setBackground(getResources().getDrawable(R.drawable.option_wrong));
            }
        }
    }
    void reset(){
        binding.option1.setBackground(getResources().getDrawable(R.drawable.option_unselected));
        binding.option2.setBackground(getResources().getDrawable(R.drawable.option_unselected));
        binding.option3.setBackground(getResources().getDrawable(R.drawable.option_unselected));
        binding.option4.setBackground(getResources().getDrawable(R.drawable.option_unselected));
    }
    public void onClick(View view){
        switch (view.getId()){
            case R.id.option_1:
            case R.id.option_2:
            case R.id.option_3:
            case R.id.option_4:
                if (timer!=null)
                    timer.cancel();
                TextView selected=(TextView) view;
                checkAns(selected);
                break;
            case R.id.nextBtn:
                reset();
                index++;
                if (index<questions.size()) {
                    setNextQuestion();
                } else{
                    Intent intent=new Intent(QuizActivity.this,ResultActivity.class);
                    intent.putExtra("correct",correctAns);
                    intent.putExtra("total",questions.size());
                    startActivity(intent);
                    finish();
                    //Toast.makeText(this, "Quiz ended", Toast.LENGTH_SHORT).show();
                }
                break;
        }

    }
}