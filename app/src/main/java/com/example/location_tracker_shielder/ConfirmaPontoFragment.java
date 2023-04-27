package com.example.location_tracker_shielder;

import android.app.AlertDialog;
import android.app.Dialog;
import android.content.Context;
import android.content.DialogInterface;
import android.os.Bundle;

import androidx.annotation.NonNull;
import androidx.fragment.app.DialogFragment;
import androidx.fragment.app.Fragment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.TextView;

import com.google.android.gms.maps.model.Marker;


public class ConfirmaPontoFragment extends DialogFragment implements View.OnClickListener {


    private String regID;
    private String id_ronda_condominio;
    private Marker marker;
    private Button btn_negativo;
    private Button btn_positivo;
    private TextView textView;
    private pontoRonda pontoRonda;


    public ConfirmaPontoFragment() {
        // Required empty public constructor
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        View view = inflater.inflate(R.layout.fragment_confirma_ponto, container, false);

        Bundle bundle = getArguments();

        regID = bundle.getString("EXTRA_REG_ID");
        id_ronda_condominio = bundle.getString("EXTRA_ID_RONDA");
        marker = (Marker) bundle.get("EXTRA_CONFIRM_MARKER");


        btn_negativo = view.findViewById(R.id.btn_negativo);
        btn_positivo = view.findViewById(R.id.btn_positivo);
        textView = view.findViewById(R.id.confirma_ponto_TextView);

        btn_positivo.setOnClickListener(this);
        btn_negativo.setOnClickListener(this);


        return view;
    }

    /* The activity that creates an instance of this dialog fragment must
     * implement this interface in order to receive event callbacks.
     * Each method passes the DialogFragment in case the host needs to query it. */

    public interface ConfirmaPontoListener {
        public void onDialogPositiveClick(DialogFragment dialog);
        public void onDialogNegativeClick(DialogFragment dialog);
    }

    // Use this instance of the interface to deliver action events
    ConfirmaPontoListener listener;

    @Override
    public void onAttach(@NonNull Context context) {
        super.onAttach(context);
        // Verify that the host activity implements the callback interface
        try {
            // Instantiate the NoticeDialogListener so we can send events to the host
            listener = (ConfirmaPontoListener) context;
        } catch (ClassCastException e) {
            // The activity doesn't implement the interface, throw exception
            throw new ClassCastException(getActivity().toString()
                    + " must implement NoticeDialogListener");
        }
    }

    @Override
    public void onClick(View view) {
        switch(view.getId()){
            case R.id.btn_positivo:
                listener.onDialogPositiveClick(this);
                break;

            case R.id.btn_negativo:
                listener.onDialogNegativeClick(this);
                break;
        }
    }
}