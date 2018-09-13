package net.smallchat.im.mine;


import android.content.Intent;
import android.graphics.Bitmap;
import android.net.Uri;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.provider.MediaStore;
import android.util.Log;
import android.view.View;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;

import net.smallchat.im.Entity.Login;
import net.smallchat.im.dialog.MMAlert;
import net.smallchat.im.dialog.MMAlert.OnAlertSelectId;
import net.smallchat.im.global.FeatureFunction;
import net.smallchat.im.global.GlobalParam;
import net.smallchat.im.global.IMCommon;
import net.smallchat.im.global.ImageLoader;
import net.smallchat.im.global.QRCodeCommand;

import net.smallchat.im.BaseActivity;
import net.smallchat.im.R;

import java.io.File;
import java.io.FileOutputStream;

/**
 * 个人二维码页
 * @author rockman
 *
 */
public class PersonalQRCodeActivity extends BaseActivity {

    private final static int SAVE_SUCCESS = 5126;
    private LinearLayout mCodeAllLayout;
    private ImageView mCodeImageView;
    private TextView mUserNameTextView;
    private Bitmap mBitmap;
    private Login login;
    private ImageLoader mImageLoader;
    ImageView mHeaderImageView;
    private Handler mHandler = new Handler(){

        @Override
        public void handleMessage(Message msg) {
            super.handleMessage(msg);
            switch (msg.what) {
                case  GlobalParam.MSG_SHOW_HEADER_IMG:
                    mCodeImageView.setImageBitmap(mBitmap);
                    break;
                case SAVE_SUCCESS:
                    Toast.makeText(mContext, mContext.getString(R.string.save_picture_to_ablun), Toast.LENGTH_SHORT).show();
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
        setContentView(R.layout.personal_qrcode_view);
        mContext = this;
        Log.d("QRCODE","PERSONAL_QRCODE");
        try {
            mImageLoader = new ImageLoader();
        }catch (Exception ex)
        {
            ex.printStackTrace();
        }
        initCompent();
    }

    /*
     * 实例化控件
     */
    private void initCompent(){
        try {
            login = IMCommon.getLoginResult(this.mContext);
        }catch (Exception ex)
        {
            ex.printStackTrace();
        }
        setTitleContent(R.drawable.back_btn,R.drawable.more_btn,R.string.personal_code_card);
        mLeftBtn.setOnClickListener(this);
        mRightBtn.setOnClickListener(this);
        mCodeImageView = (ImageView)findViewById(R.id.qrcode_icon);
        mCodeAllLayout = (LinearLayout)findViewById(R.id.code_all_layout);
        mUserNameTextView = (TextView)findViewById(R.id.username);
        mHeaderImageView= (ImageView)findViewById(R.id.header);

        showQRCode();

        try{
            mImageLoader.getBitmap(mContext, mHeaderImageView, null, login.headSmall, 0, false, true);
        }catch (Exception ex){
            ex.printStackTrace();
        }
        //显示昵称+手机号
        mUserNameTextView.setText(login.nickname+"（"+login.phone+"）");

    }
    //显示二维码
    private void showQRCode()
    {
        new Thread(){
            public void run() {
                try {
                    mBitmap = FeatureFunction.create2DCode(QRCodeCommand.QRCodeProfileCommand + login.uid, 800, 800);
                    if (mBitmap != null) {
                        mHandler.sendEmptyMessage(GlobalParam.MSG_SHOW_HEADER_IMG);
                    }
                }catch (Exception ex)
                {
                    ex.printStackTrace();
                }
            };
        }.start();
    }
    /*
     * 显示更多菜单
     */
    private void showMoreDialog(){
        MMAlert.showAlert(mContext, null, mContext.getResources().
                        getStringArray(R.array.group_card_item),
                null, new OnAlertSelectId() {

                    @Override
                    public void onClick(int whichButton) {
                        Log.e("whichButton", "whichButton: "+whichButton);
                        switch (whichButton) {
                            case 0:{//保存二维码到手机
                                if(mBitmap!=null){
                                    savePicture();
                                }
                                break;
                            }
                            case 1:{//分享给好友
                                if(mBitmap!=null){
                                    sharePicture();
                                }
                                break;
                            }
                            default:
                                break;
                        }
                    }
                });
    }


    /*
     * 保存二维码到本地
     */
    private void savePicture(){
        new Thread(){
            @Override
            public void run(){
                String fileName = FeatureFunction.getPhotoFileName(0);


                mCodeAllLayout.setDrawingCacheEnabled(true);
                mCodeAllLayout.buildDrawingCache(true);
                Bitmap bitmap=mCodeAllLayout.getDrawingCache();
                Bitmap bitmapTwo=Bitmap.createBitmap(bitmap);
                mCodeAllLayout.setDrawingCacheEnabled(false);
                mCodeAllLayout.destroyDrawingCache();

                if(bitmapTwo == null){
                    return;
                }

                String uri = MediaStore.Images.Media.insertImage(getContentResolver(), bitmapTwo, fileName, "");
                String filePath = FeatureFunction.getFilePathByContentResolver(mContext, Uri.parse(uri));
                int index = filePath.lastIndexOf("/");
                String filePrefix = filePath.substring(0, index + 1) + fileName;
                try {
                    File bitmapFile = new File(filePrefix);
                    FileOutputStream bitmapWriter;
                    bitmapWriter = new FileOutputStream(bitmapFile);
                    if (bitmapTwo.compress(Bitmap.CompressFormat.JPEG, 100, bitmapWriter)) {
                        File oldfile = new File(filePath);
                        if(oldfile.exists()){
                            oldfile.delete();
                        }
                        bitmapWriter.flush();
                        bitmapWriter.close();
                        Intent intent = new Intent(Intent.ACTION_MEDIA_SCANNER_SCAN_FILE);
                        Uri refreshUri = Uri.fromFile(bitmapFile);
                        intent.setData(refreshUri);
                        mContext.sendBroadcast(intent);
                        mHandler.sendEmptyMessage(SAVE_SUCCESS);
                    }
                }catch (Exception e) {
                    e.printStackTrace();
                }
            }
        }.start();

    }
    /*
     * 分享二维码给好友
     */
    private void sharePicture(){
        new Thread(){
            @Override
            public void run(){
                String fileName = FeatureFunction.getPhotoFileName(0);


                mCodeAllLayout.setDrawingCacheEnabled(true);
                mCodeAllLayout.buildDrawingCache(true);
                Bitmap bitmap=mCodeAllLayout.getDrawingCache();
                Bitmap bitmapTwo=Bitmap.createBitmap(bitmap);
                mCodeAllLayout.setDrawingCacheEnabled(false);
                mCodeAllLayout.destroyDrawingCache();

                if(bitmapTwo == null){
                    return;
                }

                String uri = MediaStore.Images.Media.insertImage(getContentResolver(), bitmapTwo, fileName, "");
                String filePath = FeatureFunction.getFilePathByContentResolver(mContext, Uri.parse(uri));
                int index = filePath.lastIndexOf("/");
                String filePrefix = filePath.substring(0, index + 1) + fileName;
                try {
                    File bitmapFile = new File(filePrefix);
                    FileOutputStream bitmapWriter;
                    bitmapWriter = new FileOutputStream(bitmapFile);
                    if (bitmapTwo.compress(Bitmap.CompressFormat.JPEG, 100, bitmapWriter)) {
                        File oldfile = new File(filePath);
                        if(oldfile.exists()){
                            oldfile.delete();
                        }
                        bitmapWriter.flush();
                        bitmapWriter.close();
                        Intent intent = new Intent(Intent.ACTION_MEDIA_SCANNER_SCAN_FILE);
                        Uri refreshUri = Uri.fromFile(bitmapFile);
                        intent.setData(refreshUri);
                        mContext.sendBroadcast(intent);
                        mHandler.sendEmptyMessage(SAVE_SUCCESS);
                    }
                }catch (Exception e) {
                    e.printStackTrace();
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
                PersonalQRCodeActivity.this.finish();
                break;
            case R.id.right_btn:

                showMoreDialog();
                break;

            default:
                break;
        }
    }


    /*
     * 释放图片
     * (non-Javadoc)
     * @see net.smallchat.im.BaseActivity#onDestroy()
     */
    @Override
    protected void onDestroy() {
        super.onDestroy();
        if(mBitmap!=null){
            mCodeImageView.setImageBitmap(null);
            mBitmap.recycle();
        }
    }




}