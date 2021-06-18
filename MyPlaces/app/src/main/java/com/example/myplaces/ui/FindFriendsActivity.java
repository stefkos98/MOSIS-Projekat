package com.example.myplaces.ui;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;

import android.app.Activity;
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
import android.text.Editable;
import android.text.TextWatcher;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.inputmethod.InputMethodManager;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.BaseAdapter;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ListAdapter;
import android.widget.ListView;
import android.widget.TextView;
import android.widget.Toast;

import com.example.myplaces.R;
import com.example.myplaces.models.User;
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

public class FindFriendsActivity extends Activity {
    FirebaseAuth mAuth;
    FirebaseStorage storage;
    StorageReference storageRef;
    DatabaseReference database;
    FirebaseUser user;
    ListView listView;
    EditText editText;
    ArrayList alist;
    ArrayList<String> stringlist;
    ArrayList<String> friendslist;
    ArrayList<String> requestslist;
    ProgressDialog mDialog;
    String uid;
    int i;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_find_friends);

        mAuth = FirebaseAuth.getInstance();
        storage = FirebaseStorage.getInstance();
        storageRef = storage.getReference();
        database = FirebaseDatabase.getInstance().getReference();
        user = FirebaseAuth.getInstance().getCurrentUser();
        uid = user.getUid();
        alist = new ArrayList();
        stringlist = new ArrayList<String>();
        listView = findViewById(R.id.listViewFF);
        editText = findViewById(R.id.editTextFF);

        friendslist = new ArrayList<String>();
        requestslist = new ArrayList<String>();
        database.child("users").child(uid).child("requests").get().addOnCompleteListener(new OnCompleteListener<DataSnapshot>() {
            @Override
            public void onComplete(@NonNull Task<DataSnapshot> task1) {
                for (DataSnapshot postSnapshot : task1.getResult().getChildren()) {
                    String id1 = postSnapshot.getKey();
                    requestslist.add(id1);
                }
            }
        });
        database.child("users").child(uid).child("friends").get().addOnCompleteListener(new OnCompleteListener<DataSnapshot>() {
            @Override
            public void onComplete(@NonNull Task<DataSnapshot> task2) {
                for (DataSnapshot postSnapshot : task2.getResult().getChildren()) {
                    String id2 = postSnapshot.getKey();
                    friendslist.add(id2);
                }
            }
        });
        findViewById(R.id.btnFindFriendsFF).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {

                String name = editText.getText().toString().trim();
                //Pretraga username-a na osnovu imena
                database.child("users").orderByChild("firstName").equalTo(name).get().addOnCompleteListener(new OnCompleteListener<DataSnapshot>() {
                    @Override
                    public void onComplete(@NonNull Task<DataSnapshot> task) {
                        alist.clear();
                        stringlist.clear();
                        i = 0;
                        for (DataSnapshot postSnapshot : task.getResult().getChildren()) {
                            User u = postSnapshot.getValue(User.class);
                            String idu = postSnapshot.getKey();
                            alist.add(u.username);
                            if (idu.equals(uid)) {
                                stringlist.add("NE");
                            } else stringlist.add("DA");
                            for (String s1 : requestslist)
                                if (s1.equals(idu)) {
                                    stringlist.set(i, "NE");
                                    break;
                                }
                            for (String s1 : friendslist)
                                if (s1.equals(idu)) {
                                    stringlist.set(i, "NE");
                                    break;
                                }
                            i++;
                        }

                        InputMethodManager inputManager = (InputMethodManager)
                                getSystemService(Context.INPUT_METHOD_SERVICE);
                        inputManager.hideSoftInputFromWindow(getCurrentFocus().getWindowToken(),
                                InputMethodManager.HIDE_NOT_ALWAYS);
                        MyListAdapter adapter = new MyListAdapter(alist, stringlist, FindFriendsActivity.this);
                        listView.setAdapter(adapter);
                    }
                });

            }
        });
    }

    private class MyListAdapter extends BaseAdapter implements ListAdapter {
        ArrayList<String> l = new ArrayList<String>();
        ArrayList<String> sl = new ArrayList<String>();
        private Context context;

        private MyListAdapter(ArrayList<String> list, ArrayList<String> stringlist, Context context) {
            this.l = list;
            this.sl = stringlist;
            this.context = context;
        }

        @Override
        public int getCount() {
            return l.size();
        }

        @Override
        public Object getItem(int i) {
            return l.get(i);
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
                view = inflater.inflate(R.layout.for_adapter, null);
            }
            TextView title = (TextView) view.findViewById(R.id.txtFF);
            title.setText("Username: " + l.get(position));
            Button button = (Button) view.findViewById(R.id.btnFF);
            if (sl.get(position).equals("NE")) button.setVisibility(View.INVISIBLE);
            if (sl.get(position).equals("DA")) button.setVisibility(View.VISIBLE);
            button.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    String entry = (String) l.get(position);
                    final String[] id1 = {null};
                    database.child("usernames").child(entry).get().addOnCompleteListener(new OnCompleteListener<DataSnapshot>() {
                        @Override
                        public void onComplete(@NonNull Task<DataSnapshot> task) {
                            if (task.getResult().getValue() != null) {
                                id1[0] = (String) task.getResult().getValue();

                                String uid = user.getUid();
                                database.child("users").child(uid).get().addOnCompleteListener(new OnCompleteListener<DataSnapshot>() {
                                    @Override
                                    public void onComplete(@NonNull Task<DataSnapshot> task) {
                                        User u = task.getResult().getValue(User.class);
                                        database.child("users").child(id1[0]).child("requests").child(uid).setValue(u.username);
                                    }
                                });
                            }

                        }
                    });
                    button.setVisibility(View.INVISIBLE);
                }
            });
            return view;
        }
    }
}