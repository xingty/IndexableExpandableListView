package org.wiyi.indexablelistview.sample;

import android.content.Context;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import org.wiyi.indexablelistview.IndexableListAdapter;

import java.util.ArrayList;
import java.util.List;

/**
 * Created by xing on 1/8/16.
 */
public class MyAdapter extends IndexableListAdapter {
    private Context context ;
    List<String> group ;
    List<ArrayList<String>> data ;

    public MyAdapter(Context context) {
        this.context = context ;
    }

    public void setData(List<String> group,List<ArrayList<String>> data) {
        this.group = group ;
        this.data = data ;
        notifyDataSetChanged();
    }

    @Override
    public int getGroupCount() {
        return group.size();
    }

    @Override
    public int getChildrenCount(int groupPosition) {
        return data.get(groupPosition).size();
    }

    @Override
    public Object getGroup(int groupPosition) {
        return data.get(groupPosition) ;
    }

    @Override
    public Object getChild(int groupPosition, int childPosition) {
        ArrayList children = (ArrayList) getGroup(groupPosition);

        return children.get(childPosition);
    }

    @Override
    public long getGroupId(int groupPosition) {
        return groupPosition;
    }

    @Override
    public long getChildId(int groupPosition, int childPosition) {
        return childPosition;
    }

    @Override
    public boolean hasStableIds() {
        return false;
    }

    @Override
    public View getGroupView(int groupPosition, boolean isExpanded, View convertView, ViewGroup parent) {
        TextView view ;
        if (convertView == null) {
            view = (TextView) View.inflate(context,android.R.layout.simple_list_item_1,null);
        } else {
            view = (TextView) convertView;
        }

        view.setBackgroundResource(android.R.color.white);
        view.setText(group.get(groupPosition));

        return view;
    }

    @Override
    public View getChildView(int groupPosition, int childPosition, boolean isLastChild, View convertView, ViewGroup parent) {
        TextView view ;
        if (convertView == null) {
            view = (TextView) View.inflate(context,android.R.layout.simple_list_item_1,null);
        } else {
            view = (TextView) convertView;
        }

        view.setText((String) getChild(groupPosition, childPosition));

        return view;
    }

    @Override
    public boolean isChildSelectable(int groupPosition, int childPosition) {
        return false;
    }

    @Override
    public String[] getSections() {
        String[] sections = new String[group.size()] ;
        return group.toArray(sections) ;
    }
}
