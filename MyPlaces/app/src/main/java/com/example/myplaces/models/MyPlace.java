package com.example.myplaces.models;

import androidx.annotation.NonNull;

import com.google.firebase.database.Exclude;
import com.google.firebase.database.IgnoreExtraProperties;

@IgnoreExtraProperties
public class MyPlace {
    public String animalType,uid;
    public String description;
    public String longitude;
    public String latitude;
    public String picture;
    public String firstName,lastName,phone;
    public Boolean food,medicine,water,vet,adoption;
    @Exclude
    public String key;
    public MyPlace(){}
    public MyPlace(String animalType, String description,String longitude,String latitude,String picture,Boolean food,Boolean medicine,Boolean water,Boolean vet,Boolean adoption,String first,String last,String phone,String uid)
    {
        this.animalType=animalType;
        this.description=description;
        this.longitude=longitude;
        this.latitude=latitude;
        this.picture=picture;
        this.food=food;
        this.vet=vet;
        this.medicine=medicine;
        this.water=water;
        this.adoption=adoption;
        this.phone=phone;
        this.firstName=first;
        this.lastName=last;
        this.uid=uid;
    }
    @Override
    public String toString() {
        return this.animalType;
    }
}
