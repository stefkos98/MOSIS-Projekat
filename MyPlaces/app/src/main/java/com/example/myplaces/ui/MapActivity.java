package com.example.myplaces.ui;

import android.Manifest;
import android.annotation.SuppressLint;
import android.app.Activity;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.pm.PackageManager;
import android.os.Build;
import android.os.Bundle;

import com.example.myplaces.databinding.ActivityMapBinding;
import com.example.myplaces.models.MyPlace;
import com.example.myplaces.models.MyPlacesData;

import androidx.annotation.RequiresApi;
import androidx.appcompat.app.AppCompatActivity;

import android.preference.PreferenceManager;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Toast;

import androidx.core.app.ActivityCompat;
import androidx.navigation.NavController;
import androidx.navigation.Navigation;
import androidx.navigation.ui.AppBarConfiguration;
import androidx.navigation.ui.NavigationUI;

import com.example.myplaces.R;
import com.google.firebase.auth.FirebaseAuth;

import org.osmdroid.api.IMapController;
import org.osmdroid.config.Configuration;
import org.osmdroid.events.MapEventsReceiver;
import org.osmdroid.util.GeoPoint;
import org.osmdroid.views.MapView;
import org.osmdroid.views.overlay.ItemizedIconOverlay;
import org.osmdroid.views.overlay.MapEventsOverlay;
import org.osmdroid.views.overlay.OverlayItem;
import org.osmdroid.views.overlay.mylocation.GpsMyLocationProvider;
import org.osmdroid.views.overlay.mylocation.MyLocationNewOverlay;

import java.util.ArrayList;

public class MapActivity extends AppCompatActivity {

    FirebaseAuth mAuth;
    MapView map=null;
    IMapController mapController=null;
    static int NEW_PLACE=1;
    static final int PERMISSION_ACCESS_FINE_LOCATION=1;
    private AppBarConfiguration appBarConfiguration;
    private ActivityMapBinding binding;
    MyLocationNewOverlay myLocationOverlay;
    ItemizedIconOverlay myPlacesOverlay=null;


    public static final int SHOW_MAP=0;
    public static final int CENTER_PLACE_ON_MAP=1;
    public static final int SELECT_COORDINATES=2;

    private int state=0;
    private boolean selCoorsEnabled=false;
    private GeoPoint placeLoc;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        mAuth=FirebaseAuth.getInstance();
        super.onCreate(savedInstanceState);
        getSupportActionBar().setDisplayShowTitleEnabled(false);

        // Listener za promenu liste
        MyPlacesData.getInstance().setEventListener(new MyPlacesData.ListUpdatedEventListener() {
            @Override
            public void onListUpdated() {
               // showMyPlaces(); // ? ? ?
                setupMap();
            }
        });

        //
        try{
            Intent mapIntent=getIntent();
            Bundle mapBundle=mapIntent.getExtras();
            if(mapBundle!=null)
            {
                state=mapBundle.getInt("state");
            }
            if(state==CENTER_PLACE_ON_MAP){
                String placeLat=mapBundle.getString("lat");
                String placeLon=mapBundle.getString("lon");
                placeLoc=new GeoPoint(Double.parseDouble(placeLat),Double.parseDouble(placeLon));
            }
        }
        catch(Exception e){
            Log.d("Error","Error reading state");
        }

     binding = ActivityMapBinding.inflate(getLayoutInflater());
     setContentView(binding.getRoot());

        setSupportActionBar(binding.toolbar);

        if(state!=SELECT_COORDINATES){

        }else{
        }

        Context ctx=getApplicationContext();
        Configuration.getInstance().load(ctx, PreferenceManager.getDefaultSharedPreferences(ctx));
        map=(MapView) findViewById(R.id.mapM);
        map.setMultiTouchControls(true);

        if(ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION)!= PackageManager.PERMISSION_GRANTED
              && ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_COARSE_LOCATION)!= PackageManager.PERMISSION_GRANTED)
        {
            ActivityCompat.requestPermissions(this, new String[]{Manifest.permission.ACCESS_FINE_LOCATION}, PERMISSION_ACCESS_FINE_LOCATION);
        }
        else
        {
            //setMyLocationOverlay();
            //setOnMapClickOverlay();
            setupMap();
        }
        mapController=map.getController();
        if(mapController!=null){
            mapController.setZoom(15.0);
            GeoPoint startPoint=new GeoPoint(43.3209,21.8958);
            mapController.setCenter(startPoint);
        }
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        getSupportActionBar().setDisplayShowHomeEnabled(true);

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

    private void setMyLocationOverlay() {
        myLocationOverlay=new MyLocationNewOverlay(new GpsMyLocationProvider(this),map);
        myLocationOverlay.enableMyLocation();
        map.getOverlays().add(this.myLocationOverlay);
        mapController=map.getController();
        if(mapController!=null){
            mapController.setZoom(15.0);
            myLocationOverlay.enableMyLocation();
        }
    }

    @SuppressLint("MissingPermission")
    @Override
    public void onRequestPermissionsResult(int requestCode, String permisssions[], int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permisssions, grantResults);
        switch (requestCode) {
            case PERMISSION_ACCESS_FINE_LOCATION: {
                if (grantResults.length > 0 && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                    //setMyLocationOverlay();
                    // setOnMapClickOverlay();
                    setupMap();
                }
                return;
            }
        }
    }
    @Override
    public void onResume()
    {
        super.onResume();
        map.onResume();
    }
    @Override
    public void onPause()
    {
        super.onPause();
        map.onPause();
    }
    @RequiresApi(api = Build.VERSION_CODES.HONEYCOMB)
    @Override
    public boolean onCreateOptionsMenu(Menu item){
        if(state==SELECT_COORDINATES && !selCoorsEnabled){

            MenuItem a=item.add(0,1,1, "Select Coordinates");
            a.setIcon(R.drawable.baseline_add_circle_outline_white_24dp);
            a.setShowAsAction(MenuItem.SHOW_AS_ACTION_IF_ROOM);
            MenuItem b=item.add(0,2,2,"Cancel");
            b.setIcon(R.drawable.baseline_cancel_white_24dp);
            b.setShowAsAction(MenuItem.SHOW_AS_ACTION_IF_ROOM);
            return super.onCreateOptionsMenu(item);
        }
        else {
            return true;
        }
    }
    @Override
    public boolean onSupportNavigateUp() {
        onBackPressed();
        return true;
    }

    private void setOnMapClickOverlay(){
        MapEventsReceiver mReceive=new MapEventsReceiver() {
            @Override
            public boolean singleTapConfirmedHelper(GeoPoint p) {
                if(state==SELECT_COORDINATES && selCoorsEnabled) {
                    String lon = Double.toString(p.getLongitude());
                    String lat = Double.toString(p.getLatitude());
                    Intent locationIntent = new Intent();
                    locationIntent.putExtra("lon", lon);
                    locationIntent.putExtra("lat", lat);
                    setResult(Activity.RESULT_OK, locationIntent);
                    finish();
                }
                return false;
            }

            @Override
            public boolean longPressHelper(GeoPoint p) {
                return false;
            }
        };
        MapEventsOverlay OverlayEvents=new MapEventsOverlay(mReceive);
        map.getOverlays().add(OverlayEvents);
    }
    private void setCenterPlaceOnMap(){
        mapController=map.getController();
        if(mapController!=null){
            mapController.setZoom(15.0);
            mapController.animateTo(placeLoc);
        }
    }
    private void setupMap(){
        switch(state){
            case SHOW_MAP:
                setMyLocationOverlay();
                break;
            case SELECT_COORDINATES:
                mapController=map.getController();
                if(mapController!=null){
                    mapController.setZoom(15.0);
                    mapController.setCenter(new GeoPoint(43.3209,21.8958));
                }
                setOnMapClickOverlay();
                break;
            case CENTER_PLACE_ON_MAP:
            default: setCenterPlaceOnMap();
            break;
        }
        showMyPlaces();
    }
    private void showMyPlaces(){
        if(myPlacesOverlay!=null)
            map.getOverlays().remove(myPlacesOverlay);
        final ArrayList<OverlayItem> overlayArrayList = new ArrayList<>();
        for(int i = 0; i<MyPlacesData.getInstance().getMyPlaces().size();i++){
            MyPlace mp = MyPlacesData.getInstance().getPlace(i);
            OverlayItem overlayItem = new OverlayItem(mp.name,mp.description,new GeoPoint(Double.parseDouble(mp.latitude),Double.parseDouble(mp.longitude)));
            overlayItem.setMarker(this.getResources().getDrawable(R.drawable.baseline_myplace));
            overlayArrayList.add(overlayItem);
        }
        Toast.makeText(this,"My Places!", Toast.LENGTH_LONG).show();
        myPlacesOverlay = new ItemizedIconOverlay<>(overlayArrayList, new ItemizedIconOverlay.OnItemGestureListener<OverlayItem>() {
            @Override
            public boolean onItemSingleTapUp(int index, OverlayItem item) {
                Intent intent = new Intent(MapActivity.this, ProfileActivity.class);
                intent.putExtra("position",index);
                startActivity(intent);
                return true;
            }

            @Override
            public boolean onItemLongPress(int index, OverlayItem item) {
                Intent intent = new Intent(MapActivity.this, AddCaseActivity.class);
                intent.putExtra("position",index);
                startActivityForResult(intent,5);
                return true;
            }
        },getApplicationContext());
        this.map.getOverlays().add(myPlacesOverlay);
    }
    @Override
    protected void onActivityResult(int requestCode, int resultCode,Intent data)
    {
        super.onActivityResult(requestCode,resultCode,data);
        if (resultCode==Activity.RESULT_OK)
            showMyPlaces();
    }
}