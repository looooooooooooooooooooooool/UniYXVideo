package fvv.sdk.FvvYXVideoSDK.gif;

import android.graphics.Bitmap;

// 状态变化监听
public interface GifCallback  {
    // 回调方法
    void Callback(Bitmap[] bitmaps);
}