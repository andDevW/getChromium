package com.anddevw.getchromium.Util;


import android.content.Context;

import java.io.File;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;

public class GetStorage {
    private static final String EXT_STORAGE_ROOT_PREFIX = "/Android/data/";
    private static final String EXT_STORAGE_ROOT_SUFFIX = "/files/";
    private static final String TAG = "ExternalStorage";
    private static StringBuilder sStoragePath = new StringBuilder();
    private static final String ALTERNATE_SDCARD_MOUNTS[] =
            {"/emmc", "/sdcard/ext_sd", "/sdcard-ext", "/sdcard/sd", "/sdcard/sdcard"};

public static File getDir(Context context, String dirName) {
    File cacheDir = null;

    if ( android.os.Environment.MEDIA_MOUNTED.equals(android.os.Environment.getExternalStorageState()) ) {

        Method getExternalFilesDirMethod = null;
        try {
            getExternalFilesDirMethod = Context.class.getMethod( "getExternalFilesDir", String.class );
            cacheDir = (File) getExternalFilesDirMethod.invoke( context, dirName );
        } catch (NoSuchMethodException e) {
            cacheDir = buildCacheDirPath( context, android.os.Environment.getExternalStorageDirectory(), dirName );
        } catch (IllegalArgumentException e) {
            cacheDir = buildCacheDirPath( context, android.os.Environment.getExternalStorageDirectory(), dirName );
        } catch (IllegalAccessException e) {
            cacheDir = buildCacheDirPath( context, android.os.Environment.getExternalStorageDirectory(), dirName );
        } catch (InvocationTargetException e) {
            cacheDir = buildCacheDirPath( context, android.os.Environment.getExternalStorageDirectory(), dirName );
        }
    }

    if ( cacheDir == null ) {

        for ( int i = 0; i < ALTERNATE_SDCARD_MOUNTS.length; i++ ) {
            File alternateDir = new File( ALTERNATE_SDCARD_MOUNTS[i] );
            if ( alternateDir.exists() && alternateDir.isDirectory() &&
                    alternateDir.canRead() && alternateDir.canWrite() ) {
                cacheDir = buildCacheDirPath( context, alternateDir, dirName );
                break;
            }
        }
    }

    if ( cacheDir != null && !cacheDir.exists() ) {
        if ( !cacheDir.mkdirs() ) {
            cacheDir = null;
        }
    }

    if ( cacheDir == null ) {
        cacheDir = new File( context.getCacheDir() + File.separator + dirName );
        cacheDir.mkdirs();
    }

    return cacheDir;

    }

    public static void clearSDCache( Context context, String dirName ) {
        File cacheDir = getDir(context, dirName);
        File[] files = cacheDir.listFiles();
        for (File f : files) {
            f.delete();
        }
        cacheDir.delete();
    }

    private static File buildCacheDirPath( Context context, File mountPoint, String dirName ) {
        sStoragePath.setLength(0);
        sStoragePath.append(EXT_STORAGE_ROOT_PREFIX);
        sStoragePath.append(context.getPackageName());
        sStoragePath.append(EXT_STORAGE_ROOT_SUFFIX );
        sStoragePath.append(dirName);
        return new File( mountPoint, sStoragePath.toString());
    }
}