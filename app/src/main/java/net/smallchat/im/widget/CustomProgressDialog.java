package net.smallchat.im.widget;

import android.app.Dialog;
import android.content.Context;
import android.view.Gravity;
import android.widget.TextView;

import net.smallchat.im.R;

public class CustomProgressDialog extends Dialog {
	
	private TextView mMessageView;
	
	public CustomProgressDialog(Context context){
		this(context, "");
	}
	
	public CustomProgressDialog(Context context, String strMessage) {  
        this(context, R.style.loading_dialog, strMessage);  
    }  
	
	public CustomProgressDialog(Context context, int theme, String strMessage) {  
        super(context, theme);  
        this.setContentView(R.layout.custom_progress_dialog);  
        this.getWindow().getAttributes().gravity = Gravity.CENTER;  
        mMessageView = (TextView) this.findViewById(R.id.tipTextView);  
        if ( mMessageView != null) {  
        	 mMessageView.setText(strMessage);  
        }  
    }  
  
	public void setMessage(String message){
		mMessageView.setText(message);
	}
	
    @Override  
    public void onWindowFocusChanged(boolean hasFocus) {  
  
        if (!hasFocus) {  
            dismiss();  
        }  
    }  
}
