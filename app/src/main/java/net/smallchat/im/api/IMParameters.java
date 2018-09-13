
package net.smallchat.im.api;

import android.os.Bundle;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

import net.smallchat.im.Entity.MoreFile;


/**
 * A list queue for saving keys and values.
 * Using it to construct http header or get/post parameters.
 *
 */
public class IMParameters {



	private Bundle mParameters = new Bundle();
	private List<String> mKeys = new ArrayList<String>();
	private HashMap<String,List<MoreFile>> imageMap = new HashMap<String, List<MoreFile>>();
	private HashMap<String,List<MoreFile>> audioMap = new HashMap<String, List<MoreFile>>();
	private HashMap<String,List<MoreFile>> fileMap = new HashMap<String, List<MoreFile>>();
	private HashMap<String,List<MoreFile>> videoMap = new HashMap<String, List<MoreFile>>();


	public IMParameters(){

	}


	public void add(String key, String value){
		if(this.mKeys.contains(key)){
			this.mParameters.putString(key, value);
		}else{
			this.mKeys.add(key);
			this.mParameters.putString(key, value);
		}
	}

	public void addImage(String key, List<MoreFile> list){

		if(this.mKeys.contains(key)){
			this.imageMap.put(key, list);
		}else{
			this.mKeys.add(key);
			this.imageMap.put(key, list);
		}
	}
	public void addAudio(String key, List<MoreFile> list){

		if(this.mKeys.contains(key)){
			this.audioMap.put(key, list);
		}else{
			this.mKeys.add(key);
			this.audioMap.put(key, list);
		}
	}
	public void addVideo(String key, List<MoreFile> list){

		if(this.mKeys.contains(key)){
			this.videoMap.put(key, list);
		}else{
			this.mKeys.add(key);
			this.videoMap.put(key, list);
		}
	}
	public void addFile(String key, List<MoreFile> list){

		if(this.mKeys.contains(key)){
			this.fileMap.put(key, list);
		}else{
			this.mKeys.add(key);
			this.fileMap.put(key, list);
		}
	}


	public void remove(String key){
		mKeys.remove(key);
		this.mParameters.remove(key);
	}

	public void remove(int i){
		String key = this.mKeys.get(i);
		this.mParameters.remove(key);
		mKeys.remove(key);
	}


	public int getLocation(String key){
		if(this.mKeys.contains(key)){
			return this.mKeys.indexOf(key);
		}
		return -1;
	}

	public String getKey(int location){
		if(location >= 0 && location < this.mKeys.size()){
			return this.mKeys.get(location);
		}
		return "";
	}

	public List<MoreFile> getImageList(String key){
		if (key!=null && !key.equals("")) {
			return imageMap.get(key);
		}
		return null;
	}


	public List<MoreFile> getAudioList(String key){
		if (key!=null && !key.equals("")) {
			return audioMap.get(key);
		}
		return null;
	}


	public List<MoreFile> getVideoList(String key){
		if (key!=null && !key.equals("")) {
			return videoMap.get(key);
		}
		return null;
	}

	public List<MoreFile> getFileList(String key){
		if (key!=null && !key.equals("")) {
			return fileMap.get(key);
		}
		return null;
	}


	public String getValue(String key){
		String rlt = this.mParameters.getString(key);
		return rlt;
	}

	public String getValue(int location){
		String key = this.mKeys.get(location);
		String rlt = this.mParameters.getString(key);
		return rlt;
	}


	public int size(){
		return mKeys.size();
	}

	public void addAll(IMParameters parameters){
		for(int i = 0; i < parameters.size(); i++){
			this.add(parameters.getKey(i), parameters.getValue(i));
		}

	}

	public void clear(){
		this.mKeys.clear();
		this.mParameters.clear();
	}

}
