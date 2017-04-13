package com.example.retrofit.activity;

import android.os.Bundle;
import android.view.View;
import android.widget.TextView;

import com.alibaba.fastjson.JSONObject;
import com.alibaba.fastjson.TypeReference;
import com.example.retrofit.R;
import com.example.retrofit.entity.api.CombinApi;
import com.example.retrofit.entity.resulte.BaseResultEntity;
import com.example.retrofit.entity.resulte.SubjectResulte;
import com.trello.rxlifecycle.components.support.RxAppCompatActivity;
import com.wzgiceman.rxretrofitlibrary.retrofit_rx.exception.ApiException;
import com.wzgiceman.rxretrofitlibrary.retrofit_rx.listener.HttpOnNextListener;

import java.util.ArrayList;


/**
 * 统一api类处理方案界面
 *
 * @author wzg
 */
public class CombinApiActivity extends RxAppCompatActivity implements HttpOnNextListener{
    private TextView tvMsg;
    CombinApi api;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_combin_api);

        api=new CombinApi(this,this);
        tvMsg = (TextView) findViewById(R.id.tv_msg);
        findViewById(R.id.btn_rx_all).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                api.postApi(true);
            }
        });
        findViewById(R.id.btn_rx_all_2).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                api.postApiOther(true);
            }
        });
    }

    @Override
    public void onNext(String resulte, String method) {
        BaseResultEntity<ArrayList<SubjectResulte>> subjectResulte = JSONObject.parseObject(resulte, new
                TypeReference<BaseResultEntity<ArrayList<SubjectResulte>>>(){});
        tvMsg.setText("统一post返回：\n" + subjectResulte.getData().toString());
    }

    @Override
    public void onError(ApiException e) {
        tvMsg.setText("失败：\ncode=" + e.getCode() + "\nmsg:" + e.getDisplayMessage());
    }
}
