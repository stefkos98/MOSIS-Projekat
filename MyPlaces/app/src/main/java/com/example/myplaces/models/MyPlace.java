package com.example.myplaces.models;

import androidx.annotation.NonNull;

import com.google.firebase.database.Exclude;
import com.google.firebase.database.IgnoreExtraProperties;

@IgnoreExtraProperties
public class MyPlace {
    public String animalType;
    public String description;
    public String longitude;
    public String latitude;
    public String picture;
    public Boolean food,medicine,water,vet,adoption;
    @Exclude
    public String key;
    public MyPlace(){}
    public MyPlace(String animalType, String description,String longitude,String latitude,String picture,Boolean food,Boolean medicine,Boolean water,Boolean vet,Boolean adoption)
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
    }
    @Override
    public String toString() {
        return this.animalType;
    }
}
