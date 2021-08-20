package com.example.tareafirebaseml;


import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;

import android.graphics.Color;
import android.os.Bundle;
import android.widget.ImageView;
import android.widget.TextView;



import com.bumptech.glide.Glide;
import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.OnMapReadyCallback;
import com.google.android.gms.maps.SupportMapFragment;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.Polygon;
import com.google.android.gms.maps.model.Polyline;
import com.google.android.gms.maps.model.PolylineOptions;


import org.json.JSONException;
import org.json.JSONObject;


public class CountryData extends AppCompatActivity
        implements  OnMapReadyCallback, GoogleMap.OnMapClickListener,
        GoogleMap.OnPolylineClickListener,
        GoogleMap.OnPolygonClickListener  {
    GoogleMap mMap;
    ImageView imgBandera;
    SupportMapFragment mapFragment;
    String norte, sur, este, oeste;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_country_data);

        String codigPais = getIntent().getExtras().getString("info");
        String key = getIntent().getExtras().getString("key");
        String urlImg = "http://www.geognos.com/api/en/countries/flag/" + key + ".png";

        imgBandera = findViewById(R.id.imgBandera);
        Glide.with(this).load(urlImg).centerCrop().into(imgBandera);
        mapFragment = (SupportMapFragment) getSupportFragmentManager()
                .findFragmentById(R.id.map);
        mapFragment.getMapAsync(this);

        ponerInformacion(codigPais);


    }

    public void ponerInformacion(String pais) {
        try {
            JSONObject jsonObject = new JSONObject(pais);

            String nombrePais = jsonObject.getString("Name");

            JSONObject jsonObjectCapitales = jsonObject.getJSONObject("Capital");

            JSONObject jsonObjectGeorrectangle = jsonObject.getJSONObject("GeoRectangle");

            String TelPref = jsonObject.getString("TelPref");

            JSONObject jsonObjectTelCountryCodes = jsonObject.getJSONObject("CountryCodes");

            String text = "País:" + nombrePais + "\n" +
                    "Capital:" + jsonObjectCapitales.getString("Name") + "\n" +
                    "Code ISO 2:" + jsonObjectTelCountryCodes.getString("iso2") + "\n" +
                    "Code ISO Num:" + jsonObjectTelCountryCodes.getString("isoN") + "\n" +
                    "Code ISO 3:" + jsonObjectTelCountryCodes.getString("iso3") + "\n" +
                    "Code FIPS:" + jsonObjectTelCountryCodes.getString("fips") + "\n" +
                    "Tel Prefix:" + TelPref;

            TextView textView = findViewById(R.id.txtInforma);
            textView.setText(text);

            String rect = "West" + jsonObjectGeorrectangle.getString("West") + "\n" +
                    "East" + jsonObjectGeorrectangle.getString("East") + "\n" +
                    "North" + jsonObjectGeorrectangle.getString("North") + "\n" +
                    "South" + jsonObjectGeorrectangle.getString("South") + "\n";


            norte = jsonObjectGeorrectangle.getString("North");
            sur = jsonObjectGeorrectangle.getString("South");
            este = jsonObjectGeorrectangle.getString("East");
            oeste = jsonObjectGeorrectangle.getString("West");




        } catch (JSONException e) {
            e.printStackTrace();
        }
    }
    @Override
    public void onMapReady( GoogleMap googleMap) {
        mMap = googleMap;
        mMap.getUiSettings().setZoomControlsEnabled(true);
        mMap.setOnMapClickListener(this);

        LatLng punto1 = new LatLng(Double.parseDouble(norte), Double.parseDouble(oeste));
            LatLng punto2 = new LatLng(Double.parseDouble(norte), Double.parseDouble(este));
            LatLng punto3 = new LatLng(Double.parseDouble(sur), Double.parseDouble(este));
            LatLng punto4 = new LatLng(Double.parseDouble(sur), Double.parseDouble(oeste));
            LatLng punto5 = new LatLng(Double.parseDouble(norte), Double.parseDouble(oeste));

            PolylineOptions marco = new PolylineOptions()
                    .add(punto1)
                    .add(punto2)
                    .add(punto3)
                    .add(punto4)
                    .add(punto5);
            marco.width(5);
            marco.color(Color.RED);
         googleMap.addPolyline(marco);
        googleMap.setOnPolylineClickListener(this);


    }
    @Override
    public void onMapClick( LatLng latLng) {

    }

    @Override
    public void onPolygonClick( Polygon polygon) {

    }

    @Override
    public void onPolylineClick( Polyline polyline) {

    }
}