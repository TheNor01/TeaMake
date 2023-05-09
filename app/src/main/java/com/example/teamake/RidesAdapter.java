package com.example.teamake;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import java.util.ArrayList;

public class RidesAdapter extends RecyclerView.Adapter<RidesAdapter.RidesViewHolder> {

    private ArrayList<RideItem> rideItems;
    private OnItemClickListener customListener;

    public interface OnItemClickListener {
        void  onItemClick(int position);
    }

    public void setOnItemClickLister(OnItemClickListener listener){
        customListener=listener;
    }


    public  static class RidesViewHolder extends RecyclerView.ViewHolder {
        ImageView uniImage;
        TextView universityName,rideDate,rideTime,locationCoord;
        ImageButton inviteImCheck;

        public RidesViewHolder(@NonNull View itemView, OnItemClickListener listener) {
            super(itemView);
            uniImage = itemView.findViewById(R.id.uniImg);
            universityName = itemView.findViewById(R.id.myUniversity);
            rideDate = itemView.findViewById(R.id.rideDate);
            rideTime = itemView.findViewById(R.id.rideTime);
            locationCoord = itemView.findViewById(R.id.locationInfo);

            inviteImCheck = itemView.findViewById(R.id.inviteImCheck);

            inviteImCheck.setOnClickListener(new View.OnClickListener() {
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

    public RidesAdapter(ArrayList<RideItem> localMatchList){
        rideItems = localMatchList;
    }


    @NonNull
    @Override
    public RidesViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View v = LayoutInflater.from(parent.getContext()).inflate(R.layout.single_ride,parent,false);
        RidesViewHolder mvh = new RidesViewHolder(v,customListener);
        return  mvh;

    }

    @Override
    public void onBindViewHolder(@NonNull RidesViewHolder holder, int position) {
        RideItem currentMatch = rideItems.get(position);

        holder.uniImage.setImageResource(currentMatch.getUniImg());
        holder.universityName.setText(currentMatch.getUniversityText());
        holder.rideDate.setText(currentMatch.getDateRide());
        holder.rideTime.setText(String.valueOf(currentMatch.getTimeRide()));
        holder.locationCoord.setText(String.valueOf(currentMatch.getLocationStarting()));
        holder.inviteImCheck.setImageResource(currentMatch.getInviteCheck());
        //holder.inviteImCheck.setVisibility(View.GONE);

        if(holder.rideDate.getText().equals("undefined")){
            holder.rideDate.setText("");
            holder.rideTime.setText("");
        }

    }

    @Override
    public int getItemCount() {
        return rideItems.size();
    }


}
