package com.example.retrofit;

import retrofit2.http.GET;
import retrofit2.http.Query;
import rx.Observable;

/**
 * 测试接口service-post相关
 * Created by WZG on 2016/12/19.
 */

public interface HttpPostService {


    @GET("AppFiftyToneGraph/videoLink/{once_no}")
    Observable<String> getAllVedioBy(@Query("once_no") boolean once_no);

}
