package net.smallchat.im.mine;

import android.app.Activity;
import android.app.AlertDialog;
import android.app.NotificationManager;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.SharedPreferences;
import android.content.SharedPreferences.Editor;
import android.net.Uri;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.Button;
import android.widget.FrameLayout;
import android.widget.FrameLayout.LayoutParams;
import android.widget.RelativeLayout;
import android.widget.TextView;
import android.widget.Toast;

import net.smallchat.im.BaseActivity;
import net.smallchat.im.Entity.Login;
import net.smallchat.im.Entity.Version;
import net.smallchat.im.Entity.VersionInfo;
import net.smallchat.im.HelpWebViewActivity;
import net.smallchat.im.R;
import net.smallchat.im.about.AboutActivity;
import net.smallchat.im.about.AppUpgrade;
import net.smallchat.im.about.FeedBackActivity;
import net.smallchat.im.contact.BlockListActivity;
import net.smallchat.im.global.ChatApplication;
import net.smallchat.im.global.FeatureFunction;
import net.smallchat.im.global.GlobalParam;
import net.smallchat.im.global.GlobleType;
import net.smallchat.im.global.IMCommon;
import net.smallchat.im.global.ImageLoader;
import net.smallchat.im.api.IMException;
import net.smallchat.im.service.IMService;
import net.smallchat.im.utils.AppUtils;
import net.smallchat.im.utils.PermissionUtils;
import net.smallchat.im.utils.device.RomUtils;
import net.smallchat.im.utils.device.XiaomiDeviceUtil;
import net.smallchat.im.widget.ConfirmDialog;

/**
 * 设置界面
 * @author dongli
 *
 */
public class SettingTab extends BaseActivity implements OnClickListener{

	/**
	 * 定义全局变量
	 */

	private RelativeLayout  mNotifyLayout, mScanLayout, mBlockLayout,
			mFeedBackLayout, mHelpLayout, mUpgradeLayout, mAboutLayout,
			mNewMsgNotifyLayout,mModifyPwdLayout,mBackgroudReceiveMessageLayout;
	private static Activity mSettingTab;
	private Button mLogoutBtn;
	private boolean mIsRegisterReceiver = false;
	protected AlertDialog mUpgradeNotifyDialog;
	protected AlertDialog mDownDialog;
	private Version mVersion;
	public final static int SHOW_UPGRADE_DIALOG = 10001;
	public final static int NO_NEW_VERSION = 11315;
	public final static String REFRESH_HEADER = "action_refresh_header";
	private Login mUser;
	private ImageLoader mImageLoader = new ImageLoader();
	protected AppUpgrade mClientUpgrade;

	/**
	 * 导入控件
	 */
	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		mContext = this;
        mSettingTab=this;
		setContentView(R.layout.setting_tab);

		IntentFilter filter = new IntentFilter();
		filter.addAction(GlobalParam.SWITCH_LANGUAGE_ACTION);
		filter.addAction(REFRESH_HEADER);
		registerReceiver(mReceiver, filter);
		mIsRegisterReceiver = true;

		initComponent();
	}

	/**
	 * 根据通知处理界面逻辑
	 */
	BroadcastReceiver mReceiver = new BroadcastReceiver() {
		@Override
		public void onReceive(Context context, Intent intent){
			if(intent != null){
				if(intent.getAction().equals(GlobalParam.SWITCH_LANGUAGE_ACTION)){

				}else if(intent.getAction().equals(REFRESH_HEADER)){
					mUser = IMCommon.getLoginResult(mContext);
				}
			}
		}
	};

	/**
	 * 页面销毁
	 */
	@Override
	protected void onDestroy() {
		if(mIsRegisterReceiver){
			unregisterReceiver(mReceiver);
		}
		super.onDestroy();
	}

	/**
	 * 初始化控件
	 */
	private void initComponent(){
		setTitleContent(R.drawable.back_btn,0,R.string.setting);
		mLeftBtn.setOnClickListener(this);

		mLogoutBtn = (Button) findViewById(R.id.logout);
		mLogoutBtn.setOnClickListener(this);

		mNotifyLayout = (RelativeLayout) findViewById(R.id.notify_layout);
		mNotifyLayout.setOnClickListener(this);

		mScanLayout = (RelativeLayout) findViewById(R.id.privacy_layout);
		mScanLayout.setOnClickListener(this);

		mBlockLayout = (RelativeLayout) findViewById(R.id.block_layout);
		mBlockLayout.setOnClickListener(this);


		mFeedBackLayout = (RelativeLayout) findViewById(R.id.feedback_layout);
		mFeedBackLayout.setOnClickListener(this);

		mHelpLayout = (RelativeLayout) findViewById(R.id.help_layout);
		mHelpLayout.setOnClickListener(this);

		mUpgradeLayout = (RelativeLayout) findViewById(R.id.upgrade_layout);
		mUpgradeLayout.setOnClickListener(this);

		mAboutLayout = (RelativeLayout) findViewById(R.id.about_layout);
		mAboutLayout.setOnClickListener(this);

		mNewMsgNotifyLayout = (RelativeLayout)findViewById(R.id.new_message_notify_layout);
		mNewMsgNotifyLayout.setOnClickListener(this);

		mModifyPwdLayout = (RelativeLayout)findViewById(R.id.modify_password_layout);
		mModifyPwdLayout.setOnClickListener(this);


		mBackgroudReceiveMessageLayout = (RelativeLayout) findViewById(R.id.backgroud_receive_message_layout);
		mBackgroudReceiveMessageLayout.setOnClickListener(this);

		mUser = IMCommon.getLoginResult(mContext);

	}



	/**
	 * 按钮点击事件
	 */
	@Override
	public void onClick(View v) {
		super.onClick(v);
		switch (v.getId()) {
			case R.id.left_btn://返回按钮
				SettingTab.this.finish();
				break;
			case R.id.modify_password_layout://修改密码
				Intent PwdIntent = new Intent(mContext, ModifyPwdActivity.class);
				startActivity(PwdIntent);
				break;
			case R.id.new_message_notify_layout://新消息通知
				Intent newMsgNotifyIntent = new Intent(mContext, PrivacySettingActivity.class);
				newMsgNotifyIntent.putExtra("type", GlobleType.PrivateSetActivity_New_Msg_Notify_TYPE);
				startActivity(newMsgNotifyIntent);
				break;

			case R.id.privacy_layout://隐私设置
				Intent scanIntent = new Intent(mContext, PrivacySettingActivity.class);
				startActivity(scanIntent);
				break;

			case R.id.block_layout://黑名单
				Intent intent = new Intent(mContext, BlockListActivity.class);
				startActivity(intent);
				break;

			case R.id.feedback_layout://意见反馈
				Intent feedbackIntent = new Intent(mContext, FeedBackActivity.class);
				startActivity(feedbackIntent);
				break;

			case R.id.help_layout://帮助
				Intent helpIntent = new Intent(mContext, HelpWebViewActivity.class);
				helpIntent.putExtra("type", 1);
				startActivity(helpIntent);
				break;

			case R.id.upgrade_layout://版本检测
				checkUpgrade();
				break;

			case R.id.about_layout://关于我们
				Intent aboutIntent = new Intent(mContext, AboutActivity.class);
				startActivity(aboutIntent);
				break;
			case R.id.backgroud_receive_message_layout://退出APP接收消息设置
                showReceiveMessageSettingDialog(this);
				break;

			case R.id.logout://退出登陆
				sendBroadcast(new Intent(GlobalParam.SWITCH_TAB));
				SharedPreferences preferences = this.getSharedPreferences(IMCommon.LOGIN_SHARED, 0);
				Editor editor = preferences.edit();
				editor.remove(IMCommon.LOGIN_RESULT);
				editor.commit();
				IMCommon.setUid("");
				IMCommon.setServer("");

				SharedPreferences server = this.getSharedPreferences(IMCommon.SERVER_SHARED, 0);
				Editor serverEditor = server.edit();
				serverEditor.remove(IMCommon.SERVER_PREFIX);
				serverEditor.commit();

				Intent serviceIntent = new Intent(mContext, IMService.class);
				mContext.stopService(serviceIntent);

				sendBroadcast(new Intent(GlobalParam.ACTION_LOGIN_OUT));
				SettingTab.this.finish();

				//清楚通知栏所有的通知
				NotificationManager notificationManager = (NotificationManager) mContext.getSystemService(Context.NOTIFICATION_SERVICE);
				notificationManager.cancelAll();
				break;

			default:
				break;
		}
	}

    public static void showReceiveMessageSettingDialog(final Context context) {
        String content;
        if(RomUtils.BRAND==RomUtils.BRAND_XIAOMI){
            content= context.getString(R.string.setting_power_notice_xiaomi);
        }else if(RomUtils.BRAND==RomUtils.BRAND_HUAWEI){
            content= context.getString(R.string.setting_power_notice_huawei);
        }else if(RomUtils.BRAND==RomUtils.BRAND_HONOR){
            content= context.getString(R.string.setting_power_notice_honor);
        }else if(RomUtils.BRAND==RomUtils.BRAND_VIVO){
            content= context.getString(R.string.setting_power_notice_vivo);
        }else if(RomUtils.BRAND==RomUtils.BRAND_OPPO){
            content= context.getString(R.string.setting_power_notice_oppo);
        }else if(RomUtils.BRAND==RomUtils.BRAND_SMARTISAN){
            content= context.getString(R.string.setting_power_notice_smartisan);
        }else if(RomUtils.BRAND==RomUtils.BRAND_MEIZU){
            content= context.getString(R.string.setting_power_notice_meizu);
        }else{
            content= context.getString(R.string.setting_power_notice);
        }


        content=content.replace("{{IM_PACKAGE}}", AppUtils.getPackageName(context));
        content=content.replace("{{IM_APPNAME}}", AppUtils.getAppName(context));
        final ConfirmDialog confirmDialog = new ConfirmDialog(context,content, context.getString(R.string.dialog_confirm_ok), context.getString(R.string.dialog_confirm_cancel));
        confirmDialog.show();
        confirmDialog.setClicklistener(new ConfirmDialog.ClickListenerInterface() {
            @Override
            public void doConfirm() {
                if(RomUtils.BRAND==RomUtils.BRAND_XIAOMI){
                    XiaomiDeviceUtil.toConfigApp(context,AppUtils.getPackageName(context),AppUtils.getAppName(context));
                }else if(RomUtils.BRAND==RomUtils.BRAND_HUAWEI){

                    PermissionUtils.GoToSetting(mSettingTab);

                }else if(RomUtils.BRAND==RomUtils.BRAND_HONOR){
                    PermissionUtils.GoToSetting(mSettingTab);
                }else if(RomUtils.BRAND==RomUtils.BRAND_VIVO){
                    PermissionUtils.GoToSetting(mSettingTab);

                }else if(RomUtils.BRAND==RomUtils.BRAND_OPPO){
                    PermissionUtils.GoToSetting(mSettingTab);
                }else if(RomUtils.BRAND==RomUtils.BRAND_SMARTISAN){

                }else if(RomUtils.BRAND==RomUtils.BRAND_MEIZU){
                    PermissionUtils.GoToSetting(mSettingTab);
                }else{
                    PermissionUtils.GoToSetting(mSettingTab);
                }
                confirmDialog.dismiss();
            }

            @Override
            public void doCancel() {
                // TODO Auto-generated method stub
                confirmDialog.dismiss();
            }
        });
    }
	/**
	 * 处理消息
	 */
	private Handler mHandler=new Handler(){

		@Override
		public void handleMessage(Message msg) {
			super.handleMessage(msg);
			switch (msg.what) {
				case SHOW_UPGRADE_DIALOG:
					showUpgradeDialog();
					break;
				case NO_NEW_VERSION:
					Toast.makeText(getApplicationContext(), ChatApplication.getInstance().getResources().getString(R.string.no_version), Toast.LENGTH_LONG).show();
					break;
				case GlobalParam.MSG_NETWORK_ERROR:
					Toast.makeText(mContext,R.string.network_error,Toast.LENGTH_LONG).show();
					return;

				case GlobalParam.MSG_TIME_OUT_EXCEPTION:
					String message=(String)msg.obj;
					if (message==null || message.equals("")) {
						message= ChatApplication.getInstance().getResources().getString(R.string.timeout);
					}
					Toast.makeText(mContext,message, Toast.LENGTH_LONG).show();
					break;
				default:
					break;
			}
		}

	};


    /**
     * 检测是否为最新版本
     */
    private void checkUpgrade(){
        new Thread(){
            @Override
            public void run() {
                if(IMCommon.verifyNetwork(mContext)){
                    try {
                        Log.d("info","检查升级功能");
                        VersionInfo versionInfo = IMCommon.getIMServerAPI().checkUpgrade(FeatureFunction.getAppVersionName(mContext));
                        if(versionInfo != null && versionInfo.mVersion!=null && versionInfo.mState != null && versionInfo.mState.code == 0){
                            mVersion = versionInfo.mVersion;
                            mClientUpgrade = new AppUpgrade();
                            mVersion = versionInfo.mVersion;
                            if(mClientUpgrade.compareVersion(FeatureFunction.getAppVersionName(mContext), mVersion.version)){
                                mHandler.sendEmptyMessage(GlobalParam.SHOW_UPGRADE_DIALOG);
                                return ;
                            }else{
                                Log.d("info","没有升级");
                                mHandler.sendEmptyMessage(NO_NEW_VERSION);
                                return;
                            }
                        }else
                        {
                            mHandler.sendEmptyMessage(NO_NEW_VERSION);
                            Log.d("info","检查升级功能超时");
                        }
                        Log.d("info","检查升级功能完成");
                    } catch (IMException e) {
                        e.printStackTrace();
                        mHandler.sendEmptyMessage(GlobalParam.MSG_TIME_OUT_EXCEPTION);
                    }
                }else {
                    mHandler.sendEmptyMessage(GlobalParam.MSG_NETWORK_ERROR);
                }
            }
        }.start();
    }


    /**
     * 显示升级对话框
     */
    private void showUpgradeDialog() {
        LayoutInflater factor = (LayoutInflater) mContext.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
        View serviceView = factor.inflate(R.layout.client_dialog, null);
        TextView titleTextView = (TextView) serviceView.findViewById(R.id.title);
        titleTextView.setText(ChatApplication.getInstance().getResources().getString(R.string.check_new_version));
        TextView contentView = (TextView) serviceView.findViewById(R.id.updatelog);
        contentView.setText(mVersion.discription);
        Button okBtn = (Button)serviceView.findViewById(R.id.okbtn);
        okBtn.setText(ChatApplication.getInstance().getResources().getString(R.string.upgrade));
        okBtn.setOnClickListener(new OnClickListener(){
            @Override
            public void onClick(View v) {

                showDownloadApkDilog();

                if (mUpgradeNotifyDialog != null){
                    mUpgradeNotifyDialog.dismiss();
                    mUpgradeNotifyDialog = null;
                }
            }
        });

        Button cancelBtn = (Button)serviceView.findViewById(R.id.cancelbtn);
        cancelBtn.setText(ChatApplication.getInstance().getResources().getString(R.string.cancel));
        cancelBtn.setOnClickListener(new OnClickListener(){
            @Override
            public void onClick(View v) {
                if (mUpgradeNotifyDialog != null){
                    mUpgradeNotifyDialog.dismiss();
                    mUpgradeNotifyDialog = null;
                }
            }
        });

        AlertDialog.Builder builder = new AlertDialog.Builder(mContext);
        mUpgradeNotifyDialog = builder.create();
        mUpgradeNotifyDialog.show();
        mUpgradeNotifyDialog.setContentView(serviceView);
        FrameLayout.LayoutParams layout = new FrameLayout.LayoutParams(LayoutParams.FILL_PARENT,LayoutParams.WRAP_CONTENT);
        layout.setMargins(FeatureFunction.dip2px(mContext, 10), 0, FeatureFunction.dip2px(mContext, 10), 0);
        serviceView.setLayoutParams(layout);
    }

    private void showDownloadApkDilog() {
        if (mVersion != null) {
            try {
                Uri uri = Uri.parse(mVersion.downloadUrl);
                Intent intent = new Intent(Intent.ACTION_VIEW, uri);
                startActivity(intent);
            } catch (Exception e) {
                e.printStackTrace();
            }

        }
    }


}
