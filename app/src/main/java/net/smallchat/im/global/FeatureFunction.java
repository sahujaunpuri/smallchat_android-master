package net.smallchat.im.global;

import java.io.BufferedOutputStream;
import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.lang.reflect.Field;
import java.net.HttpURLConnection;
import java.net.MalformedURLException;
import java.net.URL;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.text.Collator;
import java.text.DecimalFormat;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Date;
import java.util.GregorianCalendar;
import java.util.HashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import android.app.ActivityManager;
import android.app.ActivityManager.RunningAppProcessInfo;
import android.content.Context;
import android.content.pm.PackageInfo;
import android.content.pm.PackageManager;
import android.database.Cursor;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.BitmapFactory.Options;
import android.net.Uri;
import android.os.Environment;
import android.provider.MediaStore.MediaColumns;
import android.text.TextUtils;
import android.util.Base64;
import android.util.Log;

import com.alibaba.fastjson.JSON;
import com.google.zxing.BarcodeFormat;
import com.google.zxing.MultiFormatWriter;
import com.google.zxing.WriterException;
import com.google.zxing.common.BitMatrix;
import net.smallchat.im.R;
import net.smallchat.im.exception.SPException;
import net.smallchat.im.service.ApiServiceThread;

import static net.smallchat.im.config.APIConfig.getQrcodeUrl;

public class FeatureFunction {
	private static final String TAG = "FeatureFunction";
	private static final int ONE_MINUTE = 60; // Seconds
	private static final int ONE_HOUR = 60 * ONE_MINUTE;
	private static final int ONE_DAY = 24 * ONE_HOUR;

	public static final String PUB_TEMP_DIRECTORY = "/IM/";

	public static byte[] getImage(URL path, File file) throws SPException{
		HttpURLConnection conn = null;
		InputStream is = null;
		byte[] imgData = null;

		try {
			URL url = path;
			conn = (HttpURLConnection) url.openConnection();
			is = conn.getInputStream();
			// Get the length
			ByteArrayOutputStream bos = new ByteArrayOutputStream();

			byte[] temp = new byte[512];
			int readLen = 0;
			int destPos = 0;
			while ((readLen = is.read(temp)) > 0) {
				bos.write(temp, 0, readLen);
				destPos += readLen;
			}

			if (destPos != 0) {
				imgData = new byte[destPos];
				System.arraycopy(bos.toByteArray(), 0, imgData, 0, destPos);

				// Save to cache
				if (file != null){
					writeBitmapToCache(imgData, file);
				}
			}

			if(is != null){
				is.close();
			}

			if(conn != null){
				conn.disconnect();
			}
		} catch (MalformedURLException e) {
			e.printStackTrace();
		} catch (IOException e) {
			e.printStackTrace();
		}catch (OutOfMemoryError e){
			//throw new SPException(R.string.exception_out_of_memory);
		}

		return imgData;
	}

	private static void writeBitmapToCache(byte[] imgData, File file) {

		FileOutputStream fos = null;
		BufferedOutputStream outPutBuffer = null;

		if (file != null) {
			try {
				if (!file.exists()) {
					file.createNewFile();
				}
				fos = new FileOutputStream(file);

				outPutBuffer = new BufferedOutputStream(fos);
				outPutBuffer.write(imgData);
				outPutBuffer.flush();
				fos.flush();

			} catch (IOException e) {
				e.printStackTrace();
			} finally {
				try {
					if (fos != null) {
						fos.close();
					}

					if (outPutBuffer != null) {
						outPutBuffer.close();
					}
				} catch (IOException e) {
					e.printStackTrace();
				}
			}
		}
	}


	public static Bitmap downLoadImage(final String mImageUrl){
		Bitmap bitmap = null;
		if(mImageUrl != null){
			//Log.e("position", "position = " + position);
			File file = null;
			String fileName = new MD5().getMD5ofStr(mImageUrl);// url.replaceAll("/",
			if (FeatureFunction.checkSDCard()) {

				if (FeatureFunction.newFolder(Environment.getExternalStorageDirectory()
						+ ImageLoader.SDCARD_PICTURE_CACHE_PATH)) {
					file = new File(
							Environment.getExternalStorageDirectory()
							+ ImageLoader.SDCARD_PICTURE_CACHE_PATH, fileName);
					if(file != null && file.exists()){
						try {
							FileInputStream fin = new FileInputStream(file.getPath());
							int length = fin.available();
							byte[] buffer = new byte[length];
							fin.read(buffer);
							fin.close();
							bitmap = BitmapFactory.decodeByteArray(buffer, 0,
									buffer.length);
						} catch (FileNotFoundException e) {
							e.printStackTrace();
						} catch (IOException e) {
							e.printStackTrace();
						}

					}else{
						if (IMCommon.getNetWorkState()){
							bitmap = loadImageFromUrl(mImageUrl,file);
						}
					}
				}
			}
		}
		return bitmap;

	}
	private static Bitmap loadImageFromUrl(String urlString, File file) {
		Bitmap bitmap = null;
		HttpURLConnection conn = null;
		InputStream is = null;

		try {
			URL url = new URL(urlString);
			conn = (HttpURLConnection) url.openConnection();
			is = conn.getInputStream();
			// Get the length
			int length = (int) conn.getContentLength();
			if (length != -1) {
				ByteArrayOutputStream outstream = new ByteArrayOutputStream();
				//byte[] imgData = new byte[length];
				byte[] temp = new byte[512];
				int readLen = 0;
				int destPos = 0;
				while ((readLen = is.read(temp)) > 0) {
					outstream.write(temp, 0, readLen);
					destPos += readLen;
				}

				byte[] imgData = new byte[destPos];
				System.arraycopy(outstream.toByteArray(), 0, imgData, 0, destPos);

				bitmap = BitmapFactory.decodeByteArray(imgData, 0,
						imgData.length);
				// Save to cache
				if (file != null){
					writeBitmapToCache(imgData, file);
				}
			}

			if (is != null) {
				is.close();
			}

			if (conn != null) {
				conn.disconnect();
			}
		} catch (MalformedURLException e) {
			e.printStackTrace();
		} catch (IOException e) {
			e.printStackTrace();
		} catch (OutOfMemoryError e) {
			e.printStackTrace();
		}

		return bitmap;
	}



	/**
	 * Try to decode a image file with file path
	 * 
	 * @param filePath
	 *            image file path
	 * @param quanlity
	 *            the compress rate
	 * @param autoCompress
	 *            if need to compress more if OOM occurs
	 * @return the decoded bitmap or null if failed
	 */
	public static Bitmap tryToDecodeImageFile(String filePath, int quanlity,
			boolean autoCompress) {
		Bitmap bitmap = null;
		try {
			if (quanlity == 1) {
				bitmap = BitmapFactory.decodeFile(filePath);
			} else {
				BitmapFactory.Options options = new Options();
				options.inSampleSize = quanlity;
				bitmap = BitmapFactory.decodeFile(filePath, options);
			}
		} catch (OutOfMemoryError oe) {
			if (autoCompress) {
				int rate = (quanlity >= 4) ? 2 : 4;
				Log.d(TAG, "Decode the file automatically with quanlity :"
						+ quanlity * rate);
				bitmap = tryToDecodeImageFile(filePath, quanlity * rate, false);
			} else {
				Log.e(TAG, "Decode the file failed!, out of memory!");
				oe.printStackTrace();
			}
		} catch (Exception e) {
			e.printStackTrace();
		}

		return bitmap;
	}

	/**
	 * Check SD card
	 * 
	 * @return true if SD card is mounted
	 */
	public static boolean checkSDCard() {
		if (Environment.getExternalStorageState().equals(
				Environment.MEDIA_MOUNTED))
			return true;
		else
			return false;
	}

	/**  
	 * @author LuoB.  
	 * @param oldTime 较小的时间  
	 * @param newTime 较大的时间 (如果为空   默认当前时间 ,表示和当前时间相比)  
	 * @return -1 ：同一天.    0：昨天 .   1 ：至少是前天.  
	 * @throws ParseException 转换异常  
	 */  
	public static int isYeaterday(Date oldTime,Date newTime) throws ParseException{   
		if(newTime==null){   
			newTime=new Date();   
		}   
		//将下面的 理解成  yyyy-MM-dd 00：00：00 更好理解点   
		SimpleDateFormat format = new SimpleDateFormat("yyyy-MM-dd");   
		String todayStr = format.format(newTime);   
		Date today = format.parse(todayStr);   
		//昨天 86400000=24*60*60*1000 一天   
		if((today.getTime()-oldTime.getTime())>0 && (today.getTime()-oldTime.getTime())<=86400000) {   
			return 0;   
		}   
		else if((today.getTime()-oldTime.getTime())<=0){ //至少是今天   
			return -1;   
		}   
		else{ //至少是前天   
			return 1;   
		}   

	}

	public static String calculaterReleasedTime(Context context, Date date,long time,long lastMsgTime) {
		Date currentDate = new Date();
		long duration = (currentDate.getTime() - date.getTime()) / 1000; // Seconds

		if(lastMsgTime!=0){
			//long checkDuration = (new Date(time).getTime() - new Date(lastMsgTime).getTime()) / 1000; // Seconds
			long duration1 = (time - lastMsgTime) / 1000;
			if(duration1 <=3*ONE_MINUTE){
				return "";
			}
		}
		try {
			if(isYeaterday(date, currentDate) ==0){
				SimpleDateFormat	format =null;
				if(lastMsgTime == 0){
					format =new SimpleDateFormat("MM月dd日"); //new SimpleDateFormat("HH:mm:ss");
				}else{
					format =new SimpleDateFormat("HH:mm"); //new SimpleDateFormat("HH:mm:ss");
				}


				return format.format(date);
			}
		} catch (ParseException e) {
			e.printStackTrace();
		}

		// Not normal
		if (currentDate.before(date)) {
			if (Math.abs(duration) < ONE_MINUTE * 5) {//不足一分钟
				return context.getString(R.string.just_now);
			} else {
				return getDateString(context, date,
						currentDate.getYear() != date.getYear(),lastMsgTime!=0?true:false);
			}
		}



		if (duration >= ONE_DAY) {
			return getDateString(context, date,
					currentDate.getYear() != date.getYear(),lastMsgTime!=0?true:false);
			/*return getTime(time);*/
		}else if (duration >= ONE_HOUR) {
			/*return duration / ONE_HOUR + context.getString(R.string.hour)
					+ context.getString(R.string.before);*/
			SimpleDateFormat	format =new SimpleDateFormat("HH:mm");
			return format.format(date);
			//return getTime(time,false);
		} else if (duration >= ONE_MINUTE) {
			return duration / ONE_MINUTE + context.getString(R.string.minutes_time)
					+ context.getString(R.string.before);
		} else {
			return duration + context.getString(R.string.second)
					+ context.getString(R.string.before);
		}
	}

	public static String calculateFileSize(long size) {
		if (size < 1024l) {
			return size + "B";
		} else if (size < (1024l * 1024l)) {
			return Math.round((size * 100 >> 10)) / 100.00 + "KB";
		} else if (size < (1024l * 1024l * 1024l)) {
			return (Math.round((size * 100 >> 20)) / 100.00) + "MB";
		} else {
			return Math.round((size * 100 >> 30)) / 100.00 + "GB";
		}
	}

	public static String getDateString(Context context, Date date,
			boolean withYearString,boolean isShowMM) {
		String timeString = "";
		SimpleDateFormat format = null;
		if (withYearString) {
			if(isShowMM){
				format = new SimpleDateFormat("yyyy.MM.dd HH:mm");

			}else{
				format = new SimpleDateFormat("yyyy.MM.dd");
			}

		}else{
			if(isShowMM){
				format = new SimpleDateFormat("MM月dd日 HH:mm");
			}else{
				format = new SimpleDateFormat("MM月dd日 ");
			}
		}
		return format.format(date);
	}

	public static String getDateStringFormate(Context context, Date date,
			boolean withYearString,boolean isShowMins) {
		String time = "";
		if (withYearString) {
			if(isShowMins){
				time = (date.getYear() + 1900) + "."
						+ (date.getMonth() + 1)+"."
						+ date.getDate()+ " "
						+ date.getHours()+":"
						+ date.getMinutes();
			}else{
				time = (date.getYear() + 1900) + "."
						+ (date.getMonth() + 1)+"."
						+ date.getDate();
			}

		}else{
			if(isShowMins){
				time =  (date.getMonth() + 1) + context.getString(R.string.month)
						+ date.getDate() + context.getString(R.string.day)
						+ date.getHours() +":"
						+ date.getMinutes();
			}else{
				time =  (date.getMonth() + 1) + context.getString(R.string.month)
						+ date.getDate() + context.getString(R.string.day);
			}

		}



		return time;
	}


	public static int chineseCompare(String chineseString1,
			String chineseString2) {
		return Collator.getInstance(Locale.CHINESE).compare(chineseString1,
				chineseString2);
	}

	public static boolean createWholePermissionFolder(String path) {
		Log.d(TAG, "+ createWholePermissionFolder()");

		Process p;
		int status = -1;
		boolean isSuccess = false;

		try {
			File destDir = new File(path);
			if (!destDir.exists()) {
				destDir.mkdirs();
			}

			p = Runtime.getRuntime().exec("chmod 777 " + destDir);
			status = p.waitFor();
			if (status == 0) {
				Log.d(TAG, "Modify folder permission success!");
				isSuccess = true;
			} else {
				Log.e(TAG, "Modify folder permission fail!");
			}
		} catch (Exception e) {
			Log.e(TAG, "Modify folder permission exception!: " + e.toString());
		}

		Log.d(TAG, "- createWholePermissionFolder()");
		return isSuccess;
	}

	public static String saveTempBitmap(Bitmap bitmap, String fileName) {
		if (bitmap == null || fileName == null || fileName.length() == 0) {
			Log.e(TAG, "saveTempBitmap(), illegal param, bitmap = " + bitmap
					+ "filename = " + fileName);
			return "";
		}

		createWholePermissionFolder(Environment.getExternalStorageDirectory() + PUB_TEMP_DIRECTORY);
		File bitmapFile = new File(Environment.getExternalStorageDirectory() + PUB_TEMP_DIRECTORY, fileName);
		FileOutputStream bitmapWriter;
		String retPath = "";
		try {
			bitmapWriter = new FileOutputStream(bitmapFile);
			if (bitmap.compress(Bitmap.CompressFormat.JPEG, 75, bitmapWriter)) {
				Log.d("TAG", "Save picture successfully! file name = "
						+ PUB_TEMP_DIRECTORY + fileName);
				bitmapWriter.flush();
				bitmapWriter.close();
				retPath = Environment.getExternalStorageDirectory() + PUB_TEMP_DIRECTORY + fileName;
			}
		} catch (FileNotFoundException e) {
			e.printStackTrace();
		} catch (IOException e) {
			e.printStackTrace();
		} catch (Exception e) {
			e.printStackTrace();
		}

		return retPath;
	}

	/**
	 * Judge if the characters in the string are all number
	 * 
	 * @param str
	 * @return
	 * @author mikewu
	 */
	public static boolean isNumeric(String str) {
		Pattern pattern = Pattern.compile("[0-9]*");
		return pattern.matcher(str).matches();
	}

	public static int dip2px(Context context, int dipValue) {
		final float scale = context.getResources().getDisplayMetrics().density;
		return (int) (dipValue * scale + 0.5f);
	}

	public static int px2dip(Context context, float pxValue) {
		final float scale = context.getResources().getDisplayMetrics().density;
		return (int) (pxValue / scale + 0.5f);
	}

	/**
	 * 比较versionName 是否比之前的大
	 * @param local
	 * @param romote
	 * @return
	 */
	public  boolean compareVersion(String local, String romote){
		boolean isNewVersion = false;
		try {
			String loaclVersion[] = local.substring(1).split("\\.");
			String romoteVersion[] = romote.split("\\.");
			int length = loaclVersion.length < romoteVersion.length ? loaclVersion.length : romoteVersion.length;
			for (int i = 0; i < length; i++) {
				if (Integer.parseInt(loaclVersion[i]) < Integer.parseInt(romoteVersion[i])) {
					isNewVersion = true;
					break;
				}
				else if (Integer.parseInt(loaclVersion[i]) > Integer.parseInt(romoteVersion[i])) {
					break;
				}
			}
		} catch (Exception e) {
			e.printStackTrace();
		}

		return isNewVersion;
	}

	public static int getAppVersion(Context context) {

		int versionCode = 0;

		try {

			PackageManager pm = context.getPackageManager();

			PackageInfo pi = pm.getPackageInfo(context.getPackageName(), 0);

			versionCode = pi.versionCode;


		} catch (Exception e) {

			Log.e(TAG, "Exception", e);

		}
		return versionCode;
	}

	public static String getAppVersionName(Context context) {

		String versionName = "";

		try {

			PackageManager pm = context.getPackageManager();

			PackageInfo pi = pm.getPackageInfo(context.getPackageName(), 0);

			versionName = pi.versionName;

			if (versionName == null || versionName.length() <= 0) {

				return "";

			}

		} catch (Exception e) {

			Log.e(TAG, "Exception", e);

		}
		return versionName;
	}

	public static String replaceHtml(String html) {
		String regEx = "<.+?>";
		Pattern p = Pattern.compile(regEx);
		Matcher m = p.matcher(html);
		String s = m.replaceAll("");
		return s;
	}

	public static void freeBitmap(HashMap<String, Bitmap> cache) {
		if (cache.isEmpty()) {
			return;
		}
		for (Bitmap bitmap : cache.values()) {
			if (bitmap != null && !bitmap.isRecycled()) {
				bitmap.recycle();
				bitmap = null;

			}
		}
		cache.clear();
		System.gc();
	}

	public static String getRefreshTime(){
		String strDate = "";
		SimpleDateFormat formatter = new SimpleDateFormat("MM-dd HH:mm:ss");     
		Date  curDate = new Date(System.currentTimeMillis());//获取当前时间     
		strDate = formatter.format(curDate);

		return strDate;
	}

	public static String getChatTime(long time){
		String strDate = "";
		SimpleDateFormat formatter = new SimpleDateFormat("yyyy");     
		Date  curDate = new Date(time);//获取当前时间     
		strDate = formatter.format(curDate);

		String toDayTime = formartTime(System.currentTimeMillis()/1000, "yyyy");
		if(toDayTime!=null && strDate!=null && strDate.equals(toDayTime)){
			strDate = formartTime(time/1000, "MM-dd HH:mm");
		}else{
			strDate = formartTime(time/1000, "yyyy-MM-dd HH:mm");
		}

		return strDate;
	}

	public static String getFormatTime(String time){
		SimpleDateFormat formatter = new SimpleDateFormat("yyyy/M/d HH:mm:ss"); 
		Date d;
		String formattime = "";
		try {
			d = formatter.parse(time);
			SimpleDateFormat formatter2 = new SimpleDateFormat("MM-dd HH:mm:ss"); 
			formattime = formatter2.format(d);
		} catch (ParseException e) {
			e.printStackTrace();
		} 

		return formattime;
	}

	public static boolean newFolder(String folderPath) {  
		try {
			String filePath = folderPath;
			File myFilePath = new File(filePath);
			if (!myFilePath.exists()) {
				myFilePath.mkdirs();
			}

			return true;
		} catch (Exception e) {
			e.printStackTrace();
			return false;
		}
	}

	public static String generator(String key) {
		String cacheKey="";
		try {
			if(key!=null) {
				final MessageDigest mDigest = MessageDigest.getInstance("MD5");
				mDigest.update(key.getBytes());
				cacheKey = bytesToHexString(mDigest.digest());
			}
		} catch (NoSuchAlgorithmException e) {
			cacheKey = String.valueOf(key.hashCode());
		}
		return cacheKey;
	}

	private static String bytesToHexString(byte[] bytes) {
		// http://stackoverflow.com/questions/332079
		StringBuilder sb = new StringBuilder();
		for (int i = 0; i < bytes.length; i++) {
			String hex = Integer.toHexString(0xFF & bytes[i]);
			if (hex.length() == 1) {
				sb.append('0');
			}
			sb.append(hex);
		}
		return sb.toString();
	}

	public static boolean reNameFile(File file, String newName){
		return file.renameTo(new File(file.getParentFile(), newName));
	}

	public static boolean isPic(String filename) {

		String strPattern = "^.((jpg)|(png)|(jpeg))$";

		Pattern p = Pattern.compile(strPattern, Pattern.CASE_INSENSITIVE);

		Matcher m = p.matcher(filename);
		Log.d("m.matches()", String.valueOf(m.matches()));

		return m.matches();

	}

	/**
	 * 获取图片名称
	 * @param isSingle 0-不需要保存 1-保存单张 2-保存所有的url
	 * @return
	 */
	public static String getPhotoFileName(int  isSingle) {
		Date date = new Date(System.currentTimeMillis());
		SimpleDateFormat dateFormat = new SimpleDateFormat(
				"'IMG'_yyyyMMdd_HHmmss");
		String urlName = dateFormat.format(date) + ".jpg";
		if(isSingle == 1){
			IMCommon.saveCamerUrl(ChatApplication.getInstance(), urlName);
		}else if(isSingle == 2){
			String[] tempUrlString = IMCommon.getCamerArrayUrl(ChatApplication.getInstance());
			String[] urlString;
			if(tempUrlString!=null && tempUrlString.length>0){
				//String[] 
				 urlString = new String[tempUrlString.length+1];
				 System.arraycopy(tempUrlString, 0, urlString, 0, tempUrlString.length);
				urlString[urlString.length-1] = urlName;
			}else{
				urlString = new String[]{urlName};
			}
			if(urlString != null && urlString.length>0){
				IMCommon.saveCamerArrayUrl(ChatApplication.getInstance(), urlString);
			}
		}
		return urlName;

	}

	public static Date getTimeDate(long time){
		//String strDate = "";
		//SimpleDateFormat formatter = new SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss'Z'");     
		Date curDate = new Date(time);//获取当前时间     
		return curDate;
	}

	/**
	 * 保留一位小数点
	 * @param f
	 * @return
	 * 作者:fighter <br />
	 * 创建时间:2013-6-13<br />
	 * 修改时间:<br />
	 */
	public static String floatMac1(float f){
		DecimalFormat decimalFormat = new DecimalFormat("####.#");
		try {
			return decimalFormat.format(f);
		} catch (Exception e) {
			return f + "";
		}
	}

	public static String floatMac(String floatStr){
		DecimalFormat decimalFormat = new DecimalFormat("####.#");
		try {
			float f = Float.parseFloat(floatStr);
			return decimalFormat.format(f);
		} catch (Exception e) {
			return floatStr;
		}
	}

	/**
	 * 获取几天以前的秒数
	 * @param day
	 * @return
	 * 作者:fighter <br />
	 * 创建时间:2013-6-7<br />
	 * 修改时间:<br />
	 */
	public static String dayBefore(float day){
		//		Calendar calendar = Calendar.getInstance();
		//		calendar.add(Calendar.DAY_OF_WEEK, 0 - day);
		//		return (calendar.getTimeInMillis() / 1000) + "";
		long time = (long) (60 * 60 * 24 * day);

		return time + "";

	}

	public static Calendar getCalendar(String brithDate) {
		Calendar calendar = Calendar.getInstance();
		if (TextUtils.isEmpty(brithDate)) {
			return calendar;
		}
		String birth = brithDate;
		try {
			Date date = new Date(birth); // 出生日期d1
			calendar.setTime(date);
		} catch (IllegalArgumentException e) {
			e.printStackTrace();
		}
		return calendar;
	}

	public static String getTime(String currTime){
		long time = 0;
		try {
			time = Long.parseLong(currTime);
		} catch (Exception e) {
			time = System.currentTimeMillis();
		}

		return getTime(time);
	}

	public static String getTime(long currTime){
		Calendar calendar = Calendar.getInstance(Locale.CHINA);
		calendar.setTimeInMillis(currTime);
		String str = timeDifference(calendar);
		Date date = calendar.getTime();
		SimpleDateFormat format = null;
		if(str.endsWith(ChatApplication.getInstance().getResources().getString(R.string.minutes))
				|| str.endsWith(ChatApplication.getInstance().getResources().getString(R.string.hour))){

			format =new SimpleDateFormat("HH:mm"); //new SimpleDateFormat("HH:mm:ss");

		}else if(str.endsWith(ChatApplication.getInstance().getResources().getString(R.string.day))
				|| str.endsWith(ChatApplication.getInstance().getResources().getString(R.string.long_ago))){

			format = new SimpleDateFormat("yyyy.MM.dd");

		}

		return format.format(date);
	}

	public static boolean getOnline(String online){
		if(TextUtils.isEmpty(online)){
			return false;
		}

		try {
			long mtime = Long.parseLong(online) * 1000;
			Calendar calendar = Calendar.getInstance(Locale.CHINA);
			long currTime = calendar.getTimeInMillis() - mtime;
			if(currTime > ApiServiceThread.TIME){
				return false;
			}else{
				return true;
			}
		} catch (Exception e) {
			return false;
		}
	}

	/**
	 * 出生日期转换为年龄
	 * @param brithDate
	 * @return
	 * 作者:fighter <br />
	 * 创建时间:2013-3-26<br />
	 * 修改时间:<br />
	 */
	public static int dateToAge(String brithDate){
		if(TextUtils.isEmpty(brithDate)){
			return 0;
		}
		int age = 0;
		try {
			Calendar cal = Calendar.getInstance();
			String birth = brithDate;
			String now = (cal.get(Calendar.YEAR) + "/"
					+ cal.get(Calendar.MONTH) + "/" + cal.get(Calendar.DATE));
			Date d1 = new Date(birth); // 出生日期d1
			Date d2 = new Date(now); // 当前日期d2
			long i = (d2.getTime() - d1.getTime()) / (1000 * 60 * 60 * 24);
			int g = (int) i;
			age = g / 365;
		} catch (IllegalArgumentException e) {
		}

		return age;
	}

	public static Map<String, String> objectToMap(Object strVo) {
		return (Map<String, String>) JSON.parse(JSON.toJSONString(strVo));
	}

	public static String timeDifference(long time){
		Calendar calendar = Calendar.getInstance();
		calendar.setTimeInMillis(time);

		return timeDifference(calendar);
	}


	public static String timeOnlie(String online){
		try {
			long mtime = Long.parseLong(online) * 1000;
			Calendar calendar = Calendar.getInstance(Locale.CHINA);
			calendar.setTimeInMillis(mtime);
			String str = timeOnlie(calendar);
			Date date = calendar.getTime();
			SimpleDateFormat format = null;
			if(str.endsWith(ChatApplication.getInstance().getResources().getString(R.string.minutes))
					|| str.endsWith(ChatApplication.getInstance().getResources().getString(R.string.hour))){
				format = new SimpleDateFormat("HH:mm:ss");
			}else if(str.endsWith(ChatApplication.getInstance().getResources().getString(R.string.day))
					|| str.endsWith(ChatApplication.getInstance().getResources().getString(R.string.long_ago))){
				format = new SimpleDateFormat("MM-dd HH:mm:ss");
			}

			return format.format(date);
		} catch (Exception e) {
			return "";
		}

	}
	public static String timeOnlie(Calendar calendar){
		long cTime = calendar.getTimeInMillis();
		calendar.setTimeInMillis(cTime + ApiServiceThread.TIME);
		String info = "";
		Calendar currCalendar = Calendar.getInstance();
		long second = (currCalendar.getTimeInMillis() - calendar.getTimeInMillis()) / 1000;
		int index = 0;
		if(second < (60 * 60)){
			index = 60;
		} else if(second < (24 * 60 * 60)){
			index = 60 * 60;
		}else if(second < (30 * (24 * 60 * 60))){
			index = (24 * 60 * 60);
		}
		info = secondOnlie(second, index, 1);

		return info;
	}

	private static String secondOnlie(long second, int index, int num){
		String info = "";
		if(index == 60){
			info = ChatApplication.getInstance().getResources().getString(R.string.minutes);
		} else if(index == (60 * 60)){
			info = ChatApplication.getInstance().getResources().getString(R.string.hour);;
		} else if(index == (24 * 60 * 60)){
			info = ChatApplication.getInstance().getResources().getString(R.string.day);
		} else {
			return ChatApplication.getInstance().getResources().getString(R.string.long_ago);
		}

		if(second < index * num){
			return num + info;
		}else{
			return secondOnlie(second, index, ++num);
		}
	}

	public static String timeDifference(String currTime){
		Calendar calendar = Calendar.getInstance();
		try {
			long curr = Long.parseLong(currTime);
			calendar.setTimeInMillis(curr);
		} catch (Exception e) {
		}

		return timeDifference(calendar);
	}

	/**
	 * 判断时间与当前时间的差距， 给予字符提示.
	 * @param calendar
	 * @return
	 * 作者:fighter <br />
	 * 创建时间:2013-4-9<br />
	 * 修改时间:<br />
	 */
	public static String timeDifference(Calendar calendar){
		String info = "";
		Calendar currCalendar = Calendar.getInstance();
		long second = (currCalendar.getTimeInMillis() - calendar.getTimeInMillis()) / 1000;
		int index = 0;
		if(second < (60 * 60)){
			index = 60;
		} else if(second < (24 * 60 * 60)){
			index = 60 * 60;
		}else if(second < (30 * (24 * 60 * 60))){
			index = (24 * 60 * 60);
		}
		info = second(second, index, 1);

		return info;
	}

	private static String second(long second, int index, int num){
		String info = "";
		if(index == 60){
			info = ChatApplication.getInstance().getResources().getString(R.string.minutes);
		} else if(index == (60 * 60)){
			info = ChatApplication.getInstance().getResources().getString(R.string.hour);
		} else if(index == (24 * 60 * 60)){
			info = ChatApplication.getInstance().getResources().getString(R.string.day);
		} else {
			return ChatApplication.getInstance().getResources().getString(R.string.long_ago);
		}

		if(second < index * num){
			return num + info;
		}else{
			return second(second, index, ++num);
		}
	}

	/**
	 * 程序是否在前台运行
	 * 
	 * @return
	 */
	public static boolean isAppOnForeground(Context context) {
		// Returns a list of application processes that are running on the
		// device

		ActivityManager activityManager = (ActivityManager) context.getApplicationContext().getSystemService(Context.ACTIVITY_SERVICE);
		String packageName = context.getApplicationContext().getPackageName();

		List<RunningAppProcessInfo> appProcesses = activityManager
				.getRunningAppProcesses();
		if (appProcesses == null)
			return false;

		for (RunningAppProcessInfo appProcess : appProcesses) {
			// The name of the process that this object is associated with.
			if (appProcess.processName.equals(packageName)
					&& appProcess.importance == RunningAppProcessInfo.IMPORTANCE_FOREGROUND) {
				return true;
			}
		}

		return false;
	}

	public static  String getFilePathByContentResolver(Context context, Uri uri) {  
		if (null == uri) {  
			return null;  
		}  
		Cursor c = context.getContentResolver().query(uri, null, null, null, null);  
		String filePath  = null;  
		if (null == c) {  
			throw new IllegalArgumentException(  
					"Query on " + uri + " returns null result.");  
		}  
		try {  
			if ((c.getCount() != 1) || !c.moveToFirst()) {  
			} else {  
				filePath = c.getString(c.getColumnIndexOrThrow(MediaColumns.DATA));  
			}  
		} finally {  
			c.close();  
		}  
		return filePath;  
	}  

	public static long getTimeStamp(String brithDate) {
		long time = 0;
		try {
			SimpleDateFormat formatter = new SimpleDateFormat("yyyy-MM-dd HH:mm");
			Date date = formatter.parse(brithDate); 
			time = date.getTime();
		} catch (IllegalArgumentException e) {
			e.printStackTrace();
		} catch (ParseException e) {
			e.printStackTrace();
		}
		return time;
	}

	public static boolean isEmail(String strEmail){

		String strPattern = "^([a-z0-9A-Z]+[-|\\.]?)+[a-z0-9A-Z]@([a-z0-9A-Z]+(-[a-z0-9A-Z]+)?\\.)+[a-zA-Z]{2,4}$";

		Pattern p = Pattern.compile(strPattern);

		Matcher m = p.matcher(strEmail);
		Log.d("m.matches()", String.valueOf(m.matches()));

		return m.matches();
	}

	public static String showdate(int year, int month, int day) {
		Calendar calendar = Calendar.getInstance();
		int cYear = calendar.get(Calendar.YEAR);
		int cMonth = calendar.get(Calendar.MONTH);
		int cDay = calendar.get(Calendar.DAY_OF_MONTH);

		if(year> cYear){
			return "";
		}

		if(year == cYear && month > cMonth){
			return "";
		}

		if(year == cYear && month == cMonth && day > cDay){
			return "";
		}


		int trueMonth = (month + 1);
		String sMonth = trueMonth > 9 ? (trueMonth+"") : ("0" + trueMonth);
		String sDay = day > 9 ? (day + "") : ("0" + day);
		String date = year + "-" + sMonth + "-" + sDay;
		return date;
	}

	//格式化时间戳
	/**
	 * "yyyy-MM-dd HH:mm:ss"
	 * @param time
	 * @param formateType
	 * @return
	 */
	public static String formartTime(long time,String formateType){
		SimpleDateFormat sdf = new SimpleDateFormat(formateType);
		String formatTime = sdf.format(new Date(time*1000));
		return formatTime;
	}

	public static int getSourceIdByName(String imageName){
		int sourceID = 0;
		try { 
			Field field = Class.forName("net.smallchat.im.R$drawable").getField(imageName);
			sourceID = field.getInt(field); 
		} catch (Exception e) { 
		} 

		return sourceID;
	}
	/**
	 * 生成二维码
	 * @param str 组id
	 * @return
	 */
	public static Bitmap create2DCode(String str,int oldWidth,int oldHeight) {    
		Bitmap bitmap=null;
		try {
			String groupEncode = Base64.encodeToString(str.getBytes(), Base64.DEFAULT);
			String codeStr = getQrcodeUrl()+"/"+groupEncode;
			//IMServerAPI.SERVER_PREFIX+"/g/"+Base64.encodeToString(mRoomId.getBytes(), Base64.DEFAULT)
			//生成二维矩阵,编码时指定大小,不要生成了图片以后再进行缩放,这样会模糊导致识别失败       
			BitMatrix matrix = new MultiFormatWriter().encode(codeStr,BarcodeFormat.QR_CODE, oldWidth, oldHeight);       
			int width = matrix.getWidth();       
			int height = matrix.getHeight();       
			//二维矩阵转为一维像素数组,也就是一直横着排了       
			int[] pixels = new int[width * height];       
			for (int y = 0; y < height; y++) {       
				for (int x = 0; x < width; x++) {       
					if(matrix.get(x, y)){       
						pixels[y * width + x] = 0xff000000;       
					}       

				}       
			}       
			bitmap = Bitmap.createBitmap(width, height, Bitmap.Config.ARGB_8888);       
			//通过像素数组生成bitmap,具体参考api       
			bitmap.setPixels(pixels, 0, width, 0, 0, width, height);   
			return bitmap;
		} catch (WriterException e) {
			e.printStackTrace();
			return null;
		}catch (Exception e) {
			e.printStackTrace();
			return null;
		}
	} 

	/**
	 * 获取 昨天、今天、明天的日期
	 * @param fromat "yyyy-MM-dd"
	 */
	@SuppressWarnings("static-access")
	public static String dateTime(String fromat){
		Date date=new Date();//取时间
		Calendar calendar = new GregorianCalendar();
		calendar.setTime(date);
		calendar.add(calendar.DATE,-1);//把日期往后增加一天.整数往后推,负数往前移动
		date=calendar.getTime(); //这个时间就是日期往后推一天的结果 
		SimpleDateFormat formatter = new SimpleDateFormat(fromat);
		String dateString = formatter.format(date);
		Log.e("dateTime",dateString);
		return dateString;
	}


	/***
	 * 判断是否超过24小时  
	 * @param date1 开始时间
	 * @param date2 结束时间
	 * @return
	 * @throws Exception
	 */
	public static boolean jisuan(String date1, String date2) throws Exception { 
		java.text.SimpleDateFormat sdf = new java.text.SimpleDateFormat("yyyy-MM-dd HH:mm:ss"); 
		java.util.Date start = sdf.parse(date1); 
		java.util.Date end = sdf.parse(date2); 
		long cha = end.getTime() - start.getTime(); 
		double result = cha * 1.0 / (1000 * 60 * 60); 
		if(result<=24){ 
			//System.out.println("可用");   
			return true; 
		}else{ 
			//System.out.println("已过期");  
			return false; 
		} 
	} 

	/**
	 * 获取程序外部的缓存目录
	 * @param context
	 * @return
	 */
	public static File getExternalCacheDir(Context context) {
		final String cacheDir = "/Android/data/" + context.getPackageName() + "/cache/";
		return new File(Environment.getExternalStorageDirectory().getPath() + cacheDir);
	}

	public static String getSecondTime(long online){
		try {
			String timeStr = "";
			Calendar calendar = Calendar.getInstance(Locale.CHINA);
			calendar.setTimeInMillis(online);
			String str = timeOnlie(calendar);
			if(str.endsWith(ChatApplication.getInstance().getString(R.string.second)) || str.endsWith(ChatApplication.getInstance().getString(R.string.minutes))
					|| str.endsWith(ChatApplication.getInstance().getString(R.string.hour))){
				timeStr = str + ChatApplication.getInstance().getString(R.string.before);
			}else if(str.endsWith(ChatApplication.getInstance().getString(R.string.day)) || str.endsWith(ChatApplication.getInstance().getString(R.string.long_ago))){
				SimpleDateFormat format = new SimpleDateFormat("yyyy-MM-dd");
				Date date = calendar.getTime();
				timeStr = format.format(date);
			}

			return timeStr;
		} catch (Exception e) {
			return "";
		}

	}
	public static String showTimedate(int year, int month, int day, int hour, int minute) {
		Calendar calendar = Calendar.getInstance();
		int cYear = calendar.get(Calendar.YEAR);
		int cMonth = calendar.get(Calendar.MONTH);
		int cDay = calendar.get(Calendar.DAY_OF_MONTH);
		int cHour = calendar.get(Calendar.HOUR_OF_DAY);
		int cMinute = calendar.get(Calendar.MINUTE);

		if (year < cYear) {
			return "";
		}

		if (year == cYear && month < cMonth) {
			return "";
		}

		if (year == cYear && month == cMonth && day < cDay) {
			return "";
		}

		if (year == cYear && month == cMonth && day == cDay && hour < cHour) {
			return "";
		}

		if (year == cYear && month == cMonth && day == cDay && hour == cHour
				&& minute < cMinute) {
			return "";
		}

		int trueMonth = (month + 1);
		String sMonth = trueMonth > 9 ? (trueMonth + "") : ("0" + trueMonth);
		String sDay = day > 9 ? (day + "") : ("0" + day);
		String sHour = hour > 9 ? (hour + "") : ("0" + hour);
		String sMinute = minute > 9 ? (minute + "") : ("0" + minute);
		String date = year + "-" + sMonth + "-" + sDay + " " + sHour + ":" + sMinute;
		return date;
	}

	/**
	 * 
	 * @param string  assests 中的文件
	 * @return
	 */
	public static String getAssestsFile(String string) {
		try {   
			//Return an AssetManager instance for your application's package   
			InputStream is = ChatApplication.getInstance().getResources().getAssets().open(string);
			int size = is.available();   

			// Read the entire asset into a local byte buffer.   
			byte[] buffer = new byte[size];   
			is.read(buffer);   
			is.close();   

			// Convert the buffer into a string.   
			String text = new String(buffer, "UTF-8");   
			return text;
		} catch (IOException e) {   
			// Should never happen!   
			throw new RuntimeException(e);
		}
	}
	
	
	/**
	 * 生成缩略图的尺寸
	 * @param width 原始图片宽度
	 * @param height 原始图片高度
	 * @return
	 */
	public static int[] getScalcSize(int width,int height){
		int maxL = 220;
		float bigger = height>width?height:width;
		float coefficient = (float) 1.0;
		int maxPix = maxL;
		if(bigger > maxPix){
			coefficient = maxPix/bigger;
		}
		int[] i = new int[2];
		i[0] = (int) (width * coefficient);
		i[1] = (int) (height * coefficient);
		return i;
	}


}
