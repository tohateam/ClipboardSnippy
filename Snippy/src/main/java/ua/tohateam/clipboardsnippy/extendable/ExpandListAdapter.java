package ua.tohateam.clipboardsnippy.extendable;

import android.content.*;
import android.os.*;
import android.view.*;
import android.widget.*;
import java.util.*;
import ua.tohateam.clipboardsnippy.*;

public class ExpandListAdapter extends BaseExpandableListAdapter {

    private Context context;
    private ArrayList<GroupItem> groups;

    public ExpandListAdapter(Context context, ArrayList<GroupItem> groups) {
        this.context = context;
        this.groups = groups;
    }

    @Override
    public Object getChild(int groupPosition, int childPosition) {
        ArrayList<ChildItem> chList = groups.get(groupPosition).getItems();
        return chList.get(childPosition);
    }

    @Override
    public long getChildId(int groupPosition, int childPosition) {
        return childPosition;
    }

    @Override
    public View getChildView(int groupPosition, int childPosition,
		boolean isLastChild, View convertView, ViewGroup parent) {

        ChildItem child = (ChildItem) getChild(groupPosition, childPosition);

        if (convertView == null) {
            LayoutInflater infalInflater = (LayoutInflater) context.getSystemService(context.LAYOUT_INFLATER_SERVICE);
            convertView = infalInflater.inflate(R.layout.extendable_child_item, null);
        }

        TextView tv = (TextView) convertView.findViewById(R.id.note_body);
        tv.setText(child.getName().toString());
        return convertView;

    }

    @Override
    public int getChildrenCount(int groupPosition) {
        ArrayList<ChildItem> chList = groups.get(groupPosition).getItems();

        return chList.size();
    }

    @Override
    public Object getGroup(int groupPosition) {
        return groups.get(groupPosition);
    }

    @Override
    public int getGroupCount() {
        return groups.size();
    }

    @Override
    public long getGroupId(int groupPosition) {
        return groupPosition;
    }

    @Override
    public View getGroupView(int groupPosition, boolean isExpanded,
		View convertView, ViewGroup parent) {
        GroupItem group = (GroupItem) getGroup(groupPosition);

        if (convertView == null) {
            LayoutInflater inf = (LayoutInflater) context.getSystemService(context.LAYOUT_INFLATER_SERVICE);
            convertView = inf.inflate(R.layout.extendable_group_item, null);
        }

/*
		if (groupPosition % 2 == 1) {
			if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.JELLY_BEAN) {
				convertView.setBackground(context.getResources().getDrawable(R.drawable.cliper_one_row_bg));
			} else {
				convertView.setBackgroundDrawable(context.getResources().getDrawable(R.drawable.cliper_one_row_bg));
			}
		} else {
			if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.JELLY_BEAN) {
				convertView.setBackground(context.getResources().getDrawable(R.drawable.cliper_two_row_bg));
			} else {
				convertView.setBackgroundDrawable(context.getResources().getDrawable(R.drawable.cliper_two_row_bg));
			}
		}
*/
        TextView tv = (TextView) convertView.findViewById(R.id.group_name);

		if (group.isBlocked()) {
			tv.setTextColor(context.getResources().getColor(R.color.floatBlockColor));
		} else {
			tv.setTextColor(context.getResources().getColor(R.color.floatTextColor));
		}

        tv.setText(group.getName());
        return convertView;
    }

    @Override
    public boolean hasStableIds() {
        return true;
    }

    @Override
    public boolean isChildSelectable(int groupPosition, int childPosition) {
        return true;
    }
}
