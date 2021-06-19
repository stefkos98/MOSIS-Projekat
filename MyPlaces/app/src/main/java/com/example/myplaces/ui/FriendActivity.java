package com.example.myplaces.ui;

import android.app.Activity;
import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.drawable.BitmapDrawable;
import android.graphics.drawable.Drawable;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;

import com.example.myplaces.R;
import com.example.myplaces.models.MyPlace;
import com.example.myplaces.models.MyPlacesData;
import com.example.myplaces.models.User;
import com.example.myplaces.models.UsersData;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.StorageReference;

public class FriendActivity extends AppCompatActivity {
    TextView txtFirstName, txtLastName, txtUsername, txtPhone, txtEmail, txtRank, txtScore;
    Button btnUnfriend;
    ImageView image;

    FirebaseAuth mAuth;
    StorageReference storageRef, pictureRef;
    DatabaseReference database;
    String uid;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_friend);
        setSupportActionBar(findViewById(R.id.toolbar));
        getSupportActionBar().setIcon(R.drawable.mosis_logo_tekst);
        getSupportActionBar().setDisplayShowTitleEnabled(false);
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        getSupportActionBar().setDisplayShowHomeEnabled(true);
        mAuth = FirebaseAuth.getInstance();
        storageRef = FirebaseStorage.getInstance().getReference();
        database = FirebaseDatabase.getInstance().getReference();
        FirebaseUser userl = FirebaseAuth.getInstance().getCurrentUser();
        uid = userl.getUid();
        image = findViewById(R.id.imageViewF);

        txtUsername = findViewById(R.id.textViewUsernameF);
        txtFirstName = findViewById(R.id.textViewFirstNameF);
        txtLastName = findViewById(R.id.textViewLastNameF);
        txtPhone = findViewById(R.id.textViewPhoneNumberF);
        txtEmail = findViewById(R.id.textViewEmailF);
        txtScore = findViewById(R.id.textViewScoreF);

        btnUnfriend = findViewById(R.id.btnUnfriendF);

        int position = -1;
        try {
            Intent listIntent = getIntent();
            Bundle positionBundle = listIntent.getExtras();
            position = positionBundle.getInt("position");
            if (position >= 0) {
                User user = UsersData.getInstance().getUser(position);
                txtUsername.setText(user.username);
                txtFirstName.setText(user.firstName);
                txtLastName.setText(user.lastName);
                txtPhone.setText(user.phone);
                txtEmail.setText(user.email);
                txtScore.setText(user.points.toString());
                final long ONE_MEGABYTE = 1024 * 1024;
                storageRef.child("images").child(user.email + ".jpg").getBytes(ONE_MEGABYTE).addOnSuccessListener(new OnSuccessListener<byte[]>() {
                    @Override
                    public void onSuccess(byte[] bytes) {
                        Drawable im = new BitmapDrawable(getResources(), BitmapFactory.decodeByteArray(bytes, 0, bytes.length));
                        image.setBackground(im);
                    }
                }).addOnFailureListener(new OnFailureListener() {
                    @Override
                    public void onFailure(@NonNull Exception exception) {
                        // Handle any errors
                    }
                });
                btnUnfriend.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {
                        database.child("users").child(uid).child("friends").child(user.key).removeValue().addOnCompleteListener(new OnCompleteListener<Void>() {
                            @Override
                            public void onComplete(@NonNull  Task<Void> task) {
                                database.child("users").child(user.key).child("friends").child(uid).removeValue().addOnCompleteListener(new OnCompleteListener<Void>() {
                                    @Override
                                    public void onComplete(@NonNull Task<Void> task) {
                                        finish();
                                    }
                                });

                            }
                        });
                    }

                });
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
    }
}
