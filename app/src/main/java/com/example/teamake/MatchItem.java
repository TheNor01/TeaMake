package com.example.teamake;

import android.content.Intent;
import android.widget.ImageView;

public class MatchItem {

    private int sportImg,inviteCheck;
    private String sportTypeText;
    private String dateMatch;
    private int scoreTeam1,scoreTeam2;

    private String MatchID;


    public MatchItem(String matchID,int sportImg, String sportTypeText, String dateMatch, Integer score1, Integer score2,Integer inviteCheck){
        this.MatchID = matchID;
        this.sportImg = sportImg;
        this.sportTypeText = sportTypeText;
        this.dateMatch = dateMatch;
        this.scoreTeam1 = score1;
        this.scoreTeam2 = score2;
        this.inviteCheck = inviteCheck;

    }

    public int getSportImg() {
        return sportImg;
    }

    public int getInviteCheck() {
        return inviteCheck;
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

    public String getMatchID() {
        return MatchID;
    }

}
