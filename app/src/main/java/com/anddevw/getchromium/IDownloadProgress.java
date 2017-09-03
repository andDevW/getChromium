package com.anddevw.getchromium;

/**
 * Created by devnet on 9/3/17.
 */

public interface IDownloadProgress {
    void onProgress(int done, int total);

}
