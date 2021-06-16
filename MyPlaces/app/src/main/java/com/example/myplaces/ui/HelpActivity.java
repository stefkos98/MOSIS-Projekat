package com.example.myplaces.ui;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.graphics.BitmapFactory;
import android.graphics.drawable.BitmapDrawable;
import android.graphics.drawable.Drawable;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.CheckBox;
import android.widget.TextView;
import android.widget.Toast;

import com.example.myplaces.R;
import com.example.myplaces.models.MyPlace;
import com.example.myplaces.models.MyPlacesData;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.StorageReference;

import java.security.AuthProvider;

public class HelpActivity extends AppCompatActivity {
    DatabaseReference database;
    StorageReference storage;
    FirebaseAuth mAuth;
    FirebaseUser user;
    int position = -1;
    TextView txt1, txt2, txt3, txt4, txt5;
    CheckBox check1, check2, check3, check4, check5;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        mAuth=FirebaseAuth.getInstance();
        user=mAuth.getCurrentUser();
        database = FirebaseDatabase.getInstance().getReference();
        storage = FirebaseStorage.getInstance().getReference();
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_help);
        getSupportActionBar().setIcon(R.drawable.mosis_logo_tekst);
        getSupportActionBar().setDisplayShowTitleEnabled(false);
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        getSupportActionBar().setDisplayShowHomeEnabled(true);
        try {
            Intent listIntent = getIntent();
            Bundle positionBundle = listIntent.getExtras();
            position = positionBundle.getInt("position");
            final long ONE_MEGABYTE = 1024 * 1024;
            if (position >= 0) {
                MyPlace place = MyPlacesData.getInstance().getPlace(position);
                storage.child("animalimages").child(place.key + ".jpg").getBytes(ONE_MEGABYTE).addOnSuccessListener(new OnSuccessListener<byte[]>() {
                    @Override
                    public void onSuccess(byte[] bytes) {
                        Drawable image = new BitmapDrawable(getResources(), BitmapFactory.decodeByteArray(bytes, 0, bytes.length));
                        findViewById(R.id.imageViewHelp).setBackground(image);
                        // Data for "images/island.jpg" is returns, use this as needed
                    }
                }).addOnFailureListener(new OnFailureListener() {
                    @Override
                    public void onFailure(@NonNull Exception exception) {
                        // Handle any errors
                    }
                });
                txt1 = findViewById(R.id.editTextFirstNameHelp);
                txt2 = findViewById(R.id.editTextLastNameHelp);
                txt3 = findViewById(R.id.editTextPhoneHelp);
                txt4 = findViewById(R.id.editTextDescribeProblemHelp);
                txt5 = findViewById(R.id.editTextTypeOfAnymalHelp);
                txt1.setEnabled(false);
                txt2.setEnabled(false);
                txt3.setEnabled(false);
                txt4.setEnabled(false);
                txt5.setEnabled(false);
                txt1.setText(place.firstName);
                txt2.setText(place.lastName);
                txt3.setText(place.phone);
                txt4.setText(place.description);
                txt5.setText(place.animalType);
                check1 = findViewById(R.id.checkBoxFoodHelp);
                check2 = findViewById(R.id.checkBoxMedicineHelp);
                check3 = findViewById(R.id.checkBoxWaterHelp);
                check4 = findViewById(R.id.checkBoxVetHelp);
                check5 = findViewById(R.id.checkBoxAdoptionHelp);
                if (place.food) {
                    check1.setChecked(false);
                }
                else{
                    check1.setEnabled(false);
                }
                if (place.medicine) {
                    check2.setChecked(false);
                } else{
                    check2.setEnabled(false);
                }
                if (place.water) {
                    check3.setChecked(false);
                } else{
                    check3.setEnabled(false);
                }
                if (place.vet) {
                    check4.setChecked(false);
                } else{
                    check4.setEnabled(false);
                }
                if (place.adoption) {
                    check5.setChecked(false);
                } else{
                    check5.setEnabled(false);
                }
            }
        } catch (Exception e) {
            Toast.makeText(this, e.getMessage(), Toast.LENGTH_SHORT).show();
            finish();
        }

        final Button finishedButton = (Button) findViewById(R.id.btnIHelpedHelp);
        finishedButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                //not finished
                finish();
            }
        });
        final Button finishedButton2 = (Button) findViewById(R.id.btnNotThereHelp);
        finishedButton2.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                database.child("users").child(user.getUid()).child("points").get().addOnCompleteListener(new OnCompleteListener<DataSnapshot>() {
                    @Override
                    public void onComplete(@NonNull  Task<DataSnapshot> task) {
                       Long point=(Long) task.getResult().getValue();
                        database.child("users").child(user.getUid()).child("points").setValue(point+1);
                        MyPlacesData.getInstance().deletePlace(position);
                        finish();
                    }
                });
            }
        });
        IntentFilter intentFilter = new IntentFilter();
        intentFilter.addAction("com.package.ACTION_LOGOUT");

        registerReceiver(new BroadcastReceiver() {
            @Override
            public void onReceive(Context context, Intent intent) {
                Log.d("onReceive", "Logout in progress");
                //At this point you should start the login activity and finish this one
                finish();
            }
        }, intentFilter);
    }

    @Override
    public boolean onSupportNavigateUp() {
        onBackPressed();
        return true;
    }
}