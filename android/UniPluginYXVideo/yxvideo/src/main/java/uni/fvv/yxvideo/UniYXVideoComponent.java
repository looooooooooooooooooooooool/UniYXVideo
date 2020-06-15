package uni.fvv.yxvideo;

import android.content.Context;
import android.graphics.Bitmap;
import android.util.Log;
import android.widget.Toast;

import androidx.annotation.NonNull;

import com.alibaba.fastjson.JSONObject;
import com.netease.vcloud.video.render.NeteaseView;
import com.taobao.weex.WXSDKInstance;
import com.taobao.weex.annotation.JSMethod;
import com.taobao.weex.bridge.JSCallback;
import com.taobao.weex.ui.action.BasicComponentData;
import com.taobao.weex.ui.component.WXComponent;
import com.taobao.weex.ui.component.WXVContainer;

import fvv.sdk.FvvYXVideoSDK.FvvYXVideo;
import fvv.sdk.FvvYXVideoSDK.MessageHandle;


public class UniYXVideoComponent extends WXComponent<NeteaseView>  {

    private FvvYXVideo fvvYXVideo;
    private WXSDKInstance mWXSDKInstance;
    private JSCallback messageHandle;

    public UniYXVideoComponent(WXSDKInstance instance, WXVContainer parent, BasicComponentData basicComponentData) {
        super(instance, parent, basicComponentData);
        mWXSDKInstance = instance;
    }


    @Override
    protected NeteaseView initComponentHostView(@NonNull Context context) {
        return new NeteaseView(context);
    }

    @JSMethod
    public void test() {
        Toast.makeText(getContext(),"test" ,Toast.LENGTH_LONG).show();
    }


    @JSMethod    //初始化预览界面
    public void init(final JSONObject options, final JSCallback callback){
        messageHandle = callback;
        fvvYXVideo = new FvvYXVideo(getContext().getApplicationContext(),new MessageHandle() {
            @Override
            public void Callback(int i, Object o) {
                Log.i("callback : ", String.valueOf(i) + " : " + (o==null?"":o.toString()));
                if(i == 37){
                    o = fvvYXVideo.saveBitmap((Bitmap) o);
                }
                JSONObject data = new JSONObject();
                data.put("code",i);
                data.put("data",o);
                callback.invokeAndKeepAlive(data);
            }
        });

        fvvYXVideo.mPublishParam.uploadLog = SetValue(options,"uploadLog",false);
        fvvYXVideo.mPublishParam.frontCamera = SetValue(options,"frontCamera",false);
        fvvYXVideo.mPublishParam.isScale_16x9 = SetValue(options,"isScale_16x9",false);
        fvvYXVideo.mPublishParam.useFilter = SetValue(options,"useFilter",true);
        fvvYXVideo.mPublishParam.zoom = SetValue(options,"zoom",true);
        fvvYXVideo.mPublishParam.setVideoQuality(SetValue(options,"videoQuality","HIGH"));

        fvvYXVideo.setPreview(getHostView());
        fvvYXVideo.startPreview();
    }

    @JSMethod //关闭预览
    public void stopPreview(){
        fvvYXVideo.stopPreview();
    }

    //------------------------------------推流录像-----------------------------------

    @JSMethod   //开始推流
    public void startStream(JSONObject options) {
        fvvYXVideo.mPublishParam.pushUrl = SetValue(options,"url","rtmp://127.0.0.1");
        fvvYXVideo.mPublishParam.recordPath = SetValue(options,"save","/sdcard/fvv.mp4");
        fvvYXVideo.mPublishParam.setStreamType(SetValue(options,"streamType","AV"));
        fvvYXVideo.mPublishParam.setFormatType(SetValue(options,"formatType","RTMP_AND_MP4"));

        fvvYXVideo.startStream();
    }

    @JSMethod   //暂停视频推流
    public void pauseVideoLiveStream() {
        fvvYXVideo.pauseVideoLiveStream();
    }

    @JSMethod   //恢复视频推流
    public void resumeVideoLiveStream() {
        fvvYXVideo.resumeVideoLiveStream();
    }

    @JSMethod   //停止推流
    public void stopStream() {
        fvvYXVideo.stopStream();
    }

    @JSMethod   //截图
    public void screenShot(String savePath) {
        fvvYXVideo.screenShot(savePath == null?"":savePath);
    }

    //------------------------------------滤镜相关-----------------------------------

    @JSMethod   //滤镜
    public void setFilterType(String filterType) {
        fvvYXVideo.setFilterType(filterType == null?"none":filterType);
    }

    @JSMethod   //滤镜强度
    public void setFilterStrength(Integer i) {
        fvvYXVideo.setFilterStrength(i == null?0:i);
    }

    @JSMethod   //磨皮强度
    public void setBeautyLevel(Integer i) {
        fvvYXVideo.setBeautyLevel(i == null?0:i);
    }

    //------------------------------------镜头相关-----------------------------------

    @JSMethod   //闪光灯
    public void setCameraFlashPara(Boolean b) {
        fvvYXVideo.setCameraFlashPara(b == null?false:b);
    }

    @JSMethod   //手动对焦
    public void setCameraFocus() {
        fvvYXVideo.setCameraFocus();
    }

    @JSMethod   //自动对焦
    public void setCameraAutoFocus(Boolean b) {
        fvvYXVideo.setCameraAutoFocus(b == null?true:b);
    }

    @JSMethod   //切换摄像头
    public void switchCamera() {
        fvvYXVideo.switchCamera();
    }

    @JSMethod   //切换分辨率
    public void changeCaptureFormat(JSONObject jsonObject) {
        fvvYXVideo.mPublishParam.isScale_16x9 = SetValue(jsonObject,"isScale_16x9",false);
        fvvYXVideo.mPublishParam.setVideoQuality(SetValue(jsonObject,"videoQuality","HIGH"));
        fvvYXVideo.changeCaptureFormat();
    }

    @JSMethod   //本地预览镜像
    public void setPreviewMirror(Boolean b) {
        fvvYXVideo.setPreviewMirror(b == null?true:b);
    }

    @JSMethod   //推流镜像
    public void setVideoMirror(Boolean b) {
        fvvYXVideo.setVideoMirror(b == null?true:b);
    }

    //------------------------------------缩放相关-----------------------------------
    @JSMethod   //获取当前缩放比例
    public void getCameraZoomValue() {
        messageHandle.invokeAndKeepAlive(SetCallback(300,fvvYXVideo.getCameraZoomValue()));
    }

    @JSMethod    //获取支持最大缩放比例
    public void getCameraMaxZoomValue() {
        messageHandle.invokeAndKeepAlive(SetCallback(301,fvvYXVideo.getCameraMaxZoomValue()));
    }

    @JSMethod   //设置缩放
    public void setCameraZoomPara(Integer zoom) {
        fvvYXVideo.setCameraZoomPara(zoom == null?0:zoom);
    }

    //------------------------------------曝光相关-----------------------------------
    @JSMethod   //获取当前镜头曝光比例
    public void getExposureCompensation() {
        messageHandle.invokeAndKeepAlive(SetCallback(302,fvvYXVideo.getExposureCompensation()));
    }

    @JSMethod   //获取支持最小曝光比例
    public void getMinExposureCompensation() {
        messageHandle.invokeAndKeepAlive(SetCallback(303,fvvYXVideo.getMinExposureCompensation()));
    }

    @JSMethod   //获取支持最大曝光比例
    public void getMaxExposureCompensation() {
        messageHandle.invokeAndKeepAlive(SetCallback(304,fvvYXVideo.getMaxExposureCompensation()));
    }

    @JSMethod   //设置镜头曝光
    public void setExposureCompensation(Integer exposure) {
        fvvYXVideo.setExposureCompensation(exposure == null?0:exposure);
    }

    //------------------------------------伴音相关-----------------------------------
    @JSMethod   //播放伴音
    public void startPlayMusic(JSONObject jsonObject) {
         String path = SetValue(jsonObject,"path","/sdcard/fvv.mp3");
         Boolean loop = SetValue(jsonObject,"loop",false);
         fvvYXVideo.startPlayMusic(path,loop);
    }

    @JSMethod   //结束伴音
    public void stopPlayMusic() {
        fvvYXVideo.stopPlayMusic();
    }

    @JSMethod   //暂停伴音
    public void pausePlayMusic() {
        fvvYXVideo.pausePlayMusic();
    }

    @JSMethod   //继续伴音
    public void resumePlayMusic() {
        fvvYXVideo.resumePlayMusic();
    }

    //------------------------------------水印相关-----------------------------------
    @JSMethod  //添加水印
    public void watermark(JSONObject jsonObject)  {
        String path = SetValue(jsonObject,"path","/sdcard/Download/fvv.png");
        int w = SetValue(jsonObject,"w",0);
        int h = SetValue(jsonObject,"h",0);
        int x = SetValue(jsonObject,"x",0);
        int y = SetValue(jsonObject,"y",0);
        fvvYXVideo.addWaterMark(path,w,h,x,y);
    }

    @JSMethod  //移除水印
    public void removeWatermark()  {
        fvvYXVideo.removeWaterMark();
    }

    @JSMethod  //添加gif水印
    public void watermarkGif(JSONObject jsonObject) {
        String path = SetValue(jsonObject,"path","/sdcard/Download/fvv.png");
        int w = SetValue(jsonObject,"w",0);
        int h = SetValue(jsonObject,"h",0);
        int x = SetValue(jsonObject,"x",0);
        int y = SetValue(jsonObject,"y",0);
        int fps = SetValue(jsonObject,"fps",0);
        fvvYXVideo.addWaterMarkGif(path,w,h,x,y,fps);
    }

    @JSMethod  //移除gif水印
    public void removeWaterMarkGif()  {
        fvvYXVideo.removeWaterMarkGif();
    }

    @JSMethod   //本地是否显示水印
    public void setWaterPreview(Boolean b)  {
        fvvYXVideo.setWaterPreview(b == null?true:b);
    }

    @JSMethod   //本地是否显示gif水印
    public void setDynamicWaterPreview(Boolean b)  {
        fvvYXVideo.setDynamicWaterPreview(b == null?true:b);
    }

    //------------------------------------其他杂项-----------------------------------
    @JSMethod   //检查权限
    public void checkPublishPermission()  {
        messageHandle.invokeAndKeepAlive(SetCallback(202,fvvYXVideo.checkPublishPermission()));
    }


    public JSONObject SetCallback(int i ,Object o){
        JSONObject data = new JSONObject();
        data.put("code",i);
        data.put("data",o);
        return data;
    }

    public int SetValue(JSONObject object,String key,int defaultValue){
        return object.containsKey(key)?object.getInteger(key):defaultValue;
    }
    public String SetValue(JSONObject object,String key,String defaultValue){
        return object.containsKey(key)?object.getString(key):defaultValue;
    }
    public Boolean SetValue(JSONObject object,String key,Boolean defaultValue){
        return object.containsKey(key)?object.getBoolean(key):defaultValue;
    }


    @Override
    public void onActivityResume() {
        super.onActivityResume();
    }

    @Override
    public void onActivityPause() {
        super.onActivityPause();
    }

    @Override
    public void onActivityDestroy() {
        super.onActivityDestroy();
        fvvYXVideo.destroy();
        this.destroy();
        Log.i("destroy","destroy-----------------");
    }
}
