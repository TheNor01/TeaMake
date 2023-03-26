package com.example.teamake;

public class PlayerItem {

    private int inviteImg;
    private String nicknameText;

    private String UID;


    public PlayerItem(int invImg, String nicknameText,String UID){
        this.inviteImg = invImg;
        this.nicknameText = nicknameText;
        this.UID = UID;
    }

    public int getInviteImg() {
        return inviteImg;
    }
    public String getUID() {
        return UID;
    }


    public void setNicknameToLooking(String text){
        nicknameText = text;
    }

    public void setImageToPlayersPending(){
        this.inviteImg=R.drawable.baseline_how_to_reg_24;
    }

    public String getNicknameText() {
        return nicknameText;
    }



}
