package com.example.myplaces.ui;

import androidx.annotation.Nullable;
import androidx.annotation.RequiresApi;
import androidx.appcompat.app.AppCompatActivity;

import android.Manifest;
import android.app.Activity;
import android.content.pm.PackageManager;
import android.graphics.Bitmap;
import android.graphics.drawable.Drawable;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;

import com.example.myplaces.R;

import androidx.annotation.NonNull;
import androidx.core.app.ActivityCompat;

import android.app.ProgressDialog;
import android.content.Intent;
import android.provider.MediaStore;
import android.util.Log;
import android.util.Patterns;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.Toast;

import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.AuthResult;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseAuthUserCollisionException;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.MutableData;
import com.google.firebase.database.Transaction;
import com.google.firebase.database.ValueEventListener;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.StorageReference;
import com.google.firebase.storage.UploadTask;

import java.io.ByteArrayOutputStream;
import java.io.File;

public class RegisterActivity extends Activity {
    EditText txtFirstName, txtLastName, txtUsername, txtPhone, txtEmail, txtPassword, txtPassword2;
    Button btnRegister;
    ImageView imgCreatePhoto;
    ProgressDialog mDialog;
    FirebaseAuth mAuth;
    FirebaseStorage storage;
    StorageReference storageRef;
    DatabaseReference database;
    private static int TAKE_PICTURE = 100;
    private Task<Uri> outputFileUri;
    private Bitmap imageBitmap;
    private StorageReference pictureRef;
    int taken = 0;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_register);
        txtFirstName = findViewById(R.id.txtFirstNameR);
        txtLastName = findViewById(R.id.txtLastNameR);
        txtUsername = findViewById(R.id.txtUsernameR);
        txtPhone = findViewById(R.id.txtPhoneR);
        txtEmail = findViewById(R.id.txtEmailR);
        txtPassword = findViewById(R.id.txtPasswordR);
        txtPassword2 = findViewById(R.id.txtConfirmPasswordR);
        imgCreatePhoto = findViewById(R.id.imageCreatePhoto);
        btnRegister = findViewById(R.id.btnRegisterR);
        mAuth = FirebaseAuth.getInstance();
        storage = FirebaseStorage.getInstance();
        storageRef = storage.getReference();
        database = FirebaseDatabase.getInstance().getReference();
        imgCreatePhoto.setOnClickListener(new View.OnClickListener() {
                                              @RequiresApi(api = Build.VERSION_CODES.GINGERBREAD)
                                              @Override
                                              public void onClick(View v) {
                                                  if (ActivityCompat.checkSelfPermission(getApplicationContext(), Manifest.permission.CAMERA) != PackageManager.PERMISSION_GRANTED)
                                                      ActivityCompat.requestPermissions(RegisterActivity.this, new String[]{Manifest.permission.CAMERA}, 101);
                                                  else {
                                                      startActivityForResult(new Intent(MediaStore.ACTION_IMAGE_CAPTURE), TAKE_PICTURE);
                                                  }
                                              }
                                          }
        );
        btnRegister.setOnClickListener(new View.OnClickListener() {
            @RequiresApi(api = Build.VERSION_CODES.GINGERBREAD)
            @Override
            public void onClick(View v) {
                registerUser();
            }
        });
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, String[] permissions,
                                           int[] grantResults) {
        switch (requestCode) {
            case 101:
                // If request is cancelled, the result arrays are empty.
                if (grantResults.length > 0 &&
                        grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                    startActivityForResult(new Intent(MediaStore.ACTION_IMAGE_CAPTURE), TAKE_PICTURE);
                } else {
                    Toast.makeText(getApplicationContext(), "Camera access permission denied!", Toast.LENGTH_SHORT).show();

                }
                return;
        }
        // Other 'case' lines to check for other
        // permissions this app might request.
    }

    @RequiresApi(api = Build.VERSION_CODES.GINGERBREAD)
    public void registerUser() {
        mDialog = new ProgressDialog(RegisterActivity.this);
        mDialog.setMessage("Loading Registration...");
        mDialog.show();
        final String email = txtEmail.getText().toString().trim();
        final String password = txtPassword.getText().toString().trim();
        final String firstName = txtFirstName.getText().toString().trim();
        final String lastName = txtLastName.getText().toString().trim();
        final String username = txtUsername.getText().toString().trim();
        final String phone = txtPhone.getText().toString().trim();
        final String password2 = txtPassword2.getText().toString().trim();
        if (checkInputError(email, password, password2)) {
            database.child("usernames").child(username).get().addOnCompleteListener(new OnCompleteListener<DataSnapshot>() {
                @Override
                public void onComplete(@NonNull Task<DataSnapshot> task) {
                    if (!task.isSuccessful()) {
                        Log.e("firebase", "Error getting data", task.getException());
                    } else {
                        String result = String.valueOf(task.getResult().getValue());
                        if (result == "null") {
                            taken = 1;
                            registerContinue();
                        } else if (taken == 1) {
                            registerContinue();
                        } else {
                            mDialog.dismiss();
                            Toast.makeText(getApplicationContext(), "Username already exists!", Toast.LENGTH_SHORT).show();
                        }
                    }
                }
            });

        } else
            mDialog.dismiss();

    }

    public void registerContinue() {
        final String email = txtEmail.getText().toString().trim();
        final String password = txtPassword.getText().toString().trim();
        final String firstName = txtFirstName.getText().toString().trim();
        final String lastName = txtLastName.getText().toString().trim();
        final String username = txtUsername.getText().toString().trim();
        final String phone = txtPhone.getText().toString().trim();
        mAuth.createUserWithEmailAndPassword(email, password).

                addOnCompleteListener(this, new OnCompleteListener<AuthResult>() {
                    @Override
                    public void onComplete(Task<AuthResult> task) {
                        mDialog.dismiss();

                        if (task.isSuccessful()) {
                            // Uploadovanje slike
                            ByteArrayOutputStream baos = new ByteArrayOutputStream();
                            imageBitmap.compress(Bitmap.CompressFormat.JPEG, 100, baos);
                            byte[] data = baos.toByteArray();
                            pictureRef = storageRef.child("images/" + txtEmail.getText().toString() + ".jpg");
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
                                            String imageUrl = uri.toString();
                                            //outputFileUri.toString()
                                            database.child("users").child(mAuth.getUid().toString()).child("picture").setValue(imageUrl);
                                            mAuth.signInWithEmailAndPassword(email, password).addOnCompleteListener(new OnCompleteListener<AuthResult>() {
                                                @Override
                                                public void onComplete(@NonNull Task<AuthResult> task) {
                                                    if (task.isComplete()) {
                                                        sendToMain();
                                                    } else
                                                        Toast.makeText(getApplicationContext(), "There was an error logging in.", Toast.LENGTH_SHORT).show();
                                                }
                                            });
                                        }
                                    });

                                }

                            });
                            // Kraj uploadovanja slike
                            database.child("users").child(mAuth.getUid().toString()).child("points").setValue(0);
                            database.child("users").child(mAuth.getUid().toString()).child("share").setValue(false);
                            database.child("users").child(mAuth.getUid().toString()).child("latitude").setValue("0");
                            database.child("users").child(mAuth.getUid().toString()).child("longitude").setValue("0");
                            database.child("users").child(mAuth.getUid().toString()).child("email").setValue(email);
                            database.child("users").child(mAuth.getUid().toString()).child("username").setValue(username);
                            database.child("users").child(mAuth.getUid().toString()).child("firstName").setValue(firstName);
                            database.child("users").child(mAuth.getUid().toString()).child("lastName").setValue(lastName);
                            database.child("users").child(mAuth.getUid().toString()).child("phone").setValue(phone);
                            database.child("usernames").child(username).setValue(mAuth.getUid().toString());
                            Toast.makeText(getApplicationContext(), "Registration successful!", Toast.LENGTH_SHORT).show();
                            // Ubacivanje dodatnih podataka
                        } else {
                            if (task.getException() instanceof FirebaseAuthUserCollisionException)
                                Toast.makeText(getApplicationContext(), "User with this Email already exists.", Toast.LENGTH_SHORT).show();
                            else
                                Toast.makeText(getApplicationContext(), "There was an error. Try again later.", Toast.LENGTH_SHORT).show();
                        }
                    }
                });
    }

    @RequiresApi(api = Build.VERSION_CODES.GINGERBREAD)
    public boolean checkInputError(String email, String password, String password2) {
        if (email.isEmpty()) {
            txtEmail.setError("Please enter your Email.");
            txtEmail.requestFocus();
            return false;
        }

        if (!Patterns.EMAIL_ADDRESS.matcher(email).matches()) {
            txtEmail.setError("Please enter a valid Email address.");
            txtEmail.requestFocus();
            return false;
        }

        if (password.isEmpty()) {
            txtPassword.setError("Please enter your password.");
            txtPassword.requestFocus();
            return false;
        }

        if (password.length() < 5) {
            txtPassword.setError("Password must contain at least 5 characters.");
            txtPassword.requestFocus();
            return false;
        }

        if (password2.isEmpty()) {
            txtPassword2.setError("Please enter your password.");
            txtPassword2.requestFocus();
            return false;
        }

        if (!password.equals(password2)) {
            txtPassword2.setError("Passwords must match");
            txtPassword2.requestFocus();
            return false;
        }

        return true;
    }

    public void sendToMain() {
        Intent mainIntent = new Intent(getApplicationContext(), HomeActivity.class);
        mainIntent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
        startActivity(mainIntent);
        finish();
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        if (requestCode == TAKE_PICTURE) {
            Uri imageUri = null;
            if (data != null) {
                if (data.hasExtra("data")) {
                    Bundle extras = data.getExtras();
                    imageBitmap = (Bitmap) extras.get("data");
                    imgCreatePhoto.setBackground((Drawable) getResources().getDrawable(R.drawable.checked));
                } else {
                    Toast.makeText(getApplicationContext(), "There was an error taking a picture", Toast.LENGTH_SHORT).show();
                }
            } else {
                Toast.makeText(getApplicationContext(), "There was an error taking a picture", Toast.LENGTH_SHORT).show();
            }
        }

    }
}