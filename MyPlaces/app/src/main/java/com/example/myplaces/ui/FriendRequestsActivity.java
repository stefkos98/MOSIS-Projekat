package com.example.myplaces.ui;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;

import android.app.Activity;
import android.app.ProgressDialog;
import android.bluetooth.BluetoothDevice;
import android.content.Context;
import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.BaseAdapter;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ListAdapter;
import android.widget.ListView;
import android.widget.TextView;

import com.example.myplaces.R;
import com.example.myplaces.models.User;
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

public class FriendRequestsActivity extends Activity {
    FirebaseAuth mAuth;
    FirebaseStorage storage;
    StorageReference storageRef;
    DatabaseReference database;
    FirebaseUser user;
    ListView listView;
    ArrayList<String> ulist;
    ArrayList klist;
    ArrayList<String> idlist;
    MyArrayAdapter adapter;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_friend_requests);
        listView = findViewById(R.id.listViewFR);

        mAuth = FirebaseAuth.getInstance();
        storage = FirebaseStorage.getInstance();
        storageRef = storage.getReference();
        database = FirebaseDatabase.getInstance().getReference();
        user = FirebaseAuth.getInstance().getCurrentUser();

        ulist = new ArrayList<String>();
        klist = new ArrayList();
        idlist = new ArrayList<String>();
        String uid = user.getUid();

        database.child("users").child(uid).child("requests").get().addOnCompleteListener(new OnCompleteListener<DataSnapshot>() {
            @Override
            public void onComplete(@NonNull Task<DataSnapshot> task) {
                ulist.clear();
                idlist.clear();
                for (DataSnapshot postSnapshot : task.getResult().getChildren()) {
                    String id1 = postSnapshot.getKey();
                    String username1 = postSnapshot.getValue(String.class);
                    ulist.add(username1);
                    idlist.add(id1);
                }
                adapter = new MyArrayAdapter(idlist, ulist, FriendRequestsActivity.this);
                listView.setAdapter(adapter);
            }
        });
    }

    private class MyArrayAdapter extends BaseAdapter implements ListAdapter {
        ArrayList<String> idl = new ArrayList<String>();
        ArrayList<String> ul = new ArrayList<String>();
        private Context context;

        private MyArrayAdapter(ArrayList<String> idlist,ArrayList<String> ulist, Context context) {
            this.idl=idlist;
            this.ul = ulist;
            this.context = context;
        }

        @Override
        public int getCount() {
            return ul.size();
        }

        @Override
        public Object getItem(int i) {
            return ul.get(i);
        }

        @Override
        public long getItemId(int i) {
            return 0;
        }

        @Override
        public View getView(final int position, View convertView, ViewGroup parent) {
            View view = convertView;
            if (view == null) {
                LayoutInflater inflater = (LayoutInflater) context.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
                view = inflater.inflate(R.layout.for_adapter2, null);
            }
            TextView title = (TextView) view.findViewById(R.id.txtFR);
            title.setText("Username: " + ul.get(position));
            Button button2 = (Button)view.findViewById(R.id.btn2FR);
            Button button = (Button) view.findViewById(R.id.btnFR);
            button.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    String id1 = idl.get(position);
                    String username1 = ul.get(position);

                    String uid = user.getUid();
                    database.child("users").child(uid).get().addOnCompleteListener(new OnCompleteListener<DataSnapshot>() {
                        @Override
                        public void onComplete(@NonNull Task<DataSnapshot> task) {
                            User u = task.getResult().getValue(User.class);
                            database.child("users").child(id1).child("friends").child(uid).setValue(u.username);
                        }
                    });
                    database.child("users").child(uid).child("friends").child(id1).setValue(username1);
                    database.child("users").child(uid).child("requests").child(id1).removeValue();
                    button.setVisibility(View.INVISIBLE);
                    button2.setVisibility(View.INVISIBLE);
                }
            });
            button2.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View view) {
                    String id2 = idl.get(position);
                    String uid = user.getUid();
                    database.child("users").child(uid).child("requests").child(id2).removeValue();
                    button.setVisibility(View.INVISIBLE);
                    button2.setVisibility(View.INVISIBLE);
                }
            });
            return view;
        }

    }
}