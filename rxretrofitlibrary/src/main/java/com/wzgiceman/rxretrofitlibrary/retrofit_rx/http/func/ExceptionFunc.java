package com.wzgiceman.rxretrofitlibrary.retrofit_rx.http.func;

import com.wzgiceman.rxretrofitlibrary.retrofit_rx.exception.FactoryException;

import rx.Observable;
import rx.functions.Func1;

/**
 * 异常处理
 * Created by WZG on 2017/3/23.
 */

public class ExceptionFunc implements Func1<Throwable, Observable> {
    @Override
    public Observable call(Throwable throwable) {
        return Observable.error(FactoryException.analysisExcetpion(throwable));
    }
}
