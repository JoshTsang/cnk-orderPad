package com.htb.cnk.data;

public class TableInfo {
	public static final String IMAGE_ITEM = "imageItem";
	public static final String ITEM_TEXT = "ItemText";
	private int mImageItem;
	private int mTextItem;
	
	public void setImageItem(int imageItem) {
		this.mImageItem = imageItem;
	}

	public int getImageItem() {
		return this.mImageItem;
	}
	
	public void setTextItem(int textItem) {
		this.mTextItem = textItem;
	}

	public int getTextItem() {
		return this.mTextItem;
	}

}
