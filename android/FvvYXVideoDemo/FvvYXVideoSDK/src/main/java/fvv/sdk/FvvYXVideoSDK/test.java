package fvv.sdk.FvvYXVideoSDK;

import android.app.Application;
import android.content.Context;

public class test extends Application {
    private static Context context;
    @Override

    public void onCreate() {
        super.onCreate();
        context = getApplicationContext();
    }
    /**
      * 获取全局上下文*/
   public static Context getContext() {
      return context;
   }


}
