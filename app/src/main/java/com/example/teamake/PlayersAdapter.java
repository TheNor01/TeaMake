package com.example.teamake;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import java.util.ArrayList;

public class PlayersAdapter extends RecyclerView.Adapter<PlayersAdapter.PlayersViewHolder>{

    private ArrayList<PlayerItem> playerList;

    private OnItemClickListener customListener;

    public interface OnItemClickListener {
        void  onItemClick(int position);
    }

    public void setOnItemClickLister(OnItemClickListener listener){
        customListener=listener;
    }

    public static class PlayersViewHolder extends RecyclerView.ViewHolder {
        ImageView playerImageInvite;
        TextView nickname;


        // i have to pass listener into costructor, cause i cannot access to static field

        public PlayersViewHolder(@NonNull View itemView, OnItemClickListener listener) {
            super(itemView);
            playerImageInvite = itemView.findViewById(R.id.sportTypeMatchImg);
            nickname = itemView.findViewById(R.id.nicknamePlayer);

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

    public PlayersAdapter(ArrayList<PlayerItem> localPlayerList){
        playerList = localPlayerList;
    }

    @NonNull
    @Override
    public PlayersViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View v = LayoutInflater.from(parent.getContext()).inflate(R.layout.single_entry_offer,parent,false);
        PlayersViewHolder mvh = new PlayersViewHolder(v,customListener);
        return  mvh;

    }

    public void onBindViewHolder(@NonNull PlayersAdapter.PlayersViewHolder holder, int position) {
        PlayerItem currentPlayer = playerList.get(position);

        holder.playerImageInvite .setImageResource(currentPlayer.getInviteImg());
        holder.nickname.setText(currentPlayer.getNicknameText());
    }
    @Override
    public int getItemCount() {
        return playerList.size();
    }
}
