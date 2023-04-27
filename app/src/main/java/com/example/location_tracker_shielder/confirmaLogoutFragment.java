package com.example.location_tracker_shielder;

import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.preference.PreferenceManager;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.DialogFragment;

public class confirmaLogoutFragment extends DialogFragment implements View.OnClickListener {

    private Button btnConfirmaLogout;
    private Button btnCancelaLogout;

    @Override
    public View onCreateView(@NonNull LayoutInflater inflater,
                             @Nullable ViewGroup container,
                             @Nullable Bundle savedInstanceState) {

        View view = inflater.inflate(R.layout.fragment_confirma_logout, container, false);

        btnConfirmaLogout = view.findViewById(R.id.btn_confirm_logout);
        btnCancelaLogout = view.findViewById(R.id.btn_cancel_logout);

        btnConfirmaLogout.setOnClickListener(this);
        btnCancelaLogout.setOnClickListener(this);

        super.onCreate(savedInstanceState);
        return view;
    }


    @Override
    public void onClick(View view) {
        switch(view.getId()){
            case R.id.btn_confirm_logout:

                //cancelar a ronda ativa? acho que nao mas n sei

                SharedPreferences sharedPreferences = PreferenceManager.getDefaultSharedPreferences(view.getContext());
                SharedPreferences.Editor preferenceEditor = sharedPreferences.edit();

                preferenceEditor.putString("encoded_mobile_AES", null);
                preferenceEditor.putString("mobile", null);

                Intent intent = new Intent(view.getContext(), MainActivity2.class);
                intent.putExtra("encoded_mobile_AES", (String) null);
                intent.putExtra("mobile", (String) null);

                preferenceEditor.apply();
                startActivity(intent);
                getDialog().dismiss();
                break;
            case R.id.btn_cancel_logout:
                Toast.makeText(view.getContext(), "Logout Cancelado", Toast.LENGTH_LONG);
                getDialog().dismiss();
                break;
        }
    }
}
