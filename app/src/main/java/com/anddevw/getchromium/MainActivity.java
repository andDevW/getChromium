package com.anddevw.getchromium;

import android.app.ProgressDialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.net.Uri;
import android.os.AsyncTask;
import android.os.Build;
import android.os.Bundle;
import android.support.v4.content.ContextCompat;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import android.view.View;
import android.view.WindowManager;
import android.widget.Toast;

import com.anddevw.getchromium.util.DownloadUtil;
import com.anddevw.getchromium.util.NetworkUtil;
import com.anddevw.getchromium.util.StorageUtil;
import com.anddevw.getchromium.util.ZipUtil;
import com.android.volley.Cache;
import com.android.volley.Network;
import com.android.volley.Request;
import com.android.volley.RequestQueue;
import com.android.volley.Response;
import com.android.volley.VolleyError;
import com.android.volley.toolbox.BasicNetwork;
import com.android.volley.toolbox.DiskBasedCache;
import com.android.volley.toolbox.HurlStack;
import com.android.volley.toolbox.StringRequest;

import java.io.File;

// Created by Andrew Wright (andDevW) ©2015-2017.
// andDevW@gmail.com

public class MainActivity extends AppCompatActivity {

    private static final String TAG = "getChromium";

    private ProgressDialog mProgressDialog;
    private RequestQueue mRequestQueue;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
            getWindow().setStatusBarColor(ContextCompat.getColor(this, R.color.backgroundColor));
            getWindow().setNavigationBarColor(ContextCompat.getColor(this, R.color.backgroundColor));
        }

        findViewById(R.id.download_fab).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                getWindow().addFlags(WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON);

                if (NetworkUtil.isNetworkAvailable(getApplicationContext())) {
                    downloadLatest();
                } else {
                    Toast.makeText(MainActivity.this, R.string.device_is_not_online, Toast.LENGTH_SHORT).show();
                }
            }
        });

        findViewById(R.id.unknown_sources_settings_image_view).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                launchSecuritySettings();
            }
        });

        findViewById(R.id.get_involved_button).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                openBlog();
            }
        });
    }

    private void launchSecuritySettings() {
        Intent launchSettingsIntent = new Intent(android.provider.Settings.ACTION_SECURITY_SETTINGS);
        launchSettingsIntent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
        startActivity(launchSettingsIntent);
    }

    private void downloadLatest() {
        // Instantiate the cache
        Cache cache = new DiskBasedCache(getCacheDir(), 1024 * 1024); // 1MB cap

        // Set up the network to use HttpURLConnection as the HTTP client.
        Network network = new BasicNetwork(new HurlStack());

        // Instantiate the RequestQueue with the cache and network.
        mRequestQueue = new RequestQueue(cache, network);
        mRequestQueue.start();

        // Request a string response from "https://commondatastorage.googleapis.com/chromium-browser-snapshots/Android/LAST_CHANGE"
        // The response provides the current build number of Chromium's latest build
        String urlL = "https://commondatastorage.googleapis.com/chromium-browser-snapshots/Android/LAST_CHANGE";
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

    private void showProgress() {
        mProgressDialog = new ProgressDialog(this);
        mProgressDialog.setProgressStyle(ProgressDialog.STYLE_HORIZONTAL);
        mProgressDialog.setTitle(getString(R.string.please_wait));
        mProgressDialog.setMessage(getString(R.string.downloading_chromium_apk));
        mProgressDialog.setIndeterminate(true);
        mProgressDialog.setProgress(0);
        mProgressDialog.setProgressNumberFormat(null);
        mProgressDialog.setProgressPercentFormat(null);
        mProgressDialog.setOnDismissListener(new DialogInterface.OnDismissListener() {
            @Override
            public void onDismiss(DialogInterface dialog) {
                if (mRequestQueue != null) {
                    mRequestQueue.cancelAll(TAG);
                }
            }
        });
        mProgressDialog.show();
    }

    private void dismissProgress() {
        if (mProgressDialog != null && mProgressDialog.
                isShowing() && mProgressDialog.getWindow() != null) {
            try {
                mProgressDialog.dismiss();
            } catch (IllegalArgumentException ignore) {
            }
        }

        mProgressDialog = null;
    }

    private void downloadAllAssets(String url) {
        File zipDir = StorageUtil.getDir(this, "tmp");
        File zipFile = new File(zipDir.getPath() + "/temp.zip");
        File outputDir = StorageUtil.getDir(this, "getChromium");
        try {
            DownloadUtil.download(url, zipFile, zipDir);
            unzipFile(zipFile, outputDir);
        } finally {
            zipFile.delete();
            installChromium();
        }
    }

    private void installChromium() {
        // Install package ChromePublic.apk(Chromium for Android).
        Intent intent = new Intent(Intent.ACTION_INSTALL_PACKAGE);
        intent.setDataAndType(Uri.fromFile(
                new File(String.valueOf(StorageUtil.getDir
                        (this, "getChromium/chrome-android/apks/ChromePublic.apk")))),
                "application/vnd.android.package-archive");
        intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
        startActivity(intent);

        // Call to delete 'ContentShell.apk'
        deleteApk();
    }

    private void unzipFile(File zipFile, File destination) {
        ZipUtil decomp = new ZipUtil(zipFile.getPath(),
                destination.getPath() + File.separator);
        decomp.unzip();
        dismissProgress();
    }

    // 'chrome-android.zip' contains two different packages: 'ChromePublic.apk' and 'ContentShell.apk'.
    // We never delete 'ChromePublic.apk' because we don't want to download and then unzip everything again.
    private void deleteApk() {
        try {
            new File(String.valueOf(StorageUtil.getDir
                    (this, "getChromium/chrome-android/apks/ContentShell.apk"))).delete();
        } catch (Exception ex) {
            Log.e("tag", ex.getMessage());
        }
    }

    // Link to Chromium Project page 'Getting Involved'.
    private void openBlog() {
        String urlA = "https://www.chromium.org/getting-involved";
        Uri webpage = Uri.parse(urlA);
        Intent intent = new Intent(Intent.ACTION_VIEW, webpage);
        if (intent.resolveActivity(getPackageManager()) != null) {
            startActivity(intent);
        }
    }

    @Override
    protected void onStop() {
        super.onStop();

        // Cancel all requests(Chromium's latest build is updated CONSTANTLY).
        if (mRequestQueue != null) {
            mRequestQueue.cancelAll(TAG);
        }
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

            Toast.makeText(MainActivity.this, result.getLocalizedMessage(),
                    Toast.LENGTH_LONG).show();
        }
    }
}



