package com.wzgiceman.rxretrofitlibrary.retrofit_rx.downlaod;

import okhttp3.ResponseBody;
import retrofit2.http.GET;
import retrofit2.http.Header;
import retrofit2.http.Streaming;
import retrofit2.http.Url;
import rx.Observable;

/**
 * service-下载接口
 * Created by WZG on 2016/7/16.
 */
public interface HttpDownService {

    /*断点续传下载接口*/
    @Streaming/*大文件需要加入这个判断，防止下载过程中写入到内存中*/
    @GET
    Observable<ResponseBody> download(@Header("RANGE") String start, @Url String url);
}
