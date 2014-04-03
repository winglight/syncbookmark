package com.favekeeper.android.model;

import java.util.List;

public class BookmarkModel {

	private String id;
	private String date_added;
	private String name;
	private String type;
	private String url;
	private List<BookmarkModel> children;
	
	public String getId() {
		return id;
	}
	public void setId(String id) {
		this.id = id;
	}
	public String getDate_added() {
		return date_added;
	}
	public void setDate_added(String date_added) {
		this.date_added = date_added;
	}
	public String getName() {
		return name;
	}
	public void setName(String name) {
		this.name = name;
	}
	public String getType() {
		return type;
	}
	public void setType(String type) {
		this.type = type;
	}
	public String getUrl() {
		return url;
	}
	public void setUrl(String url) {
		this.url = url;
	}
	
	public List<BookmarkModel> getChildren() {
		return children;
	}
	public void setChildren(List<BookmarkModel> children) {
		this.children = children;
	}
	public boolean isFolder(){
		return "folder".equals(type);
	}
	
}
