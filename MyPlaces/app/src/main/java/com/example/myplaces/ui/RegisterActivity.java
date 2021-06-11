package com.example.myplaces.ui;

import androidx.annotation.RequiresApi;
import androidx.appcompat.app.AppCompatActivity;

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
import com.google.firebase.auth.AuthResult;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseAuthUserCollisionException;

public class RegisterActivity extends AppCompatActivity
{
    EditText txtEmail,txtPassword, txtPassword2;
    Button btnRegister;
    ProgressDialog mDialog;
    FirebaseAuth mAuth;

    @Override
    protected void onCreate(Bundle savedInstanceState)
    {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_register);
        getSupportActionBar().setDisplayShowTitleEnabled(false);

        txtEmail = findViewById(R.id.txtEmailR);
        txtPassword = findViewById(R.id.txtPasswordR);
        txtPassword2 = findViewById(R.id.txtConfirmPasswordR);
        btnRegister = findViewById(R.id.btnRegisterR);
        mAuth = FirebaseAuth.getInstance();

        btnRegister.setOnClickListener(new View.OnClickListener()
        {
            @RequiresApi(api = Build.VERSION_CODES.GINGERBREAD)
            @Override
            public void onClick(View v)
            {
                registerUser();
            }
        });
    }

    @RequiresApi(api = Build.VERSION_CODES.GINGERBREAD)
    public void registerUser()
    {
        mDialog = new ProgressDialog(RegisterActivity.this);
        mDialog.setMessage("Loading Registration...");
        mDialog.show();

        final String email = txtEmail.getText().toString().trim();
        final String password = txtPassword.getText().toString().trim();
        final String password2 = txtPassword2.getText().toString().trim();

        if(checkInputError(email,password,password2))
        {
            mAuth.createUserWithEmailAndPassword(email,password).addOnCompleteListener(this, new OnCompleteListener<AuthResult>()
            {
                @Override
                public void onComplete(@NonNull Task<AuthResult> task)
                {
                    mDialog.dismiss();

                    if(task.isSuccessful())
                    {
                        Toast.makeText(getApplicationContext(),"Registration successful!",Toast.LENGTH_SHORT).show();

                        mAuth.signInWithEmailAndPassword(email,password).addOnCompleteListener(new OnCompleteListener<AuthResult>()
                        {
                            @Override
                            public void onComplete(@NonNull Task<AuthResult> task)
                            {
                                if(task.isSuccessful())
                                {
                                    sendToMain();
                                }
                                else
                                    Toast.makeText(getApplicationContext(), "There was an error logging in.", Toast.LENGTH_SHORT).show();
                            }
                        });
                    }
                    else
                    {
                        if (task.getException() instanceof FirebaseAuthUserCollisionException)
                            Toast.makeText(getApplicationContext(), "User with this Email already exists.", Toast.LENGTH_SHORT).show();
                        else
                            Toast.makeText(getApplicationContext(), "There was an error. Try again later.", Toast.LENGTH_SHORT).show();
                    }
                }
            });
        }
        else
            mDialog.dismiss();

    }

    @RequiresApi(api = Build.VERSION_CODES.GINGERBREAD)
    public boolean checkInputError(String email, String password, String password2)
    {
        if(email.isEmpty())
        {
            txtEmail.setError("Please enter your Email.");
            txtEmail.requestFocus();
            return false;
        }

        if(!Patterns.EMAIL_ADDRESS.matcher(email).matches())
        {
            txtEmail.setError("Please enter a valid Email address.");
            txtEmail.requestFocus();
            return false;
        }

        if (password.isEmpty())
        {
            txtPassword.setError("Please enter your password.");
            txtPassword.requestFocus();
            return false;
        }

        if(password.length() < 5)
        {
            txtPassword.setError("Password must contain at least 5 characters.");
            txtPassword.requestFocus();
            return false;
        }

        if (password2.isEmpty())
        {
            txtPassword2.setError("Please enter your password.");
            txtPassword2.requestFocus();
            return false;
        }

        if(!password.equals(password2))
        {
            txtPassword2.setError("Passwords must match");
            txtPassword2.requestFocus();
            return false;
        }

        return true;
    }

    public void sendToMain()
    {
        Intent mainIntent = new Intent(getApplicationContext(), HomeActivity.class);
        mainIntent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
        startActivity(mainIntent);
        finish();
    }

}