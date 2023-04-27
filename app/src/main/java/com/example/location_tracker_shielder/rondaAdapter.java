package com.example.location_tracker_shielder;

import android.content.Intent;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.RelativeLayout;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.google.android.material.card.MaterialCardView;
import com.google.android.material.floatingactionbutton.ExtendedFloatingActionButton;
import com.google.android.material.floatingactionbutton.FloatingActionButton;

public class rondaAdapter extends RecyclerView.Adapter<rondaAdapter.rondaViewHolder> implements View.OnClickListener {

    Ronda rondas[];
    String registerId;
    Integer parentWidth;

    public rondaAdapter(Ronda[] rondas, String regId){
        this.rondas = rondas;
        registerId = regId;
    }

    @NonNull
    @Override
    public rondaViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {

        View view = LayoutInflater.from(parent.getContext())
                .inflate(R.layout.padrao_ronda, parent, false);

        parentWidth = parent.getWidth();
        return new rondaViewHolder(view);
    }


    @Override
    public void onBindViewHolder(@NonNull rondaViewHolder holder, int position) {
        holder.nomeRonda.setText(rondas[position].nome_ronda);
        holder.nomeCond.setText(rondas[position].nome_cond);
        holder.horaRonda.setText("Início: "+rondas[position].inicio);
        holder.fimHoraRonda.setText("Fim: "+rondas[position].fim);
        holder.position = position;


    }


    @Override
    public int getItemCount() {
        return rondas.length;
    }

    @Override
    public void onClick(View view) {

    }


    public class rondaViewHolder extends RecyclerView.ViewHolder{


        TextView nomeRonda, nomeCond, horaRonda, fimHoraRonda;
        ExtendedFloatingActionButton selectRondaFAB;
        Ronda ronda;
        private int position;



        public rondaViewHolder(@NonNull View itemView) {
            super(itemView);


            nomeRonda = (TextView)itemView.findViewById(R.id.nomeRondaA);
            nomeCond = (TextView)itemView.findViewById(R.id.nomeCondA);
            horaRonda = (TextView)itemView.findViewById(R.id.horaRondaA);
            fimHoraRonda = (TextView)itemView.findViewById(R.id.fimRondaA);
            selectRondaFAB = (ExtendedFloatingActionButton)itemView.findViewById(R.id.btn_selecionaRonda);


            selectRondaFAB.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View view) {

                    Intent intent = new Intent(view.getContext(), RondasActivity.class);

                    intent.putExtra("EXTRA_REG_ID", registerId);
                    intent.putExtra("EXTRA_ID_RONDA_CONDOMINIO", rondas[position].id_ronda_condominio);
                    intent.putExtra("EXTRA_NOME_RONDA", rondas[position].nome_ronda);
                    intent.putExtra("EXTRA_HORA_RONDA", "Inicio: "+rondas[position].inicio);
                    intent.putExtra("EXTRA_FIM_HORA_RONDA", "Fim: "+ rondas[position].fim);
                    intent.putExtra("EXTRA_DESCRICAO_RONDA", rondas[position].descricao);
                    intent.putExtra("EXTRA_FUNCAO_RONDA", rondas[position].funcao);
                    intent.putExtra("EXTRA_NOME_COND", rondas[position].nome_cond);

                    view.getContext().startActivity(intent);

                }
            });
        }
    }
}
