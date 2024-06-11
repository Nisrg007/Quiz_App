package com.tooglamtogivedamn.earnwquizspin;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;

import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.Toast;

import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.FirebaseAuth;
import com.tooglamtogivedamn.earnwquizspin.databinding.ActivityForgetPassBinding;
import com.tooglamtogivedamn.earnwquizspin.databinding.ActivityLoginBinding;

public class ForgetPass extends AppCompatActivity {
    ActivityForgetPassBinding binding;
    FirebaseAuth auth;
    private String email;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        binding= ActivityForgetPassBinding.inflate(getLayoutInflater());
        View view=binding.getRoot();
        setContentView(view);

        auth=FirebaseAuth.getInstance();

        binding.btnLogin.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                validateData();
            }
        });

        binding.forgetPass.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                startActivity(new Intent(ForgetPass.this,LoginActivity.class));
            }
        });
    }

    private void validateData() {
        email=binding.emailBox.getText().toString();

        if (email.isEmpty()){
            binding.emailBox.setError("Enter the email");

        }else{
            forgetPass();
        }
    }

    private void forgetPass() {
        auth.sendPasswordResetEmail(email)
                .addOnCompleteListener(new OnCompleteListener<Void>() {
                    @Override
                    public void onComplete(@NonNull Task<Void> task) {
                        if (task.isSuccessful()){
                            Toast.makeText(ForgetPass.this, "Check your email", Toast.LENGTH_SHORT).show();
                            startActivity(new Intent(ForgetPass.this,LoginActivity.class));
                            finish();
                        }else {
                            Toast.makeText(ForgetPass.this, task.getException().getMessage(), Toast.LENGTH_SHORT).show();
                        }
                    }
                });

    }
}