package net.smallchat.im;

import android.os.Bundle;
import android.view.View;
import android.view.View.OnClickListener;
import android.webkit.WebChromeClient;
import android.webkit.WebView;

/**
 * 音乐详情
 * @author dl
 *
 */
public class MusicActivity extends BaseActivity implements OnClickListener{
	
	private WebView musicView;
	private String mMusicUrl;
	
	
	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.music_view);
		mMusicUrl = getIntent().getStringExtra("musicurl");
		mContext = this;
		initCompnet();
	}

	private void initCompnet(){
		setTitleContent(R.drawable.back_btn,0, R.string.music);
		mLeftBtn.setOnClickListener(this);
		musicView = (WebView) findViewById(R.id.webView);
		musicView.loadUrl(mMusicUrl);
		musicView.getSettings().setJavaScriptEnabled(true);
		//musicView.getSettings().setPluginState(true);
		musicView.setWebChromeClient(new WebChromeClient());
	}

	@Override
	public void onClick(View v) {
		super.onClick(v);
		switch (v.getId()) {
		case R.id.left_btn:
			MusicActivity.this.finish();
			break;

		default:
			break;
		}
	}

	@Override
	protected void onDestroy() {
		super.onDestroy();
		if(musicView!=null){
			musicView.removeAllViews();
			musicView.destroy();
		}
	}
	
	
}
