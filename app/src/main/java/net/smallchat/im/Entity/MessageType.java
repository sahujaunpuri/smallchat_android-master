package net.smallchat.im.Entity;

import android.util.Log;

import net.smallchat.im.global.FileTool;

import java.util.HashMap;

public final class MessageType {

	/**
	 * 文字
	 */
	public static final int TEXT = 1;

	/**
	 * 图片
	 */
	public static final int IMAGE = 2;

	/**
	 * 声音
	 */
	public static final int AUDIO = 3;


	/**
	 * 位置
	 */
	public static final int LOCATION = 4;

	/**
	 * 通讯录名片
	 */
	public static final int CARD = 5;


	/**
	 * 文件
	 */
	public static final int FILE = 6;


	/**
	 * 小视频
	 */
	public static final int VIDEO = 7;


	/**
	 * 转账
	 */
	public static final int TRANSFER=8;

	/**
	 * 红包
	 */
	public static final int REDPACKET=9;

	/**
	 * 收藏
	 */
	public static final int FAVORITE=10;





	public static int getMessageTypeByFile(String path){


		String fileExt = FileTool.getFileExt(path);
		fileExt=fileExt.toLowerCase();
		//建立一个MIME类型与文件后缀名的匹配表
		HashMap<String,Integer> map=new HashMap<String,Integer>();
		// 后缀名，MIME类型
		map.put("3gp", MessageType.VIDEO);
		map.put("apk", MessageType.FILE );
		map.put("asf",MessageType.VIDEO);
		map.put("avi",MessageType.VIDEO);
		map.put("bin",MessageType.FILE);
		map.put("bmp",MessageType.IMAGE);
		map.put("c", MessageType.FILE );
		map.put("class",MessageType.FILE );
		map.put("conf", MessageType.FILE );
		map.put("cpp", MessageType.FILE );
		map.put("doc",MessageType.FILE);
		map.put("docx",MessageType.FILE);
		map.put("xls",MessageType.FILE);
		map.put("xlsx",MessageType.FILE);
		map.put("exe",MessageType.FILE );
		map.put("gif",MessageType.IMAGE );
		map.put("gtar", MessageType.FILE );
		map.put("gz", MessageType.FILE );
		map.put("h", MessageType.FILE );
		map.put("htm", MessageType.FILE );
		map.put("html", MessageType.FILE );
		map.put("jar", MessageType.FILE );
		map.put("java", MessageType.FILE );
		map.put("jpeg", MessageType.IMAGE );
		map.put("jpg", MessageType.IMAGE );
		map.put("js", MessageType.FILE );
		map.put("log", MessageType.FILE );
		map.put("m3u", MessageType.AUDIO);
		map.put("m4a", MessageType.AUDIO);
		map.put("m4b", MessageType.AUDIO);
		map.put("m4p", MessageType.AUDIO);
		map.put("m4u", MessageType.VIDEO);
		map.put("m4v", MessageType.VIDEO);
		map.put("mov", MessageType.VIDEO);
		map.put("mp2", MessageType.AUDIO);
		map.put("mp3", MessageType.AUDIO);
		map.put("mp4", MessageType.VIDEO);
		map.put("mpc", MessageType.FILE );
		map.put("mpe", MessageType.VIDEO);
		map.put("mpeg", MessageType.VIDEO);
		map.put("mpg", MessageType.VIDEO);
		map.put("mpg4", MessageType.VIDEO);
		map.put("mpga", MessageType.AUDIO);
		map.put("msg", MessageType.FILE );
		map.put("ogg", MessageType.AUDIO);
		map.put("pdf", MessageType.FILE );
		map.put("png", MessageType.IMAGE );
		map.put("pps", MessageType.FILE );
		map.put("ppt", MessageType.FILE );
		map.put("pptx",MessageType.FILE );
		map.put("prop", MessageType.FILE );
		map.put("rc", MessageType.FILE );
		map.put("rmvb", MessageType.VIDEO);
		map.put("rtf", MessageType.FILE );
		map.put("sh", MessageType.FILE );
		map.put("tar", MessageType.FILE );
		map.put("tgz",MessageType.FILE );
		map.put("txt", MessageType.FILE );
		map.put("wav", MessageType.AUDIO);
		map.put("wma", MessageType.AUDIO);
		map.put("wmv", MessageType.VIDEO);
		map.put("wps",MessageType.FILE );
		map.put("xml", MessageType.FILE );
		map.put("z", MessageType.FILE);
		map.put("zip", MessageType.FILE );
		map.put("rar", MessageType.FILE );
		map.put("7z", MessageType.FILE );
		map.put("pages", MessageType.FILE );
		map.put("key", MessageType.FILE );
		map.put("nubmers", MessageType.FILE );
		if(fileExt!=null) {

			try {
				return map.get(fileExt);
			}catch (Exception ex){
				ex.printStackTrace();
				Log.d("fileExt",fileExt);
				return MessageType.FILE;
			}
		}else {
			return MessageType.FILE;
		}
	}

}
