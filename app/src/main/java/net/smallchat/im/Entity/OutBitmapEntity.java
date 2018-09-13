package net.smallchat.im.Entity;

import java.io.Serializable;

import android.graphics.Bitmap;
import android.widget.ImageView;

public class OutBitmapEntity implements Serializable{

	/**
	 * 
	 */
	private static final long serialVersionUID = 1L;
	public String fileName;
	public ImageView imageView;
	public Bitmap bitmap;
	
	public OutBitmapEntity(String fileName, ImageView imageView, Bitmap bitmap) {
		super();
		this.fileName = fileName;
		this.imageView = imageView;
		this.bitmap = bitmap;
	}

	public OutBitmapEntity() {
		super();
	}
	
}
