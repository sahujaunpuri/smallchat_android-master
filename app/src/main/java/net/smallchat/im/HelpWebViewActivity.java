package net.smallchat.im;

import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.view.View;
import android.view.View.OnClickListener;
import android.webkit.WebView;

import net.smallchat.im.global.GlobalParam;
import net.smallchat.im.global.IMCommon;
import net.smallchat.im.api.IMException;

/*
 * 帮助页面
 */
public class HelpWebViewActivity extends BaseActivity implements OnClickListener{

	private WebView mWebView;
	private int mType = 0;
	
	private Handler mHandler = new Handler(){

		@Override
		public void handleMessage(Message msg) {
			super.handleMessage(msg);
			switch (msg.what) {
			case GlobalParam.MSG_SHOW_LOAD_DATA:
				String helpHtml = (String) msg.obj; 
				if(helpHtml!=null && !helpHtml.equals("")){
					mWebView.loadData(helpHtml,"text/html; charset=UTF-8", null);
					//mWebView.loadData(helpHtml, "'text/html'", "GBK");
				}
				break;

			default:
				break;
			}
		}
		
	};
	
	/*
	 * 导入控件
	 * (non-Javadoc)
	 * @see net.smallchat.im.BaseActivity#onCreate(android.os.Bundle)
	 */
	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.help_webview);
		mContext = this;
		initComponent();
	}

	/*
	 * 实例化控件
	 */
	private void initComponent(){

		mType = getIntent().getIntExtra("type", 0);
		setTitleContent(R.drawable.back_btn, 0, 0);
		mLeftBtn.setOnClickListener(this);
		if(mType == 0){
			
			titileTextView.setText(R.string.operation_help);
		}else {
			titileTextView.setText(R.string.help_center);
		}

		mWebView = (WebView) findViewById(R.id.webview);
		
		loadHelpHtml();
	}

	/*
	 * 加载帮助页面内容
	 */
	private void loadHelpHtml(){
		if(!IMCommon.getNetWorkState()){
			mBaseHandler.sendEmptyMessage(BASE_MSG_NETWORK_ERROR);
			return;
		}
		new Thread(){
			public void run() {
				try {
					IMCommon.sendMsg(mBaseHandler, BASE_SHOW_PROGRESS_DIALOG,"数据加载中,请稍后...");
					String helpHtml = IMCommon.getIMServerAPI().getHelpHtml();
					mBaseHandler.sendEmptyMessage(BASE_HIDE_PROGRESS_DIALOG);
					IMCommon.sendMsg(mHandler, GlobalParam.MSG_SHOW_LOAD_DATA, helpHtml);
				} catch (IMException e) {
					e.printStackTrace();
					IMCommon.sendMsg(mBaseHandler, BASE_MSG_TIMEOUT_ERROR,
							mContext.getResources().getString(e.getStatusCode()));
				}catch (Exception e) {
					e.printStackTrace();
					mBaseHandler.sendEmptyMessage(BASE_HIDE_PROGRESS_DIALOG);
				}
			};
		}.start();
	}
	
	
	/*
	 * 按钮点击事件
	 * (non-Javadoc)
	 * @see net.smallchat.im.BaseActivity#onClick(android.view.View)
	 */
	@Override
	public void onClick(View v) {
		super.onClick(v);
		switch (v.getId()) {
		case R.id.left_btn:
			this.finish();
			break;

		default:
			break;
		}
	}

}
