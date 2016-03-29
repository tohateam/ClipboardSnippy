package ua.tohateam.clipboardsnippy.services;


import android.app.*;
import android.content.*;
import android.database.*;
import android.graphics.*;
import android.os.*;
import android.support.v4.app.*;
import android.text.*;
import java.util.*;
import java.util.concurrent.*;
import ua.tohateam.clipboardsnippy.*;
import ua.tohateam.clipboardsnippy.data.*;

import android.content.ClipboardManager;

public class ClipboardMonitorService extends Service {
	public static final String ACTION_CLIPBOARD_CHANGED = "ua.tohateam.clipboardsnippy.services.ClipboardChangedEvent";
	public static int mHistorySize = SnippySettings.DEFAULT_HISTORY_SIZE;
	
    private ExecutorService mThreadPool = Executors.newSingleThreadExecutor();
	// Уведомления
	private static final int PERSISTENT_NOTIFICATION_ID = 1;
	private NotificationManager notificationManager;
	private boolean showNotification = SnippySettings.DEFAULT_SHOW_NOTIFICATION;
	private boolean showNotificationTop = SnippySettings.DEFAULT_SHOW_NOTIFICATION_TOP;
	private boolean showFloatWindow = SnippySettings.DEFAULT_SHOW_FLOAT_WINDOW;
	private boolean showPopupNotification = SnippySettings.DEFAULT_SHOW_POPUP_NOTIFICATION;

    private ClipboardManager mClipboardManager;
	private boolean saveOk = false;

    @Override
    public void onCreate() {
        super.onCreate();
        mClipboardManager = (ClipboardManager) getSystemService(CLIPBOARD_SERVICE);
        mClipboardManager.addPrimaryClipChangedListener(mOnPrimaryClipChangedListener);
		notificationManager = (NotificationManager) getSystemService(NOTIFICATION_SERVICE);

		SharedPreferences prefs = getSharedPreferences(SnippySettings.SETTINGS_NAME, 0);

		// Уведомления в строке состояния
		showNotification = prefs.getBoolean(SnippySettings.SHOW_NOTIFICATION,
			SnippySettings.DEFAULT_SHOW_NOTIFICATION);
		showNotificationTop = prefs.getBoolean(SnippySettings.SHOW_NOTIFICATION_TOP,
			SnippySettings.DEFAULT_SHOW_NOTIFICATION_TOP);
		showFloatWindow = prefs.getBoolean(SnippySettings.SHOW_FLOAT_WINDOW,
			SnippySettings.DEFAULT_SHOW_FLOAT_WINDOW);
		// Всплывающие уведомления
		showPopupNotification = prefs.getBoolean(SnippySettings.CLIPBOARD_MONITORING,
			SnippySettings.DEFAULT_CLIPBOARD_MONITORING);
		// Кол-во записей
		String historySize = prefs.getString(SnippySettings.HISTORY_SIZE,
			Integer.toString(SnippySettings.DEFAULT_HISTORY_SIZE));
		this.mHistorySize = Integer.valueOf(historySize);
    }

	@Override
	public int onStartCommand(Intent intent, int flags, int startId) {
		sendNotification();
		return START_NOT_STICKY;
	}

    @Override
    public void onDestroy() {
        super.onDestroy();
        if (mClipboardManager != null) {
            mClipboardManager.removePrimaryClipChangedListener(
				mOnPrimaryClipChangedListener);
        }
    }

    @Override
    public IBinder onBind(Intent intent) {
        return null;
    }

    private ClipboardManager.OnPrimaryClipChangedListener mOnPrimaryClipChangedListener =
	new ClipboardManager.OnPrimaryClipChangedListener() {
        @Override
        public void onPrimaryClipChanged() {
            ClipData clip = mClipboardManager.getPrimaryClip();
            mThreadPool.execute(new WriteHistoryRunnable(clip.getItemAt(0).getText()));
			if (saveOk) {
				Intent intent = new Intent(ACTION_CLIPBOARD_CHANGED);
				sendBroadcast(intent);
			}
        }
    };

    private class WriteHistoryRunnable implements Runnable {
        private final Date mNow;
        private final CharSequence mTextToWrite;

        public WriteHistoryRunnable(CharSequence text) {
            mNow = new Date(System.currentTimeMillis());
            mTextToWrite = text;
        }

        @Override
        public void run() {
            if (TextUtils.isEmpty(mTextToWrite)) {
                // Don't write empty text to the file
                return;
            }
			saveOk = saveClip(mTextToWrite.toString());
		}
    }

	private boolean saveClip(String text) {
		boolean flag = false;
		CliperHelper dbHelper = new CliperHelper(ClipboardMonitorService.this);
		dbHelper.open();
		// Проверяем на дубликат записи
		Cursor cursor = dbHelper.fetchAllClips();
		if (cursor != null) {
			if  (cursor.moveToFirst()) {
				do {
					String noteBody= cursor.getString(cursor.getColumnIndex("body"));
					if (text.equals(noteBody)) {
						flag = true;
					}
				} while (cursor.moveToNext());
			}
		}

		if (!flag) {
			if (showNotification) sendNotification();
			dbHelper.createClip(text.toString(), 0);
		}

		dbHelper.close();
		return flag;
	}

    public void sendNotification() {
		CharSequence title = getText(R.string.app_name);
		CharSequence content = getText(R.string.tap_to_open);
		PendingIntent pendingIntent = null;

        NotificationCompat.Builder builder = new NotificationCompat.Builder(this);
		if (!showFloatWindow) {
			Intent notificationIntent = new Intent(this, FloatingActivity.class);
			pendingIntent = PendingIntent.getService(this, 0, notificationIntent, 0);
		} else {
			pendingIntent = PendingIntent.getActivity(this, 0, new Intent(this, MainActivity.class), 0);
		}
		
        builder.setContentIntent(pendingIntent);
		builder.setOngoing(true); // не удалять свайпом
		builder.setSmallIcon(R.drawable.ic_stat_notify);
        builder.setLargeIcon(BitmapFactory.decodeResource(getResources(), R.drawable.edit_paste));
        builder.setContentTitle(title);
        builder.setContentText(content);
		
		if (showNotificationTop)
			builder.setPriority(Notification.PRIORITY_HIGH);// покзывать вверху
		else
			builder.setPriority(Notification.PRIORITY_DEFAULT);

		if (!showNotification)
			notificationManager.cancel(PERSISTENT_NOTIFICATION_ID);
		else
			notificationManager.notify(PERSISTENT_NOTIFICATION_ID, builder.build());
    }

}
