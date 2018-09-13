package net.smallchat.im.utils;

import android.graphics.Bitmap;
import android.media.MediaMetadataRetriever;
import android.os.Environment;
import android.text.TextUtils;
import android.util.Log;

import net.smallchat.im.components.multi_image_selector.utils.FileUtils;
import net.smallchat.im.global.FileTool;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.util.HashMap;

public class VideoUtils {

    public static int NETWORK=0;
    public static int LOCAL=0;


    public static long getPlayTime(String path){
        if(path.contains("http")) {
            return getDurationLong(path, NETWORK);
        }else {
            return getDurationLong(path, LOCAL);
        }
    }
    //根据url获取音视频时长，返回毫秒
    private static long  getDurationLong(String url,int type){
        String duration = null;
        MediaMetadataRetriever retriever = new MediaMetadataRetriever();
        try {
            //如果是网络路径
            if(type == NETWORK){
                retriever.setDataSource(url,new HashMap<String, String>());
            }else if(type == LOCAL){//如果是本地路径
                retriever.setDataSource(url);
            }
            duration = retriever.extractMetadata(android.media.MediaMetadataRetriever.METADATA_KEY_DURATION);
        } catch (Exception ex) {
            Log.e("error",ex.getMessage());
            Log.d("nihao", "获取音频时长失败");
        } finally {
            try {
                retriever.release();
            } catch (RuntimeException ex) {
                Log.e("error",ex.getMessage());
                Log.d("nihao", "释放MediaMetadataRetriever资源失败");
            }
        }
        if(!TextUtils.isEmpty(duration)){
            return Long.parseLong(duration);
        }else{
            return 0;
        }
    }

    //获取视频缩略图
    private static Bitmap createVideoThumbnail(String url, int type) {
        Bitmap bitmap = null;
        MediaMetadataRetriever retriever = new MediaMetadataRetriever();
        try {
            //将网络文件以及本地文件区分开来设置
            if (type == NETWORK) {
                retriever.setDataSource(url, new HashMap<String, String>());
            } else if(type == LOCAL){
                retriever.setDataSource(url);
            }
            bitmap = retriever.getFrameAtTime(0, MediaMetadataRetriever.OPTION_NEXT_SYNC);
        } catch (IllegalArgumentException ex) {
            Log.e("error",ex.getMessage());
            Log.d("nihao", "获取视频缩略图失败");
        } finally {
            try {
                retriever.release();
            } catch (RuntimeException ex) {
                Log.e("error",ex.getMessage());
                Log.d("error", "释放MediaMetadataRetriever资源失败");
            }
        }
        return bitmap;
    }
    
    
    
    /**
     * Bitmap保存成File
     *
     * @param bitmap input bitmap
     * @param name output file's name
     * @return String output file's path
     */
    public static String bitmapToFile(Bitmap bitmap, String name) {
        File f = new File(Environment.getExternalStorageDirectory() + name + ".jpg");
        if (f.exists()) f.delete();
        FileOutputStream fOut = null;
        try {
            fOut = new FileOutputStream(f);
            bitmap.compress(Bitmap.CompressFormat.JPEG, 100, fOut);
            fOut.flush();
            fOut.close();
        } catch (IOException e) {
            return null;
        }
        return f.getAbsolutePath();
    }

    /**
     * 获取视频文件截图
     *
     * @param path 视频文件的路径
     * @return Bitmap 返回获取的Bitmap
     */
    public static Bitmap getVideoThumb(String path) {
        if(path.contains("http")) {
           return createVideoThumbnail(path, NETWORK);
        }else {
           return createVideoThumbnail(path, LOCAL);
        }
    }

    /**
     * 获取视频文件截图
     *
     * @param path 视频文件的路径
     * @return Bitmap 返回获取的Bitmap
     */
    public static String getVideoThumbPath(String path) {

        if(path==null ||path=="")return  "";
        try {
            Bitmap bitmap = null;
            String filename = FileTool.getFileName(path);
            if (path.contains("http")) {
                bitmap = createVideoThumbnail(path, NETWORK);
            } else {
                bitmap = createVideoThumbnail(path, LOCAL);
            }
            return bitmapToFile(bitmap, filename + "_thumb");
        }catch (Exception ex){
            ex.printStackTrace();
        }
        return "";
    }
}
