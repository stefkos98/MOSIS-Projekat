package com.example.myplaces.ui;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;

import android.app.Dialog;
import android.app.ProgressDialog;
import android.bluetooth.BluetoothAdapter;
import android.bluetooth.BluetoothDevice;
import android.bluetooth.BluetoothSocket;
import android.content.Context;
import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.drawable.BitmapDrawable;
import android.graphics.drawable.Drawable;
import android.net.Uri;
import android.os.Bundle;
import android.os.ParcelUuid;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ListView;
import android.widget.TextView;
import android.widget.Toast;

import com.example.myplaces.R;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.AuthResult;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseAuthUserCollisionException;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.StorageReference;
import com.google.firebase.storage.UploadTask;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.util.ArrayList;
import java.util.List;
import java.util.Set;

public class FindFriendsActivity extends AppCompatActivity {
    FirebaseAuth mAuth;
    FirebaseStorage storage;
    StorageReference storageRef;
    DatabaseReference database;
    FirebaseUser user;
    ListView listView;
    EditText editText;
    ArrayList alist;
    ProgressDialog mDialog;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_find_friends);

        mAuth = FirebaseAuth.getInstance();
        storage = FirebaseStorage.getInstance();
        storageRef = storage.getReference();
        database = FirebaseDatabase.getInstance().getReference();
        user = FirebaseAuth.getInstance().getCurrentUser();

        alist = new ArrayList();
        listView = findViewById(R.id.listViewFF);
        editText = findViewById(R.id.editTextFF);

        findViewById(R.id.btnFindFriendsFF).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {

                String name = editText.getText().toString().trim();
                //Pretraga username-a na osnovu imena
                database.child("users").orderByChild("firstName").equalTo(name).get().addOnCompleteListener(new OnCompleteListener<DataSnapshot>() {
                    @Override
                    public void onComplete(@NonNull  Task<DataSnapshot> task) {
                        alist.clear();
                        for (DataSnapshot postSnapshot: task.getResult().getChildren()) {
                            User u = postSnapshot.getValue(User.class);
                            alist.add(u.username);
                        }
                    }
                });
                ArrayAdapter adapter= new ArrayAdapter(FindFriendsActivity.this, android.R.layout.simple_list_item_1, alist);
                listView.setAdapter(adapter);
            }
        });

        listView.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
                String entry= (String) parent.getAdapter().getItem(position);
                final String[] id1 = {null};
                database.child("usernames").child(entry).get().addOnCompleteListener(new OnCompleteListener<DataSnapshot>() {
                    @Override
                    public void onComplete(@NonNull  Task<DataSnapshot> task) {
                        if (task.getResult().getValue() != null) {
                            id1[0] = (String)task.getResult().getValue();

                            String uid = user.getUid();
                            database.child("users").child(id1[0]).child("requests").child(uid).setValue("");
                        }
                    }
                });

            };
        });

    }

    public class User {
        public String username;
        public String email;
        public String firstName;
        public String lastName;
        public String phone;
        public String picture;
        public String points;
        public ArrayList<Object> requests = new ArrayList<Object>();
        public ArrayList<Object> friends = new ArrayList<Object>();
    }
}