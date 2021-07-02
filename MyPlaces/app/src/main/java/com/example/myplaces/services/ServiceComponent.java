package com.example.myplaces.services;

import android.Manifest;
import android.app.Notification;
import android.app.NotificationChannel;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.app.Service;
import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.location.Location;
import android.location.LocationListener;
import android.location.LocationManager;
import android.os.Build;
import android.os.IBinder;

import androidx.annotation.RequiresApi;
import androidx.core.app.ActivityCompat;
import androidx.core.app.NotificationCompat;
import androidx.core.app.NotificationManagerCompat;

import com.example.myplaces.R;
import com.example.myplaces.models.User;
import com.example.myplaces.models.UsersData;
import com.example.myplaces.ui.FriendsMapActivity;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;

public class ServiceComponent extends Service implements LocationListener {
    private static final String CHANNEL_1_ID = "channel1";
    private boolean allowRebind;
    private static final double latitudeconst = 0.5;
    private static final double longitudeconst = 0.5;
    public ServiceComponent() {
    }

    FirebaseAuth mAuth;
    FirebaseUser user;
    Location location;
    String userID;
    DatabaseReference database;
    LocationManager locationManager;
    static final int PERMISSION_ACCESS_FINE_LOCATION = 1;
    int notificationId = 1;
    NotificationManagerCompat nm;

    @RequiresApi(api = Build.VERSION_CODES.O)
    @Override
    public void onCreate() {
        super.onCreate();

        mAuth = FirebaseAuth.getInstance();
        user = mAuth.getCurrentUser();
        userID = user.getUid();
        database = FirebaseDatabase.getInstance().getReference();
        locationManager = (LocationManager) getSystemService(Context.LOCATION_SERVICE);
        getLocation();

        nm = NotificationManagerCompat.from(this);
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            NotificationChannel channel1 = new NotificationChannel(
                    CHANNEL_1_ID,
                    "Channel 1",
                    NotificationManager.IMPORTANCE_HIGH
            );
            channel1.setDescription("This is Channel 1");
            NotificationManager manager = getSystemService(NotificationManager.class);
            manager.createNotificationChannel(channel1);
        }

        for (User u : UsersData.getInstance().getUsers())
            if (!u.key.equals(userID)) {
                double dif1=Math.abs(Double.parseDouble(u.latitude)-location.getLatitude());
                double dif2=Math.abs(Double.parseDouble(u.longitude)-location.getLongitude());
                if(dif1<=latitudeconst && dif2<=longitudeconst) //da li treba da dodam i && u.share==true
                    sendOnChannel1("Some user is near!", u.username+" is near. Click to see map!");
            }
    }

    @Override
    public int onStartCommand(Intent intent, int flags, int startId) {
        getLocation();
        user = mAuth.getCurrentUser();
        userID = user.getUid();
        super.onStartCommand(intent, flags, startId);
        return START_NOT_STICKY;
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
        locationManager.removeUpdates(this);
        stopSelf();
    }

    @Override
    public void onLocationChanged(Location location) {
        this.location = location;
        database.child("users").child(user.getUid()).child("latitude").setValue(String.valueOf(this.location.getLatitude()));
        database.child("users").child(user.getUid()).child("longitude").setValue(String.valueOf(this.location.getLongitude()));


        for (User u : UsersData.getInstance().getUsers())
            if (!u.key.equals(userID)) {
                double dif1=Math.abs(Double.parseDouble(u.latitude)-location.getLatitude());
                double dif2=Math.abs(Double.parseDouble(u.longitude)-location.getLongitude());
                if(dif1<=latitudeconst && dif2<=longitudeconst) //da li treba da dodam i && u.share==true
                    sendOnChannel1("User is near", u.username+" is near. Click to see map!");
            }
    }

    private void getLocation() {
        if (ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
            stopSelf();
            return;
        }
        locationManager.requestLocationUpdates(LocationManager.GPS_PROVIDER, 500, 5, (LocationListener) this);
    }

    @Override
    public IBinder onBind(Intent intent) {
        // TODO: Return the communication channel to the service.
        return null;
    }

    public void sendOnChannel1(String title, String message) {
        Intent intent = new Intent(this, FriendsMapActivity.class);
        intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
        PendingIntent pendingIntent = PendingIntent.getActivity(this, 0, intent, 0);

        Notification notification = new NotificationCompat.Builder(this, CHANNEL_1_ID)
                .setSmallIcon(R.drawable.pin)
                .setContentTitle(title)
                .setContentText(message)
                .setPriority(NotificationCompat.PRIORITY_HIGH)
                .setCategory(NotificationCompat.CATEGORY_MESSAGE)
                .setContentIntent(pendingIntent)
                .setAutoCancel(true)
                .build();
        nm.notify(1, notification);
    }
}