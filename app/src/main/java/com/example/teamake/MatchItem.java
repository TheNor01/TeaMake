package com.example.teamake;

import android.content.Intent;
import android.widget.ImageView;

public class MatchItem {

    private int sportImg;
    private String sportTypeText;
    private String dateMatch;
    private int scoreTeam1,scoreTeam2;


    public MatchItem(int sportImg, String sportTypeText, String dateMatch, Integer score1, Integer score2){
        this.sportImg = sportImg;
        this.sportTypeText = sportTypeText;
        this.dateMatch = dateMatch;
        this.scoreTeam1 = score1;
        this.scoreTeam2 = score2;

    }

    public int getSportImg() {
        return sportImg;
    }

    public String getSportTypeText() {
        return sportTypeText;
    }

    public String getDateMatch() {
        return dateMatch;
    }

    public int getScoreTeam1() {
        return scoreTeam1;
    }

    public int getScoreTeam2() {
        return scoreTeam2;
    }
}
