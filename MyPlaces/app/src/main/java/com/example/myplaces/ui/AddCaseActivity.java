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
import com.google.firebase.auth.FirebaseAuth;

import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;

import android.text.Editable;
import android.text.TextWatcher;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;

public class AddCaseActivity extends AppCompatActivity implements View.OnClickListener{
    boolean editMode=true;
    int position=-1;
    FirebaseAuth mAuth;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        mAuth=FirebaseAuth.getInstance();
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_add_case);
        Toolbar toolbar = findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);
        getSupportActionBar().setDisplayShowTitleEnabled(false);

        final Button finishedButon = (Button)findViewById(R.id.editmyplace_finished_button);
        finishedButon.setOnClickListener(this);
        finishedButon.setEnabled(false);
        finishedButon.setText("Add");
        EditText nameEditText=(EditText)findViewById(R.id.editmyplace_name_edit);
        nameEditText.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {

            }

            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {

            }

            @Override
            public void afterTextChanged(Editable s) {
                finishedButon.setEnabled(s.length()>0);
            }


        });

        Button locationButton = (Button)findViewById(R.id.editmyplace_location_button);
        locationButton.setOnClickListener(this);

        Button cancelButon = (Button)findViewById(R.id.editmyplace_cancel_button);
        cancelButon.setOnClickListener(this);
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        getSupportActionBar().setDisplayShowHomeEnabled(true);
        try{
            Intent listIntent=getIntent();
            Bundle positionBundle=listIntent.getExtras();
            if(positionBundle!=null){
                position=positionBundle.getInt("position");
            }
            else editMode=false;
        }catch(Exception e){
            editMode=false;
        }
        if(!editMode){
            finishedButon.setEnabled(false);
            finishedButon.setText("Add");
        }
        else if(position>=0){
            finishedButon.setText("Save");
            MyPlace place= MyPlacesData.getInstance().getPlace(position);
            nameEditText.setText(place.name);
            EditText descEditText=(EditText)findViewById(R.id.editmyplace_desc_edit);
            descEditText.setText(place.description);
        }

        /**snip **/
        IntentFilter intentFilter = new IntentFilter();
        intentFilter.addAction("com.package.ACTION_LOGOUT");
        registerReceiver(new BroadcastReceiver() {
            @Override
            public void onReceive(Context context, Intent intent) {
                Log.d("onReceive","Logout in progress");
                //At this point you should start the login activity and finish this one
                finish();
            }
        }, intentFilter);
        //** snip **//
    }

    @Override
    public void onClick(View view)
    {
        switch(view.getId())
        {
            case R.id.editmyplace_finished_button: {
            String name=((EditText)findViewById(R.id.editmyplace_name_edit)).getText().toString();
            String desc=((EditText)findViewById(R.id.editmyplace_desc_edit)).getText().toString();

            String lat=((EditText)findViewById(R.id.editmyplace_lat_edit)).getText().toString();
            String lon=((EditText)findViewById(R.id.editmyplace_lon_edit)).getText().toString();
            if(!editMode){
            MyPlace place=new MyPlace(name,desc);
            place.latitude=lat;
            place.longitude=lon;
            MyPlacesData.getInstance().addNewPlace(place);}
            else{
               MyPlacesData.getInstance().updatePlace(position,name,desc,lon,lat);
            }
            setResult(Activity.RESULT_OK);
            finish();
            break;
            }
            case R.id.editmyplace_cancel_button: {
                setResult(Activity.RESULT_CANCELED);
                finish();
                break;
            }
            case R.id.editmyplace_location_button:{
                Intent i=new Intent(this, MapActivity.class);
                i.putExtra("state", MapActivity.SELECT_COORDINATES);
                startActivityForResult(i,1);
                break;
            }
        }
    }


    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.menu_add_case, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        // Handle action bar item clicks here. The action bar will
        // automatically handle clicks on the Home/Up button, so long
        // as you specify a parent activity in AndroidManifest.xml.
        int id = item.getItemId();

        //noinspection SimplifiableIfStatement
        if (id == R.id.first_setting) {
            Intent i=new Intent(this, MapActivity.class);
            i.putExtra("state", MapActivity.SHOW_MAP);
            startActivity(i);
        }
        if (id == R.id.third_setting) {
            Intent i=new Intent(this, RankActivity.class);
            startActivity(i);
        }
        if (id == R.id.fourth_setting) {
            Intent i=new Intent(this, FriendActivity.class);
            startActivity(i);
        }
        if (id == R.id.home) {
            finish();
        }
        if(id == R.id.fifth_setting)
        {
            //BROADCAST
            Intent broadcastIntent = new Intent();
            broadcastIntent.setAction("com.package.ACTION_LOGOUT");
            sendBroadcast(broadcastIntent);
            //
            mAuth.signOut();
            Intent logoutIntent = new Intent(AddCaseActivity.this, WelcomeActivity.class);
            logoutIntent.setFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP | Intent.FLAG_ACTIVITY_SINGLE_TOP);
            startActivity(logoutIntent);
            finish();
        }
        return super.onOptionsItemSelected(item);
    }
    @Override
    public boolean onSupportNavigateUp() {
        onBackPressed();
        return true;
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data)
    {

        super.onActivityResult(requestCode,resultCode,data);
        try {
            if(resultCode==Activity.RESULT_OK)
            {
                String lon=data.getExtras().getString("lon");
                EditText lonText=(EditText)findViewById(R.id.editmyplace_lon_edit);
                lonText.setText(lon);
                String lat=data.getExtras().getString("lat");
                EditText latText=(EditText)findViewById(R.id.editmyplace_lat_edit);
                latText.setText(lat);
            }
        }
        catch (Exception e)
        {

        }
    }
}
