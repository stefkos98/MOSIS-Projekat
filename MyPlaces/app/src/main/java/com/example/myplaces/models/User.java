package com.example.myplaces.models;

import android.graphics.drawable.Drawable;

import com.google.firebase.database.Exclude;
import com.google.firebase.database.IgnoreExtraProperties;

import java.util.ArrayList;
import java.util.HashMap;
@IgnoreExtraProperties
public class User {
    public String latitude,longitude;
    public String username;
    public String email;
    public String firstName;
    public String lastName;
    public String phone;
    public String picture;
    public Boolean share;
    public Long points;
    public HashMap<String,String> requests;
    public HashMap<String,String> friends;
    @Exclude
    public String key;
    @Exclude
    public byte[] Uimage;
    public User() {
    }
}