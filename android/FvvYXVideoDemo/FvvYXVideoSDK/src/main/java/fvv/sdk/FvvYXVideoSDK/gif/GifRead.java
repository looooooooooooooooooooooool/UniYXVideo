package fvv.sdk.FvvYXVideoSDK.gif;


import android.graphics.Bitmap;
import android.graphics.Matrix;
import android.util.Log;

import java.io.FileInputStream;
import java.io.FileNotFoundException;

import javax.security.auth.callback.Callback;

public class GifRead  {

    private GifDecoder gifDecoder;
    private Bitmap[] bitmaps = new Bitmap[0];
    private GifCallback mGifCallback;

    public void GetFile(String path, final int w , final int h , GifCallback callback){
        FileInputStream fis = null;
        mGifCallback = callback;
        try {
            fis = new FileInputStream(path);
            gifDecoder = new GifDecoder(fis, new GifDecoderAction() {
                @Override
                public void parseOk(boolean parseStatus, int frameIndex) {
                    if(frameIndex > 0){
                        return;
                    }
                    bitmaps = new Bitmap[gifDecoder.getFrameCount()];
                    if(bitmaps.length <= 0){
                        mGifCallback.Callback(bitmaps);
                        return;
                    }
                    for(int i = 0; i < gifDecoder.getFrameCount(); i++){
                        bitmaps[i] = gifDecoder.getFrameImage(i);
                        if(w != 0 && h != 0){
                            bitmaps[i] = formatBitmap(bitmaps[i],w,h);
                        }
                    }
                    mGifCallback.Callback(bitmaps);
                    gifDecoder.free();
                    gifDecoder = null;
                }
            });
            gifDecoder.start();
        } catch (FileNotFoundException e) {
            mGifCallback.Callback(bitmaps);
        } catch (Exception e){
            mGifCallback.Callback(bitmaps);
        }
    }

    public static Bitmap formatBitmap(Bitmap bitmap,int w,int h){
        int width = bitmap.getWidth();
        int height = bitmap.getHeight();

        //设置想要的大小
        float newWidth = w == 0?(float)h/height * width:w;
        float newHeight = h == 0?(float)w/width * height:h;

        //计算压缩的比率
        float scaleWidth= newWidth / width;
        float scaleHeight= newHeight / height;

        //获取想要缩放的matrix
        Matrix matrix = new Matrix();
        matrix.postScale(scaleWidth,scaleHeight);

        return Bitmap.createBitmap(bitmap,0,0,width,height,matrix,true);
    }
}

