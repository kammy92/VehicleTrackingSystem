package com.actiknow.tracking.activity;

import android.Manifest;
import android.annotation.TargetApi;
import android.app.Activity;
import android.app.AlertDialog;
import android.app.ProgressDialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.pm.PackageInfo;
import android.content.pm.PackageManager;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.provider.Settings;
import android.util.Log;
import android.view.View;
import android.widget.RelativeLayout;
import android.widget.TextView;

import com.actiknow.tracking.R;
import com.actiknow.tracking.service.LocationService;
import com.actiknow.tracking.utils.AppConfigTags;
import com.actiknow.tracking.utils.AppConfigURL;
import com.actiknow.tracking.utils.AppDetailsPref;
import com.actiknow.tracking.utils.Constants;
import com.actiknow.tracking.utils.NetworkConnection;
import com.actiknow.tracking.utils.SetTypeFace;
import com.actiknow.tracking.utils.UserDetailsPref;
import com.actiknow.tracking.utils.Utils;
import com.afollestad.materialdialogs.DialogAction;
import com.afollestad.materialdialogs.MaterialDialog;
import com.android.volley.AuthFailureError;
import com.android.volley.DefaultRetryPolicy;
import com.android.volley.Request;
import com.android.volley.Response;
import com.android.volley.VolleyError;
import com.android.volley.toolbox.StringRequest;
import com.mikepenz.fontawesome_typeface_library.FontAwesome;
import com.mikepenz.materialdrawer.AccountHeader;
import com.mikepenz.materialdrawer.AccountHeaderBuilder;
import com.mikepenz.materialdrawer.Drawer;
import com.mikepenz.materialdrawer.DrawerBuilder;
import com.mikepenz.materialdrawer.model.PrimaryDrawerItem;
import com.mikepenz.materialdrawer.model.ProfileDrawerItem;
import com.mikepenz.materialdrawer.model.interfaces.IDrawerItem;
import com.mikepenz.materialdrawer.model.interfaces.IProfile;

import org.json.JSONObject;

import java.util.HashMap;
import java.util.Hashtable;
import java.util.Map;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.coordinatorlayout.widget.CoordinatorLayout;

import static android.Manifest.permission.ACCESS_COARSE_LOCATION;
import static android.Manifest.permission.RECEIVE_BOOT_COMPLETED;

public class MainActivity extends AppCompatActivity {
    Bundle savedInstanceState;
    UserDetailsPref userDetailsPref;
    AppDetailsPref appDetailsPref;
    ProgressDialog progressDialog;
    CoordinatorLayout clMain;
    RelativeLayout rlBack;
    private AccountHeader headerResult = null;
    private Drawer result = null;
    
    TextView tvUserName;
    TextView tvUserMobile;
    TextView tvUserEmail;
    TextView tvUserConstituency;
    TextView tvUserVehicleNumber;
    
    
    public static int PERMISSION_REQUEST_CODE = 11;
    
    
    @Override
    protected void onCreate (Bundle savedInstanceState) {
        super.onCreate (savedInstanceState);
        setContentView (R.layout.activity_main);
        initView ();
        initData ();
        initListener ();
        initApplication ();
        isLogin ();
        initDrawer ();
        checkPermissions ();
        initService ();
    }
    
    private void isLogin () {
        if (userDetailsPref.getIntPref (MainActivity.this, UserDetailsPref.USER_ID) == 0) {
            Intent intent = new Intent (MainActivity.this, LoginActivity.class);
            startActivity (intent);
            finish ();
            overridePendingTransition (R.anim.slide_in_left, R.anim.slide_out_right);
        }
    }
    
    private void initListener () {
        rlBack.setOnClickListener (new View.OnClickListener () {
            @Override
            public void onClick (View view) {
                result.openDrawer ();
            }
        });
    }
    
    private void initView () {
        rlBack = (RelativeLayout) findViewById (R.id.rlBack);
        clMain = (CoordinatorLayout) findViewById (R.id.clMain);
        tvUserName = (TextView) findViewById (R.id.tvUserName);
        tvUserMobile = (TextView) findViewById (R.id.tvUserMobile);
        tvUserEmail = (TextView) findViewById (R.id.tvUserEmail);
        tvUserConstituency = (TextView) findViewById (R.id.tvUserConstituency);
        tvUserVehicleNumber = (TextView) findViewById (R.id.tvUserVehicleNumber);
    }
    
    private void initData () {
        Utils.setTypefaceToAllViews (this, clMain);
        progressDialog = new ProgressDialog (this);
        userDetailsPref = UserDetailsPref.getInstance ();
        appDetailsPref = AppDetailsPref.getInstance ();
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
            getWindow ().getDecorView ().setSystemUiVisibility (View.SYSTEM_UI_FLAG_LIGHT_STATUS_BAR);
        }
        tvUserName.setText ("Name: " + userDetailsPref.getStringPref (this, UserDetailsPref.USER_NAME));
        tvUserMobile.setText ("Mobile: " + userDetailsPref.getStringPref (this, UserDetailsPref.USER_MOBILE));
        tvUserEmail.setText ("Email: " + userDetailsPref.getStringPref (this, UserDetailsPref.USER_EMAIL));
        tvUserConstituency.setText ("Constituency: " + userDetailsPref.getStringPref (this, UserDetailsPref.USER_CONSTITUENCY));
        tvUserVehicleNumber.setText ("Vehicle: " + userDetailsPref.getStringPref (this, UserDetailsPref.USER_VEHICLE_NUMBER));
    }
    
    private void initDrawer () {
        headerResult = new AccountHeaderBuilder ()
                .withActivity (this)
                .withCompactStyle (false)
                .withTypeface (SetTypeFace.getTypeface (MainActivity.this))
                .withPaddingBelowHeader (false)
                .withSelectionListEnabled (false)
                .withSelectionListEnabledForSingleProfile (false)
                .withProfileImagesVisible (true)
                .withDividerBelowHeader (true)
                .withTextColor (getResources ().getColor (R.color.primary_text))
                .withOnlyMainProfileImageVisible (false)
                .withDividerBelowHeader (true)
                .withHeaderBackground (R.color.text_color_grey_light2)
                .withSavedInstance (savedInstanceState)
                .withOnAccountHeaderListener (new AccountHeader.OnAccountHeaderListener () {
                    @Override
                    public boolean onProfileChanged (View view, IProfile profile, boolean currentProfile) {
                        return false;
                    }
                })
                .build ();
        
        ProfileDrawerItem profileDrawerItem = new ProfileDrawerItem ();
        profileDrawerItem.withName (userDetailsPref.getStringPref (MainActivity.this, UserDetailsPref.USER_NAME));
        profileDrawerItem.withEmail (userDetailsPref.getStringPref (MainActivity.this, UserDetailsPref.USER_MOBILE));
        
        
        profileDrawerItem.withIcon (R.drawable.ic_profile_male);
        headerResult.addProfiles (profileDrawerItem);
        
        DrawerBuilder drawerBuilder = new DrawerBuilder ()
                .withActivity (this)
                .withAccountHeader (headerResult)
                .withSavedInstance (savedInstanceState)
                .withOnDrawerItemClickListener (new Drawer.OnDrawerItemClickListener () {
                    @Override
                    public boolean onItemClick (View view, int position, IDrawerItem drawerItem) {
                        switch ((int) drawerItem.getIdentifier ()) {
                            case 2:
                                showLogOutDialog ();
                                break;
                        }
                        return false;
                    }
                });
        
        drawerBuilder.addDrawerItems (
                new PrimaryDrawerItem ().withName ("Home").withIcon (FontAwesome.Icon.faw_home).withIdentifier (1).withSelectable (false).withTypeface (SetTypeFace.getTypeface (MainActivity.this)),
                new PrimaryDrawerItem ().withName ("Sign Out").withIcon (FontAwesome.Icon.faw_sign_out).withIdentifier (2).withSelectable (false).withTypeface (SetTypeFace.getTypeface (MainActivity.this))
        );
        result = drawerBuilder.build ();
    }
    
    private void initService () {
        if (userDetailsPref.getIntPref (this, UserDetailsPref.USER_ID) != 0) {
            Intent mServiceIntent = new Intent (this, LocationService.class);
            startService (mServiceIntent);
        }
    }
    
    private void showLogOutDialog () {
        MaterialDialog dialog = new MaterialDialog.Builder (this)
                .contentColor (getResources ().getColor (R.color.primary_text))
                .positiveColor (getResources ().getColor (R.color.primary_text))
                .negativeColor (getResources ().getColor (R.color.primary_text))
                .content ("Do you wish to Sign Out?")
                .positiveText ("Yes")
                .negativeText ("No")
                .typeface (SetTypeFace.getTypeface (MainActivity.this), SetTypeFace.getTypeface (MainActivity.this))
                .onPositive (new MaterialDialog.SingleButtonCallback () {
                    @Override
                    public void onClick (@NonNull MaterialDialog dialog, @NonNull DialogAction which) {
                        userDetailsPref.putStringPref (MainActivity.this, UserDetailsPref.USER_NAME, "");
                        userDetailsPref.putStringPref (MainActivity.this, UserDetailsPref.USER_MOBILE, "");
                        userDetailsPref.putStringPref (MainActivity.this, UserDetailsPref.USER_EMAIL, "");
                        userDetailsPref.putIntPref (MainActivity.this, UserDetailsPref.USER_ID, 0);
                        Intent intent = new Intent (MainActivity.this, LoginActivity.class);
                        intent.setFlags (Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
                        startActivity (intent);
                        overridePendingTransition (R.anim.slide_in_left, R.anim.slide_out_right);
                    }
                }).build ();
        dialog.show ();
    }
    
    private void initApplication () {
        PackageInfo pInfo = null;
        try {
            pInfo = getPackageManager ().getPackageInfo (getPackageName (), 0);
        } catch (PackageManager.NameNotFoundException e) {
            e.printStackTrace ();
        }
        final String android_id = Settings.Secure.getString (getContentResolver (), Settings.Secure.ANDROID_ID);
        final PackageInfo finalPInfo = pInfo;
        
        if (NetworkConnection.isNetworkAvailable (this)) {
            Utils.showLog (Log.INFO, AppConfigTags.URL, AppConfigURL.URL_INIT, true);
            StringRequest strRequest = new StringRequest (Request.Method.POST, AppConfigURL.URL_INIT,
                    new Response.Listener<String> () {
                        @Override
                        public void onResponse (String response) {
                            Utils.showLog (Log.INFO, AppConfigTags.SERVER_RESPONSE, response, true);
                            if (response != null) {
                                try {
                                    JSONObject jsonObj = new JSONObject (response);
                                    boolean error = jsonObj.getBoolean (AppConfigTags.ERROR);
                                    String message = jsonObj.getString (AppConfigTags.MESSAGE);
                                    if (! error) {
                                        userDetailsPref.putStringPref (MainActivity.this, UserDetailsPref.USER_NAME, jsonObj.getString (AppConfigTags.USER_NAME));
                                        userDetailsPref.putStringPref (MainActivity.this, UserDetailsPref.USER_MOBILE, jsonObj.getString (AppConfigTags.USER_MOBILE));
                                        userDetailsPref.putStringPref (MainActivity.this, UserDetailsPref.USER_EMAIL, jsonObj.getString (AppConfigTags.USER_EMAIL));
                                        userDetailsPref.putStringPref (MainActivity.this, UserDetailsPref.USER_CONSTITUENCY, jsonObj.getString (AppConfigTags.USER_CONSTITUENCY));
                                        userDetailsPref.putStringPref (MainActivity.this, UserDetailsPref.USER_VEHICLE_NUMBER, jsonObj.getString (AppConfigTags.USER_VEHICLE_NUMBER));
                                        appDetailsPref.putStringPref (MainActivity.this, AppDetailsPref.LOGGING_START_TIME, jsonObj.getString (AppConfigTags.LOGGING_START_TIME));
                                        appDetailsPref.putStringPref (MainActivity.this, AppDetailsPref.LOGGING_END_TIME, jsonObj.getString (AppConfigTags.LOGGING_END_TIME));
                                        appDetailsPref.putStringPref (MainActivity.this, AppDetailsPref.LOGGING_INTERVAL, jsonObj.getString (AppConfigTags.LOGGING_INTERVAL));
    
                                        if (jsonObj.getInt (AppConfigTags.VERSION_UPDATE) > 0) {
                                            if (jsonObj.getInt (AppConfigTags.VERSION_CRITICAL) == 1) {
                                                MaterialDialog dialog = new MaterialDialog.Builder (MainActivity.this)
                                                        .title ("New Update Available")
                                                        .content (jsonObj.getString (AppConfigTags.UPDATE_MESSAGE))
                                                        .titleColor (getResources ().getColor (R.color.primary_text))
                                                        .positiveColor (getResources ().getColor (R.color.primary_text))
                                                        .contentColor (getResources ().getColor (R.color.primary_text))
                                                        .negativeColor (getResources ().getColor (R.color.primary_text))
                                                        .typeface (SetTypeFace.getTypeface (MainActivity.this), SetTypeFace.getTypeface (MainActivity.this))
                                                        .canceledOnTouchOutside (false)
                                                        .cancelable (false)
                                                        .positiveText (R.string.dialog_action_update)
                                                        .negativeText (R.string.dialog_action_exit)
                                                        .onPositive (new MaterialDialog.SingleButtonCallback () {
                                                            @Override
                                                            public void onClick (@NonNull MaterialDialog dialog, @NonNull DialogAction which) {
                                                                final String appPackageName = getPackageName ();
                                                                try {
                                                                    startActivity (new Intent (Intent.ACTION_VIEW, Uri.parse ("market://details?id=" + appPackageName)));
                                                                } catch (android.content.ActivityNotFoundException anfe) {
                                                                    startActivity (new Intent (Intent.ACTION_VIEW, Uri.parse ("https://play.google.com/store/apps/details?id=" + appPackageName)));
                                                                }
                                                            }
                                                        })
                                                        .onNegative (new MaterialDialog.SingleButtonCallback () {
                                                            @Override
                                                            public void onClick (@NonNull MaterialDialog dialog, @NonNull DialogAction which) {
                                                                finish ();
                                                                overridePendingTransition (R.anim.slide_in_left, R.anim.slide_out_right);
                                                            }
                                                        }).build ();
                                                
                                                dialog.getActionButton (DialogAction.POSITIVE).setOnClickListener (new CustomListener (MainActivity.this, dialog, DialogAction.POSITIVE));
                                                dialog.getActionButton (DialogAction.NEGATIVE).setOnClickListener (new CustomListener (MainActivity.this, dialog, DialogAction.NEGATIVE));
                                                dialog.show ();
                                            } else {
                                                MaterialDialog dialog = new MaterialDialog.Builder (MainActivity.this)
                                                        .title ("New Update Available")
                                                        .content (jsonObj.getString (AppConfigTags.UPDATE_MESSAGE))
                                                        .titleColor (getResources ().getColor (R.color.primary_text))
                                                        .positiveColor (getResources ().getColor (R.color.primary_text))
                                                        .contentColor (getResources ().getColor (R.color.primary_text))
                                                        .negativeColor (getResources ().getColor (R.color.primary_text))
                                                        .typeface (SetTypeFace.getTypeface (MainActivity.this), SetTypeFace.getTypeface (MainActivity.this))
                                                        .canceledOnTouchOutside (true)
                                                        .cancelable (true)
                                                        .positiveText (R.string.dialog_action_update)
                                                        .negativeText (R.string.dialog_action_ignore)
                                                        .onPositive (new MaterialDialog.SingleButtonCallback () {
                                                            @Override
                                                            public void onClick (@NonNull MaterialDialog dialog, @NonNull DialogAction which) {
                                                                final String appPackageName = getPackageName ();
                                                                try {
                                                                    startActivity (new Intent (Intent.ACTION_VIEW, Uri.parse ("market://details?id=" + appPackageName)));
                                                                } catch (android.content.ActivityNotFoundException anfe) {
                                                                    startActivity (new Intent (Intent.ACTION_VIEW, Uri.parse ("https://play.google.com/store/apps/details?id=" + appPackageName)));
                                                                }
                                                            }
                                                        }).build ();
                                                dialog.show ();
                                            }
                                        }
                                    }
                                } catch (Exception e) {
                                    e.printStackTrace ();
                                }
                            } else {
                                Utils.showLog (Log.WARN, AppConfigTags.SERVER_RESPONSE, AppConfigTags.DIDNT_RECEIVE_ANY_DATA_FROM_SERVER, true);
                            }
                        }
                    },
                    new Response.ErrorListener () {
                        @Override
                        public void onErrorResponse (VolleyError error) {
                            
                            Utils.showLog (Log.ERROR, AppConfigTags.VOLLEY_ERROR, error.toString (), true);
                        }
                    }) {
                
                @Override
                protected Map<String, String> getParams () throws AuthFailureError {
                    Map<String, String> params = new Hashtable<> ();
                    params.put (AppConfigTags.APP_VERSION, String.valueOf (finalPInfo.versionCode));
                    params.put (AppConfigTags.DEVICE_TYPE, "ANDROID");
                    params.put (AppConfigTags.DEVICE_ID, android_id);
                    params.put (AppConfigTags.USER_ID, String.valueOf (userDetailsPref.getIntPref (MainActivity.this, UserDetailsPref.USER_ID)));
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
            strRequest.setRetryPolicy (new DefaultRetryPolicy (DefaultRetryPolicy.DEFAULT_TIMEOUT_MS * 2, DefaultRetryPolicy.DEFAULT_MAX_RETRIES, DefaultRetryPolicy.DEFAULT_BACKOFF_MULT));
            Utils.sendRequest (strRequest, 30);
        } else {
        }
    }
    
    class CustomListener implements View.OnClickListener {
        private final MaterialDialog dialog;
        Activity activity;
        DialogAction dialogAction;
        
        public CustomListener (Activity activity, MaterialDialog dialog, DialogAction dialogAction) {
            this.dialog = dialog;
            this.activity = activity;
            this.dialogAction = dialogAction;
        }
        
        @Override
        public void onClick (View v) {
            if (dialogAction == DialogAction.POSITIVE) {
                final String appPackageName = getPackageName ();
                try {
                    startActivity (new Intent (Intent.ACTION_VIEW, Uri.parse ("market://details?id=" + appPackageName)));
                } catch (android.content.ActivityNotFoundException anfe) {
                    startActivity (new Intent (Intent.ACTION_VIEW, Uri.parse ("https://play.google.com/store/apps/details?id=" + appPackageName)));
                }
            }
            if (dialogAction == DialogAction.NEGATIVE) {
                finish ();
                overridePendingTransition (R.anim.slide_in_left, R.anim.slide_out_right);
                
            }
        }
    }
    
    public void checkPermissions () {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
            if (checkSelfPermission (Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED ||
                    checkSelfPermission (Manifest.permission.RECEIVE_BOOT_COMPLETED) != PackageManager.PERMISSION_GRANTED ||
                    checkSelfPermission (ACCESS_COARSE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
                
                requestPermissions (new String[] {Manifest.permission.ACCESS_FINE_LOCATION,
                                Manifest.permission.ACCESS_COARSE_LOCATION,
                                Manifest.permission.RECEIVE_BOOT_COMPLETED},
                        MainActivity.PERMISSION_REQUEST_CODE);
            }
        }
    }
    
    @Override
    @TargetApi(23)
    public void onRequestPermissionsResult (int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        super.onRequestPermissionsResult (requestCode, permissions, grantResults);
        if (requestCode == PERMISSION_REQUEST_CODE) {
            for (int i = 0, len = permissions.length; i < len; i++) {
                String permission = permissions[i];
                if (grantResults[i] == PackageManager.PERMISSION_DENIED) {
                    boolean showRationale = shouldShowRequestPermissionRationale (permission);
                    if (! showRationale) {
                        AlertDialog.Builder builder = new AlertDialog.Builder (MainActivity.this);
                        builder.setMessage ("Permission are required please enable them on the App Setting page")
                                .setCancelable (false)
                                .setPositiveButton ("OK", new DialogInterface.OnClickListener () {
                                    public void onClick (DialogInterface dialog, int id) {
                                        dialog.dismiss ();
                                        Intent intent = new Intent (Settings.ACTION_APPLICATION_DETAILS_SETTINGS,
                                                Uri.fromParts ("package", getPackageName (), null));
                                        startActivity (intent);
                                    }
                                });
                        AlertDialog alert = builder.create ();
                        alert.show ();
                        // user denied flagging NEVER ASK AGAIN
                        // you can either enable some fall back,
                        // disable features of your app
                        // or open another dialog explaining
                        // again the permission and directing to
                        // the app setting
                    } else if (Manifest.permission.ACCESS_COARSE_LOCATION.equals (permission)) {
//                        Utils.showToast (this, "Camera Permission is required");
//                        showRationale (permission, R.string.permission_denied_contacts);
                        // user denied WITHOUT never ask again
                        // this is a good place to explain the user
                        // why you need the permission and ask if he want
                        // to accept it (the rationale)
                    } else if (Manifest.permission.ACCESS_FINE_LOCATION.equals (permission)) {
//                        Utils.showToast (this, "Location Permission is required");
//                        showRationale (permission, R.string.permission_denied_contacts);
                        // user denied WITHOUT never ask again
                        // this is a good place to explain the user
                        // why you need the permission and ask if he want
                        // to accept it (the rationale)
                    } else if (RECEIVE_BOOT_COMPLETED.equals (permission)) {
//                        Utils.showToast (this, "Write Permission is required");
//                        showRationale (permission, R.string.permission_denied_contacts);
                        // user denied WITHOUT never ask again
                        // this is a good place to explain the user
                        // why you need the permission and ask if he want
                        // to accept it (the rationale)
                    }
                }
            }
            
            
            if (grantResults[0] == PackageManager.PERMISSION_GRANTED) {
            }
        }
    }
}