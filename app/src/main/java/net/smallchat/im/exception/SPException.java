package net.smallchat.im.exception;



import android.content.Context;
import android.os.Handler;
import android.widget.Toast;

public class SPException extends Exception{
    private static final long serialVersionUID = -5333745547651166329L;
    int stringId = 0;
    String msg = null;
    
    public SPException(int id) {
        super();
        this.stringId = id;
    }
    
    public int getExceptionStringId(){
        return this.stringId;
    }
    
    public SPException(String msg){
        super(msg);
        this.msg = msg;
    }
    
    public void showExceptionByToast(final Context context, Handler handler){
        handler.post(new Runnable(){
            @Override
            public void run() {
                String text = (msg != null) ? msg : context.getString(stringId);
                Toast.makeText(context, text, Toast.LENGTH_SHORT).show();
            }
        });        
    }
}
