package com.anddevw.getchromium.util;


import android.content.Context;

import java.io.File;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;

public class StorageUtil {

    private static final String EXT_STORAGE_ROOT_PREFIX = "/Android/data/";
    private static final String EXT_STORAGE_ROOT_SUFFIX = "/files/";
    private static final StringBuilder sStoragePath = new StringBuilder();

    public static File getDir(Context context, String dirName) {
        File cacheDir = null;
        if (android.os.Environment.MEDIA_MOUNTED.equals(android.os.Environment.getExternalStorageState())) {
            Method getExternalFilesDirMethod = null;
            try {
                getExternalFilesDirMethod = Context.class.getMethod("getExternalFilesDir", String.class);
                cacheDir = (File) getExternalFilesDirMethod.invoke(context, dirName);
            } catch (NoSuchMethodException | InvocationTargetException | IllegalAccessException | IllegalArgumentException e) {
                cacheDir = buildCacheDirPath(context, android.os.Environment.getExternalStorageDirectory(), dirName);
            }
        }

        if (cacheDir != null && !cacheDir.exists()) {
            if (!cacheDir.mkdirs()) {
                cacheDir = null;
            }
        }
        if (cacheDir == null) {
            cacheDir = new File(context.getCacheDir() + File.separator + dirName);
            cacheDir.mkdirs();
        }
        return cacheDir;
    }

    private static File buildCacheDirPath(Context context, File mountPoint, String dirName) {
        sStoragePath.setLength(0);
        sStoragePath.append(EXT_STORAGE_ROOT_PREFIX);
        sStoragePath.append(context.getPackageName());
        sStoragePath.append(EXT_STORAGE_ROOT_SUFFIX);
        sStoragePath.append(dirName);
        return new File(mountPoint, sStoragePath.toString());
    }
}