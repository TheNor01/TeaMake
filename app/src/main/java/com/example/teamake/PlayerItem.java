package com.example.teamake;

public class PlayerItem {

    private int inviteImg;
    private String nicknameText;


    public PlayerItem(int invImg, String nicknameText){
        this.inviteImg = invImg;
        this.nicknameText = nicknameText;
    }

    public int getInviteImg() {
        return inviteImg;
    }


    public void setNicknameToLooking(String text){
        nicknameText = text;
    }

    public String getNicknameText() {
        return nicknameText;
    }
}
