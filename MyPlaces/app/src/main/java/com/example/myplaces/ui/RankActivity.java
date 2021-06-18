package com.example.myplaces.ui;

import android.app.Activity;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.os.Bundle;
import com.example.myplaces.R;
import com.example.myplaces.models.MyPlace;
import com.example.myplaces.models.MyPlacesData;
import com.example.myplaces.models.User;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.android.material.floatingactionbutton.FloatingActionButton;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.Query;
import com.google.firebase.database.ValueEventListener;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;

import android.util.Log;
import android.view.ContextMenu;
import android.view.MenuItem;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.ListView;
import android.widget.Toast;

import java.util.ArrayList;
import java.util.Collections;

public class RankActivity extends Activity {
    DatabaseReference database;
    FirebaseAuth mAuth;
    ArrayList<String> lista;
    ArrayList<String> lista2;
    ArrayList<String> lista3,lista4;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        lista = new ArrayList<>();
        lista2=new ArrayList<>();
        lista3=new ArrayList<>();
        lista4=new ArrayList<>();
        mAuth = FirebaseAuth.getInstance();
        super.onCreate(savedInstanceState);
        setContentView(R.layout.content_rank);

        ListView myPlacesList = (ListView) findViewById(R.id.listViewRank);
        database = FirebaseDatabase.getInstance().getReference();
        database.child("users").orderByChild("points").get().addOnCompleteListener(new OnCompleteListener<DataSnapshot>() {
            @Override
            public void onComplete(@NonNull Task<DataSnapshot> task) {
                int size=0,index=0;
                for (DataSnapshot postSnapshot : task.getResult().getChildren()) {
                    User u = postSnapshot.getValue(User.class);
                        lista.add(String.valueOf(u.points));
                        lista2.add(u.username);
                        if (u.email.equals(FirebaseAuth.getInstance().getCurrentUser().getEmail())) {
                            index=size;
                        }
                        size++;
                    }
                Collections.reverse(lista);
                Collections.reverse(lista2);
                index=(size-1)-index;
                if(index<7){
                    for(int i=0;i<index;i++){
                        lista3.add(lista.get(i));
                        lista4.add(lista2.get(i));
                    }
                    for(int i=index;i<5;i++){
                        lista3.add(lista.get(i));
                        lista4.add(lista2.get(i));
                    }
                }
                else{
                    for(int i=0;i<5;i++){
                        lista3.add(lista.get(i));
                        lista4.add(lista2.get(i));
                    }
                    lista3.add(". . .");
                    lista4.add(". . .");
                    lista3.add(lista.get(index-1));
                    lista4.add(lista2.get(index-1));
                    lista3.add(lista.get(index));
                    lista4.add(lista2.get(index));
                    if(index!=size-1){
                        lista3.add(lista.get(index+1));
                        lista4.add(lista2.get(index+1));
                    }
                }
                myPlacesList.setAdapter(new ArrayAdapter<String>(getApplicationContext(), android.R.layout.simple_list_item_1, lista3));
            }
        });

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
        //** snip **//
    }

}
