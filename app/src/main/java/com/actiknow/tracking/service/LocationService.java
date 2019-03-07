package com.actiknow.tracking.service;

import android.app.AlarmManager;
import android.app.PendingIntent;
import android.app.Service;
import android.content.Context;
import android.content.Intent;
import android.location.Location;
import android.location.LocationListener;
import android.location.LocationManager;
import android.os.Bundle;
import android.os.IBinder;
import android.util.Log;

import com.actiknow.tracking.app.AppController;
import com.actiknow.tracking.helper.DatabaseHandler;
import com.actiknow.tracking.model.UserLocation;
import com.actiknow.tracking.utils.AppConfigTags;
import com.actiknow.tracking.utils.AppConfigURL;
import com.actiknow.tracking.utils.AppDetailsPref;
import com.actiknow.tracking.utils.Constants;
import com.actiknow.tracking.utils.NetworkConnection;
import com.actiknow.tracking.utils.UserDetailsPref;
import com.actiknow.tracking.utils.Utils;
import com.android.volley.AuthFailureError;
import com.android.volley.Request;
import com.android.volley.VolleyError;
import com.android.volley.toolbox.StringRequest;

import org.json.JSONException;
import org.json.JSONObject;

import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Date;
import java.util.HashMap;
import java.util.Hashtable;
import java.util.List;
import java.util.Locale;
import java.util.Map;


public class LocationService extends Service implements LocationListener {
    
    // The minimum distance to change Updates in meters
    private static final long MIN_DISTANCE_CHANGE_FOR_UPDATES = 0; // 0 meters
    // The minimum time between updates in milliseconds
    private static final long MIN_TIME_BW_UPDATES = 60 * 1000; // 1 second
    public static String LOG = "Log";
    
    //   JSONParser jsonParser = new JSONParser ();
    private final Context mContext;
    // Declaring a Location Manager
    protected LocationManager locationManager;
    DatabaseHandler db;
    Calendar cur_cal = Calendar.getInstance ();
    // flag for GPS status
    boolean isGPSEnabled = false;
    // flag for network status
    boolean isNetworkEnabled = false;
    // flag for GPS status
    boolean canGetLocation = false;
    Location location; // location
    double latitude; // latitude
    double longitude; // longitude
    
    UserDetailsPref userDetailsPref;
    AppDetailsPref appDetailsPref;
    
    public LocationService (Context context) {
        this.mContext = context;
    }
    
    public LocationService () {
        super ();
        mContext = LocationService.this;
    }
    
    @Override
    public IBinder onBind (Intent intent) {
        return null;
    }
    
    @Override
    public int onStartCommand (Intent intent, int flags, int startId) {
        Context context = AppController.getAppContext ();
        db = new DatabaseHandler (context);
        Utils.showLog (Log.INFO, "LOCATION SERVICE", "SERVICE STARTED", true);
        int user_id = userDetailsPref.getIntPref (context, UserDetailsPref.USER_ID);
        Calendar c = Calendar.getInstance ();
        SimpleDateFormat df = new SimpleDateFormat ("yyyy-MM-dd HH:mm:ss");
        final String formattedDate = df.format (c.getTime ());
        
        Calendar now = Calendar.getInstance ();
        int hour = now.get (Calendar.HOUR_OF_DAY); // Get hour in 24 hour format
        int minute = now.get (Calendar.MINUTE);
        Date date = parseDate (hour + ":" + minute);
        Date dateCompareOne = parseDate (appDetailsPref.getStringPref (context, AppDetailsPref.LOGGING_START_TIME));
        Date dateCompareTwo = parseDate (appDetailsPref.getStringPref (context, AppDetailsPref.LOGGING_END_TIME));
        try {
            if (dateCompareOne.before (date) && dateCompareTwo.after (date)) {
                Utils.showLog (Log.INFO, "LOCATION SERVICE", "WITHIN TIME LIMITS", true);
                sendLocationDetailsToServer (context, user_id, String.valueOf (getLocation ().getLatitude ()), String.valueOf (getLocation ().getLongitude ()), formattedDate);
            } else {
                Utils.showLog (Log.INFO, "LOCATION SERVICE", "NOT WITHIN TIME LIMITS", true);
            }
        } catch (Exception e) {
            Utils.showLog (Log.ERROR, "EXCEPTION", "Exception in location", true);
        }
        return START_STICKY;
    }
    
    @Override
    public void onCreate () {
        super.onCreate ();
        userDetailsPref = UserDetailsPref.getInstance ();
        appDetailsPref = AppDetailsPref.getInstance ();
    
        Utils.showLog (Log.INFO, "LOCATION SERVICE", "SERVICE CREATED", true);
        Intent intent = new Intent (this, LocationService.class);
        PendingIntent pendingIntent = PendingIntent.getService (getApplicationContext (), 0, intent, 0);
        AlarmManager alarm = (AlarmManager) getSystemService (Context.ALARM_SERVICE);
        cur_cal.setTimeInMillis (System.currentTimeMillis ());
        long interval = Long.parseLong (appDetailsPref.getStringPref (this, AppDetailsPref.LOGGING_INTERVAL).length ()>0 ? appDetailsPref.getStringPref (this, AppDetailsPref.LOGGING_INTERVAL) : "0");
        alarm.setRepeating (AlarmManager.RTC_WAKEUP, cur_cal.getTimeInMillis (), interval, pendingIntent);
    }
    
    @Override
    public void onDestroy () {
        super.onDestroy ();
        Utils.showLog (Log.INFO, "LOCATION SERVICE", "SERVICE DESTROYED", true);
    }
    
    private void sendLocationDetailsToServer (Context context, final int auditor_id, final String latitude, final String longitude, final String formattedDate) {
        final UserLocation userLocation = new UserLocation (auditor_id, latitude, longitude, formattedDate);
        if (NetworkConnection.isNetworkAvailable (context)) {
            Utils.showLog (Log.INFO, AppConfigTags.URL, AppConfigURL.SUBMIT_USER_LOCATION, true);
            StringRequest strRequest1 = new StringRequest (Request.Method.POST, AppConfigURL.SUBMIT_USER_LOCATION,
                    new com.android.volley.Response.Listener<String> () {
                        @Override
                        public void onResponse (String response) {
                            Utils.showLog (Log.INFO, AppConfigTags.SERVER_RESPONSE, response, true);
                            if (response != null) {
                                try {
                                    JSONObject jsonObj = new JSONObject (response);
                                    if (jsonObj.getBoolean (AppConfigTags.ERROR)) {
                                    
                                    } else {
                                        db.createUserLocation (userLocation);
                                    }
                                } catch (JSONException e) {
                                    e.printStackTrace ();
                                }
                            } else {
                                Utils.showLog (Log.WARN, AppConfigTags.SERVER_RESPONSE, AppConfigTags.DIDNT_RECEIVE_ANY_DATA_FROM_SERVER, true);
                            }
                        }
                    },
                    new com.android.volley.Response.ErrorListener () {
                        @Override
                        public void onErrorResponse (VolleyError error) {
                            Utils.showLog (Log.ERROR, AppConfigTags.VOLLEY_ERROR, error.toString (), true);
                            db.createUserLocation (userLocation);
                        }
                    }) {
                @Override
                protected Map<String, String> getParams () throws AuthFailureError {
                    Map<String, String> params = new Hashtable<String, String> ();
                    params.put (AppConfigTags.USER_ID, String.valueOf (userLocation.getUser_id ()));
                    params.put (AppConfigTags.LATITUDE, userLocation.getLatitude ());
                    params.put (AppConfigTags.LONGITUDE, userLocation.getLongitude ());
                    params.put (AppConfigTags.APP_TIME, userLocation.getApp_time ());
                    Utils.showLog (Log.INFO, AppConfigTags.PARAMETERS_SENT_TO_THE_SERVER, "" + params, true);
                    return params;
                }
                
                @Override
                public Map<String, String> getHeaders () throws AuthFailureError {
                    Map<String, String> params = new HashMap<> ();
                    params.put (AppConfigTags.HEADER_API_KEY, Constants.api_key);
                    Utils.showLog (Log.INFO, AppConfigTags.HEADERS_SENT_TO_THE_SERVER, "" + params, false);
                    return params;
                }
            };
            Utils.sendRequest (strRequest1, 30);
        } else {
            db.createUserLocation (userLocation);
        }
    }
    
    public Location getLocation () {
        try {
            locationManager = (LocationManager) mContext.getSystemService (LOCATION_SERVICE);
            // getting GPS status
            isGPSEnabled = locationManager.isProviderEnabled (LocationManager.GPS_PROVIDER);
            // getting network status
            isNetworkEnabled = locationManager.isProviderEnabled (LocationManager.NETWORK_PROVIDER);
            if (! isGPSEnabled && ! isNetworkEnabled) {
                // no network provider is enabled
            } else {
                this.canGetLocation = true;
                if (isNetworkEnabled) {
                    //updates will be send according to these arguments
                    locationManager.requestLocationUpdates (LocationManager.NETWORK_PROVIDER, MIN_TIME_BW_UPDATES, MIN_DISTANCE_CHANGE_FOR_UPDATES, this);
                    Utils.showLog (Log.INFO, "LOCATION TYPE", "NETWORK", true);
                    if (locationManager != null) {
                        location = locationManager.getLastKnownLocation (LocationManager.NETWORK_PROVIDER);
                        if (location != null) {
                            latitude = location.getLatitude ();
                            longitude = location.getLongitude ();
                        }
                    }
                }
                // if GPS Enabled get lat/long using GPS Services
                if (isGPSEnabled) {
                    if (location == null) {
                        locationManager.requestLocationUpdates (LocationManager.GPS_PROVIDER, MIN_TIME_BW_UPDATES, MIN_DISTANCE_CHANGE_FOR_UPDATES, this);
                        Utils.showLog (Log.INFO, "LOCATION TYPE", "GPS", true);
                        if (locationManager != null) {
                            location = locationManager.getLastKnownLocation (LocationManager.GPS_PROVIDER);
                            if (location != null) {
                                latitude = location.getLatitude ();
                                longitude = location.getLongitude ();
                            }
                        }
                    }
                }
            }
        } catch (Exception e) {
            e.printStackTrace ();
        }
        return location;
    }
    
    @Override
    public void onLocationChanged (Location location) {
        Utils.showLog (Log.INFO, "LOCATION SERVICE", "IN LOCATION CHANGED", true);
//        Context appCtx = AppController.getAppContext ();
//        LoginDetailsPref loginDetailsPref = LoginDetailsPref.getInstance ();
//        int auditor_id = loginDetailsPref.getIntPref (appCtx, LoginDetailsPref.AUDITOR_ID);
//        sendLocationDetailsToServer (auditor_id, String.valueOf (getLocation ().getLatitude ()), String.valueOf (getLocation ().getLongitude ()));
    }
    
    @Override
    public void onStatusChanged (String provider, int status, Bundle extras) {
    }
    
    @Override
    public void onProviderEnabled (String provider) {
    }
    
    @Override
    public void onProviderDisabled (String provider) {
    }
    
    private Date parseDate (String date) {
        final String inputFormat = "HH:mm";
        SimpleDateFormat inputParser = new SimpleDateFormat (inputFormat, Locale.US);
        try {
            return inputParser.parse (date);
        } catch (java.text.ParseException e) {
            return new Date (0);
        }
    }
    
    private void uploadStoredAuditorLocationToServer () {
        Utils.showLog (Log.DEBUG, AppConfigTags.TAG, "Getting all the auditor_location from local database", true);
        List<UserLocation> allUserLocations = db.getAllUserLocation ();
        for (UserLocation userLocation : allUserLocations) {
            final UserLocation finalUserLocation = userLocation;
            if (NetworkConnection.isNetworkAvailable (this)) {
                Utils.showLog (Log.INFO, AppConfigTags.URL, AppConfigURL.SUBMIT_USER_LOCATION, true);
                StringRequest strRequest1 = new StringRequest (Request.Method.POST, AppConfigURL.SUBMIT_USER_LOCATION,
                        new com.android.volley.Response.Listener<String> () {
                            @Override
                            public void onResponse (String response) {
                                Utils.showLog (Log.INFO, AppConfigTags.SERVER_RESPONSE, response, true);
                                if (response != null) {
                                    try {
                                        JSONObject jsonObj = new JSONObject (response);
                                        if (jsonObj.getBoolean (AppConfigTags.ERROR)) {
                                            db.deleteAuditorLocation (finalUserLocation.getApp_time ());
                                        }
                                    } catch (JSONException e) {
                                        e.printStackTrace ();
                                    }
                                } else {
                                    Utils.showLog (Log.WARN, AppConfigTags.SERVER_RESPONSE, AppConfigTags.DIDNT_RECEIVE_ANY_DATA_FROM_SERVER, true);
                                }
                            }
                        },
                        new com.android.volley.Response.ErrorListener () {
                            @Override
                            public void onErrorResponse (VolleyError error) {
                                Utils.showLog (Log.ERROR, AppConfigTags.VOLLEY_ERROR, error.toString (), true);
                            }
                        }) {
                    @Override
                    protected Map<String, String> getParams () throws AuthFailureError {
                        Map<String, String> params = new Hashtable<String, String> ();
                        params.put (AppConfigTags.USER_ID, String.valueOf (finalUserLocation.getUser_id ()));
                        params.put (AppConfigTags.LATITUDE, finalUserLocation.getLatitude ());
                        params.put (AppConfigTags.LONGITUDE, finalUserLocation.getLongitude ());
                        params.put (AppConfigTags.APP_TIME, finalUserLocation.getApp_time ());
                        Utils.showLog (Log.INFO, AppConfigTags.PARAMETERS_SENT_TO_THE_SERVER, "" + params, true);
                        return params;
                    }
                    
                    @Override
                    public Map<String, String> getHeaders () throws AuthFailureError {
                        Map<String, String> params = new HashMap<> ();
                        params.put (AppConfigTags.HEADER_API_KEY, Constants.api_key);
                        Utils.showLog (Log.INFO, AppConfigTags.HEADERS_SENT_TO_THE_SERVER, "" + params, false);
                        return params;
                    }
                };
                Utils.sendRequest (strRequest1, 30);
            } else {
                Utils.showLog (Log.WARN, AppConfigTags.TAG, "If no internet connection", true);
            }
        }
    }
}