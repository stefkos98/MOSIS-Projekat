package com.example.myplaces.models;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;

import com.google.firebase.database.ChildEventListener;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

import java.util.ArrayList;
import java.util.HashMap;

public class MyPlacesData {
    private ArrayList<MyPlace> myPlaces;
    private HashMap<String,Integer> myPlacesKeyIndexMapping;
    private DatabaseReference database;
    private static final String FIREBASE_CHILD="my-places";
    private MyPlacesData()
    {
        myPlaces=new ArrayList<>();
        myPlacesKeyIndexMapping=new HashMap<>();
        database= FirebaseDatabase.getInstance().getReference();
        database.child(FIREBASE_CHILD).addChildEventListener(childEventListener);
        database.child(FIREBASE_CHILD).addListenerForSingleValueEvent(parentEventListener);

    }
    ValueEventListener parentEventListener=new ValueEventListener() {
        @Override
        public void onDataChange( DataSnapshot snapshot) {
            if(updateListener!=null){
                updateListener.onListUpdated();
            }
        }

        @Override
        public void onCancelled(DatabaseError error) {

        }
    };
    ChildEventListener childEventListener=new ChildEventListener() {
        @Override
        public void onChildAdded(DataSnapshot snapshot, String previousChildName) {
            String myPlaceKey=snapshot.getKey();
            if(!myPlacesKeyIndexMapping.containsKey(myPlaceKey)){
                MyPlace myPlace=snapshot.getValue(MyPlace.class);
                myPlace.key=myPlaceKey;
                myPlaces.add(myPlace);
                myPlacesKeyIndexMapping.put(myPlaceKey,myPlaces.size()-1);
                if(updateListener!=null){
                    updateListener.onListUpdated();
                }
            }
        }

        @Override
        public void onChildChanged(DataSnapshot snapshot,  String previousChildName) {
            String myPlaceKey=snapshot.getKey();
            MyPlace myPlace=snapshot.getValue(MyPlace.class);
            myPlace.key=myPlaceKey;
            if(myPlacesKeyIndexMapping.containsKey(myPlaceKey)){
                int index=myPlacesKeyIndexMapping.get(myPlaceKey);
                myPlaces.set(index,myPlace);
            }else{
                myPlaces.add(myPlace);
                myPlacesKeyIndexMapping.put(myPlaceKey,myPlaces.size()-1);
            }
            if(updateListener!=null){
                updateListener.onListUpdated();
            }
        }

        @Override
        public void onChildRemoved(DataSnapshot snapshot) {
            String myPlaceKey=snapshot.getKey();
            if(myPlacesKeyIndexMapping.containsKey(myPlaceKey)){
                int index=myPlacesKeyIndexMapping.get(myPlaceKey);
                myPlaces.remove(index);
                recreateKeyIndexMapping();
            }
            if(updateListener!=null){
                updateListener.onListUpdated();
            }
        }

        @Override
        public void onChildMoved(DataSnapshot snapshot,  String previousChildName) {

        }

        @Override
        public void onCancelled(DatabaseError error) {

        }
    };
    private static class SingletonHolder {
        public static final MyPlacesData instance =new MyPlacesData();
    }
    public static MyPlacesData getInstance()
    {
        return SingletonHolder.instance;
    }
    public ArrayList<MyPlace> getMyPlaces()
    {
        return myPlaces;
    }
    public void addNewPlace (MyPlace place)
    {
        String key=database.push().getKey();
        myPlaces.add(place);
        myPlacesKeyIndexMapping.put(key,myPlaces.size()-1);
        database.child(FIREBASE_CHILD).child(key).setValue(place);
        place.key=key;
    }
    public MyPlace getPlace(int index)
    {
        return myPlaces.get(index);
    }
    public void deletePlace(int index)
    {
        database.child(FIREBASE_CHILD).child(myPlaces.get(index).key).removeValue();
        myPlaces.remove(index);
        recreateKeyIndexMapping();
    }
    public void updatePlace(int index,String nme,String desc,String lng,String lat){
        MyPlace myPlace=myPlaces.get(index);
        myPlace.name=nme;
        myPlace.description=desc;
        myPlace.longitude=lng;
        myPlace.latitude=lat;
        database.child(FIREBASE_CHILD).child(myPlace.key).setValue(myPlace);
    }

    private void recreateKeyIndexMapping() {
        myPlacesKeyIndexMapping.clear();
        for(int i=0;i<myPlaces.size();i++){
            myPlacesKeyIndexMapping.put(myPlaces.get(i).key,i);
        }
    }

    ListUpdatedEventListener updateListener;
    public void setEventListener(ListUpdatedEventListener listener){
        updateListener=listener;
    }
    public interface ListUpdatedEventListener{
        void onListUpdated();
    }
}
