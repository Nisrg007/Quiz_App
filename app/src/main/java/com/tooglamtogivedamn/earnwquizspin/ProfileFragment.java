package com.tooglamtogivedamn.earnwquizspin;

import android.app.Activity;
import android.content.Intent;
import android.graphics.Bitmap;
import android.net.Uri;
import android.os.Bundle;
import android.provider.MediaStore;
import android.text.InputType;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;

import com.bumptech.glide.Glide;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;
import com.google.firebase.firestore.DocumentReference;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.StorageReference;
import com.google.firebase.storage.UploadTask;

import java.io.ByteArrayOutputStream;
import java.io.IOException;

import de.hdodenhof.circleimageview.CircleImageView;


public class ProfileFragment extends Fragment {

    private static final int PICK_IMAGE_REQUEST = 1;
    private StorageReference storageReference;
    private DatabaseReference databaseReference;
    private CircleImageView profileImage;
    private Uri imageUri;
    private FirebaseAuth auth;
    private FirebaseDatabase database;
    private FirebaseStorage storage;
    private ImageView selectImageButton;
    private EditText nameEditText, passEditText;
    private Button updateProfileButton;
    private FirebaseFirestore mFirestore;
    private FirebaseUser user;

    public ProfileFragment() {
        // Required empty public constructor
    }


    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

    }


    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {

        View view = inflater.inflate(R.layout.fragment_profile, container, false);


        auth = FirebaseAuth.getInstance();
        mFirestore = FirebaseFirestore.getInstance();
        database = FirebaseDatabase.getInstance();
        storage = FirebaseStorage.getInstance();

        nameEditText = view.findViewById(R.id.etUserNameSettings);
        passEditText = view.findViewById(R.id.passBox);
        updateProfileButton = view.findViewById(R.id.updateBtn);

        profileImage = view.findViewById(R.id.profile_image);
        selectImageButton = view.findViewById(R.id.plusImage);
        // Set the inputType of passEditText to "textPassword"
        passEditText.setInputType(InputType.TYPE_CLASS_TEXT | InputType.TYPE_TEXT_VARIATION_PASSWORD);

        user = auth.getCurrentUser();
        if (user != null) {
            String userId = user.getUid();
            DocumentReference userRef = mFirestore.collection("users").document(userId);
            userRef.get().addOnCompleteListener(new OnCompleteListener<DocumentSnapshot>() {
                @Override
                public void onComplete(@NonNull Task<DocumentSnapshot> task) {
                    if (task.isSuccessful()) {
                        DocumentSnapshot document = task.getResult();
                        if (document.exists()) {
                            String name = document.getString("name");
                            String email = document.getString("email");
                            String pass = document.getString("pass");

                            // Set the retrieved data to the corresponding EditText views
                            nameEditText.setText(name);
                            passEditText.setText(pass);
                        }
                    } else {
                        Toast.makeText(getActivity(), "Failed to retrieve user data", Toast.LENGTH_SHORT).show();
                    }
                }
            });
        }

        updateProfileButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                updateProfile();
            }
        });

        // Initialize Firebase
        storage = FirebaseStorage.getInstance();
        storageReference = storage.getReference();
        databaseReference = FirebaseDatabase.getInstance().getReference().child("users");

        selectImageButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                openGallery();
            }
        });

        return view;
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        // Load the profile photo using Glide inside onViewCreated()
        String userId = user.getUid(); // You may use the user's unique ID here

        // Check if the Fragment is attached and has a valid Activity
        if (isAdded() && getActivity() != null) {
            databaseReference.child(userId).child("profileImageUrl").addListenerForSingleValueEvent(new ValueEventListener() {
                @Override
                public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                    String profileImageUrl = dataSnapshot.getValue(String.class);
                    if (profileImageUrl != null) {
                        // Check again if the Fragment is still attached before loading the image
                        if (isAdded() && getActivity() != null) {
                            Glide.with(getActivity())
                                    .load(profileImageUrl)
                                    .into(profileImage);
                        }
                    }
                }

                @Override
                public void onCancelled(@NonNull DatabaseError databaseError) {
                    // Handle the error if necessary
                }
            });
        }
    }

    private void openGallery() {

        Intent intent = new Intent(Intent.ACTION_PICK, MediaStore.Images.Media.EXTERNAL_CONTENT_URI);
        startActivityForResult(intent, PICK_IMAGE_REQUEST);

    }

    @Override
    public void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);

        if (requestCode == PICK_IMAGE_REQUEST && resultCode == Activity.RESULT_OK && data != null && data.getData() != null) {
            String mediaType = requireActivity().getContentResolver().getType(data.getData());
            if (mediaType != null && mediaType.startsWith("image/")) {
                imageUri = data.getData();

                try {
                    Bitmap bitmap = MediaStore.Images.Media.getBitmap(requireActivity().getContentResolver(), imageUri);
                    profileImage.setImageBitmap(bitmap);
                    uploadImage(bitmap);
                } catch (IOException e) {
                    e.printStackTrace();
                    Toast.makeText(requireContext(), "Failed to load image", Toast.LENGTH_SHORT).show();

                }
            } else {
                // The selected media is not an image, show an error message to the user
                Toast.makeText(getContext(), "Please select an image instead of a video.", Toast.LENGTH_LONG).show();
            }
        }
    }


    private void uploadImage(Bitmap bitmap) {
        // Convert the bitmap to a byte array to upload to Firebase Storage
        ByteArrayOutputStream baos = new ByteArrayOutputStream();
        bitmap.compress(Bitmap.CompressFormat.JPEG, 100, baos);
        byte[] data = baos.toByteArray();

        String userId = user.getUid(); // You may use the user's unique ID here
        String imageName = "profile_image.jpg";

        StorageReference imageRef = storageReference.child("images").child(userId).child(imageName);

        // Upload the image to Firebase Storage
        UploadTask uploadTask = imageRef.putBytes(data);
        uploadTask.addOnFailureListener(e -> Toast.makeText(getActivity(), "Upload failed!", Toast.LENGTH_SHORT).show())
                .addOnSuccessListener(taskSnapshot -> {
                    // Image uploaded successfully
                    // Get the download URL and store it in Firebase Database
                    imageRef.getDownloadUrl().addOnSuccessListener(uri -> {
                        // Save the download URL in Firebase Database
                        String downloadUrl = uri.toString();
                        saveImageUrlToDatabase(downloadUrl);
                    });
                });
    }

    private void saveImageUrlToDatabase(String downloadUrl) {
        String userId = user.getUid(); // You may use the user's unique ID here
        databaseReference.child(userId).child("profileImageUrl").setValue(downloadUrl);
    }


    private void updateProfile() {

        FirebaseUser user = auth.getCurrentUser();
        if (user != null) {
            String userId = user.getUid();
            String name = nameEditText.getText().toString().trim();
            String pass = passEditText.getText().toString().trim();


//            if (email.isEmpty()){
//                emailEditText.setError("Enter new Email");
//                return;
//            }  if (pass.isEmpty()){
//                passEditText.setError("Enter new Password");
//                return;
//            }

            if (!name.isEmpty()) {
                DocumentReference userRef = mFirestore.collection("users").document(userId);
                userRef.update("name", name)
                        .addOnCompleteListener(new OnCompleteListener<Void>() {
                            @Override
                            public void onComplete(@NonNull Task<Void> task) {
                                if (task.isSuccessful()) {
                                    Toast.makeText(getActivity(), "Profile updated", Toast.LENGTH_SHORT).show();
                                } else {
                                    Toast.makeText(getActivity(), "Failed to update name", Toast.LENGTH_SHORT).show();
                                }
                            }
                        });
            }

            if (!pass.isEmpty()) {
                DocumentReference userRef = mFirestore.collection("users").document(userId);
                userRef.update("pass", pass)
                        .addOnCompleteListener(new OnCompleteListener<Void>() {
                            @Override
                            public void onComplete(@NonNull Task<Void> task) {
                                if (task.isSuccessful()) {
                                    Toast.makeText(getActivity(), "Password updated successfully", Toast.LENGTH_SHORT).show();
                                } else {
                                    Toast.makeText(getActivity(), "Failed to update Password", Toast.LENGTH_SHORT).show();
                                }
                            }
                        });
            }
        }
    }
}