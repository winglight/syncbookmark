package com.favekeeper.android.model;

import java.util.List;

public class RootModel {
	private Root roots;
	

	public Root getRoot() {
		return roots;
	}

	public void setRoot(Root root) {
		this.roots = root;
	}

	public class Root{
		private Bar bookmark_bar;

		public Bar getBar() {
			return bookmark_bar;
		}

		public void setBar(Bar bar) {
			this.bookmark_bar = bar;
		}
		
	}
	
	public class Bar{
		private List<BookmarkModel> children;

		public List<BookmarkModel> getChildren() {
			return children;
		}

		public void setChildren(List<BookmarkModel> children) {
			this.children = children;
		}
		
	}
}
