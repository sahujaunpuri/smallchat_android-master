package net.smallchat.im.global;

import android.annotation.SuppressLint;
import android.content.ContentUris;
import android.content.Context;
import android.database.Cursor;
import android.net.Uri;
import android.os.Build;
import android.os.Environment;
import android.os.StatFs;
import android.provider.DocumentsContract;
import android.provider.MediaStore;

import net.smallchat.im.R;
import net.smallchat.im.utils.FileSizeConverter;

import java.io.File;

public class FileTool {
    public static final int FILE_SIZE_B =1 ;
    public static final int FILE_SIZE_KB =1 ;
    public static final int FILE_SIZE_MB =2 ;
    public static final int FILE_SIZE_GB =3 ;

    /**
	 * 文件重命名
	 * @param file 要重命名的文件
	 * @param newName 命名的字!
	 * @return
	 * 作者:fighter <br />
	 * 创建时间:2013-3-4<br />
	 * 修改时间:<br />
	 */
	public static boolean reNameFile(File file, String newName){
		return file.renameTo(new File(file.getParentFile(), newName));
	}
	
	/**
	 * SD卡可用容量
	 * @return 字节数
	 *  -1  SD card 读取空间错误!
	 * 作者:fighter <br />
	 * 创建时间:2013-3-4<br />
	 * 修改时间:<br />
	 */
	public static long SDCardAvailable(){
		try {
			StatFs statFs = new StatFs(getExternalDirectory());
			return (long)statFs.getBlockSize() * (long)statFs.getAvailableBlocks();
		} catch (Exception e) {
			e.printStackTrace();
			return -1;
		}
	}
	
	/**
	 * SD卡容量是否还有可用容量 ( 基数为 40MB )
	 * @return
	 * 作者:fighter <br />
	 * 创建时间:2013-4-16<br />
	 * 修改时间:<br />
	 */
	public static boolean isSDCardAvailable(){
		long volume = SDCardAvailable();
		long mb = 1024 * 1024 * 40;
		if(volume > mb){
			return true;
		}else{
			return false;
		}
	}
	
	public static String getExternalDirectory(){
		return android.os.Environment.getExternalStorageDirectory().getAbsolutePath();
	} 
	
	/**
	 * SD卡是否可用
	 * @return
	 * 作者:fighter <br />
	 * 创建时间:2013-5-6<br />
	 * 修改时间:<br />
	 */
	public static boolean isMounted(){
		return android.os.Environment.MEDIA_MOUNTED.equals(android.os.Environment.getExternalStorageState());
	}
	/**
	 * 判断两个字符串大小
	 * */
	public static boolean isSort(String str1,String str2){
		if(str1.hashCode()<=str2.hashCode()){
			return true;
		}else{
		return false;
		}
	}




    /**
     * 获取URI的文件路径
     * @param context
     * @param uri
     * @return 文件绝对路径
     */
    public static String getFilePath(Context context, Uri uri) {
        String path;
        if ("file".equalsIgnoreCase(uri.getScheme())){//使用第三方应用打开
            path = uri.getPath();

            return path;
        }
        if (Build.VERSION.SDK_INT > Build.VERSION_CODES.KITKAT) {//4.4以后
            path = getPath(context, uri);

            return path;
        } else {//4.4以下下系统调用方法
            path = getRealPathFromURI(context, uri);
            return path;
        }
    }


    public static String getRealPathFromURI(Context context,Uri uri) {
        String res = null;
        String[] proj = { MediaStore.Images.Media.DATA };
        Cursor cursor = context.getContentResolver().query(uri, proj, null, null, null);
        if(null!=cursor&&cursor.moveToFirst()){;
            int column_index = cursor.getColumnIndexOrThrow(MediaStore.Images.Media.DATA);
            res = cursor.getString(column_index);
            cursor.close();
        }
        return res;
    }



    /**
     * 专为Android4.4设计的从Uri获取文件绝对路径，以前的方法已不好使
     */
    @SuppressLint("NewApi")
    public static String getPath(final Context context, final Uri uri) {

        final boolean isKitKat = Build.VERSION.SDK_INT >= Build.VERSION_CODES.KITKAT;

        // DocumentProvider
        if (isKitKat && DocumentsContract.isDocumentUri(context, uri)) {
            // ExternalStorageProvider
            if (isExternalStorageDocument(uri)) {
                final String docId = DocumentsContract.getDocumentId(uri);
                final String[] split = docId.split(":");
                final String type = split[0];

                if ("primary".equalsIgnoreCase(type)) {
                    return Environment.getExternalStorageDirectory() + "/" + split[1];
                }
            }
            // DownloadsProvider
            else if (isDownloadsDocument(uri)) {

                final String id = DocumentsContract.getDocumentId(uri);
                final Uri contentUri = ContentUris.withAppendedId(
                        Uri.parse("content://downloads/public_downloads"), Long.valueOf(id));

                return getDataColumn(context, contentUri, null, null);
            }
            // MediaProvider
            else if (isMediaDocument(uri)) {
                final String docId = DocumentsContract.getDocumentId(uri);
                final String[] split = docId.split(":");
                final String type = split[0];

                Uri contentUri = null;
                if ("image".equals(type)) {
                    contentUri = MediaStore.Images.Media.EXTERNAL_CONTENT_URI;
                } else if ("video".equals(type)) {
                    contentUri = MediaStore.Video.Media.EXTERNAL_CONTENT_URI;
                } else if ("audio".equals(type)) {
                    contentUri = MediaStore.Audio.Media.EXTERNAL_CONTENT_URI;
                }

                final String selection = "_id=?";
                final String[] selectionArgs = new String[]{split[1]};

                return getDataColumn(context, contentUri, selection, selectionArgs);
            }
        }
        // MediaStore (and general)
        else if ("content".equalsIgnoreCase(uri.getScheme())) {
            return getDataColumn(context, uri, null, null);
        }
        // File
        else if ("file".equalsIgnoreCase(uri.getScheme())) {
            return uri.getPath();
        }
        return null;
    }

    /**
     * Get the value of the data column for this Uri. This is useful for
     * MediaStore Uris, and other file-based ContentProviders.
     *
     * @param context       The context.
     * @param uri           The Uri to query.
     * @param selection     (Optional) Filter used in the query.
     * @param selectionArgs (Optional) Selection arguments used in the query.
     * @return The value of the _data column, which is typically a file path.
     */
    public static  String getDataColumn(Context context, Uri uri, String selection,
                                String[] selectionArgs) {

        Cursor cursor = null;
        final String column = "_data";
        final String[] projection = {column};

        try {
            cursor = context.getContentResolver().query(uri, projection, selection, selectionArgs,
                    null);
            if (cursor != null && cursor.moveToFirst()) {
                final int column_index = cursor.getColumnIndexOrThrow(column);
                return cursor.getString(column_index);
            }
        } finally {
            if (cursor != null)
                cursor.close();
        }
        return null;
    }

    /**
     * @param uri The Uri to check.
     * @return Whether the Uri authority is ExternalStorageProvider.
     */
    public static  boolean isExternalStorageDocument(Uri uri) {
        return "com.android.externalstorage.documents".equals(uri.getAuthority());
    }

    /**
     * @param uri The Uri to check.
     * @return Whether the Uri authority is DownloadsProvider.
     */
    public static  boolean isDownloadsDocument(Uri uri) {
        return "com.android.providers.downloads.documents".equals(uri.getAuthority());
    }

    /**
     * @param uri The Uri to check.
     * @return Whether the Uri authority is MediaProvider.
     */
    public static boolean isMediaDocument(Uri uri) {
        return "com.android.providers.media.documents".equals(uri.getAuthority());
    }

    /**
     * 获取文件名
     * @param filePath
     * @return
     */
    public static String getFileName(String filePath){
        File f =new File(filePath);
        String fileName=f.getName();
        return fileName;
    }

	/**
	 * 获取文件后缀名
	 * @param filename
	 * @return
	 */
	public static String getFileExt(String filename){
        if ((filename != null) && (filename.length() > 0)) {
            int dot = filename.lastIndexOf('.');
            if ((dot >-1) && (dot < (filename.length() - 1))) {
                return filename.substring(dot + 1);
            }
        }
        return filename;
    }

    /**
     * 获取文件大小
     * @param filePath
     * @return
     */
    public static long getFileSize(String filePath){
        File f =new File(filePath);
        try {
            return f.length();
        } catch (Exception e) {

            e.printStackTrace();
            //return 0;
        }
        return 0;
    }


    /**
     * 获取人类友好文件大小
     * @param filePath
     * @return
     */
    public static String getFileSizeString(String filePath){
        File f =new File(filePath);
        long size=f.length();
        return FileSizeConverter.BTrim.convert(size);
    }

    public static String getFileSizeString(long fileSize){
        return FileSizeConverter.BTrim.convert(fileSize);
    }

    public static int getFileIcon(String ext){
        if(ext.toLowerCase().contains("doc")||ext.toLowerCase().contains("wps")) {
           return R.drawable.file_icon_doc;
        }else if(ext.toLowerCase().contains("xlt")) {
            return  R.drawable.file_icon_excel;
        }else if(ext.toLowerCase().contains("pdf")) {
           return R.drawable.file_icon_pdf;
        }else if(ext.toLowerCase().contains("ppt")) {
            return R.drawable.file_icon_ppt;
        }else if(ext.toLowerCase().contains("txt")) {
            return R.drawable.file_icon_txt;
        }else if(ext.toLowerCase().contains("apk")) {
            return R.drawable.file_icon_apk;
        }else if(ext.toLowerCase().contains("mp4")||ext.toLowerCase().contains("avi")) {
            return R.drawable.file_icon_video;
        }else if(ext.toLowerCase().contains("mp3")||ext.toLowerCase().contains("wav")) {
            return R.drawable.file_icon_music;
        }else if(ext.toLowerCase().contains("jpg")||ext.toLowerCase().contains("png")||ext.toLowerCase().contains("gif")||ext.toLowerCase().contains("bmp")) {
            return R.drawable.file_icon_other;
        }

        else  {
            return R.drawable.file_icon_other;
        }
    }
}
