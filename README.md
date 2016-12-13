## Rxjava+ReTrofit+okHttp深入浅出-终极封装特殊篇（替换Gson返回）

##背景

在之前的封装1-5[Rxjava+ReTrofit+okHttp深入浅出-终极封装](http://blog.csdn.net/column/details/13297.html)中我们都是通过传统的GsonConverterFactory自动解析，这样做确实很方便，用户能直接获取返回的对象，不用关心具体的转换，但是：这随之而来有很多的缺陷（虽然官网推荐这样使用）；

比如：无法使用其他第三发转换框架；泛型无法中间传递，封装无法统一处理缓存结果；回调信息无法统一处理..........
所以我们在享受它遍历的同时也被迫的要限制做很多的处理，限制我们的扩展！

这里就介绍如何放弃GsonConverterFactory，直接返回String，扩展我们的封装！（封装的整体思想和之前的封装一样，所以不会有大的改动！）

##效果
![Preview](https://github.com/wzgiceman/RxjavaRetrofitDemo-string-master/blob/master/gif/retrofit_string.gif)

##功能

        1.Retrofit+Rxjava+okhttp基本使用方法
        2.统一处理请求数据格式
        3.统一的ProgressDialog和回调Subscriber处理
        4.取消http请求
        5.预处理http请求
        6.返回数据的统一判断
        7.失败后的retry处理
        8.RxLifecycle管理生命周期，防止泄露
        9.文件上传下载(支持多文件，断点续传)
        10.Cache数据持久化和数据库（greenDao）两种缓存机制
        11.异常统一处理


##添加相关引用
```java
    /*rx-android-java*/
     compile 'com.squareup.retrofit:adapter-rxjava:+'
     compile 'com.trello:rxlifecycle:+'
     compile 'com.trello:rxlifecycle-components:+'
     /*rotrofit*/
     compile 'com.squareup.retrofit2:retrofit:+'
     compile 'com.squareup.retrofit2:adapter-rxjava:+'
     compile 'com.squareup.retrofit2:converter-scalars:+'
     compile 'com.alibaba:fastjson:+'
```

##代码使用

###1.初始化

moudel导入工程

```java
  compile project(':rxretrofitlibrary')
```

在Application中初始化RxRetrofitApp

```java
 RxRetrofitApp.init(this);
```

###2.初始化HttpManager

需要传递一个回调HttpOnNextListener接口和activity生命周期

```java
  HttpManager manager=new HttpManager(this,this);
```

###3.初始请求的数据和参数

```java
public class SubjectPostApi extends BaseApi {
//    接口需要传入的参数 可自定义不同类型
    private boolean all;
    /*任何你先要传递的参数*/
//    String xxxxx;

    /**
     * 默认初始化需要给定回调和rx周期类
     * 可以额外设置请求设置加载框显示，回调等（可扩展）
     */
    public SubjectPostApi() {
        setShowProgress(true);
        setCancel(true);
        setCache(true);
        setMothed("AppFiftyToneGraph/videoLink");
        setCookieNetWorkTime(60);
        setCookieNoNetWorkTime(24*60*60);
    }
  }

```
###4.请求后的统一处理

通过method参数判断接口，然后动态解析返回的数据
```java
    @Override
    public void onNext(String resulte, String method) {
        /*post返回处理*/
        if(method.equals(postEntity.getMothed())){
            List<SubjectResulte>  subjectResulte= JSONObject.parseArray(resulte,SubjectResulte.class);
            tvMsg.setText("post返回：\n"+subjectResulte.toString() );
        }

        /*上传返回处理*/
        if(method.equals(uplaodApi.getMothed())){
            UploadResulte uploadResulte=JSONObject.parseObject(resulte,UploadResulte.class);
            tvMsg.setText("上传成功返回：\n"+uploadResulte.getHeadImgUrl());
            Glide.with(MainActivity.this).load(uploadResulte.getHeadImgUrl()).skipMemoryCache(true).into(img);
        }
    }

    @Override
    public void onError(Throwable e) {
        tvMsg.setText("失败：\n" + e.toString());
    }
```


* 初始化一个请求数据的对象继承BaseApi对象设置请求需要的参数
* 通过httpmanger对象，触发请求
* 结果统一通过BaseApi中的fun1方法判断，最后返回HttpOnNextListener

##优化迭代
根据反馈及时更新和优化的过程，如果在使用过程中有任何问题，欢迎反馈给我！

##思路
详细思路可以可以参看我的博客：
[Rxjava+ReTrofit+okHttp深入浅出-终极封装](http://blog.csdn.net/column/details/13297.html)

**如有帮助，欢迎start和follow后续会出更多好的开源技术**

#                                     微信交流群

![](https://github.com/wzgiceman/RxjavaRetrofitDemo-string-master/blob/master/gif/wxq.png)

[**最近有幸被CSDN提名参加博客之星评选，希望你能投我一票!**](http://blog.csdn.net/vote/candidate.html?username=u014610664)
