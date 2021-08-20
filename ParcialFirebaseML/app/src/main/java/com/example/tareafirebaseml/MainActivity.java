package com.example.tareafirebaseml;

import androidx.activity.result.ActivityResult;
import androidx.activity.result.ActivityResultCallback;
import androidx.activity.result.ActivityResultLauncher;
import androidx.activity.result.contract.ActivityResultContracts;
import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;

import android.Manifest;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.net.Uri;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.TextView;
import android.widget.Toast;

import com.android.volley.AuthFailureError;
import com.android.volley.Cache;
import com.android.volley.Network;
import com.android.volley.Request;
import com.android.volley.RequestQueue;
import com.android.volley.Response;
import com.android.volley.VolleyError;
import com.android.volley.toolbox.BasicNetwork;
import com.android.volley.toolbox.DiskBasedCache;
import com.android.volley.toolbox.HurlStack;
import com.android.volley.toolbox.JsonArrayRequest;
import com.android.volley.toolbox.StringRequest;
import com.android.volley.toolbox.Volley;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.android.gms.tasks.Task;
import com.google.mlkit.vision.common.InputImage;
import com.google.mlkit.vision.text.Text;
import com.google.mlkit.vision.text.TextRecognition;
import com.google.mlkit.vision.text.TextRecognizer;
import com.google.mlkit.vision.text.TextRecognizerOptions;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;

public class MainActivity extends AppCompatActivity {

    private static final String TAG = "MyTag";
    TextView lblResult;
    Button btnElegirImg,btnVerMapa;
    private RequestQueue rq;
    private static final int STORAGE_PERMISSION_CODE=113;

    ActivityResultLauncher<Intent> intentActivityResultLauncher;

    InputImage inputImage;
    TextRecognizer textRecognizer;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        lblResult=findViewById(R.id.lblResult);
        btnElegirImg=findViewById(R.id.btnElegirImg);
        btnVerMapa=findViewById(R.id.btnVerMapa);
        textRecognizer= TextRecognition.getClient(TextRecognizerOptions.DEFAULT_OPTIONS);

        intentActivityResultLauncher=registerForActivityResult(
                new ActivityResultContracts.StartActivityForResult(),
                new ActivityResultCallback<ActivityResult>() {
                    @Override
                    public void onActivityResult(ActivityResult result) {
                        Intent data=result.getData();
                        Uri imageUri=data.getData();
                        convertImageToText(imageUri);
                    }
                }
        );


        btnElegirImg.setOnClickListener(new View.OnClickListener(){

            @Override
            public void onClick(View v) {
                Intent intent=new Intent();
                intent.setType("image/*");
                intent.setAction(Intent.ACTION_GET_CONTENT);
                intentActivityResultLauncher.launch(intent);

            }
        });

        btnVerMapa.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {

                datosCountry(lblResult.getText().toString());
            }
        });


    }

    private void convertImageToText(Uri imageUri) {
        //Prepare the input image
        try{
            inputImage=InputImage.fromFilePath(getApplicationContext(),imageUri);

            Task<Text> result=textRecognizer.process(inputImage)
                    .addOnSuccessListener(new OnSuccessListener<Text>() {
                        @Override
                        public void onSuccess(@NonNull Text text) {
                            lblResult.setText(text.getText());

                        }
                    }).addOnFailureListener(new OnFailureListener() {
                        @Override
                        public void onFailure(@NonNull Exception e) {
                            lblResult.setText("Error: "+e.getMessage());
                            Log.d(TAG, "Error: "+e.getMessage());
                        }
                    });
        }catch (Exception e){
            Log.d(TAG, "convertImageToText: Error: "+e.getMessage());
        }
    }

    @Override
    protected void onResume() {
        super.onResume();
        checkPermission(Manifest.permission.READ_EXTERNAL_STORAGE,STORAGE_PERMISSION_CODE);
    }

    public void checkPermission(String permission, int requestCode){
        if(ContextCompat.checkSelfPermission(MainActivity.this, permission)== PackageManager.PERMISSION_DENIED) {
            ActivityCompat.requestPermissions(MainActivity.this, new String[]{permission}, requestCode);
        }
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull  int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);
        //Comprobación del código de petición de la solicitud
        if(requestCode==STORAGE_PERMISSION_CODE){
            if(grantResults.length>0 && grantResults[0]==PackageManager.PERMISSION_GRANTED){
                Toast.makeText(MainActivity.this,"Permiso de almacenamiento Concedido", Toast.LENGTH_SHORT).show();
            }else{
                Toast.makeText(MainActivity.this,"Usted acaba de negar el permiso", Toast.LENGTH_SHORT).show();
            }
        }

    }

    public void datosCountry(String pais) {
        String URL = "http://www.geognos.com/api/en/countries/info/all.json";
        StringRequest stringRequest = new StringRequest(Request.Method.GET, URL, new Response.Listener<String>() {
            @Override
            public void onResponse(String response) {
                if (response != null) {
                    try {
                        JSONObject obj = new JSONObject(response);
                        JSONObject objResults= obj.getJSONObject("Results");
                        Iterator<String> iterator = objResults.keys();
                        while(iterator.hasNext()){
                            String key = iterator.next();
                            JSONObject jsonObject2= objResults.getJSONObject(key);
                            String name=jsonObject2.getString("Name").toUpperCase();
                            if(name.equals(pais.toUpperCase()))
                            {
                                Intent i = new Intent(MainActivity.this,CountryData.class);
                                i.putExtra("info",objResults.getJSONObject(key).toString());
                                i.putExtra("key",key);
                                startActivity(i);

                            }else
                            {
                                System.out.println("JSON no tiene registros");
                            }
                        }

                    } catch (JSONException e) {
                        e.printStackTrace();
                    }
                }
            }
        }, new Response.ErrorListener() {
            @Override
            public void onErrorResponse(VolleyError error) {
                Log.e("error is ", "" + error);
            }
        }) ;

        RequestQueue requestQueue;
        Cache cache = new DiskBasedCache(getCacheDir(), 2024 * 2024);
        Network network = new BasicNetwork(new HurlStack());
        requestQueue = new RequestQueue(cache, network);
        requestQueue.start();
        requestQueue = Volley.newRequestQueue(this);
        requestQueue.add(stringRequest);
    }
}