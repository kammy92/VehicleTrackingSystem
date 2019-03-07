package com.actiknow.tracking.utils;

import android.app.Activity;
import android.app.ActivityManager;
import android.app.ProgressDialog;
import android.content.ActivityNotFoundException;
import android.content.Context;
import android.content.Intent;
import android.content.pm.ApplicationInfo;
import android.content.pm.PackageInfo;
import android.content.pm.PackageManager;
import android.content.pm.ResolveInfo;
import android.content.pm.Signature;
import android.content.res.Resources;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Color;
import android.graphics.Typeface;
import android.graphics.drawable.ColorDrawable;
import android.net.Uri;
import android.provider.Settings;
import android.text.TextUtils;
import android.util.Base64;
import android.util.DisplayMetrics;
import android.util.Log;
import android.view.View;
import android.view.inputmethod.InputMethodManager;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;

import com.actiknow.tracking.R;
import com.actiknow.tracking.app.AppController;
import com.android.volley.DefaultRetryPolicy;
import com.android.volley.toolbox.StringRequest;
import com.google.android.material.snackbar.Snackbar;

import java.io.BufferedReader;
import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.URL;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.text.DateFormat;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.List;
import java.util.Locale;
import java.util.TimeZone;
import java.util.concurrent.TimeUnit;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import androidx.annotation.Nullable;
import androidx.recyclerview.widget.DefaultItemAnimator;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;
import androidx.swiperefreshlayout.widget.SwipeRefreshLayout;

import static android.content.Context.ACTIVITY_SERVICE;

/**
 * Created by Admin on 23-12-2015.
 */
public class Utils {
    public static int isValidEmail (String email) {
        if (email.length () != 0) {
            boolean validMail = isValidEmail2 (email);
            if (validMail)
                return 1;
            else
                return 2;
        } else
            return 0;
    }

    public static boolean isValidEmail2 (CharSequence target) {
        return ! TextUtils.isEmpty (target) && android.util.Patterns.EMAIL_ADDRESS.matcher (target).matches ();
    }

    public static int isValidPassword (String password) {
        if (password.length () > 0) {
            return 1;
        } else {
            return 0;
        }
    }

    public static Bitmap base64ToBitmap (String b64) {
        byte[] imageAsBytes = Base64.decode (b64.getBytes (), Base64.DEFAULT);
        return BitmapFactory.decodeByteArray (imageAsBytes, 0, imageAsBytes.length);
    }

    public static String bitmapToBase64 (Bitmap bmp) {
        if (bmp != null) {
            ByteArrayOutputStream baos = new ByteArrayOutputStream ();
            bmp.compress (Bitmap.CompressFormat.JPEG, 100, baos);
            byte[] imageBytes = baos.toByteArray ();
            String encodedImage = Base64.encodeToString (imageBytes, Base64.DEFAULT);
            return encodedImage;
        } else {
            return "";
        }
    }

    public static String convertTimeFormat (String dateInOriginalFormat, String originalFormat, String requiredFormat) {
        if (dateInOriginalFormat != "null") {
            SimpleDateFormat sdf = new SimpleDateFormat (originalFormat);//yyyy-MM-dd");
            Date testDate = null;
            try {
                testDate = sdf.parse (dateInOriginalFormat);
            } catch (Exception ex) {
                ex.printStackTrace ();
            }
            SimpleDateFormat formatter = new SimpleDateFormat (requiredFormat);
            String newFormat = formatter.format (testDate);
            return newFormat;
        } else {
            return "Unavailable";
        }
    }
    
    
    public static long getDaysBetweenDates (String start, String end) {
        SimpleDateFormat dateFormat = new SimpleDateFormat ("yyyy-MM-dd", Locale.ENGLISH);
        Date startDate, endDate;
        long numberOfDays = 0;
        try {
            startDate = dateFormat.parse (start);
            endDate = dateFormat.parse (end);
            numberOfDays = getUnitBetweenDates (startDate, endDate, TimeUnit.DAYS);
        } catch (ParseException e) {
            e.printStackTrace ();
        }
        return numberOfDays;
    }
    
    private static long getUnitBetweenDates (Date startDate, Date endDate, TimeUnit unit) {
        long timeDiff = endDate.getTime () - startDate.getTime ();
        return unit.convert (timeDiff, TimeUnit.MILLISECONDS);
    }
    
    public static void showSnackBar (Activity activity, View view, String message, int duration, String button_text, View.OnClickListener onClickListener) {
        final Snackbar snackbar = Snackbar.make (view, message, duration);
        snackbar.setAction (button_text, onClickListener);

        View sbView = snackbar.getView ();
        sbView.setBackgroundColor (activity.getResources ().getColor (R.color.primary));
        TextView textView = (TextView) sbView.findViewById (com.google.android.material.R.id.snackbar_text);
        TextView textView2 = (TextView) sbView.findViewById (com.google.android.material.R.id.snackbar_action);
        textView.setTextColor (activity.getResources ().getColor (R.color.text_color_white));
        textView2.setTextColor (activity.getResources ().getColor (R.color.text_color_white));
        textView.setTypeface (SetTypeFace.getTypeface (activity));
        textView2.setTypeface (SetTypeFace.getTypeface (activity));
        snackbar.show ();
    }

    public static void showToast (Activity activity, String message, boolean duration_long) {
        if (duration_long) {
            Toast.makeText (activity, message, Toast.LENGTH_LONG).show ();
        } else {
            Toast.makeText (activity, message, Toast.LENGTH_SHORT).show ();
        }
    }

    public static void setTypefaceToAllViews (Activity activity, View view) {
        Typeface tf = SetTypeFace.getTypeface (activity);
        SetTypeFace.applyTypeface (activity, SetTypeFace.getParentView (view), tf);
    }


    public static void showProgressDialog (Activity activity, ProgressDialog progressDialog, String message, boolean cancelable) {
        // Initialize the progressDialog before calling this function
        TextView tvMessage;
        progressDialog.show ();
        progressDialog.getWindow ().setBackgroundDrawable (new ColorDrawable (Color.TRANSPARENT));
        progressDialog.setContentView (R.layout.progress_dialog);
        tvMessage = (TextView) progressDialog.findViewById (R.id.tvProgressDialogMessage);
        if (message != null) {
            tvMessage.setText (message);
            tvMessage.setVisibility (View.VISIBLE);
            tvMessage.setTypeface (SetTypeFace.getTypeface (activity));
        } else {
            tvMessage.setVisibility (View.GONE);
        }
        progressDialog.setCancelable (cancelable);
    }

    public static void showLog (int log_type, String tag, String message, boolean show_flag) {
        if (Constants.show_log) {
            if (show_flag) {
                switch (log_type) {
                    case Log.DEBUG:
                        Log.d (tag, message);
                        break;
                    case Log.ERROR:
                        Log.e (tag, message);
                        break;
                    case Log.INFO:
                        Log.i (tag, message);
                        break;
                    case Log.VERBOSE:
                        Log.v (tag, message);
                        break;
                    case Log.WARN:
                        Log.w (tag, message);
                        break;
                    case Log.ASSERT:
                        Log.wtf (tag, message);
                        break;
                }
            }
        }
    }

    public static void showErrorInEditText (EditText editText, String message) {
        editText.setError (message);
    }

    public static void hideSoftKeyboard (Activity activity) {
        View view = activity.getCurrentFocus ();
        if (view != null) {
            InputMethodManager imm = (InputMethodManager) activity.getSystemService (Context.INPUT_METHOD_SERVICE);
            imm.hideSoftInputFromWindow (view.getWindowToken (), 0);
        }
    }

    public static boolean isPackageExists (Activity activity, String targetPackage) {
        List<ApplicationInfo> packages;
        PackageManager pm;
        pm = activity.getPackageManager ();
        packages = pm.getInstalledApplications (0);
        for (ApplicationInfo packageInfo : packages) {
            if (packageInfo.packageName.equals (targetPackage))
                return true;
        }
        return false;
    }

    public static void sendRequest (StringRequest strRequest, int timeout_seconds) {
        strRequest.setShouldCache (false);
        AppController.getInstance ().getRequestQueue ().getCache ().clear ();
        int timeout = timeout_seconds * 1000;
        AppController.getInstance ().addToRequestQueue (strRequest);
        strRequest.setRetryPolicy (new DefaultRetryPolicy(timeout,
                DefaultRetryPolicy.DEFAULT_MAX_RETRIES,
                DefaultRetryPolicy.DEFAULT_BACKOFF_MULT));
    }

    public static Bitmap compressBitmap (Bitmap bitmap, Activity activity) {
        int image_quality = 10; // 10
        int max_image_size = 320; // 320

        Bitmap decoded = null;
        try {
            ByteArrayOutputStream out = new ByteArrayOutputStream ();
            if (NetworkConnection.isNetworkAvailable (activity)) {
                bitmap.compress (Bitmap.CompressFormat.JPEG, image_quality, out);
            } else {
                bitmap.compress (Bitmap.CompressFormat.JPEG, image_quality, out);
            }
            decoded = Utils.scaleDown (BitmapFactory.decodeStream (new ByteArrayInputStream (out.toByteArray ())), max_image_size, true);
        } catch (Exception e) {
            e.printStackTrace ();
            Utils.showLog (Log.ERROR, "EXCEPTION", e.getMessage (), true);
        }
        return decoded;
    }

    public static Bitmap scaleDown (Bitmap realImage, float maxImageSize, boolean filter) {
        float ratio = Math.min ((float) maxImageSize / realImage.getWidth (), (float) maxImageSize / realImage.getHeight ());
        int width = Math.round ((float) ratio * realImage.getWidth ());
        int height = Math.round ((float) ratio * realImage.getHeight ());
        Bitmap newBitmap = Bitmap.createScaledBitmap (realImage, width, height, filter);
        return newBitmap;
    }

    public static String getJSONFromAsset (Activity activity, String file_name) {
        String json = null;
        try {
            InputStream is = activity.getAssets ().open (file_name);
            int size = is.available ();
            byte[] buffer = new byte[size];
            is.read (buffer);
            is.close ();
            json = new String (buffer, "UTF-8");
        } catch (IOException ex) {
            ex.printStackTrace ();
            return null;
        }
        return json;
    }
    
    public static float convertPixelsToDp (float px, Context context) {
        Resources resources = context.getResources ();
        DisplayMetrics metrics = resources.getDisplayMetrics ();
        float dp = px / ((float) metrics.densityDpi / DisplayMetrics.DENSITY_DEFAULT);
        return dp;
    }

    public static boolean isEnoughMemory (Activity activity) {
        // Before doing something that requires a lot of memory,
        // check to see whether the device is in a low memory state.
        ActivityManager.MemoryInfo memoryInfo = getAvailableMemory (activity);
        if (! memoryInfo.lowMemory) {
            return true;
            // Do memory intensive work ...
        } else {
            return false;
        }
    }

    private static ActivityManager.MemoryInfo getAvailableMemory (Activity activity) {
        ActivityManager activityManager = (ActivityManager) activity.getSystemService (ACTIVITY_SERVICE);
        ActivityManager.MemoryInfo memoryInfo = new ActivityManager.MemoryInfo ();
        activityManager.getMemoryInfo (memoryInfo);
        return memoryInfo;
    }

    public static String generateMD5 (String s) {
        try {
            // Create MD5 Hash
            MessageDigest digest = MessageDigest.getInstance ("MD5");
            digest.update (s.getBytes ());
            byte messageDigest[] = digest.digest ();

            // Create Hex String
            StringBuffer hexString = new StringBuffer ();
            for (int i = 0; i < messageDigest.length; i++)
                hexString.append (Integer.toHexString (0xFF & messageDigest[i]));
            return hexString.toString ();

        } catch (NoSuchAlgorithmException e) {
            e.printStackTrace ();
        }
        return "";
    }

    public static float dpFromPx (Context context, float px) {
        return px / context.getResources ().getDisplayMetrics ().density;
    }

    public static float pxFromDp (Context context, float dp) {
        return dp * context.getResources ().getDisplayMetrics ().density;
    }

    public static boolean dial (Context c) {
        final Intent i = new Intent (Intent.ACTION_DIAL);
        i.setData (Uri.parse ("tel:" + "9873684678"));//+ c.getString (R.string.intent_number)));
//        final Intent icc = Intent.createChooser (i, c.getString (R.string.intent_call));
        try {
//            c.startActivity (icc);
            return true;
        } catch (ActivityNotFoundException e) {
            return false;
        }
    }

    public static boolean sendMail (Context c) {
        final String mail = "karman.singhh@gmail.com";//c.getString (R.string.intent_mail);
        final Uri u = Uri.fromParts ("mailto", mail, null);
        final Intent i = new Intent (Intent.ACTION_SENDTO, u);
//        final Intent icc = Intent.createChooser (i, c.getString (R.string.intent_mail_tit));
        try {
//            c.startActivity (icc);
            return true;
        } catch (ActivityNotFoundException e) {
            return false;
        }
    }

    public static String getAutoCompleteUrl (String place) {
        // Obtain browser key from https://code.google.com/apis/console
        String key = "key=AIzaSyAxfILlKxFzEN-K5y2hwm4NdvGjKleUa60";
        String inputc = "components=country:in";
        // place to be be searched
        String input = "input=" + place;
        // place type to be searched
        String types = "types=geocode";
        // Sensor enabled
        String sensor = "sensor=false";
        // Building the parameters to the web ic_intro_service
        String parameters = inputc + "&" + input + "&" + types + "&" + sensor + "&" + key;
        // Output format
        String output = "json";
        // Building the url to the web ic_intro_service
        String url = "https://maps.googleapis.com/maps/api/place/autocomplete/" + output + "?" + parameters;
        return url;
    }

    public static String getPlaceDetailsUrl (String ref) {
        // Obtain browser key from https://code.google.com/apis/console
        String key = "key=AIzaSyAxfILlKxFzEN-K5y2hwm4NdvGjKleUa60";
        // reference of place
        String reference = "reference=" + ref;
        // Sensor enabled
        String sensor = "sensor=false";
        // Building the parameters to the web ic_intro_service
        String parameters = reference + "&" + sensor + "&" + key;
        // Output format
        String output = "json";
        // Building the url to the web ic_intro_service
        String url = "https://maps.googleapis.com/maps/api/place/details/" + output + "?" + parameters;
        Log.d ("URL", "" + url);
        return url;
    }

    public static String downloadUrl (String strUrl) throws IOException {
        String data = "";
        InputStream iStream = null;
        HttpURLConnection urlConnection = null;
        try {
            URL url = new URL (strUrl);
            // Creating an http connection to communicate with url
            urlConnection = (HttpURLConnection) url.openConnection ();
            // Connecting to url
            urlConnection.connect ();
            // Reading data from url
            iStream = urlConnection.getInputStream ();
            BufferedReader br = new BufferedReader (new InputStreamReader (iStream));
            StringBuffer sb = new StringBuffer ();
            String line = "";
            while ((line = br.readLine ()) != null) {
                sb.append (line);
            }
            data = sb.toString ();
            br.close ();
        } catch (Exception e) {
            Log.d ("Exception", e.toString ());
        } finally {
            iStream.close ();
            urlConnection.disconnect ();
        }
        return data;
    }

    public static void initAdapter (Activity activity, RecyclerView.Adapter<RecyclerView.ViewHolder> adapter, RecyclerView recyclerView, boolean divider, @Nullable SwipeRefreshLayout swipeRefreshLayout) {
        recyclerView.setAdapter (adapter);
        recyclerView.setHasFixedSize (true);
        recyclerView.setLayoutManager (new LinearLayoutManager (activity));

        if (divider) {

        } else {
            recyclerView.setItemAnimator (new DefaultItemAnimator ());
        }
        if (swipeRefreshLayout != null) {
            swipeRefreshLayout.setColorSchemeResources (R.color.primary);
        }
    }

    public static void turnGPSOn (Activity activity) {
        String provider = Settings.Secure.getString (activity.getContentResolver (), Settings.Secure.LOCATION_PROVIDERS_ALLOWED);

        if (! provider.contains ("gps")) { //if gps is disabled
            final Intent poke = new Intent ();
            poke.setClassName ("com.android.settings", "com.android.settings.widget.SettingsAppWidgetProvider");
            poke.addCategory (Intent.CATEGORY_ALTERNATIVE);
            poke.setData (Uri.parse ("3"));
            activity.sendBroadcast (poke);
        }
    }

    public static void turnGPSOff (Activity activity) {
        String provider = Settings.Secure.getString (activity.getContentResolver (), Settings.Secure.LOCATION_PROVIDERS_ALLOWED);

        if (provider.contains ("gps")) { //if gps is enabled
            final Intent poke = new Intent ();
            poke.setClassName ("com.android.settings", "com.android.settings.widget.SettingsAppWidgetProvider");
            poke.addCategory (Intent.CATEGORY_ALTERNATIVE);
            poke.setData (Uri.parse ("3"));
            activity.sendBroadcast (poke);
        }
    }
    
    
    public static int isValidMobile (String mobile) {
        int number_status = 0;
        String first_char = mobile.substring (0, 1);
        if (mobile.length () == 10 && Integer.parseInt (first_char) > 6) {
            number_status = 1;
        } else if (mobile.length () < 10 && Integer.parseInt (first_char) > 6) {
            number_status = 2;
        } else if (Integer.parseInt (first_char) <= 6 && Integer.parseInt (first_char) > 0 && mobile.length () <= 10 && mobile.length () > 1) {
            number_status = 3;
        } else if (mobile.length () == 0) {
            number_status = 4;
        }
        return number_status;
    }


    public static boolean isValidEmail1 (String emailInput) {
        String EMAIL_PATTERN = "^[_A-Za-z0-9-\\+]+(\\.[_A-Za-z0-9-]+)*@"
                + "[A-Za-z0-9-]+(\\.[A-Za-z0-9]+)*(\\.[A-Za-z]{2,})$";

        Pattern pattern = Pattern.compile (EMAIL_PATTERN);
        Matcher matcher = pattern.matcher (emailInput);
        return matcher.matches ();
    }


    public static void getHashKey (Activity activity) {
        try {
            PackageInfo info = activity.getPackageManager ().getPackageInfo (
                    "com.actiknow.famdent",
                    PackageManager.GET_SIGNATURES);
            for (Signature signature : info.signatures) {
                MessageDigest md = MessageDigest.getInstance ("SHA");
                md.update (signature.toByteArray ());
                Log.d ("KeyHash:", Base64.encodeToString (md.digest (), Base64.DEFAULT));
            }
        } catch (PackageManager.NameNotFoundException e) {
            
        } catch (NoSuchAlgorithmException e) {
            
        }
    }
    
    
    
  /*  public static void startShimmer (ShimmerFrameLayout shimmerFrameLayout) {
        shimmerFrameLayout.useDefaults ();
        if (shimmerFrameLayout.isAnimationStarted ()) {
            shimmerFrameLayout.startShimmerAnimation ();
        }
    }*/
    
  /*  public static void disableShiftMode (BottomNavigationView view) {
        BottomNavigationMenuView menuView = (BottomNavigationMenuView) view.getChildAt (0);
        try {
            Field shiftingMode = menuView.getClass ().getDeclaredField ("mShiftingMode");
            shiftingMode.setAccessible (true);
            shiftingMode.setBoolean (menuView, false);
            shiftingMode.setAccessible (false);
            for (int i = 0; i < menuView.getChildCount (); i++) {
                BottomNavigationItemView item = (BottomNavigationItemView) menuView.getChildAt (i);
                //noinspection RestrictedApi
                item.setShiftingMode (false);
                // set once again checked value, so view will be updated
                //noinspection RestrictedApi
                item.setChecked (item.getItemData ().isChecked ());
            }
        } catch (NoSuchFieldException e) {
            Log.e ("BNVHelper", "Unable to get shift mode field", e);
        } catch (IllegalAccessException e) {
            Log.e ("BNVHelper", "Unable to change value of shift mode", e);
        }
    }*/
    
    public static void shareToGmail (Activity activity, String[] email, String subject, String content) {
        Intent emailIntent = new Intent (Intent.ACTION_SEND);
        emailIntent.putExtra (Intent.EXTRA_EMAIL, email);
        emailIntent.putExtra (Intent.EXTRA_SUBJECT, subject);
        emailIntent.setType ("message/rfc822");
        emailIntent.putExtra (Intent.EXTRA_TEXT, content);
        final PackageManager pm = activity.getPackageManager ();
        final List<ResolveInfo> matches = pm.queryIntentActivities (emailIntent, 0);
        ResolveInfo best = null;
        for (final ResolveInfo info : matches)
            if (info.activityInfo.packageName.endsWith (".gm") || info.activityInfo.name.toLowerCase ().contains ("gmail"))
                best = info;
        if (best != null)
            emailIntent.setClassName (best.activityInfo.packageName, best.activityInfo.name);
        activity.startActivity (emailIntent);
    }
    
    public static void callPhone (Activity activity, String number) {
        Intent sIntent = new Intent (Intent.ACTION_DIAL, Uri.parse ("tel:" + number));
        sIntent.setFlags (Intent.FLAG_ACTIVITY_NEW_TASK);
        activity.startActivity (sIntent);
    }


    public static String dateFormat(String date) {

        DateFormat inputFormat = new SimpleDateFormat ("yyyy-MM-dd");
        DateFormat outputFormat = new SimpleDateFormat ("dd/MM/yyyy");
        String inputDateStr = date;

        Date date1 = null;
        String Date = null;
        try {
            date1 = inputFormat.parse(inputDateStr);
            Date = outputFormat.format(date1);
        } catch (ParseException e) {
            e.printStackTrace();
        }

        return Date;
    }


    public static String dateFormat2(String date) {

        DateFormat inputFormat = new SimpleDateFormat ("dd/MM/yyyy");
        DateFormat outputFormat = new SimpleDateFormat ("yyyy-MM-dd");
        String inputDateStr = date;

        Date date1 = null;
        String Date = null;
        try {
            date1 = inputFormat.parse(inputDateStr);
            Date = outputFormat.format(date1);
        } catch (ParseException e) {
            e.printStackTrace();
        }

        return Date;
    }


    public static List<Date> getDates(String dateString1, String dateString2)
    {
        ArrayList<Date> dates = new ArrayList<Date> ();

        DateFormat df1 = new SimpleDateFormat ("yyyy-MM-dd");
        df1.setTimeZone(TimeZone.getTimeZone("IST"));
        Date date1 = null;
        Date date2 = null;

        try {
            date1 = df1 .parse(dateString1);
            date2 = df1 .parse(dateString2);
        } catch (ParseException e) {
            e.printStackTrace();
        }

        Calendar cal1 = Calendar.getInstance();
        cal1.setTime(date1);


        Calendar cal2 = Calendar.getInstance();
        cal2.setTime(date2);

        while(!cal1.after(cal2))
        {
            dates.add(cal1.getTime());
            cal1.add(Calendar.DATE, 1);
        }
        return dates;
    }
}