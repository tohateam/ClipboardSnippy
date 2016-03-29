package ua.tohateam.clipboardsnippy;

import android.app.*;
import android.content.*;
import android.content.SharedPreferences.*;
import android.database.*;
import android.os.*;
import android.view.*;
import android.widget.*;
import android.widget.AdapterView.*;
import android.widget.ExpandableListView.*;
import android.widget.PopupMenu.*;
import java.util.*;
import ua.tohateam.clipboardsnippy.data.*;
import ua.tohateam.clipboardsnippy.extendable.*;
import ua.tohateam.clipboardsnippy.services.*;
import ua.tohateam.clipboardsnippy.settings.*;

import android.widget.AdapterView.OnItemLongClickListener;

public class MainActivity extends Activity 
implements OnSharedPreferenceChangeListener, OnMenuItemClickListener {

	static final int EDIT_NOTE_REQUEST = 1;

	private int currentNoteId;
	private boolean showPopupNotification = SnippySettings.DEFAULT_SHOW_POPUP_NOTIFICATION;
	public static String mTheme = SnippySettings.DEFAULT_APP_THEME;

	private ExpandListAdapter ExpAdapter;
    private ArrayList<GroupItem> ExpListItems;
    private ExpandableListView ExpandList;

	MenuItem mBlockedMenuItem;

	private BroadcastReceiver clipboardChangedReceiver = new BroadcastReceiver() {
		public void onReceive(Context context, Intent intent) {
			if (ClipboardMonitorService.ACTION_CLIPBOARD_CHANGED.equals(intent.getAction())) {
				showListView();
			}
		}
	};

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
		startInit();
		ThemeUtils.onActivityCreateSetTheme(this, mTheme);
        setContentView(R.layout.expandable_activity);
		showListView();
	}

	/*********************************************************************
	 * Методы активности
	 *********************************************************************/
	@Override
	protected void onResume() {
		showListView();
		super.onResume();
	}

	@Override
	protected void onPause() {
		super.onPause();
	}

	@Override
	protected void onStart() {
		super.onStart();

		getBaseContext().registerReceiver(clipboardChangedReceiver,
			new IntentFilter(ClipboardMonitorService.ACTION_CLIPBOARD_CHANGED));
	}

	@Override
	protected void onDestroy() {
		super.onDestroy();
	}

	/*********************************************************************
	 * Основное меню
	 *********************************************************************/
	@Override
	public boolean onCreateOptionsMenu(Menu menu) {
		new MenuInflater(this).inflate(R.menu.main, menu);
		return (super.onCreateOptionsMenu(menu));
	}

	@Override
	public boolean onOptionsItemSelected(MenuItem item) {
		CliperHelper dbHelper = new CliperHelper(this);
		dbHelper.open();

		switch (item.getItemId()) {
			case R.id.settings:
				startActivity(new Intent(this, SnippySettings.class));
				return true;
			case R.id.item_clear:
				dbHelper.deleteAllNoBlocked();
				showListView();
				return true;
			case R.id.item_refresh:
				showListView();
				return true;
		}
		dbHelper.close();
		return false;
	}

	/****************************************************************
	 * Настройки
	 ***************************************************************/
	private void startInit() {
		// Check if clipboard monitoring should be enabled at start
		SharedPreferences prefs = getSharedPreferences(SnippySettings.SETTINGS_NAME, 0);
		if (prefs != null) {
			prefs.registerOnSharedPreferenceChangeListener(this);

			boolean isMonitoringEnabled = prefs.getBoolean(SnippySettings.CLIPBOARD_MONITORING,
				SnippySettings.DEFAULT_CLIPBOARD_MONITORING);
			// Если монитор буфера разрешён, то запускаем сервис
			if (isMonitoringEnabled) {
				getBaseContext().startService(
					new Intent(getBaseContext(), ClipboardMonitorService.class));
			}

			// Показывать всплывающие уведомления
			showPopupNotification = prefs.getBoolean(SnippySettings.CLIPBOARD_MONITORING,
				SnippySettings.DEFAULT_CLIPBOARD_MONITORING);

			// Тема приложения 
			mTheme = prefs.getString(SnippySettings.APP_THEME, SnippySettings.DEFAULT_APP_THEME);
		}
	}

	@Override
	public void onSharedPreferenceChanged(SharedPreferences sharedPreferences, String key) {
		boolean isMonitoringEnabled = sharedPreferences.getBoolean(
			SnippySettings.CLIPBOARD_MONITORING,
			SnippySettings.DEFAULT_CLIPBOARD_MONITORING);

		// Start/Stop monotoring
		if (SnippySettings.CLIPBOARD_MONITORING.equals(key)) {
			isMonitoringEnabled = sharedPreferences.getBoolean(
				SnippySettings.CLIPBOARD_MONITORING,
				SnippySettings.DEFAULT_CLIPBOARD_MONITORING);
			if (isMonitoringEnabled) {
				getBaseContext().startService(new Intent(getBaseContext(), ClipboardMonitorService.class));
			} else {
				getBaseContext().stopService(new Intent(getBaseContext(), ClipboardMonitorService.class));
			}
		}

		// Change notifocation
		if (SnippySettings.SHOW_NOTIFICATION.equals(key)
			|| SnippySettings.SHOW_FLOAT_WINDOW.equals(key)
			|| SnippySettings.SHOW_NOTIFICATION_TOP.equals(key)) {

			if (isMonitoringEnabled) {
				getBaseContext().stopService(new Intent(getBaseContext(), ClipboardMonitorService.class));
				getBaseContext().startService(new Intent(getBaseContext(), ClipboardMonitorService.class));
			}
		}

		if (SnippySettings.APP_THEME.equals(key)) {
			// Тема приложения 
			mTheme = sharedPreferences.getString(SnippySettings.APP_THEME, SnippySettings.DEFAULT_APP_THEME);
			if (mTheme.equals("light")) {
				ThemeUtils.changeToTheme(this, ThemeUtils.THEME_LIGHT);
			} else {
				ThemeUtils.changeToTheme(this, ThemeUtils.THEME_DARK);
			}

		}
	}

	/*********************************************************************
	 * Расширенный список
	 *********************************************************************/
	private void showListView() {
        ExpandList = (ExpandableListView) findViewById(R.id.expandableListView);
		// Показать быструю прокрутку
//		ExpandList.setFastScrollEnabled(true);
        ExpListItems = setStandardGroups();
        ExpAdapter = new ExpandListAdapter(this, ExpListItems);
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

	/*********************************************************************
	 * Заполняем список
	 *********************************************************************/
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
					int rowId = cursor.getInt(cursor.getColumnIndex("_id"));
					String title = cursor.getString(cursor.getColumnIndex("title"));
					String body = cursor.getString(cursor.getColumnIndex("body"));
					boolean isBlocked = cursor.getInt(cursor.getColumnIndex("fixed")) > 0;

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
	 * Всплывающее меню списка
	 *********************************************************************/
	private void showPopupMenu(View view) {
		PopupMenu popupMenu = new PopupMenu(this, view);
		popupMenu.setOnMenuItemClickListener(this);
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
				showListView();
				break;
			case R.id.item_edit:
				Intent intent = new Intent(this, EditClipActivity.class);
				intent.putExtra("currentId", currentNoteId);
				startActivityForResult(intent, EDIT_NOTE_REQUEST);
				dbHelper.close();
				break;
			case R.id.item_delete:
				dbHelper.deleteClip(currentNoteId);
				showListView();
				break;
			case R.id.item_refresh:
				showListView();
				break;
		}

		dbHelper.close();
		return true;
	}

	/*********************************************************************
	 * Показать всплывающее уведомление
	 *********************************************************************/
	public void showToastMsg(String Msg) {
		if (showPopupNotification)
			Toast.makeText(getApplicationContext(), Msg, Toast.LENGTH_SHORT).show();
	}

	@Override
	protected void onActivityResult(int requestCode, int resultCode, Intent data) {
		// Check which request we're responding to
		if (requestCode == EDIT_NOTE_REQUEST) {
			// Make sure the request was successful
			if (resultCode == RESULT_OK) {
				showListView();
			}
		}
	}
}
