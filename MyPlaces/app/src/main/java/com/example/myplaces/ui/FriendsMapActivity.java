package com.example.myplaces.ui;

import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;
import androidx.navigation.ui.AppBarConfiguration;

import android.Manifest;
import android.annotation.SuppressLint;
import android.app.Activity;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.pm.PackageManager;
import android.location.Location;
import android.location.LocationListener;
import android.location.LocationManager;
import android.os.Bundle;
import android.preference.PreferenceManager;
import android.util.Log;
import android.view.View;
import android.widget.SearchView;
import android.widget.Toast;

import com.example.myplaces.R;
import com.example.myplaces.databinding.ActivityMapBinding;
import com.example.myplaces.models.MyPlace;
import com.example.myplaces.models.MyPlacesData;
import com.example.myplaces.models.User;
import com.example.myplaces.models.UsersData;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;

import org.osmdroid.api.IMapController;
import org.osmdroid.config.Configuration;
import org.osmdroid.util.GeoPoint;
import org.osmdroid.views.MapView;
import org.osmdroid.views.overlay.ItemizedIconOverlay;
import org.osmdroid.views.overlay.OverlayItem;
import org.osmdroid.views.overlay.mylocation.GpsMyLocationProvider;
import org.osmdroid.views.overlay.mylocation.MyLocationNewOverlay;

import java.util.ArrayList;

public class FriendsMapActivity extends AppCompatActivity implements LocationListener {

    FirebaseAuth mAuth;
    FirebaseUser user;
    DatabaseReference database;
    MapView map = null;
    IMapController mapController = null;
    static final int PERMISSION_ACCESS_FINE_LOCATION = 1;
    private ActivityMapBinding binding;
    MyLocationNewOverlay myLocationOverlay;
    ItemizedIconOverlay myUsersOverlay = null;
    private int state = 0;
    private GeoPoint placeLoc;
    private Location location;
    LocationManager locationManager;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        //setContentView(R.layout.activity_friends_map);
        database=FirebaseDatabase.getInstance().getReference();
        locationManager = (LocationManager) getSystemService(Context.LOCATION_SERVICE);
        mAuth = FirebaseAuth.getInstance();
        user = mAuth.getCurrentUser();
        // Listener za promenu liste
        UsersData.getInstance().setEventListener(new UsersData.ListUpdatedEventListener() {
            @Override
            public void onListUpdated() {
                setupMap();
            }
        });

        //
        binding = ActivityMapBinding.inflate(getLayoutInflater());
        setContentView(binding.getRoot());
        //mapa
        Context ctx = getApplicationContext();
        Configuration.getInstance().load(ctx, PreferenceManager.getDefaultSharedPreferences(ctx));
        map = (MapView) findViewById(R.id.mapM);
        map.setMultiTouchControls(true);

        if (ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED
                && ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_COARSE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
            ActivityCompat.requestPermissions(this, new String[]{Manifest.permission.ACCESS_FINE_LOCATION}, PERMISSION_ACCESS_FINE_LOCATION);
        } else {
            setupMap();
            locationManager.requestLocationUpdates(LocationManager.GPS_PROVIDER, 500, 5, (LocationListener) this);
        }
        // gde da pokazuje mapa
        mapController = map.getController();
        if (mapController != null) {
            mapController.setZoom(15.0);
            GeoPoint startPoint = new GeoPoint(43.3209, 21.8958);
            mapController.setCenter(startPoint);
        }
        // za logout
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

    private void setMyLocationOverlay() {
        myLocationOverlay = new MyLocationNewOverlay(new GpsMyLocationProvider(this), map);
        myLocationOverlay.enableMyLocation();
        map.getOverlays().add(this.myLocationOverlay);
        mapController = map.getController();
        if (mapController != null) {
            mapController.setZoom(15.0);
            myLocationOverlay.enableFollowLocation();
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
                    locationManager.requestLocationUpdates(LocationManager.GPS_PROVIDER, 500, 5, (LocationListener) this);
                }
                return;
            }
        }
    }

    @Override
    public void onResume() {
        super.onResume();
        map.onResume();
    }

    @Override
    public void onPause() {
        super.onPause();
        map.onPause();
    }

    private void setupMap() {
        setMyLocationOverlay();
        showMyPlaces();
    }

    private void showMyPlaces() {
        if (myUsersOverlay != null)
            map.getOverlays().remove(myUsersOverlay);
        final ArrayList<OverlayItem> overlayArrayList = new ArrayList<>();
        for (int i = 0; i < UsersData.getInstance().getUsers().size(); i++) {
            User mp = UsersData.getInstance().getUser(i);
            OverlayItem overlayItem = new OverlayItem(mp.username, mp.email, new GeoPoint(Double.parseDouble(mp.latitude), Double.parseDouble(mp.longitude)));
            // AKO PRIJATELJI JEDNA SLICICA INACE DRUGA
            overlayItem.setMarker(this.getResources().getDrawable(R.drawable.user));
            overlayArrayList.add(overlayItem);
        }
        myUsersOverlay = new ItemizedIconOverlay<>(overlayArrayList, new ItemizedIconOverlay.OnItemGestureListener<OverlayItem>() {
            @Override
            public boolean onItemSingleTapUp(int index, OverlayItem item) {
                float[] distance = new float[2];
                MyPlace mp = MyPlacesData.getInstance().getPlace(index);
                Location.distanceBetween(Double.parseDouble(mp.latitude),
                        Double.parseDouble(mp.longitude), location.getLatitude(),
                        location.getLongitude(), distance);
                // AKO SU PRIJATELJI JEDAN INTENT AKO NISU DRUGI PROSLEDI index i ako je jednak useru
                if (mp.uid.equals(user.getUid())) {
                    Intent intent = new Intent(FriendsMapActivity.this, HelpActivity.class);
                    intent.putExtra("position", index);
                    startActivity(intent);
                } else if (distance[0] > 10) {
                    Intent intent = new Intent(FriendsMapActivity.this, ShowActivity.class);
                    intent.putExtra("position", index);
                    startActivity(intent);
                } else {
                    Intent intent = new Intent(FriendsMapActivity.this, HelpActivity.class);
                    intent.putExtra("position", index);
                    intent.putExtra("delete", false);
                    startActivity(intent);
                }

                return true;
            }

            @Override
            public boolean onItemLongPress(int index, OverlayItem item) {
                return false;
            }
        }, getApplicationContext());
        this.map.getOverlays().add(myUsersOverlay);
    }

    @Override
    public void onLocationChanged(Location location) {
        this.location = location;
        database.child("users").child(user.getUid()).child("latitude").setValue(String.valueOf(this.location.getLatitude()));
        database.child("users").child(user.getUid()).child("longitude").setValue(String.valueOf(this.location.getLongitude()));
        setMyLocationOverlay();
    }
}