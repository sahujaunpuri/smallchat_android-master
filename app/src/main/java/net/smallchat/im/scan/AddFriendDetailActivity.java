package net.smallchat.im.scan;

import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import net.smallchat.im.Entity.IMResponseState;
import net.smallchat.im.Entity.Login;
import net.smallchat.im.Entity.LoginResult;
import net.smallchat.im.global.GlobalParam;
import net.smallchat.im.global.IMCommon;
import net.smallchat.im.global.ImageLoader;
import net.smallchat.im.api.IMException;

import net.smallchat.im.BaseActivity;
import net.smallchat.im.R;

/**
 * 加好友详情
 * @author dongli
 *
 */
public class AddFriendDetailActivity extends BaseActivity {

    private TextView mPersonNameTextView,mPersonSignTextView,mPersonSexTextView,mPersonAreaTextView;
    private ImageView mPersonHeaderImageView;
    private Button mJoinBtn;

    private Login mPersonDetail;
    private ImageLoader mImageLoader;

    /*
     * 导入控件
     * (non-Javadoc)
     * @see net.smallchat.im.BaseActivity#onCreate(android.os.Bundle)
     */
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        Log.d("QRCODE","ADD FRIEND DETAIL");
        super.onCreate(savedInstanceState);
        mContext = this;
        setContentView(R.layout.add_friend_detail);
        mImageLoader = new ImageLoader();

        initCompent();
        String userId = getIntent().getStringExtra("userId");
        getUserDetail(userId);
    }

    /*
     * 实例化控件
     */
    private void initCompent(){
        setTitleContent(R.drawable.back_btn,0,0);
        mLeftBtn.setOnClickListener(this);
        mPersonHeaderImageView =(ImageView) findViewById(R.id.person_header_image);
        mPersonNameTextView = (TextView)findViewById(R.id.person_name);
        mPersonSignTextView = (TextView)findViewById(R.id.person_sign);
        mPersonSexTextView = (TextView)findViewById(R.id.person_sex);
        mPersonAreaTextView = (TextView)findViewById(R.id.person_area);
        mJoinBtn = (Button)findViewById(R.id.join_btn);
        mJoinBtn.setOnClickListener(this);
        updateUIData();
        Log.d("QRCODE","ADD FRIEND DETAIL INIT UI  FINISH.");
    }

    /*
     * 给控件设置文本
     */
    private void updateUIData(){
        if(mPersonDetail!=null) {
            mPersonNameTextView.setText(mPersonDetail.nickname);
            mPersonAreaTextView.setText("地区未知");
            //mPersonAreaTextView.updateUIData(mPersonDetail.cityid);
            if (mPersonDetail.gender == 0) {//0-男 1-女 2-未填写
                mPersonSexTextView.setText("男");
            } else if (mPersonDetail.gender == 1) {
                mPersonSexTextView.setText("女");
            } else {
                mPersonSexTextView.setText("性别未知");
            }

            mPersonSignTextView.setText(mPersonDetail.sign);
            setHeader();
        }else {
            mPersonNameTextView.setText("更在获取中...");
            mPersonAreaTextView.setText("更在获取中...");
            mPersonSexTextView.setText("正在获取中...");
            mPersonSignTextView.setText("正在获取中...");
        }
    }

    /*
     * 显示用户头像
     */
    private void setHeader(){
        try {
            mImageLoader.getBitmap(mContext, mPersonHeaderImageView, null, mPersonDetail.headSmall, 0, false, true);
        }catch (Exception ex){
            ex.printStackTrace();
        }
    }

    /**
     * 扫码加入好友
     */
    private void getUserDetail(final String userId){
        new Thread(){
            @Override
            public void run(){
                if(IMCommon.verifyNetwork(mContext)){
                    try {
                        Log.e("QRCODE", "start get user info ,userId:"+userId);
                        LoginResult result = IMCommon.getIMServerAPI().getUserInfo(userId);
                        if(result != null && result.mState != null && result.mState.code == 0){
                            mPersonDetail=result.mLogin;
                            Message msg=new Message();
                            //更新界面
                            msg.what=GlobalParam.MSG_UPDATE_ADD_FRIEND_DETAIL;
                            mHandler.sendMessage(msg);
                            Log.d("QRCODE","ADD FRIEND DETAIL GET  USER INFO FINISH.");
                        }
                    } catch (IMException e) {
                        e.printStackTrace();
                        IMCommon.sendMsg(mBaseHandler, BASE_MSG_TIMEOUT_ERROR,
                                mContext.getResources().getString(R.string.timeout));
                    }

                }else {
                    mBaseHandler.sendEmptyMessage(BASE_MSG_NETWORK_ERROR);
                }
            }
        }.start();
    }

    /**
     * 加好友
     * @param
     */
    private void join(){
        new Thread(){
            @Override
            public void run(){
                if(IMCommon.verifyNetwork(mContext)){
                    try {
                        Login login=IMCommon.getLoginResult(mContext);
                        String reason="";
                        IMResponseState state = IMCommon.getIMServerAPI().applyFriends(login.uid,mPersonDetail.uid,reason);
                        mBaseHandler.sendEmptyMessage(BASE_HIDE_PROGRESS_DIALOG);
                        if(state != null && state.code == 0){
                            IMCommon.sendMsg(mHandler, GlobalParam.MSG_CHECK_STATE, state);
                        }else {
                            Message msg=new Message();
                            msg.what=GlobalParam.MSG_LOAD_ERROR;
                            if(state != null && state.errorMsg != null && !state.errorMsg.equals("")){
                                msg.obj = state.errorMsg;
                            }else {
                                msg.obj = mContext.getString(R.string.operate_failed);
                            }
                            mHandler.sendMessage(msg);
                        }
                    } catch (IMException e) {
                        e.printStackTrace();
                        IMCommon.sendMsg(mBaseHandler, BASE_MSG_TIMEOUT_ERROR,
                                mContext.getResources().getString(R.string.timeout));
                    }catch (Exception e) {
                        e.printStackTrace();
                        mBaseHandler.sendEmptyMessage(BASE_HIDE_PROGRESS_DIALOG);

                    }

                }else {
                    mBaseHandler.sendEmptyMessage(BASE_MSG_NETWORK_ERROR);
                }
            }
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
                AddFriendDetailActivity.this.finish();
                break;
            case R.id.join_btn:
                join();
                break;

            default:
                break;
        }
    }


    /*
     * 处理消息
     */
    private Handler mHandler = new Handler() {

        @Override
        public void handleMessage(Message msg) {
            switch (msg.what) {

                case GlobalParam.MSG_CHECK_STATE:
                    IMResponseState state = (IMResponseState)msg.obj;
                    if(state!=null&&state.code==0){
                        AddFriendDetailActivity.this.finish();
                    }
                    break;
                case GlobalParam.MSG_UPDATE_ADD_FRIEND_DETAIL://更新数据
                    updateUIData();
                    break;
                case GlobalParam.MSG_LOAD_ERROR:
                    hideProgressDialog();
                    String error_Detail = (String)msg.obj;
                    if(error_Detail != null && !error_Detail.equals("")){
                        Toast.makeText(mContext,error_Detail,Toast.LENGTH_LONG).show();
                    }else{
                        Toast.makeText(mContext, R.string.load_error,Toast.LENGTH_LONG).show();
                    }
                    break;
            }
        }
    };
}
