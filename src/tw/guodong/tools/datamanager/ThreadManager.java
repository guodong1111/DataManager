package tw.guodong.tools.datamanager;

import android.content.Context;
import android.os.Handler;
import android.os.HandlerThread;

/**
 * Created by USER on 2015/3/3.
 */
public class ThreadManager {
    private static ThreadManager instance = null;
    private Handler mUI_Handler,mThreadHandler;
    private HandlerThread mThread;
    private ThreadManager(Context context){
        mUI_Handler = new Handler(context.getMainLooper());
        mThread = new HandlerThread("backgroundThread");
        mThread.start();
        mThreadHandler = new Handler(mThread.getLooper());
    }
    synchronized static public ThreadManager getInstance(Context context) {
        if (instance == null) {
            instance = new ThreadManager(context);
        }
        return instance;
    }

    public void postToUIThread(Runnable runnable){
        mUI_Handler.post(runnable);
    }

    public void postToBackgroungThread(Runnable runnable){
        mThreadHandler.post(runnable);
    }

    public void destroy(){
        mThread.quit();
        instance = null;
    }
}
