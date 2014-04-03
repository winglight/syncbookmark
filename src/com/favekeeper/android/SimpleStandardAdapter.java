package com.favekeeper.android;

import java.util.Arrays;
import java.util.List;
import java.util.Set;

import com.favekeeper.android.model.BookmarkModel;

import pl.polidea.treeview.AbstractTreeViewAdapter;
import pl.polidea.treeview.TreeNodeInfo;
import pl.polidea.treeview.TreeStateManager;
import android.content.Intent;
import android.net.Uri;
import android.opengl.Visibility;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.CheckBox;
import android.widget.CompoundButton;
import android.widget.ImageView;
import android.widget.CompoundButton.OnCheckedChangeListener;
import android.widget.LinearLayout;
import android.widget.TextView;

/**
 * This is a very simple adapter that provides very basic tree view with a
 * checkboxes and simple item description.
 * 
 */
class SimpleStandardAdapter extends AbstractTreeViewAdapter<String> {

    private final List<BookmarkModel> bmList;

    private final OnCheckedChangeListener onCheckedChange = new OnCheckedChangeListener() {
        @Override
        public void onCheckedChanged(final CompoundButton buttonView,
                final boolean isChecked) {
            final String id = (String) buttonView.getTag();
        }

    };

    public SimpleStandardAdapter(final SyncbookmarkActivity treeViewListDemo,
            final List<BookmarkModel> list,
            final TreeStateManager<String> treeStateManager,
            final int numberOfLevels) {
        super(treeViewListDemo, treeStateManager, numberOfLevels);
        this.bmList = list;
    }

    private String getDescription(final String id) {
        final Integer[] hierarchy = getManager().getHierarchyDescription(id);
        return "Node " + id + Arrays.asList(hierarchy);
    }
    
    private BookmarkModel getModel(final String id) {
        final Integer[] hierarchy = getManager().getHierarchyDescription(id);
        
        BookmarkModel bookmark = bmList.get(hierarchy[0]) ;
        int j = 0;
        for(int i : Arrays.asList(hierarchy)){
        	if(j == 0){
        		j++;
        		continue;
        	}
        	bookmark = bookmark.getChildren().get(i);
        			
        }
        return bookmark;
    }

    @Override
    public View getNewChildView(final TreeNodeInfo<String> treeNodeInfo) {
        final LinearLayout viewLayout = (LinearLayout) getActivity()
                .getLayoutInflater().inflate(R.layout.dragitem, null);
        return updateView(viewLayout, treeNodeInfo);
    }

    @Override
    public LinearLayout updateView(final View view,
            final TreeNodeInfo<String> treeNodeInfo) {
    	BookmarkModel bookmark = getModel(treeNodeInfo.getId());
    	
        final LinearLayout viewLayout = (LinearLayout) view;
        final TextView descriptionView = (TextView) viewLayout
                .findViewById(R.id.titleTxt);
        descriptionView.setText(bookmark.getName());
        ImageView icon = (ImageView) view.findViewById(R.id.icon);
        if (treeNodeInfo.isWithChildren()) {
        	icon.setVisibility(View.GONE);
        } else {
        	icon.setVisibility(View.VISIBLE);
        	icon.setImageResource(R.drawable.url_24);
        }
//        viewLayout.setTag(bookmark);
        return viewLayout;
    }
    
    @Override
    public void handleItemClick(final View view, final Object id) {
        final String longId = (String) id;
        final TreeNodeInfo<String> info = getManager().getNodeInfo(longId);
        if (info.isWithChildren()) {
            super.handleItemClick(view, id);
        } else {
        	BookmarkModel bookmark = getModel(longId);
			if (bookmark.isFolder()) {
				// if(bookmarkList.containsAll(model.getChildren())){
				// bookmarkList.removeAll(model.getChildren());
				// }else{
				// bookmarkList.addAll(model.getChildren());
				// }
				// refreshBookmarkList();
				// list.scrollTo(position, 0);
			} else {
				Intent intent = new Intent(Intent.ACTION_VIEW);
				intent.setData(Uri.parse(bookmark.getUrl()));
				this.getActivity().startActivity(intent);
			}
        }
    }

    @Override
    public long getItemId(final int position) {
        return Long.valueOf(getTreeId(position));
    }
}