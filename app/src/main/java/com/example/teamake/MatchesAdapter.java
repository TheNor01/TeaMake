package com.example.teamake;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import java.util.ArrayList;

public class MatchesAdapter extends RecyclerView.Adapter<MatchesAdapter.MatchesViewHolder> {

    private ArrayList<MatchItem> matchList;
    public static class MatchesViewHolder extends RecyclerView.ViewHolder {
        ImageView sportImage;
        TextView sportTypeMatch,sportDateMatch,sportScore1,sportScore2;

        public MatchesViewHolder(@NonNull View itemView) {
            super(itemView);
            sportImage = itemView.findViewById(R.id.sportTypeMatchImg);
            sportTypeMatch = itemView.findViewById(R.id.sportTypeMatchText);
            sportDateMatch = itemView.findViewById(R.id.sportDateMatch);
            sportScore1 = itemView.findViewById(R.id.scoreMatch1);
            sportScore2 = itemView.findViewById(R.id.scoreMatch2);
        }
    }

    public MatchesAdapter(ArrayList<MatchItem> localMatchList){
        matchList = localMatchList;
    }


    @NonNull
    @Override
    public MatchesViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View v = LayoutInflater.from(parent.getContext()).inflate(R.layout.single_match,parent,false);
        MatchesViewHolder mvh = new MatchesViewHolder(v);
        return  mvh;

    }

    @Override
    public void onBindViewHolder(@NonNull MatchesAdapter.MatchesViewHolder holder, int position) {
        MatchItem currentMatch = matchList.get(position);

        holder.sportImage.setImageResource(currentMatch.getSportImg());
        holder.sportTypeMatch.setText(currentMatch.getSportTypeText());
        holder.sportDateMatch.setText(currentMatch.getDateMatch());
        holder.sportScore1.setText(currentMatch.getScoreTeam1());
        holder.sportScore2.setText(currentMatch.getScoreTeam2());

    }

    @Override
    public int getItemCount() {
        return matchList.size();
    }


}
