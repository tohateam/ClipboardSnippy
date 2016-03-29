package ua.tohateam.clipboardsnippy;

import android.app.*;
import android.content.*;
import android.database.*;
import android.graphics.*;
import android.os.*;
import android.view.*;
import android.view.View.*;
import android.view.WindowManager.*;
import android.widget.*;
import android.widget.AdapterView.*;
import android.widget.ExpandableListView.*;
import android.widget.PopupMenu.*;
import java.util.*;
import ua.tohateam.clipboardsnippy.data.*;
import ua.tohateam.clipboardsnippy.extendable.*;
import ua.tohateam.clipboardsnippy.services.*;

import android.view.View.OnClickListener;
import android.view.WindowManager.LayoutParams;
import android.widget.AdapterView.OnItemLongClickListener;

public class FloatingActivity extends Service
implements OnMenuItemClickListener {

	private boolean showPopupNotification = SnippySettings.DEFAULT_SHOW_POPUP_NOTIFICATION;
	private boolean mPositionRight = SnippySettings.DEFAULT_SHOW_FLOATWINDOW_RIGHT;

	public LinearLayout mFloatLayout;
	private WindowManager.LayoutParams wmParams;
	private WindowManager mWindowManager;

	private ImageButton mButtonClose;
	private ImageButton mButtonClear;
	private ImageButton mButtonRefresh;

	private int currentNoteId;

	private ExpandListAdapter ExpAdapter;
    private ArrayList<GroupItem> ExpListItems;
    private ExpandableListView ExpandList;


	private BroadcastReceiver clipboardMonitorReceiver = new BroadcastReceiver() {
		public void onReceive(Context context, Intent intent) {
			showNoteList();
		}
	};

	@Override
	public void onCreate() {
		super.onCreate();
		startInit();
		IntentFilter filterClipboard = new IntentFilter(ClipboardMonitorService.ACTION_CLIPBOARD_CHANGED);
		registerReceiver(clipboardMonitorReceiver, filterClipboard);
		createFloatView();
		showNoteList();
	}

	@Override
	public IBinder onBind(Intent intent) {
		// TODO Auto-generated method stub
		return null;
	}

	private void createFloatView() {
		wmParams = new WindowManager.LayoutParams();
		mWindowManager = (WindowManager) getApplication().getSystemService(getApplication().WINDOW_SERVICE);
		wmParams.type = LayoutParams.TYPE_PHONE;
		wmParams.flags = LayoutParams.FLAG_NOT_FOCUSABLE;
		wmParams.format = PixelFormat.RGBA_8888;

		if (mPositionRight) {
			wmParams.gravity = Gravity.RIGHT | Gravity.TOP;
		} else {
			wmParams.gravity = Gravity.LEFT | Gravity.TOP;
		}
		wmParams.x = 0;
		wmParams.y = 0;
		wmParams.width = LayoutParams.WRAP_CONTENT;
		wmParams.height = LayoutParams.WRAP_CONTENT;

		LayoutInflater inflater = LayoutInflater.from(getApplication());
		mFloatLayout = (LinearLayout) inflater.inflate(R.layout.floating_activity, null);
		mWindowManager.addView(mFloatLayout, wmParams);

		mFloatLayout.setOnTouchListener(new View.OnTouchListener() {
				WindowManager.LayoutParams updatedParameters = wmParams;
				double x, y, pressedX, pressedY;

				@Override
				public boolean onTouch(View v, MotionEvent event) {
					switch (event.getAction()) {
						case MotionEvent.ACTION_DOWN:
							x = updatedParameters.x;
							y = updatedParameters.y;
							pressedX = event.getRawX();
							pressedY = event.getRawY();
							break;
						case MotionEvent.ACTION_MOVE:
							if (mPositionRight) {
								// Показать окно справа
								updatedParameters.x = (int) (x + (pressedX - event.getRawX()));
							} else {
								// Показать окно слева или по центру
								updatedParameters.x = (int) (x + (event.getRawX() - pressedX));
							}
							updatedParameters.y = (int) (y + (event.getRawY() - pressedY));
							mWindowManager.updateViewLayout(mFloatLayout, updatedParameters);
							break;
						default:
							break;
					}
					return false;
				}
			});

		mButtonClose = (ImageButton) mFloatLayout.findViewById(R.id.btn_float_close);
		mButtonClose.setOnClickListener(new OnClickListener() {
				@Override
				public void onClick(View v) {
					stopSelf();
				}
			});

		mButtonClear = (ImageButton) mFloatLayout.findViewById(R.id.btn_float_clear);
		mButtonClear.setOnClickListener(new OnClickListener() {
				@Override
				public void onClick(View v) {
					CliperHelper dbHelper = new CliperHelper(FloatingActivity.this);
					dbHelper.open();
					dbHelper.deleteAllNoBlocked();
					dbHelper.close();
					showNoteList();
				}
			});

		mButtonRefresh = (ImageButton) mFloatLayout.findViewById(R.id.btn_float_refresh);
		mButtonRefresh.setOnClickListener(new OnClickListener() {
				@Override
				public void onClick(View v) {
					showNoteList();
				}
			});

	}

	@Override
	public void onDestroy() {
		super.onDestroy();
		if (mFloatLayout != null) {
			mWindowManager.removeView(mFloatLayout);
			unregisterReceiver(clipboardMonitorReceiver);
		}
		getBaseContext().startService(new Intent(getBaseContext(), ClipboardMonitorService.class));
	}

	/*********************************************************************
	 * Расширяемый список
	 *********************************************************************/
	private void showNoteList() {
        ExpandList = (ExpandableListView) mFloatLayout.findViewById(R.id.float_exp_list);
        ExpListItems = setStandardGroups();
        ExpAdapter = new ExpandListAdapter(FloatingActivity.this, ExpListItems);
        ExpandList.setAdapter(ExpAdapter);

        ExpandList.setOnChildClickListener(new OnChildClickListener() {
				@Override
				public boolean onChildClick(ExpandableListView parent, View v,
					int groupPosition, int childPosition, long id) {
					ArrayList<ChildItem> ch_list = ExpListItems.get(groupPosition).getItems();
					// Копируем в буфер обмена текущее значение
					ClipboardManager clipboard = (ClipboardManager) getSystemService(CLIPBOARD_SERVICE);
					showToastMsg(getString(R.string.clip_copied));
					clipboard.setText(ch_list.get(childPosition).getName());
//					Intent intent = new Intent();
					stopSelf();
					return false;
				}
			});

		ExpandList.setOnItemLongClickListener(new OnItemLongClickListener() {
				public boolean onItemLongClick(AdapterView<?> parent, View view,
					int position, long id) {
					// When clicked on child, function longClick is executed
					if (ExpandableListView.getPackedPositionType(id) == ExpandableListView.PACKED_POSITION_TYPE_CHILD) {
						int groupPosition = ExpandableListView.getPackedPositionGroup(id);
						int childPosition = ExpandableListView.getPackedPositionChild(id);
						ArrayList<ChildItem> ch_list = ExpListItems.get(groupPosition).getItems();
						currentNoteId = ch_list.get(childPosition).getRowId();
						showPopupMenu(view);
						return true;
					}
					return false;
				}
			});

	}

    public ArrayList<GroupItem> setStandardGroups() {
		CliperHelper dbHelper = new CliperHelper(this);
		dbHelper.open();
		Cursor cursor = dbHelper.fetchAllClips();

        ArrayList<GroupItem> group_list = new ArrayList<GroupItem>();
        ArrayList<ChildItem> child_list = null;

		if (cursor != null) {
			if  (cursor.moveToFirst()) {
				do {
					GroupItem group = new GroupItem();
					String body = cursor.getString(cursor.getColumnIndex("body"));
					boolean isBlocked = cursor.getInt(cursor.getColumnIndex("fixed")) > 0;
					int rowId = cursor.getInt(cursor.getColumnIndex("_id"));
					String title = "";

					if (body.length() > 50) {
						title = body.substring(0, 50);
					} else {
						title = body;
					}
					title = title.trim();
					group.setName(title);
					group.setBlocked(isBlocked);
					group.setRowId(rowId);

					child_list = new ArrayList<ChildItem>();
					ChildItem child = new ChildItem();
					child.setName(body);
					child.setRowId(rowId);
					child_list.add(child);

					group.setItems(child_list);
					group_list.add(group);
				} while(cursor.moveToNext());
			}
		}
		dbHelper.close();
		return group_list;
	}

	/*********************************************************************
	 * Показать всплывающее уведомление
	 *********************************************************************/
	public void showToastMsg(String Msg) {
		if (showPopupNotification)
			Toast.makeText(getApplicationContext(), Msg, Toast.LENGTH_SHORT).show();
	}

	/*********************************************************************
	 * Всплывающее меню списка
	 *********************************************************************/
	private void showPopupMenu(View view) {
		PopupMenu popupMenu = new PopupMenu(FloatingActivity.this, view);
		popupMenu.setOnMenuItemClickListener(FloatingActivity.this);
		popupMenu.inflate(R.menu.cotext_menu);
		popupMenu.show();
	}

	@Override
	public boolean onMenuItemClick(MenuItem item) {
		CliperHelper dbHelper = new CliperHelper(this);
		dbHelper.open();
		Cursor cursor = dbHelper.fetchIdClips(currentNoteId);

		switch (item.getItemId()) {
			case R.id.item_block:
				String title = cursor.getString(cursor.getColumnIndex("title"));
				String body = cursor.getString(cursor.getColumnIndex("body"));
				int blocked = cursor.getInt(cursor.getColumnIndex("fixed"));

				if (blocked == 0) blocked = 1;
				else blocked = 0;

				dbHelper.updateClip(currentNoteId, title, body, blocked);
				showNoteList();
				break;
			case R.id.item_edit:
				Intent intent = new Intent(this, EditClipActivity.class);
				intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
				intent.putExtra("currentId", currentNoteId);
				startActivity(intent);
				stopSelf();
				dbHelper.close();
				break;
			case R.id.item_delete:
				dbHelper.deleteClip(currentNoteId);
				showNoteList();
				break;
		}

		dbHelper.close();
		return true;
	}

	/****************************************************************
	 * Настройки
	 ***************************************************************/
	private void startInit() {
		// Check if clipboard monitoring should be enabled at start
		SharedPreferences prefs = getSharedPreferences(SnippySettings.SETTINGS_NAME, 0);
		// Показывать всплывающие уведомления
		showPopupNotification = prefs.getBoolean(SnippySettings.CLIPBOARD_MONITORING,
			SnippySettings.DEFAULT_CLIPBOARD_MONITORING);
	}

}
