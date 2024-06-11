package com.tooglamtogivedamn.earnwquizspin;

import android.os.Bundle;

import androidx.fragment.app.Fragment;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Toast;

import com.google.android.gms.tasks.OnSuccessListener;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FirebaseFirestore;
import com.tooglamtogivedamn.earnwquizspin.databinding.FragmentWalletBinding;

public class WalletFragment extends Fragment {


    public WalletFragment() {
        // Required empty public constructor

    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
super.onCreate(savedInstanceState);
    }

    FragmentWalletBinding binding;
    FirebaseFirestore database;
    User user;
    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment
       binding=FragmentWalletBinding.inflate(inflater,container,false);
       database=FirebaseFirestore.getInstance();

       database.collection("users")
               .document(FirebaseAuth.getInstance().getUid())
               .get().addOnSuccessListener(new OnSuccessListener<DocumentSnapshot>() {
                   @Override
                   public void onSuccess(DocumentSnapshot documentSnapshot) {
                       user=documentSnapshot.toObject(User.class);
                        binding.currentCoins.setText(String.valueOf(user.getCoins()));

                   }
               });

binding.ppsendRequest.setOnClickListener(new View.OnClickListener() {
    @Override
    public void onClick(View v) {
        if (user.getCoins()>50000){
            String uid=FirebaseAuth.getInstance().getUid();
            String payPal=binding.emailBox.getText().toString();
            WithdrawRequest request=new WithdrawRequest(uid,payPal,user.getName());
            database.collection("withdraws")
                    .document()
                    .set(request).addOnSuccessListener(new OnSuccessListener<Void>() {
                        @Override
                        public void onSuccess(Void unused) {
                            Toast.makeText(getContext(), "Request sent!", Toast.LENGTH_SHORT).show();
                        }
                    });
        }else {
            Toast.makeText(getContext(), "insufficient coins", Toast.LENGTH_SHORT).show();
        }
    }
});
        binding.gpsendRequest.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (user.getCoins()>50000){
                    String uid=FirebaseAuth.getInstance().getUid();
                    String gpay=binding.googleEmailBox.getText().toString();
                    WithdrawRequest requestGpay=new WithdrawRequest(uid,gpay,user.getName());
                    database.collection("withdraws")
                            .document()
                            .set(requestGpay).addOnSuccessListener(new OnSuccessListener<Void>() {
                                @Override
                                public void onSuccess(Void unused) {
                                    Toast.makeText(getContext(), "Request sent Gpay!", Toast.LENGTH_SHORT).show();
                                }
                            });

                }else {
                    Toast.makeText(getContext(), "insufficient coins", Toast.LENGTH_SHORT).show();
                }
            }
        });




return binding.getRoot();
    }
}