package com.example.location_tracker_shielder;

import androidx.appcompat.app.ActionBar;
import androidx.appcompat.app.AppCompatActivity;
import androidx.fragment.app.DialogFragment;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import android.app.Dialog;
import android.content.Context;
import android.content.Intent;
import android.os.Build;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.view.WindowManager;
import android.webkit.WebView;
import android.widget.Toast;
import android.widget.Toolbar;

import com.android.volley.Request;
import com.android.volley.RequestQueue;
import com.android.volley.Response;
import com.android.volley.VolleyError;
import com.android.volley.toolbox.JsonArrayRequest;
import com.android.volley.toolbox.JsonObjectRequest;
import com.android.volley.toolbox.StringRequest;
import com.android.volley.toolbox.Volley;
import com.google.android.material.appbar.MaterialToolbar;
import com.google.android.material.floatingactionbutton.FloatingActionButton;
import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.gson.JsonArray;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.lang.reflect.Array;
import java.lang.reflect.Type;
import java.net.URI;
import java.net.URISyntaxException;
import java.util.ArrayList;
import java.util.Arrays;


public class RondaViewActivity extends AppCompatActivity{

    private String url = "https://shielder.com.br/controle/getInsertRonda.php?code=";
    private String url1 = "https://shielder.com.br/controle/getRondas.php?code=";
    private String regID;
    private String id_idronda_condominio;
    private RecyclerView recyclerView;
    private View topAppBarMenu;
    private FloatingActionButton logoutFAB;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_ronda_view);


        regID = getIntent().getStringExtra("encoded_mobile_AES");
//        regID="eyJpdiI6Imp2eWh2anozUkdJQUFPRFhzUEVYcVE9PVxuIiwiZGF0YSI6IkxoKzE4STBqdU5oR05jYmh5bFpQVGc9PVxuIn0=";


        url = url+regID;


        recyclerView = findViewById(R.id.recview);
        recyclerView.setLayoutManager(new LinearLayoutManager(getApplicationContext()));


        ActionBar actionBar = getSupportActionBar();

        if(actionBar!=null){actionBar.hide();}

        logoutFAB = findViewById(R.id.logoutFAB);
        logoutFAB.show();
        logoutFAB.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                DialogFragment dialog = new confirmaLogoutFragment();
                dialog.show(getSupportFragmentManager(), "Confirma Logout");
            }
        });


//        JsonArrayRequest objectRequest = new JsonArrayRequest
//            (Request.Method.GET, url, null, new Response.Listener<JSONArray>() {
//                @Override
//                public void onResponse(JSONArray response) {
//                    try {
//                        if(response.length() == 0){


        getWindow().setFlags(WindowManager.LayoutParams.FLAG_FULLSCREEN,
                WindowManager.LayoutParams.FLAG_FULLSCREEN);


        url1 = url1+regID;
                            JsonArrayRequest arrayRequest = new JsonArrayRequest(Request.Method.GET, url1, null, new Response.Listener<JSONArray>() {
                                @Override
                                public void onResponse(JSONArray response) {

                                    Ronda[] rondas = new Ronda[response.length()];

                                    if (response.length() == 0){
                                        Toast.makeText(getApplicationContext(), "Este usuario ainda não possui rondas disponiveis", Toast.LENGTH_LONG).show();
                                        onBackPressed();
                                        return;
                                    }
                                    for (int i = 0; i < response.length(); i++) {
                                        try{
                                            Ronda ronda_aux = new Ronda(response.getJSONObject(i).getString("id_ronda_condominio"),
                                                    response.getJSONObject(i).getString("nome_ronda"),
                                                    response.getJSONObject(i).getString("descricao"),
                                                    response.getJSONObject(i).getString("inicio"),
                                                    response.getJSONObject(i).getString("fim"),
                                                    response.getJSONObject(i).getString("habilitado"),
                                                    response.getJSONObject(i).getString("funcao"),
                                                    response.getJSONObject(i).getString("nome_funcionario"),
                                                    response.getJSONObject(i).getString("nome_condomino"));

                                            rondas[i] = ronda_aux;
                                        } catch (JSONException e) {
                                            e.printStackTrace();
                                        }
                                    }
                                    rondaAdapter rondaAdapter = new rondaAdapter(rondas, regID);
                                    recyclerView.setAdapter(rondaAdapter);
                                }
                            }, new Response.ErrorListener() {
                                @Override
                                public void onErrorResponse(VolleyError error) {
                                    Log.i("jsonObjectRequest", "Error, Status Code " + error.networkResponse.statusCode);
                                    Log.i("jsonObjectRequest", "URL: " + url);
                                    Log.i("jsonObjectRequest", "Net Response to String: " + error.networkResponse.toString());
                                    Log.i("jsonObjectRequest", "Error bytes: " + new String(error.networkResponse.data));
                                }
                            });
                            RequestQueue requestQueue1 = Volley.newRequestQueue(getApplicationContext());
                            requestQueue1.add(arrayRequest);

//                        }else{
//                            Intent intent = new Intent(getApplicationContext(), MapsActivity.class);
//
//                            intent.putExtra("EXTRA_ID_RONDA_FUNCIONARIO", response.getJSONObject(0).getInt("id_ronda_funcionario"));
//                            intent.putExtra("EXTRA_REG_ID", regID);
//                            intent.putExtra("EXTRA_NOME_FUNCIONARIO_RONDA", response.getJSONObject(0).getString("nome_funcionario"));
//                            intent.putExtra("EXTRA_ID_RONDA_CONDOMINIO", response.getJSONObject(0).getInt("ronda_id_ronda_condominio"));
//                            startActivity(intent);
//                            return;
//                        }
//                    } catch (JSONException e) {
//                        e.printStackTrace();
//                    }
//
//                }
//            }, new Response.ErrorListener() {
//                @Override
//                public void onErrorResponse(VolleyError error) {
//                    Toast.makeText(getApplicationContext(), error.getMessage(), Toast.LENGTH_LONG).show();
//                    Log.i("jsonObjectRequest", "Error, Status Code " + error.networkResponse.statusCode);
//                    Log.i("jsonObjectRequest", "URL: " + url);
//                    Log.i("jsonObjectRequest", "Net Response to String: " + error.networkResponse.toString());
//                    Log.i("jsonObjectRequest", "Error bytes: " + new String(error.networkResponse.data));
//                }
//            });
//
//        RequestQueue requestQueue = Volley.newRequestQueue(this);
//        requestQueue.add(objectRequest);
//
    }
}