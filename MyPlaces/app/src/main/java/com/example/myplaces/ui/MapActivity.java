package com.example.myplaces.ui;

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
import com.google.firebase.auth.FirebaseUser;

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

public class MapActivity extends AppCompatActivity  implements LocationListener {

    FirebaseAuth mAuth;
    FirebaseUser user;
    MapView map = null;
    IMapController mapController = null;
    static int NEW_PLACE = 1;
    static final int PERMISSION_ACCESS_FINE_LOCATION = 1;
    private AppBarConfiguration appBarConfiguration;
    private ActivityMapBinding binding;
    MyLocationNewOverlay myLocationOverlay;
    ItemizedIconOverlay myPlacesOverlay = null;
    private int state = 0;
    private GeoPoint placeLoc;
    private Location location;
    LocationManager locationManager;
    protected LocationListener locationListener;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        locationManager = (LocationManager) getSystemService(Context.LOCATION_SERVICE);
        mAuth = FirebaseAuth.getInstance();
        user=mAuth.getCurrentUser();
        super.onCreate(savedInstanceState);
        // Listener za promenu liste
        MyPlacesData.getInstance().setEventListener(new MyPlacesData.ListUpdatedEventListener() {
            @Override
            public void onListUpdated() {
                setupMap();
            }
        });

        //
        binding = ActivityMapBinding.inflate(getLayoutInflater());
        setContentView(binding.getRoot());
       /* NavController navController = Navigation.findNavController(this, R.id.navnav_host_fragment_content_my_places_maps);
        appBarConfiguration = new AppBarConfiguration.Builder(navController.getGraph()).build();
        NavigationUI.setupActionBarWithNavController(this, navController, appBarConfiguration);*/
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
        findViewById(R.id.btnAddM).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Intent intent = new Intent(MapActivity.this, AddCaseActivity.class);
                intent.putExtra("latitude",location.getLatitude());
                intent.putExtra("longitude",location.getLongitude());
                startActivityForResult(intent,1);
            }
        });
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
        if (myPlacesOverlay != null)
            map.getOverlays().remove(myPlacesOverlay);
        final ArrayList<OverlayItem> overlayArrayList = new ArrayList<>();
        for (int i = 0; i < MyPlacesData.getInstance().getMyPlaces().size(); i++) {
            MyPlace mp = MyPlacesData.getInstance().getPlace(i);
            OverlayItem overlayItem = new OverlayItem(mp.animalType, mp.description, new GeoPoint(Double.parseDouble(mp.latitude), Double.parseDouble(mp.longitude)));
            overlayItem.setMarker(this.getResources().getDrawable(R.drawable.baseline_myplace));
            overlayArrayList.add(overlayItem);
        }
        myPlacesOverlay = new ItemizedIconOverlay<>(overlayArrayList, new ItemizedIconOverlay.OnItemGestureListener<OverlayItem>() {
            @Override
            public boolean onItemSingleTapUp(int index, OverlayItem item) {
                float[] distance=new float[2];
                MyPlace mp = MyPlacesData.getInstance().getPlace(index);
                Location.distanceBetween(Double.parseDouble(mp.latitude),
                        Double.parseDouble(mp.longitude), location.getLatitude(),
                        location.getLongitude(), distance);

                if (distance[0] > 10 || mp.uid.equals(user.getUid() )) {
                    Intent intent = new Intent(MapActivity.this, ShowActivity.class);
                    intent.putExtra("position", index);
                    startActivity(intent);
                }else {
                    Intent intent = new Intent(MapActivity.this, HelpActivity.class);
                    intent.putExtra("position", index);
                    startActivity(intent);                }

                return true;
            }

            @Override
            public boolean onItemLongPress(int index, OverlayItem item) {
                Intent intent = new Intent(MapActivity.this, AddCaseActivity.class);
                intent.putExtra("position", index);
                startActivityForResult(intent, 5);
                return true;
            }
        }, getApplicationContext());
        this.map.getOverlays().add(myPlacesOverlay);
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (resultCode == Activity.RESULT_OK)
            showMyPlaces();
    }
    @Override
    public void onLocationChanged(Location location) {
        this.location=location;
        setMyLocationOverlay();
    }
}