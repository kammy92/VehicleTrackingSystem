package com.actiknow.tracking.utils;

public class AppConfigURL {
    public static String version = "v1.0";
    
    private static String BASE_URL = "http://drkamalguptabjp.com/api/" + version + "/";
//    private static String BASE_URL = " https://factory-app-cammy92.c9users.io/api_temp/" + version + "/";

    public static String LOGIN = BASE_URL + "app/vts/login";
    public static String FORGOT_PASSWORD = BASE_URL + "app/vts/forgot-password";
    public static String URL_INIT = BASE_URL + "app/vts/init/application";
    
    public static String SUBMIT_USER_LOCATION = BASE_URL + "app/vts/location";
}

