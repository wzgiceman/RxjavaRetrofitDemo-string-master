package com.wzgiceman.rxretrofitlibrary.retrofit_rx.http;

import com.trello.rxlifecycle.components.support.RxAppCompatActivity;
import com.wzgiceman.rxretrofitlibrary.retrofit_rx.Api.BaseApi;
import com.wzgiceman.rxretrofitlibrary.retrofit_rx.exception.FactoryException;
import com.wzgiceman.rxretrofitlibrary.retrofit_rx.exception.RetryWhenNetworkException;
import com.wzgiceman.rxretrofitlibrary.retrofit_rx.listener.HttpOnNextListener;
import com.wzgiceman.rxretrofitlibrary.retrofit_rx.subscribers.ProgressSubscriber;

import java.lang.ref.SoftReference;
import java.util.concurrent.TimeUnit;

import okhttp3.OkHttpClient;
import retrofit2.Retrofit;
import retrofit2.adapter.rxjava.RxJavaCallAdapterFactory;
import retrofit2.converter.scalars.ScalarsConverterFactory;
import rx.Observable;
import rx.android.schedulers.AndroidSchedulers;
import rx.functions.Func1;
import rx.schedulers.Schedulers;

/**
 * http交互处理类
 * Created by WZG on 2016/7/16.
 */
public class HttpManager {
    /*软引用對象*/
    private SoftReference<HttpOnNextListener> onNextListener;
    private SoftReference<RxAppCompatActivity> appCompatActivity;

    public HttpManager(HttpOnNextListener onNextListener, RxAppCompatActivity appCompatActivity) {
        this.onNextListener = new SoftReference(onNextListener);
        this.appCompatActivity = new SoftReference(appCompatActivity);
    }

    /**
     * 处理http请求
     *
     * @param basePar 封装的请求数据
     */
    public void doHttpDeal(BaseApi basePar) {
        //手动创建一个OkHttpClient并设置超时时间缓存等设置
        OkHttpClient.Builder builder = new OkHttpClient.Builder();
        builder.connectTimeout(basePar.getConnectionTime(), TimeUnit.SECONDS);
       /* builder.addInterceptor(new CacheInterceptor());
        builder.addNetworkInterceptor(new CacheInterceptor());
        builder.cache(new Cache(MyApplication.app.getCacheDir(),10*1024*1024));*/

        /*创建retrofit对象*/
        Retrofit retrofit = new Retrofit.Builder()
                .client(builder.build())
                .addConverterFactory(ScalarsConverterFactory.create())
                .addCallAdapterFactory(RxJavaCallAdapterFactory.create())
                .baseUrl(basePar.getBaseUrl())
                .build();
        /*rx处理*/
        ProgressSubscriber subscriber = new ProgressSubscriber(basePar, onNextListener, appCompatActivity);
        Observable observable = basePar.getObservable(retrofit)
                /*失败后的retry配置*/
                .retryWhen(new RetryWhenNetworkException())
                /*异常处理*/
                .onErrorResumeNext(funcException)
                /*生命周期管理*/
                .compose(appCompatActivity.get().bindToLifecycle())
                /*http请求线程*/
                .subscribeOn(Schedulers.io())
                .unsubscribeOn(Schedulers.io())
                /*回调线程*/
                .observeOn(AndroidSchedulers.mainThread())
                /*结果判断*/
                .map(basePar);

        /*数据回调*/
        observable.subscribe(subscriber);
    }


    /**
     * 异常处理
     */
    Func1 funcException = new Func1<Throwable, Observable>() {
        @Override
        public Observable call(Throwable throwable) {
            return Observable.error(FactoryException.analysisExcetpion(throwable));
        }
    };

}
