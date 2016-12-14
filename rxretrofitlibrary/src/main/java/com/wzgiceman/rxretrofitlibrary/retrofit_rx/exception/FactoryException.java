package com.wzgiceman.rxretrofitlibrary.retrofit_rx.exception;

import com.alibaba.fastjson.JSONException;
import com.alibaba.fastjson.JSONPathException;

import java.net.ConnectException;
import java.net.SocketTimeoutException;
import java.net.UnknownHostException;
import java.text.ParseException;

import retrofit2.adapter.rxjava.HttpException;

/**
 * 异常处理工厂
 * 主要是解析异常，输出自定义ApiException
 * Created by WZG on 2016/12/12.
 */

public class FactoryException {
    private static final String HttpException_MSG = "网络错误";
    private static final String ConnectException_MSG = "连接失败";
    private static final String JSONException_MSG = "fastjeson解析失败";
    private static final String UnknownHostException_MSG = "无法解析该域名";

    /**
     * 解析异常
     *
     * @param e
     * @return
     */
    public static ApiException analysisExcetpion(Throwable e) {
        ApiException apiException = new ApiException(e);
        if (e instanceof HttpException) {
             /*网络异常*/
            apiException.setCode(CodeException.HTTP_ERROR);
            apiException.setDisplayMessage(HttpException_MSG);
        } else if (e instanceof HttpTimeException) {
             /*自定义运行时异常*/
            HttpTimeException exception = (HttpTimeException) e;
            apiException.setCode(CodeException.RUNTIME_ERROR);
            apiException.setDisplayMessage(exception.getMessage());
        } else if (e instanceof ConnectException||e instanceof SocketTimeoutException) {
             /*链接异常*/
            apiException.setCode(CodeException.HTTP_ERROR);
            apiException.setDisplayMessage(ConnectException_MSG);
        } else if (e instanceof JSONPathException || e instanceof JSONException || e instanceof ParseException) {
             /*fastjson解析异常*/
            apiException.setCode(CodeException.JSON_ERROR);
            apiException.setDisplayMessage(JSONException_MSG);
        }else if (e instanceof UnknownHostException){
            /*无法解析该域名异常*/
            apiException.setCode(CodeException.UNKOWNHOST_ERROR);
            apiException.setDisplayMessage(UnknownHostException_MSG);
        } else {
            /*未知异常*/
            apiException.setCode(CodeException.UNKNOWN_ERROR);
            apiException.setDisplayMessage(e.getMessage());
        }
        return apiException;
    }
}
