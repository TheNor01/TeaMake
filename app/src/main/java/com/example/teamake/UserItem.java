package com.example.teamake;

public class UserItem {

    private int inviteImg;
    private String nicknameText;

    private String UID;


    public UserItem(int invImg, String nicknameText, String UID){
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

    public void setUID(String text){
        UID = text;
    }


    public void setImageToPlayersPending(){
        this.inviteImg=R.drawable.baseline_how_to_reg_24;
    }

    public String getNicknameText() {
        return nicknameText;
    }



}
