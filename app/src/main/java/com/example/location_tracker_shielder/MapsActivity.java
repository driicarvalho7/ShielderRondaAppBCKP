package com.example.location_tracker_shielder;

import static android.app.PendingIntent.FLAG_IMMUTABLE;
import static android.content.Intent.FLAG_ACTIVITY_NEW_TASK;
import static java.lang.Integer.parseInt;
import static java.util.Comparator.comparing;

import androidx.annotation.NonNull;
import androidx.core.app.ActivityCompat;
import androidx.fragment.app.DialogFragment;
import androidx.fragment.app.FragmentActivity;

import android.Manifest;
import android.app.Dialog;
import android.app.Notification;
import android.app.NotificationChannel;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.pm.PackageManager;
import android.graphics.Color;
import android.os.Build;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.view.WindowManager;
import android.widget.Button;
import android.widget.TextView;
import android.widget.Toast;

import com.android.volley.RequestQueue;
import com.android.volley.Response;
import com.android.volley.VolleyError;
import com.android.volley.toolbox.JsonArrayRequest;
import com.android.volley.toolbox.JsonObjectRequest;
import com.android.volley.toolbox.StringRequest;
import com.android.volley.toolbox.Volley;
import com.google.android.gms.location.FusedLocationProviderClient;
import com.google.android.gms.location.LocationCallback;
import com.google.android.gms.location.LocationRequest;
import com.google.android.gms.location.LocationResult;
import com.google.android.gms.location.LocationServices;
import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.OnMapReadyCallback;
import com.google.android.gms.maps.SupportMapFragment;
import com.google.android.gms.maps.model.BitmapDescriptorFactory;
import com.google.android.gms.maps.model.LatLng;
import com.example.location_tracker_shielder.databinding.ActivityMapsBinding;
import com.google.android.gms.maps.model.Marker;
import com.google.android.gms.maps.model.MarkerOptions;
import com.google.android.gms.maps.model.Polyline;
import com.google.android.gms.maps.model.PolylineOptions;
import com.google.android.material.floatingactionbutton.FloatingActionButton;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.Date;
import java.util.Locale;

public class MapsActivity extends FragmentActivity implements OnMapReadyCallback, ConfirmaPontoFragment.ConfirmaPontoListener {

    private GoogleMap mMap;
    private ActivityMapsBinding binding;
    private float zoomMedio;
    private boolean lockCam = false;
    private Button confirmaPontoRonda;
    private Button terminaRonda;
    private FloatingActionButton logoutButton;
    private TextView distanciaPercurso;
    private TextView duracaoPercurso;

    private static Context context;
    private static Dialog dialog_progress;

    private String urlGetPontos = "https://shielder.com.br/controle/getPontosRonda.php?ronda=";
    private String urlSetPontoPassado = "https://shielder.com.br/controle/getPontosFuncionario.php?";
    private String regID;
    private String id_ronda_condominio;
    private Double currentLatitude;
    private Double currentLongitude;
    private boolean isRegistered = true;


    private ArrayList<pontoRonda> arrPontos;
    private ArrayList<Marker> arrMarcadores = new ArrayList<>();
    private ArrayList<Polyline> polyList = new ArrayList<>();
    private String urlEncerra = "https://box.shielder.com.br/controle/getInsertRonda.php?code=";


    @Override
    protected void onCreate(Bundle savedInstanceState) {

        super.onCreate(savedInstanceState);
        //setContentView(R.layout.activity_maps);
        context = getApplicationContext();
        requestCurrentLocation();

        regID = getIntent().getStringExtra("EXTRA_REG_ID");
        id_ronda_condominio = getIntent().getStringExtra("EXTRA_ID_RONDA_CONDOMINIO");

        urlGetPontos = urlGetPontos + id_ronda_condominio + "&code=" + regID;
        getWindow().setFlags(WindowManager.LayoutParams.FLAG_FULLSCREEN,
                WindowManager.LayoutParams.FLAG_FULLSCREEN);

        JsonArrayRequest request = new JsonArrayRequest(urlGetPontos, new Response.Listener<JSONArray>() {
            @Override
            public void onResponse(JSONArray response) {

                arrPontos = new ArrayList<pontoRonda>();

                for (int position = 0; position < response.length(); position++) {
                    try {
                        pontoRonda aux_pontoRonda = new pontoRonda(response.getJSONObject(position).getString("ronda_idronda_condominio"),
                                response.getJSONObject(position).getString("id_ponto_ronda"),
                                response.getJSONObject(position).getString("geo"),
                                response.getJSONObject(position).getString("ordem"),
                                response.getJSONObject(position).getString("nome"),
                                "0");

                        arrPontos.add(aux_pontoRonda);

                    } catch (JSONException e) {
                        e.printStackTrace();
                    }
                }
                Collections.sort(arrPontos, new Comparator<pontoRonda>() {
                    @Override
                    public int compare(pontoRonda ponto1, pontoRonda ponto2) {
                        return ponto1.getOrdemPontoRonda().compareTo(ponto2.getOrdemPontoRonda());
                    }
                });
                setMap();

                //Toast.makeText(getApplicationContext(), arrPontos[0].nomePontoRonda, Toast.LENGTH_LONG).show();
            }
        }, new Response.ErrorListener() {
            @Override
            public void onErrorResponse(VolleyError error) {
                Toast.makeText(getApplicationContext(), error.toString(), Toast.LENGTH_LONG).show();

            }
        });

        buildNotification();

        RequestQueue requestQueue = Volley.newRequestQueue(getApplicationContext());
        requestQueue.add(request);

    }

    public void setMap() {
        binding = ActivityMapsBinding.inflate(getLayoutInflater());
        setContentView(binding.getRoot());


        // Obtain the SupportMapFragment and get notified when the map is ready to be used.
        SupportMapFragment mapFragment = (SupportMapFragment) getSupportFragmentManager()
                .findFragmentById(R.id.mapRonda);
        mapFragment.getMapAsync(this);
    }

    private void mostraMarcadores(ArrayList<pontoRonda> arrPontos, ArrayList<Marker> arrMarcadores) {

        for (int i = 0; i < arrPontos.size(); i++) {
            MarkerOptions markerOptions = new MarkerOptions();
            LatLng auxLatLng = new LatLng(arrPontos.get(i).getLatitudePontoRonda(), arrPontos.get(i).getLongitudePontoRonda());
            markerOptions.title("Ordem:" + (new Integer(arrPontos.get(i).getOrdemPontoRonda())))
                    .position(auxLatLng)
                    .flat(true);

            final Marker marker = mMap.addMarker(markerOptions);
            arrMarcadores.add(marker);
        }

        mMap.moveCamera(CameraUpdateFactory.newLatLngZoom(new LatLng(arrPontos.get(0).getLatitudePontoRonda(), arrPontos.get(0).getLongitudePontoRonda()), 2));

    }

    public void desenhaRonda(LatLng posicaoPontoRonda) {

        arrMarcadores.get(getPontoPassar()).setIcon(BitmapDescriptorFactory.defaultMarker(BitmapDescriptorFactory.HUE_AZURE));
        Toast.makeText(this, "Siga para o ponto de ronda em azul", Toast.LENGTH_LONG).show();

        LatLng posicaoAtual;
        if (currentLongitude!=null && currentLongitude!=null) {
            posicaoAtual = new LatLng(currentLatitude, currentLongitude);
        }else{
            posicaoAtual = new LatLng(mMap.getMyLocation().getLatitude(), mMap.getMyLocation().getLongitude());
        }


        String urlDirecao = "https://maps.googleapis.com/maps/api/directions/json?"
                + "origin=" + posicaoAtual.latitude + "," + posicaoAtual.longitude
                + "&destination=" + posicaoPontoRonda.latitude + "," + posicaoPontoRonda.longitude
                + "&sensor=false&units=metric&mode=walking&key=AIzaSyAYWB0Ax2zAJ4DjEOCygfU1jTLoSMnbZhA";
        Log.d("url: ", urlDirecao);


        JsonObjectRequest stringRequest1 = new JsonObjectRequest(urlDirecao, null , new Response.Listener<JSONObject>() {
            @Override
            public void onResponse(JSONObject response) {

                try {

                    JSONObject js_aux = response.getJSONArray("routes")
                            .getJSONObject(0)
                            .getJSONArray("legs")
                            .getJSONObject(0);
                    JSONArray js = js_aux.getJSONArray("steps");

                    ArrayList<LatLng> caminhoAtual = new ArrayList<LatLng>();
                    caminhoAtual.add(new LatLng(currentLatitude, currentLongitude));

                    for(int i = 0; i<js.length(); i++){
                        JSONObject aux = js.getJSONObject(i);
                        JSONObject location_aux = aux.getJSONObject("end_location");
                        LatLng latlng = new LatLng(location_aux.getDouble("lat"), location_aux.getDouble("lng"));
                        caminhoAtual.add(latlng);

                    }

                    PolylineOptions lineOpt = new PolylineOptions().width(10).color(Color.RED);

                    for (int i = 0; i < caminhoAtual.size(); i++) {
                        lineOpt.add(caminhoAtual.get(i));
                    }

                    Polyline polyline = mMap.addPolyline(lineOpt);
                    polyList.add(polyline);
                    return;
                }catch (Exception e){
                    e.printStackTrace();
                }
            }
        }, new Response.ErrorListener() {
            @Override
            public void onErrorResponse(VolleyError error) {

                Log.e("ERROR DirectionsRequest", error.networkResponse.toString());
            }
        });

        RequestQueue requestQueue1 = Volley.newRequestQueue(getApplicationContext());
        requestQueue1.add(stringRequest1);
    }

    private void requestCurrentLocation(){

        LocationRequest request = new LocationRequest();
        request.setInterval(5000);
        request.setPriority(LocationRequest.PRIORITY_HIGH_ACCURACY);

        FusedLocationProviderClient client = LocationServices.getFusedLocationProviderClient(this);

        if (ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED && ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_COARSE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
            // TODO: Consider calling
            //    ActivityCompat#requestPermissions
            // here to request the missing permissions, and then overriding
            //   public void onRequestPermissionsResult(int requestCode, String[] permissions,
            //                                          int[] grantResults)
            // to handle the case where the user grants the permission. See the documentation
            // for ActivityCompat#requestPermissions for more details.
            return;
        }

        client.requestLocationUpdates(request, new LocationCallback() {
            @Override
            public void onLocationResult(@NonNull LocationResult locationResult) {
                if(!isRegistered){
                    client.removeLocationUpdates(this);
                    return;
                }
                super.onLocationResult(locationResult);
                currentLatitude = locationResult.getLastLocation().getLatitude();
                currentLongitude = locationResult.getLastLocation().getLongitude();
                Log.i("My Location Update: ", String.valueOf(locationResult));
                return;
            }
        }, null);
    }

    private void buildNotification() {
        String stop = "stop";
        registerReceiver(stopReceiver, new IntentFilter(stop));
        isRegistered = true;
        PendingIntent broadcastIntent = PendingIntent.getBroadcast(
                this, 0, new Intent(stop), PendingIntent.FLAG_UPDATE_CURRENT | FLAG_IMMUTABLE);


        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            CharSequence name = getString(R.string.tracking_enabled_notif);
            String description = getString(R.string.tracking_desc);
            int importance = NotificationManager.IMPORTANCE_DEFAULT;
            NotificationChannel channel = new NotificationChannel("tracking_not", name, importance);
            channel.setDescription(description);

            NotificationManager notificationManager = getSystemService(NotificationManager.class);
            notificationManager.createNotificationChannel(channel);

            Notification.Builder builder = new Notification.Builder(this,"tracking_not")
                    .setContentTitle(getString(R.string.app_name))
                    .setContentText(getString(R.string.tracking_enabled_notif))
                    .setOngoing(true)
                    .setContentIntent(broadcastIntent)
                    .setSmallIcon(R.drawable.tracking_enabled);
            builder.build();

        }else {

            Notification.Builder builder = new Notification.Builder(this)
                    .setContentTitle(getString(R.string.app_name))
                    .setContentText(getString(R.string.tracking_enabled_notif))
                    .setOngoing(true)
                    .setContentIntent(broadcastIntent)
                    .setSmallIcon(R.drawable.tracking_enabled);
            builder.build();
        }
    }

    protected BroadcastReceiver stopReceiver = new BroadcastReceiver() {
        @Override
        public void onReceive(Context context, Intent intent) {
            unregisterReceiver(stopReceiver);

            Intent openApp = new Intent(context.getApplicationContext(), MapsActivity.class);
            openApp.setFlags(FLAG_ACTIVITY_NEW_TASK);
            startActivity(openApp);

        }
    };

    /**
     * Manipulates the map once available.
     * This callback is triggered when the map is ready to be used.
     * This is where we can add markers or lines, add listeners or move the camera. In this case,
     * we just add a marker near Sydney, Australia.
     * If Google Play services is not installed on the device, the user will be prompted to install
     * it inside the SupportMapFragment. This method will only be triggered once the user has
     * installed Google Play services and returned to the app.
     */


    @Override
    public void onMapReady(GoogleMap googleMap) {
        mMap = googleMap;
        mMap.setPadding(30,0,50,0);

        mostraMarcadores(arrPontos, arrMarcadores);
        //distanciaPercurso = (TextView) findViewById(R.id.distanceView);
        //duracaoPercurso = (TextView) findViewById(R.id.durationView);

        if (ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED && ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_COARSE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
            // TODO: Consider calling
            //    ActivityCompat#requestPermissions
            // here to request the missing permissions, and then overriding
            //   public void onRequestPermissionsResult(int requestCode, String[] permissions,
            //                                          int[] grantResults)
            // to handle the case where the user grants the permission. See the documentation
            // for ActivityCompat#requestPermissions for more details.
            return;
        }
        mMap.setMyLocationEnabled(true);

        LatLng pontoRondaAtual = arrMarcadores.get(getPontoPassar()).getPosition();
        desenhaRonda(pontoRondaAtual);

        logoutButton = findViewById(R.id.logoutFAB);
        logoutButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                DialogFragment confirmaLogout = new confirmaLogoutFragment();
                confirmaLogout.show(getSupportFragmentManager(), "Confirma Logout");
            }
        });

        confirmaPontoRonda = findViewById(R.id.btn_confirmaPonto);

        confirmaPontoRonda.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                mostraDialogoConfirma();
            }
        });

        terminaRonda = (Button) findViewById(R.id.btn_cancelaRonda);

        terminaRonda.setOnClickListener(new View.OnClickListener(){
            @Override
            public void onClick(View view){
                //setContentView(R.layout.MenuRotas);

                Bundle bundle = new Bundle();
                bundle.putString("EXTRA_REG_ID", regID);
                bundle.putString("EXTRA_ID_RONDA", id_ronda_condominio);

                cancelaRondaFragment dialog = new cancelaRondaFragment();
                dialog.setArguments(bundle);
                dialog.show(getFragmentManager(), "ScrollingFragment");

            }
        });

        if (ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED && ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_COARSE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
            // TODO: Consider calling
            //    ActivityCompat#requestPermissions
            // here to request the missing permissions, and then overriding
            //   public void onRequestPermissionsResult(int requestCode, String[] permissions,
            //                                          int[] grantResults)
            // to handle the case where the user grants the permission. See the documentation
            // for ActivityCompat#requestPermissions for more details.
            return;
        }
        mMap.setMyLocationEnabled(true);

    }

    private void mostraDialogoConfirma() {
        Bundle bundle = new Bundle();
        bundle.putString("EXTRA_REG_ID", regID);
        bundle.putString("EXTRA_ID_RONDA", id_ronda_condominio);

        DialogFragment confirmaPontoRonda = new ConfirmaPontoFragment();
        confirmaPontoRonda.setArguments(bundle);
        confirmaPontoRonda.show(getSupportFragmentManager(), "ConfirmaPontoRonda");
    }

    @Override
    public void onDialogPositiveClick(DialogFragment confirmaPontoDF){

        Marker marker = arrMarcadores.get(getPontoPassar());

        if (currentLongitude==null || currentLatitude==null){
            currentLatitude = mMap.getMyLocation().getLatitude();
            currentLongitude = mMap.getMyLocation().getLongitude();
        }

        LatLng myLatLng = new LatLng(currentLatitude, currentLongitude);

        if(!arrMarcadores.get(getPontoPassar()).getPosition().equals(myLatLng)){

            marker.setIcon(BitmapDescriptorFactory.defaultMarker(BitmapDescriptorFactory.HUE_GREEN));

            urlSetPontoPassado = urlSetPontoPassado+"code="+regID+"&coord="+ marker.getPosition().latitude+","+ marker.getPosition().longitude
                    +"&horario="+new SimpleDateFormat("(dd-MM-yyyy)HH:mm::ss", Locale.getDefault()).format(new Date()).toString()
                    +"&confirmacao="+ arrPontos.get(getPontoPassar()).getIdPontoRonda();


            StringRequest stringRequest = new StringRequest(urlSetPontoPassado, new Response.Listener<String>() {
                @Override
                public void onResponse(String response) {
                    arrPontos.get(getPontoPassar()).setPontoPassado();
                    if (getPontoPassar() == -1){
                        Intent intent = new Intent(context, RondaViewActivity.class);
                        intent.putExtra("EXTRA_ID_RONDA", id_ronda_condominio);
                        intent.putExtra("EXTRA_REG_ID", regID);
                        intent.setFlags(FLAG_ACTIVITY_NEW_TASK);

                        urlEncerra = urlEncerra+regID+"&status=0";

                        StringRequest request = new StringRequest(urlEncerra, new Response.Listener<String>() {
                            @Override
                            public void onResponse(String response) {
                                Toast.makeText(getMapsContext(), "Ronda Finalizada com Sucesso", Toast.LENGTH_LONG);
                                getMapsContext().startActivity(intent);
                            }
                        }, new Response.ErrorListener() {
                            @Override
                            public void onErrorResponse(VolleyError error) {
                                Toast.makeText(getMapsContext(), error.getMessage(), Toast.LENGTH_LONG).show();
                            }
                        });
                        RequestQueue requestQueue = Volley.newRequestQueue(context);
                        requestQueue.add(request);
                    }else{
                        LatLng pontoRondaAtual = arrMarcadores.get(getPontoPassar()).getPosition();

                        desenhaRonda(pontoRondaAtual);
                        confirmaPontoDF.dismiss();


                        //arrMarcadores.remove(marker);
                        polyList.get(0).remove();
                        polyList.remove(0);
                    }
                }
            }, new Response.ErrorListener() {
                @Override
                public void onErrorResponse(VolleyError error) {
                    Toast.makeText(getMapsContext(), error.getMessage(), Toast.LENGTH_LONG).show();
                }
            });

            RequestQueue requestQueueInsertPonto = Volley.newRequestQueue(getMapsContext());
            requestQueueInsertPonto.add(stringRequest);
            return;
        }
    }

    @Override
    public void onDialogNegativeClick(DialogFragment confirmaPontoDF) {

        Toast.makeText(this, "Ponto de ronda NÃO confirmado", Toast.LENGTH_LONG).show();
        confirmaPontoDF.dismiss();
    }

    private int getPontoPassar() {

        for(int i = 0; i< arrPontos.size(); i++){
            if(isPontoPassar(i)){
                return i;
            }
        }
        return -1;
    }

    private boolean isPontoPassar(int position) {
        if(position == 0 && arrPontos.get(position).getPontoPassado().equals("0")){
            return true;
        }

        if(arrPontos.get(position).getPontoPassado().equals("0")
                && arrPontos.get(position - 1) !=null
                && arrPontos.get(position - 1).getPontoPassado().equals("1")){
            return true;
        }
        return false;
    }

    public static final Context getMapsContext(){
        return context;
    }

    @Override
    public void onDestroy() {
        isRegistered = false;
        super.onDestroy();
    }
}
