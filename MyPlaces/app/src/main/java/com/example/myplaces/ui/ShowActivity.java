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
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.StorageReference;

public class ShowActivity extends AppCompatActivity {
    DatabaseReference database;
    StorageReference storage;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        database = FirebaseDatabase.getInstance().getReference();
        storage = FirebaseStorage.getInstance().getReference();
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_show);
        getSupportActionBar().setIcon(R.drawable.mosis_logo_tekst);
        getSupportActionBar().setDisplayShowTitleEnabled(false);
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        getSupportActionBar().setDisplayShowHomeEnabled(true);
        int position = -1;
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
                        findViewById(R.id.imageViewS).setBackground(image);
                        // Data for "images/island.jpg" is returns, use this as needed
                    }
                }).addOnFailureListener(new OnFailureListener() {
                    @Override
                    public void onFailure(@NonNull Exception exception) {
                        // Handle any errors
                    }
                });
                TextView txt1, txt2, txt3, txt4, txt5;
                CheckBox check1, check2, check3, check4, check5;
                txt1 = findViewById(R.id.editTextFirstNameS);
                txt2 = findViewById(R.id.editTextLastNameS);
                txt3 = findViewById(R.id.editTextPhoneS);
                txt4 = findViewById(R.id.editTextDescribeProblemS);
                txt5 = findViewById(R.id.editTextTypeOfAnymalS);
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
                check1 = findViewById(R.id.checkBoxFoodS);
                check2 = findViewById(R.id.checkBoxMedicineS);
                check3 = findViewById(R.id.checkBoxWaterS);
                check4 = findViewById(R.id.checkBoxVetS);
                check5 = findViewById(R.id.checkBoxAdoptionS);
                check1.setEnabled(false);
                check2.setEnabled(false);
                check3.setEnabled(false);
                check4.setEnabled(false);
                check5.setEnabled(false);
                if (place.food) {
                    check1.setChecked(true);
                }
                if (place.medicine) {
                    check2.setChecked(true);
                }
                if (place.water) {
                    check3.setChecked(true);
                }
                if (place.vet) {
                    check4.setChecked(true);
                }
                if (place.adoption) {
                    check5.setChecked(true);
                }
            }
        } catch (Exception e) {
            Toast.makeText(this, e.getMessage(), Toast.LENGTH_SHORT).show();
            finish();
        }

        final Button finishedButton = (Button) findViewById(R.id.btnCloseS);
        finishedButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                finish();
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