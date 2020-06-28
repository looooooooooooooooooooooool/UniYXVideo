package fvv.sdk.FvvYXVideoSDK;

import android.Manifest;
import android.app.Activity;
import android.app.AlarmManager;
import android.app.PendingIntent;
import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.os.Build;
import android.util.Log;
import android.view.MotionEvent;
import android.view.View;

import androidx.core.app.ActivityCompat;

import com.netease.LSMediaCapture.lsMediaCapture;
import com.netease.LSMediaCapture.lsMessageHandler;
import com.netease.LSMediaCapture.lsLogUtil;
import com.netease.vcloud.video.effect.VideoEffect;
import com.netease.vcloud.video.render.NeteaseView;

import org.json.JSONObject;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

import fvv.sdk.FvvYXVideoSDK.gif.GifCallback;
import fvv.sdk.FvvYXVideoSDK.gif.GifDecoder;
import fvv.sdk.FvvYXVideoSDK.gif.GifRead;


public class FvvYXVideo {

    public PublishParam mPublishParam;
    private lsMediaCapture mLSMediaCapture;
    private lsMediaCapture.LiveStreamingPara mLiveStreamingPara;
    private lsMediaCapture.LsMediaCapturePara lsMediaCapturePara;
    private NeteaseView mNeteaseView;
    private MessageHandle mMessageHandle;
    private Context mContext;

    //视频缩放相关变量
    private int mMaxZoomValue = 0;
    private int mCurrentZoomValue = 0;
    private float mCurrentDistance;
    private float mLastDistance = -1;

    //gif水印
    private GifDecoder gifDecoder;


    public FvvYXVideo(Context context,MessageHandle messageHandle){
        mMessageHandle = messageHandle;
        mPublishParam = new PublishParam();
        mContext = context;
        lsMediaCapturePara = new lsMediaCapture.LsMediaCapturePara();
        lsMediaCapturePara.setContext(context); //设置SDK上下文（建议使用ApplicationContext）
        lsMediaCapturePara.setMessageHandler(new lsMessageHandler() {
            @Override
            public void handleMessage(int i, Object o) {
                if(i == 25 && checkRecordFile()){
                    o = mPublishParam.recordPath;
                }
                mMessageHandle.Callback(i,o);
            }
        }); //设置SDK消息回调
        lsMediaCapturePara.setLogLevel(lsLogUtil.LogLevel.INFO); //日志级别
        lsMediaCapturePara.setUploadLog(mPublishParam.uploadLog);//是否上传SDK日志
        mLSMediaCapture = new lsMediaCapture(lsMediaCapturePara);
    }

    //销毁
    public void destroy(){
        mLSMediaCapture.stopLiveStreaming();
        mLSMediaCapture.stopVideoPreview();
        mLSMediaCapture.destroyVideoPreview();
        //反初始化推流实例，当它与stopLiveStreaming连续调用时，参数为false
        mLSMediaCapture.uninitLsMediaCapture(false);
        mLSMediaCapture.uninitLsMediaCapture(true);
    }

    //设置预览界面
    public void setPreview(View view){
        view.setOnTouchListener(new View.OnTouchListener() {
            @Override
            public boolean onTouch(View view, MotionEvent motionEvent) {
                mMessageHandle.Callback(201,motionEvent);
                switch (motionEvent.getAction()) {
                    case MotionEvent.ACTION_DOWN:
                        //Log.i(TAG, "test: down!!!");
                        //调用摄像头对焦操作相关API
                        if(mLSMediaCapture != null) {
                            mLSMediaCapture.setCameraFocus();
                        }
                        break;
                    case MotionEvent.ACTION_MOVE:
                        if(!mPublishParam.zoom){
                            return true;
                        }
                        //Log.i(TAG, "test: move!!!");
                        /**
                         * 首先判断按下手指的个数是不是大于两个。
                         * 如果大于两个则执行以下操作（即图片的缩放操作）。
                         */
                        if (motionEvent.getPointerCount() >= 2) {

                            float offsetX = motionEvent.getX(0) - motionEvent.getX(1);
                            float offsetY = motionEvent.getY(0) - motionEvent.getY(1);
                            /**
                             * 原点和滑动后点的距离差
                             */
                            mCurrentDistance = (float) Math.sqrt(offsetX * offsetX + offsetY * offsetY);
                            if (mLastDistance < 0) {
                                mLastDistance = mCurrentDistance;
                            } else {
                                if(mLSMediaCapture != null) {
                                    mMaxZoomValue = mLSMediaCapture.getCameraMaxZoomValue();
                                    mCurrentZoomValue = mLSMediaCapture.getCameraZoomValue();
                                }

                                /**
                                 * 如果当前滑动的距离（currentDistance）比最后一次记录的距离（lastDistance）相比大于5英寸（也可以为其他尺寸），
                                 * 那么现实图片放大
                                 */
                                if (mCurrentDistance - mLastDistance > 5) {
                                    //Log.i(TAG, "test: 放大！！！");
                                    mCurrentZoomValue+=2;
                                    if(mCurrentZoomValue > mMaxZoomValue) {
                                        mCurrentZoomValue = mMaxZoomValue;
                                    }

                                    if(mLSMediaCapture != null) {
                                        mLSMediaCapture.setCameraZoomPara(mCurrentZoomValue);
                                    }

                                    mLastDistance = mCurrentDistance;
                                    /**
                                     * 如果最后的一次记录的距离（lastDistance）与当前的滑动距离（currentDistance）相比小于5英寸，
                                     * 那么图片缩小。
                                     */
                                } else if (mLastDistance - mCurrentDistance > 5) {
                                    //Log.i(TAG, "test: 缩小！！！");
                                    mCurrentZoomValue-=2;
                                    if(mCurrentZoomValue < 0) {
                                        mCurrentZoomValue = 0;
                                    }
                                    if(mLSMediaCapture != null) {
                                        mLSMediaCapture.setCameraZoomPara(mCurrentZoomValue);
                                    }
                                    mLastDistance = mCurrentDistance;
                                }
                            }
                        }
                        break;
                    default:
                        break;
                }
                return true;
            }
        });
        mNeteaseView = (NeteaseView) view;
    }

    //打开预览
    public void startPreview(){
        boolean frontCamera = mPublishParam.frontCamera; // 是否前置摄像头
        boolean mScale_16x9 = mPublishParam.isScale_16x9; //是否强制16:9
        boolean useFilter = mPublishParam.useFilter; //是否使用滤镜

        lsMediaCapture.VideoQuality videoQuality = mPublishParam.videoQuality; //视频模板（SUPER_HIGH 1280*720、SUPER 960*540、HIGH 640*480、MEDIUM 480*360、LOW 352*288）
        try{
            mLSMediaCapture.startVideoPreview(mNeteaseView,frontCamera,useFilter,videoQuality,mScale_16x9);
        }catch (Exception e){
            mMessageHandle.Callback(500,"start camera fail");
        }
    }

    //自定义打开预览
    public void startPreviewEx(int width,int height ,int fps, int bitrate){
        boolean frontCamera = mPublishParam.frontCamera; // 是否前置摄像头
        boolean useFilter = mPublishParam.useFilter; //是否使用滤镜

        lsMediaCapture.VideoPara para = new lsMediaCapture.VideoPara();
        para.setWidth(height);
        para.setHeight(width);
        para.setFps(fps);
        para.setBitrate(bitrate);
        try{
            mLSMediaCapture.startVideoPreviewEx(mNeteaseView,frontCamera,useFilter,para);
        }catch (Exception e){
            mMessageHandle.Callback(500,"start camera fail");
        }
    }


    //关闭预览
    public void stopPreview(){
        mLSMediaCapture.stopVideoPreview();
        mLSMediaCapture.destroyVideoPreview();
    }

    //--------------------------------------推流录像-----------------------------------------

    //打开推流
    public void startStream(){
        mLiveStreamingPara = new lsMediaCapture.LiveStreamingPara();
        mLiveStreamingPara.setStreamType(mPublishParam.streamType); // 推流类型 AV、AUDIO、VIDEO
        mLiveStreamingPara.setFormatType(mPublishParam.formatType); // 推流格式 RTMP、MP4、RTMP_AND_MP4
        mLiveStreamingPara.setRecordPath(mPublishParam.recordPath);//formatType 为 MP4 或 RTMP_AND_MP4 时有效
        mLiveStreamingPara.setQosOn(mPublishParam.qosEnable);
        mLiveStreamingPara.setQosEncodeMode(mPublishParam.qosEncodeMode);

        mLSMediaCapture.initLiveStream(mLiveStreamingPara, mPublishParam.pushUrl);
        mLSMediaCapture.startLiveStreaming();
    }

    //结束推流
    public void stopStream(){
        mLSMediaCapture.stopLiveStreaming();
    }

    //暂停视频推流
    public void pauseVideoLiveStream(){
        mLSMediaCapture.pauseVideoLiveStream();
    }

    //恢复视频推流
    public void resumeVideoLiveStream(){
        mLSMediaCapture.resumeVideoLiveStream();
    }

    //暂停音频推流
    public void pauseAudioLiveStream(){
        mLSMediaCapture.pauseAudioLiveStream();
    }

    //恢复视频推流
    public void resumeAudioLiveStream(){
        mLSMediaCapture.resumeAudioLiveStream();
    }

    //截图
    public void screenShot(String savePath){
        mPublishParam.screenShotPath = savePath;
        mLSMediaCapture.enableScreenShot();
    }

    //保存图片
    public String saveBitmap(Bitmap mBitmap) {
        File filePic;
        if(mPublishParam.screenShotPath == null || mPublishParam.screenShotPath == ""){
            return "";
        }
        try {
            filePic = new File(mPublishParam.screenShotPath);
            if (!filePic.exists()) {
                filePic.getParentFile().mkdirs();
                filePic.createNewFile();
            }
            FileOutputStream fos = new FileOutputStream(filePic);
            mBitmap.compress(Bitmap.CompressFormat.JPEG, 100, fos);
            fos.flush();
            fos.close();
        } catch (IOException e) {
            // TODO Auto-generated catch block
            e.printStackTrace();
            return null;
        }
        return filePic.getAbsolutePath();
    }

    //检查录像文件是否存在
    public Boolean checkRecordFile(){
        try{
            File f = new File(mPublishParam.recordPath);
            if(!f.exists()){
                return false;
            }
        }catch (Exception e) {
            return false;
        }
        return true;
    }

    //--------------------------------------滤镜相关-----------------------------------------

    //设置滤镜
    public void setFilterType(String type){
        if(!mPublishParam.useFilter){
            mMessageHandle.Callback(501,"Preview No Use Filter");
            return;
        }
        switch (type.toUpperCase()){
            case "BROOKLYN":
                mLSMediaCapture.setFilterType(VideoEffect.FilterType.brooklyn);
                break;
            case "CALM":
                mLSMediaCapture.setFilterType(VideoEffect.FilterType.calm);
                break;
            case "CLEAN":
                mLSMediaCapture.setFilterType(VideoEffect.FilterType.clean);
                break;
            case "FAIRYTALE":
                mLSMediaCapture.setFilterType(VideoEffect.FilterType.fairytale);
                break;
            case "NATURE":
                mLSMediaCapture.setFilterType(VideoEffect.FilterType.nature);
                break;
            case "HEALTHY":
                mLSMediaCapture.setFilterType(VideoEffect.FilterType.healthy);
                break;
            case "PIXAR":
                mLSMediaCapture.setFilterType(VideoEffect.FilterType.pixar);
                break;
            case "TENDER":
                mLSMediaCapture.setFilterType(VideoEffect.FilterType.tender);
                break;
            case "WHITEN":
                mLSMediaCapture.setFilterType(VideoEffect.FilterType.whiten);
                break;
            default:
                mLSMediaCapture.setFilterType(VideoEffect.FilterType.none);
                break;
        }
    }

    //设置滤镜强度
    public void setFilterStrength(int i){
        if(!mPublishParam.useFilter){
            mMessageHandle.Callback(501,"Preview Not Use Filter");
            return;
        }
        mLSMediaCapture.setFilterStrength(i / 100);
    }

    //设置磨皮强度
    public void setBeautyLevel(int i){
        if(!mPublishParam.useFilter){
            mMessageHandle.Callback(501,"Preview Not Use Filter");
            return;
        }
        mLSMediaCapture.setBeautyLevel(i / 20);
    }

    //--------------------------------------镜头相关-----------------------------------------

    //闪光灯开关
    public void setCameraFlashPara(Boolean b){
        mLSMediaCapture.setCameraFlashPara(b);
    }

    //获取当前视频缩放比例
    public int getCameraZoomValue(){
        return mLSMediaCapture.getCameraZoomValue();
    }

    //获取摄像头支持的最大缩放比例
    public int getCameraMaxZoomValue(){
        return mLSMediaCapture.getCameraMaxZoomValue();
    }

    //设置摄像头缩放比例
    public void setCameraZoomPara(int zoomValue){
        mLSMediaCapture.setCameraZoomPara(zoomValue);
    }

    //手动对焦一次
    public void setCameraFocus(){
        mLSMediaCapture.setCameraFocus();
    }

    //是否自动对焦，默认true
    public void setCameraAutoFocus(Boolean isAutoFocus){
        mLSMediaCapture.setCameraAutoFocus(isAutoFocus);
    }

    //切换摄像头
    public void switchCamera(){
        mLSMediaCapture.switchCamera();
    }

    //获取镜头曝光强度
    public int getExposureCompensation(){
        return mLSMediaCapture.getExposureCompensation();
    }

    //获取摄像头支持的最小曝光强度
    public int getMinExposureCompensation(){
        return mLSMediaCapture.getMinExposureCompensation();
    }

    //获取摄像头支持的最大曝光强度
    public int getMaxExposureCompensation(){
        return mLSMediaCapture.getMaxExposureCompensation();
    }

    //设置摄像头曝光强度
    public void setExposureCompensation(int value){
        mLSMediaCapture.setExposureCompensation(value);
    }

    //切换分辨率
    public void changeCaptureFormat(){
        mLSMediaCapture.changeCaptureFormat(mPublishParam.videoQuality,mPublishParam.isScale_16x9);
    }


    //切换分辨率自定义
    public void changeCaptureFormatEx(int width,int height ,int fps, int bitrate){
        lsMediaCapture.VideoPara para = new lsMediaCapture.VideoPara();
        para.setWidth(height);
        para.setHeight(width);
        para.setFps(fps);
        para.setBitrate(bitrate);
        mLSMediaCapture.changeCaptureFormatEx(para);
    }

    //预览镜像
    public void setPreviewMirror(boolean mirror){
        mLSMediaCapture.setPreviewMirror(mirror);
    }

    //推流镜像
    public void setVideoMirror(boolean mirror){
        mLSMediaCapture.setVideoMirror(mirror);
    }


    //--------------------------------------伴音相关-----------------------------------------
    //开始播放伴音
    public boolean startPlayMusic(String musicURL,boolean loop){
        return mLSMediaCapture.startPlayMusic(musicURL,loop);
    }
    //结束播放伴音文件
    public boolean stopPlayMusic(){
        return mLSMediaCapture.stopPlayMusic();
    }
    //暂停播放伴音文件
    public boolean pausePlayMusic(){
        return mLSMediaCapture.pausePlayMusic();
    }
    // 继续播放伴音文件
    public boolean resumePlayMusic(){
        return mLSMediaCapture.resumePlayMusic();
    }

    //--------------------------------------水印相关-----------------------------------------

    //读取本地图片为bitmap
    public Bitmap getBitmap(String path){
        Bitmap bitmap = null;
        FileInputStream fis = null;
        try {
            fis = new FileInputStream(path);
            bitmap = BitmapFactory.decodeStream(fis);
        } catch (FileNotFoundException e) {
            return null;
        } catch (Exception e){
            return null;
        }
        return bitmap;
    }

    //添加水印
    public void addWaterMark(String path,int w,int h,int x,int y){
        Bitmap bitmap = getBitmap(path);
        if(bitmap == null){
            mMessageHandle.Callback(502,"read file fail");
            return;
        }
        if(w != 0 && h != 0){
           bitmap = GifRead.formatBitmap(bitmap,w,h);
        }

        mLSMediaCapture.setWaterMarkPara(bitmap,VideoEffect.Rect.leftTop,x,y);
    }

    //添加gif水印
    public void addWaterMarkGif(String path , int w , int h , final int x, final int y, final int fps)  {
        GifRead gifRead = new GifRead();
        gifRead.GetFile(path, w, h, new GifCallback() {
            @Override
            public void Callback(Bitmap[] bitmaps) {
                int tempFps = fps;
                if(bitmaps.length <= 0){
                    mMessageHandle.Callback(502,"read file fail");
                    return;
                }
                if(tempFps == 0){
                    tempFps = bitmaps.length;
                }
                mLSMediaCapture.setDynamicWaterMarkPara(bitmaps, VideoEffect.Rect.leftTop,x, y, tempFps, true);
            }
        });
    }

    //移除水印
    public void removeWaterMark(){
        mLSMediaCapture.setWaterMarkPara(null,VideoEffect.Rect.leftTop,0,0);
    }

    //移除gif水印
    public void removeWaterMarkGif(){
        mLSMediaCapture.setDynamicWaterMarkPara(null, VideoEffect.Rect.leftTop,0, 0, 1, false);
    }

    //本地是否显示水印
    public void setWaterPreview(Boolean bool){
        mLSMediaCapture.setWaterPreview(bool);
    }

    //本地是否显示gif水印
    public void setDynamicWaterPreview(Boolean bool){
        mLSMediaCapture.setDynamicWaterPreview(bool);
    }

    //--------------------------------------屏幕共享-----------------------------------------
    public void startScreenCapture(Intent intent){
        stopPreview();
        mLSMediaCapture.startScreenCapture(mNeteaseView, intent, mPublishParam.videoQuality, mPublishParam.isScale_16x9);
    }

    //--------------------------------------其他操作-----------------------------------------
    //发送自定义消息
    public void sendCustomData(JSONObject jsonObject){
        mLSMediaCapture.updateCustomStatistics(jsonObject);
    }

    //开始测速
    public void startSpeedCalc(long bytes){
        mLSMediaCapture.startSpeedCalc(mPublishParam.pushUrl,bytes);
    }

    //停止测速
    public void stopSpeedCalc(){
        mLSMediaCapture.stopSpeedCalc();
    }

    //--------------------------------------权限相关-----------------------------------------
    //检查权限
    public boolean checkPublishPermission() {
        if (Build.VERSION.SDK_INT >= 23) {
            List<String> permissions = new ArrayList<>();
            if (PackageManager.PERMISSION_GRANTED != ActivityCompat.checkSelfPermission(mContext, Manifest.permission.WRITE_EXTERNAL_STORAGE)) {
                permissions.add(Manifest.permission.WRITE_EXTERNAL_STORAGE);
            }
            if (PackageManager.PERMISSION_GRANTED != ActivityCompat.checkSelfPermission(mContext, Manifest.permission.CAMERA)) {
                permissions.add(Manifest.permission.CAMERA);
            }
            if (PackageManager.PERMISSION_GRANTED != ActivityCompat.checkSelfPermission(mContext, Manifest.permission.RECORD_AUDIO)) {
                permissions.add(Manifest.permission.RECORD_AUDIO);
            }
            if (PackageManager.PERMISSION_GRANTED != ActivityCompat.checkSelfPermission(mContext, Manifest.permission.READ_PHONE_STATE)) {
                permissions.add(Manifest.permission.READ_PHONE_STATE);
            }
            if (permissions.size() != 0) {
                ActivityCompat.requestPermissions((Activity) mContext,(String[]) permissions.toArray(new String[0]),100);
                return false;
            }
        }
        return true;
    }

}

