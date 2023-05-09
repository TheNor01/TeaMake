package com.example.teamake;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import java.util.ArrayList;

public class DriversAdapter extends RecyclerView.Adapter<DriversAdapter.PlayersViewHolder>{

    private ArrayList<UserItem> driverList;

    private OnItemClickListener customListener;

    public interface OnItemClickListener {
        void  onItemClick(int position);
    }

    public void setOnItemClickLister(OnItemClickListener listener){
        customListener=listener;
    }

    public static class PlayersViewHolder extends RecyclerView.ViewHolder {
        ImageView imageInvite;
        TextView nickname;


        // i have to pass listener into costructor, cause i cannot access to static field

        public PlayersViewHolder(@NonNull View itemView, OnItemClickListener listener) {
            super(itemView);
            imageInvite = itemView.findViewById(R.id.inviteImg);
            nickname = itemView.findViewById(R.id.nicknameUser);

            itemView.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View view) {
                    if(listener != null){
                        int position = getAdapterPosition();
                        if(position != RecyclerView.NO_POSITION){
                            listener.onItemClick(position);
                        }
                    }
                }
            });
        }
    }

    public DriversAdapter(ArrayList<UserItem> localPlayerList){
        driverList = localPlayerList;
    }

    @NonNull
    @Override
    public PlayersViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View v = LayoutInflater.from(parent.getContext()).inflate(R.layout.single_entry_offer,parent,false);
        PlayersViewHolder mvh = new PlayersViewHolder(v,customListener);
        return  mvh;

    }

    public void onBindViewHolder(@NonNull DriversAdapter.PlayersViewHolder holder, int position) {
        UserItem currentPlayer = driverList.get(position);

        holder.imageInvite.setImageResource(currentPlayer.getInviteImg());
        holder.nickname.setText(currentPlayer.getNicknameText());
    }
    @Override
    public int getItemCount() {
        return driverList.size();
    }
}
