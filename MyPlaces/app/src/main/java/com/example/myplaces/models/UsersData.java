package com.example.myplaces.models;

import android.graphics.BitmapFactory;
import android.graphics.drawable.BitmapDrawable;
import android.graphics.drawable.Drawable;
import android.util.Log;

import com.google.android.gms.auth.api.signin.internal.Storage;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.firebase.database.ChildEventListener;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.StorageReference;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;

public class UsersData {
    private ArrayList<User> users;
    private HashMap<String, Integer> usersKeyIndexMapping;
    private DatabaseReference database;
    private StorageReference storage;
    final long ONE_MEGABYTE = 1024 * 1024;

    private UsersData() {
        users = new ArrayList<>();
        usersKeyIndexMapping = new HashMap<>();
        database = FirebaseDatabase.getInstance().getReference();
        storage = FirebaseStorage.getInstance().getReference();
        database.child("users").addChildEventListener(childEventListener);
        database.child("users").addListenerForSingleValueEvent(parentEventListener);

    }

    ValueEventListener parentEventListener = new ValueEventListener() {
        @Override
        public void onDataChange(DataSnapshot snapshot) {
            if (updateListener != null) {
                updateListener.onListUpdated();
            }
        }

        @Override
        public void onCancelled(DatabaseError error) {

        }
    };
    ChildEventListener childEventListener = new ChildEventListener() {
        @Override
        public void onChildAdded(DataSnapshot snapshot, String previousChildName) {
            String userKey = snapshot.getKey();
            if (!usersKeyIndexMapping.containsKey(userKey)) {
                User user = snapshot.getValue(User.class);
                user.key = userKey;
                storage.child("images").child(user.email + ".jpg").getBytes(ONE_MEGABYTE).addOnSuccessListener(new OnSuccessListener<byte[]>() {
                    @Override
                    public void onSuccess(byte[] bytes) {
                        user.Uimage=new byte[bytes.length];
                        for (int i = 0; i < bytes.length; i++) {
                            user.Uimage[i] = bytes[i];
                        }
                        users.add(user);
                        usersKeyIndexMapping.put(userKey, users.size() - 1);
                        if (updateListener != null) {
                            updateListener.onListUpdated();
                        }
                    }
                });
            }
        }

        @Override
        public void onChildChanged(DataSnapshot snapshot, String previousChildName) {
            String userKey = snapshot.getKey();
            User user = snapshot.getValue(User.class);
            user.key = userKey;
            storage.child("images").child(user.email + ".jpg").getBytes(ONE_MEGABYTE).addOnSuccessListener(new OnSuccessListener<byte[]>() {
                @Override
                public void onSuccess(byte[] bytes) {
                    user.Uimage=new byte[bytes.length];
                    for (int i = 0; i < bytes.length; i++) {
                        user.Uimage[i] = bytes[i];
                    }
                    if (usersKeyIndexMapping.containsKey(userKey)) {
                        int index = usersKeyIndexMapping.get(userKey);
                        users.set(index, user);
                    } else {
                        users.add(user);
                        usersKeyIndexMapping.put(userKey, users.size() - 1);
                    }
                    if (updateListener != null) {
                        updateListener.onListUpdated();
                    }
                }
            });

        }

        @Override
        public void onChildRemoved(DataSnapshot snapshot) {
            String userKey = snapshot.getKey();
            if (usersKeyIndexMapping.containsKey(userKey)) {
                int index = usersKeyIndexMapping.get(userKey);
                users.remove(index);
                recreateKeyIndexMapping();
            }
            if (updateListener != null) {
                updateListener.onListUpdated();
            }
        }

        @Override
        public void onChildMoved(DataSnapshot snapshot, String previousChildName) {

        }

        @Override
        public void onCancelled(DatabaseError error) {

        }
    };

    private static class SingletonHolder {
        public static final UsersData instance = new UsersData();
    }

    public static UsersData getInstance() {
        return UsersData.SingletonHolder.instance;
    }

    public ArrayList<User> getUsers() {
        return users;
    }

    public void addNewUser(User user) {
        String key = database.push().getKey();
        users.add(user);
        usersKeyIndexMapping.put(key, users.size() - 1);
        database.child("users").child(key).setValue(user);
        user.key = key;
    }

    public User getUser(int index) {
        return users.get(index);
    }

    public void deleteUser(int index) {
        database.child("users").child(users.get(index).key).removeValue();
        users.remove(index);
        recreateKeyIndexMapping();
    }

    public void updateUser(int index, String username, String email, String firstName, String lastName, String phone, String picture, Long points, HashMap<String, String> requests, HashMap<String, String> friends, String latitude, String longitude, Boolean share) {
        User user = users.get(index);
        user.username = username;
        user.email = email;
        user.phone = phone;
        user.firstName = firstName;
        user.lastName = lastName;
        user.picture = picture;
        user.points = points;
        user.latitude = latitude;
        user.longitude = longitude;
        user.requests = new HashMap<>();
        user.share = share;
        for (Map.Entry<String, String> e : requests.entrySet())
            user.requests.put(e.getKey(), e.getValue());

        user.friends = new HashMap<>();
        for (Map.Entry<String, String> e : friends.entrySet())
            user.friends.put(e.getKey(), e.getValue());

        database.child("users").child(user.key).setValue(user);
    }

    private void recreateKeyIndexMapping() {
        usersKeyIndexMapping.clear();
        for (int i = 0; i < users.size(); i++) {
            usersKeyIndexMapping.put(users.get(i).key, i);
        }
    }

    UsersData.ListUpdatedEventListener updateListener;

    public void setEventListener(UsersData.ListUpdatedEventListener listener) {
        this.updateListener = listener;
    }

    public interface ListUpdatedEventListener {
        void onListUpdated();
    }
}
