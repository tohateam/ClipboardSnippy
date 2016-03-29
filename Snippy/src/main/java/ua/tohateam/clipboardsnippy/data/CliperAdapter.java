package ua.tohateam.clipboardsnippy.data;

import android.content.*;
import android.database.*;
import android.view.*;
import android.widget.*;
import ua.tohateam.clipboardsnippy.*;

public class CliperAdapter extends CursorAdapter
{
	private LayoutInflater mInflater;
	private int currentPosotion;
	private static final String TAG ="CliperAdapter";
	
	public CliperAdapter(Context context, Cursor c, int flags) {
		super(context, c, flags);
		mInflater = (LayoutInflater) context.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
	}

	public void bindView(View view, Context context, Cursor cursor) {
		currentPosotion = cursor.getPosition();
		
		if (cursor.getPosition() % 2 == 1) {
			view.setBackground(context.getResources().getDrawable(R.drawable.cliper_one_row_bg));
		} else {
			view.setBackground(context.getResources().getDrawable(R.drawable.cliper_two_row_bg));
		}

		TextView tvBody = (TextView) view.findViewById(R.id.clipBody);

		boolean value = cursor.getInt(cursor.getColumnIndexOrThrow("fixed")) > 0;
		if (value) {
			tvBody.setTextColor(context.getResources().getColor(R.color.blockedText));
		} else {
			tvBody.setTextColor(context.getResources().getColor(R.color.defaultText));
		}

		String body = cursor.getString(cursor.getColumnIndexOrThrow("body"));
		tvBody.setText(body);
	}

	@Override
	public View newView(Context context, Cursor cursor, ViewGroup parent) {
		View v =  mInflater.inflate(R.layout.cliper_row_list, parent, false);
//		v.setOnClickListener(ITEM_CLICK_LISTENER);
		return v;
	}
}
