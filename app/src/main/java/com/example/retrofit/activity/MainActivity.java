package com.example.retrofit.activity;

import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.ImageView;
import android.widget.TextView;

import com.alibaba.fastjson.JSONObject;
import com.bumptech.glide.Glide;
import com.daimajia.numberprogressbar.NumberProgressBar;
import com.example.retrofit.R;
import com.example.retrofit.entity.api.SubjectPostApi;
import com.example.retrofit.entity.api.UploadApi;
import com.example.retrofit.entity.resulte.SubjectResulte;
import com.example.retrofit.entity.resulte.UploadResulte;
import com.trello.rxlifecycle.components.support.RxAppCompatActivity;
import com.wzgiceman.rxretrofitlibrary.retrofit_rx.exception.ApiException;
import com.wzgiceman.rxretrofitlibrary.retrofit_rx.http.HttpManager;
import com.wzgiceman.rxretrofitlibrary.retrofit_rx.listener.HttpOnNextListener;
import com.wzgiceman.rxretrofitlibrary.retrofit_rx.listener.HttpOnNextSubListener;
import com.wzgiceman.rxretrofitlibrary.retrofit_rx.listener.upload.ProgressRequestBody;
import com.wzgiceman.rxretrofitlibrary.retrofit_rx.listener.upload.UploadProgressListener;

import java.io.File;
import java.util.List;

import okhttp3.MediaType;
import okhttp3.MultipartBody;
import okhttp3.RequestBody;
import rx.Observable;

public class MainActivity extends RxAppCompatActivity implements View.OnClickListener, HttpOnNextListener, HttpOnNextSubListener {
    private TextView tvMsg;
    private NumberProgressBar progressBar;
    private ImageView img;
    //    公用一个HttpManager
    private HttpManager manager;
    //    post请求接口信息
    private SubjectPostApi postEntity;
    //    上传接口信息
    private UploadApi uplaodApi;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        tvMsg = (TextView) findViewById(R.id.tv_msg);
        findViewById(R.id.btn_rx).setOnClickListener(this);
        findViewById(R.id.btn_rx_mu_down).setOnClickListener(this);
        findViewById(R.id.btn_rx_uploade).setOnClickListener(this);
        img = (ImageView) findViewById(R.id.img);
        progressBar = (NumberProgressBar) findViewById(R.id.number_progress_bar);


        /*初始化数据*/
        manager = new HttpManager(this, this);

        postEntity = new SubjectPostApi();
        postEntity.setAll(true);

        /*上传接口内部接口有token验证，所以需要换成自己的接口测试，检查file文件是否手机存在*/
        uplaodApi = new UploadApi();
        File file = new File("/storage/emulated/0/Download/11.jpg");
        RequestBody requestBody = RequestBody.create(MediaType.parse("image/jpeg"), file);
        MultipartBody.Part part = MultipartBody.Part.createFormData("file_name", file.getName(), new ProgressRequestBody
                (requestBody,
                        new UploadProgressListener() {
                            @Override
                            public void onProgress(long currentBytesCount, long totalBytesCount) {
                                tvMsg.setText("提示:上传中");
                                progressBar.setMax((int) totalBytesCount);
                                progressBar.setProgress((int) currentBytesCount);
                            }
                        }));
        uplaodApi.setPart(part);
    }


    @Override
    public void onClick(View v) {
        switch (v.getId()) {
            case R.id.btn_rx:
                manager.doHttpDeal(postEntity);
                break;
            case R.id.btn_rx_uploade:
                manager.doHttpDeal(uplaodApi);
                break;
            case R.id.btn_rx_mu_down:
                Intent intent = new Intent(this, DownLaodActivity.class);
                startActivity(intent);
                break;
        }
    }

    //    完美封装简化版
    private void simpleDo() {
         /*初始化数据*/
        manager = new HttpManager(this, this);
        postEntity = new SubjectPostApi();
        postEntity.setAll(true);
        manager.doHttpDeal(postEntity);
    }


    @Override
    public void onNext(String resulte, String mothead) {
        /*post返回处理*/
        if (mothead.equals(postEntity.getMothed())) {
            List<SubjectResulte> subjectResulte= JSONObject.parseArray(resulte,SubjectResulte.class);
            tvMsg.setText("post返回：\n"+subjectResulte.toString() );
        }

        /*上传返回处理*/
        if (mothead.equals(uplaodApi.getMothed())) {
            UploadResulte uploadResulte = JSONObject.parseObject(resulte, UploadResulte.class);
            tvMsg.setText("上传成功返回：\n" + uploadResulte.getHeadImgUrl());
            Glide.with(MainActivity.this).load(uploadResulte.getHeadImgUrl()).skipMemoryCache(true).into(img);
        }
    }

    @Override
    public void onError(ApiException e) {
        tvMsg.setText("失败：\ncode=" + e.getCode() + "\nmsg:" + e.getDisplayMessage());
    }


    @Override
    public void onNext(Observable observable) {

    }

}
