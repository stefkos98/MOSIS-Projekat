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
    ArrayList<OverlayItem> overlayArrayList;
    Resources Resources;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_friends_map);
        Context ctx = getApplicationContext();
        Configuration.getInstance().load(ctx, PreferenceManager.getDefaultSharedPreferences(ctx));
        map = (MapView) findViewById(R.id.mapFM);
        map.setMultiTouchControls(true);

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

        ulogovanUser = database.child("users").child(uid).get().getResult().getValue(User.class);

        mapController = map.getController();
        if (mapController != null) {
            mapController.setZoom(14.0);
            GeoPoint startPoint = new GeoPoint(43.3209, 21.8958);
            mapController.setCenter(startPoint);
        }

        overlayArrayList = new ArrayList<>();
        if (myUsersOverlay != null)
            map.getOverlays().remove(myUsersOverlay);
        UsersData.getInstance().setEventListener(new UsersData.ListUpdatedEventListener() {
            @Override
            public void onListUpdated() {
                setupMap();
            }
        });
        if (ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED
                && ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_COARSE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
            ActivityCompat.requestPermissions(this, new String[]{Manifest.permission.ACCESS_FINE_LOCATION}, PERMISSION_ACCESS_FINE_LOCATION);
        } else {
            setupMap();
            locationManager.requestLocationUpdates(LocationManager.GPS_PROVIDER, 500, 5, (LocationListener) this);
        }

        for (Map.Entry<String, String> e : ulogovanUser.friends.entrySet()) {
            ids.add(e.getKey());
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
        database.child("users").child(user.getUid()).child("latitude").setValue(String.valueOf(this.location.getLatitude()));
        database.child("users").child(user.getUid()).child("longitude").setValue(String.valueOf(this.location.getLongitude()));
        setMyLocationOverlay();
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

    private void setupMap() {
        setMyLocationOverlay();
        showMyPlaces();
    }

    private void showMyPlaces() {
        for (int i = 0; i < UsersData.getInstance().getUsers().size(); i++) {
            User mp = UsersData.getInstance().getUser(i);
            final long ONE_MEGABYTE = 1024 * 1024;
            storageRef.child("images").child(mp.email + ".jpg").getBytes(ONE_MEGABYTE).addOnSuccessListener(new OnSuccessListener<byte[]>() {
                @Override
                public void onSuccess(byte[] bytes) {
                    Drawable image = new BitmapDrawable(getResources(), BitmapFactory.decodeByteArray(bytes, 0, bytes.length));
                    image.setBounds(0, 0, 50, 50);
                    OverlayItem overlayItem = new OverlayItem(mp.username, mp.email, new GeoPoint(Double.parseDouble(mp.latitude), Double.parseDouble(mp.longitude)));
                    if (mp.key.equals(uid))
                        overlayItem.setMarker(Resources.getDrawable(R.drawable.current_user));
                    else {
                        if (ulogovanUser.friends.containsKey(mp.key)) {
                            overlayItem.setMarker(image);
                        } else {
                            overlayItem.setMarker(Resources.getDrawable(R.drawable.user));
                        }
                    }
                    overlayArrayList.add(overlayItem);
                }
            }).addOnFailureListener(new OnFailureListener() {
                @Override
                public void onFailure(@NonNull Exception exception) {
                    // Handle any errors
                }
            });
        }
        myUsersOverlay = new ItemizedIconOverlay<>(overlayArrayList, new ItemizedIconOverlay.OnItemGestureListener<OverlayItem>() {
            @Override
            public boolean onItemSingleTapUp(int index, OverlayItem item) {
                float[] distance = new float[2];
                User mp = UsersData.getInstance().getUser(index);
                if (mp.key.equals(uid)) {
                    Intent intent = new Intent(FriendsMapActivity.this, ProfileActivity.class);
                    intent.putExtra("position", index);
                    startActivity(intent);
                } else if (ulogovanUser.friends.containsKey(mp.key)) {
                    Intent intent = new Intent(FriendsMapActivity.this, FriendActivity.class);
                    intent.putExtra("position", index);
                    startActivity(intent);
                } else {
                    if (mp.requests == null || (!mp.requests.containsKey(uid) && !ulogovanUser.requests.containsKey(mp.key))) {
                        AlertDialog.Builder builder1 = new AlertDialog.Builder(FriendsMapActivity.this);
                        builder1.setMessage("Do you want to send request to " + mp.username + "?");
                        builder1.setCancelable(true);

                        builder1.setPositiveButton(
                                "Yes",
                                new DialogInterface.OnClickListener() {
                                    public void onClick(DialogInterface dialog, int id) {
                                        database.child("users").child(mp.key).child("requests").child(uid).setValue(ulogovanUser.username);
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
                    } else if (!ulogovanUser.requests.containsKey(mp.key)) {
                        AlertDialog.Builder builder1 = new AlertDialog.Builder(FriendsMapActivity.this);
                        builder1.setMessage("Request is already sent to " + mp.username + "!");
                        builder1.setCancelable(true);

                        builder1.setPositiveButton(
                                "Ok",
                                new DialogInterface.OnClickListener() {
                                    public void onClick(DialogInterface dialog, int id) {
                                        dialog.cancel();
                                    }
                                });

                        builder1.setNegativeButton(
                                "Cancel",
                                new DialogInterface.OnClickListener() {
                                    public void onClick(DialogInterface dialog, int id) {
                                        dialog.cancel();
                                    }
                                });

                        AlertDialog alert11 = builder1.create();
                        alert11.show();
                    } else {
                        AlertDialog.Builder builder1 = new AlertDialog.Builder(FriendsMapActivity.this);
                        builder1.setMessage("User " + mp.username + " is already sent request to you!");
                        builder1.setCancelable(true);

                        builder1.setPositiveButton(
                                "Ok",
                                new DialogInterface.OnClickListener() {
                                    public void onClick(DialogInterface dialog, int id) {
                                        dialog.cancel();
                                    }
                                });

                        builder1.setNegativeButton(
                                "Cancel",
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
        this.map.getOverlays().add(myUsersOverlay);
    }
}