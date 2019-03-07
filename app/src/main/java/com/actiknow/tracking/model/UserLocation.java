package com.actiknow.tracking.model;

public class UserLocation {
    private int user_id;
    private String latitude, longitude, app_time;
    
    public UserLocation () {
    }
    
    public UserLocation (int user_id, String latitude, String longitude, String app_time) {
        this.user_id = user_id;
        this.latitude = latitude;
        this.longitude = longitude;
        this.app_time = app_time;
    }
    
    public int getUser_id () {
        return user_id;
    }
    
    public void setUser_id (int user_id) {
        this.user_id = user_id;
    }
    
    public String getLatitude () {
        return latitude;
    }
    
    public void setLatitude (String latitude) {
        this.latitude = latitude;
    }
    
    public String getLongitude () {
        return longitude;
    }
    
    public void setLongitude (String longitude) {
        this.longitude = longitude;
    }
    
    public String getApp_time () {
        return app_time;
    }
    
    public void setApp_time (String app_time) {
        this.app_time = app_time;
    }
}
