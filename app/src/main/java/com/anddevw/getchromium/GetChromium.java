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
import android.support.v7.widget.ShareActionProvider;
import android.support.v7.widget.Toolbar;
import android.util.Log;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
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

    private ShareActionProvider mShareActionProvider;


    private static final String PREFS_NAME = "prefs";
    private static final String PREF_DARK_THEME = "dark_theme";
    public static final String WIDGET_BUTTON = "com.anddevw.getchromium.WIDGET_BUTTON";
    protected ProgressDialog mProgressDialog;
    private final String urlL = "https://commondatastorage.googleapis.com/" +
            "chromium-browser-snapshots/Android/LAST_CHANGE";
    private final String urlA = "https://www.chromium.org/getting-involved";
    public static final String TAG = "getChromium";
    RequestQueue mRequestQueue;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        SharedPreferences preferences = getSharedPreferences(PREFS_NAME, MODE_PRIVATE);

        // Set dark theme as default
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

                //Enable experimental features in Chromium:
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

    private void toggleTheme(boolean darkTheme) {
        SharedPreferences.Editor editor = getSharedPreferences(PREFS_NAME, MODE_PRIVATE).edit();
        editor.putBoolean(PREF_DARK_THEME, darkTheme);
        editor.apply();

        Intent intent = getIntent();
        finish();

        startActivity(intent);
    }

    public void runSetup() {
        // Keep device awake
        getWindow().addFlags(WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON);
        isNetworkAvailable();
        isOnline();
    }

    private Boolean isNetworkAvailable() {
        ConnectivityManager connectivityManager = (ConnectivityManager)
                getSystemService(Context.CONNECTIVITY_SERVICE);
        NetworkInfo activeNetworkInfo = connectivityManager.getActiveNetworkInfo();
        return activeNetworkInfo != null && activeNetworkInfo.isConnectedOrConnecting();
    }

    public boolean isOnline() {
        Runtime runtime = Runtime.getRuntime();
        try {
            Process ipProcess = runtime.exec("/system/bin/ping -c 1 8.8.8.8");
            int exitValue = ipProcess.waitFor();
            return (exitValue == 0);
        } catch (IOException e) {
            e.printStackTrace();
        } catch (InterruptedException e) {
            e.printStackTrace();
        }
        return false;
    }

    private void launchSecuritySettings() {
            Intent launchSettingsIntent = new Intent(android.provider.Settings.ACTION_SECURITY_SETTINGS);
            launchSettingsIntent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
            startActivity(launchSettingsIntent);
          //finish();
        }

    public void downloadLatest() {
        Cache cache = new DiskBasedCache(getCacheDir(), 1024 * 1024);
        com.android.volley.Network network = new BasicNetwork(new HurlStack());
        mRequestQueue = new RequestQueue(cache, network);
        mRequestQueue.start();

        final StringRequest stringRequest = new StringRequest(Request.Method.GET, urlL,
                new Response.Listener<String>() {
                    @Override
                    public void onResponse(String response) {
                        Uri.Builder builder = new Uri.Builder();
                        builder.scheme("https")
                                .authority("commondatastorage.googleapis.com")
                                .appendPath("chromium-browser-snapshots")
                                .appendPath("Android")
                                .appendPath(response)
                                .appendPath("chrome-android.zip");
                        String apkUrl = builder.build().toString();
                        new DownloadTask().execute(apkUrl);
                    }
                },

                new Response.ErrorListener() {
                    @Override
                    public void onErrorResponse(VolleyError error) {
                    }
                });

        mRequestQueue.add(stringRequest);
    }

    private class DownloadTask extends AsyncTask<String, Integer, Exception> {

        @Override
        protected void onPreExecute() {
            showProgress();
        }

        @Override
        protected Exception doInBackground(String... params) {
            String url = params[0];
            try {
                downloadAllAssets(url, new IDownloadProgress() {
                    @Override
                    public void onProgress(int done, int total) {
                        publishProgress(done, total);
                    }
                });
            } catch (Exception e) {
                return e;
            }
            return null;
        }

        @Override
        protected void onProgressUpdate(Integer... values) {
            updateProgress(values[0], values[1]);
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
        mProgressDialog.setIndeterminate(false);
        mProgressDialog.setCancelable(false);
        mProgressDialog.setProgress(0);
        mProgressDialog.setProgressNumberFormat(null);
        mProgressDialog.setProgressPercentFormat(null);
        mProgressDialog.show();
    }

    protected void updateProgress(int done, int total) {
        mProgressDialog.setMax(total);
        mProgressDialog.setProgress(done);
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

    private void downloadAllAssets( String url, IDownloadProgress downloadProgress ) {
        File zipDir =  GetStorage.getDir(this, "tmp");
        File zipFile = new File( zipDir.getPath() + "/temp.zip" );
        File outputDir = GetStorage.getDir(this, "getChromium");
        try {
            DownloadChromiumApk.download(url, zipFile, zipDir, downloadProgress);
            unzipFile( zipFile, outputDir, downloadProgress );
        } finally {
            zipFile.delete();
         instChromium();
        }
    }

    private void instChromium() {
        Intent intent = new Intent(Intent.ACTION_INSTALL_PACKAGE);
        intent.setDataAndType(Uri.fromFile(
                new File(String.valueOf(GetStorage.getDir
                        (this, "getChromium/chrome-android/apks/ChromePublic.apk")))),
                "application/vnd.android.package-archive");
        intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
        startActivity(intent);
        deleteAPk();
    }

    protected void unzipFile( File zipFile, File destination, IDownloadProgress downloadProgress ) {
        DecompressZip decomp = new DecompressZip( zipFile.getPath(),
                destination.getPath() + File.separator );
        decomp.unzip(downloadProgress);
        dismissProgress();
    }

    private void deleteAPk() {
        try {
            new File(String.valueOf(GetStorage.getDir
                    (this, "getChromium/chrome-android/apks/ContentShell.apk"))).delete();
        }
        catch (Exception ex) {
            Log.e("tag", ex.getMessage());
        }
    }


    // Link to Chromium Project 'Getting Involved'.
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
        if (mRequestQueue != null) {
            mRequestQueue.cancelAll(TAG);
        }

    }
}