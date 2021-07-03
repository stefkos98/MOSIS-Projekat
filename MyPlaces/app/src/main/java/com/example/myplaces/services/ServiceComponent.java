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
import android.graphics.Color;
import android.location.Location;
import android.location.LocationListener;
import android.location.LocationManager;
import android.os.Build;
import android.os.IBinder;
import android.widget.Toast;

import androidx.annotation.RequiresApi;
import androidx.core.app.ActivityCompat;
import androidx.core.app.NotificationCompat;
import androidx.core.app.NotificationManagerCompat;

import com.example.myplaces.R;
import com.example.myplaces.models.MyPlace;
import com.example.myplaces.models.MyPlacesData;
import com.example.myplaces.models.User;
import com.example.myplaces.models.UsersData;
import com.example.myplaces.ui.FriendsMapActivity;
import com.example.myplaces.ui.HomeActivity;
import com.example.myplaces.ui.MapActivity;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;

public class ServiceComponent extends Service implements LocationListener {
    private static final String CHANNEL_1_ID = "channel1";
    private static final String CHANNEL_2_ID = "channel2";

    public ServiceComponent() {
    }

    FirebaseAuth mAuth;
    FirebaseUser user;
    Location location;
    String userID;
    DatabaseReference database;
    LocationManager locationManager;
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

            NotificationChannel channel2 = new NotificationChannel(
                    CHANNEL_2_ID,
                    "Channel 2",
                    NotificationManager.IMPORTANCE_HIGH
            );
            channel2.setDescription("This is Channel 2");
            NotificationManager manager2 = getSystemService(NotificationManager.class);
            manager2.createNotificationChannel(channel2);
        }

        UsersData.getInstance().setEventListener(new UsersData.ListUpdatedEventListener() {
            @Override
            public void
            onListUpdated() {
                for (User u : UsersData.getInstance().getUsers())
                    if (!u.key.equals(userID) && location != null) {
                        float[] distance = new float[2];
                        Location.distanceBetween(Double.parseDouble(u.latitude), Double.parseDouble(u.longitude), location.getLatitude(),
                                location.getLongitude(), distance);
                        if (distance[0] < 100 && u.share)
                            sendOnChannel1("User is near", u.username + " is near. Click to see map!");
                    }
            }
        });
        MyPlacesData.getInstance().setEventListener(new MyPlacesData.ListUpdatedEventListener() {
            @Override
            public void
            onListUpdated() {
                for (MyPlace mp : MyPlacesData.getInstance().getMyPlaces())
                    if (location != null) {
                        float[] distance = new float[2];
                        Location.distanceBetween(Double.parseDouble(mp.latitude), Double.parseDouble(mp.longitude), location.getLatitude(),
                                location.getLongitude(), distance);
                        if (distance[0] < 100)
                            sendOnChannel2("Help needed", " One " + mp.animalType + " needs help. Click to see where it is!");
                    }
            }
        });
        // FOREGROUND SERVICE
        Intent notificationIntent = new Intent(this, HomeActivity.class);

        PendingIntent pendingIntent = PendingIntent.getActivity(this, 0,
                notificationIntent, 0);

        Notification notification = new NotificationCompat.Builder(this)
                .setSmallIcon(R.drawable.baseline_info_white_24dp)
                .setContentTitle("Pet Care")
                .setContentText("Background service active")
                .setContentIntent(pendingIntent).build();
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O)
            startMyOwnForeground();
        else
            startForeground(1, notification);
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
        Toast.makeText(this,"KRAAJ",Toast.LENGTH_SHORT).show();
        locationManager.removeUpdates(this);
        stopForeground(true);
        stopSelf();
    }

    @Override
    public void onLocationChanged(Location location) {
        this.location = location;
        database.child("users").child(user.getUid()).child("latitude").setValue(String.valueOf(this.location.getLatitude()));
        database.child("users").child(user.getUid()).child("longitude").setValue(String.valueOf(this.location.getLongitude()));


        for (User u : UsersData.getInstance().getUsers())
            if (!u.key.equals(userID)) {
                float[] distance = new float[2];
                Location.distanceBetween(Double.parseDouble(u.latitude), Double.parseDouble(u.longitude), location.getLatitude(),
                        location.getLongitude(), distance);
                if (distance[0] < 100 && u.share)
                    sendOnChannel1("User is near", u.username + " is near. Click to see map!");
            }

        for (MyPlace mp : MyPlacesData.getInstance().getMyPlaces())
            if (location != null) {
                float[] distance = new float[2];
                Location.distanceBetween(Double.parseDouble(mp.latitude), Double.parseDouble(mp.longitude), location.getLatitude(),
                        location.getLongitude(), distance);
                if (distance[0] < 100)
                    sendOnChannel2("Help needed", "One " + mp.animalType + " needs help. Click to see where it is!");
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
        //intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
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

    public void sendOnChannel2(String title, String message) {
        Intent intent = new Intent(this, MapActivity.class);
        //intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
        PendingIntent pendingIntent = PendingIntent.getActivity(this, 0, intent, 0);

        Notification notification = new NotificationCompat.Builder(this, CHANNEL_2_ID)
                .setSmallIcon(R.drawable.baseline_myplace)
                .setContentTitle(title)
                .setContentText(message)
                .setPriority(NotificationCompat.PRIORITY_HIGH)
                .setCategory(NotificationCompat.CATEGORY_MESSAGE)
                .setContentIntent(pendingIntent)
                .setAutoCancel(true)
                .build();
        nm.notify(2, notification);
    }
    @RequiresApi(api = Build.VERSION_CODES.O)
    private void startMyOwnForeground(){
        String NOTIFICATION_CHANNEL_ID = "com.example.simpleapp";
        String channelName = "My Background Service";
        NotificationChannel chan = new NotificationChannel(NOTIFICATION_CHANNEL_ID, channelName, NotificationManager.IMPORTANCE_NONE);
        chan.setLightColor(Color.BLUE);
        chan.setLockscreenVisibility(Notification.VISIBILITY_PRIVATE);
        NotificationManager manager = (NotificationManager) getSystemService(Context.NOTIFICATION_SERVICE);
        assert manager != null;
        manager.createNotificationChannel(chan);

        NotificationCompat.Builder notificationBuilder = new NotificationCompat.Builder(this, NOTIFICATION_CHANNEL_ID);
        Notification notification = notificationBuilder.setOngoing(true)
                .setSmallIcon(R.drawable.baseline_info_white_24dp)
                .setContentTitle("App is running in background")
                .setPriority(NotificationManager.IMPORTANCE_MIN)
                .setCategory(Notification.CATEGORY_SERVICE)
                .build();
        startForeground(1338, notification);
    }
}