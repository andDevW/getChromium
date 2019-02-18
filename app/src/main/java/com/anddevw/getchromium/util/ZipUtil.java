package com.anddevw.getchromium.util;

import android.util.Log;

import java.io.BufferedOutputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.OutputStream;
import java.util.zip.ZipEntry;
import java.util.zip.ZipInputStream;


public class ZipUtil {

    private static final int BUFFER_SIZE = 8192;

    private final String apkZip;
    private final String address;
    private final byte[] buffer;

    public ZipUtil(String zipFile, String location) {
        apkZip = zipFile;
        address = location;
        buffer = new byte[BUFFER_SIZE];
        dirChecker("");
    }

    public void unzip() {
        FileInputStream fileInputStream = null;
        ZipInputStream zipInputStream = null;
        OutputStream outputStream = null;

        File outputDir = new File(address);
        File tmp = null;

        try {
            fileInputStream = new FileInputStream(apkZip);
            zipInputStream = new ZipInputStream(fileInputStream);
            ZipEntry zipEntry;
            while ((zipEntry = zipInputStream.getNextEntry()) != null) {
                Log.d("Decompress", "Unzipping " + zipEntry.getName());

                if (zipEntry.isDirectory()) {
                    dirChecker(zipEntry.getName());
                } else {
                    tmp = File.createTempFile("decomp", ".tmp", outputDir);
                    outputStream = new BufferedOutputStream(new FileOutputStream(tmp));
                    DownloadUtil.copyStream(zipInputStream, outputStream, buffer, BUFFER_SIZE);
                    zipInputStream.closeEntry();
                    outputStream.close();
                    outputStream = null;
                    tmp.renameTo(new File(address + zipEntry.getName()));
                    tmp = null;
                }
            }
            zipInputStream.close();
            zipInputStream = null;
        } catch (IOException e) {
            throw new RuntimeException(e);
        } finally {
            if (tmp != null) {
                try {
                    tmp.delete();
                } catch (Exception ignore) {
                }
            }
            if (outputStream != null) {
                try {
                    outputStream.close();
                } catch (Exception ignore) {
                }
            }
            if (zipInputStream != null) {
                try {
                    zipInputStream.closeEntry();
                } catch (Exception ignore) {
                }
            }
            if (fileInputStream != null) {
                try {
                    fileInputStream.close();
                } catch (Exception ignore) {
                }
            }
        }
    }

    private void dirChecker(String dir) {
        File f = new File(address + dir);

        if (!f.isDirectory()) {
            f.mkdirs();
        }
    }
}