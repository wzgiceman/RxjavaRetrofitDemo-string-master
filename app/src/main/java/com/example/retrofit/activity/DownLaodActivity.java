package com.example.retrofit.activity;

import android.os.Bundle;
import android.os.Environment;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.LinearLayoutManager;

import com.example.retrofit.R;
import com.example.retrofit.activity.adapter.DownAdapter;
import com.jude.easyrecyclerview.EasyRecyclerView;
import com.wzgiceman.rxretrofitlibrary.retrofit_rx.downlaod.DownInfo;
import com.wzgiceman.rxretrofitlibrary.retrofit_rx.downlaod.DownState;
import com.wzgiceman.rxretrofitlibrary.retrofit_rx.utils.DbDwonUtil;

import java.io.File;
import java.util.List;

/**
 * 多任務下載
 */
public class DownLaodActivity extends AppCompatActivity {
    List<DownInfo> listData;
    DbDwonUtil dbUtil;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_down_laod);
        initResource();
        initWidget();
    }

    /*数据*/
    private void initResource() {
        dbUtil = DbDwonUtil.getInstance();
        listData = dbUtil.queryDownAll();
        /*第一次模拟服务器返回数据掺入到数据库中*/
        if (listData.isEmpty()) {
            String[] downUrl = new String[]{"https://pic.newbanker" +
                    ".cn/1491537974620__%E3%80%90%E5%9F%BA%E9%87%91%E5%90%88%E5%90%8C%E3%80%91%E5%8C%97%E4%BA%AC%E6%81%92%E5%AE" +
                    "%87%E5%A4%A9%E6%B3%BD%E6%8A%95%E8%B5%84%E7%AE%A1%E7%90%86%E6%9C%89%E9%99%90%E5%85%AC%E5%8F%B8-%E6%81%92%E5" +
                    "%AE%87%E5%A4%A9%E6%B3%BD%E4%BA%9A%E9%A9%AC%E9%80%8A%E5%85%AB%E5%8F%B7%E7%A7%81%E5%8B%9F%E6%8A%95%E8%B5%84" +
                    "%E5%9F%BA%E9%87%91%EF%BC%88%E4%BA%8C%E6%9C%9F%EF%BC%89.pdf", "http://www.izaodao.com/app/izaodao_app.apk",
                    "http://www.izaodao.com/app/izaodao_app.apk", "http://www.izaodao.com/app/izaodao_app.apk"};
            for (int i = 0; i < downUrl.length; i++) {
                File outputFile = new File(Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_DOWNLOADS),
                        "test" + i + (i==0?".pdf":".apk"));
                DownInfo apkApi = new DownInfo(downUrl[i]);
                apkApi.setId(i);
                apkApi.setState(DownState.START);
                apkApi.setSavePath(outputFile.getAbsolutePath());
                dbUtil.save(apkApi);
            }
            listData = dbUtil.queryDownAll();
        }
    }

    /*加载控件*/
    private void initWidget() {
        EasyRecyclerView recyclerView = (EasyRecyclerView) findViewById(R.id.rv);
        DownAdapter adapter = new DownAdapter(this);
        recyclerView.setLayoutManager(new LinearLayoutManager(this));
        recyclerView.setAdapter(adapter);
        adapter.addAll(listData);
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        /*记录退出时下载任务的状态-复原用*/
        for (DownInfo downInfo : listData) {
            dbUtil.update(downInfo);
        }
    }
}
