package net.smallchat.im;

import android.app.ProgressDialog;
import android.content.Context;
import android.content.Intent;
import android.content.pm.ApplicationInfo;
import android.content.pm.PackageManager;
import android.net.Uri;
import android.os.Bundle;
import android.os.Environment;
import android.os.Handler;
import android.os.Message;
import android.text.TextUtils;
import android.view.KeyEvent;
import android.view.View;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import net.smallchat.im.fragment.MiniBrowserFragment;
import net.smallchat.im.global.DownloadUtil;
import net.smallchat.im.global.FileTool;

import java.io.File;

import static net.smallchat.im.global.FileTool.getFileExt;

public class FilePreviewActivity extends BaseActivity {


    public ProgressDialog mProgressDialog;

    // 下载成功
    public final int DOWNLOAD_ERROR = 7;
    // 下载失败
    public final int DOWNLOAD_SUCCESS = 6;
    //建立一个MIME类型与文件后缀名的匹配表
    private static final String[][] MIME_MapTable={
            // {后缀名，MIME类型}
            { ".3gp", "video/3gpp" },
            { ".apk", "application/vnd.android.package-archive" },
            { ".asf", "video/x-ms-asf" },
            { ".avi", "video/x-msvideo" },
            { ".bin", "application/octet-stream" },
            { ".bmp", "image/bmp" },
            { ".c", "text/plain" },
            { ".class", "application/octet-stream" },
            { ".conf", "text/plain" },
            { ".cpp", "text/plain" },
            { ".doc", "application/msword" },
            { ".docx",
                    "application/vnd.openxmlformats-officedocument.wordprocessingml.document" },
            { ".xls", "application/vnd.ms-excel" },
            { ".xlsx",
                    "application/vnd.openxmlformats-officedocument.spreadsheetml.sheet" },
            { ".exe", "application/octet-stream" },
            { ".gif", "image/gif" },
            { ".gtar", "application/x-gtar" },
            { ".gz", "application/x-gzip" },
            { ".h", "text/plain" },
            { ".htm", "text/html" },
            { ".html", "text/html" },
            { ".jar", "application/java-archive" },
            { ".java", "text/plain" },
            { ".jpeg", "image/jpeg" },
            { ".jpg", "image/jpeg" },
            { ".js", "application/x-javascript" },
            { ".log", "text/plain" },
            { ".m3u", "audio/x-mpegurl" },
            { ".m4a", "audio/mp4a-latm" },
            { ".m4b", "audio/mp4a-latm" },
            { ".m4p", "audio/mp4a-latm" },
            { ".m4u", "video/vnd.mpegurl" },
            { ".m4v", "video/x-m4v" },
            { ".mov", "video/quicktime" },
            { ".mp2", "audio/x-mpeg" },
            { ".mp3", "audio/x-mpeg" },
            { ".mp4", "video/mp4" },
            { ".mpc", "application/vnd.mpohun.certificate" },
            { ".mpe", "video/mpeg" },
            { ".mpeg", "video/mpeg" },
            { ".mpg", "video/mpeg" },
            { ".mpg4", "video/mp4" },
            { ".mpga", "audio/mpeg" },
            { ".msg", "application/vnd.ms-outlook" },
            { ".ogg", "audio/ogg" },
            { ".pdf", "application/pdf" },
            { ".png", "image/png" },
            { ".pps", "application/vnd.ms-powerpoint" },
            { ".ppt", "application/vnd.ms-powerpoint" },
            { ".pptx",
                    "application/vnd.openxmlformats-officedocument.presentationml.presentation" },
            { ".prop", "text/plain" }, { ".rc", "text/plain" },
            { ".rmvb", "audio/x-pn-realaudio" }, { ".rtf", "application/rtf" },
            { ".sh", "text/plain" }, { ".tar", "application/x-tar" },
            { ".tgz", "application/x-compressed" }, { ".txt", "text/plain" },
            { ".wav", "audio/x-wav" }, { ".wma", "audio/x-ms-wma" },
            { ".wmv", "audio/x-ms-wmv" },
            { ".wps", "application/vnd.ms-works" }, { ".xml", "text/plain" },
            { ".z", "application/x-compress" },
            { ".zip", "application/x-zip-compressed" }, { "", "*/*" }
    };

    private  TextView fileNameTextView;
    private ImageView fileIconImageView;
    private Button fileOpenButton;
    private  String fileUrl = "";
    private  String filePath ="";
    private  String fileName ="";
    private   String fileExt ="";



    Handler handler = new Handler() {
        public void handleMessage(android.os.Message msg) {
            switch (msg.what) {
                case DOWNLOAD_SUCCESS:
                    File f=(File)msg.obj;
                    // 下载成功
                    openFileByApp(FilePreviewActivity.this,f.getAbsolutePath());
                    break;
                case DOWNLOAD_ERROR:
                    // 下载失败
                    break;
                default:
                    break;
            }
        }
    };
    
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_file_preview);

        mContext = this;
        //获取传入的参数
        fileUrl = getIntent().getStringExtra("url");
        filePath = getIntent().getStringExtra("filePath");
        fileName = getIntent().getStringExtra("filename");
        fileExt = getIntent().getStringExtra("ext");
        //绑定UI
        fileNameTextView=(TextView)this.findViewById(R.id.file_preview_file_name);
        fileIconImageView=(ImageView)this.findViewById(R.id.file_preview_file_icon);
        fileOpenButton=(Button)this.findViewById(R.id.file_preview_open_btn);
        //后退按钮+更多按钮
        setTitleContent(R.drawable.back_btn, 0, R.string.file_preview_title);

        //绑定点击事件
        mLeftBtn.setOnClickListener(this);
        //mRightBtn.setOnClickListener(this);
        fileOpenButton.setOnClickListener(this);

        //显示数据
        fileNameTextView.setText(fileName);
        int fileIcon= FileTool.getFileIcon(fileExt);
        fileIconImageView.setImageResource(fileIcon);

    }

    @Override
    public void onClick(View v) {
        super.onClick(v);
        int id = v.getId();
        switch (id) {

            case R.id.left_btn://点击后退按钮
                this.finish();
                break;
            case R.id.more_btn://点击更多按钮

                break;
            case R.id.file_preview_open_btn://点击用其他APP打开文件

                String name=FileTool.getFileName(filePath);
                final File file1 = new File(
                        Environment.getExternalStorageDirectory(),
                        DownloadUtil.getFileName(name));
                if(file1.exists()){//文件存在，则直接打开
                    openFileByApp(this,file1.getAbsolutePath());
                }else{
                    mProgressDialog = new ProgressDialog(this);
                    mProgressDialog
                            .setProgressStyle(ProgressDialog.STYLE_HORIZONTAL);
                    mProgressDialog.show();
                    //不存在，则下载文件
                    new Thread() {
                        public void run() {
                            File downloadfile = DownloadUtil.downLoad(filePath,
                                    file1.getAbsolutePath(),
                                    mProgressDialog);
                            Message msg = Message.obtain();
                            if (downloadfile != null) {

                                // 下载成功
                                showToast(FilePreviewActivity.this,downloadfile.getAbsolutePath());
                                msg.obj = downloadfile;
                                msg.what = DOWNLOAD_SUCCESS;
                            } else {
                                // 提示用户下载失败.
                                msg.what = DOWNLOAD_ERROR;
                            }
                            mProgressDialog.dismiss();
                            handler.sendMessage(msg);

                        };
                    }.start();
                }
                break;

        }
    }

    @Override
    public boolean onKeyUp(int keyCode, KeyEvent event) {

        return super.onKeyUp(keyCode, event);
    }

    public void updateTitle(String title) {
        setMiniBrowserTitle(title);
    }


    public void  showToast(Context context,String content){
        Toast.makeText(context, content, Toast.LENGTH_LONG).show();
    }
    public boolean checkApkExist(Context context, String packageName){
        if (TextUtils.isEmpty(packageName))
            return false;
        try {
            ApplicationInfo info = context.getPackageManager()
                    .getApplicationInfo(packageName,
                            PackageManager.GET_UNINSTALLED_PACKAGES);
            //Timber.d(info.toString());

            return true;
        } catch (PackageManager.NameNotFoundException e) {
            e.printStackTrace();
            //Timber.d(e.toString());
            return false;
        }
    }

    /**
     *  打开文件
     * @param context
     * @param filePath
     */
    public void openFileByApp(Context context,final  String filePath) {

        String fileExt=FileTool.getFileExt(filePath);
        if(fileExt.toLowerCase()=="txt"){
            try {
                String url = filePath;
                Intent intent = new Intent(context, MiniBrowserActivity.class);
                intent.putExtra(MiniBrowserFragment.EXTRA_URL, url);
                context.startActivity(intent);
            }catch (Exception ex){
                ex.printStackTrace();
            }
        }


       if(checkApkExist(context,"office")){
           //判断是否安装office

       }else if(checkApkExist(context,"cn.wps.moffice")){
           //判断是否安装wps
       }
//
//        Intent intent = new Intent(Intent.ACTION_VIEW);
//        intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
//        File file = new File(filePath);
//        //officeFile：本地文档；type：文档MIMEType类型，可以使用文件格式后缀
//        intent.setDataAndType(Uri.fromFile(file), type);
//        if (intent.resolveActivity(context.getPackageManager())!=null){
//            context.startActivity(intent);
//        }




        try {
            File file = new File(filePath);
            //Uri uri = Uri.parse("file://"+file.getAbsolutePath());
            Intent intent = new Intent();
            intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
            //设置intent的Action属性
            intent.setAction(Intent.ACTION_VIEW);
            //获取文件file的MIME类型
            String type = getMIMEType(file);
            //设置intent的data和Type属性。
            intent.setDataAndType(/*uri*/Uri.fromFile(file), type);
            //跳转
            context.startActivity(intent);
        }catch(Exception ex){
            ex.printStackTrace();
            showToast(context,context.getString(R.string.file_preview_app_open_error));
        }

    }

    private static String getMIMEType(File file) {
        String filename=file.getName();
        String ext=getFileExt(filename);
        for (int i=0;i<MIME_MapTable.length;i++) {
            if(MIME_MapTable[i][0].contains(ext)){
                return MIME_MapTable[i][1];
            }
        }
        return "";
    }

    public void openFileByWps(Context context,File file){
        Intent intent = new Intent();
        intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
        intent.setAction(android.content.Intent.ACTION_VIEW);
        intent.setClassName("cn.wps.moffice", "cn.wps.moffice.documentmanager.PreStartActivity");
        Uri uri = Uri.fromFile(file);
        intent.setData(uri);
        context.startActivity(intent);
    }





}
