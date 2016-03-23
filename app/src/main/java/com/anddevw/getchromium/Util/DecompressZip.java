package com.anddevw.getchromium.Util;

import android.util.Log;

import java.io.BufferedOutputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.OutputStream;
import java.util.zip.ZipEntry;
import java.util.zip.ZipInputStream;


public class DecompressZip {
    private static final int BUFFER_SIZE=8192;

    private String apkzip;
    private String address;
    private byte[] buffer;

    public DecompressZip(String zipFile, String location) {
        apkzip = zipFile;
        address = location;
        buffer = new byte[BUFFER_SIZE];
        dirChecker("");
    }
    public void unzip() {
        FileInputStream finstream = null;
        ZipInputStream zinstream = null;
        OutputStream foutstream = null;

        File outputDir = new File(address);
        File tmp = null;

        try {
            finstream = new FileInputStream(apkzip);
            zinstream = new ZipInputStream(finstream);
            ZipEntry zentry = null;
            while ((zentry = zinstream.getNextEntry()) != null) {
                Log.d("Decompress", "Unzipping " + zentry.getName());

                if (zentry.isDirectory()) {
                    dirChecker(zentry.getName());
                } else {
                    tmp = File.createTempFile( "decomp", ".tmp", outputDir );
                    foutstream = new BufferedOutputStream(new FileOutputStream(tmp));
                    DownloadChromiumApk.copyStream(zinstream, foutstream, buffer, BUFFER_SIZE);
                    zinstream.closeEntry();
                    foutstream.close();
                    foutstream = null;
                    tmp.renameTo( new File(address + zentry.getName()) );
                    tmp = null;
                }
            }
            zinstream.close();
            zinstream = null;
        } catch (IOException e) {
            throw new RuntimeException(e);
        } finally {
            if ( tmp != null  ) { try { tmp.delete();     } catch (Exception ignore) {
            } }
            if ( foutstream != null ) { try { foutstream.close(); 	  } catch (Exception ignore) {
            } }
            if ( zinstream != null  ) { try { zinstream.closeEntry(); } catch (Exception ignore) {
            } }
            if ( finstream != null  ) { try { finstream.close(); 	  } catch (Exception ignore) {
            } }
        }
    }
    private void dirChecker(String dir) {
        File f = new File(address + dir);

        if (!f.isDirectory()) {
            f.mkdirs();
        }
    }
}