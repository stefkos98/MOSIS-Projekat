package com.example.myplaces.ui;

import android.Manifest;
import android.app.Notification;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.pm.PackageManager;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.drawable.BitmapDrawable;
import android.graphics.drawable.Drawable;
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
import com.google.firebase.auth.AuthCredential;
import com.google.firebase.auth.AuthResult;
import com.google.firebase.auth.EmailAuthProvider;
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
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.AppCompatImageView;
import androidx.appcompat.widget.Toolbar;
import androidx.core.app.ActivityCompat;

import android.provider.MediaStore;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;

import java.io.ByteArrayOutputStream;
import java.util.HashMap;
import java.util.Map;

public class ProfileActivity extends AppCompatActivity {
    EditText txtFirstName, txtLastName, txtUsername, txtPhone, txtEmail, txtPassword, txtPassword2;
    private static final int TAKE_PICTURE = 100;
    int NEW_PHOTO_TAKEN = 0;
    FirebaseAuth mAuth;
    private Bitmap imageBitmap;
    StorageReference storageRef, pictureRef;
    DatabaseReference database;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_profile);
        /**snip **/
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
        txtFirstName = findViewById(R.id.txtFirstNameP);
        txtLastName = findViewById(R.id.txtLastNameP);
        txtUsername = findViewById(R.id.txtUsernameP);
        txtPhone = findViewById(R.id.txtPhoneP);
        txtEmail = findViewById(R.id.txtEmailP);
        txtPassword = findViewById(R.id.txtPasswordP);
        txtPassword2 = findViewById(R.id.txtConfirmPasswordP);
        mAuth = FirebaseAuth.getInstance();
        storageRef = FirebaseStorage.getInstance().getReference();
        database = FirebaseDatabase.getInstance().getReference();
        FirebaseUser user = FirebaseAuth.getInstance().getCurrentUser();
        String uid = user.getUid();
        database.child("users").child(uid).get().addOnCompleteListener(new OnCompleteListener<DataSnapshot>() {
            @Override
            public void onComplete(@NonNull Task<DataSnapshot> task) {
                if (!task.isSuccessful()) {
                    Log.e("firebase", "Error getting data", task.getException());
                } else {
                    String firstName = String.valueOf(task.getResult().child("firstName").getValue());
                    String lastName = String.valueOf(task.getResult().child("lastName").getValue());
                    String email = String.valueOf(task.getResult().child("email").getValue());
                    String phone = String.valueOf(task.getResult().child("phone").getValue());
                    String username = String.valueOf(task.getResult().child("username").getValue());
                    String picture = String.valueOf(task.getResult().child("picture").getValue());
                    txtFirstName.setText(firstName);
                    txtLastName.setText(lastName);
                    txtEmail.setText(email);
                    txtEmail.setEnabled(false);
                    txtUsername.setEnabled(false);
                    txtUsername.setText(username);
                    txtPhone.setText(phone);
                    final long ONE_MEGABYTE = 1024 * 1024;
                    storageRef.child("images").child(email + ".jpg").getBytes(ONE_MEGABYTE).addOnSuccessListener(new OnSuccessListener<byte[]>() {
                        @Override
                        public void onSuccess(byte[] bytes) {
                            Drawable image = new BitmapDrawable(getResources(), BitmapFactory.decodeByteArray(bytes, 0, bytes.length));
                            findViewById(R.id.photoProfileP).setBackground(image);
                            // Data for "images/island.jpg" is returns, use this as needed
                        }
                    }).addOnFailureListener(new OnFailureListener() {
                        @Override
                        public void onFailure(@NonNull Exception exception) {
                            // Handle any errors
                        }
                    });
                }
            }
        });
        findViewById(R.id.imageChangePhoto).setOnClickListener(new View.OnClickListener() {
                                                                   @RequiresApi(api = Build.VERSION_CODES.GINGERBREAD)
                                                                   @Override
                                                                   public void onClick(View v) {
                                                                       if (ActivityCompat.checkSelfPermission(getApplicationContext(), Manifest.permission.CAMERA) != PackageManager.PERMISSION_GRANTED)
                                                                           ActivityCompat.requestPermissions(ProfileActivity.this, new String[]{Manifest.permission.CAMERA}, 101);
                                                                       else {
                                                                           startActivityForResult(new Intent(MediaStore.ACTION_IMAGE_CAPTURE), TAKE_PICTURE);
                                                                       }
                                                                   }
                                                               }
        );
        findViewById(R.id.btnSaveChangesP).setOnClickListener(new View.OnClickListener() {
                                                                  @RequiresApi(api = Build.VERSION_CODES.GINGERBREAD)
                                                                  @Override
                                                                  public void onClick(View v) {
                                                                      if (NEW_PHOTO_TAKEN == 1) {
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
                                                                                  Task<Uri> outputFileUri = pictureRef.getDownloadUrl().addOnSuccessListener(new OnSuccessListener<Uri>() {
                                                                                      @Override
                                                                                      public void onSuccess(Uri uri) {
                                                                                          String imageUrl = uri.toString();
                                                                                          database.child("users").child(mAuth.getUid().toString()).child("picture").setValue(imageUrl);
                                                                                      }
                                                                                  });

                                                                              }

                                                                          });
                                                                      }
                                                                      String pass1 = txtPassword.getText().toString();
                                                                      String pass2 = txtPassword2.getText().toString();
                                                                      Log.d("TAG1", pass1);
                                                                      Log.d("TAg2", pass2);
                                                                      if (txtPassword.getText().toString() != "" && txtPassword2.getText().toString() != "") {
                                                                          FirebaseUser user = FirebaseAuth.getInstance().getCurrentUser();
                                                                          AuthCredential credential = EmailAuthProvider
                                                                                  .getCredential(user.getEmail(), txtPassword.getText().toString());
                                                                          user.reauthenticate(credential)
                                                                                  .addOnCompleteListener(new OnCompleteListener<Void>() {
                                                                                      @Override
                                                                                      public void onComplete(@NonNull Task<Void> task) {
                                                                                          if(task.isSuccessful()) {
                                                                                              Log.d("TAG", "User re-authenticated.");
                                                                                              user.updatePassword(txtPassword2.getText().toString()).addOnCompleteListener(new OnCompleteListener<Void>() {
                                                                                                  @Override
                                                                                                  public void onComplete(@NonNull Task<Void> task) {
                                                                                                      if (task.isSuccessful()) {
                                                                                                          Log.d("TAG", "Password updated");
                                                                                                          //database

                                                                                                      } else {
                                                                                                          Log.d("TAG", "Error password not updated");
                                                                                                      }
                                                                                                  }
                                                                                              });
                                                                                          }else{
                                                                                              Toast.makeText(getApplicationContext(), "Wrong password! Password not changed", Toast.LENGTH_SHORT).show();
                                                                                                return;
                                                                                          }
                                                                                      }
                                                                                  });
                                                                      }
                                                                      DatabaseReference hopperRef = database.child("users").child(uid);
                                                                      Map<String, Object> hopperUpdates = new HashMap<>();
                                                                      hopperUpdates.put("firstName", txtFirstName.getText().toString());
                                                                      hopperUpdates.put("lastName", txtLastName.getText().toString());
                                                                      hopperUpdates.put("phone", txtPhone.getText().toString());
                                                                      hopperRef.updateChildren(hopperUpdates).addOnCompleteListener(new OnCompleteListener<Void>() {
                                                                          @Override
                                                                          public void onComplete(@NonNull Task<Void> task) {
                                                                              finish();
                                                                          }
                                                                      });
                                                                  }
                                                              }
        );
        findViewById(R.id.btnDeleteProfileP).setOnClickListener(new View.OnClickListener() {
            @RequiresApi(api = Build.VERSION_CODES.GINGERBREAD)
            @Override
            public void onClick(View v) {
                new AlertDialog.Builder(ProfileActivity.this)
                        .setTitle("Delete Profile")
                        .setMessage("Are you sure you want to delete your profile?")

                        // Specifying a listener allows you to take an action before dismissing the dialog.
                        // The dialog is automatically dismissed when a dialog button is clicked.
                        .setPositiveButton("Yes", new DialogInterface.OnClickListener() {
                            public void onClick(DialogInterface dialog, int which) {
                                String uid = user.getUid();
                                String email = user.getEmail();
                                database.child("users").child(uid).get().addOnCompleteListener(new OnCompleteListener<DataSnapshot>() {
                                    @Override
                                    public void onComplete(@NonNull Task<DataSnapshot> task) {
                                        String username = task.getResult().child("username").getValue().toString();
                                        database.child("usernames").child(username).removeValue();
                                        database.child("users").child(uid).removeValue();
                                        storageRef.child("images").child(user.getEmail() + ".jpg").delete().addOnCompleteListener(new OnCompleteListener<Void>() {
                                            @Override
                                            public void onComplete(@NonNull Task<Void> task) {
                                            }
                                        });
                                        user.delete().addOnCompleteListener(new OnCompleteListener<Void>() {
                                                                                @Override
                                                                                public void onComplete(@NonNull Task<Void> task) {

                                                                                    if (task.isSuccessful()) {
                                                                                        Log.d("TAG", "User account deleted.");
                                                                                        Intent broadcastIntent = new Intent();
                                                                                        broadcastIntent.setAction("com.package.ACTION_LOGOUT");
                                                                                        sendBroadcast(broadcastIntent);
                                                                                        mAuth.signOut();
                                                                                        Intent logoutIntent = new Intent(ProfileActivity.this, WelcomeActivity.class);
                                                                                        logoutIntent.setFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP | Intent.FLAG_ACTIVITY_SINGLE_TOP);
                                                                                        startActivity(logoutIntent);
                                                                                        finish();

                                                                                    }
                                                                                }
                                                                            }
                                        );


                                    }
                                });
                            }
                        })
                        // A null listener allows the button to dismiss the dialog and take no further action.
                        .setNegativeButton("No", null)
                        .setIcon(android.R.drawable.ic_dialog_alert)
                        .show();
            }
        });
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
                    startActivityForResult(new Intent(MediaStore.ACTION_IMAGE_CAPTURE), TAKE_PICTURE);
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
        if (requestCode == TAKE_PICTURE) {
            Uri imageUri = null;

            if (data != null) {
                if (data.hasExtra("data")) {
                    NEW_PHOTO_TAKEN = 1;
                    Bundle extras = data.getExtras();
                    imageBitmap = (Bitmap) extras.get("data");
                    findViewById(R.id.photoProfileP).setBackground(new BitmapDrawable(getResources(), imageBitmap));
                    findViewById(R.id.imageChangePhoto).setBackground((Drawable) getResources().getDrawable(R.drawable.checked));
                } else {
                    Toast.makeText(getApplicationContext(), "There was an error taking a picture", Toast.LENGTH_SHORT).show();
                }
            } else {
                Toast.makeText(getApplicationContext(), "There was an error taking a picture", Toast.LENGTH_SHORT).show();
            }
        }

    }

    @Override
    public boolean onSupportNavigateUp() {
        onBackPressed();
        return true;
    }
}
