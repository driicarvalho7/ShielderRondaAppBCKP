package com.example.location_tracker_shielder;

import androidx.annotation.NonNull;
import androidx.appcompat.app.ActionBar;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;
import androidx.fragment.app.DialogFragment;

import android.Manifest;
import android.app.Activity;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.os.Bundle;
import android.view.View;
import android.view.WindowManager;
import android.widget.Button;
import android.widget.TextView;
import android.widget.Toast;

import com.android.volley.RequestQueue;
import com.android.volley.Response;
import com.android.volley.VolleyError;
import com.android.volley.toolbox.JsonArrayRequest;
import com.android.volley.toolbox.StringRequest;
import com.android.volley.toolbox.Volley;
import com.example.location_tracker_shielder.databinding.ActivityRondasBinding;
import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.OnMapReadyCallback;
import com.google.android.gms.maps.SupportMapFragment;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.MarkerOptions;
import com.google.android.material.dialog.MaterialAlertDialogBuilder;

import org.json.JSONArray;
import org.json.JSONException;

public class RondasActivity extends AppCompatActivity implements OnMapReadyCallback {

    private String url = "https://shielder.com.br/controle/getPontosRonda.php?ronda=";

    private Button btn_inicia_ronda;
    private Button btn_back_ronda;
    private com.google.android.material.floatingactionbutton.FloatingActionButton logoutFAB;
    private TextView nomeRonda, nomeCond, descricaoRonda, horaRonda, funcaoRonda, fimRonda;

    private String id_idronda_condominio;
    private String regId;
    private String nomeRondaSelecionada;
    private String condRondaSelecionada;
    private String horaRondaSelecionada;
    private String descricaoRondaSelecionada;
    private String inicioRonda;
    private String funcaoRondaSelecionada;
    private String fimRondaSelecionada;

    private pontoRonda[] arrPontos;

    private GoogleMap mMap;
    private ActivityRondasBinding binding;
    private String urlInsert = "https://shielder.com.br/controle/getInsertRonda.php?code=";


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        setContentView(R.layout.activity_rondas);

        regId = getIntent().getStringExtra("EXTRA_REG_ID");
        id_idronda_condominio = getIntent().getStringExtra("EXTRA_ID_RONDA_CONDOMINIO");
        nomeRondaSelecionada = getIntent().getStringExtra("EXTRA_NOME_RONDA");
        condRondaSelecionada = getIntent().getStringExtra("EXTRA_NOME_COND");
        horaRondaSelecionada = getIntent().getStringExtra("EXTRA_HORA_RONDA");
        descricaoRondaSelecionada = getIntent().getStringExtra("EXTRA_DESCRICAO_RONDA");
        funcaoRondaSelecionada = getIntent().getStringExtra("EXTRA_FUNCAO_RONDA");
        fimRondaSelecionada = getIntent().getStringExtra("EXTRA_FIM_HORA_RONDA");

        url = url + id_idronda_condominio + "&code=" + regId;

        JsonArrayRequest jsonArrayRequest = new JsonArrayRequest(url, new Response.Listener<JSONArray>() {
            @Override
            public void onResponse(JSONArray response) {

                if(response.equals(null) || response.length()==0){
                    Toast.makeText(getApplicationContext(), "Essa ronda ainda não está disponível. Selecione outra ronda para iniciar.", Toast.LENGTH_LONG).show();
                    Intent intent = new Intent(getApplicationContext(), RondaViewActivity.class);
                    intent.putExtra("encoded_mobile_AES", getIntent().getStringExtra("encoded_mobile_AES"));
                    startActivity(intent);
                    return;
                }

                arrPontos = new pontoRonda[response.length()];

                for (int position = 0; position < response.length(); position++) {
                    try {
                        pontoRonda aux_pontoRonda = new pontoRonda(response.getJSONObject(position).getString("ronda_idronda_condominio"),
                                response.getJSONObject(position).getString("id_ponto_ronda"),
                                response.getJSONObject(position).getString("geo"),
                                response.getJSONObject(position).getString("ordem"),
                                response.getJSONObject(position).getString("nome"),
                                "0");

                        arrPontos[position] = aux_pontoRonda;

                    } catch (JSONException e) {
                        e.printStackTrace();
                    }
                }

                setMap();

                Toast.makeText(getApplicationContext(), response.toString(), Toast.LENGTH_LONG).show();


                //Toast.makeText(getApplicationContext(), arrPontos[0].nomePontoRonda, Toast.LENGTH_LONG).show();
            }
        }, new Response.ErrorListener() {
            @Override
            public void onErrorResponse(VolleyError error) {
                Toast.makeText(getApplicationContext(), "Essa ronda ainda não está disponível.", Toast.LENGTH_LONG).show();
                Intent intent = new Intent(getApplicationContext(), RondaViewActivity.class);
                intent.putExtra("encoded_mobile_AES", getIntent().getStringExtra("encoded_mobile_AES"));
                startActivity(intent);
                return;
            }
        });

        RequestQueue requestQueue = Volley.newRequestQueue(getApplicationContext());
        requestQueue.add(jsonArrayRequest);

        ActionBar actionBar = getSupportActionBar();
        if(actionBar!=null){actionBar.hide();}

    }

    private void setMap() {


        SupportMapFragment rondaMapFragment = SupportMapFragment.newInstance();
        getSupportFragmentManager()
                .beginTransaction()
                .add(R.id.mapRonda, rondaMapFragment)
                .commit();


        rondaMapFragment.getMapAsync(this);
    }

    @Override
    public void onMapReady(@NonNull GoogleMap googleMap) {

        mMap = googleMap;
        mMap.setPadding(30, 0, 50, 0);

        mostraMarcadores(arrPontos);


        btn_inicia_ronda = findViewById(R.id.btn_iniciaRonda);
        btn_back_ronda = findViewById(R.id.btn_backRonda);
        nomeRonda = findViewById(R.id.nomeRondaA);
        nomeCond = findViewById(R.id.nomeCondA);
        descricaoRonda = findViewById(R.id.descricaoRondaA);
        horaRonda = findViewById(R.id.horaRondaA);
        funcaoRonda = findViewById(R.id.funcaoRondaA);
        fimRonda = findViewById(R.id.fimRondaA);
        logoutFAB = findViewById(R.id.logoutFAB);

        nomeRonda.setText(nomeRondaSelecionada);
        nomeCond.setText(condRondaSelecionada);
        horaRonda.setText(horaRondaSelecionada);
        descricaoRonda.setText(descricaoRondaSelecionada);
        funcaoRonda.setText(funcaoRondaSelecionada);
        fimRonda.setText(fimRondaSelecionada);

        logoutFAB.show();

        logoutFAB.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                DialogFragment dialog = new confirmaLogoutFragment();
                dialog.show(getSupportFragmentManager(), "Confirma Logout");
            }
        });

        btn_back_ronda.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                onBackPressed();
            }
        });

        btn_inicia_ronda.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Context context = view.getContext();
                Intent intent = new Intent(context, MapsActivity.class);

                intent.putExtra("EXTRA_REG_ID", regId);
                intent.putExtra("EXTRA_ID_RONDA_CONDOMINIO", id_idronda_condominio);
                intent.putExtra("EXTRA_NOME_RONDA", nomeRondaSelecionada);
                intent.putExtra("EXTRA_HORA_RONDA", horaRondaSelecionada);
                intent.putExtra("EXTRA_DESCRICAO_RONDA", descricaoRondaSelecionada);
                intent.putExtra("EXTRA_FUNCAO_RONDA", funcaoRondaSelecionada);
                intent.putExtra("EXTRA_NOME_COND", condRondaSelecionada);

                urlInsert = urlInsert + regId + "&status=1";

                StringRequest request = new StringRequest(urlInsert, new Response.Listener<String>() {
                    @Override
                    public void onResponse(String response) {
                        context.startActivity(intent);

                    }
                }, new Response.ErrorListener() {
                    @Override
                    public void onErrorResponse(VolleyError error) {
                        Toast.makeText(context, error.getMessage(), Toast.LENGTH_LONG).show();
                    }
                });
                RequestQueue requestQueue = Volley.newRequestQueue(context);
                requestQueue.add(request);
            }
        });
    }

    private void mostraMarcadores(pontoRonda[] arrPontos) {
        for (int i = 0; i < arrPontos.length; i++) {
            MarkerOptions markerOptions = new MarkerOptions();
            LatLng auxLatLng = new LatLng(arrPontos[i].getLatitudePontoRonda(), arrPontos[i].getLongitudePontoRonda());
            markerOptions.title("Ordem Ponto: " + arrPontos[i].getIdPontoRonda() + "\nHorário: " + arrPontos[i].getNomePontoRonda()).position(auxLatLng);
            mMap.addMarker(markerOptions);
        }

        mMap.moveCamera(CameraUpdateFactory.newLatLngZoom(new LatLng(arrPontos[arrPontos.length - 1].getLatitudePontoRonda(), arrPontos[arrPontos.length - 1].getLongitudePontoRonda()), mMap.getMaxZoomLevel()));
        if (ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED && ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_COARSE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
            return;
        }
        mMap.setMyLocationEnabled(true);

        getWindow().setFlags(WindowManager.LayoutParams.FLAG_FULLSCREEN,
                WindowManager.LayoutParams.FLAG_FULLSCREEN);
    }
}