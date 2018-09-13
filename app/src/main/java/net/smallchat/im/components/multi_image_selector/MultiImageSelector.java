package net.smallchat.im.components.multi_image_selector;

import android.Manifest;
import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.net.Uri;
import android.os.Build;
import android.provider.MediaStore;
import android.support.v4.app.Fragment;
import android.support.v4.content.ContextCompat;
import android.widget.Toast;

import net.smallchat.im.R;
import net.smallchat.im.components.multi_image_selector.utils.FileUtils;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;

/**
 * 图片选择器
 * Created by nereo on 16/3/17.
 */
public class MultiImageSelector {

    public static final String EXTRA_RESULT = net.smallchat.im.components.multi_image_selector.MultiImageSelectorActivity.EXTRA_RESULT;

    private boolean mShowCamera = true;
    private int mMaxCount = 9;
    private int mMode = net.smallchat.im.components.multi_image_selector.MultiImageSelectorActivity.MODE_MULTI;
    private ArrayList<String> mOriginData;
    private static MultiImageSelector sSelector;
    private File mCameraTempFile=null;

    @Deprecated
    private MultiImageSelector(Context context){

    }

    private MultiImageSelector(){}

    @Deprecated
    public static MultiImageSelector create(Context context){
        if(sSelector == null){
            sSelector = new MultiImageSelector(context);
        }
        return sSelector;
    }

    public static MultiImageSelector create(){
        if(sSelector == null){
            sSelector = new MultiImageSelector();
        }
        return sSelector;
    }

    public MultiImageSelector showCamera(boolean show){
        mShowCamera = show;
        return sSelector;
    }

    public MultiImageSelector count(int count){
        mMaxCount = count;
        return sSelector;
    }

    public MultiImageSelector single(){
        mMode = net.smallchat.im.components.multi_image_selector.MultiImageSelectorActivity.MODE_SINGLE;
        return sSelector;
    }

    public MultiImageSelector multi(){
        mMode = net.smallchat.im.components.multi_image_selector.MultiImageSelectorActivity.MODE_MULTI;
        return sSelector;
    }

    public MultiImageSelector origin(ArrayList<String> images){
        mOriginData = images;
        return sSelector;
    }

    public void start(Activity activity, int requestCode){
        final Context context = activity;
        if(hasPermission(context)) {
            activity.startActivityForResult(createIntent(context), requestCode);
        }else{
            Toast.makeText(context, R.string.mis_error_no_permission, Toast.LENGTH_SHORT).show();
        }
    }

    public void start(Fragment fragment, int requestCode){
        final Context context = fragment.getContext();
        if(hasPermission(context)) {
            fragment.startActivityForResult(createIntent(context), requestCode);
        }else{
            Toast.makeText(context, R.string.mis_error_no_permission, Toast.LENGTH_SHORT).show();
        }
    }

    public void startCamera(Activity activity,int requestCode){
        final Context context = activity;
        if(hasPermission(context)) {
            activity.startActivityForResult(createCameraIntent(context), requestCode);
        }else{
            Toast.makeText(context, R.string.mis_error_no_permission, Toast.LENGTH_SHORT).show();
        }
    }

    private boolean hasPermission(Context context){
        if(Build.VERSION.SDK_INT >= Build.VERSION_CODES.JELLY_BEAN){
            // Permission was added in API Level 16
            return ContextCompat.checkSelfPermission(context, Manifest.permission.READ_EXTERNAL_STORAGE)
                    == PackageManager.PERMISSION_GRANTED;
        }
        return true;
    }

    /**
     * 开启相册选择界面
     * @param context
     * @return
     */
    private Intent createIntent(Context context){
        Intent intent = new Intent(context, net.smallchat.im.components.multi_image_selector.MultiImageSelectorActivity.class);
        intent.putExtra(net.smallchat.im.components.multi_image_selector.MultiImageSelectorActivity.EXTRA_SHOW_CAMERA, mShowCamera);
        intent.putExtra(net.smallchat.im.components.multi_image_selector.MultiImageSelectorActivity.EXTRA_SELECT_COUNT, mMaxCount);
        if(mOriginData != null){
            intent.putStringArrayListExtra(net.smallchat.im.components.multi_image_selector.MultiImageSelectorActivity.EXTRA_DEFAULT_SELECTED_LIST, mOriginData);
        }
        intent.putExtra(net.smallchat.im.components.multi_image_selector.MultiImageSelectorActivity.EXTRA_SELECT_MODE, mMode);
        return intent;
    }

    /**
     * 直接开启摄像头
     * @param context
     * @return
     */
    private Intent createCameraIntent(Context context){
        Intent intent = new Intent(MediaStore.ACTION_IMAGE_CAPTURE);

        if (intent.resolveActivity(context.getPackageManager()) != null) {
            try {
                mCameraTempFile = FileUtils.createTmpFile(context);
            } catch (IOException e) {
                e.printStackTrace();
            }
            if (mCameraTempFile != null && mCameraTempFile.exists()) {
                intent.putExtra(MediaStore.EXTRA_OUTPUT, Uri.fromFile(mCameraTempFile));
                
            } else {
                Toast.makeText(context, R.string.mis_error_image_not_exist, Toast.LENGTH_SHORT).show();
            }
        } else {
            Toast.makeText(context, R.string.mis_msg_no_camera, Toast.LENGTH_SHORT).show();
        }
        
        return intent;
    }


   public File  getCameraTempFile(){
       return mCameraTempFile;
   }
}
