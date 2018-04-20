package com.wzgiceman.rxretrofitlibrary.retrofit_rx.subscribers;


import android.app.ProgressDialog;
import android.content.Context;
import android.content.DialogInterface;

import com.wzgiceman.rxretrofitlibrary.retrofit_rx.Api.BaseApi;
import com.wzgiceman.rxretrofitlibrary.retrofit_rx.RxRetrofitApp;
import com.wzgiceman.rxretrofitlibrary.retrofit_rx.exception.ApiException;
import com.wzgiceman.rxretrofitlibrary.retrofit_rx.exception.HttpTimeException;
import com.wzgiceman.rxretrofitlibrary.retrofit_rx.http.cookie.CookieResulte;
import com.wzgiceman.rxretrofitlibrary.retrofit_rx.listener.HttpOnNextListener;
import com.wzgiceman.rxretrofitlibrary.retrofit_rx.utils.AppUtil;
import com.wzgiceman.rxretrofitlibrary.retrofit_rx.utils.CookieDbUtil;

import java.lang.ref.SoftReference;

import rx.Subscriber;


/**
 * 用于在Http请求开始时，自动显示一个ProgressDialog
 * 在Http请求结束是，关闭ProgressDialog
 * 调用者自己对请求数据进行处理
 * Created by WZG on 2016/7/16.
 */
public class ProgressSubscriber<T> extends Subscriber<T> {
    //    回调接口
    private SoftReference<HttpOnNextListener> mSubscriberOnNextListener;
    //    软引用反正内存泄露
    private SoftReference<Context> mActivity;
    //    加载框可自己定义
    private ProgressDialog pd;
    /*请求数据*/
    private BaseApi api;


    /**
     * 构造
     *
     * @param api
     */
    public ProgressSubscriber(BaseApi api, SoftReference<HttpOnNextListener> listenerSoftReference, SoftReference<Context>
            mActivity) {
        this.api = api;
        this.mSubscriberOnNextListener = listenerSoftReference;
        this.mActivity = mActivity;
    }


    /**
     * 订阅开始时调用
     * 显示ProgressDialog
     */
    @Override
    public void onStart() {
        /*缓存并且有网*/
        if (api.isCache() && AppUtil.isNetworkAvailable(RxRetrofitApp.getApplication())) {
            /*获取缓存数据*/
            CookieResulte cookieResulte = CookieDbUtil.getInstance().queryCookieBy(api.getCacheUrl());
            if (cookieResulte != null && mSubscriberOnNextListener.get() != null && (System.currentTimeMillis() - cookieResulte
                    .getTime()) / 1000 < api.getCookieNetWorkTime()) {
                mSubscriberOnNextListener.get().onNext(cookieResulte.getResulte(), api.getMethod());
                onCompleted();
                unsubscribe();
                return;
            }
        }

        if (api.isShowProgress()) {
            initProgressDialog(api.isCancel());
        }
    }


    /**
     * 初始化加载框
     */
    private void initProgressDialog(boolean cancel) {
        if (!api.isShowProgress()) return;
        Context context = mActivity.get();
        if (pd == null && context != null) {
            pd = new ProgressDialog(context);
            pd.setCancelable(cancel);
            if (cancel) {
                pd.setOnCancelListener(new DialogInterface.OnCancelListener() {
                    @Override
                    public void onCancel(DialogInterface dialogInterface) {
                        onCancelProgress();
                    }
                });
            }
        }
        pd.show();
    }


    /**
     * 隐藏
     */
    private void dismissProgressDialog() {
        if (pd != null) {
            pd.dismiss();
        }
    }


    /**
     * 完成，隐藏ProgressDialog
     */
    @Override
    public void onCompleted() {
        dismissProgressDialog();
    }

    /**
     * 对错误进行统一处理
     * 隐藏ProgressDialog
     *
     * @param e
     */
    @Override
    public void onError(Throwable e) {
        /*需要緩存并且本地有缓存才返回*/
        if (api.isCache()) {
            getCache(e);
        } else {
            errorDo(e);
        }
        dismissProgressDialog();
    }

    /**
     * 获取cache数据
     */
    private void getCache(Throwable te) {
        try {
            /*获取缓存数据*/
            CookieResulte cookieResulte = CookieDbUtil.getInstance().queryCookieBy(api.getCacheUrl());
            if (cookieResulte == null) {
                errorDo(te);
                return;
            }
            if (mSubscriberOnNextListener.get() != null) {
                mSubscriberOnNextListener.get().onNext(cookieResulte.getResulte(), api.getMethod());
            }
        } catch (Exception e) {
            errorDo(te == null ? e : te);
        }
    }


    /**
     * 错误统一处理
     *
     * @param e
     */
    private void errorDo(Throwable e) {
        Context context = mActivity.get();
        if (context == null) return;
        HttpOnNextListener httpOnNextListener = mSubscriberOnNextListener.get();
        if (httpOnNextListener == null) return;
        ApiException apiException;
        if (e instanceof ApiException) {
            apiException = (ApiException) e;
            httpOnNextListener.onError(apiException, api.getMethod());
        } else if (e instanceof HttpTimeException) {
            HttpTimeException exception = (HttpTimeException) e;
            apiException = new ApiException(exception, exception.getCode(), exception.getMessage());
            httpOnNextListener.onError(apiException, api.getMethod());
        } else {
            apiException = new ApiException(e, HttpTimeException.UNKNOWN_ERROR, e.getMessage());
            httpOnNextListener.onError(apiException, api.getMethod());
        }
    }


    /**
     * 将onNext方法中的返回结果交给Activity或Fragment自己处理
     *
     * @param t 创建Subscriber时的泛型类型
     */
    @Override
    public void onNext(T t) {
        if (mSubscriberOnNextListener.get() != null) {
            mSubscriberOnNextListener.get().onNext((String) t, api.getMethod());
        }

        /*缓存处理*/
        CookieResulte resulte = CookieDbUtil.getInstance().queryCookieBy(api.getCacheUrl());
        long time = System.currentTimeMillis();
        if (resulte == null && api.isCache()) {
            resulte = new CookieResulte(api.getCacheUrl(), t.toString(), time);
            CookieDbUtil.getInstance().saveCookie(resulte);
        }
        if (resulte != null) {
            resulte.setResulte(t.toString());
            resulte.setTime(time);
            CookieDbUtil.getInstance().updateCookie(resulte);
        }
    }


    /**
     * 取消ProgressDialog的时候，取消对observable的订阅，同时也取消了http请求
     */
    public void onCancelProgress() {
        if (!this.isUnsubscribed()) {
            this.unsubscribe();
            if (api.isCache()) {
                getCache(null);
            } else {
                errorDo(new ApiException(new Throwable(), HttpTimeException.HTTP_CANCEL, "请求取消！"));
            }
        }
    }
}