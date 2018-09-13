package net.smallchat.im.chat;
import android.app.AlertDialog;
import android.app.Dialog;
import android.app.NotificationManager;
import android.app.ProgressDialog;
import android.content.BroadcastReceiver;
import android.content.ClipData;
import android.content.ClipboardManager;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.IntentFilter;
import android.database.sqlite.SQLiteDatabase;
import android.graphics.Bitmap;
import android.graphics.Color;
import android.graphics.drawable.BitmapDrawable;
import android.graphics.drawable.ColorDrawable;
import android.graphics.drawable.Drawable;
import android.net.Uri;
import android.os.Bundle;
import android.os.Environment;
import android.os.Handler;
import android.os.Message;
import android.provider.MediaStore;
import android.support.v4.view.ViewPager;
import android.support.v4.view.ViewPager.OnPageChangeListener;
import android.text.Editable;
import android.text.Spannable;
import android.text.SpannableString;
import android.text.TextUtils;
import android.text.TextWatcher;
import android.text.style.ImageSpan;
import android.util.Log;
import android.view.KeyEvent;
import android.view.LayoutInflater;
import android.view.MotionEvent;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.View.OnFocusChangeListener;
import android.view.View.OnTouchListener;
import android.view.inputmethod.EditorInfo;
import android.view.inputmethod.InputMethodManager;
import android.widget.AbsListView;
import android.widget.AbsListView.OnScrollListener;
import android.widget.AdapterView;
import android.widget.AdapterView.OnItemClickListener;
import android.widget.AdapterView.OnItemLongClickListener;
import android.widget.Button;
import android.widget.EditText;
import android.widget.GridView;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.ListView;
import android.widget.RelativeLayout;
import android.widget.RelativeLayout.LayoutParams;
import android.widget.TextView;
import android.widget.Toast;
import android.widget.ToggleButton;

import net.smallchat.im.BaseActivity;
import net.smallchat.im.DB.DBHelper;
import net.smallchat.im.DB.MessageTable;
import net.smallchat.im.DB.SessionTable;
import net.smallchat.im.DB.UserTable;
import net.smallchat.im.Entity.ChatMessage;
import net.smallchat.im.Entity.ChatType;
import net.smallchat.im.Entity.IMResponseState;
import net.smallchat.im.Entity.Login;
import net.smallchat.im.Entity.MapInfo;
import net.smallchat.im.Entity.MessageReadState;
import net.smallchat.im.Entity.MessageRedPacket;
import net.smallchat.im.Entity.MessageResult;
import net.smallchat.im.Entity.MessageSendState;
import net.smallchat.im.Entity.MessageType;
import net.smallchat.im.Entity.MovingContent;
import net.smallchat.im.Entity.MovingLoaction;
import net.smallchat.im.Entity.MovingPic;
import net.smallchat.im.Entity.MovingVoice;
import net.smallchat.im.Entity.MessagePrivacyMode;
import net.smallchat.im.Entity.Session;

import net.smallchat.im.Entity.UploadAudio;
import net.smallchat.im.Entity.UploadFile;
import net.smallchat.im.Entity.UploadImage;
import net.smallchat.im.Entity.UploadVideo;
import net.smallchat.im.R;
import net.smallchat.im.action.AudioPlayListener;
import net.smallchat.im.action.AudioRecorderAction;
import net.smallchat.im.adapter.ChatMessageAdapter;
import net.smallchat.im.adapter.EmojiAdapter;
import net.smallchat.im.adapter.IMViewPagerAdapter;
import net.smallchat.im.components.ReaderImpl;
import net.smallchat.im.components.multi_image_selector.MultiImageSelector;
import net.smallchat.im.components.slidepager.DataBean;
import net.smallchat.im.components.slidepager.SlidePagerGridViewAdapter;
import net.smallchat.im.components.slidepager.SlidePagerViewPagerAdapter;
import net.smallchat.im.components.slidepager.indicator.CirclePageIndicator;
import net.smallchat.im.fragment.ChatFragment;
import net.smallchat.im.global.AjaxCallBack;
import net.smallchat.im.global.ChatApplication;
import net.smallchat.im.global.FeatureFunction;
import net.smallchat.im.global.FileTool;
import net.smallchat.im.global.GlobalParam;
import net.smallchat.im.global.IMCommon;
import net.smallchat.im.global.ImageLoader;
import net.smallchat.im.global.VoiceTask;
import net.smallchat.im.menu.FastContextMenu;
import net.smallchat.im.menu.FastContextMenuItem;

import net.smallchat.im.api.IMException;
import net.smallchat.im.api.Utility;
import net.smallchat.im.receiver.NotifyChatMessage;
import net.smallchat.im.receiver.PushChatMessage;
import net.smallchat.im.service.IMService;
import net.smallchat.im.service.type.XmppType;
import net.smallchat.im.utils.ImageUtils;
import net.smallchat.im.utils.VideoUtils;
import net.smallchat.im.widget.MainSearchDialog;
import net.smallchat.im.widget.MainSearchDialog.OnFinishClick;
import net.smallchat.im.widget.ResizeLayout;
import net.smallchat.im.widget.ResizeLayout.OnResizeListener;
import net.smallchat.im.components.LocationActivity;
import net.smallchat.im.contact.ChooseUserActivity;
import net.smallchat.im.meeting.MettingDetailActivity;
import net.smallchat.im.mine.MyFavoriteActivity;

import org.apache.http.client.methods.HttpGet;
import org.apache.http.impl.client.DefaultHttpClient;
import org.apache.http.protocol.BasicHttpContext;
import org.apache.http.protocol.SyncBasicHttpContext;

import java.io.File;
import java.lang.ref.SoftReference;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Set;
import java.util.UUID;
import java.util.concurrent.Executor;
import java.util.concurrent.Executors;
import java.util.concurrent.ThreadFactory;
import java.util.concurrent.atomic.AtomicInteger;

/**
 *
 * 功能： 个人会话 <br />
 *
 *
 * 1 成功;
 * 0 失败;
 * 2 正在发送;
 * @author dl
 * @since
 */
public class ChatMainActivity  extends BaseActivity implements TextWatcher,OnItemLongClickListener, OnClickListener, OnTouchListener, OnPageChangeListener{
	/**
	 * 定义全局变量
	 */

	private static final String TAG = "chat_main";
	private final static int MSG_RESIZE = 1234;

	private List<List<String>> mTotalEmotionList = new ArrayList<List<String>>();
	private ViewPager mViewPager;
	private IMViewPagerAdapter mEmotionAdapter;
	private LinkedList<View> mViewList = new LinkedList<View>();
	private LinearLayout mLayoutCircle;
	public int mPageIndxe = 0;
	private RelativeLayout mEmotionLayout;
	private File mCameraTempimageFile=null;
	private RelativeLayout mChatBottomLayout;
	private RelativeLayout mChatBottomMoreBarLayout;
	private LinearLayout mChatBoxMoreBarView;

	private ListView mListView;
	private Button  mMsgSendBtn,mMoreBarDeleteBtn,mMoreBarShareBtn;

	private Button mVoiceSendBtn;
	private ToggleButton mToggleBtn,mAddBtn;
	private EditText mContentEdit;
	private View mChatExpraLayout;
	private ChatMessageAdapter mAdapter;
	private List<ChatMessage> chatMessages;
	private ReaderImpl mReaderImpl;
	private AudioRecorderAction audioRecorder;
	private AudioPlayListener playListener;
	private AlertDialog messageDialog;		// 消息功能ui
	private Handler handler = new Handler();
	private boolean opconnectState = false;


	private ProgressDialog waitDialog;

	private List<String> downVoiceList = new ArrayList<String>();
	private ResizeLayout mRootLayout;
	private ResizeLayout mListLayout;
	private static final int BIGGER = 1;
	private static final int SMALLER = 2;
	private ImageLoader mImageLoader = new ImageLoader();
	public static final int SEND_VOICE_TO_LIST = 4445;
	public static final int ADD_VOICE_TO_LIST = 4446;
	private static final int REQUEST_GET_IMAGE_BY_CAMERA = 1002;
	private static final int REQUEST_GET_VIDEO_BY_CAMERA = 1003;
	public static final int REQUEST_GET_IMAGE = 124;
	public static final int REQUEST_GET_FILE = 125;
	private boolean mIsFirst = true;
	public static final String DESTORY_ACTION = "net.smallchat.im.intent.action.DESTORY_ACTION";
	public static final String REFRESH_ADAPTER = "net.smallchat.im.intent.action.REFRESH_ADAPTER";
	public static final String ACTION_READ_VOICE_STATE = "net.smallchat.im.sns.push.ACTION_READ_VOICE_STATE";
	public static final String ACTION_CHANGE_FRIEND = "net.smallchat.im.intent.action.ACTION_CHANGE_FRIEND";
	public static final String ACTION_RECORD_AUTH = "net.smallchat.im.intent.action.ACTION_RECORD_AUTH";
	public static final String ACTION_RECOMMEND_CARD = "net.smallchat.im.intent.action.ACTION_RECOMMEND_CARD";
	public static final String ACTION_DESTROY_ROOM = "net.smallchat.im.intent.action.ACTION_RECOMMEND_CARD";
	public static final String ACTION_SHOW_NICKNAME = "net.smallchat.im.intent.action.show.nickname";

	private HashMap<String, SoftReference<Bitmap>> mBitmapCache;
	private Login mLogin;
	private String mFilePath = "";
	private int mScalcWith,mScalcHeigth;


	private final static int CHAT_MESSAGE_SEND_SUCCESS = 13454;
	private final static int CHAT_MESSAGE_SEND_FAILED = 13455;
	private final static int CHANGE_STATE = 13456;

	private final static int UPLOAD_SUCCESS = 14454;
	private final static int UPLOAD_FAILED = 14455;

	private final static int  OPEN_PRIVACY_MODE_SUCCESS=160001;
	private final static int  OPEN_PRIVACY_MODE_FAILED=160002;

	private final static int HIDE_PROGRESS_DIALOG = 15453;
	private final static int SHOW_KICK_OUT_DIALOG = 15454;
	private boolean mIsRegisterReceiver = false;
	private Login fCustomerVo;
	private boolean mHasLocalData = true;
	private int mType =  ChatType.PrivateMessage;
	private static final int RESQUEST_MAP_CODE = 100;
	private static final int RESQUEST_REDPACKET_CODE = 200;

	private Dialog  mPhoneDialog;
	private int mIsOwner =0;
	private int mSendCard;
	private ChatMessage mCardMsg,mForMsg;
	private int mIsShowSearchDialog,mFromPage; // fromage: 1=来自会话详情的查找聊天记录
	private String mSearchContent;



	//底部加号窗口的扩展功能
	public static int item_grid_num = 8;//每一页中GridView中item的数量
	public static int number_columns = 4;//gridview一行展示的数目

	private ViewPager mChatExpraViewPager;
	private SlidePagerViewPagerAdapter mChatExpraAdapter;
	private List<DataBean> mChatExpraDataList=new ArrayList<DataBean>(){{
		//第一栏 顺序显示，可以直接按正常逻辑调整顺序。
		add(new DataBean("表情",R.drawable.emotion_btn));
		add(new DataBean("拍照",R.drawable.camera_btn));
		add(new DataBean("图片",R.drawable.gallery_btn));
		add(new DataBean("小视频",R.drawable.small_video_btn));
		add(new DataBean("位置",R.drawable.location_btn));
		add(new DataBean("语音",R.drawable.audio_call_btn));
		add(new DataBean("视频",R.drawable.video_call_btn));
		add(new DataBean("文件",R.drawable.file_btn));
		add(new DataBean("名片", R.drawable.user_card_btn));
		add(new DataBean("收藏",R.drawable.favorite_btn));
		//可以扩展其他功能，自动翻页的，每页8个图标
		add(new DataBean("红包",R.drawable.redpacket_btn));
		add(new DataBean("转账",R.drawable.transfer_btn));

	}};

	private int mChatExpraDataPage=0;
	private List<GridView> mChatExpraGridList = new ArrayList<>();
	private CirclePageIndicator mChatExpraIndicator;

	//底部扩展栏功能点击事件
	public void onChatExproBoxClick(DataBean data) {

		switch (data.getIcon()){
			case R.drawable.emotion_btn:
				btnEmojiAction();
				break;
			case R.drawable.gallery_btn:
				btnPhotoAction();
				break;
			case R.drawable.camera_btn:
				btnCameraAction();
				break;
			case R.drawable.redpacket_btn:
				Toast.makeText(this,"红包功能正在开发",Toast.LENGTH_SHORT).show();
				//btnRedPacketAction();
				break;
			case R.drawable.location_btn:
				btnLocationAction();
				break;
			case R.drawable.file_btn:
				btnFileAction();
				break;
			case R.drawable.small_video_btn:
				btnSmallVideoAction();
				break;
			case R.drawable.user_card_btn:
				hideExpra();
				Intent atIntent = new Intent();
				atIntent.setClass(mContext, ChooseUserActivity.class);
				atIntent.putExtra("isJump",1);
				atIntent.putExtra("toLogin",fCustomerVo);
				startActivityForResult(atIntent, 1);
				break;
			case R.drawable.favorite_btn:
				hideExpra();
				Intent favoriteIntent = new Intent();
				favoriteIntent.setClass(mContext, MyFavoriteActivity.class);
				favoriteIntent.putExtra("isShow",false);
				//startActivityForResult(favoriteIntent, 1);
				startActivity(favoriteIntent);
				break;
			case R.drawable.audio_call_btn:
				Toast.makeText(this,"语音通话功能预留",Toast.LENGTH_SHORT).show();
				if (mType ==ChatType.PrivateMessage) {
					String nickname = fCustomerVo.nickname;
					String contactId = fCustomerVo.uid;
					net.smallchat.im.mediacall.MediaCallCommon.callVoipAction(this, net.smallchat.im.mediacall.MediaCallCommon.CallType.VOICE, nickname, contactId);
				} else {
					net.smallchat.im.mediacall.MediaCallCommon.makeVoiceMeeting(this, fCustomerVo);
				}
				break;
			case R.drawable.video_call_btn:
				Toast.makeText(this,"视频通话功能预留",Toast.LENGTH_SHORT).show();
				if (mType == ChatType.PrivateMessage) {
					String nickname = fCustomerVo.nickname;
					String contactId = fCustomerVo.uid;
					net.smallchat.im.mediacall.MediaCallCommon.callVideoAction(this, net.smallchat.im.mediacall.MediaCallCommon.CallType.VIDEO, nickname, contactId);
				} else {
					net.smallchat.im.mediacall.MediaCallCommon.makeVideoMeeting(this, fCustomerVo);
				}
				break;
			case R.drawable.transfer_btn:
				Toast.makeText(this,"转账功能预留",Toast.LENGTH_SHORT).show();
				break;
			default:

				Toast.makeText(this,"未知",Toast.LENGTH_SHORT).show();
				break;
		}
	}

	/**
	 * 导入控件
	 */
	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		Log.e("ChatMainActivity", "onCreated");
		mContext = this;
		setContentView(R.layout.chat_main);
		sendBroadcast(new Intent(DESTORY_ACTION));
		mSendCard = getIntent().getIntExtra("cardType", 0);
		mCardMsg = (ChatMessage)getIntent().getSerializableExtra("cardMsg");
		mForMsg = (ChatMessage)getIntent().getSerializableExtra("forMsg");
		if(mForMsg!=null) {
			Log.d(TAG, "AUDIO  STRING ORIGIN=== " + mForMsg.content);
		}
		mIsShowSearchDialog = getIntent().getIntExtra("is_show_dialog",0);
		mSearchContent = getIntent().getStringExtra("search_content");
		mFromPage = getIntent().getIntExtra("from_page", 0);
		//读取阅后即焚模式是否打开
		initPrivacyMode();
		//读取本地聊天记录数据
		initMessageInfos();
		//初始化UI组件
		initComponent();
	}


	/**
	 * 初始化控件
	 */
	private void initComponent(){
		setTitleContent(R.drawable.back_btn, 0, "");
		//显示阅后即焚 按钮
		setPrivacyMode(getPrivacyMode(),true);

		mLeftBtn.setOnClickListener(this);
		mRightBtn.setOnClickListener(this);
		mPrivacyModeBtn.setOnClickListener(this);

		mBitmapCache = new HashMap<String, SoftReference<Bitmap>>();
		mLogin = IMCommon.getLoginResult(mContext);
		opconnectState = isOpconnect();
		//		broadcast = new MyBroadcast();
		registerReceiver();

		mChatBottomLayout = (RelativeLayout)findViewById(R.id.RelativeLayout1);
		mChatBoxMoreBarView= (LinearLayout)findViewById(R.id.chat_box_more_bar_view);
		mChatBottomMoreBarLayout = (RelativeLayout)findViewById(R.id.ChatBoxBottomMoreBarRelativeLayout);

		mListView = (ListView) findViewById(R.id.chat_main_list_msg);
		mListView.setOnScrollListener(new OnScrollListener() {

			@Override
			public void onScrollStateChanged(AbsListView view, int scrollState) {
				switch (scrollState) {
					case OnScrollListener.SCROLL_STATE_IDLE:
						if(view.getFirstVisiblePosition() == 0){
							if(mHasLocalData){
								SQLiteDatabase db = DBHelper.getInstance(mContext).getReadableDatabase();

								MessageTable messageTable = new MessageTable(db);
								//查询聊天记录
								List<ChatMessage> tempList = messageTable.query(fCustomerVo.uid, chatMessages.get(0).auto_id, mType);
								Log.d("chat_log","tempList="+tempList.size());
								if(tempList == null || tempList.size() < 20){
									mHasLocalData = false;
								}

								if(tempList != null && tempList.size() != 0){
									chatMessages.addAll(0, tempList);
									mListView.setSelection(tempList.size());
									mAdapter.notifyDataSetChanged();

								}
							}

						}
						break;
				}
			}

			@Override
			public void onScroll(AbsListView view, int firstVisibleItem, int visibleItemCount, int totalItemCount) {
				int beforeItem = firstVisibleItem - 4;
				if(beforeItem > 0){
					recycleBitmapCaches(0, beforeItem);
				}

				int endItem = firstVisibleItem + visibleItemCount + 4;
				if(endItem < totalItemCount){
					recycleBitmapCaches(endItem, totalItemCount);
				}
			}
		});

		mListView.setOnItemLongClickListener(this);


		mMsgSendBtn = (Button) findViewById(R.id.chat_box_btn_send);
		mMsgSendBtn.setText(mContext.getString(R.string.send));
		mMsgSendBtn.setOnClickListener(this);





		mContentEdit = (EditText) findViewById(R.id.chat_box_edit_keyword);
		mContentEdit.setOnClickListener(this);
		mContentEdit.addTextChangedListener(this);

		mToggleBtn = (ToggleButton) findViewById(R.id.chat_box_btn_info);
		mToggleBtn.setOnClickListener(this);
		//按住说话
		mVoiceSendBtn = (Button) findViewById(R.id.chat_box_btn_voice);
		mVoiceSendBtn.setText(mContext.getString(R.string.pressed_to_record));
		mVoiceSendBtn.setOnTouchListener(new OnVoice());
		//mVoiceSendBtn.setVisibility(View.VISIBLE);


		mMoreBarDeleteBtn = (Button) findViewById(R.id.chat_box_more_bar_btn_delete);
		//mMoreBarDeleteBtn.setText(mContext.getString(R.string.send));
		mMoreBarDeleteBtn.setOnClickListener(this);

		mMoreBarShareBtn = (Button) findViewById(R.id.chat_box_more_bar_btn_share);
		//mChatBoxBottomMoreBarDeleteBtn.setText(mContext.getString(R.string.send));
		mMoreBarShareBtn.setOnClickListener(this);


		mAddBtn = (ToggleButton) findViewById(R.id.chat_box_btn_add);
		mAddBtn.setOnClickListener(this);
		//扩展功能区域
		mChatExpraLayout = (View) findViewById(R.id.chat_box_layout_expra);

		initChatExpraViews();
		initChatExpraDatas();


		//表情界面初始化
		initChatEmotionsViews();
		mRootLayout = (ResizeLayout) findViewById(R.id.rootlayout);
		mRootLayout.setOnResizeListener(new OnResizeListener() {

			@Override
			public void OnResize(int w, int h, int oldw, int oldh) {
				int change = BIGGER;
				if(mIsFirst){
					change = SMALLER;
					mIsFirst = false;
				}
				if (h < oldh) {
					change = SMALLER;
				}
				Message msg = new Message();
				msg.what = MSG_RESIZE;
				msg.arg1 = change;
				mHandler.sendMessage(msg);
			}
		});
		mListLayout = (ResizeLayout) findViewById(R.id.listlayout);
		mListLayout.setOnResizeListener(new OnResizeListener() {

			@Override
			public void OnResize(int w, int h, int oldw, int oldh) {

				int change = BIGGER;
				if(mIsFirst){
					change = SMALLER;
					mIsFirst = false;
				}
				if (h < oldh) {
					change = SMALLER;
				}

				if(mListView.getLastVisiblePosition() == chatMessages.size() - 1){
					Message msg = new Message();
					msg.what = MSG_RESIZE;
					msg.arg1 = change;
					mHandler.sendMessage(msg);
				}
			}
		});



		if(mType == ChatType.PrivateMessage){//单聊模式
			mRightBtn.setImageDrawable(mContext.getResources().getDrawable(R.drawable.people_btn));
			mRightBtn.setVisibility(View.VISIBLE);
		}else if(mType == ChatType.GroupMessage){//群聊模式
			mRightBtn.setImageDrawable(mContext.getResources().getDrawable(R.drawable.chat_btn));
			mRightBtn.setVisibility(View.VISIBLE);
		}else if(mType == ChatType.MeetingMessage){//会议模式
			//mVideoCall.setVisibility(View.GONE);
			//mVoipCall.setVisibility(View.GONE);
		}
		if(mFromPage == 1){
			mRightBtn.setVisibility(View.GONE);
			mChatBottomLayout.setVisibility(View.GONE);
		}else{
			mChatBottomLayout.setVisibility(View.VISIBLE);
		}

		titileTextView.setText(getFromName());

		clearNotification();

		audioRecorder = new AudioRecorderAction(getBaseContext());
		mReaderImpl = new ReaderImpl(ChatMainActivity.this, handler, audioRecorder){

			@Override
			public void stop(String path) {

				if (TextUtils.isEmpty(path)) {
					showToast(mContext.getString(R.string.record_time_too_short));
					return;
				}

				if(audioRecorder.getRecordTime() > AudioRecorderAction.MAX_TIME){
					showToast(mContext.getString(R.string.record_time_too_long));
					return;
				}else if (audioRecorder.getRecordTime() < AudioRecorderAction.MIN_TIME) {
					showToast(mContext.getString(R.string.record_time_too_short));
					return;
				}
				File file = new File(path);
				if(file.exists()){
					sendMediaMessage( path);
				}else {
					Toast.makeText(mContext, mContext.getString(R.string.file_not_exist), Toast.LENGTH_SHORT).show();
				}
			}

		};
		playListener = new AudioPlayListener(this){

			@Override
			public void down(ChatMessage msg) {
				super.down(msg);
				downVoice(msg, 1);
			}


		};



		mListView.setOnTouchListener(this);
		mListView.setOnItemLongClickListener(this);
		mVoiceSendBtn.setOnTouchListener(new OnVoice());

		if(!opconnectState){
			Toast.makeText(mContext, mContext.getString(R.string.connect_to_server), Toast.LENGTH_SHORT).show();
		}

		mContentEdit.setOnFocusChangeListener(sendTextFocusChangeListener);
		mContentEdit.setOnClickListener(sendTextClickListener);


		mContentEdit.setHint(mContext.getString(R.string.input_message_hint));

		//判断是否打开了阅后即焚模式

		//显示阅后即焚 按钮
		setPrivacyMode(false,true);

		//将本地聊天记录数据显示到界面
		mAdapter = new ChatMessageAdapter(this,playListener,mBitmapCache,mImageLoader,mLogin,fCustomerVo, chatMessages,mType);
		mListView.setAdapter(mAdapter);



		if(mSearchContent!=null && !mSearchContent.equals("")){//搜索内容需要定位
			checkRecordId();
		}else{
			mListView.setSelection(chatMessages.size() - 1);
		}
		if(chatMessages == null || chatMessages.size() < 20){
			mHasLocalData = false;
		}

		/**
		 * 发送名片
		 */
		if(mSendCard == 1 && mCardMsg!=null){
			createDialog(mContext,mContext.getResources().getString(R.string.confirm_send)
					+mCardMsg.cardOwerName+mContext.getResources().getString(R.string.from_card_to_chat));
		}
		/**
		 * 转发内容
		 */
		if(mForMsg!=null&&!mForMsg.getContent().trim().equals("")){
			preSendMessage(mForMsg,true);
		}

		/**
		 * 显示搜索对话框
		 */
		if(mIsShowSearchDialog == 1){
			MainSearchDialog searchDialog= new MainSearchDialog(mContext,1,new OnFinishClick() {

				@Override
				public void onFinishListener() {
					ChatMainActivity.this.finish();
				}
			},1,fCustomerVo.uid, mType);
			searchDialog.show();
		}


	}

	private String getFromName(){
		String nickname = fCustomerVo.remark;
		if(nickname == null || nickname.equals("")){
			nickname = fCustomerVo.nickname;
		}
		return nickname;
	}

	private void initChatExpraViews() {
		//初始化ViewPager
		mChatExpraViewPager = (ViewPager) findViewById(R.id.view_pager);
		mChatExpraAdapter = new SlidePagerViewPagerAdapter();
		mChatExpraViewPager.setAdapter(mChatExpraAdapter);

		//圆点指示器
		mChatExpraIndicator = (CirclePageIndicator) findViewById(R.id.indicator);
		mChatExpraIndicator.setVisibility(View.VISIBLE);
		mChatExpraIndicator.setViewPager(mChatExpraViewPager);
	}

	private void initChatExpraDatas() {

//
//        mGalleryBtn = (Button) findViewById(R.tid.chat_box_expra_btn_picture);
//        mGalleryBtn.setOnClickListener(this);
//        //发送文件按钮
//        //mFileBtn = (Button) findViewById(R.tid.chat_box_expra_btn_file);
//        //mFileBtn.setOnClickListener(this);
//
//        //红包
//        mRedPacketBtn = (Button) findViewById(R.tid.chat_box_expra_btn_redpacket);
//        mRedPacketBtn.setOnClickListener(this);
//
//
//        mEmotionBtn = (Button) findViewById(R.tid.chat_box_expra_btn_experssion);
//        mEmotionBtn.setOnClickListener(this);
//
//        mLocationBtn = (Button) findViewById(R.tid.chat_box_expra_btn_location);
//        mLocationBtn.setOnClickListener(this);
//
//        mCardBtn = (Button)findViewById(R.tid.chat_box_expra_btn_card);
//        mCardBtn.setOnClickListener(this);
//
//        mFavoritebtn = (Button)findViewById(R.tid.chat_box_expra_btn_favorite);
//        mFavoritebtn.setOnClickListener(this);
//
//        mVideoCall = (Button)findViewById(R.tid.chat_box_expra_btn_video);
//        mVideoCall.setOnClickListener(this);
//
//        mVoipCall = (Button)findViewById(R.tid.chat_box_expra_btn_voipcall);
//        mVoipCall.setOnClickListener(this);



		if (mChatExpraGridList.size() > 0) {
			mChatExpraGridList.clear();
		}

		//计算viewpager一共显示几页
		final int pageSize = mChatExpraDataList.size() % item_grid_num == 0
				? mChatExpraDataList.size() / item_grid_num
				: mChatExpraDataList.size() / item_grid_num + 1;
		for (int i = 0; i < pageSize; i++) {

			GridView gridView = new GridView(this);
			//设置选中背景色透明
			gridView.setSelector(new ColorDrawable(Color.TRANSPARENT));

			SlidePagerGridViewAdapter adapter = new SlidePagerGridViewAdapter(mChatExpraDataList, i,item_grid_num);
			gridView.setOnItemClickListener(new AdapterView.OnItemClickListener() {
				@Override
				public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
					mChatExpraDataPage=mChatExpraViewPager.getCurrentItem();
					//计算list位置
					onChatExproBoxClick(mChatExpraDataList.get(item_grid_num*(mChatExpraDataPage)+position));
				}


			});
			gridView.setNumColumns(number_columns);
			gridView.setAdapter(adapter);
			mChatExpraGridList.add(gridView);
		}
		mChatExpraAdapter.add(mChatExpraGridList);

	}

	/**
	 * 表情界面
	 */
	private void initChatEmotionsViews()
	{
		//表情界面
		mViewPager = (ViewPager) findViewById(R.id.imagepager);
		mViewPager.setOnPageChangeListener(this);

		mLayoutCircle = (LinearLayout) findViewById(R.id.circlelayout);

		mEmotionLayout = (RelativeLayout) findViewById(R.id.emotionlayout);
		mEmotionLayout.setVisibility(View.GONE);

		mTotalEmotionList = getEmojiList();
		for (int i = 0; i < mTotalEmotionList.size(); i++) {
			addView(i);
		}

		mEmotionAdapter = new IMViewPagerAdapter(mViewList);
		mViewPager.setAdapter(mEmotionAdapter);
		mViewPager.setCurrentItem(0);
		showCircle(mViewList.size());

	}


	/**
	 * 创建发送名片对话框
	 * @param context
	 * @param cardTitle
	 */
	protected void createDialog(Context context, String cardTitle) {
		mPhoneDialog = new Dialog (context,R.style.dialog);
		LayoutInflater factor = (LayoutInflater) context
				.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
		View serviceView = factor.inflate(R.layout.card_dialog, null);

		mPhoneDialog.setContentView(serviceView);
		mPhoneDialog.show();
		mPhoneDialog.setCancelable(false);
		mPhoneDialog.getWindow().setLayout(LayoutParams.WRAP_CONTENT
				/*mContext.getResources().getDimensionPixelSize(R.dimen.bind_phone_height)*/
				, LayoutParams.WRAP_CONTENT);

		/*
		TextView signId=(TextView) serviceView
				.findViewById(R.tid.sign_id);
		signId.setText(string[0]);*/
		final TextView phoneEdit=(TextView)serviceView.findViewById(R.id.card_title);


		phoneEdit.setText(cardTitle);

		Button okBtn=(Button)serviceView.findViewById(R.id.yes);
		okBtn.setText(mContext.getResources().getString(R.string.ok));

		okBtn.setOnClickListener(new OnClickListener() {

			@Override
			public void onClick(View v) {
				if (mPhoneDialog!=null) {
					mPhoneDialog.dismiss();
					mPhoneDialog=null;
				}
				if(mCardMsg!=null){
					preSendMessage(mCardMsg,false);
				}

			}
		});

		Button Cancel = (Button)serviceView.findViewById(R.id.no);
		Cancel.setOnClickListener(new OnClickListener() {
			@Override
			public void onClick(View v) {
				if (mPhoneDialog!=null) {
					mPhoneDialog.dismiss();
					mPhoneDialog = null;
				}
			}
		});
	}



	/**
	 * 释放图片
	 * @param start
	 * @param end
	 */
	private void recycleBitmapCaches(int start, int end){
		if(mAdapter != null){
			HashMap<String, Bitmap> buffer = mImageLoader.getImageBuffer();
			for(int i = start; i < end; i++){
				if(chatMessages.get(i).messageType == MessageType.IMAGE){
					String url = chatMessages.get(i).imageData.smallUrl;
					ImageView imageView = (ImageView)mListView.findViewWithTag(url);
					if (imageView != null) {
						imageView.setImageBitmap(null);
						imageView.setImageResource(R.drawable.default_image);
					}

					if(url.startsWith("http://") && 1 == chatMessages.get(i).getSendState()){
						Bitmap bitmap = buffer.get(url);
						if (bitmap != null && !bitmap.isRecycled()) {
							bitmap.recycle();
							bitmap = null;
							buffer.remove(url);
						}
					}else {
						if(mBitmapCache!=null
								&& mBitmapCache.get(url)!=null){
							Bitmap bitmap = mBitmapCache.get(url).get();
							if (bitmap != null && !bitmap.isRecycled()) {
								bitmap.recycle();
								bitmap = null;
								mBitmapCache.remove(url);
							}
						}

					}
				}
			}

		}
	}


	/**
	 * 创建销毁对话框
	 * @param title
	 */
	private void destoryDialog(String title){
		AlertDialog builder = new AlertDialog.Builder(this).create();
		builder.setTitle(title);
		builder.setButton(mContext.getString(R.string.ok), new DialogInterface.OnClickListener() {
			@Override
			public void onClick(DialogInterface dialog, int which) {
				dialog.dismiss();
				ChatMainActivity.this.finish();
			}
		});
		builder.setCancelable(false);
		builder.setCanceledOnTouchOutside(false);
		builder.show();
	}

	/**
	 * 隐藏底部控件
	 */
	@Override
	public boolean onTouch(View v, MotionEvent event) {
		if(event.getAction() == MotionEvent.ACTION_DOWN){

			if(mChatExpraLayout.getVisibility() == View.VISIBLE || mEmotionLayout.getVisibility() == View.VISIBLE){
				hideEmojiGridView();
			}

			if(getCurrentFocus()!=null && getCurrentFocus().getWindowToken()!=null){
				InputMethodManager manager= (InputMethodManager) getSystemService(Context.INPUT_METHOD_SERVICE);
				manager.hideSoftInputFromWindow(getCurrentFocus().getWindowToken(), InputMethodManager.HIDE_NOT_ALWAYS);
			}
		}
		return super.onTouchEvent(event);
	}

	/**
	 * 处理消息
	 */
	private Handler mHandler = new Handler(){
		@Override
		public void handleMessage(Message msg) {
			switch (msg.what) {
				case MSG_RESIZE:
					if(msg.arg1 != BIGGER){
						if((mSearchContent==null || mSearchContent.equals("")) && chatMessages != null && chatMessages.size() != 0){
							mListView.setSelection(chatMessages.size() - 1);
						}
					}

					break;

				case CHAT_MESSAGE_SEND_SUCCESS: {//消息发送成功后处理
					ChatMessage chatMessage = (ChatMessage) msg.obj;
					int isResend = msg.arg1;
					updateNewMessage(chatMessage);//更新数据库
					modifyMessageState(chatMessage);//更新消息状态

					break;
				}
				case UPLOAD_SUCCESS://上传成功
				{
					ChatMessage chatMessage = (ChatMessage) msg.obj;
					//调用消息发送接口
					sendChatMessage(chatMessage,0,false);
					break;
				}
				case UPLOAD_FAILED://上传失败
				{
					ChatMessage chatMessage = (ChatMessage) msg.obj;
					updateNewMessage(chatMessage);//更新数据库
					modifyMessageState(chatMessage);//更新消息状态
					break;
				}
				case CHANGE_STATE:
					ChatMessage messageSend = (ChatMessage) msg.obj;
					updateMessage(messageSend);
					break;

				case CHAT_MESSAGE_SEND_FAILED:
					ChatMessage chatMessage = (ChatMessage) msg.obj;
				/*if(isResend == 0){
					Intent refreshIntent = new Intent(MessageTab.ACTION_REFRESH_SESSION);
					refreshIntent.putExtra("message", message);
					mContext.sendBroadcast(refreshIntent);
				}*/
					updateMessage(chatMessage);
					modifyMessageState(chatMessage);
					break;

				case GlobalParam.HIDE_PROGRESS_DIALOG:
					hideProgressDialog();

					break;

				case GlobalParam.SHOW_PROGRESS_DIALOG:
					String dialogMsg = (String)msg.obj;
					showProgressDialog(dialogMsg);
					mProgressDialog.setCancelable(false);
					break;

				case GlobalParam.MSG_NETWORK_ERROR:
					hideProgressDialog();
					Toast.makeText(mContext,R.string.network_error,Toast.LENGTH_LONG).show();
					break;

				case GlobalParam.MSG_TIME_OUT_EXCEPTION:
					hideProgressDialog();
					String prompt=(String)msg.obj;
					if (prompt==null || prompt.equals("")) {
						prompt=mContext.getString(R.string.timeout);
					}
					Toast.makeText(mContext,prompt, Toast.LENGTH_LONG).show();
					break;

				case HIDE_PROGRESS_DIALOG:
					hideProgressDialog();
					break;

				case SHOW_KICK_OUT_DIALOG:
					destoryDialog(mContext.getString(R.string.group_chat_you_are_kicked));
					break;
				case GlobalParam.MSG_CHECK_FAVORITE_STATUS:
					IMResponseState favoriteResult = (IMResponseState)msg.obj;
					if(favoriteResult == null){
						Toast.makeText(mContext, R.string.commit_dataing, Toast.LENGTH_LONG).show();
						return;
					}
					if(favoriteResult.code!=0){
						Toast.makeText(mContext, favoriteResult.errorMsg, Toast.LENGTH_LONG).show();
						return;
					}
					break;
				case OPEN_PRIVACY_MODE_SUCCESS: {
					String uid = fCustomerVo.uid;
					int flag = 0;
					setPrivacyMode(getPrivacyMode(), true);
					if (getPrivacyMode()) {
						flag = 1;
					} else {
						flag = 0;
					}
					updatePrivacyModeData(uid, flag);
				}
				break;
				case OPEN_PRIVACY_MODE_FAILED: {

					setPrivacyMode(getPrivacyMode(), true);
					String uid = fCustomerVo.uid;
					int flag = 0;
					setPrivacyMode(getPrivacyMode(), true);
					if (getPrivacyMode()) {
						flag = 1;
					} else {
						flag = 0;
					}
					updatePrivacyModeData(uid, flag);
				}
				break;
				default:
					break;
			}
		}

	};

	/**
	 * 拍一张照片
	 */
	private void getImageFromCamera() {
		//启用类微信的图片选择器,只开启摄像头
		MultiImageSelector.create().startCamera(this, REQUEST_GET_IMAGE_BY_CAMERA);
	}
	/**
	 * 拍摄小视频，启用系统相机录制
	 */
	private void getVideoFromCamera() {
		Uri fileUri = Uri.fromFile(getOutputMediaFile());
		Intent intent = new Intent(MediaStore.ACTION_VIDEO_CAPTURE);
		intent.putExtra(MediaStore.EXTRA_OUTPUT, fileUri);
		intent.putExtra(MediaStore.EXTRA_DURATION_LIMIT, 10);     //限制的录制时长 以秒为单位
//        intent.putExtra(MediaStore.EXTRA_SIZE_LIMIT, 1024);        //限制视频文件大小 以字节为单位
//        intent.putExtra(MediaStore.EXTRA_VIDEO_QUALITY, 1);      //设置拍摄的质量0~1
//        intent.putExtra(MediaStore.EXTRA_FULL_SCREEN, false);        // 全屏设置
		startActivityForResult(intent, REQUEST_GET_VIDEO_BY_CAMERA);
	}

	/**
	 * Create a File for saving an video
	 */
	private File getOutputMediaFile() {
		if (!Environment.MEDIA_MOUNTED.equals(Environment.getExternalStorageState())) {
			Toast.makeText(this, "请检查SDCard！", Toast.LENGTH_SHORT).show();
			return null;
		}

		File mediaStorageDir = new File(Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_DCIM), "MyCameraApp");
		if (!mediaStorageDir.exists()) {
			mediaStorageDir.mkdirs();
		}
		// Create a media file name
		String timeStamp = new SimpleDateFormat("yyyyMMdd_HHmmss").format(new java.util.Date());
		File mediaFile = new File(mediaStorageDir.getPath() + File.separator + "VID_" + timeStamp + ".mp4");
		return mediaFile;
	}

	/**
	 * 从相册中选取图片
	 */
	private void getImageFromGallery() {
		//启用类微信的图片选择器
		MultiImageSelector.create()
				.showCamera(true)//显示摄像头
				.count(10)//发10张图片
				.multi()//多图选择
				.start(this, REQUEST_GET_IMAGE);
	}

	/**
	 * 从存储设备卡中选取文件
	 */
	private void getFileFromStorage() {
		Intent intent = new Intent(Intent.ACTION_GET_CONTENT, null);
		intent.setType("images/*");
		intent.setType("zip/*");
		startActivityForResult(intent, REQUEST_GET_FILE);
	}

	/**
	 * 页面返回结果
	 */
	@Override
	protected void onActivityResult(int requestCode, int resultCode, Intent data) {
		super.onActivityResult(requestCode, resultCode, data);

		switch (requestCode) {

			case RESQUEST_MAP_CODE:
				if(data != null && RESULT_OK == resultCode){
					Bundle bundle = data.getExtras();
					if(bundle != null){

						MapInfo mapInfo = (MapInfo)data.getSerializableExtra("mapInfo");
						if(mapInfo == null){
							Toast.makeText(mContext, mContext.getString(R.string.get_location_failed), Toast.LENGTH_SHORT).show();
							return;
						}

						sendMap(mapInfo);
					}
				}
				break;
			case REQUEST_GET_VIDEO_BY_CAMERA://小视频拍摄完成后的处理
				if(resultCode == RESULT_OK && data!=null){
					String videoPath="";
					Uri uri=data.getData();
					videoPath=uri.getPath();

					if(videoPath != null && !videoPath.equals("")){
						//发送小视频
						sendMediaMessage( videoPath);
					}
				}
				break;

			case RESQUEST_REDPACKET_CODE:
				if(data != null && RESULT_OK == resultCode){
					Bundle bundle = data.getExtras();
					if(bundle != null){

						MessageRedPacket redpacketInfo = (MessageRedPacket)data.getSerializableExtra("redpacket");
						if(redpacketInfo == null){

							return;
						}

//						sendRedPacket(redpacketInfo);
					}
				}
				break;
			case REQUEST_GET_IMAGE_BY_CAMERA://拍照处理
				if(resultCode == RESULT_OK){
					mCameraTempimageFile=MultiImageSelector.create().getCameraTempFile();
					String picPath="";

					if(mCameraTempimageFile != null) {
						// notify system the image has change
						sendBroadcast(new Intent(Intent.ACTION_MEDIA_SCANNER_SCAN_FILE, Uri.fromFile(mCameraTempimageFile)));
						picPath=mCameraTempimageFile.getAbsolutePath();
					}
					if(picPath != null && !picPath.equals("")){

						sendMediaMessage( picPath);
					}
				}
				break;
			case REQUEST_GET_IMAGE://选择完图片结果处理
				if(resultCode == RESULT_OK){
					ArrayList<String> mSelectPath = data.getStringArrayListExtra(MultiImageSelector.EXTRA_RESULT);
					StringBuilder sb = new StringBuilder();
					for(String picPath: mSelectPath){
						if(picPath != null && !picPath.equals("")){
							sendMediaMessage(picPath);
						}
					}
				}
				break;
			case REQUEST_GET_FILE://发送文档
				Log.d(TAG,"REQUEST_GET_FILE START");
				if(resultCode == RESULT_OK){
					Log.d(TAG,"REQUEST_GET_FILE RESULT_OK");
					Uri uri=data.getData();
					mFilePath = FileTool.getFilePath(this, uri);
					Log.d(TAG,"REQUEST_GET_FILE FILE PATH="+mFilePath);
					if(mFilePath != null && !mFilePath.equals("")){
						sendMediaMessage(mFilePath);
					}
				}

				break;
			default:
				break;
		}
	}

	/**
	 * 获取隐私模式设置
	 */
	private void	initPrivacyMode(){
		fCustomerVo = (Login)getIntent().getSerializableExtra("data");
		String uid=fCustomerVo.uid;
		//读取数据库
		SQLiteDatabase db =  DBHelper.getInstance(mContext).getWritableDatabase();
		UserTable userTable = new UserTable(db);

		Login login = userTable.query(uid);
		if(login!=null && login.privacyMode == 0){
			setPrivacyMode(false,true);
		}else if(login!=null && login.privacyMode == 1){
			setPrivacyMode(true,true);
		}else {
			setPrivacyMode(false,true);
		}
	}
	private void updatePrivacyModeData(String uid,int flag)
	{
		//更新数据库
		SQLiteDatabase db =  DBHelper.getInstance(mContext).getWritableDatabase();
		UserTable userTable = new UserTable(db);
		userTable.updateIsPrivacyMode(uid,flag);

	}
	/**
	 * 获取本地聊天内容
	 */
	private void initMessageInfos() {

		fCustomerVo = (Login)getIntent().getSerializableExtra("data");
		mType = fCustomerVo.mIsRoom;

		//删除阅后即焚的聊天记录

		deletePrivacyModeData(fCustomerVo.uid);


		SQLiteDatabase db = DBHelper.getInstance(mContext).getWritableDatabase();
		MessageTable messageTable = new MessageTable(db);
		boolean status = messageTable.updateReadState(fCustomerVo.uid, mType);
		if(status){
			mContext.sendBroadcast(new Intent(ChatFragment.ACTION_REFRESH_SESSION));
			mContext.sendBroadcast(new Intent(GlobalParam.ACTION_UPDATE_SESSION_COUNT));
			if(mType ==  ChatType.MeetingMessage){
				mContext.sendBroadcast(new Intent(MettingDetailActivity.ACTION_HIDE_NEW_MEETING_TIP));
				mContext.sendBroadcast(new Intent(GlobalParam.ACTION_REFRESH_MEETING_LIST));
			}
		}
		chatMessages = messageTable.query(fCustomerVo.uid, -1, mType);


		if(chatMessages == null){
			chatMessages = new ArrayList<ChatMessage>();
		}else {
			for (int i = 0; i < chatMessages.size(); i++) {
				if(chatMessages.get(i).readState == MessageReadState.NOT_READ){
					chatMessages.get(i).readState =  MessageReadState.READ;
					updateMessage(chatMessages.get(i));
				}else if(chatMessages.get(i).sendState == MessageSendState.SEND_STATE_SENDING){
					chatMessages.get(i).sendState =  MessageSendState.SEND_STATE_FAIL;
					updateMessage(chatMessages.get(i));
				}
			}
		}

	}


	/**
	 * 获取定位记录的下标
	 */
	private void checkRecordId(){
		int checkId = -1;
		if(mSearchContent !=null  && !mSearchContent.equals("")){
			SQLiteDatabase db = DBHelper.getInstance(mContext).getWritableDatabase();
			MessageTable messageTable = new MessageTable(db);
			boolean isExits = true;

			for (int i = 0; i < chatMessages.size(); i++) {
				if((chatMessages.get(i).content!=null && !chatMessages.get(i).content.equals(""))
						&& !chatMessages.get(i).content.equals(mSearchContent)){
					isExits = false;
				}else if(chatMessages.get(i).content == null || chatMessages.get(i).content.equals("")){
					isExits = false;
				}else{
					checkId = i;
					isExits = true;
					break;
				}

				if(i == chatMessages.size() && !isExits){
					isExits = false;
				}
			}
			if(!isExits){
				List<ChatMessage> tempList = messageTable.query(fCustomerVo.uid, chatMessages.get(0).auto_id, mType);
				if(tempList == null || tempList.size() < 20){
					mHasLocalData = false;
				}

				if(tempList != null && tempList.size() != 0){
					chatMessages.addAll(0, tempList);
					checkRecordId();
				}

			}
			if(checkId!=-1){
				mListView.setSelection(checkId);
			}
			mAdapter.notifyDataSetChanged();


		}
	}



	/************  发送消息 ************/
	private void send(){
		startService(new Intent(getBaseContext(), IMService.class));
	}

	/**
	 * 发送文本
	 */
	private void sendText() {
		send();
		Log.d(TAG, "sendText()");
		String str = mContentEdit.getText().toString();

		if (str != null
				&& (str.trim().replaceAll("\r", "")
				.replaceAll("\t", "").replaceAll("\n", "")
				.replaceAll("\f", "")) != ""&&!str.trim().equals("")) {
			if(str.length() > IMCommon.MESSAGE_CONTENT_LEN){
				//showToast(mContext.getString(R.string.message_limit_count));
				return;
			}
			mContentEdit.setText("");

			ChatMessage msg = new ChatMessage();
			msg.fromId = IMCommon.getUserId(mContext);
			msg.tag = UUID.randomUUID().toString();
			msg.fromName = mLogin.nickname;
			msg.fromUrl = mLogin.headSmall;
			msg.toId = fCustomerVo.uid;
			msg.toName = getFromName();
			msg.toUrl = fCustomerVo.headSmall;
			msg.messageType = MessageType.TEXT;
			msg.content = str;
			msg.chatType = mType;
			msg.time = System.currentTimeMillis();
			msg.readState = 1;
			msg.privacyMode=getPrivacyMode()?1:0;
			Log.d("privacyMode","阅后即焚模式 "+msg.privacyMode);
			preSendMessage(msg,false);
		}
	}
//	/**
//	 * 发送红包
//	 * @param redpacketInfo
//	 */
//	private void sendRedPacket(RedPacketInfo redpacketInfo){
//		send();
//		ChatMessage msg = new ChatMessage();
//		msg.fromId = IMCommon.getUserId(mContext);
//		msg.tag = UUID.randomUUID().toString();
//		msg.fromName = mLogin.nickname;
//		msg.fromUrl = mLogin.headSmall;
//		msg.toId = fCustomerVo.uid;
//		msg.toName = getFromName();
//		msg.toUrl = fCustomerVo.headSmall;
//		msg.messageType = MessageType.LOCATION;
//		msg.mLat = Double.parseDouble(redpacketInfo.getLat());
//		msg.mLng = Double.parseDouble(redpacketInfo.getLng());
//		msg.mAddress = redpacketInfo.getAddr();
//		msg.chatType = mType;
//		msg.time = System.currentTimeMillis();
//		msg.readState = 1;
//
//		preSendMessage(msg,false);
//	}

	/**
	 * 发送地图
	 * @param mapInfo
	 */
	private void sendMap(MapInfo mapInfo){
		send();
		ChatMessage msg = new ChatMessage();
		msg.fromId = IMCommon.getUserId(mContext);
		msg.tag = UUID.randomUUID().toString();
		msg.fromName = mLogin.nickname;
		msg.fromUrl = mLogin.headSmall;
		msg.toId = fCustomerVo.uid;
		msg.toName = getFromName();
		msg.toUrl = fCustomerVo.headSmall;
		msg.messageType = MessageType.LOCATION;
		msg.locationData.lat = Double.parseDouble(mapInfo.getLat());
		msg.locationData.lng = Double.parseDouble(mapInfo.getLng());
		msg.locationData.address = mapInfo.getAddr();
		msg.chatType = mType;
		msg.time = System.currentTimeMillis();
		msg.readState = 1;

		preSendMessage(msg,false);
	}

	/**
	 * 发送声音，图片，文件，小视频
	 * @param filePath
	 */
	private void sendMediaMessage(String filePath){

		ChatMessage msg = new ChatMessage();
		msg.fromId = IMCommon.getUserId(mContext);
		msg.tag = UUID.randomUUID().toString();
		msg.fromName = mLogin.nickname;
		msg.fromUrl = mLogin.headSmall;
		msg.toId = fCustomerVo.uid;
		msg.toName = getFromName();
		msg.toUrl = fCustomerVo.headSmall;

		//根据文件的后缀判断属于什么消息类型
		int messageType=MessageType.getMessageTypeByFile(filePath);

		if(messageType == MessageType.AUDIO){//语音留言
			msg.audioData.localPath=filePath;//本地路径
			msg.audioData.url = filePath;//远程路径
			msg.audioData.time=(int)mReaderImpl.getReaderTime();
		}else if(messageType == MessageType.IMAGE){//图片
			//需要对图片进行压缩处理在发送。
			msg.imageData.width = mScalcWith;
			msg.imageData.height = mScalcHeigth;
			msg.imageData.localPath=filePath;
			String filePath2=ImageUtils.compressImage(filePath);
			if(filePath2!=null){
				msg.imageData.smallUrl = filePath2;
			}
		} else if(messageType == MessageType.FILE){//文件
			msg.fileData.localPath=filePath;
			msg.fileData.url= filePath;
			//获取文件信息
			msg.fileData.filename=FileTool.getFileName(filePath);
			msg.fileData.ext=FileTool.getFileExt(filePath);
			msg.fileData.type="";
			msg.fileData.size=FileTool.getFileSize(filePath);
		} else if(messageType == MessageType.VIDEO){//小视频
			//获取小视频的缩略图
			String thumbPath=VideoUtils.getVideoThumbPath(filePath);
			msg.videoData.localPath=filePath;
			msg.videoData.url = filePath;//小视频路径
			msg.videoData.time=VideoUtils.getPlayTime(filePath);//小视频时间
			msg.videoData.thumb=thumbPath;//小视频缩略图
		}

		msg.messageType = messageType;
		msg.chatType = mType;

		msg.encryptMode=getEncryptMode()?1:0;//加密模式
		msg.privacyMode=getPrivacyMode()?1:0;//阅后即焚/隐私模式
		Log.d("privacyMode","隐私模式："+msg.privacyMode);

		msg.time = System.currentTimeMillis();
		msg.readState = MessageReadState.NOT_READ;
		msg.convertObjectToContent();
		insertMessageToDBAndUpdateUI(msg);

		//发送文件
		sendFilePath(msg, 0);
	}

	private void sendFilePath(ChatMessage chatMessage, int isResend){

		switch (chatMessage.messageType){
			case MessageType.IMAGE:
				uploadImage(chatMessage);
				break;
			case MessageType.FILE:
				uploadFile(chatMessage);
				break;
			case MessageType.AUDIO:
				uploadAudio(chatMessage);
				break;
			case MessageType.VIDEO:
				uploadVideo(chatMessage);
				break;
		}

	}

	/**
	 * 重发文件
	 * @param chatMessage
	 */
	private void resendFile(ChatMessage chatMessage){
		try {
			sendFilePath(chatMessage, 1);
		} catch (Exception e) {
			Log.d(TAG, "resend file:", e);
			showToast(mContext.getString(R.string.resend_failed));
		}
	}

	// 下载音频
	private synchronized void downVoice(final ChatMessage msg, final int type){
		if(!FeatureFunction.checkSDCard()){
			return;
		}

		if(downVoiceList.contains(msg.audioData.url)){
			//showToast(mContext.getString(R.string.download_voice));
			return;
		}
		downVoiceList.add(msg.audioData.url);
		File voicePath = ReaderImpl.getAudioPath(getBaseContext());
		String tag = FeatureFunction.generator(msg.audioData.url);
		String tagName = new File(voicePath, tag).getAbsolutePath();
		HttpGet get = new HttpGet(msg.audioData.url);
		DefaultHttpClient client = Utility.getHttpClient(mContext);


		VoiceTask<File> voiceTask = new VoiceTask<File>(client, new SyncBasicHttpContext(new BasicHttpContext()), new AjaxCallBack<File>() {
			@Override
			public void onSuccess(File t) {
				super.onSuccess(t);
				downVoiceSuccess(msg, type);
				downVoiceList.remove(msg.audioData.url);
			}

			@Override
			public void onFailure(Throwable t, String strMsg) {
				super.onFailure(t, strMsg);
				showToast(mContext.getString(R.string.download_voice_error) + strMsg);
				downVoiceList.remove(msg.audioData.url);
			}
		});

		Executor executor = Executors.newFixedThreadPool(5, new ThreadFactory() {
			private final AtomicInteger mCount = new AtomicInteger(1);
			@Override
			public Thread newThread(Runnable r) {
				Thread tread = new Thread(r, "FinalHttp #" + mCount.getAndIncrement());
				tread.setPriority(Thread.NORM_PRIORITY - 1);
				return tread;
			}
		});
		voiceTask.executeOnExecutor(executor, get, tagName);
	}

	private void downVoiceSuccess(final ChatMessage msg, final int type){
		msg.setSendState(1);
		SQLiteDatabase dbDatabase = DBHelper.getInstance(mContext).getWritableDatabase();
		MessageTable messageTable = new MessageTable(dbDatabase);
		messageTable.update(msg);
		if(type == 1 && playListener.getMessageTag().equals(msg.tag)){
			playListener.play(msg);
		}
	}

	/**
	 * 删除临时图片
	 * @param path
	 */
	private void deleteImgFile(String path){
		File file = new File(path);
		if(file != null && file.exists()){
			file.delete();
		}
	}

	/*********  聊天选择器 ***********/


	private void togInfoSelect(){
		hideExpra();
		if (mToggleBtn.isChecked()) {
			// 语音开启显示
			mContentEdit.setVisibility(View.GONE);
			mVoiceSendBtn.setVisibility(View.VISIBLE);
			hideSoftKeyboard(mToggleBtn);
		} else {
			mContentEdit.setVisibility(View.VISIBLE);
			mVoiceSendBtn.setVisibility(View.GONE);
			hideSoftKeyboard(mToggleBtn);
		}
	}

	/* 重发信息 */
	public void btnResendAction(ChatMessage chatMessage){
		if(messageDialog != null && messageDialog.isShowing()){
			messageDialog.cancel();
		}
		if(chatMessage != null){
			switch (chatMessage.messageType) {
				case MessageType.IMAGE:
				case MessageType.FILE:
				case MessageType.VIDEO:
				case MessageType.AUDIO:
					resendFile(chatMessage);
					break;
				case MessageType.TEXT:
					//sendBroad2Update(chatMessage, 1);
					sendChatMessage(chatMessage, 1,false);
					break;

				default:
					break;
			}
		}
	};

	/**
	 * 显示表情
	 */
	private void btnEmojiAction(){
		showEmojiGridView();
	}



	/*
	 * 发送图片
	 */
	private void btnPhotoAction(){
		getImageFromGallery();
		hideExpra();
	}
	/*
	 * 拍照
	 */
	private void btnCameraAction(){
		getImageFromCamera();
		hideExpra();
	}

	/*
	 * 从存储设备中选择文件,发送文件
	 */
	private void btnFileAction(){
		getFileFromStorage();
		hideExpra();
	}

	/*
	 * 拍摄小视频发送
	 */
	private void btnSmallVideoAction(){
		getVideoFromCamera();
		hideExpra();
	}

	/*
	 * 发送红包
	 */
	private void btnRedPacketAction(){

//		hideExpra();
//		Intent intent = new Intent(this, RedPacketActivity.class);
//		startActivityForResult(intent, RESQUEST_REDPACKET_CODE);
	}
	/*
	 * 发送地图
	 */
	private void btnLocationAction(){
		hideExpra();
		Intent intent = new Intent(this, LocationActivity.class);
		startActivityForResult(intent, RESQUEST_MAP_CODE);
	}

	/*
	 * 处理+号事件
	 */
	private void btnAddAction(){
		if(mChatExpraLayout.getVisibility() == View.VISIBLE){
			hideExpra();
		}else{

			if(mEmotionLayout.getVisibility() == View.VISIBLE){
				hideEmojiGridView();
			}
			showExpra();
		}
	}

	/*
	 * 隐藏键盘显示聊天选择器
	 */
	private void showExpra(){
		hideSoftKeyboard();
		mChatExpraLayout.setVisibility(View.VISIBLE);
	}

	/*
	 * 隐藏聊天选择器
	 */
	private void hideExpra(){
		mChatExpraLayout.setVisibility(View.GONE);
		mAddBtn.setChecked(false);
	}

	/*
	 * 键盘返回事件
	 * (non-Javadoc)
	 * @see android.app.Activity#dispatchKeyEvent(android.view.KeyEvent)
	 */
	@Override
	public boolean dispatchKeyEvent(KeyEvent event) {
		if (event.getAction() == KeyEvent.ACTION_DOWN) {
			if(event.getKeyCode() == KeyEvent.KEYCODE_BACK){
				hideSoftKeyboard();
				if(mChatExpraLayout.getVisibility() == View.VISIBLE || mEmotionLayout.getVisibility() == View.VISIBLE){
					hideEmojiGridView();
					//hideExpra();
					return true;
				}
			}else{
				if(mIsShowSearchDialog == 1){
					ChatMainActivity.this.finish();
					return true;
				}
			}

		}
		if(event.getKeyCode() == KeyEvent.KEYCODE_ENTER){
			/*隐藏软键盘*/
			InputMethodManager inputMethodManager = (InputMethodManager) getSystemService(Context.INPUT_METHOD_SERVICE);
			//发送文本
			sendText();
			return true;
		}
		return super.dispatchKeyEvent(event);
	}

	/*
	 * 消息预发送
	 */
	private void preSendMessage(ChatMessage msg, boolean isForward){
		//插入数据库，并更新UI
		insertMessageToDBAndUpdateUI(msg);
		Log.d("write_db","preSendMessage MESSAGE CONTENT ===="+msg.content);
		//设置未消息发送成功状态
		msg.setSendState(MessageSendState.SEND_STATE_SUCCESS);

		//调用发送接口
		sendChatMessage(msg, 0,isForward);

	}

	/**
	 * 上传文件
	 * @param msg
	 */
	private void uploadFile(final ChatMessage msg) {
		//开启线程发送
		new Thread(){
			@Override
			public void run(){
				if(IMCommon.verifyNetwork(mContext)){

					//消息正在发送
					msg.sendState = MessageSendState.SEND_STATE_SENDING;

					Message stateMessage = new Message();
					stateMessage.obj= msg;
					stateMessage.what = CHANGE_STATE;
					mHandler.sendMessage(stateMessage);
					try {
						String path=msg.fileData.localPath;
						//后台发送消息
						UploadFile uploadFile = IMCommon.getIMServerAPI().uploadChatFile(msg,path);

						if(uploadFile != null){
							//发送成功

							msg.fileData.url=uploadFile.fileUrl;
							msg.fileData.ext=uploadFile.fileExt;
							msg.fileData.filename=uploadFile.fileName;
							try {
								Float f=Float.parseFloat(uploadFile.fileSize);
								long fsize=(long)Math.ceil(f);
								msg.fileData.size =fsize;
							}catch (Exception ex){
								ex.printStackTrace();
								msg.fileData.size =0;
							}
							msg.fileData.type=uploadFile.fileType;
							msg.fileData.icon="";


							Log.d(TAG,"upload.fileUrl===="+uploadFile.fileUrl);
							//发送消息通知
							msg.sendState =  MessageSendState.SEND_STATE_SUCCESS;
							//定义消息
							Message message = new Message();
							//消息发送成功
							message.what = UPLOAD_SUCCESS;
							message.obj = msg;
							mHandler.sendMessage(message);
							return;
						}else{
							Log.d(TAG,"文件发送失败");
							//发送失败
						}
					} catch (IMException e) {
						e.printStackTrace();
					}

				}else {
					mHandler.sendEmptyMessage(GlobalParam.MSG_NETWORK_ERROR);
				}

				msg.sendState =  MessageSendState.SEND_STATE_FAIL;
				Message message = new Message();
				message.what = UPLOAD_FAILED;//消息发送失败
				message.obj = msg;
				mHandler.sendMessage(message);
			}
		}.start();
		//发送消息通知

	}


	/**
	 * 上传图片
	 * @param msg
	 */
	private void uploadImage(final ChatMessage msg) {
		//开启线程发送
		new Thread(){
			@Override
			public void run(){
				if(IMCommon.verifyNetwork(mContext)){

					//消息正在发送
					msg.sendState = MessageSendState.SEND_STATE_SENDING;

					Message stateMessage = new Message();
					stateMessage.obj= msg;
					stateMessage.what = CHANGE_STATE;
					mHandler.sendMessage(stateMessage);
					try {
						String path=msg.imageData.localPath;
						//后台发送消息
						UploadImage uploadImage = IMCommon.getIMServerAPI().uploadChatImage(msg,path);

						Log.d(TAG,"uploadImage.fileUrl="+uploadImage.fileUrl);
						if(uploadImage != null){
							//发送成功

									msg.imageData.largeUrl=uploadImage.fileUrl;
									msg.imageData.smallUrl=uploadImage.thumbUrl;
									try {
										msg.imageData.height = Integer.parseInt(uploadImage.imageHeight);
									}catch (Exception ex){
										msg.imageData.height =100;
										ex.printStackTrace();
									}
									try {
										msg.imageData.width = Integer.parseInt(uploadImage.imageWidth);
									}catch (Exception ex){
										ex.printStackTrace();
										msg.imageData.width =100;
									}


							Log.d(TAG,"upload.fileUrl===="+uploadImage.fileUrl);
							//发送消息通知
							msg.sendState =  MessageSendState.SEND_STATE_SUCCESS;
							//定义消息
							Message message = new Message();
							//消息发送成功
							message.what = UPLOAD_SUCCESS;
							message.obj = msg;
							mHandler.sendMessage(message);
							return;
						}else{
							Log.d(TAG,"文件发送失败");
							//发送失败
						}
					} catch (IMException e) {
						e.printStackTrace();
					}

				}else {
					mHandler.sendEmptyMessage(GlobalParam.MSG_NETWORK_ERROR);
				}

				msg.sendState =  MessageSendState.SEND_STATE_FAIL;
				Message message = new Message();
				message.what = UPLOAD_FAILED;//消息发送失败
				message.obj = msg;
				mHandler.sendMessage(message);
			}
		}.start();
		//发送消息通知

	}


	/**
	 * 上传语音
	 * @param msg
	 */
	private void uploadAudio(final ChatMessage msg) {
		//开启线程发送
		new Thread(){
			@Override
			public void run(){
				if(IMCommon.verifyNetwork(mContext)){

					//消息正在发送
					msg.sendState = MessageSendState.SEND_STATE_SENDING;

					Message stateMessage = new Message();
					stateMessage.obj= msg;
					stateMessage.what = CHANGE_STATE;
					mHandler.sendMessage(stateMessage);
					try {
						String path=msg.audioData.localPath;
						//后台发送消息
						UploadAudio uploadAudio = IMCommon.getIMServerAPI().uploadChatAudio(msg,path);

						if(uploadAudio != null){
							//发送成功
							msg.audioData.url=uploadAudio.fileUrl;
							try {
								msg.audioData.time = Integer.parseInt(uploadAudio.audioTime);
							}catch (Exception ex){
								msg.audioData.time =0;
							}

							Log.d(TAG,"upload.fileUrl===="+uploadAudio.fileUrl);
							//发送消息通知
							msg.sendState =  MessageSendState.SEND_STATE_SUCCESS;
							//定义消息
							Message message = new Message();
							//消息发送成功
							message.what = UPLOAD_SUCCESS;
							message.obj = msg;
							mHandler.sendMessage(message);
							return;
						}else{
							Log.d(TAG,"文件发送失败");
							//发送失败
						}
					} catch (IMException e) {
						e.printStackTrace();
					}

				}else {
					mHandler.sendEmptyMessage(GlobalParam.MSG_NETWORK_ERROR);
				}

				msg.sendState =  MessageSendState.SEND_STATE_FAIL;
				Message message = new Message();
				message.what = UPLOAD_FAILED;//消息发送失败
				message.obj = msg;
				mHandler.sendMessage(message);
			}
		}.start();
		//发送消息通知

	}

	/**
	 * 上传视频
	 * @param msg
	 */
	private void uploadVideo(final ChatMessage msg) {
		//开启线程发送
		new Thread(){
			@Override
			public void run(){
				if(IMCommon.verifyNetwork(mContext)){

					//消息正在发送
					msg.sendState = MessageSendState.SEND_STATE_SENDING;

					Message stateMessage = new Message();
					stateMessage.obj= msg;
					stateMessage.what = CHANGE_STATE;
					mHandler.sendMessage(stateMessage);
					try {
						String path=msg.videoData.localPath;
						//后台发送消息
						UploadVideo uploadVideo = IMCommon.getIMServerAPI().uploadChatVideo(msg,path);

						if(uploadVideo != null){
							//发送成功
									msg.videoData.url=uploadVideo.fileUrl;
									try {
										msg.videoData.thumb = uploadVideo.thumbUrl;
									}catch (Exception ex){
										ex.printStackTrace();
									}
									try {
										msg.videoData.time = Long.parseLong(uploadVideo.videoTime);
									}catch (Exception ex){
										ex.printStackTrace();
									}
									try {
										msg.videoData.time = Integer.parseInt(uploadVideo.videoTime);
									}catch (Exception ex){
										msg.videoData.time =0;
										ex.printStackTrace();
									}
									try {
										Float f=Float.parseFloat(uploadVideo.fileSize);
										long fsize=(long)Math.ceil(f);
										msg.videoData.size =fsize;
									}catch (Exception ex){
										msg.videoData.size =0;
										ex.printStackTrace();
									}
							
							Log.d(TAG,"upload.fileUrl===="+uploadVideo.fileUrl);
							//发送消息通知
							msg.sendState =  MessageSendState.SEND_STATE_SUCCESS;
							//定义消息
							Message message = new Message();
							//消息发送成功
							message.what = UPLOAD_SUCCESS;
							message.obj = msg;
							mHandler.sendMessage(message);
							return;
						}else{
							Log.d(TAG,"文件发送失败");
							//发送失败
						}
					} catch (IMException e) {
						e.printStackTrace();
					}

				}else {
					mHandler.sendEmptyMessage(GlobalParam.MSG_NETWORK_ERROR);
				}

				msg.sendState =  MessageSendState.SEND_STATE_FAIL;
				Message message = new Message();
				message.what = UPLOAD_FAILED;//消息发送失败
				message.obj = msg;
				mHandler.sendMessage(message);
			}
		}.start();
		//发送消息通知

	}
	/**
	 * 发送消息
	 * @param msg  消息体
	 * @param isResend  是否重发
	 * @param isForward 是否转发
	 */
	private void sendChatMessage(final ChatMessage msg, final int isResend, final boolean isForward){

		new Thread(){
			@Override
			public void run(){
				if(IMCommon.verifyNetwork(mContext)){
					//消息正在发送
					msg.sendState = MessageSendState.SEND_STATE_SENDING;

					Message stateMessage = new Message();
					stateMessage.obj= msg;
					stateMessage.what = CHANGE_STATE;
					mHandler.sendMessage(stateMessage);
					try {



						//后台发送消息
						MessageResult result = IMCommon.getIMServerAPI().sendMessage(msg,isForward);

						if(result != null && result.mState != null &&
								(result.mState.code == 0 || result.mState.code == 4)){
							result.mChatMessage.sendState =MessageSendState.SEND_STATE_SUCCESS;
							//将content中的JSON数据转换成对象，关键点
							result.mChatMessage.convertContentToObject();
							if(msg.messageType == MessageType.IMAGE){//图片消息
								String url = FeatureFunction.generator(result.mChatMessage.imageData.largeUrl);
								FeatureFunction.reNameFile(new File(msg.imageData.largeUrl), url);
							}
							result.mChatMessage.readState = MessageReadState.READ;
							Message message = new Message();
							message.what = CHAT_MESSAGE_SEND_SUCCESS;//消息发送成功
							message.arg1 = isResend;
							if(result.mState.code == 4){
								message.arg2 = 4;
							}
							message.obj = result.mChatMessage;
							mHandler.sendMessage(message);
							return;
						}else if(result != null && result.mState != null && result.mState.code == 3){

							SQLiteDatabase db = DBHelper.getInstance(mContext).getWritableDatabase();
							SessionTable sessionTable = new SessionTable(db);
							MessageTable messageTable = new MessageTable(db);
							Session session = sessionTable.query(fCustomerVo.uid, ChatType.GroupMessage);
							if(session != null){
								messageTable.delete(fCustomerVo.uid, ChatType.GroupMessage);
								sessionTable.delete(fCustomerVo.uid, ChatType.GroupMessage);

								mContext.sendBroadcast(new Intent(ChatFragment.ACTION_REFRESH_SESSION));
								mContext.sendBroadcast(new Intent(GlobalParam.ACTION_UPDATE_SESSION_COUNT));
							}
							mHandler.sendEmptyMessage(SHOW_KICK_OUT_DIALOG);
							return;
						}
					} catch (IMException e) {
						e.printStackTrace();
					}

				}else {
					mHandler.sendEmptyMessage(GlobalParam.MSG_NETWORK_ERROR);
				}

				msg.sendState =  MessageSendState.SEND_STATE_FAIL;
				Message message = new Message();
				message.what = CHAT_MESSAGE_SEND_FAILED;//消息发送失败
				message.arg1 = isResend;
				message.obj = msg;
				mHandler.sendMessage(message);
			}
		}.start();
	}


	/**
	 * 请求隐私模式
	 * @param flag
	 * @param uid
	 */
	private void requestPrivacyMode(final int flag, final String uid){

		new Thread(){
			@Override
			public void run(){
				if(IMCommon.verifyNetwork(mContext)){
					try {
						//后台发送消息
						IMCommon.getIMServerAPI().privacyMode(flag,uid);
						//更新数据库

						updatePrivacyModeData(uid,flag);
						//通知界面
						Message message = new Message();
						message.what = OPEN_PRIVACY_MODE_SUCCESS;//打开隐私模式成功
						mHandler.sendMessage(message);
					} catch (IMException e) {
						e.printStackTrace();
						//更新数据库

						updatePrivacyModeData(uid,0);

						Message message = new Message();
						message.what = OPEN_PRIVACY_MODE_FAILED;//打开隐私模式失败
						mHandler.sendMessage(message);
					}


				}else {
					mHandler.sendEmptyMessage(GlobalParam.MSG_NETWORK_ERROR);
					Message message = new Message();
					message.what = OPEN_PRIVACY_MODE_FAILED;//打开隐私模式失败
				}
			}
		}.start();
	}



	/**
	 * op是否连接
	 * @return
	 * 作者:fighter <br />
	 * 创建时间:2013-6-9<br />
	 * 修改时间:<br />
	 */
	private boolean isOpconnect(){
		return true;
	}

	/*
	 * 将消息显示在界面中
	 */
	private void insertMessageToDBAndUpdateUI(ChatMessage msg){

		mVoiceSendBtn.setText(mContext.getString(R.string.pressed_to_record));
		mContentEdit.setHint(mContext.getString(R.string.input_message_hint));
		if(chatMessages.size() == 0){
			chatMessages.add(msg);
		}else {
			boolean isEixst = false;
			for (int i = 0; i < chatMessages.size(); i++) {
				if(chatMessages.get(i).tag.equals(msg.tag)){
					isEixst = true;
					break;
				}
			}
			if(!isEixst){
				chatMessages.add(msg);
			}
		}
		mAdapter.notifyDataSetInvalidated();
		if(chatMessages !=  null && chatMessages.size() != 0){
			mListView.setSelection(chatMessages.size() - 1);
		}

		//clearNotification();
		//写入聊天记录
		insertMessage(msg);
		//写入session表
		Session session = new Session();
		session.setFromId(fCustomerVo.uid);
		session.name = getFromName();
		session.heading = fCustomerVo.headSmall;
		session.type = mType;
		session.lastMessageTime =msg.time;
		insertSession(session);
		//发送广播通知聊天刷新数据
		mContext.sendBroadcast(new Intent(ChatFragment.ACTION_REFRESH_SESSION));
	}

	@Override
	protected void onPause() {
		super.onPause();

		if(!mReaderImpl.mIsStop){
			mReaderImpl.cancelDg();
		}else {
			mReaderImpl.mIsStop = false;
		}

	}
	private void deletePrivacyModeData(String toId){
		SQLiteDatabase db = DBHelper.getInstance(mContext).getWritableDatabase();
		MessageTable messageTable = new MessageTable(db);
		if(mType == ChatType.PrivateMessage){
			messageTable.deletePrivacyModeHadReadData(toId, ChatType.PrivateMessage);
		}else{
			messageTable.deletePrivacyModeHadReadData(toId, ChatType.GroupMessage);
		}

					/*SessionTable sessionTable = new SessionTable(db);
					if(mIsSignChat == 1){
						sessionTable.delete(mGroupID, 100);
					}else{
						sessionTable.delete(mGroupID, 300);
					}*/

		Intent chatIntent = new Intent(ChatMainActivity.REFRESH_ADAPTER);
		chatIntent.putExtra("id", toId);
		mContext.sendBroadcast(chatIntent);
		mContext.sendBroadcast(new Intent(ChatFragment.ACTION_REFRESH_SESSION));
		mContext.sendBroadcast(new Intent(GlobalParam.ACTION_UPDATE_SESSION_COUNT));

	}
	/*
	 * 页面销毁
	 * (non-Javadoc)
	 * @see net.smallchat.im.BaseActivity#onDestroy()
	 */
	@Override
	protected void onDestroy(){
		super.onDestroy();
		IMCommon.saveCamerUrl(mContext,"");
		//删除阅后即焚的聊天记录

		deletePrivacyModeData(fCustomerVo.uid);

		//updateSessionList();
		if(mIsRegisterReceiver){
			unregisterReceiver();
		}

		if(mReaderImpl != null){
			mReaderImpl.unregisterRecordReceiver();
		}
		/*Intent intent = new Intent(MessageTab.ACTION_RESET_SESSION_COUNT);
		intent.putExtra("fromId", fCustomerVo.uid);
		sendBroadcast(intent);*/
		mContext.sendBroadcast(new Intent(ChatFragment.ACTION_REFRESH_SESSION));
		mContext.sendBroadcast(new Intent(GlobalParam.ACTION_UPDATE_SESSION_COUNT));
		playListener.stop();

		/*++释放图片++*/
		if(chatMessages != null){
			for (int i = 0; i < chatMessages.size(); i++) {

				if(!TextUtils.isEmpty(chatMessages.get(i).fromUrl)){
					ImageView headerView = (ImageView) mListView.findViewWithTag(chatMessages.get(i).fromUrl);
					if(headerView != null){
						headerView.setImageBitmap(null);
						headerView.setImageResource(R.drawable.contact_default_header);
					}
				}

				if(chatMessages.get(i).messageType == MessageType.IMAGE){
					ImageView imageView = (ImageView) mListView.findViewWithTag(chatMessages.get(i).content);
					if(imageView != null){
						imageView.setImageBitmap(null);
						imageView.setVisibility(View.GONE);
					}

					if(!chatMessages.get(i).imageData.smallUrl.startsWith("http://") && chatMessages.get(i).sendState != 1){
						mBitmapCache.remove(chatMessages.get(i).imageData.smallUrl);
					}
				}
			}
		}

		Set<String> keys = mBitmapCache.keySet();
		if(keys != null && !keys.isEmpty()){
			for (String key : keys) {
				deleteImgFile(key);
			}
		}

		FeatureFunction.freeBitmap(mImageLoader.getImageBuffer());
		freeBitmap(mBitmapCache);
		System.gc();
		/*--释放图片--*/
	}

	/*
	 * 清空图片缓存
	 */
	private void freeBitmap(HashMap<String, SoftReference<Bitmap>> cache){
		if(cache == null || cache.isEmpty()){
			return;
		}
		for(SoftReference<Bitmap> bitmap:cache.values()){
			if(bitmap.get() != null && !bitmap.get().isRecycled()){
				bitmap.get().recycle();
				bitmap = null;

			}
		}
		cache.clear();
	}

	/*
	 * 注册通知
	 */
	private void registerReceiver(){
		IntentFilter filter = new IntentFilter();
		filter.addAction(IMService.ACTION_CONNECT_CHANGE);
		filter.addAction(PushChatMessage.ACTION_SEND_STATE);
		filter.addAction(NotifyChatMessage.ACTION_NOTIFY_CHAT_MESSAGE);
		filter.addAction(NotifyChatMessage.ACTION_CHANGE_VOICE_CONTENT);
		filter.addAction(DESTORY_ACTION);
		filter.addAction(REFRESH_ADAPTER);
		filter.addAction(ACTION_READ_VOICE_STATE);
		filter.addAction(ACTION_CHANGE_FRIEND);
		filter.addAction(ACTION_RECORD_AUTH);
		filter.addAction(ACTION_DESTROY_ROOM);
		filter.addAction(GlobalParam.BE_KICKED_ACTION);
		filter.addAction(ACTION_RECOMMEND_CARD);
		filter.addAction(ACTION_SHOW_NICKNAME);

		filter.addAction(GlobalParam.ACTION_RESET_GROUP_NAME);

		filter.addAction(GlobalParam.ACTION_REFRESH_CHAT_PRIVACY_MODE);
		//异地登录
		filter.addAction(GlobalParam.ACTION_DIFFERENT_PLACES_LOGIN);

		registerReceiver(chatReceiver, filter);
		mIsRegisterReceiver = true;
	}

	/*
	 * 销毁通知
	 */
	private void unregisterReceiver(){
		unregisterReceiver(chatReceiver);
	}

	/**
	 * 添加表情滑动控件
	 * @param i					添加的位置
	 */
	private void addView(final int i){
		View view = LayoutInflater.from(mContext).inflate(R.layout.emotion_gridview, null);
		GridView gridView = (GridView) view.findViewById(R.id.emoji_grid);
		gridView.setOnItemClickListener(new OnItemClickListener() {

			@Override
			public void onItemClick(AdapterView<?> parent, View view,
									int position, long id) {
				if(position < mTotalEmotionList.get(i).size() - 1){
					ImageView imageView = (ImageView)view.findViewById(R.id.emotion);
					if(imageView != null){
						Drawable drawable = imageView.getDrawable();
						if(drawable instanceof BitmapDrawable){
							Bitmap bitmap = ((BitmapDrawable)drawable).getBitmap();
							String name = mTotalEmotionList.get(i).get(position);

							Drawable mDrawable = new BitmapDrawable(getResources(), bitmap);
							int width = getResources().getDimensionPixelSize(R.dimen.pl_emoji);
							int height = width;
							mDrawable.setBounds(0, 0, width > 0 ? width : 0, height > 0 ? height : 0);
							ImageSpan span = new ImageSpan(mDrawable);

							SpannableString spannableString = new SpannableString("[" + name + "]");
							//类似于集合中的(start, end)，不包括起始值也不包括结束值。
							// 同理，Spannable.SPAN_INCLUSIVE_EXCLUSIVE类似于 [start，end)
							spannableString.setSpan(span, 0, spannableString.length(), Spannable.SPAN_EXCLUSIVE_EXCLUSIVE);
							Editable dEditable = mContentEdit.getEditableText();
							int index = mContentEdit.getSelectionStart();
							dEditable.insert(index, spannableString);
						}
					}
				}else {
					int index = mContentEdit.getSelectionStart();

					String text = mContentEdit.getText().toString();
					if (index > 0) {
						String text2 = text.substring(index - 1);
						if ("]".equals(text2)) {
							int start = text.lastIndexOf("[");
							int end = index;
							mContentEdit.getText().delete(start, end);
							return;
						}
						mContentEdit.getText().delete(index - 1, index);
					}
				}
			}

		});
		gridView.setAdapter(new EmojiAdapter(mContext, mTotalEmotionList.get(i), IMCommon.mScreenWidth));
		mViewList.add(view);
	}


	/**
	 * 显示表情处于第几页标志
	 * @param size
	 */
	private void showCircle(int size){
		mLayoutCircle.removeAllViews();

		for( int i = 0; i < size; i++){
			ImageView img = new ImageView(mContext);
			img.setLayoutParams(new LinearLayout.LayoutParams(FeatureFunction.dip2px(mContext, 5), FeatureFunction.dip2px(mContext, 5)));
			LinearLayout layout = new LinearLayout(mContext);
			LinearLayout.LayoutParams params = new LinearLayout.LayoutParams(LayoutParams.WRAP_CONTENT, LayoutParams.WRAP_CONTENT);
			int margin = FeatureFunction.dip2px(mContext, 5);
			params.setMargins(margin, 0, margin, 0);
			layout.setLayoutParams(params);
			layout.addView(img);
			//img.setLayoutParams()
			if ( mPageIndxe == i){
				img.setImageResource(R.drawable.circle_d);
			} else{
				img.setImageResource(R.drawable.circle_n);
			}
			mLayoutCircle.addView(layout);
		}
	}


	/**
	 * 获取表情列表
	 * @return
	 * guoxin <br />
	 * 创建时间:2013-6-21<br />
	 * 修改时间:<br />
	 */
	private List<List<String>> getEmojiList() {
		List<String> emojiList = new ArrayList<String>();
		String baseName = "emoji_";
		for (int i = 85; i <= 88; i++) {
			emojiList.add(baseName + i);
		}

		for (int i = 340; i <= 363; i++) {
			emojiList.add(baseName + i);
		}

		for (int i = 94; i <= 101; i++) {
			emojiList.add(baseName + i);
		}

		for (int i = 115; i <= 117; i++) {
			emojiList.add(baseName + i);
		}

		for (int i = 364; i <= 373; i++) {
			emojiList.add(baseName + i);
		}

		for (int i = 12; i <= 17; i++) {
			emojiList.add(baseName + i);
		}

		for (int i = 0; i <= 11; i++) {
			emojiList.add(baseName + i);
		}

		for (int i = 18; i <= 84; i++) {
			emojiList.add(baseName + i);
		}

		for (int i = 89; i <= 93; i++) {
			emojiList.add(baseName + i);
		}

		for (int i = 101; i <= 114; i++) {
			emojiList.add(baseName + i);
		}

		for (int i = 114; i <= 339; i++) {
			emojiList.add(baseName + i);
		}

		List<List<String>> totalList = new ArrayList<List<String>>();
		int page = emojiList.size() % 20 ==0 ? emojiList.size() / 20 : emojiList.size() / 20 + 1;
		for (int i = 0; i < page; i++) {
			int startIndex = i * 20;
			List<String> singleList = new ArrayList<String>();
			if(singleList != null){
				singleList.clear();
			}
			int endIndex = 0;
			if(i < page - 1){
				endIndex = startIndex + 20;
			}else if(i == page - 1){
				endIndex = emojiList.size() - 1;
			}

			singleList.addAll(emojiList.subList(startIndex, endIndex));
			singleList.add("delete_emotion_btn");
			totalList.add(singleList);

		}

		return totalList;
	}

	/*
	 * 销毁通知栏的通知列表
	 */
	void clearNotification(){
		//getNotificationManager().cancel(String.valueOf(fCustomerVo.uid).hashCode());
		NotificationManager notificationManager = (NotificationManager) mContext.getSystemService(Context.NOTIFICATION_SERVICE);
		/*if(notificationManager != null && fCustomerVo != null
				&& fCustomerVo.uid!=null && !fCustomerVo.uid.equals("")){
			notificationManager.cancel(fCustomerVo.uid.hashCode());
		}*/

		notificationManager.cancel(0);

	}

	@Override
	public void beforeTextChanged(CharSequence s, int start, int count, int after) {

	}

	@Override
	public void onTextChanged(CharSequence s, int start, int before, int count) {

	}

	@Override
	public void afterTextChanged(Editable s) {
		// 文字改变后出发事件
		String content = mContentEdit.getText().toString();
		//如果不为空显示发送按钮
		if (!content.isEmpty()) {
			//显示发送按钮
			mMsgSendBtn.setVisibility(View.VISIBLE);
			//隐藏加号
			mAddBtn.setVisibility(View.GONE);
		}else{
			//隐藏发送按钮
			mMsgSendBtn.setVisibility(View.GONE);
			//显示加号
			mAddBtn.setVisibility(View.VISIBLE);
		}
	}


	/**
	 * 语音按钮触发
	 */
	class OnVoice implements OnTouchListener {
		// 说话键按下和弹起处理事件
		@Override
		public boolean onTouch(View view, MotionEvent event) {
			switch (event.getAction()) {
				case MotionEvent.ACTION_DOWN:
					if(!FeatureFunction.checkSDCard()){
						break;
					}
					//按住录音掉用
					mReaderImpl.showDg();
					break;
				case MotionEvent.ACTION_UP:
					if(!mReaderImpl.mIsStop){
						mReaderImpl.cancelDg();
					}else {
						mReaderImpl.mIsStop = false;
					}
					break;
			}
			return true;
		}

	}

	/*public FinalBitmap getPhotoBitmap() {
		return FinalFactory.createFinalAlbumBitmap(getBaseContext());
	}*/


	/**************        表情功能       *************/

	// 显示表情列表
	private void showEmojiGridView(){
		hideExpra();
		mToggleBtn.setChecked(false);
		togInfoSelect();
		mEmotionLayout.setVisibility(View.VISIBLE);
	}

	// 隐藏表情列表
	private void hideEmojiGridView(){
		hideExpra();

		mEmotionLayout.setVisibility(View.GONE);
	}


	private View.OnFocusChangeListener sendTextFocusChangeListener = new OnFocusChangeListener() {

		@Override
		public void onFocusChange(View v, boolean hasFocus) {
			// TODO Auto-generated method stub
			if(hasFocus){
				// 文本框得到焦点，隐藏附加信息和表情列表
				hideEmojiGridView();
			}

		}
	};

	private View.OnClickListener sendTextClickListener = new View.OnClickListener() {

		@Override
		public void onClick(View v) {
			// 获取到文本框的点击事件隐藏表情
			hideEmojiGridView();
		}
	};

	private EditText.OnEditorActionListener mEditActionLister = new EditText.OnEditorActionListener() {

		@Override
		public boolean onEditorAction(TextView v, int actionId, KeyEvent event) {
			if(actionId == EditorInfo.IME_ACTION_SEND && mContentEdit.getVisibility() == View.VISIBLE){
				hideSoftKeyboard();
				sendText();

				return true;
			}
			return false;
		}

	};

	/**  聊天广播 */
	private BroadcastReceiver chatReceiver = new BroadcastReceiver() {

		@Override
		public void onReceive(Context context, Intent intent) {
			String action = intent.getAction();
			if(IMService.ACTION_CONNECT_CHANGE.equals(action)){
				Log.d(TAG, "receiver:" + action);
				String type = intent.getExtras().getString(IMService.EXTRAS_CHANGE);
				Log.d(TAG, "receiver:Exper" + type);
				if(XmppType.XMPP_STATE_AUTHENTICATION.equals(type)){
					// 认证成功
					opconnectState = true;
				}else if(XmppType.XMPP_STATE_AUTHERR.equals(type)){
					// 认证失败
					opconnectState = false;
					Log.d(TAG, mContext.getString(R.string.login_user_auth_error));
				}else if(XmppType.XMPP_STATE_REAUTH.equals(type)){
					// 未认证
					opconnectState = false;
				}else if(XmppType.XMPP_STATE_START.equals(type)){
					// 开始登录
					opconnectState = false;
				}else if(XmppType.XMPP_STATE_STOP.equals(type)){
					// 没开启登录
					opconnectState = false;
				}
			}else if(PushChatMessage.ACTION_SEND_STATE.equals(action)){
				Log.d(TAG, "receiver:" + PushChatMessage.ACTION_SEND_STATE);
				ChatMessage chatMessage = (ChatMessage) intent.getSerializableExtra(PushChatMessage.EXTRAS_MESSAGE);
				updateMessage(chatMessage);
				changeSendState(chatMessage);

			}else if(NotifyChatMessage.ACTION_NOTIFY_CHAT_MESSAGE.equals(action)){
				final ChatMessage msg = (ChatMessage) intent.getSerializableExtra(NotifyChatMessage.EXTRAS_NOTIFY_CHAT_MESSAGE);
				if((msg.chatType == ChatType.PrivateMessage && msg.fromId.equals(fCustomerVo.uid))
						|| ((msg.chatType == ChatType.GroupMessage || msg.chatType == ChatType.MeetingMessage)  && msg.toId.equals(fCustomerVo.uid))){
					msg.readState = 1;
					//changeReadState(msg);
					updateMessage(msg);
					mContext.sendBroadcast(new Intent(ChatFragment.ACTION_REFRESH_SESSION));
					mContext.sendBroadcast(new Intent(GlobalParam.ACTION_UPDATE_SESSION_COUNT));
					notifyMessage(msg);
				}
			}else if(action.equals(DESTORY_ACTION)){
				/*
				if(intent.getIntExtra("type", 0) !=1){
					Toast.makeText(mContext,"你已经被管理员移除了", Toast.LENGTH_LONG).show();
				}

				Log.e("ChatMainActivity", "DESTORY_ACTION");*/
				ChatMainActivity.this.finish();
			}else if(action.equals(REFRESH_ADAPTER)){
				//更新聊天界面
				String id = intent.getStringExtra("id");
				if(!TextUtils.isEmpty(id)){
					if(!fCustomerVo.uid.equals(id)){
						return;
					}
				}
				SQLiteDatabase db = DBHelper.getInstance(mContext).getReadableDatabase();
				MessageTable messageTable = new MessageTable(db);
				chatMessages = messageTable.query(fCustomerVo.uid, -1, mType);

				if(chatMessages == null){
					chatMessages = new ArrayList<ChatMessage>();
				}

				mAdapter.notifyDataSetChanged();
			}else if(action.equals(GlobalParam.BE_KICKED_ACTION)){
				String id = intent.getStringExtra("id");
				String uid = intent.getStringExtra("uid");
				int type = intent.getIntExtra("type", 0);
				String hintMsg = intent.getStringExtra("hintMsg");
				if(type!=0){
					if(id.equals(fCustomerVo.uid)){
						destoryDialog(hintMsg);
					}
				}else{
					if(!TextUtils.isEmpty(id)){
						if(!TextUtils.isEmpty(uid) && uid.equals(IMCommon.getUserId(mContext))){
							if(id.equals(fCustomerVo.uid)){
								destoryDialog(mContext.getString(R.string.group_chat_you_are_kicked));
							}
						}

					}
				}


			}else if(ACTION_READ_VOICE_STATE.equals(action)){
				final ChatMessage chatMessage = (ChatMessage) intent.getSerializableExtra(PushChatMessage.EXTRAS_MESSAGE);
				updateMessage(chatMessage);
				changeVoiceState(chatMessage);

			}else if(action.equals(ACTION_RECORD_AUTH)){
				Toast.makeText(mContext,mContext.getString(R.string.record_auth_control),Toast.LENGTH_LONG).show();
			}else if(action.equals(GlobalParam.ACTION_RESET_GROUP_NAME)){
				String groupId = intent.getStringExtra("group_id");
				String groupName = intent.getStringExtra("group_name");
				if(groupId!=null && !groupId.equals("")){
					if(groupName!=null && !groupName.equals("")){
						if(fCustomerVo.uid.equals(groupId)){
							titileTextView.setText(groupName);
						}
					}
				}

			}else if(action.equals(ACTION_RECOMMEND_CARD)){
				ChatMessage msg = (ChatMessage)intent.getSerializableExtra("cardMsg");
				if(msg!=null){
					Log.e("send_card","true++++++++");
					preSendMessage(msg,false);
				}
			}else if(action.equals(ACTION_SHOW_NICKNAME)){
				boolean isShowNickName = intent.getBooleanExtra("is_show_nickname",false);
				if(mAdapter!=null){
					mAdapter.setIsShowNickName(isShowNickName);
					mAdapter.notifyDataSetChanged();
					//mAdapter.
				}
			}else if(action.equals(GlobalParam.ACTION_REFRESH_CHAT_PRIVACY_MODE)){
				String userId = intent.getStringExtra("uid");
				int flag = intent.getIntExtra("flag",0);
				if(userId!=null && !userId.equals("")){
					if(flag== MessagePrivacyMode.PrivacyModeEnabled){//开启隐私模式
						setPrivacyMode(true,true);
						//更新数据库
						updatePrivacyModeData(userId,1);
					}else {//关闭隐私模式
						setPrivacyMode(false,true);
						//更新数据库
						updatePrivacyModeData(userId,0);
					}
				}

			}else if(action.equals(GlobalParam.ACTION_DIFFERENT_PLACES_LOGIN)){
				//处理异地登录信息
				//提醒用户，账号异地登录
				Intent toastIntent = new Intent(GlobalParam.ACTION_SHOW_TOAST);
				toastIntent.putExtra("toast_msg",ChatApplication.getInstance().getResources().getString(R.string.account_repeat));
				ChatApplication.getInstance().sendBroadcast(toastIntent);
				//退出账号
				ChatApplication.getInstance().sendBroadcast(new Intent(GlobalParam.ACTION_LOGIN_OUT));
			}
		}
	};

	private void updateNewMessage(ChatMessage chatMessage){
		SQLiteDatabase db = DBHelper.getInstance(mContext).getWritableDatabase();
		MessageTable table = new MessageTable(db);
		table.updateMessage(chatMessage);
	}

	private void updateMessage(ChatMessage chatMessage){
		SQLiteDatabase db = DBHelper.getInstance(mContext).getWritableDatabase();
		MessageTable table = new MessageTable(db);
		table.update(chatMessage);
	}

	private void insertSession(Session session){
		SQLiteDatabase db = DBHelper.getInstance(mContext).getWritableDatabase();
		SessionTable table = new SessionTable(db);
		Session existSession = table.query(fCustomerVo.uid, session.type);
		if(existSession != null){
			if(existSession.isTop!=0){
				List<Session> exitsSesList = table.getTopSessionList();
				if(exitsSesList!=null && exitsSesList.size()>0){
					for (int i = 0; i < exitsSesList.size(); i++) {
						Session ses = exitsSesList.get(i);
						if(ses.isTop>1){
							ses.isTop = ses.isTop-1;
							table.update(ses, ses.type);
						}
					}
				}
				session.isTop = table.getTopSize();
			}
			table.update(session, session.type);
		}else {
			table.insert(session);
		}
	}

	private void insertMessage(ChatMessage chatMessage){
		SQLiteDatabase db = DBHelper.getInstance(mContext).getWritableDatabase();
		MessageTable table = new MessageTable(db);
		table.insert(chatMessage);
	}

	private void changeSendState(final ChatMessage chatMessage){
		if(chatMessage != null){
			handler.post(new Runnable() {

				@Override
				public void run() {
					modifyMessageState(chatMessage);
				}
			});
		}
	}

	private void notifyMessage(final ChatMessage msg){
		if(msg == null){
			return;
		}
		handler.post(new Runnable() {

			@Override
			public void run() {
				try {
					// 当该信息不来自好友就过滤掉!
					if(msg.getFromId().equals(IMCommon.getUserId(mContext))){
						return;
					}
					chatMessages.add(msg);
					mAdapter.notifyDataSetInvalidated();
					if(chatMessages.size() == 1 || (mListView.getLastVisiblePosition() == chatMessages.size() - 2)){
						mListView.setSelection(chatMessages.size() - 1);
					}
				} catch (Exception e) {

				}

			}
		});
	}

	private void changeVoiceState(final ChatMessage chatMessage){
		if(chatMessage != null){
			handler.post(new Runnable() {

				@Override
				public void run() {
					modifyMessageVoiceState(chatMessage);
				}
			});
		}
	}

	private void changeReadState(final ChatMessage chatMessage){
		if(chatMessage != null){
			handler.post(new Runnable() {

				@Override
				public void run() {
					modifyMessageReadState(chatMessage);
				}
			});
		}
	}

	private void modifyMessageReadState(ChatMessage chatMessage){
		for (int i = 0; i < chatMessages.size(); i++) {
			if(chatMessage.tag.equals(chatMessages.get(i).tag)){
				ChatMessage tempInfo = chatMessages.get(i);
				tempInfo.setReadState(chatMessage.readState);
				mAdapter.notifyDataSetInvalidated();
				break;
			}
		}

	}

	private void modifyMessageState(ChatMessage chatMessage){

		for (int i = 0; i < chatMessages.size(); i++) {
			if(chatMessage.tag.equals(chatMessages.get(i).tag)){
				ChatMessage tempInfo = chatMessages.get(i);
				tempInfo.setSendState(chatMessage.getSendState());

				tempInfo.id = chatMessage.id;
				//消息读取状态
				tempInfo.readState = chatMessage.readState;

				tempInfo.content= chatMessage.content;
				//图片相关
				tempInfo.imageData = chatMessage.imageData;
				//语音留言相关
				tempInfo.audioData = chatMessage.audioData;
				//文件相关
				tempInfo.fileData= chatMessage.fileData;
				//小视频
				tempInfo.videoData = chatMessage.videoData;
				//地图
				tempInfo.locationData = chatMessage.locationData;
				//转账
				tempInfo.transferData= chatMessage.transferData;
				//红包
				tempInfo.redpacketData= chatMessage.redpacketData;
				//收藏
				tempInfo.favoriteData= chatMessage.favoriteData;
				//名片
				tempInfo.cardData= chatMessage.cardData;

				tempInfo.time = chatMessage.time;
				mAdapter.notifyDataSetInvalidated();
				break;
			}
		}

	}

	private void modifyMessageVoiceState(ChatMessage chatMessage){
		for (int i = 0; i < chatMessages.size(); i++) {
			if(chatMessage.tag.equals(chatMessages.get(i).tag)){
				ChatMessage tempInfo = chatMessages.get(i);
				tempInfo.audioData.isReadVoice = chatMessage.audioData.isReadVoice;
				break;
			}
		}
	}

	@Override
	public boolean onItemLongClick(AdapterView<?> arg0, View arg1, int arg2,
								   long arg3) {
		//showLongDialog(mContext, arg2);
		return true;
	}

	protected void createDialog(Context context, String cardTitle,final int type,final String okTitle) {
		mPhoneDialog = new Dialog (context,R.style.dialog);
		LayoutInflater factor = (LayoutInflater) context
				.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
		View serviceView = factor.inflate(R.layout.card_dialog, null);

		mPhoneDialog.setContentView(serviceView);
		mPhoneDialog.show();
		mPhoneDialog.setCancelable(false);
		mPhoneDialog.getWindow().setLayout(LayoutParams.WRAP_CONTENT
				/*mContext.getResources().getDimensionPixelSize(R.dimen.bind_phone_height)*/
				, LayoutParams.WRAP_CONTENT);

		/*
		TextView signId=(TextView) serviceView
				.findViewById(R.tid.sign_id);
		signId.setText(string[0]);*/
		final TextView phoneEdit=(TextView)serviceView.findViewById(R.id.card_title);


		phoneEdit.setText(cardTitle);

		Button okBtn=(Button)serviceView.findViewById(R.id.yes);
		okBtn.setText(okTitle);


		okBtn.setOnClickListener(new OnClickListener() {

			@Override
			public void onClick(View v) {
				if (mPhoneDialog!=null) {
					mPhoneDialog.dismiss();
					mPhoneDialog=null;
				}

				IMCommon.sendMsg(mBaseHandler, BASE_SHOW_PROGRESS_DIALOG, "请求发送中,请稍后...");

				if(type == 1){
					//addBlock();
				}else {
					//deleteFriend();
				}
				/*	send();
				Card card = new Card(mCardLogin.uid, mCardLogin.headSmall, mCardLogin.name, mCardLogin.auth);
				ChatMessage msg = new ChatMessage();
				msg.setContent(Card.convertToObject(card));
				msg.setFromId(SiBaDaCommon.getUserId(mContext));
				msg.setToId(fCustomerVo.uid);
				msg.isRoom = mIsRoom;
				msg.type = MessageType.SUBINFOCARD;
				msg.isMsgBurn = 0;//是否是阅后即焚消息
				msg.isMsgImportant = 0;//是否重要消息
				msg.sendTime = System.currentTimeMillis();
				msg.pullTime = System.currentTimeMillis();
				preSendMessage(msg);*/

			}
		});

		Button Cancel = (Button)serviceView.findViewById(R.id.no);
		Cancel.setOnClickListener(new OnClickListener() {
			@Override
			public void onClick(View v) {
				if (mPhoneDialog!=null) {
					mPhoneDialog.dismiss();
					mPhoneDialog = null;
				}
			}
		});
	}


	/**
	 * pop 长按事件
	 * @param pos
	 * @param type 1-text 消息
	 * @param content
	 */
	public void showContextMenu(View v, final int pos, final int type, final String content, final ChatMessage oldMsg){
		FastContextMenu fastContextMenu = new FastContextMenu(mContext);
		List<FastContextMenuItem> itemList = new ArrayList<>();


		String context_menu_copy=mContext.getResources().getString(R.string.context_menu_copy);
		String context_menu_forward=mContext.getResources().getString(R.string.context_menu_forward);
		String context_menu_revoke=mContext.getResources().getString(R.string.context_menu_revoke);
		String context_menu_favorite=mContext.getResources().getString(R.string.context_menu_favorite);
		String context_menu_delete=mContext.getResources().getString(R.string.context_menu_delete);
		String context_menu_more=mContext.getResources().getString(R.string.context_menu_more);


		itemList.add(new FastContextMenuItem(R.id.menu_item_copy,0, context_menu_copy, true, "#FFFFFF"));
		itemList.add(new FastContextMenuItem(R.id.menu_item_forward,0, context_menu_forward, true, "#FFFFFF"));
		//只能撤回自己的消息，2分钟以内
		if(oldMsg.getFromId().equals(IMCommon.getUserId(mContext))){
			itemList.add(new FastContextMenuItem(R.id.menu_item_revoke,0, context_menu_revoke, true, "#FFFFFF"));
		}

		itemList.add(new FastContextMenuItem(R.id.menu_item_favorite,0, context_menu_favorite, true, "#FFFFFF"));
		itemList.add(new FastContextMenuItem(R.id.menu_item_delete,0, context_menu_delete, true, "#FFFFFF"));
		itemList.add(new FastContextMenuItem(R.id.menu_item_more,0, context_menu_more, true, "#FFFFFF"));
		fastContextMenu.setItemList(itemList);
		fastContextMenu.setOnItemSelectListener(new FastContextMenu.OnItemSelectListener() {
			@Override
			public void onItemSelect(int itemFlag,int position) {
				switch (itemFlag) {

					case R.id.menu_item_copy:
						processContextMenuCopy(type,content,oldMsg);
						break;
					case R.id.menu_item_forward:
						processContextMenuForward(type,content,oldMsg);
						break;
					case R.id.menu_item_revoke:
						processContextMenuWithdraw(type,content,oldMsg);
						break;
					case R.id.menu_item_favorite:
						processContextMenuFavorite(type,content,oldMsg);
						break;
					case R.id.menu_item_delete:
						processContextMenuDelete(type,content,oldMsg);
						break;
					case R.id.menu_item_more:
						processContextMenuMore(type,content,oldMsg);
						break;
				}
			}
		});
		fastContextMenu.showMenu(v);
	}
	//处理复制菜单
	private void processContextMenuCopy(int type, String content, ChatMessage oldMsg) {
		if (type == MessageType.TEXT) {//文本消息

			//复制
			ClipboardManager cm = (ClipboardManager) mContext.getSystemService(Context.CLIPBOARD_SERVICE);
			ClipData clip = ClipData.newPlainText("",content);
			cm.setPrimaryClip(clip);
		}
	}
	//处理转发菜单
	private void processContextMenuForward(int type, String content, ChatMessage oldMsg) {
		Log.d(TAG,"转发信息");
		//转发
		Intent chooseUserIntent = new Intent();
		chooseUserIntent.setClass(mContext, ChooseUserActivity.class);
		chooseUserIntent.putExtra(ChooseUserActivity.FORWARD_MSG, oldMsg);
		startActivity(chooseUserIntent);
	}

	//处理撤回菜单
	private void processContextMenuWithdraw(int type, String content,final ChatMessage oldMsg) {
		Log.d(TAG,"撤回信息");


		Thread thread = new Thread(new Runnable() {
			@Override
			public void run() {
				try {
					//设置消息状态 撤回
					SQLiteDatabase db = DBHelper.getInstance(mContext).getWritableDatabase();
					MessageTable messageTable = new MessageTable(db);
					//正在撤回
					oldMsg.setSendState(MessageSendState.SEND_STATE_REVOKING);
					messageTable.update(oldMsg);
					Intent chatIntent = new Intent(ChatMainActivity.REFRESH_ADAPTER);
					chatIntent.putExtra("id",""+ fCustomerVo.groupId);
					mContext.sendBroadcast(chatIntent);
					mContext.sendBroadcast(new Intent(ChatFragment.ACTION_REFRESH_SESSION));
					mContext.sendBroadcast(new Intent(GlobalParam.ACTION_UPDATE_SESSION_COUNT));
					//发送消息撤回请求
					IMResponseState state = null;
					try {
						state = IMCommon.getIMServerAPI().revokeMessage(601,oldMsg.tag,oldMsg.getToId());
					} catch (IMException e) {
						e.printStackTrace();
					}
					if(state != null && state.code == 0){
						deleteSingleMessage(oldMsg);
						//撤回消息请求发送成功！
						//IMCommon.sendMsg(mHandler, GlobalParam.MSG_REFRESH_REVOKE_MESSAGE, state);

					}else {
						Message msg=new Message();
						msg.what=GlobalParam.MSG_LOAD_ERROR;
						if(state != null
								&& !state.errorMsg.equals("")){
							msg.obj = state.errorMsg;
						}else {
							msg.obj = mContext.getString(R.string.fail_revoke);
						}
						mHandler.sendMessage(msg);
					}

				} catch (Exception e) {
					e.printStackTrace();
				}
			}
		});

		thread.start();



	}


	//处理收藏菜单
	private void processContextMenuFavorite(int type, String content, ChatMessage oldMsg) {
		//收藏菜单
		String ownerId = "";
		String groupId = "";
		if (oldMsg.chatType == ChatType.GroupMessage || oldMsg.chatType == ChatType.MeetingMessage) {
			groupId = oldMsg.toId;
			ownerId = oldMsg.fromId;
		} else if (oldMsg.chatType == ChatType.PrivateMessage) {
			ownerId = oldMsg.fromId;
		}
		switch (oldMsg.messageType) {
			case MessageType.TEXT:
				MovingContent movingContent = new MovingContent(content, MessageType.TEXT + "");
				favoriteMoving(MovingContent.getInfo(movingContent), ownerId, groupId);
				break;
			case MessageType.IMAGE:
				MovingPic movingPic = new MovingPic(oldMsg.imageData.smallUrl, oldMsg.imageData.largeUrl, MessageType.IMAGE + "");
				favoriteMoving(MovingPic.getInfo(movingPic), ownerId, groupId);
				break;
			case MessageType.AUDIO:
				MovingVoice movingVoice = new MovingVoice(oldMsg.audioData.time + "", oldMsg.audioData.url, MessageType.AUDIO + "");
				favoriteMoving(MovingVoice.getInfo(movingVoice), ownerId, groupId);
				break;
			case MessageType.LOCATION:
				MovingLoaction movingLocation = new MovingLoaction(oldMsg.locationData.lat + "", oldMsg.locationData.lng + "", oldMsg.locationData.address, MessageType.LOCATION + "");
				favoriteMoving(MovingLoaction.getInfo(movingLocation), ownerId, groupId);
				break;
			default:
				break;
		}
	}
	//处理删除菜单
	private void processContextMenuDelete(int type, String content, ChatMessage oldMsg) {
		deleteSingleMessage(oldMsg);
	}

	/**
	 * 删除单条消息
	 * @param oldMsg
	 */
	private void deleteSingleMessage(ChatMessage oldMsg){
		//删除单条消息
		SQLiteDatabase db = DBHelper.getInstance(mContext).getWritableDatabase();
		MessageTable messageTable = new MessageTable(db);
		messageTable.delete(oldMsg);


		Intent chatIntent = new Intent(ChatMainActivity.REFRESH_ADAPTER);
		chatIntent.putExtra("id", fCustomerVo.groupId);
		mContext.sendBroadcast(chatIntent);
		mContext.sendBroadcast(new Intent(ChatFragment.ACTION_REFRESH_SESSION));
		mContext.sendBroadcast(new Intent(GlobalParam.ACTION_UPDATE_SESSION_COUNT));
	}
	//处理更多菜单
	private void processContextMenuMore(int type, String content, ChatMessage oldMsg) {
		//更多菜单显示多选框
		showSelectBox();
		//显示更多菜单
		showMoreMenu();
	}

	/**
	 * 显示更多菜单
	 */
	private void showMoreMenu(){
		mChatExpraLayout.setVisibility(View.GONE);
		mChatBottomLayout.setVisibility(View.GONE);
		mChatBoxMoreBarView.setVisibility(View.VISIBLE);
	}

	/**
	 * 隐藏更多菜单
	 */
	private void hideMoreMenu(){
		mChatExpraLayout.setVisibility(View.GONE);
		mChatBoxMoreBarView.setVisibility(View.GONE);
		mChatBottomLayout.setVisibility(View.VISIBLE);

	}
	/**
	 * 显示更多菜单
	 */
	private void showSelectBox(){
		for (int i = 0; i < chatMessages.size(); i++) {

			ChatMessage tempInfo = chatMessages.get(i);
			tempInfo.setShowSelect(1);
			tempInfo.setSelected(0);
		}
		mAdapter.notifyDataSetInvalidated();
	}

	/**
	 * 隐藏更多菜单
	 */
	private void hideSelectBox(){
		for (int i = 0; i < chatMessages.size(); i++) {

			ChatMessage tempInfo = chatMessages.get(i);
			tempInfo.setShowSelect(0);
			tempInfo.setSelected(0);
		}
		mAdapter.notifyDataSetInvalidated();
	}




	private void favoriteMoving(final String favoriteContent,final String ownerUid,final String groupid){
		if (!IMCommon.getNetWorkState()) {
			mBaseHandler.sendEmptyMessage(BASE_MSG_NETWORK_ERROR);
			return;
		}
		new Thread(){
			public void run() {
				try {
					IMCommon.sendMsg(mBaseHandler, BASE_SHOW_PROGRESS_DIALOG,
							mContext.getResources().getString(R.string.send_request));
					IMResponseState status = IMCommon.getIMServerAPI().favoreiteMoving(ownerUid, groupid, favoriteContent);
					IMCommon.sendMsg(mHandler, GlobalParam.MSG_CHECK_FAVORITE_STATUS,status);
					mBaseHandler.sendEmptyMessage(BASE_HIDE_PROGRESS_DIALOG);
				} catch (IMException e) {
					e.printStackTrace();
					IMCommon.sendMsg(mBaseHandler,BASE_MSG_TIMEOUT_ERROR,
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
		int id = v.getId();
		switch (id) {
			case R.id.chat_box_btn_send://发送按钮
				sendText();
				break;
			case R.id.chat_box_btn_info:
				togInfoSelect();
				break;
			case R.id.chat_box_btn_add:
				btnAddAction();
				break;
			case R.id.left_btn:
				hideSoftKeyboard();
				this.finish();
				break;
			case R.id.privacy_mode_btn://阅后即焚
				fCustomerVo = (Login)getIntent().getSerializableExtra("data");
				//setPrivacyMode(!getPrivacyMode(),true);
				int flag= MessagePrivacyMode.Normal;
				mPrivacyMode=!mPrivacyMode;
				if(getPrivacyMode()){
					//调用接口发送请求
					flag= MessagePrivacyMode.PrivacyModeEnabled;
					showToast(getString(R.string.privacy_mode_open_tips));
				}else {
					//调用接口发送请求
					flag= MessagePrivacyMode.Normal;
					showToast(getString(R.string.privacy_mode_close_tips));
				}
				//请求网络
				requestPrivacyMode(flag,fCustomerVo.uid);
				break;

			case R.id.right_btn:
				SQLiteDatabase dbDatabase = DBHelper.getInstance(mContext).getWritableDatabase();
				SessionTable sessionTable = new SessionTable(dbDatabase);
				Session existSession = sessionTable.query(fCustomerVo.uid,mType);
				Intent detailIntent = new Intent(mContext, GroupChatDetailActivity.class);
				if(existSession!=null){
					if(existSession.isTop>=1){
						detailIntent.putExtra("isTop",true);
					}else{
						detailIntent.putExtra("isTop",false);
					}
				}
				detailIntent.putExtra("groupid", fCustomerVo.uid);

				detailIntent.putExtra("chatType", mType);
				if(mType !=  ChatType.PrivateMessage){
					detailIntent.putExtra("isOwner", getIntent().getIntExtra("isOwner", 0));
				/*if(mGroupListItemIndex!=-1){
					detailIntent.putExtra("group_list_item_index",mGroupListItemIndex);
				}*/
				}else {

					detailIntent.putExtra("isSignChat",1);
					detailIntent.putExtra("to_login", fCustomerVo);
				}

				mContext.startActivity(detailIntent);
				//showMoreDialog(mContext);
				break;
			case R.id.chat_box_more_bar_btn_delete://更多菜单删除按钮

				deleteSelectedMessage();
				break;
			case R.id.chat_box_more_bar_btn_share://更多菜单分享按钮

				shareSelectedMessage();

				break;
			default:
				break;
		}
	}

	/**
	 * 分享选择的消息
	 */
	private void shareSelectedMessage() {
		hideMoreMenu();
		hideSelectBox();
	}

	/**
	 * 删除选择的消息
	 */
	private void deleteSelectedMessage() {
		for (int i = 0; i < chatMessages.size(); i++) {
			if(chatMessages.get(i).getSelected()==1){
				deleteSingleMessage(chatMessages.get(i));
				chatMessages.remove(i);
			}
		}
		hideMoreMenu();
		hideSelectBox();
	}


	public void hideSoftKeyboard(){
		hideSoftKeyboard(getCurrentFocus());
	}
	public void hideSoftKeyboard(View view){
		InputMethodManager imm = (InputMethodManager) getSystemService(Context.INPUT_METHOD_SERVICE);
		if(view != null){
			imm.hideSoftInputFromWindow(view.getWindowToken(), 0);
		}
	}

	public void showSoftKeyboard(){
		InputMethodManager imm = (InputMethodManager) mContentEdit.getContext().getSystemService(Context.INPUT_METHOD_SERVICE);
		imm.toggleSoftInput(0, InputMethodManager.SHOW_FORCED);
	}

	public ProgressDialog getWaitDialog() {
		return waitDialog;
	}

	public void showToast(String content){
		Toast.makeText(mContext, content, Toast.LENGTH_LONG).show();
	}



	@Override
	public void onPageScrollStateChanged(int arg0) {
		// TODO Auto-generated method stub

	}



	@Override
	public void onPageScrolled(int arg0, float arg1, int arg2) {
		// TODO Auto-generated method stub

	}



	@Override
	public void onPageSelected(int position) {
		mPageIndxe = position;
		showCircle(mViewList.size());
	}

}