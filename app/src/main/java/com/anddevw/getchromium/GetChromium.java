package com.anddevw.getchromium;

import android.content.SharedPreferences;
import android.content.pm.PackageManager;
import android.os.Environment;
import android.support.design.widget.Snackbar;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;

import android.app.ProgressDialog;
import android.content.Context;
import android.content.Intent;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.net.Uri;
import android.os.AsyncTask;
import android.support.design.widget.FloatingActionButton;
import android.support.v7.widget.Toolbar;
import android.view.View;
import android.view.WindowManager;
import android.widget.Button;
import android.widget.CompoundButton;
import android.widget.ImageButton;
import android.widget.RadioButton;
import android.widget.RadioGroup;
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

public class   GetChromium extends AppCompatActivity {
    private static final String PREFS_NAME = "prefs";
    private static final String PREF_DARK_THEME = "dark_theme";
    private String url = "https://commondatastorage.googleapis.com/" +
            "chromium-browser-snapshots/Android/LAST_CHANGE";
    private String urlC = "https://commondatastorage.googleapis.com/" +
            "chromium-browser-continuous/Android/LAST_CHANGE";
    protected ProgressDialog mProgressDialog;
    RequestQueue mRequestQueue;
    public static final String TAG = "MyTag";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        SharedPreferences preferences = getSharedPreferences(PREFS_NAME, MODE_PRIVATE);
        boolean useDarkTheme = preferences.getBoolean(PREF_DARK_THEME, false);

        if (useDarkTheme) {
            setTheme(R.style.AppTheme_Dark_NoActionBar);
        }
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);

        FloatingActionButton fabA = (FloatingActionButton) findViewById(R.id.fabA);
        fabA.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                runSetup();
                downloadGoodRevision();
                return;
            }
        });


        FloatingActionButton fabB = (FloatingActionButton) findViewById(R.id.fabB);
        fabB.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                runSetup();
                downloadLastChange();
                return;
            }
        });
            fabB.setOnLongClickListener(new View.OnLongClickListener() {
                @Override
                public boolean onLongClick(View view) {
                    instContent();
                    return true;
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

        }

    public void runSetup() {
        getWindow().addFlags(WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON);
        isNetworkAvailable();
        isOnline();
        isNetworkAvailable();
    }

    private void toggleTheme(boolean darkTheme) {
        SharedPreferences.Editor editor = getSharedPreferences(PREFS_NAME, MODE_PRIVATE).edit();
        editor.putBoolean(PREF_DARK_THEME, darkTheme);
        editor.apply();

        Intent intent = getIntent();
        finish();

        startActivity(intent);
    }


    private Boolean isNetworkAvailable() {
        ConnectivityManager connectivityManager
                = (ConnectivityManager) getSystemService(Context.CONNECTIVITY_SERVICE);
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

    public void downloadLastChange() {

        Cache cache = new DiskBasedCache(getCacheDir(), 1024 * 1024);
        com.android.volley.Network network = new BasicNetwork(new HurlStack());
        mRequestQueue = new RequestQueue(cache, network);
        mRequestQueue.start();

        final StringRequest stringRequest = new StringRequest(Request.Method.GET, url,
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
                        Toast.makeText(getBaseContext(),
                                "CHECK NETWORK", Toast.LENGTH_LONG);
                    }
                });

        mRequestQueue.add(stringRequest);
    }

    public void downloadGoodRevision() {

        Cache cache1 = new DiskBasedCache(getCacheDir(), 1024 * 1024);
        com.android.volley.Network network = new BasicNetwork(new HurlStack());
        mRequestQueue = new RequestQueue(cache1, network);
        mRequestQueue.start();

        final StringRequest stringRequest = new StringRequest(Request.Method.GET, urlC,
                new Response.Listener<String>() {
                    @Override
                    public void onResponse(String response) {
                        Uri.Builder builder = new Uri.Builder();
                        builder.scheme("https")
                                .authority("commondatastorage.googleapis.com")
                                .appendPath("chromium-browser-continuous")
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
                        Toast.makeText(getBaseContext(),
                                "CHECK NETWORK", Toast.LENGTH_LONG);
                    }
                });

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
        mProgressDialog.setTitle(getString(R.string.title_get_cr));
        mProgressDialog.setMessage(getString(R.string.progress_detail));
        mProgressDialog.setIndeterminate(true);
        mProgressDialog.setCancelable(false);
        mProgressDialog.setProgress(0);
        mProgressDialog.setProgressNumberFormat(null);
        mProgressDialog.setProgressPercentFormat(null);
        mProgressDialog.show();
    }

    protected void dismissProgress() {
        // You can't be too careful.
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
        Intent intent = new Intent(Intent.ACTION_INSTALL_PACKAGE);
        intent.setDataAndType(Uri.fromFile(
                new File(String.valueOf(GetStorage.getDir
                        (this, "getChromium/chrome-android/apks/ChromePublic.apk")))),
                                        "application/vnd.android.package-archive");
        intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
        startActivity(intent);
    }

    private void instContent() {
        Intent intent = new Intent(Intent.ACTION_INSTALL_PACKAGE);
        intent.setDataAndType(Uri.fromFile(
                new File(String.valueOf(GetStorage.getDir
                        (this, "getChromium/chrome-android/apks/ContentShell.apk")))),
                                        "application/vnd.android.package-archive");
        intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
        startActivity(intent);
    }
    protected void unzipFile( File zipFile, File destination ) {
        DecompressZip decomp = new DecompressZip( zipFile.getPath(),
                destination.getPath() + File.separator );
        decomp.unzip();
    }

    @Override
    protected void onStop () {
        super.onStop();
        if (mRequestQueue != null) {
            mRequestQueue.cancelAll(TAG);
        }
    }
}
