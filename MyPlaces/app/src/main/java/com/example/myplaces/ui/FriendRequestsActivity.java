package com.example.myplaces.ui;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;

import android.app.ProgressDialog;
import android.bluetooth.BluetoothDevice;
import android.os.Bundle;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.EditText;
import android.widget.ListView;

import com.example.myplaces.R;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.StorageReference;

import java.util.ArrayList;
import java.util.Set;

public class FriendRequestsActivity extends AppCompatActivity {
    FirebaseAuth mAuth;
    FirebaseStorage storage;
    StorageReference storageRef;
    DatabaseReference database;
    FirebaseUser user;
    ListView listView;
    ArrayList ulist;
    ArrayList klist;
    ArrayList<String> idlist;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_friend_requests);
        listView= findViewById(R.id.listViewFR);

        mAuth = FirebaseAuth.getInstance();
        storage = FirebaseStorage.getInstance();
        storageRef = storage.getReference();
        database = FirebaseDatabase.getInstance().getReference();
        user = FirebaseAuth.getInstance().getCurrentUser();

        ulist = new ArrayList();
        klist = new ArrayList();
        idlist = new ArrayList<String>();
        String uid = user.getUid();
        database.child("users").child(uid).child("requests").get().addOnCompleteListener(new OnCompleteListener<DataSnapshot>() {
            @Override
            public void onComplete(@NonNull  Task<DataSnapshot> task) {
                ulist.clear();
                idlist.clear();
                for (DataSnapshot postSnapshot: task.getResult().getChildren()) {
                    String in = postSnapshot.getKey();
                    database.child("users").child(in).get().addOnCompleteListener(new OnCompleteListener<DataSnapshot>(){
                        @Override
                        public void onComplete(@NonNull Task<DataSnapshot> task) {
                            FindFriendsActivity.User u = (FindFriendsActivity.User)task.getResult().getValue();
                            ulist.add(u.username);
                            idlist.add(in);
                        }
                    });
                }
            }
        });

        ArrayAdapter adapter= new ArrayAdapter(FriendRequestsActivity.this, android.R.layout.simple_list_item_1, ulist);
        listView.setAdapter(adapter);
        listView.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
                String id1=  idlist.get(position);
                String uid = user.getUid();
                database.child("users").child(id1).child("friends").child(uid).setValue("");
                database.child("users").child(uid).child("friends").child(id1).setValue("");
                database.child("users").child(uid).child("requests").child(id1).removeValue();
            };
        });
    }
}