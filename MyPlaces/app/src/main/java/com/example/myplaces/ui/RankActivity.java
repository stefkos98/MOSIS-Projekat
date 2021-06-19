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
import android.view.LayoutInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.BaseAdapter;
import android.widget.Button;
import android.widget.LinearLayout;
import android.widget.ListAdapter;
import android.widget.ListView;
import android.widget.TextView;
import android.widget.Toast;

import java.util.ArrayList;
import java.util.Collections;

public class RankActivity extends Activity {
    DatabaseReference database;
    FirebaseAuth mAuth;
    ArrayList<String> lista;
    ArrayList<String> lista2;
    ArrayList<String> lista3, lista4, lista5;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        lista = new ArrayList<>();
        lista2 = new ArrayList<>();
        lista3 = new ArrayList<>();
        lista4 = new ArrayList<>();
        lista5 = new ArrayList<>();
        mAuth = FirebaseAuth.getInstance();
        super.onCreate(savedInstanceState);
        setContentView(R.layout.content_rank);

        ListView myPlacesList = (ListView) findViewById(R.id.listViewRank);
        database = FirebaseDatabase.getInstance().getReference();
        database.child("users").orderByChild("points").get().addOnCompleteListener(new OnCompleteListener<DataSnapshot>() {
            @Override
            public void onComplete(@NonNull Task<DataSnapshot> task) {
                int size = 0, index = 0;
                String usrnm=null;
                for (DataSnapshot postSnapshot : task.getResult().getChildren()) {
                    User u = postSnapshot.getValue(User.class);
                    lista.add(String.valueOf(u.points));
                    lista2.add(u.username);
                    if (u.email.equals(FirebaseAuth.getInstance().getCurrentUser().getEmail())) {
                        index = size;
                        usrnm=u.username;
                    }
                    size++;
                }
                Collections.reverse(lista);
                Collections.reverse(lista2);
                index = (size - 1) - index;
                if (index < 7) {
                    for (int i = 0; i <= index; i++) {
                        lista3.add(lista.get(i));
                        lista4.add(lista2.get(i));
                        lista5.add(String.valueOf(i+1));
                    }
                    for (int i = index+1; i < 5; i++) {
                        lista3.add(lista.get(i));
                        lista4.add(lista2.get(i));
                        lista5.add(String.valueOf(i+1));
                    }
                } else {
                    for (int i = 0; i < 5; i++) {
                        lista3.add(lista.get(i));
                        lista4.add(lista2.get(i));
                        lista5.add(String.valueOf(i+1));
                    }
                    lista3.add(". . .");
                    lista4.add(". . .");
                    lista5.add(" ");
                    lista3.add(lista.get(index - 1));
                    lista4.add(lista2.get(index - 1));
                    lista5.add(String.valueOf(index));
                    lista3.add(lista.get(index));
                    lista4.add(lista2.get(index));
                    lista5.add(String.valueOf(index+1));
                    if (index != size - 1) {
                        lista3.add(lista.get(index + 1));
                        lista4.add(lista2.get(index + 1));
                        lista5.add(String.valueOf(index + 2));
                    }
                }
                myPlacesList.setAdapter(new MyListAdapter(usrnm, lista3, lista4, lista5, getApplicationContext()));
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


    private class MyListAdapter extends BaseAdapter implements ListAdapter {
        ArrayList<String> lista3 = new ArrayList<String>();
        ArrayList<String> lista4 = new ArrayList<String>();
        ArrayList<String> lista5 = new ArrayList<String>();
        String i;
        private Context context;

        private MyListAdapter(String usrnm, ArrayList<String> lista3, ArrayList<String> lista4, ArrayList<String> lista5, Context context) {
            this.i = usrnm;
            this.lista3 = lista3;
            this.lista4 = lista4;
            this.lista5 = lista5;
            this.context = context;
        }

        @Override
        public int getCount() {
            return lista3.size();
        }

        @Override
        public Object getItem(int i) {
            return lista3.get(i);
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
                view = inflater.inflate(R.layout.for_adapter3, null);
            }
            TextView txt1 = (TextView) view.findViewById(R.id.txt1Rank);
            TextView txt2 = (TextView) view.findViewById(R.id.txt2Rank);
            TextView txt3 = (TextView) view.findViewById(R.id.txt3Rank);
            txt1.setText(this.lista5.get(position));
            txt2.setText(this.lista4.get(position));
            txt3.setText(this.lista3.get(position));
            if (i.equals(lista4.get(position))) {
                ((LinearLayout) view.findViewById(R.id.linearRank)).setBackgroundColor(getResources().getColor(R.color.lightgreen));
            }
            return view;
        }
    }
}
