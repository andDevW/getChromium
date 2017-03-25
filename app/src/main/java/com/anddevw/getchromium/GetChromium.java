package com.anddevw.getchromium;

import android.app.ProgressDialog;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.net.Uri;
import android.os.AsyncTask;
import android.os.Bundle;
import android.support.design.widget.FloatingActionButton;
import android.support.design.widget.Snackbar;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.util.Log;
import android.view.View;
import android.view.WindowManager;
import android.widget.Button;
import android.widget.CompoundButton;
import android.widget.ImageButton;
import android.widget.Switch;
import android.widget.Toast;

import com.anddevw.getchromium.Util.DecompressZip;
import com.anddevw.getchromium.Util.DownloadChromiumApk;
import com.anddevw.getchromium.Util.GetStorage;
import com.android.volley.Cache;
import com.android.volley.Request;
import com.android.volley.RequestQueue;
import com.android.volley.Response;
import com.android.volley.VolleyError;
import com.android.volley.toolbox.BasicNetwork;
import com.android.volley.toolbox.DiskBasedCache;
import com.android.volley.toolbox.HurlStack;
import com.android.volley.toolbox.StringRequest;

import java.io.File;
import java.io.IOException;

import static com.anddevw.getchromium.R.id.fabA;


// Created by andDevW(Andrew Wright) ©2015-2017.
// andrew@andDevW.com


public class GetChromium extends AppCompatActivity {

    //private ShareActionProvider mShareActionProvider;
    private final String urlA = "https://www.chromium.org/getting-involved";
    private final String urlL = "https://commondatastorage.googleapis.com/chromium-browser-snapshots/Android/LAST_CHANGE";

    private static final String PREF_DARK_THEME = "dark_theme";
    private static final String PREFS_NAME = "prefs";

    protected ProgressDialog mProgressDialog;

    public static final String TAG = "getChromium";
    public static final String WIDGET_BUTTON = "com.anddevw.getchromium.WIDGET_BUTTON";

    RequestQueue mRequestQueue;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        SharedPreferences preferences = getSharedPreferences(PREFS_NAME, MODE_PRIVATE);

        // Set dark theme as default.
        boolean useDarkTheme = preferences.getBoolean(PREF_DARK_THEME, true);
        if (useDarkTheme) {
            setTheme(R.style.AppTheme_Dark_NoActionBar);
        }
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);

        FloatingActionButton fab = (FloatingActionButton) findViewById(fabA);
        fab.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {

            // Spread the word about 'chrome://flags'
            Snackbar.make(view, "ENABLE EXPERIMENTAL FEATURES\nIn Chromium, navigate to chrome://flags", 6000)
                        .setAction("Action", null).show();
                runSetup();
                downloadLatest();
                return;
            }
        });

        ImageButton imageButton = (ImageButton) findViewById(R.id.buttonCog);
        imageButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                launchSecuritySettings();
                return;
            }
        });

        Switch toggle = (Switch) findViewById(R.id.switch1);
        toggle.setChecked(useDarkTheme);
        toggle.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(CompoundButton view, boolean isChecked) {
                toggleTheme(isChecked);

            }
        });

        Button buttonA = (Button) findViewById(R.id.button1);
        buttonA.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                openBlogA();
            }
        });
    }

    // The toggle switch changes the app theme from default(dark) to non-default(light).
    private void toggleTheme(boolean darkTheme) {
        SharedPreferences.Editor editor = getSharedPreferences(PREFS_NAME, MODE_PRIVATE).edit();
        editor.putBoolean(PREF_DARK_THEME, darkTheme);
        editor.apply();
        Intent intent = getIntent();
        finish();
        startActivity(intent);
    }

    public void runSetup() {
        // Keep device awake without adding special permissions.
        getWindow().addFlags(WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON);

        isNetworkAvailable();
        isOnline();
    }

    // Check to see if we have an Internet connection.
    private Boolean isNetworkAvailable() {
        ConnectivityManager connectivityManager = (ConnectivityManager)
                getSystemService(Context.CONNECTIVITY_SERVICE);
        NetworkInfo activeNetworkInfo = connectivityManager.getActiveNetworkInfo();
        return activeNetworkInfo != null && activeNetworkInfo.isConnectedOrConnecting();
    }

    // Check for any connection.
    public boolean isOnline() {
        Runtime runtime = Runtime.getRuntime();
        try {
            Process ipProcess = runtime.exec("/system/bin/ping -c 1 8.8.8.8"); // Ping Google Public DNS.
            int exitValue = ipProcess.waitFor();
            return (exitValue == 0);
        } catch (IOException e) {
            e.printStackTrace();
        } catch (InterruptedException e) {
            e.printStackTrace();
        }
        return false;
    }

    // Chromium won't install unless 'Unknown sources' are allowed in the system-level settings.
    private void launchSecuritySettings() {

            // Display Settings > Security > 'Unknown sources'
            Intent launchSettingsIntent = new Intent(android.provider.Settings.ACTION_SECURITY_SETTINGS);
            launchSettingsIntent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
            startActivity(launchSettingsIntent);
          //finish();
        }


    public void downloadLatest() {

        // Instantiate the cache
        Cache cache = new DiskBasedCache(getCacheDir(), 1024 * 1024); // 1MB cap

        // Set up the network to use HttpURLConnection as the HTTP client.
        com.android.volley.Network network = new BasicNetwork(new HurlStack());

        // Instantiate the RequestQueue with the cache and network.
        mRequestQueue = new RequestQueue(cache, network);
        mRequestQueue.start();

        // Request a string response from "https://commondatastorage.googleapis.com/chromium-browser-snapshots/Android/LAST_CHANGE"
        // The response provides the current ID of Chromium's latest build
        final StringRequest stringRequest = new StringRequest(Request.Method.GET, urlL,
                new Response.Listener<String>() {
                    @Override
                    public void onResponse(String response) {

                        // Build new String for the current APK download URL
                        Uri.Builder builder = new Uri.Builder();
                        builder.scheme("https")
                                .authority("commondatastorage.googleapis.com")
                                .appendPath("chromium-browser-snapshots")
                                .appendPath("Android")
                                .appendPath(response) // Build revision number
                                .appendPath("chrome-android.zip"); // Name of the file that will contain the APKs.
                        String apkUrl = builder.build().toString();
                        new DownloadTask().execute(apkUrl);
                    }
                },

                new Response.ErrorListener() {
                    @Override
                    public void onErrorResponse(VolleyError error) {
                    }
                });

        // Add the request to the RequestQueue.
        mRequestQueue.add(stringRequest);
    }

    private class DownloadTask extends AsyncTask<String, Void, Exception> {
        @Override
        protected void onPreExecute() {
            showProgress();
        }

        @Override
        protected Exception doInBackground(String... params) {
            String url = params[0];
            try {
                downloadAllAssets(url);
            } catch (Exception e) {
                return e;
            }
            return null;
        }

        @Override
        protected void onPostExecute(Exception result) {
            dismissProgress();
            if (result == null) {
                return;
            }

            Toast.makeText(GetChromium.this, result.getLocalizedMessage(),
                    Toast.LENGTH_LONG).show();
        }
    }

    protected void showProgress() {
        mProgressDialog = new ProgressDialog(this);
        mProgressDialog.setProgressStyle(ProgressDialog.STYLE_HORIZONTAL);
        mProgressDialog.setTitle(getString(R.string.progress_title));
        mProgressDialog.setMessage(getString(R.string.progress_detail));
        mProgressDialog.setIndeterminate(true);
        mProgressDialog.setCancelable(false);
        mProgressDialog.setProgress(0);
        mProgressDialog.setProgressNumberFormat(null);
        mProgressDialog.setProgressPercentFormat(null);
        mProgressDialog.show();
    }

    protected void dismissProgress() {
        if (mProgressDialog != null && mProgressDialog.
                isShowing() && mProgressDialog.getWindow() != null) {
            try {
                mProgressDialog.dismiss();
            } catch ( IllegalArgumentException ignore ) {
            }
        }

        mProgressDialog = null;
    }

    private void downloadAllAssets( String url ) {
        File zipDir =  GetStorage.getDir(this, "tmp");
        File zipFile = new File( zipDir.getPath() + "/temp.zip" );
        File outputDir = GetStorage.getDir(this, "getChromium");
        try {
            DownloadChromiumApk.download(url, zipFile, zipDir);
            unzipFile( zipFile, outputDir );
        } finally {
            zipFile.delete();
         instChromium();
        }
    }

    private void instChromium() {

        // Install package ChromePublic.apk(Chromium for Android).
        Intent intent = new Intent(Intent.ACTION_INSTALL_PACKAGE);
        intent.setDataAndType(Uri.fromFile(
                new File(String.valueOf(GetStorage.getDir
                        (this, "getChromium/chrome-android/apks/ChromePublic.apk")))),
                "application/vnd.android.package-archive");
        intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
        startActivity(intent);

        // Call to delete 'ContentShell.apk'
        deleteAPk();
    }

    protected void unzipFile( File zipFile, File destination ) {
        DecompressZip decomp = new DecompressZip( zipFile.getPath(),
                destination.getPath() + File.separator );
        decomp.unzip();
        dismissProgress();
    }

    // 'chrome-android.zip' contains two different packages: 'ChromePublic.apk' and 'ContentShell.apk'.
    // We never delete 'ChromePublic.apk' because we don't want to download and then unzip everything again.
    private void deleteAPk() {
        try {
            new File(String.valueOf(GetStorage.getDir
                    (this, "getChromium/chrome-android/apks/ContentShell.apk"))).delete();
        }
        catch (Exception ex) {
            Log.e("tag", ex.getMessage());
        }
    }


    // Link to Chromium Project page 'Getting Involved'.
    public void openBlogA() {
        Uri webpage = Uri.parse(urlA);
        Intent intent = new Intent(Intent.ACTION_VIEW, webpage);
        if (intent.resolveActivity(getPackageManager()) != null) {
            startActivity(intent);
        }
    }

    @Override
    protected void onStop () {
        super.onStop();

        // Cancel all requests(Chromium's latest build is updated CONSTANTLY).
        if (mRequestQueue != null) {
            mRequestQueue.cancelAll(TAG);
        }

    }
}