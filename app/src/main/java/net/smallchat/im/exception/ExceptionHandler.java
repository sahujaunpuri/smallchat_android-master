package net.smallchat.im.exception;

import java.lang.Thread.UncaughtExceptionHandler;

import android.app.Activity;
import android.content.Context;
import android.os.Looper;
import android.util.Log;

public class ExceptionHandler implements UncaughtExceptionHandler{

	private static final String TAG = "winxuanException";
	private Context mContext;

	public ExceptionHandler(Context context) {
		mContext = context;
	}

	@Override
	public void uncaughtException(Thread thread, Throwable ex) {
		Thread.dumpStack();
		ex.printStackTrace();
		if(!handleException(ex)){
		    Thread.getDefaultUncaughtExceptionHandler().uncaughtException(thread, ex);
		}else{
			try {  
                Thread.sleep(1500);  
            } catch (InterruptedException e) {  
                Log.e(TAG, "Error : ", e);  
            }  
            //android.os.Process.killProcess(android.os.Process.myPid());  
            ((Activity)mContext).finish();
            System.exit(0);
		}
	}
	
	 private boolean handleException(Throwable ex) {  
		 Log.e("before", "handleException");
		    if (ex == null) {  
	            return true;  
	        }  
	        new Thread() {  
	            @Override  
	            public void run() {  
	                Looper.prepare();  
	                Log.e("center", "handleException");
	                //Toast.makeText(mContext, mContext.getString(R.string.uncaught_exception), Toast.LENGTH_LONG).show();
	                Looper.loop();  
	            }  
	  
	        }.start();  
	        Log.e("after", "handleException");
	        return true;  
		}  
}
