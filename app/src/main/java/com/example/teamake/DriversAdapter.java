package com.example.teamake;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import java.util.ArrayList;

public class DriversAdapter extends RecyclerView.Adapter<DriversAdapter.DriversViewHolder>{

    private ArrayList<UserItem> driverList;

    private OnItemClickListener customListener;

    public interface OnItemClickListener {
        void  onItemClick(int position);
    }

    public void setOnItemClickLister(OnItemClickListener listener){
        customListener=listener;
    }

    public static class DriversViewHolder extends RecyclerView.ViewHolder {
        ImageView imageInvite;
        TextView nickname;
        TextView seats;


        // i have to pass listener into costructor, cause i cannot access to static field

        public DriversViewHolder(@NonNull View itemView, OnItemClickListener listener) {
            super(itemView);
            imageInvite = itemView.findViewById(R.id.inviteImg);
            nickname = itemView.findViewById(R.id.nicknameUser);
            seats = itemView.findViewById(R.id.freeSeats);

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
    public DriversViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View v = LayoutInflater.from(parent.getContext()).inflate(R.layout.single_entry_offer,parent,false);
        DriversViewHolder mvh = new DriversViewHolder(v,customListener);
        return  mvh;

    }

    public void onBindViewHolder(@NonNull DriversViewHolder holder, int position) {
        UserItem currentDriver = driverList.get(position);

        holder.imageInvite.setImageResource(currentDriver.getInviteImg());
        holder.nickname.setText(currentDriver.getNicknameText());
        holder.seats.setText(Integer.toString(currentDriver.getFreeSeats()));
    }
    @Override
    public int getItemCount() {
        return driverList.size();
    }
}
