package com.htb.cnk.data;

import java.util.ArrayList;
import java.util.List;

public class Categories {
	public class Category {
		public int mId;
		public String mName;
		
		public Category(int id, String name) {
			mId = id;
			mName = name;
		}
	}
	
	List<Category> categories = new ArrayList<Category>();
	
	public  Categories(){
		getCategoriesData();
	}
	
	public int count() {
		return categories.size();
	}
	
	public String getName(int position) {
		return categories.get(position).mName;
	}
	
	public int getId(int position) {
		return categories.get(position).mId;
	}
	
	private void getCategoriesData() {
		test();
	}

	private void test() {
		categories.add(new Category(0, "凉菜"));
		categories.add(new Category(1, "热菜"));
		categories.add(new Category(2, "汤"));
		categories.add(new Category(3, "稀饭"));
		categories.add(new Category(4, "饮料"));
		categories.add(new Category(5, "特色菜"));
		categories.add(new Category(6, "私房菜"));
	}
}
