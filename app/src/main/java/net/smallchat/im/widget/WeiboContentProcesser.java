package net.smallchat.im.widget;

import java.lang.reflect.Field;
import java.util.ArrayList;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import android.content.Context;
import android.graphics.BitmapFactory;
import android.graphics.Color;
import android.text.Spannable;
import android.text.SpannableString;
import android.text.SpannableStringBuilder;
import android.text.Spanned;
import android.text.style.ForegroundColorSpan;
import android.text.style.URLSpan;
import android.util.Log;

import net.smallchat.im.R;
import net.smallchat.im.adapter.EmojiUtil;
import net.smallchat.im.widget.WeiboUrlSpan.OnViewClick;

public class WeiboContentProcesser {

	private static TextItem getString(String content, char head){
		if(head == 'H'){
			return getHTTPLinkString(content);
		}else if(head == '#'){
			return getSharpString(content);
		}else if(head == '@'){
			return getAtString(content);
		}else if(head == '['){
			return getEmotionString(content);
		}else{
			return null;
		}
	}

	private static TextItem getHTTPLinkString(String str){
		String expr = "http://[0-9a-zA-Z/:\\.?&=_]+";
		String ret = getMatchedString(str, expr);

		if(ret != null){            
			//            System.out.println("Got http link:" + ret);
			return new TextItem(ret, TextItem.TYPE_LINK);
		}else{
			return null;
		}
	}

	/**
	 * Search the matched string begin with the index 0
	 * @param str
	 * @param regex
	 * @return
	 */
	private static String getMatchedString(String str, String regex){
		String ret = null;

		Pattern pattern = Pattern.compile(regex); 
		Matcher matcher = pattern.matcher(str);
		if(matcher.find()){
			if(matcher.start() == 0){
				ret = matcher.group();
			}
		}

		return ret;
	}

	private static TextItem getEmotionString(String str){
		String expr = "\\[[0-9a-zA-Z\u4e00-\u9fa5]+\\]";
		String ret = getMatchedString(str, expr);

		if(ret != null){
			Log.e("getEmotionString", ret);
			//            System.out.println("Got Emotion:" + ret);
			return new TextItem(ret, TextItem.TYPE_EMOTION);
		}else{
			return null;
		}
	}

	private static TextItem getSharpString(String str){

		String expr = "#[0-9a-zA-Z\u4e00-\u9fa5\u201c\u201d\" ]+#";
		String ret = getMatchedString(str, expr);

		if(ret != null){
			//            System.out.println("Got #:" + ret);
			return new TextItem(ret, TextItem.TYPE_SHARP);
		}else{
			return null;
		}

	}

	private static TextItem getAtString(String str){
		String expr = "@[0-9a-zA-Z\u4e00-\u9fa5_\\-]+";
		//        String expr = "@[[^@\\s%s]0-9]{1,20}";  // From Sina
		String ret = getMatchedString(str, expr);

		if(ret != null){
			// http link do not included after @
			int index = ret.indexOf("http");
			if(index != -1){ // find "http"
				if(index == 1){ // http is follow @
					ret = null;
				}else{
					ret = ret.substring(0, index);
				}
			}
		}

		if(ret != null){
			//            System.out.println("Got @ link:" + ret);
			return new TextItem(ret, TextItem.TYPE_AT);
		}else{
			return null;
		}
	}

	public static List<TextItem> splitWeibo(Context context, String content,int type){
		List<TextItem> items = splitWeiboWithoutImage(content,type);

		try {
			for(TextItem item : items){
				if(item.mType == TextItem.TYPE_EMOTION){
					Log.e("splitWeibo", "splitWeibo");
					// 得到图片在R 文件中的属性
					Field field = R.drawable.class.getDeclaredField(item.mText);
					// 得到该属性的值
					int resId = field.getInt(null);		
					if (resId != 0) {
						item.mBitmap = BitmapFactory.decodeResource(context.getResources(), resId);
					}
					//  item.mBitmap = reSearchCommon.getImageLoader().getEmotion(context, item.mText);
				}
			}
		} catch (SecurityException e) {
			e.printStackTrace();
		} catch (IllegalArgumentException e) {
			e.printStackTrace();
		} catch (NoSuchFieldException e) {
			e.printStackTrace();
		} catch (IllegalAccessException e) {
			e.printStackTrace();
		}

		return items;
	}

	public static List<TextItem> splitWeiboWithoutImage(String content,int type){
		List<TextItem> items = new ArrayList<TextItem>();
		String item = "";
		String tempStr = content;
		int sType = 0;
		for(int i = 0; i < content.length(); i++){
			char  current = content.charAt(i);
			if (current == '#' || current == '@' || current == '[' || tempStr.startsWith("http://", i)
					|| type == 2 || type == 3 || type == 7){
				boolean isNext = true;
				if(type == 2 || type == 3 || type == 7){
					if(sType ==type){
						isNext = false;
					}
				}
				if(isNext){

					String text = "";
					if(tempStr.startsWith("http://", i)){
						current = 'H';
						text += "http://";
					}else{
						text += current;
					}

					if(item.length() != 0){
						items.add(new TextItem(item));
					}
					TextItem textItem=null;
					if(type == 2){
						textItem = new TextItem("分享了音乐", TextItem.TYPE_MUSIC,Color.rgb(229, 209, 109));
					}else if(type == 3){
						textItem =new TextItem("分享了投票", TextItem.TYPE_VOTE,Color.rgb(229, 209, 109));
					}else if(type == 7){
						textItem = new TextItem("分享了新闻", TextItem.TYPE_APPNEWS,Color.rgb(229, 209, 109));
					}else{
						 textItem = getString(tempStr.substring(i), current);
					}
					if(textItem != null){
						items.add(textItem);
						i += textItem.mText.length() - 1;
						item = "";
					}else{
						item = "" + text;
						i += text.length() - 1;
					}
					
					sType = type;

				}else{
					item += current;
				}
			}else{
				item += current;
			}
		}

		if(item.length() != 0){
			items.add(new TextItem(item));
		}

		return items;
	}

	public static Spanned getSpannedString(Context context, String content,int type){
		List<TextItem> items = splitWeibo(context, content,type);
		SpannableStringBuilder spannableString = new SpannableStringBuilder("");

		for(TextItem item : items){
			SpannableString spannable = new SpannableString(item.mText);
			switch (item.mType) {
			case TextItem.TYPE_TEXT:
				break;

			case TextItem.TYPE_LINK:
				URLSpan urlSpan = new URLSpan(item.mText);
				spannable.setSpan(urlSpan, 0, item.mText.length(), Spannable.SPAN_INCLUSIVE_EXCLUSIVE); 
				//                ForegroundColorSpan cSpan = new ForegroundColorSpan(item.mColor);
				//                spannable.setSpan(cSpan, 0, item.mText.length(), Spannable.SPAN_INCLUSIVE_EXCLUSIVE); 
				//                break;
			case TextItem.TYPE_AT:
			case TextItem.TYPE_SHARP:
				ForegroundColorSpan colorSpan = new ForegroundColorSpan(item.mColor);
				spannable.setSpan(colorSpan, 0, item.mText.length(), Spannable.SPAN_INCLUSIVE_EXCLUSIVE); 
			case TextItem.TYPE_EMOTION:

				/*if(item.mBitmap != null){
                	//Update the size of emotion  2011.12.13
                	Drawable drawable = new BitmapDrawable(item.mBitmap);
                	int width  = (int)(FeatureFunction.dip2px(context, 22) * 10((float)WeiboCommon.getSettingValue().mWeiboFontSize/(float)15));
                	int height = (int)(FeatureFunction.dip2px(context, 22) * 10((float)WeiboCommon.getSettingValue().mWeiboFontSize/(float)15));
                	drawable.setBounds(0, 0, width, height);
                    ImageSpan imageSpan = new ImageSpan(drawable, ImageSpan.ALIGN_BOTTOM);   
                    spannable.setSpan(imageSpan, 0, item.mText.length(), Spannable.SPAN_INCLUSIVE_EXCLUSIVE);
                    break;
                }else{
                    // If we can not get emotion bitmap here, we will highlight it, we do not break here
                }*/

			default:
				break;
			}

			spannableString.append(spannable);
		}

		return spannableString;
	}

	/**
	 * 文本类型
	 * @param context
	 * @param content
	 * @param type 2-音乐 3-投票 7-新闻
	 * @return
	 */
	public static Spanned getCustomizedSpannedString(Context context, String content,int type,OnViewClick onclick){
		List<TextItem> items = splitWeibo(context, content,type);
		SpannableStringBuilder spannableString = new SpannableStringBuilder("");
		for(TextItem item : items){
			SpannableString spannable = new SpannableString(item.mText);
		
			switch (item.mType) {
			case TextItem.TYPE_TEXT:
				spannable = EmojiUtil.getExpressionString(context, item.mText, "emoji_[\\d]{0,3}");
				break;

			case TextItem.TYPE_LINK:
			case TextItem.TYPE_AT:
			case TextItem.TYPE_SHARP:
			case TextItem.TYPE_MUSIC:
			case TextItem.TYPE_VOTE:
			case TextItem.TYPE_APPNEWS:
				WeiboUrlSpan urlSpan = new WeiboUrlSpan(item.mText, context,onclick,item.mColor);
				spannable.setSpan(urlSpan, 0, item.mText.length(), Spannable.SPAN_INCLUSIVE_EXCLUSIVE); 
				break;
			case TextItem.TYPE_EMOTION:
				/*    if(item.mBitmap != null){
                	//Update the size of emotion 2011.12.13
                	 Drawable drawable = new BitmapDrawable(item.mBitmap);
                	 int width  = (int)(FeatureFunction.dip2px(context, 22) * 10((float)WeiboCommon.getSettingValue().mWeiboFontSize/(float)15));
                 	 int height = (int)(FeatureFunction.dip2px(context, 22) * 10((float)WeiboCommon.getSettingValue().mWeiboFontSize/(float)15));
                     drawable.setBounds(0, 0, width, height);
                    ImageSpan imageSpan = new ImageSpan(drawable, ImageSpan.ALIGN_BOTTOM);   
                    spannable.setSpan(imageSpan, 0, item.mText.length(), Spannable.SPAN_INCLUSIVE_EXCLUSIVE);
                    break;
                }else{
                    // If we can not get emotion bitmap here, we will highlight it, we do not break here
                }*/
			default:
				break;
			}

			spannableString.append(spannable);
		}
		

		return spannableString;
	}
}
