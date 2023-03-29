package com.example.teamake;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import java.util.ArrayList;

public class MatchesAdapter extends RecyclerView.Adapter<MatchesAdapter.MatchesViewHolder> {

    private ArrayList<MatchItem> matchList;
    private OnItemClickListener customListener;

    public interface OnItemClickListener {
        void  onItemClick(int position);
    }

    public void setOnItemClickLister(OnItemClickListener listener){
        customListener=listener;
    }


    public  static class  MatchesViewHolder extends RecyclerView.ViewHolder {
        ImageView sportImage;
        TextView sportTypeMatch,sportDateMatch,sportScore1,sportScore2;
        ImageButton inviteImCheck;

        public MatchesViewHolder(@NonNull View itemView, OnItemClickListener listener) {
            super(itemView);
            sportImage = itemView.findViewById(R.id.sportTypeMatchImg);
            sportTypeMatch = itemView.findViewById(R.id.sportTypeMatchText);
            sportDateMatch = itemView.findViewById(R.id.sportDateMatch);
            sportScore1 = itemView.findViewById(R.id.scoreMatch1);
            sportScore2 = itemView.findViewById(R.id.scoreMatch2);
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

    public MatchesAdapter(ArrayList<MatchItem> localMatchList){
        matchList = localMatchList;
    }


    @NonNull
    @Override
    public MatchesViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View v = LayoutInflater.from(parent.getContext()).inflate(R.layout.single_match,parent,false);
        MatchesViewHolder mvh = new MatchesViewHolder(v,customListener);
        return  mvh;

    }

    @Override
    public void onBindViewHolder(@NonNull MatchesAdapter.MatchesViewHolder holder, int position) {
        MatchItem currentMatch = matchList.get(position);

        holder.sportImage.setImageResource(currentMatch.getSportImg());
        holder.sportTypeMatch.setText(currentMatch.getSportTypeText());
        holder.sportDateMatch.setText(currentMatch.getDateMatch());
        holder.sportScore1.setText(String.valueOf(currentMatch.getScoreTeam1()));
        holder.sportScore2.setText(String.valueOf(currentMatch.getScoreTeam2()));
        holder.inviteImCheck.setImageResource(currentMatch.getInviteCheck());
        //holder.inviteImCheck.setVisibility(View.GONE);

        System.out.println(holder.sportScore1.getText());
        if(holder.sportScore1.getText().equals("-1")){
            holder.sportScore1.setText("");
            holder.sportScore2.setText("");
        }

    }

    @Override
    public int getItemCount() {
        return matchList.size();
    }


}
