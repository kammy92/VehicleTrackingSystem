package com.actiknow.tracking.helper;

import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;
import android.util.Log;

import com.actiknow.tracking.model.UserLocation;
import com.actiknow.tracking.utils.AppConfigTags;
import com.actiknow.tracking.utils.Utils;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.Locale;

public class DatabaseHandler extends SQLiteOpenHelper {
    // Database Version
    private static final int DATABASE_VERSION = 13;

    // Database Name
    private static final String DATABASE_NAME = "liveAudit";

    private static final String TABLE_USER_LOCATION = "geo_location";

    // Common column names
    private static final String KEY_ID = "id";
    private static final String KEY_CREATED_AT = "created_at";
    
    // REPORT Table - column names
    private static final String KEY_USER_ID = "user_id";
    private static final String KEY_LATITUDE = "latitude";
    private static final String KEY_LONGITUDE = "longitude";

    // AUDITOR_LOCATION Table - column names
    private static final String KEY_APP_TIME = "app_time";

    // Auditor location table create statement
    private static final String CREATE_TABLE_AUDITOR_LOCATION = "CREATE TABLE " + TABLE_USER_LOCATION
            + "(" + KEY_ID + " INTEGER PRIMARY KEY AUTOINCREMENT," + KEY_USER_ID + " INTEGER," +
            KEY_LATITUDE + " TEXT," + KEY_LONGITUDE + " TEXT," + KEY_APP_TIME + " TEXT," + KEY_CREATED_AT + " DATETIME" + ")";

    public DatabaseHandler (Context context) {
        super (context, DATABASE_NAME, null, DATABASE_VERSION);
    }

    @Override
    public void onCreate (SQLiteDatabase db) {
        db.execSQL (CREATE_TABLE_AUDITOR_LOCATION);
    }

    @Override
    public void onUpgrade (SQLiteDatabase db, int oldVersion, int newVersion) {
        db.execSQL ("DROP TABLE IF EXISTS " + TABLE_USER_LOCATION);
        onCreate (db);
    }
    
    // ------------------------ "auditor location" table methods ----------------//

    public long createUserLocation (UserLocation userLocation) {
        SQLiteDatabase db = this.getWritableDatabase ();
        Utils.showLog (Log.DEBUG, AppConfigTags.DATABASE_LOG, "Creating User Location", false);
        ContentValues values = new ContentValues ();
        values.put (KEY_USER_ID, userLocation.getUser_id ());
        values.put (KEY_LATITUDE, userLocation.getLatitude ());
        values.put (KEY_LONGITUDE, userLocation.getLongitude ());
        values.put (KEY_APP_TIME, userLocation.getApp_time ());
        values.put (KEY_CREATED_AT, getDateTime ());
        long id = db.insert (TABLE_USER_LOCATION, null, values);
        return id;
    }

    public List<UserLocation> getAllUserLocation () {
        List<UserLocation> userLocations = new ArrayList<UserLocation> ();
        String selectQuery = "SELECT  * FROM " + TABLE_USER_LOCATION;
        Utils.showLog (Log.DEBUG, AppConfigTags.DATABASE_LOG, "Get All User Locations", false);
        SQLiteDatabase db = this.getReadableDatabase ();
        Cursor c = db.rawQuery (selectQuery, null);
        // looping through all rows and adding to list
        if (c.moveToFirst ()) {
            do {
                UserLocation userLocation = new UserLocation ();
                userLocation.setUser_id (c.getInt (c.getColumnIndex (KEY_USER_ID)));
                userLocation.setApp_time (c.getString (c.getColumnIndex (KEY_APP_TIME)));
                userLocation.setLatitude (c.getString (c.getColumnIndex (KEY_LATITUDE)));
                userLocation.setLongitude (c.getString (c.getColumnIndex (KEY_LONGITUDE)));
                userLocations.add (userLocation);
            } while (c.moveToNext ());
        }
        return userLocations;
    }
    
    public void deleteAuditorLocation (String time) {
        SQLiteDatabase db = this.getWritableDatabase ();
        Utils.showLog (Log.DEBUG, AppConfigTags.DATABASE_LOG, "Delete auditor location where time = " + time, false);
        db.delete (TABLE_USER_LOCATION, KEY_APP_TIME + " = ?",
                new String[] {time});
    }

    public void deleteAllAuditorLocation () {
        SQLiteDatabase db = this.getWritableDatabase ();
        Utils.showLog (Log.DEBUG, AppConfigTags.DATABASE_LOG, "Delete all  locations", false);
        db.execSQL ("delete from " + TABLE_USER_LOCATION);
    }
    
    public void closeDB () {
        SQLiteDatabase db = this.getReadableDatabase ();
        if (db != null && db.isOpen ())
            db.close ();
    }

    private String getDateTime () {
        SimpleDateFormat dateFormat = new SimpleDateFormat ("yyyy-MM-dd HH:mm:ss", Locale.getDefault ());
        Date date = new Date ();
        return dateFormat.format (date);
    }
}