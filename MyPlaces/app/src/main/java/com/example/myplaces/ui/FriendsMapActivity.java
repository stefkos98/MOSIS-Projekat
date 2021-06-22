package com.example.myplaces.ui;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;

import android.Manifest;
import android.annotation.SuppressLint;
import android.app.AlertDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.content.res.Resources;
import android.graphics.BitmapFactory;
import android.graphics.drawable.BitmapDrawable;
import android.graphics.drawable.Drawable;
import android.location.Location;
import android.location.LocationListener;
import android.location.LocationManager;
import android.os.Bundle;
import android.preference.PreferenceManager;
import android.util.Log;

import com.example.myplaces.R;
import com.example.myplaces.models.User;
import com.example.myplaces.models.UsersData;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.StorageReference;

import org.osmdroid.api.IMapController;
import org.osmdroid.config.Configuration;
import org.osmdroid.util.GeoPoint;
import org.osmdroid.views.MapView;
import org.osmdroid.views.overlay.ItemizedIconOverlay;
import org.osmdroid.views.overlay.Marker;
import org.osmdroid.views.overlay.Overlay;
import org.osmdroid.views.overlay.OverlayItem;
import org.osmdroid.views.overlay.mylocation.GpsMyLocationProvider;
import org.osmdroid.views.overlay.mylocation.MyLocationNewOverlay;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;

public class FriendsMapActivity extends AppCompatActivity implements LocationListener {
    MapView map = null;
    IMapController mapController = null;
    FirebaseAuth mAuth;
    FirebaseUser user;
    DatabaseReference database;
    static final int PERMISSION_ACCESS_FINE_LOCATION = 1;
    MyLocationNewOverlay myLocationOverlay;
    ItemizedIconOverlay myUsersOverlay = null;
    private int state = 0;
    private GeoPoint placeLoc;
    private Location location;
    LocationManager locationManager;
    String uid;
    ArrayList<User> friends;
    ArrayList<User> notfriends;
    ArrayList<String> ids;
    ArrayList<String> friendids;
    ArrayList<String> notfriendids;
    User ulogovanUser;
    StorageReference storageRef;
    HashMap<String, Drawable> hm;
    Resources Resources;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        UsersData.getInstance().setEventListener(new UsersData.ListUpdatedEventListener() {
            @Override
            public void onListUpdated() {
                setupMap();
            }
        });
        setContentView(R.layout.activity_friends_map);
        Context ctx = getApplicationContext();
        Configuration.getInstance().load(ctx, PreferenceManager.getDefaultSharedPreferences(ctx));
        map = (MapView) findViewById(R.id.mapFM);
        map.setMultiTouchControls(true);
        //map.setClickable(false);
        database = FirebaseDatabase.getInstance().getReference();
        locationManager = (LocationManager) getSystemService(Context.LOCATION_SERVICE);
        storageRef = FirebaseStorage.getInstance().getReference();
        mAuth = FirebaseAuth.getInstance();
        user = mAuth.getCurrentUser();
        uid = user.getUid();
        ids = new ArrayList<>();
        friends = new ArrayList<>();
        notfriends = new ArrayList<>();
        friendids = new ArrayList<>();
        notfriendids = new ArrayList<>();
        hm = new HashMap<>();
        Resources = this.getResources();


        mapController = map.getController();
        if (mapController != null) {
            mapController.setZoom(14.0);
            GeoPoint startPoint = new GeoPoint(43.3209, 21.8958);
            mapController.setCenter(startPoint);
        }
        if (ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED
                && ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_COARSE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
            ActivityCompat.requestPermissions(this, new String[]{Manifest.permission.ACCESS_FINE_LOCATION}, PERMISSION_ACCESS_FINE_LOCATION);
        } else {
            setupMap();
            locationManager.requestLocationUpdates(LocationManager.GPS_PROVIDER, 500, 5, (LocationListener) this);
        }
        //setupMap();
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
    public void onLocationChanged(@NonNull Location location) {
        this.location = location;
        setupMap();
    }

    private void setMyLocationOverlay() {
        myLocationOverlay = new MyLocationNewOverlay(new GpsMyLocationProvider(this), map);
        myLocationOverlay.enableMyLocation();
        map.getOverlays().add(this.myLocationOverlay);
        mapController = map.getController();
        if (mapController != null) {
            mapController.setZoom(14.0);
            myLocationOverlay.enableFollowLocation();
        }
    }

    private void setupMap() {
        setMyLocationOverlay();
        showMyPlaces();
    }

    private void showMyPlaces() {
      /* if (myUsersOverlay != null)
            for (int l = 0; l < map.getOverlays().size(); l++) {
                Overlay o = map.getOverlays().get(l);
                map.getOverlays().remove(o);
            }*/
        if (myUsersOverlay != null)
            map.getOverlays().remove(myUsersOverlay);
        map.setMultiTouchControls(true);
        final ArrayList<OverlayItem> overlayArrayList = new ArrayList<>();
        final ArrayList<User> lista = new ArrayList<>();
        for (User u : UsersData.getInstance().getUsers()) {
            if (u.key.equals(uid))
                ulogovanUser = u;
        }
        if (ulogovanUser!=null) {
            //final User mp2 = UsersData.getInstance().getUser(UsersData.getInstance().getUsers().size() - 1);
            int j=UsersData.getInstance().getUsers().size();
            for (int i = 0; i <j; i++) {
                User mp = UsersData.getInstance().getUser(i);
                Drawable image = new BitmapDrawable(getResources(), BitmapFactory.decodeByteArray(mp.Uimage, 0, mp.Uimage.length));
                image.setBounds(0, 0, 50, 50);
                OverlayItem overlayItem;
                if (mp.key.equals(uid)) {
                    overlayItem = new OverlayItem(mp.username, mp.email, new GeoPoint(location.getLatitude(), location.getLongitude()));
                    overlayItem.setMarker(this.getResources().getDrawable(R.drawable.current_user));
                } else {
                    overlayItem = new OverlayItem(mp.username, mp.email, new GeoPoint(Double.parseDouble(mp.latitude), Double.parseDouble(mp.longitude)));
                   if (ulogovanUser.friends!=null && ulogovanUser.friends.containsKey(mp.key)) {
                        overlayItem.setMarker(image);
                    } else {
                        overlayItem.setMarker(this.getResources().getDrawable(R.drawable.user));
                    }
                }
                overlayArrayList.add(overlayItem);
                lista.add(mp);
            }
            myUsersOverlay = new ItemizedIconOverlay<>(overlayArrayList, new ItemizedIconOverlay.OnItemGestureListener<OverlayItem>() {
                @Override
                public boolean onItemSingleTapUp(int index, OverlayItem item) {
                    User mpu = lista.get(index);
                    if (mpu.key.equals(uid)) {
                        Intent intent = new Intent(FriendsMapActivity.this, ProfileActivity.class);
                        intent.putExtra("position", index);
                        startActivity(intent);
                    } else if (ulogovanUser.friends.containsKey(mpu.key)) {
                        Intent intent = new Intent(FriendsMapActivity.this, FriendActivity.class);
                        intent.putExtra("position", index);
                        startActivity(intent);
                    } else {
                        if ((mpu.requests == null || !mpu.requests.containsKey(uid)) && (ulogovanUser.requests == null || !ulogovanUser.requests.containsKey(mpu.key))) {
                            AlertDialog.Builder builder1 = new AlertDialog.Builder(FriendsMapActivity.this);
                            builder1.setMessage("Do you want to send request to " + mpu.username + "?");
                            builder1.setCancelable(true);

                            builder1.setPositiveButton(
                                    "Yes",
                                    new DialogInterface.OnClickListener() {
                                        public void onClick(DialogInterface dialog, int id) {
                                            database.child("users").child(mpu.key).child("requests").child(uid).setValue(ulogovanUser.username);
                                            dialog.cancel();
                                        }
                                    });

                            builder1.setNegativeButton(
                                    "No",
                                    new DialogInterface.OnClickListener() {
                                        public void onClick(DialogInterface dialog, int id) {
                                            dialog.cancel();
                                        }
                                    });

                            AlertDialog alert11 = builder1.create();
                            alert11.show();
                        } else if (mpu.requests != null && mpu.requests.containsKey(uid)) {
                            AlertDialog.Builder builder1 = new AlertDialog.Builder(FriendsMapActivity.this);
                            builder1.setMessage("Request is already sent to " + mpu.username + "!");
                            builder1.setNeutralButton(
                                    "Ok",
                                    new DialogInterface.OnClickListener() {
                                        public void onClick(DialogInterface dialog, int id) {
                                            dialog.cancel();
                                        }
                                    });
                            AlertDialog alert11 = builder1.create();
                            alert11.show();
                        } else {
                            AlertDialog.Builder builder1 = new AlertDialog.Builder(FriendsMapActivity.this);
                            builder1.setMessage("User " + mpu.username + " has already sent request to you!");
                            builder1.setNeutralButton(
                                    "Ok",
                                    new DialogInterface.OnClickListener() {
                                        public void onClick(DialogInterface dialog, int id) {
                                            dialog.cancel();
                                        }
                                    });
                            AlertDialog alert11 = builder1.create();
                            alert11.show();
                        }
                    }
                    return true;
                }

                @Override
                public boolean onItemLongPress(int index, OverlayItem item) {
                    return false;
                }
            }, getApplicationContext());
            map.getOverlays().add(myUsersOverlay);

        }else{
            myUsersOverlay = new ItemizedIconOverlay<>(overlayArrayList, new ItemizedIconOverlay.OnItemGestureListener<OverlayItem>() {
                @Override
                public boolean onItemSingleTapUp(int index, OverlayItem item) {
                return true;}

                @Override
                public boolean onItemLongPress(int index, OverlayItem item) {
                    return false;
                }
            },getApplicationContext());
            map.getOverlays().add(myUsersOverlay);
        }
    }
}