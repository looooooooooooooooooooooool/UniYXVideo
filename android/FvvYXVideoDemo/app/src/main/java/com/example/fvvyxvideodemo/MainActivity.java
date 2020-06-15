package com.example.fvvyxvideodemo;

import androidx.annotation.RequiresApi;
import androidx.appcompat.app.AppCompatActivity;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.graphics.Bitmap;
import android.media.projection.MediaProjectionManager;
import android.os.Build;
import android.os.Bundle;
import android.util.Log;
import android.view.View;

import fvv.sdk.FvvYXVideoSDK.FvvYXVideo;
import fvv.sdk.FvvYXVideoSDK.MessageHandle;


public class MainActivity  extends AppCompatActivity {

    private FvvYXVideo fvvYXVideo;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        fvvYXVideo  = new FvvYXVideo(MainActivity.this, new MessageHandle() {
            @Override
            public void Callback(int i, Object o) {
                switch (i){
                    //停止推流回调
                    case 25:
                        //o为录制的文件路径
                        break;
                    //截图回调
                    case 37:
                        o = fvvYXVideo.saveBitmap((Bitmap) o);
                        break;
                }
                Log.i("callback : ", String.valueOf(i) + " : " + (o==null?"":o.toString()));
            }
        });
        Boolean test = fvvYXVideo.checkPublishPermission();
        Log.i("Permission",String.valueOf(test));

        fvvYXVideo.setPreview(findViewById(R.id.videoview));
        fvvYXVideo.mPublishParam.pushUrl = "rtmp://your rtmp url";
        fvvYXVideo.mPublishParam.recordPath = "/sdcard/1.mp4";
        //fvvYXVideo.mPublishParam.setFormatType("mp4");
        //fvvYXVideo.mPublishParam.setVideoQuality("MEDIUM");
        fvvYXVideo.startPreview();
    }

    public void start(View view){
        fvvYXVideo.mPublishParam.qosEnable = false;
        fvvYXVideo.startStream();

    }
    public void stop(View view){
        fvvYXVideo.destroy();
    }

    public void watermark(View view){
        fvvYXVideo.addWaterMarkGif("/sdcard/1.gif",0,0,0,0,0);
    }


    public void check(View view){
        Log.i("test",String.valueOf(fvvYXVideo.checkPublishPermission()));
    }


    @RequiresApi(api = Build.VERSION_CODES.LOLLIPOP)
    public void ScreenShare(View view)  {
        MediaProjectionManager mediaProjectionManager = (MediaProjectionManager) getSystemService(Context.MEDIA_PROJECTION_SERVICE);
        Intent captureIntent = mediaProjectionManager.createScreenCaptureIntent();
        startActivityForResult(captureIntent, 11);
    }


    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);

        Log.i("test code",String.valueOf(requestCode));
        if (requestCode == 11) {
            if (resultCode != Activity.RESULT_OK || data == null) {
                Log.i("test","你拒绝了录屏请求");
                return;
            }
            fvvYXVideo.startScreenCapture(data);
        }
    }
}
