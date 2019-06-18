package com.actiknow.tracking.activity;

import android.app.Activity;
import android.app.ProgressDialog;
import android.content.Intent;
import android.os.Bundle;
import android.provider.Settings;
import android.text.Editable;
import android.text.InputType;
import android.text.Spannable;
import android.text.SpannableString;
import android.text.TextWatcher;
import android.util.Log;
import android.view.View;
import android.widget.EditText;
import android.widget.TextView;

import com.actiknow.tracking.R;
import com.actiknow.tracking.utils.AppConfigTags;
import com.actiknow.tracking.utils.AppConfigURL;
import com.actiknow.tracking.utils.AppDetailsPref;
import com.actiknow.tracking.utils.Constants;
import com.actiknow.tracking.utils.NetworkConnection;
import com.actiknow.tracking.utils.SetTypeFace;
import com.actiknow.tracking.utils.TypefaceSpan;
import com.actiknow.tracking.utils.UserDetailsPref;
import com.actiknow.tracking.utils.Utils;
import com.afollestad.materialdialogs.DialogAction;
import com.afollestad.materialdialogs.MaterialDialog;
import com.android.volley.AuthFailureError;
import com.android.volley.NetworkResponse;
import com.android.volley.Request;
import com.android.volley.VolleyError;
import com.android.volley.toolbox.StringRequest;
import com.google.android.material.snackbar.Snackbar;

import org.json.JSONObject;

import java.util.HashMap;
import java.util.Hashtable;
import java.util.Map;

import androidx.appcompat.app.AppCompatActivity;
import androidx.coordinatorlayout.widget.CoordinatorLayout;

public class LoginActivity extends AppCompatActivity {
    EditText etUserName;
    EditText etPassword;
    TextView tvLogin;
    TextView tvShowHide;
    ProgressDialog progressDialog;
    CoordinatorLayout clMain;
    UserDetailsPref userDetailsPref;
    AppDetailsPref appDetailsPref;
    TextView tvForgotPassword;
    
    @Override
    protected void onCreate (Bundle savedInstanceState) {
        super.onCreate (savedInstanceState);
        setContentView (R.layout.activity_login);
        initView ();
        initData ();
        initListener ();
    }
    
    private void initData () {
        Utils.setTypefaceToAllViews (this, clMain);
        userDetailsPref = UserDetailsPref.getInstance ();
        appDetailsPref = AppDetailsPref.getInstance ();
        progressDialog = new ProgressDialog (this);
    }
    
    private void initView () {
        clMain = (CoordinatorLayout) findViewById (R.id.clMain);
        tvLogin = (TextView) findViewById (R.id.tvLogin);
        tvForgotPassword = (TextView) findViewById (R.id.tvForgotPassword);
        etUserName = (EditText) findViewById (R.id.etUserName);
        etPassword = (EditText) findViewById (R.id.etPassword);
        tvShowHide = (TextView) findViewById (R.id.tvShowHide);
    }
    
    private void initListener () {
        tvLogin.setOnClickListener (new View.OnClickListener () {
            @Override
            public void onClick (View view) {
                SpannableString s1 = new SpannableString (getResources ().getString (R.string.please_enter_email));
                s1.setSpan (new TypefaceSpan (LoginActivity.this, Constants.font_name), 0, s1.length (), Spannable.SPAN_EXCLUSIVE_EXCLUSIVE);
                SpannableString s2 = new SpannableString (getResources ().getString (R.string.please_enter_password));
                s2.setSpan (new TypefaceSpan (LoginActivity.this, Constants.font_name), 0, s2.length (), Spannable.SPAN_EXCLUSIVE_EXCLUSIVE);
                
                if (etUserName.getText ().toString ().length () == 0) {
                    etUserName.setError (s1);
                }
                if (etPassword.getText ().toString ().length () == 0) {
                    etPassword.setError (s2);
                }
                
                if ((etUserName.getText ().toString ().length () != 0) && (etPassword.getText ().toString ().length () != 0)) {
                    sendLoginDetailsToServer (etUserName.getText ().toString (), etPassword.getText ().toString ());
                }
            }
        });
        
        tvShowHide.setOnClickListener (new View.OnClickListener () {
            @Override
            public void onClick (View v) {
                if (tvShowHide.getText ().toString ().equalsIgnoreCase ("SHOW")) {
                    tvShowHide.setText ("HIDE");
                    etPassword.setInputType (InputType.TYPE_TEXT_VARIATION_VISIBLE_PASSWORD);
                    etPassword.setSelection (etPassword.getText ().length ());
                    etPassword.setTypeface (SetTypeFace.getTypeface (LoginActivity.this));
                } else {
                    etPassword.setInputType (InputType.TYPE_CLASS_TEXT | InputType.TYPE_TEXT_VARIATION_PASSWORD);
                    etPassword.setSelection (etPassword.getText ().length ());
                    etPassword.setTypeface (SetTypeFace.getTypeface (LoginActivity.this));
                    tvShowHide.setText ("SHOW");
                }
            }
        });
        etUserName.addTextChangedListener (new TextWatcher () {
            @Override
            public void onTextChanged (CharSequence s, int start, int before, int count) {
                if (count == 0) {
                    etUserName.setError (null);
                }
            }
            
            @Override
            public void beforeTextChanged (CharSequence s, int start, int count, int after) {
            }
            
            @Override
            public void afterTextChanged (Editable s) {
            }
        });
        etPassword.addTextChangedListener (new TextWatcher () {
            @Override
            public void onTextChanged (CharSequence s, int start, int before, int count) {
                if (count == 0) {
                    etPassword.setError (null);
                }
            }
            
            @Override
            public void beforeTextChanged (CharSequence s, int start, int count, int after) {
            }
            
            @Override
            public void afterTextChanged (Editable s) {
            }
        });
        tvForgotPassword.setOnClickListener (new View.OnClickListener () {
            @Override
            public void onClick (View view) {
                showForgotPasswordDialog ();
            }
        });
    }
    
    @Override
    public void onBackPressed () {
        super.onBackPressed ();
        finish ();
        overridePendingTransition (R.anim.slide_in_right, R.anim.slide_out_left);
    }
    
    private void showForgotPasswordDialog () {
        final MaterialDialog.Builder mBuilder = new MaterialDialog.Builder (LoginActivity.this)
                .content ("Enter your Email Address")
                .contentColor (getResources ().getColor (R.color.primary_text))
                .positiveColor (getResources ().getColor (R.color.primary_text))
                .negativeColor (getResources ().getColor (R.color.primary_text))
                .inputType (InputType.TYPE_CLASS_TEXT | InputType.TYPE_TEXT_VARIATION_EMAIL_ADDRESS)
                .typeface (SetTypeFace.getTypeface (LoginActivity.this), SetTypeFace.getTypeface (LoginActivity.this))
                .alwaysCallInputCallback ()
                .canceledOnTouchOutside (true)
                .cancelable (true)
                .negativeText (getResources ().getString (R.string.dialog_action_cancel))
                .positiveText (getResources ().getString (R.string.dialog_action_ok));
        
        mBuilder.input (null, null, new MaterialDialog.InputCallback () {
            @Override
            public void onInput (MaterialDialog dialog, CharSequence input) {
                if (input.toString ().length () > 0) {
                    dialog.getInputEditText ().setError (null);
                    dialog.getActionButton (DialogAction.POSITIVE).setEnabled (true);
                } else {
                    dialog.getActionButton (DialogAction.POSITIVE).setEnabled (false);
                }
            }
        });
        
        MaterialDialog dialog = mBuilder.build ();
        dialog.getActionButton (DialogAction.POSITIVE).setOnClickListener (new CustomListener (LoginActivity.this, dialog, DialogAction.POSITIVE));
        dialog.getActionButton (DialogAction.POSITIVE).setEnabled (false);
        dialog.show ();
    }
    
    private void sendForgotPasswordRequestToServer (final String login_username) {
        if (NetworkConnection.isNetworkAvailable (LoginActivity.this)) {
            Utils.showProgressDialog (LoginActivity.this, progressDialog, getResources ().getString (R.string.progress_dialog_text_please_wait), true);
            Utils.showLog (Log.INFO, "" + AppConfigTags.URL, AppConfigURL.FORGOT_PASSWORD, true);
            StringRequest strRequest1 = new StringRequest (Request.Method.POST, AppConfigURL.FORGOT_PASSWORD,
                    new com.android.volley.Response.Listener<String> () {
                        @Override
                        public void onResponse (String response) {
                            Utils.showLog (Log.INFO, AppConfigTags.SERVER_RESPONSE, response, true);
                            if (response != null) {
                                try {
                                    JSONObject jsonObj = new JSONObject (response);
                                    boolean error = jsonObj.getBoolean (AppConfigTags.ERROR);
                                    String message = jsonObj.getString (AppConfigTags.MESSAGE);
                                    if (! error) {
                                        Utils.showSnackBar (LoginActivity.this, clMain, message, Snackbar.LENGTH_LONG, null, null);
                                    } else {
                                        Utils.showSnackBar (LoginActivity.this, clMain, message, Snackbar.LENGTH_LONG, null, null);
                                    }
                                    progressDialog.dismiss ();
                                } catch (Exception e) {
                                    progressDialog.dismiss ();
                                    Utils.showSnackBar (LoginActivity.this, clMain, getResources ().getString (R.string.snackbar_text_exception_occurred), Snackbar.LENGTH_LONG, getResources ().getString (R.string.snackbar_action_dismiss), null);
                                    e.printStackTrace ();
                                }
                            } else {
                                Utils.showSnackBar (LoginActivity.this, clMain, getResources ().getString (R.string.snackbar_text_error_occurred), Snackbar.LENGTH_LONG, getResources ().getString (R.string.snackbar_action_dismiss), null);
                                Utils.showLog (Log.WARN, AppConfigTags.SERVER_RESPONSE, AppConfigTags.DIDNT_RECEIVE_ANY_DATA_FROM_SERVER, true);
                            }
                            progressDialog.dismiss ();
                        }
                    },
                    new com.android.volley.Response.ErrorListener () {
                        @Override
                        public void onErrorResponse (VolleyError error) {
                            progressDialog.dismiss ();
                            Utils.showLog (Log.ERROR, AppConfigTags.VOLLEY_ERROR, error.toString (), true);
                            Utils.showSnackBar (LoginActivity.this, clMain, getResources ().getString (R.string.snackbar_text_error_occurred), Snackbar.LENGTH_LONG, getResources ().getString (R.string.snackbar_action_dismiss), null);
                        }
                    }) {
                @Override
                protected Map<String, String> getParams () throws AuthFailureError {
                    Map<String, String> params = new Hashtable<String, String> ();
                    params.put (AppConfigTags.LOGIN_USERNAME, login_username);
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
            Utils.sendRequest (strRequest1, 60);
        } else {
            Utils.showSnackBar (LoginActivity.this, clMain, getResources ().getString (R.string.snackbar_text_no_internet_connection_available), Snackbar.LENGTH_LONG, getResources ().getString (R.string.snackbar_action_go_to_settings), new View.OnClickListener () {
                @Override
                public void onClick (View v) {
                    Intent dialogIntent = new Intent (Settings.ACTION_SETTINGS);
                    dialogIntent.addFlags (Intent.FLAG_ACTIVITY_NEW_TASK);
                    startActivity (dialogIntent);
                }
            });
        }
    }
    
    private void sendLoginDetailsToServer (final String username, final String password) {
        if (NetworkConnection.isNetworkAvailable (LoginActivity.this)) {
            Utils.showProgressDialog (LoginActivity.this, progressDialog, getResources ().getString (R.string.progress_dialog_text_please_wait), true);
            Utils.showLog (Log.INFO, "" + AppConfigTags.URL, AppConfigURL.LOGIN, true);
            StringRequest strRequest1 = new StringRequest (Request.Method.POST, AppConfigURL.LOGIN,
                    new com.android.volley.Response.Listener<String> () {
                        @Override
                        public void onResponse (String response) {
                            Utils.showLog (Log.INFO, AppConfigTags.SERVER_RESPONSE, response, true);
                            if (response != null) {
                                try {
                                    JSONObject jsonObj = new JSONObject (response);
                                    boolean error = jsonObj.getBoolean (AppConfigTags.ERROR);
                                    String message = jsonObj.getString (AppConfigTags.MESSAGE);
                                    if (! error) {
                                        userDetailsPref.putIntPref (LoginActivity.this, UserDetailsPref.USER_ID, jsonObj.getInt (AppConfigTags.USER_ID));
                                        userDetailsPref.putStringPref (LoginActivity.this, UserDetailsPref.USER_NAME, jsonObj.getString (AppConfigTags.USER_NAME));
                                        userDetailsPref.putStringPref (LoginActivity.this, UserDetailsPref.USER_MOBILE, jsonObj.getString (AppConfigTags.USER_MOBILE));
                                        userDetailsPref.putStringPref (LoginActivity.this, UserDetailsPref.USER_EMAIL, jsonObj.getString (AppConfigTags.USER_EMAIL));
                                        userDetailsPref.putStringPref (LoginActivity.this, UserDetailsPref.USER_CONSTITUENCY, jsonObj.getString (AppConfigTags.USER_CONSTITUENCY));
                                        userDetailsPref.putStringPref (LoginActivity.this, UserDetailsPref.USER_VEHICLE_NUMBER, jsonObj.getString (AppConfigTags.USER_VEHICLE_NUMBER));
                                        appDetailsPref.putStringPref (LoginActivity.this, AppDetailsPref.LOGGING_START_TIME, jsonObj.getString (AppConfigTags.LOGGING_START_TIME));
                                        appDetailsPref.putStringPref (LoginActivity.this, AppDetailsPref.LOGGING_END_TIME, jsonObj.getString (AppConfigTags.LOGGING_END_TIME));
                                        appDetailsPref.putStringPref (LoginActivity.this, AppDetailsPref.LOGGING_INTERVAL, jsonObj.getString (AppConfigTags.LOGGING_INTERVAL));
                                        Intent intent = new Intent (LoginActivity.this, MainActivity.class);
                                        startActivity (intent);
                                        finish ();
                                        overridePendingTransition (R.anim.slide_in_right, R.anim.slide_out_left);
                                    } else {
                                        Utils.showSnackBar (LoginActivity.this, clMain, message, Snackbar.LENGTH_LONG, null, null);
                                    }
                                    progressDialog.dismiss ();
                                } catch (Exception e) {
                                    progressDialog.dismiss ();
                                    Utils.showSnackBar (LoginActivity.this, clMain, getResources ().getString (R.string.snackbar_text_exception_occurred), Snackbar.LENGTH_LONG, getResources ().getString (R.string.snackbar_action_dismiss), null);
                                    e.printStackTrace ();
                                }
                            } else {
                                Utils.showSnackBar (LoginActivity.this, clMain, getResources ().getString (R.string.snackbar_text_error_occurred), Snackbar.LENGTH_LONG, getResources ().getString (R.string.snackbar_action_dismiss), null);
                                Utils.showLog (Log.WARN, AppConfigTags.SERVER_RESPONSE, AppConfigTags.DIDNT_RECEIVE_ANY_DATA_FROM_SERVER, true);
                            }
                            progressDialog.dismiss ();
                        }
                    },
                    new com.android.volley.Response.ErrorListener () {
                        @Override
                        public void onErrorResponse (VolleyError error) {
                            Utils.showLog (Log.ERROR, AppConfigTags.VOLLEY_ERROR, error.toString (), true);
                            NetworkResponse response = error.networkResponse;
                            if (response != null && response.data != null) {
                                Utils.showLog (Log.ERROR, AppConfigTags.ERROR, new String (response.data), true);
                            }
                            Utils.showSnackBar (LoginActivity.this, clMain, getResources ().getString (R.string.snackbar_text_error_occurred), Snackbar.LENGTH_LONG, getResources ().getString (R.string.snackbar_action_dismiss), null);
                            progressDialog.dismiss ();
                        }
                    }) {
                @Override
                protected Map<String, String> getParams () throws AuthFailureError {
                    Map<String, String> params = new Hashtable<String, String> ();
                    params.put (AppConfigTags.LOGIN_USERNAME, username);
                    params.put (AppConfigTags.LOGIN_PASSWORD, password);
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
            Utils.sendRequest (strRequest1, 60);
        } else {
            Utils.showSnackBar (this, clMain, getResources ().getString (R.string.snackbar_text_no_internet_connection_available), Snackbar.LENGTH_LONG, getResources ().getString (R.string.snackbar_action_go_to_settings), new View.OnClickListener () {
                @Override
                public void onClick (View v) {
                    Intent dialogIntent = new Intent (Settings.ACTION_SETTINGS);
                    dialogIntent.addFlags (Intent.FLAG_ACTIVITY_NEW_TASK);
                    startActivity (dialogIntent);
                }
            });
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
            if (dialogAction == DialogAction.NEGATIVE) {
                dialog.dismiss ();
            } else if (dialogAction == DialogAction.POSITIVE) {
                if (dialog.getInputEditText ().getText ().toString ().length () > 0 && Utils.isValidEmail1 (dialog.getInputEditText ().getText ().toString ())) {
                    dialog.dismiss ();
                    sendForgotPasswordRequestToServer (dialog.getInputEditText ().getText ().toString ());
                } else {
                    dialog.getInputEditText ().setError ("Invalid Email");
                }
            }
        }
    }
}