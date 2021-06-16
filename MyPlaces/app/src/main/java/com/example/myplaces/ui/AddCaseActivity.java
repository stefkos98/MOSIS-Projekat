package com.example.myplaces.ui;

import android.Manifest;
import android.app.Activity;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.pm.PackageManager;
import android.graphics.Bitmap;
import android.graphics.drawable.Drawable;
import android.location.Location;
import android.location.LocationListener;
import android.location.LocationManager;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;

import com.example.myplaces.R;
import com.example.myplaces.models.MyPlace;
import com.example.myplaces.models.MyPlacesData;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.AuthResult;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.StorageReference;
import com.google.firebase.storage.UploadTask;

import androidx.annotation.NonNull;
import androidx.annotation.RequiresApi;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;
import androidx.core.app.ActivityCompat;

import android.provider.MediaStore;
import android.text.Editable;
import android.text.TextWatcher;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.CheckBox;
import android.widget.EditText;
import android.widget.Spinner;
import android.widget.Toast;

import java.io.ByteArrayOutputStream;
import java.util.UUID;

public class AddCaseActivity extends AppCompatActivity implements AdapterView.OnItemSelectedListener {
    boolean editMode = true;
    String latitude="0", longitude="0";
    String first = "", last = "", phone = "", uid;
    String animalType;
    FirebaseAuth mAuth;
    DatabaseReference database;
    FirebaseUser mUser;
    private Bitmap imageBitmap;
    private Task<Uri> outputFileUri;
    private StorageReference storageRef;
    private StorageReference pictureRef;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        mAuth = FirebaseAuth.getInstance();
        database = FirebaseDatabase.getInstance().getReference();
        storageRef = FirebaseStorage.getInstance().getReference();
        mUser = mAuth.getCurrentUser();
        uid = mUser.getUid();
        database.child("users").child(uid).get().addOnCompleteListener(new OnCompleteListener<DataSnapshot>() {
            @Override
            public void onComplete(@NonNull Task<DataSnapshot> task) {
                first = task.getResult().child("firstName").getValue().toString();
                last = task.getResult().child("lastName").getValue().toString();
                phone = task.getResult().child("phone").getValue().toString();
            }
        });
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_add_case);
        Toolbar toolbar = findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);
        getSupportActionBar().setIcon(R.drawable.mosis_logo_tekst);
        getSupportActionBar().setDisplayShowTitleEnabled(false);
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        getSupportActionBar().setDisplayShowHomeEnabled(true);
        Intent listIntent = getIntent();
        Bundle positionBundle = listIntent.getExtras();
        if (positionBundle != null) {
            latitude = Double.toString(positionBundle.getDouble("latitude"));
            longitude = Double.toString(positionBundle.getDouble("longitude"));
            Log.d("onReceive", latitude + " "+longitude);
        }

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
        //** snip **//
        findViewById(R.id.btnMakeAnAddAC).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                EditText text = findViewById(R.id.editTextDescribeProblemAC);
                String description = text.getText().toString();
                CheckBox cfood = findViewById(R.id.checkBoxFoodAC);
                CheckBox cmedicine = findViewById(R.id.checkBoxMedicineAC);
                CheckBox cwater = findViewById(R.id.checkBoxWaterAC);
                CheckBox cvet = findViewById(R.id.checkBoxVetAC);
                CheckBox cadoption = findViewById(R.id.checkBoxAdoptionAC);

                Boolean food = cfood.isChecked(), medicine = cmedicine.isChecked(), water = cwater.isChecked(),
                        vet = cvet.isChecked(), adoption = cadoption.isChecked();
                MyPlace place = new MyPlace(animalType, description, longitude, latitude, "picture", food, medicine, water, vet, adoption, first, last, phone,mAuth.getCurrentUser().getUid());
                MyPlacesData.getInstance().addNewPlace(place);
                // UPLOAD SLIKE
                ByteArrayOutputStream baos = new ByteArrayOutputStream();
                imageBitmap.compress(Bitmap.CompressFormat.JPEG, 100, baos);
                byte[] data = baos.toByteArray();
                pictureRef = storageRef.child("animalimages/" + place.key + ".jpg");
                UploadTask uploadTask = pictureRef.putBytes(data);
                uploadTask.addOnFailureListener(new OnFailureListener() {
                    @Override
                    public void onFailure(@NonNull Exception exception) {
                        Toast.makeText(getApplicationContext(), "There was an error uploading a photo.", Toast.LENGTH_SHORT).show();
                        return;
                    }
                }).addOnSuccessListener(new OnSuccessListener<UploadTask.TaskSnapshot>() {
                    @Override
                    public void onSuccess(UploadTask.TaskSnapshot taskSnapshot) {
                        outputFileUri = pictureRef.getDownloadUrl().addOnSuccessListener(new OnSuccessListener<Uri>() {
                            @Override
                            public void onSuccess(Uri uri) {
                                String picture = uri.toString();
                                database.child("places").child(place.key).child("picture").setValue(picture).addOnCompleteListener(new OnCompleteListener<Void>() {
                                    @Override
                                    public void onComplete(@NonNull Task<Void> task) {
                                        setResult(Activity.RESULT_OK);
                                        finish();
                                    }
                                });

                            }
                        });

                    }

                });
            }
        });
        findViewById(R.id.imageAnimalCreatePhoto).setOnClickListener(new View.OnClickListener() {
                                                                         @RequiresApi(api = Build.VERSION_CODES.GINGERBREAD)
                                                                         @Override
                                                                         public void onClick(View v) {
                                                                             if (ActivityCompat.checkSelfPermission(getApplicationContext(), Manifest.permission.CAMERA) != PackageManager.PERMISSION_GRANTED)
                                                                                 ActivityCompat.requestPermissions(AddCaseActivity.this, new String[]{Manifest.permission.CAMERA}, 101);
                                                                             else {
                                                                                 startActivityForResult(new Intent(MediaStore.ACTION_IMAGE_CAPTURE), 1);
                                                                             }
                                                                         }
                                                                     }
        );

        // SPINNER

        Spinner spinner = (Spinner) findViewById(R.id.animaltype_spinnerAC);
        spinner.setOnItemSelectedListener(this);
        ArrayAdapter<CharSequence> adapter = ArrayAdapter.createFromResource(this,
                R.array.amimals_array, android.R.layout.simple_spinner_item);
        adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        spinner.setAdapter(adapter);
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.menu_add_case, menu);
        return true;
    }

    @Override
    public boolean onSupportNavigateUp() {
        onBackPressed();
        return true;
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, String[] permissions,
                                           int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);
        switch (requestCode) {
            case 101:
                // If request is cancelled, the result arrays are empty.
                if (grantResults.length > 0 &&
                        grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                    startActivityForResult(new Intent(MediaStore.ACTION_IMAGE_CAPTURE), 1);
                } else {
                    Toast.makeText(getApplicationContext(), "Camera access permission denied!", Toast.LENGTH_SHORT).show();

                }
                return;
        }
        // Other 'case' lines to check for other
        // permissions this app might request.
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (requestCode == 1) {
            Uri imageUri = null;
            if (data != null) {
                if (data.hasExtra("data")) {
                    Bundle extras = data.getExtras();
                    imageBitmap = (Bitmap) extras.get("data");
                    findViewById(R.id.imageAnimalCreatePhoto).setBackground((Drawable) getResources().getDrawable(R.drawable.checked));
                } else {
                    Toast.makeText(getApplicationContext(), "There was an error taking a picture", Toast.LENGTH_SHORT).show();
                }
            } else {
                Toast.makeText(getApplicationContext(), "There was an error taking a picture", Toast.LENGTH_SHORT).show();
            }
        }

    }
    public void onItemSelected(AdapterView<?> parent, View view,
                               int pos, long id) {
        animalType= parent.getItemAtPosition(pos).toString();
    }

    public void onNothingSelected(AdapterView<?> parent) {
       animalType=parent.getItemAtPosition(0).toString();
    }
}
