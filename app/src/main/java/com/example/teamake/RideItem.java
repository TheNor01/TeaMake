package com.example.teamake;

public class RideItem {

    private int uniImg,inviteCheck,infoLocation;
    private String universityText;
    private String dateRide;

    private String timeRide; // HH:mm


    private String RideID;


    public RideItem(String RideID, int uniImg, String universityText, String dateMatch, String timeMatch, Integer locationInfo,Integer inviteCheck){
        this.RideID = RideID;
        this.uniImg = uniImg;
        this.universityText = universityText;
        this.dateRide = dateMatch;
        this.timeRide = timeMatch;
        this.infoLocation = locationInfo;
        this.inviteCheck = inviteCheck;

    }

    public int getUniImg() {
        return uniImg;
    }

    public int getInviteCheck() {
        return inviteCheck;
    }

    public String getUniversityText() {
        return universityText;
    }

    public String getDateRide() {
        return dateRide;
    }

    public String getTimeRide() {
        return timeRide;
    }

    public int getInfoLocation() {
        return infoLocation;
    }

    public String getRideID() {
        return RideID;
    }
}