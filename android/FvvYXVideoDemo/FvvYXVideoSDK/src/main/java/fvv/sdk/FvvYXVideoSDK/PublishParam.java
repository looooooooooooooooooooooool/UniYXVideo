package fvv.sdk.FvvYXVideoSDK;

import android.util.Log;

import com.netease.LSMediaCapture.lsMediaCapture;

public class PublishParam {

    public String pushUrl = null; //推流地址
    public lsMediaCapture.StreamType streamType = lsMediaCapture.StreamType.AV;  // 推流类型
    public lsMediaCapture.FormatType formatType = lsMediaCapture.FormatType.RTMP_AND_MP4; // 推流格式
    public String recordPath; //文件录制地址，当formatType 为 MP4 或 RTMP_AND_MP4 时有效
    public String screenShotPath; //截图保存地址
    public lsMediaCapture.VideoQuality videoQuality = lsMediaCapture.VideoQuality.HIGH; //清晰度

    public boolean isScale_16x9 = false; //是否强制16:9
    public boolean frontCamera = false; //是否默认前置摄像头
    public boolean uploadLog = false; //是否上传SDK运行日志

    public boolean useFilter = true; //是否使用滤镜
    public boolean zoom = true; //是否使用滤镜

    public boolean qosEnable = true;  //是否开启QOS
    public int qosEncodeMode = 1; // 1:流畅优先, 2:清晰优先

    //设置推流类型
    public void setStreamType(String type){
        switch (type.toUpperCase()){
            case "AUDIO":
                streamType = lsMediaCapture.StreamType.AUDIO;
                break;
            case "VIDEO":
                streamType = lsMediaCapture.StreamType.VIDEO;
                break;
            default:
                streamType = lsMediaCapture.StreamType.AV;
                break;
        }
    }

    //设置格式
    public void setFormatType(String type){
        switch (type.toUpperCase()){
            case "MP4":
                formatType = lsMediaCapture.FormatType.MP4;
                break;
            case "RTMP":
                formatType = lsMediaCapture.FormatType.RTMP;
                break;
            default:
                formatType = lsMediaCapture.FormatType.RTMP_AND_MP4;
                break;
        }
    }

    //设置视频清晰度
    public void setVideoQuality(String quality){
        switch (quality.toUpperCase()){
            case "MEDIUM":
                videoQuality = lsMediaCapture.VideoQuality.MEDIUM;
                break;
            case "SUPER":
                videoQuality = lsMediaCapture.VideoQuality.SUPER;
                break;
            case "SUPER_HIGH":
                videoQuality = lsMediaCapture.VideoQuality.SUPER_HIGH;
                break;
            case "HD1080P":
                videoQuality = lsMediaCapture.VideoQuality.HD1080P;
                break;
            default:
                videoQuality = lsMediaCapture.VideoQuality.HIGH;
                break;
        }
    }
}
