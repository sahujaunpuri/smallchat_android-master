package net.smallchat.im.widget;



import android.app.Activity;
import android.content.Context;
import android.graphics.drawable.ColorDrawable;
import android.view.LayoutInflater;
import android.view.MotionEvent;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.View.OnTouchListener;
import android.view.ViewGroup.LayoutParams;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.PopupWindow;
import android.widget.TextView;

import net.smallchat.im.R;
import net.smallchat.im.Entity.Login;
import net.smallchat.im.global.ImageLoader;
import net.smallchat.im.global.IMCommon;


public class SelectPicPopupWindow extends PopupWindow {


	//private Button  btn_cancel;
	private View mMenuView;
	private LinearLayout my_profile,my_photo,my_collection,my_setting,my_feedback;
	private ImageLoader mImageLoader;

	public SelectPicPopupWindow(final Activity context,final OnClickListener itemsOnClick) {
		super(context);
		LayoutInflater inflater = (LayoutInflater) context
				.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
		mMenuView = inflater.inflate(R.layout.bottomdialog, null);

		mImageLoader = new ImageLoader();
		int h = context.getWindowManager().getDefaultDisplay().getHeight();
		int w = context.getWindowManager().getDefaultDisplay().getWidth();
		/*btn_cancel = (Button) mMenuView.findViewById(R.tid.btn_cancel);
		//取锟斤拷钮
		btn_cancel.setOnClickListener(new OnClickListener() {

			public void onClick(View v) {
				//锟斤拷俚锟斤拷锟斤拷锟�				//SaveDate.saveDate(context, new OAuthV2()); 
				
			}
		});*/
		my_profile = (LinearLayout) mMenuView.findViewById(R.id.my_profile);
		ImageView iv=(ImageView)mMenuView.findViewById(R.id.user_icon);
		TextView userName = (TextView)mMenuView.findViewById(R.id.user_name);
		TextView userSign = (TextView)mMenuView.findViewById(R.id.user_sign);
		Login login = IMCommon.getLoginResult(context);
		if(login!=null && login.headSmall!=null && !login.headSmall.equals("")){
			mImageLoader.getBitmap(context, iv,null,login.headSmall,0,false,true);
		}
		
		userName.setText(login.nickname);
		userSign.setText(login.sign);
	
		my_profile.setOnClickListener(new OnClickListener() {

			@Override
			public void onClick(View v) {
				itemsOnClick.onClick(v);
				dismiss();
			}
		});

		my_photo = (LinearLayout) mMenuView.findViewById(R.id.my_photo);
		my_photo.setOnClickListener(new OnClickListener() {
			public void onClick(View v) {
				itemsOnClick.onClick(v);
				dismiss();
			}
		});
		my_collection = (LinearLayout) mMenuView.findViewById(R.id.my_collection);
		my_collection.setOnClickListener(new OnClickListener() {

			@Override
			public void onClick(View v) {
				itemsOnClick.onClick(v);
				dismiss();
			}
		});
		my_setting = (LinearLayout) mMenuView.findViewById(R.id.my_setting);
		my_setting.setOnClickListener(new OnClickListener() {

			@Override
			public void onClick(View v) {
				itemsOnClick.onClick(v);
				dismiss();
			}
		});
		my_feedback = (LinearLayout)mMenuView.findViewById(R.id.my_feedback);
		my_feedback.setOnClickListener(new OnClickListener() {

			@Override
			public void onClick(View v) {
				itemsOnClick.onClick(v);
				dismiss();
			}
		});
		//锟斤拷锟矫帮拷钮锟斤拷锟斤拷
		//锟斤拷锟斤拷SelectPicPopupWindow锟斤拷View
		this.setContentView(mMenuView);
		//锟斤拷锟斤拷SelectPicPopupWindow锟斤拷锟斤拷锟斤拷锟斤拷目锟�		
		this.setWidth(w/2/*+20*/);
		//锟斤拷锟斤拷SelectPicPopupWindow锟斤拷锟斤拷锟斤拷锟斤拷母锟�		
		this.setHeight(LayoutParams.WRAP_CONTENT);
		//锟斤拷锟斤拷SelectPicPopupWindow锟斤拷锟斤拷锟斤拷锟斤拷傻锟斤拷
		this.setFocusable(true);
		//锟斤拷锟斤拷SelectPicPopupWindow锟斤拷锟斤拷锟斤拷锟藉动锟斤拷效锟斤拷
		this.setAnimationStyle(R.style.mystyle);
		//实锟斤拷一锟斤拷ColorDrawable锟斤拷色为锟斤拷透锟斤拷
		ColorDrawable dw = new ColorDrawable(000000);
		//锟斤拷锟斤拷SelectPicPopupWindow锟斤拷锟斤拷锟斤拷锟斤拷谋锟斤拷锟�		
		//this.setBackgroundDrawable(dw);
		this.setBackgroundDrawable(context.getResources().getDrawable(R.drawable.no_top_arrow_bg));
		//mMenuView锟斤拷锟絆nTouchListener锟斤拷锟斤拷锟叫断伙拷取锟斤拷锟斤拷位锟斤拷锟斤拷锟斤拷锟窖★拷锟斤拷锟斤拷锟斤拷锟斤拷锟斤拷俚锟斤拷锟斤拷锟�		
		mMenuView.setOnTouchListener(new OnTouchListener() {

			public boolean onTouch(View v, MotionEvent event) {

				int height = mMenuView.findViewById(R.id.pop_layout).getTop();
				int y=(int) event.getY();
				if(event.getAction()==MotionEvent.ACTION_UP){
					if(y<height){
						dismiss();
					}
				}				
				return true;
			}
		});
	}
}
