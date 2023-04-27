package com.example.location_tracker_shielder;

import android.app.DialogFragment;
import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.TextView;
import android.widget.Toast;


import androidx.annotation.NonNull;
import androidx.annotation.Nullable;

import com.android.volley.RequestQueue;
import com.android.volley.Response;
import com.android.volley.VolleyError;
import com.android.volley.toolbox.StringRequest;
import com.android.volley.toolbox.Volley;
import com.google.android.gms.maps.model.Marker;

public class cancelaRondaFragment extends DialogFragment implements View.OnClickListener {

    private static final String TAG = "ScrollingFragment";
    private Button btn_confirm, btn_cancel;
    private String urlEncerra = "https://box.shielder.com.br/controle/getInsertRonda.php?code=";
    private String regID;
    private String id_ronda_condominio;
    private TextView textView;
    private Marker marker;


    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater,
                             @Nullable ViewGroup container,
                             @Nullable Bundle savedInstanceState) {


        View view = inflater.inflate(R.layout.fragment_cancela_ronda, container, false);

        Bundle bundle = getArguments();

        regID = bundle.getString("EXTRA_REG_ID");
        id_ronda_condominio = bundle.getString("EXTRA_ID_RONDA");
        marker = (Marker) bundle.get("EXTRA_CONFIRM_MARKER");

        btn_cancel = view.findViewById(R.id.btn_cancel);
        btn_confirm = view.findViewById(R.id.btn_confirm);
        textView = view.findViewById(R.id.textView2);

        btn_confirm.setOnClickListener(this);
        btn_cancel.setOnClickListener(this);
        return view;
    }

    @Override
    public void onClick(View view) {
        switch(view.getId()){
            case R.id.btn_cancel:
                Toast.makeText(MapsActivity.getMapsContext(), "A ronda não foi encerrada", Toast.LENGTH_SHORT).show();
                getDialog().dismiss();
                break;

            case R.id.btn_confirm:
                Toast.makeText(MapsActivity.getMapsContext(), "Ronda encerrada", Toast.LENGTH_SHORT).show();
                Context context = view.getContext();
                Intent intent = new Intent(context, RondaViewActivity.class);
                intent.putExtra("EXTRA_ID_RONDA", id_ronda_condominio);
                intent.putExtra("EXTRA_REG_ID", regID);

                urlEncerra = urlEncerra+regID+"&status=0";

                StringRequest request = new StringRequest(urlEncerra, new Response.Listener<String>() {
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
                getDialog().dismiss();
                break;
        }
    }
}