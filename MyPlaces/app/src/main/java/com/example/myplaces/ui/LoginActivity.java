package com.example.myplaces.ui;

import androidx.annotation.RequiresApi;
import androidx.appcompat.app.AppCompatActivity;

import android.app.Activity;
import android.os.Build;
import android.os.Bundle;

import com.example.myplaces.R;

import androidx.annotation.NonNull;

import android.app.ProgressDialog;
import android.content.Intent;
import android.util.Patterns;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Toast;

import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.FirebaseError;
import com.google.firebase.auth.AuthResult;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

public class LoginActivity extends Activity {
    EditText txtUsername, txtPassword;
    Button btnLogin;
    ProgressDialog mDialog;
    FirebaseAuth mAuth;
    DatabaseReference database;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_login);

        txtUsername = findViewById(R.id.txtUsernameL);
        txtPassword = findViewById(R.id.txtPasswordL);
        btnLogin = findViewById(R.id.btnLoginL);
        mAuth = FirebaseAuth.getInstance();
        database = FirebaseDatabase.getInstance().getReference();
        btnLogin.setOnClickListener(new View.OnClickListener() {
            @RequiresApi(api = Build.VERSION_CODES.GINGERBREAD)
            @Override
            public void onClick(View v) {
                userLogin();
            }
        });
    }

    @RequiresApi(api = Build.VERSION_CODES.GINGERBREAD)
    protected void userLogin() {
        mDialog = new ProgressDialog(LoginActivity.this);
        mDialog.setMessage("Login processing...");
        mDialog.show();

        String username = txtUsername.getText().toString().trim();
        String password = txtPassword.getText().toString().trim();

        //Pretraga mejla na osnovu username-a
        database.child("usernames").child(username)
                .addListenerForSingleValueEvent(new ValueEventListener() {
                    @Override
                    public void onDataChange(DataSnapshot dataSnapshot) {
                        if (dataSnapshot.getValue(String.class) != null) {
                            String email = dataSnapshot.getValue(String.class);
                            database.child("users").child(email).child("email").get().addOnCompleteListener(new OnCompleteListener<DataSnapshot>() {
                                @Override
                                public void onComplete(@NonNull Task<DataSnapshot> task) {
                                    String email = String.valueOf(task.getResult().getValue());
                                    if (checkInputError(email, password)) {
                                        mAuth.signInWithEmailAndPassword(email, password).addOnCompleteListener(new OnCompleteListener<AuthResult>() {
                                            @Override
                                            public void onComplete(@NonNull Task<AuthResult> task) {
                                                mDialog.dismiss();
                                                if (task.isSuccessful()) {
                                                    Toast.makeText(getApplicationContext(), "Login successful!", Toast.LENGTH_SHORT).show();
                                                    sendToMain();
                                                } else {
                                                    Toast.makeText(getApplicationContext(), "Wrong password", Toast.LENGTH_SHORT).show();
                                                }
                                            }
                                        });
                                    } else
                                        mDialog.dismiss();
                                }
                            });
                        } else {
                            Toast.makeText(getApplicationContext(), "Wrong username!", Toast.LENGTH_SHORT).show();
                            mDialog.dismiss();
                        }
                    }

                    @Override
                    public void onCancelled(@NonNull DatabaseError error) {

                    }
                });
    }

    @RequiresApi(api = Build.VERSION_CODES.GINGERBREAD)
    public boolean checkInputError(String email, String password) {
        if (email.isEmpty()) {
            txtUsername.setError("Please enter your Email.");
            txtUsername.requestFocus();
            return false;
        }

        if (!Patterns.EMAIL_ADDRESS.matcher(email).matches()) {
            txtUsername.setError("Please enter a valid Email address.");
            txtUsername.requestFocus();
            return false;
        }

        if (password.isEmpty()) {
            txtPassword.setError("Please enter your password.");
            txtPassword.requestFocus();
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
}