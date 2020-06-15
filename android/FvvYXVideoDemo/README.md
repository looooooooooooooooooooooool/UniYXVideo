# Android-FvvYXVideo

基于 [网易云信安卓推流SDK](https://dev.yunxin.163.com/docs/product/%E7%9B%B4%E6%92%AD/%E6%8E%A8%E6%B5%81%E7%AB%AFSDK/Android%E6%8E%A8%E6%B5%81SDK/%E5%BC%80%E5%8F%91%E6%8C%87%E5%8D%97?pos=toc-0) 开发的SDK，实现安卓直播推流，录制，截图，水印，伴音等功能，详细可参考 [网易云信的文档](https://dev.yunxin.163.com/docs/product/%E7%9B%B4%E6%92%AD/%E6%8E%A8%E6%B5%81%E7%AB%AFSDK/Android%E6%8E%A8%E6%B5%81SDK/%E5%BC%80%E5%8F%91%E6%8C%87%E5%8D%97?pos=toc-0)
我是为了封装个SDK用作uniapp插件并没有java和安卓的基础，所以很多地方可能写法或语法有问题或不是最简便，如有不足请见谅。



### 使用方法

在页面上添加 NeteaseView控件，所有操作都离不开这个控件
```xml
<com.netease.vcloud.video.render.NeteaseView
	android:id="@+id/videoview"
	android:layout_width="match_parent"
	android:layout_height="match_parent"
	android:layout_centerInParent="true" />
```

初始化 FvvYXVideo，[回调参考 - SDK消息回调具体状态码](https://dev.yunxin.163.com/docs/product/%E7%9B%B4%E6%92%AD/%E6%8E%A8%E6%B5%81%E7%AB%AFSDK/Android%E6%8E%A8%E6%B5%81SDK/%E5%BC%80%E5%8F%91%E6%8C%87%E5%8D%97?#枚举值参数介绍)

```java
fvvYXVideo  = new FvvYXVideo(MainActivity.this, new  MessageHandle() {
	@Override	//回调函数，部分操作会有回调
	public void Callback(int i, Object o) {
		Log.i("callback : ", String.valueOf(i) + " : " + (o==null?"":o.toString()));
		switch (i){
		   //停止推流回调
			case 25:
				//o为录制的文件路径
			break;
			//截图回调
			case 37:
			    //保存截图
				fvvYXVideo.saveBitmap((Bitmap) o,"/sdcard/test.jpg");
			break;
		}
	}
});
```

### fvvYXVideo.mPublishParam 说明

| 参数 | 类型 | 说明 |
| :------------: | :------------: | :------------: |
| pushUrl | String | 推流地址 |
| recordPath | String | 文件录制存放路径 |
| screenShotPath | String | 截图存放路径 |
| isScale_16x9 | boolean | 是否强制16:9 |
| frontCamera | boolean | 是否默认前置摄像头 |
| useFilter | boolean | 是否使用滤镜 |
| zoom | boolean | 预览是否支持手势缩放 |
| qosEnable | boolean | 是否开启QOS |
| qosEncodeMode | boolean | 1:流畅优先, 2:清晰优先 |

```java
//设置推流类型，AUDIO，VIDEO，AV，默认AV
//AUDIO（只音频），VIDEO（只视频），AV（音视频）
fvvYXVideo.mPublishParam.setStreamType(String type)

//设置推流格式，MP4，RTMP，RTMP_AND_MP4，默认RTMP_AND_MP4
//MP4（只录像），RTMP（只推流），RTMP_AND_MP4（推流并录像）
fvvYXVideo.mPublishParam.setFormatType(String type)

//设置视频清晰度，MEDIUM，HIGH，SUPER，SUPER_HIGH，HD1080P，默认HIGH
//MEDIUM：标清 480360，HIGH：高清 640480，SUPER：超清 960540，SUPER_HIGH：超高清 1280720
fvvYXVideo.mPublishParam.setVideoQuality(String type)
```

### fvvYXVideo 说明
#### 界面预览：
```java
//设置预览界面 setPreview(View view)
//view NeteaseView控件 
fvvYXVideo.setPreview(findViewById(R.id.videoview));

//打开预览界面 startPreview()
fvvYXVideo.startPreview();

//关闭预览界面 stopPreview()
fvvYXVideo.stopPreview();
```
#### 推流录像：
```java
//开始推流 startStream()
fvvYXVideo.startStream();

//结束推流 stopStream()
fvvYXVideo.stopStream();

//暂停视频推流 pauseVideoLiveStream()
fvvYXVideo.pauseVideoLiveStream();

//恢复视频推流 resumeVideoLiveStream()
fvvYXVideo.resumeVideoLiveStream();

//暂停音频推流 pauseAudioLiveStream()
fvvYXVideo.pauseAudioLiveStream();

//恢复音频推流 resumeAudioLiveStream()
fvvYXVideo.resumeAudioLiveStream();

//截图 enableScreenShot(String savePath)
//调用后会在回调中返回图片bitmap，在回调中保存图片到本地
fvvYXVideo.enableScreenShot("/sdcard/test.jpg");

//保存图片 String saveBitmap(Bitmap mBitmap)
//mBitmap 图片bitmap，回调中传入o即可
//返回图片路径
String savePath = fvvYXVideo.saveBitmap((Bitmap) o);
```
#### 滤镜相关：需在打开预览后操作
[滤镜类型参考 - 滤镜类型VideoEffect.FilterType参数说明](https://dev.yunxin.163.com/docs/product/%E7%9B%B4%E6%92%AD/%E6%8E%A8%E6%B5%81%E7%AB%AFSDK/Android%E6%8E%A8%E6%B5%81SDK/%E5%BC%80%E5%8F%91%E6%8C%87%E5%8D%97?pos=toc-0)
```java
//设置滤镜 setFilterType(String type)
//type 滤镜类型，默认 none
fvvYXVideo.setFilterType("FAIRYTALE");

//设置滤镜强度 setFilterStrength(int i)
//i 强度 0-100
fvvYXVideo.setFilterStrength(50);

//设置磨皮强度 setBeautyLevel(int i)
//i 强度 0-100
fvvYXVideo.setBeautyLevel(50);
```
#### 镜头相关：
```java
//闪光灯开关 setCameraFlashPara(Boolean b)
//b 闪光灯开关，默认 false
fvvYXVideo.setCameraFlashPara(true);

//手动对焦一次 setCameraFocus()
fvvYXVideo.setCameraFocus();

//设置自动对焦 setCameraAutoFocus(Boolean isAutoFocus)
//isAutoFocus 是否自动对焦，默认true
fvvYXVideo.setCameraAutoFocus(true);

//切换摄像头 switchCamera()
fvvYXVideo.switchCamera();

//切换分辨率 changeCaptureFormat()
//根据 fvvYXVideo.mPublishParam 切换分辨率
//fvvYXVideo.mPublishParam.videoQuality
//fvvYXVideo.mPublishParam.isScale_16x9
fvvYXVideo.changeCaptureFormat();

//设置本地预览镜像 setPreviewMirror(Boolean mirror)
//mirror 本地预览镜像
fvvYXVideo.setPreviewMirror(true);

//设置推流镜像 setVideoMirror(Boolean mirror)
//mirror 推流镜像
fvvYXVideo.setVideoMirror(true);

//------------------缩放---------------------

//获取当前摄像头缩放比例 int getCameraZoomValue()
int zoom = fvvYXVideo.getCameraZoomValue();

//获取摄像头支持最大的缩放比例 int getCameraMaxZoomValue()
int zoomMax = fvvYXVideo.getCameraMaxZoomValue();

//设置摄像头缩放比例 setCameraZoomPara(int zoomValue)
//zoomValue 缩放比例，默认0
fvvYXVideo.setCameraZoomPara(0);

//------------------曝光---------------------

//获取镜头曝光强度 int getExposureCompensation()
int exposure = fvvYXVideo.getExposureCompensation();

//获取摄像头支持的最小曝光强度 int getMinExposureCompensation()
int exposureMin = fvvYXVideo.getMinExposureCompensation();

//获取摄像头支持的最大曝光强度 int getMaxExposureCompensation()
int exposureMax = fvvYXVideo.getMaxExposureCompensation();

//设置摄像头曝光强度 setExposureCompensation(int exposure)
//exposure 设置摄像头曝光强度
fvvYXVideo.setExposureCompensation(0);
```
#### 伴音相关：
```java
//播放伴音 startPlayMusic(String musicURL,boolean loop)
//musicURL 音频文件地址/文件名
//loop 是否循环
fvvYXVideo.startPlayMusic("/sdcard/test.mp3",true);

//结束播放伴音文件 stopPlayMusic()
fvvYXVideo.stopPlayMusic();

//暂停播放伴音文件 pausePlayMusic()
fvvYXVideo.pausePlayMusic();

//继续播放伴音文件 resumePlayMusic()
fvvYXVideo.resumePlayMusic();
```

#### 水印相关：
```java
//添加水印 addWaterMark(String path,int w,int h,int x,int y)
//path 本地水印文件路径
//w 宽度，0为自适应
//h 高度，0为自适应
//x 距离屏幕左边距离
//y 距离屏幕上边距离
fvvYXVideo.addWaterMark("/sdcard/1.png",0,0,0,0);

//移除水印 removeWaterMark()
fvvYXVideo.removeWaterMark();

//添加GIF水印 addWaterMark(String path,int w,int h,int x,int y,int fps)
//path 本地水印文件路径
//w 宽度，0为自适应
//h 高度，0为自适应
//x 距离屏幕左边距离
//y 距离屏幕上边距离
//fps 一秒多少帧，0为1秒播完
fvvYXVideo.addWaterMarkGif("/sdcard/1.gif",0,0,0,0,0);

//移除GIF水印 removeWaterMarkGif()
fvvYXVideo.removeWaterMarkGif();

//本地是否显示水印 setWaterPreview(Boolean bool)
//bool 本地是否显示水印，默认true
fvvYXVideo.setWaterPreview(false);

//本地是否显示GIF水印 setDynamicWaterPreview(Boolean bool)
//bool 本地是否显示GIF水印，默认true
fvvYXVideo.setDynamicWaterPreview(false);
```
#### 屏幕共享：
```java
//试了好像没用，不知道是什么问题
 @RequiresApi(api = Build.VERSION_CODES.LOLLIPOP)
public void ScreenShare(View view)  {	//点击事件，申请权限
	MediaProjectionManager mediaProjectionManager = (MediaProjectionManager) getSystemService(Context.MEDIA_PROJECTION_SERVICE);
	Intent captureIntent = mediaProjectionManager.createScreenCaptureIntent();
	startActivityForResult(captureIntent, 11);
}


@Override
protected void onActivityResult(int requestCode, int resultCode, Intent data) {
	super.onActivityResult(requestCode, resultCode, data);

	if (requestCode == 11) {
		if (resultCode != Activity.RESULT_OK || data == null) {
			Log.i("test","你拒绝了录屏请求");
			return;
		}
		fvvYXVideo.startScreenCapture(data);
	}
}
```
#### 其他杂项：
```java
//发送自定义消息 sendCustomData(JSONObject jsonObject)
JSONObject jsonObject = new JSONObject();
jsonObject.put("test","1111");
fvvYXVideo.sendCustomData(jsonObject);

//检查权限 checkPublishPermission()
fvvYXVideo.checkPublishPermission();
```

