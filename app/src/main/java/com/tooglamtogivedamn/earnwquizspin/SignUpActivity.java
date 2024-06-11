package com.tooglamtogivedamn.earnwquizspin;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;

import android.app.ProgressDialog;
import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Toast;

import com.google.android.gms.auth.api.signin.GoogleSignIn;
import com.google.android.gms.auth.api.signin.GoogleSignInAccount;
import com.google.android.gms.auth.api.signin.GoogleSignInClient;
import com.google.android.gms.auth.api.signin.GoogleSignInOptions;
import com.google.android.gms.common.api.ApiException;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.AuthCredential;
import com.google.firebase.auth.AuthResult;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.auth.GoogleAuthProvider;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.firestore.DocumentReference;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FirebaseFirestore;
import com.tooglamtogivedamn.earnwquizspin.databinding.ActivitySignUpBinding;

public class SignUpActivity extends AppCompatActivity {
    ActivitySignUpBinding binding;
    FirebaseAuth auth;
    FirebaseFirestore database;

    FirebaseDatabase mDatabase;
    DatabaseReference reference;

    ProgressDialog progressDialog;

    GoogleSignInClient mGoogleSignInClient;
    Button googleBtn;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        binding=ActivitySignUpBinding.inflate(getLayoutInflater());
        View view=binding.getRoot();
        setContentView(view);
        auth=FirebaseAuth.getInstance();
        database=FirebaseFirestore.getInstance();
        //configure google signin
        GoogleSignInOptions gso=new GoogleSignInOptions.Builder(GoogleSignInOptions.DEFAULT_SIGN_IN)
                .requestIdToken(getString(R.string.default_web_client_id))
                .requestEmail().build();
        mGoogleSignInClient = GoogleSignIn.getClient(this,gso);

        googleBtn=findViewById(R.id.btn_google);



        progressDialog = new ProgressDialog(SignUpActivity.this);
        progressDialog.setTitle("Creating Account");
        progressDialog.setMessage("We're creating your account");

        binding.btnSignUp.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                mDatabase=FirebaseDatabase.getInstance();

                String  email,pass,name;
                email=binding.emailBox.getText().toString().trim();
                pass=binding.passBox.getText().toString().trim();
                name=binding.etUserNameSettings.getText().toString().trim();

                if (email.isEmpty()){
                    binding.emailBox.setError("Enter your Email");
                    return;
                }  if (pass.isEmpty()){
                    binding.passBox.setError("Enter your Password");
                    return;
                }
                if (name.isEmpty()){
                    binding.etUserNameSettings.setError("Enter your Name");
                    return;
                }
                progressDialog.show();
                User user=new User(name,email,pass);

                auth.createUserWithEmailAndPassword(email,pass).addOnCompleteListener(new OnCompleteListener<AuthResult>() {
                    @Override
                    public void onComplete(@NonNull Task<AuthResult> task) {
                        progressDialog.dismiss();

                        if (task.isSuccessful()){
                            String uid=task.getResult().getUser().getUid();
                            String userId=auth.getCurrentUser().getUid();

                            database.collection("users")
                                    .document(userId)
                                    .set(user).addOnCompleteListener(new OnCompleteListener<Void>() {
                                        @Override
                                        public void onComplete(@NonNull Task<Void> task) {
                                            if(task.isSuccessful()){
                                                startActivity(new Intent(SignUpActivity.this,MainActivity.class));
                                                finish();
                                            }else {
                                                Toast.makeText(SignUpActivity.this, task.getException().getLocalizedMessage(), Toast.LENGTH_SHORT).show();
                                            }
                                        }
                                    });
                        }else {
                            Toast.makeText(SignUpActivity.this, task.getException().getLocalizedMessage(), Toast.LENGTH_SHORT).show();
                        }
                    }
                });
            }
        });




        binding.alreadyAccount.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                startActivity(new Intent(SignUpActivity.this,LoginActivity.class));

            }
        });

        googleBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                signIn();
            }
        });
    }
    int RC_SIGN_IN=65;
    private void signIn(){
        Intent signInIntent=mGoogleSignInClient.getSignInIntent();
        startActivityForResult(signInIntent,RC_SIGN_IN);
    }

    @Override
    public void onActivityResult(int requestCode,int resultCode,Intent data){
        super.onActivityResult(requestCode,resultCode,data);

        if (requestCode==RC_SIGN_IN){
            Task<GoogleSignInAccount> task= GoogleSignIn.getSignedInAccountFromIntent(data);
            try {
                GoogleSignInAccount account=task.getResult(ApiException.class);

                Log.d("TAG","firebaseAuthWithGoogle:"+account.getId());
                firebaseAuthWithGoogle(account.getIdToken());
            }
            catch (ApiException e) {
                Log.w("TAG","Google_sign_in_failed",e);
            }
        }
    }
    private void firebaseAuthWithGoogle(String idToken) {
        database=FirebaseFirestore.getInstance();
        String email, pass, name;
        email = binding.emailBox.getText().toString().trim();
        pass = binding.passBox.getText().toString().trim();
        name = binding.etUserNameSettings.getText().toString().trim();

        AuthCredential credential = GoogleAuthProvider.getCredential(idToken, null);
        auth.signInWithCredential(credential)
                .addOnCompleteListener(this, task -> {
                    if (task.isSuccessful()) {
                        Log.d("TAG", "signInWithCredential:success");
                        FirebaseUser user = auth.getCurrentUser();
                        if (user != null) {
                            // Create a new user document in Firestore
                            User users = new User(name, email, pass);
                            users.setEmail(user.getEmail());
                            users.setName(user.getDisplayName());

                            // Set the document with the user's UID as the document ID
                            database.collection("users").document(user.getUid()).set(users)
                                    .addOnSuccessListener(aVoid -> {
                                        Log.d("TAG", "DocumentSnapshot successfully written!");
                                        // Proceed to the main activity or any other action
                                        Intent intent = new Intent(SignUpActivity.this, MainActivity.class);
                                        startActivity(intent);
                                        Toast.makeText(SignUpActivity.this, "Sign Up With Google", Toast.LENGTH_SHORT).show();
                                    finish();
                                    })
                                    .addOnFailureListener(e -> {
                                        Log.w("TAG", "Error writing document", e);
                                        // Handle the error if necessary
                                    });
                        }
                    } else {
                        Log.w("TAG", "signInWithCredential:failure", task.getException());
                        // Handle the authentication failure if necessary
                    }
                });
    }


}