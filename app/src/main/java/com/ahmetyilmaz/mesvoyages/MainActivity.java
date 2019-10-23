package com.ahmetyilmaz.mesvoyages;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;

import android.content.Intent;
import android.database.Cursor;
import android.os.Bundle;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.ListView;

import com.google.android.gms.maps.model.LatLng;

import java.util.ArrayList;

public class MainActivity extends AppCompatActivity {

  //create array list to wrap places data VARIABLE FOR LISTVIEW
    static ArrayList<String> names =new ArrayList<String>();
    static ArrayList<LatLng> locations =new ArrayList<LatLng>();
    static ArrayAdapter arrayAdapter;

//To add options to our menu we need this
    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        //add our menu to project
        MenuInflater menuInflater=getMenuInflater();
        menuInflater.inflate(R.menu.ajouter_place,menu);

        return super.onCreateOptionsMenu(menu);
    }

    //When menu item selected what to do
    @Override
    public boolean onOptionsItemSelected(@NonNull MenuItem item) {

        if (item.getItemId()==R.id.ajouter_place){
            //intent to maps activity

            Intent intent = new Intent(getApplicationContext(),MapsActivity.class);
            intent.putExtra("info","new");
            startActivity(intent);
        }
        return super.onOptionsItemSelected(item);
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        ListView listView=(ListView)findViewById(R.id.listView1);

        //database operation
        try {

            MapsActivity.database=this.openOrCreateDatabase("Places",MODE_PRIVATE,null);
            Cursor cursor = MapsActivity.database.rawQuery("SELECT * FROM places",null);

            int nameIndex = cursor.getColumnIndex("name");
            int latitudeIndex = cursor.getColumnIndex("latitude");
            int longitudeIndex = cursor.getColumnIndex("longitude");

            //Get all saved locations from database -> set to array list


            while (cursor.moveToNext()){
                String nameFromDatabase = cursor.getString(nameIndex);
                String latitudeFromDatabase = cursor.getString(latitudeIndex);
                String longitudeFromDatabase = cursor.getString(longitudeIndex);

                names.add(nameFromDatabase);

                Double l1 = Double.parseDouble(latitudeFromDatabase);
                Double l2 = Double.parseDouble(longitudeFromDatabase);
                LatLng locationFromDatabase = new LatLng(l1,l2);
                locations.add(locationFromDatabase);

                System.out.println("name: "+nameFromDatabase);

            }
            cursor.close();

        }catch (Exception exc){
            exc.printStackTrace();
        }

        //SET arraylist to listViews
        arrayAdapter=new ArrayAdapter(this,android.R.layout.simple_list_item_1,names);
        listView.setAdapter(arrayAdapter);

        listView.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
                Intent intent =new Intent(getApplicationContext(),MapsActivity.class);

                //put info before to start maps activity la position de item, add/show address
                intent.putExtra("info","old");
                intent.putExtra("position",position);
                startActivity(intent);
            }
        });



    }


}
