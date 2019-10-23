package com.ahmetyilmaz.mesvoyages;

import androidx.annotation.NonNull;
import androidx.core.content.ContextCompat;
import androidx.fragment.app.FragmentActivity;

import android.Manifest;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.pm.PackageManager;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteStatement;
import android.location.Address;
import android.location.Geocoder;
import android.location.Location;
import android.location.LocationListener;
import android.location.LocationManager;
import android.os.Build;
import android.os.Bundle;
import android.widget.Toast;

import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.OnMapReadyCallback;
import com.google.android.gms.maps.SupportMapFragment;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.MarkerOptions;

import java.io.IOException;
import java.util.List;
import java.util.Locale;

public class MapsActivity extends FragmentActivity implements OnMapReadyCallback,GoogleMap.OnMapLongClickListener {

    private GoogleMap mMap;


    LocationManager locationManager;
    LocationListener locationListener;
    static SQLiteDatabase database;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_maps);
        // Obtain the SupportMapFragment and get notified when the map is ready to be used.
        SupportMapFragment mapFragment = (SupportMapFragment) getSupportFragmentManager()
                .findFragmentById(R.id.map);
        mapFragment.getMapAsync(this);
    }



    @Override
    public void onMapReady(GoogleMap googleMap) {
        mMap = googleMap;

        //rooting operations is it new place or show old place on map
        Intent intent=getIntent();
        String info=intent.getStringExtra("info");
        if(info.matches("new")){



        mMap.setOnMapLongClickListener(this);

        locationManager = (LocationManager) this.getSystemService(Context.LOCATION_SERVICE);
        //all about location listening here
        locationListener = new LocationListener() {
            @Override
            public void onLocationChanged(Location location) {
                //test is it work ..

                //to have user basic info about first use
                SharedPreferences sharedPreferences = MapsActivity.this.getSharedPreferences("com.ahmetyilmaz.mesvoyages",MODE_PRIVATE);
                boolean firstTimeCheck = sharedPreferences.getBoolean("notFirstTime",false);

                if (!firstTimeCheck){
                    LatLng userLocation=new LatLng(location.getLatitude(),location.getLongitude());
                    mMap.moveCamera(CameraUpdateFactory.newLatLngZoom(userLocation,15));
                    sharedPreferences.edit().putBoolean("notFirstTime",true).apply();
                }

            }

            @Override
            public void onStatusChanged(String provider, int status, Bundle extras) {

            }

            @Override
            public void onProviderEnabled(String provider) {

            }

            @Override
            public void onProviderDisabled(String provider) {

            }
        };
//version control
        if (Build.VERSION.SDK_INT>=23){
            //control of permissions - > if there is no permission request it
            if (checkSelfPermission(Manifest.permission.ACCESS_FINE_LOCATION)!= PackageManager.PERMISSION_GRANTED){
                requestPermissions(new String[] {Manifest.permission.ACCESS_FINE_LOCATION},1);
            }else { //if there is permission request location information
                locationManager.requestLocationUpdates(LocationManager.GPS_PROVIDER,0,0,locationListener);

                mMap.clear();
                Location lastLocation = locationManager.getLastKnownLocation(LocationManager.GPS_PROVIDER);
                if (lastLocation != null) {
                    LatLng lastUserLocation=new LatLng(lastLocation.getLatitude(),lastLocation.getLongitude());
                    mMap.moveCamera(CameraUpdateFactory.newLatLngZoom(lastUserLocation,15));
                }


            }
        }
        //if version is smaller then 23 request permission NOT NECESSARY -> start directly asking location info
        else {
            locationManager.requestLocationUpdates(LocationManager.GPS_PROVIDER,0,0,locationListener);
            Location lastLocation = locationManager.getLastKnownLocation(LocationManager.GPS_PROVIDER);
            if (lastLocation != null) {
                LatLng lastUserLocation=new LatLng(lastLocation.getLatitude(),lastLocation.getLongitude());
                mMap.moveCamera(CameraUpdateFactory.newLatLngZoom(lastUserLocation,15));
            }
        }


    }else{ // if it is not new add operation-> to show old places
            mMap.clear();
            int position=intent.getIntExtra("position",0);
            LatLng location=new LatLng(MainActivity.locations.get(position).latitude,MainActivity.locations.get(position).longitude);
            String placeName=MainActivity.names.get(position);

            mMap.addMarker(new MarkerOptions().position(location).title(placeName));
            mMap.moveCamera(CameraUpdateFactory.newLatLngZoom(location,15));

        }
    }
    //if user need permission this function will be called
    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {

       if (grantResults.length>0){
           if (requestCode==0){
               if (ContextCompat.checkSelfPermission(this,Manifest.permission.ACCESS_FINE_LOCATION)==PackageManager.PERMISSION_GRANTED){
                   //if user has the permission interested -> get location info
                   locationManager.requestLocationUpdates(LocationManager.GPS_PROVIDER,0,0,locationListener);


                   //OPTIONAL if we need permission for old places (not probably)
                   Intent intent=getIntent();
                   String info=intent.getStringExtra("info");
                   if (info.matches("new")){

                       Location lastLocation = locationManager.getLastKnownLocation(LocationManager.GPS_PROVIDER);
                       if (lastLocation != null) {
                           LatLng lastUserLocation=new LatLng(lastLocation.getLatitude(),lastLocation.getLongitude());
                           mMap.moveCamera(CameraUpdateFactory.newLatLngZoom(lastUserLocation,15));
                       }

                   }else{
                       mMap.clear();
                       int position=intent.getIntExtra("position",0);
                       LatLng location=new LatLng(MainActivity.locations.get(position).latitude,MainActivity.locations.get(position).longitude);
                       String placeName=MainActivity.names.get(position);

                       mMap.addMarker(new MarkerOptions().position(location).title(placeName));
                       mMap.moveCamera(CameraUpdateFactory.newLatLngZoom(location,15));
                   }




               }
           }
       }
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);
    }

    @Override
    public void onMapLongClick(LatLng latLng) {

        Geocoder geocoder = new Geocoder(getApplicationContext(), Locale.getDefault());
        String address = "";
        try {
            List<Address> adressList = geocoder.getFromLocation(latLng.latitude,latLng.longitude,1);
            if (adressList!=null && adressList.size()>0){
                if (adressList.get(0).getThoroughfare()!=null){//getting street name
                    address +=adressList.get(0).getCountryName();

                    address +=adressList.get(0).getThoroughfare();

                    if (adressList.get(0).getSubThoroughfare()!=null){//street number is not null
                        address +=adressList.get(0).getSubThoroughfare();
                    }
                }

            }else{
                address = "new place";
            }

        } catch (IOException exc) {
            exc.printStackTrace();
        }

        mMap.addMarker(new MarkerOptions().title("Address").position(latLng));
        Toast.makeText(getApplicationContext(),"Nouvelle Place ajoutée",Toast.LENGTH_SHORT).show();

        //add address to items array list
        MainActivity.names.add(address);
        MainActivity.locations.add(latLng);
        MainActivity.arrayAdapter.notifyDataSetChanged();

        //ADD to DATABASE
        try {
            //we should save altitude longtitude
            Double l1 = latLng.latitude;
            Double l2 = latLng.longitude;

            String coord1 = l1.toString();
            String coord2 = l2.toString();

            database = this.openOrCreateDatabase("Places",MODE_PRIVATE,null);
            database.execSQL("CREATE TABLE IF NOT EXISTS places (name VARCHAR, latitude VARCHAR ,longitude VARCHAR)");

            //prepare statement
            String toCompile = "INSERT INTO places (name, latitude, longitude) VALUES (?, ?, ?)";
            SQLiteStatement sqLiteStatement = database.compileStatement(toCompile);
            sqLiteStatement.bindString(1,address);
            sqLiteStatement.bindString(2,coord1);
            sqLiteStatement.bindString(3,coord2);
            //execute statement
            sqLiteStatement.execute();



        }catch (Exception exc){
            exc.printStackTrace();
        }



    }


}
