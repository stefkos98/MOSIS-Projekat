package com.example.myplaces.ui;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;

import android.bluetooth.BluetoothAdapter;
import android.bluetooth.BluetoothDevice;
import android.content.Context;
import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.drawable.BitmapDrawable;
import android.graphics.drawable.Drawable;
import android.net.Uri;
import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.Button;
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
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.StorageReference;
import com.google.firebase.storage.UploadTask;

import java.io.ByteArrayOutputStream;
import java.util.ArrayList;
import java.util.List;
import java.util.Set;

public class FindFriendsActivity extends AppCompatActivity {
    FirebaseAuth mAuth;
    FirebaseStorage storage;
    StorageReference storageRef;
    DatabaseReference database;
    Set<BluetoothDevice> pairedDevices;
    FirebaseUser user;
    BluetoothAdapter mBluetoothAdapter = BluetoothAdapter.getDefaultAdapter();
    int REQUEST_ENABLE_BT=0;
    ListView listView;
    TextView textView;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_find_friends);

        mAuth = FirebaseAuth.getInstance();
        storage = FirebaseStorage.getInstance();
        storageRef = storage.getReference();
        database = FirebaseDatabase.getInstance().getReference();
        user= FirebaseAuth.getInstance().getCurrentUser();

        listView=findViewById(R.id.ListViewFF);
        textView=findViewById(R.id.textViewFF);
        textView.setText("Name: " + getLocalBluetoothName());
        Set<BluetoothDevice> pairedDevices;
        BluetoothAdapter mBluetoothAdapter = BluetoothAdapter.getDefaultAdapter();
        int REQUEST_ENABLE_BT=0;

        findViewById(R.id.btnFindFriendsFF).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                if (mBluetoothAdapter==null ) {
                    Toast.makeText(FindFriendsActivity.this, "Bluetooth not supported", Toast.LENGTH_SHORT).show();
                    finish();
                }
                if (!mBluetoothAdapter.isEnabled()) {
                    Intent enableBluetoothIntent = new Intent(BluetoothAdapter.ACTION_REQUEST_ENABLE);
                    startActivityForResult(enableBluetoothIntent,REQUEST_ENABLE_BT);
                }
                if (mBluetoothAdapter.isEnabled()) {
                    Intent discoverableIntent = new Intent(BluetoothAdapter.ACTION_REQUEST_DISCOVERABLE);
                    startActivityForResult(discoverableIntent,0);
                    Toast.makeText(FindFriendsActivity.this, "Visible for 2 min", Toast.LENGTH_SHORT).show();
                    List();
                }
            }
        });

    }
    private void List() {
        pairedDevices =mBluetoothAdapter.getBondedDevices();

        ArrayList list = new ArrayList();
        for (BluetoothDevice bt :pairedDevices)
            list.add(bt.getName());

        MyListAdapter adapter= new MyListAdapter(this, android.R.layout.simple_list_item_1, list);
        listView.setAdapter(adapter);
    }

    public String getLocalBluetoothName()
    {
        if(mBluetoothAdapter == null) {
            mBluetoothAdapter=BluetoothAdapter.getDefaultAdapter();
        }
        String name=mBluetoothAdapter.getName();
        if(name==null) {
            name=mBluetoothAdapter.getAddress();
        }
        return name;
    }

    private class MyListAdapter extends ArrayAdapter<String> {
        private int layout;
        private MyListAdapter(Context context, int resource, List<String> objects){
            super(context,resource, objects);
            layout=resource;
        }

        @Override
        public View getView (int position, View convertView, ViewGroup parent)
        {
            ViewHolder mainViewHolder=null;
            if(convertView == null) {
                LayoutInflater inflater=LayoutInflater.from(getContext());
                convertView= inflater.inflate(layout, parent, false);
                ViewHolder viewHolder =  new ViewHolder();
                viewHolder.title= (TextView) convertView.findViewById(R.id.textViewFF);
                viewHolder.title.setText(getItem(position));
                viewHolder.button= (Button) convertView.findViewById(R.id.btnFF);
                viewHolder.button.setHeight(150);
                viewHolder.button.setOnClickListener(new View.OnClickListener(){
                    @Override
                    public void onClick(View v) {
                        Toast.makeText(getContext(), "Button was clicked for list item" + position, Toast.LENGTH_SHORT).show();
                        String uid = user.getUid();
                        database.child("users").child(uid).child("friends").get().addOnCompleteListener(new OnCompleteListener<DataSnapshot>() {
                            @Override
                            public void onComplete(@NonNull Task<DataSnapshot> task) {
                                if (!task.isSuccessful()) {
                                    Log.e("firebase", "Error getting data", task.getException());
                                } else {
                                    database.child("users").child(uid).child("friends").child(getItem(position)).setValue(getItem(position));
                                }
                            }
                        });
                    }
                });
                convertView.setTag(viewHolder);
            }
            else {
                mainViewHolder= (ViewHolder) convertView.getTag();
                mainViewHolder.title.setText(getItem(position));
            }
            return convertView;
        }
    }

    public class ViewHolder {
        Button button;
        TextView title;
    }
}
