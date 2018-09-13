package net.smallchat.im.about;

import java.io.BufferedReader;
import java.io.ByteArrayInputStream;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.MalformedURLException;
import java.net.URL;
import java.util.HashMap;

import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;

import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.NodeList;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.net.Uri;
import android.os.Environment;
import android.util.Log;
import android.widget.Toast;

import net.smallchat.im.R;
import net.smallchat.im.global.FeatureFunction;

/**
 * 升级文件
 * @author dongli
 *
 */
public class AppUpgrade {
	private static final String TAG = "AppUpgrade";
	public final static String urlWebsit = "http://www.deedkey.com/app/download/";
	public final static String urlVerConfigFile = "version.xml";
	public final static String LOGIN_PICTURE_PATH = "/QiYue/download/";
	private String apkFileName = null;
	private String imageFileName = null;
	private HashMap<String, String> themeMap;
	URL mFileUrl;
	
	public boolean initVersionCheck(Context context){
		boolean hasNewVer = false;
		URL url = null;
		HttpURLConnection urlConnection = null;
		InputStream inputStream = null;
		StringBuffer xmlBuffer = null;
		
		apkFileName = null;
		
		if (!FeatureFunction.checkSDCard()){return false;}
		
		/**New Dirctory*/
		if (!FeatureFunction.newFolder(Environment.getExternalStorageDirectory() + LOGIN_PICTURE_PATH)){return false;}
		
		try {
			url= new URL(urlWebsit + urlVerConfigFile);
			urlConnection = (HttpURLConnection) url.openConnection();
			urlConnection.connect();
			
			if (urlConnection.getResponseCode() == HttpURLConnection.HTTP_OK){
				inputStream = urlConnection.getInputStream();
				
				xmlBuffer = new StringBuffer();
				BufferedReader br = new BufferedReader(new InputStreamReader(inputStream, "UTF-8"));
				String line;
				
				while (null != (line = br.readLine())) {
					xmlBuffer.append(line);					
				}
				
				br.close();
				inputStream.close();
				urlConnection.disconnect();
				
				// Parse XML file
				if (null != xmlBuffer && xmlBuffer.length() > 0){
					DocumentBuilderFactory factory = DocumentBuilderFactory.newInstance();
					try {
						DocumentBuilder builder = factory.newDocumentBuilder();
						InputStream ios = new ByteArrayInputStream(xmlBuffer.toString().getBytes());
						Document domDocument = builder.parse(ios);
						Element rootElement = domDocument.getDocumentElement();
						NodeList items = rootElement.getElementsByTagName("application");
						
						Element applicationElement = null;
						
						for (int i = 0; i< items.getLength(); i++){
							applicationElement = (Element) items.item(i);
						}
										
						if (null != applicationElement){
							NodeList tempItems;
							tempItems = applicationElement.getElementsByTagName("version");
							
							for (int i = 0; i< items.getLength(); i++){
								Element element = (Element)tempItems.item(i);
								String id = element.getAttribute("id");
								if(!id.equals(FeatureFunction.getAppVersionName(context))){
									NodeList listItemList = element.getElementsByTagName("apk");
									if (listItemList.getLength()> 0){
										apkFileName = ((Element)listItemList.item(0)).getFirstChild().getNodeValue();//.getAttribute("name");
									}

									if (null != apkFileName){
										hasNewVer = true;
									}
								}
							}
						}
						
						ios.close();
					} catch (Exception e) {
						// handle XML file exception
						Log.w("WebService", "Pasre XML file exception");
					}
				}
			}
			
		} catch (MalformedURLException e) {
			e.printStackTrace();
		} catch (IOException e) {
			e.printStackTrace();
		}

		return hasNewVer;
	}
	
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

	public boolean hasNewVersion(){
		if (null != apkFileName){
			Log.d(TAG, "Got new version!" + apkFileName);
			return true;
		}else{
			Log.d(TAG, "Does not get new version!");
			return false;
		}
	}
	
	public boolean hasNewResource(){
		if (null != imageFileName){
			return true;
		}else{
			return false;
		}
	}
	
	/**
	 * Call {@link #initThemeCheck()} and then get theme name and related apk name
	 * @param context
	 * @param apkName
	 * @return
	 */
	public boolean downloadTheme(Context context, String apkName){
		boolean success = false;
		if (null != apkName){
			if (downloadFile(apkName)){
				Intent intent = new Intent(Intent.ACTION_VIEW);
				intent.setDataAndType(Uri.fromFile(new File(Environment.getExternalStorageDirectory() + LOGIN_PICTURE_PATH + apkName)), "application/vnd.android.package-archive"); 
				context.startActivity(intent);
				success = true;
			}
		}
		
		return success;
	}
	
	public boolean upgradeApp(Context context,String downloadUrl){
		boolean success = false;
		if (null != downloadUrl && !downloadUrl.equals("")){

			try {
				mFileUrl= new URL(downloadUrl);
				Uri uri = Uri.parse(mFileUrl.toString());
				Intent intent = new Intent(Intent.ACTION_VIEW , uri); 
				context.startActivity(intent);
				success = true;
			} catch (MalformedURLException e) {
				e.printStackTrace();
			}
			
		}
		
		return success;
	}
	
	public void updateApp(Context context){
		//boolean success = false;
		if (null != apkFileName){
			try{
				if (downloadFile(apkFileName)){
					Uri uri = Uri.parse(mFileUrl.toString());
					Intent intent = new Intent(Intent.ACTION_VIEW , uri); 
					context.startActivity(intent);
					((Activity)context).finish();
				}
			}catch (Exception e) {
				Toast.makeText(context, context.getString(R.string.upgradfail),
						Toast.LENGTH_LONG).show();
			}
			
		}
	}
	
	
	public boolean updateResource(){
		boolean success = false;

		if (null != imageFileName){
			if (downloadFile(imageFileName)){
				success = true;
			}
		}
		
		return success;
	}
	
	private boolean downloadFile(String fileName){
		boolean ret = false;
		mFileUrl = null;
		HttpURLConnection urlConnection = null;
		InputStream inputStream = null;
		FileOutputStream outputStream = null;
	
		if (null != fileName){		
			try {
				mFileUrl= new URL(urlWebsit+fileName);
				urlConnection = (HttpURLConnection) mFileUrl.openConnection();
				urlConnection.connect();
				
				if (urlConnection.getResponseCode() == HttpURLConnection.HTTP_OK){
					inputStream = urlConnection.getInputStream();
					
					String filePath = Environment.getExternalStorageDirectory()+LOGIN_PICTURE_PATH+fileName;
					
					outputStream = new FileOutputStream(new File(filePath));
					BufferedReader br = new BufferedReader(new InputStreamReader(inputStream));
					
					byte[] tempBuffer = new byte[1024];
					int count = 0;
					while (-1 != (count = inputStream.read(tempBuffer))) {
						outputStream.write(tempBuffer, 0, count);
					}

					br.close();
					inputStream.close();
					outputStream.flush();
					outputStream.close();
		        
					urlConnection.disconnect();
					ret = true;
				}
			} catch (MalformedURLException e) {
				e.printStackTrace();
			} catch (IOException e) {
				e.printStackTrace();
			}
		}
		
		return ret;
	}
	
	public String getResourceName(){
		return imageFileName;
	}
}
