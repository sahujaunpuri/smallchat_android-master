package net.smallchat.im;

import java.lang.reflect.Field;
import java.util.ArrayList;
import java.util.List;

import android.app.Activity;
import android.content.Context;
import android.content.SharedPreferences;
import android.content.SharedPreferences.Editor;
import android.os.Bundle;
import android.util.Log;
import android.view.KeyEvent;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.Window;
import android.view.WindowManager;
import android.widget.ImageView;
import android.widget.ImageView.ScaleType;
import android.widget.LinearLayout.LayoutParams;

import net.smallchat.im.R.drawable;
import net.smallchat.im.adapter.ViewPagerAdapter;
import net.smallchat.im.global.FeatureFunction;
import net.smallchat.im.global.IMCommon;
import net.smallchat.im.widget.ViewPager;
import net.smallchat.im.widget.ViewPager.OnPageChangeListener;
/**
 * 新功能简介
 * @author dongli
 *
 */
public class GuideActivity extends Activity implements OnPageChangeListener{
	private Context mContext;
	private ViewPager mViewPager;
	private String[] imgArray;
	private int mPosition = 0;

	/*
	 * 导入控件
	 * (non-Javadoc)
	 * @see android.app.Activity#onCreate(android.os.Bundle)
	 */
    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        mContext = this;
        requestWindowFeature(Window.FEATURE_NO_TITLE);
		getWindow().setFlags(WindowManager.LayoutParams.FLAG_FULLSCREEN, WindowManager.LayoutParams.FLAG_FULLSCREEN);
        setContentView(R.layout.function_layout);
        imgArray = getResources().getStringArray(R.array.ch_function_array);
        this.initData();
        
        saveGuideVersion();
    }
    
    /*
     * 保存版本号信息
     */
    private void saveGuideVersion(){
    	SharedPreferences preferences = this.getSharedPreferences(IMCommon.SHOWGUDIEVERSION, 0);
    	Editor editor = preferences.edit();
    	editor.putString("app_version", FeatureFunction.getAppVersionName(mContext));
    	editor.commit();
    }
    
    /**
     * 销毁页面
     */
    private void goActivity() {
    	setResult(RESULT_OK);
    	GuideActivity.this.finish();
	}
    
    /*
     * 初始化页面数据
     */
    private void initData() {
		LayoutParams mParams = new LayoutParams(LayoutParams.FILL_PARENT, LayoutParams.FILL_PARENT);
		List<View> views = new ArrayList<View>();
		for (int i = 0; i < imgArray.length; i++) {
			ImageView iv = new ImageView(this);
			iv.setLayoutParams(mParams);
			iv.setImageResource(getResourceByReflect(imgArray[i]));
			iv.setScaleType(ScaleType.FIT_XY);
			views.add(iv);
			if (i == imgArray.length - 1) { //在最后一页上点击图片触发的监听，如果不需要则不用编写此监听
				iv.setOnClickListener(new OnClickListener() {
					@Override
					public void onClick(View v) {
						goActivity();
					}
				});
			}
		}
		mViewPager = (ViewPager) findViewById(R.id.viewpager);
		ViewPagerAdapter adpter= new ViewPagerAdapter(views);
		mViewPager.setAdapter(adpter);
		mViewPager.setCurrentItem(mPosition);
		mViewPager.setOnPageChangeListener(this);
	}
    
    /*
     * 获取页面数据
     */
    public int getResourceByReflect(String imageName){   
		Class<drawable> drawable = R.drawable.class;
		Field field = null;
		int r_id;
		try {
			field = drawable.getField(imageName);
			r_id = field.getInt(field.getName());
		} catch (Exception e) {
			r_id = R.drawable.ch_guide_1;
			//这段代码主要是防止字段被Android studio自动删除。
			int guide1=R.drawable.ch_guide_1;
			int guide2=R.drawable.ch_guide_2;
			int guide3=R.drawable.ch_guide_3;
			int guide4=R.drawable.ch_guide_4;
			Log.e("ERROR", "PICTURE NOT　FOUND！");
		}
		return r_id;
	} 
	
	@Override
	public boolean dispatchKeyEvent(KeyEvent event) {
		if (event.getAction() == KeyEvent.ACTION_DOWN
				&& event.getKeyCode() == KeyEvent.KEYCODE_BACK) {
			return true;
		}
		return super.dispatchKeyEvent(event);
	}

	@Override
	public void onPageScrolled(int position, float paramFloat, int paramInt2) {
		
	}

	@Override
	public void onPageSelected(int position) {
		mPosition = position;
	}

	@Override
	public void onPageScrollStateChanged(int state) {
		if(mViewPager.getRelationX() > 0 && mPosition == imgArray.length - 1){
			goActivity();
		}
	}
}
