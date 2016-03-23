package com.anddevw.getchromium.Util;

import java.io.BufferedOutputStream;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.net.URL;

public class DownloadChromiumApk  {

    private static final int BUFFER_SIZE = 8192;

    public static void download( String url, File output, File tmpDir ) {
        InputStream is = null;
        OutputStream os = null;
        File tmp = null;

        try {
            tmp = File.createTempFile( "download", ".tmp", tmpDir );
            is = new URL(url).openStream();
            os = new BufferedOutputStream( new FileOutputStream( tmp ) );
            copyStream( is, os );
            tmp.renameTo( output );
            tmp = null;
        } catch ( IOException e ) {
            throw new RuntimeException( e );
        } finally {
            if ( tmp != null ) { try { tmp.delete(); tmp = null; } catch (Exception ignore) {
            } }
            if ( is != null  ) { try { is.close();   is = null;  } catch (Exception ignore) {
            } }
            if ( os != null  ) { try { os.close();   os = null;  } catch (Exception ignore) {
            } }
        }
    }

    public static void copyStream( InputStream is, OutputStream os ) throws IOException {
        byte[] buffer = new byte[ BUFFER_SIZE ];
        copyStream( is, os, buffer, BUFFER_SIZE );
    }

    public static void copyStream( InputStream is, OutputStream os,
                                   byte[] buffer, int bufferSize ) throws IOException {
        try {
            for (;;) {
                int count = is.read( buffer, 0, bufferSize );
                if ( count == -1 ) { break; }
                os.write( buffer, 0, count );
            }
        } catch ( IOException e ) {
            throw e;
        }
    }
}