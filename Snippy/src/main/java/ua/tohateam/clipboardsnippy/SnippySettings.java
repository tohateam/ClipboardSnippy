package ua.tohateam.clipboardsnippy;

import android.os.Bundle;
import android.preference.PreferenceActivity;
import android.preference.PreferenceManager;

public class SnippySettings extends PreferenceActivity {
	public static final String SETTINGS_NAME = "SnippySettings";

	public static final String CLIPBOARD_MONITORING 		 = "clipboardMonitoring";
	public static final boolean DEFAULT_CLIPBOARD_MONITORING = false;
	
	public static final String START_ON_BOOT				 = "startOnBoot";
	public static final boolean DEFAULT_START_ON_BOOT		 = false;
	
	public static final String HISTORY_SIZE					 = "historySize";
	public static final int DEFAULT_HISTORY_SIZE			 = 10;
	
	public static final String SHOW_NOTIFICATION 				= "showNotification";
	public static final boolean DEFAULT_SHOW_NOTIFICATION 		= false;
	
	public static final String SHOW_NOTIFICATION_TOP 			= "showNotificationTop";
	public static final boolean DEFAULT_SHOW_NOTIFICATION_TOP 	= false;
	
//	public static final String SHOW_NOTIFICATION_ICON 			= "showNotificationIcon";
//	public static final boolean DEFAULT_SHOW_NOTIFICATION_ICON 	= true;
	
	public static final String SHOW_FLOAT_WINDOW 			= "showFloatWindow";
	public static final boolean DEFAULT_SHOW_FLOAT_WINDOW 	= false;

	public static final String SHOW_FLOATWINDOW_RIGHT			= "showFloatWindowRight";
	public static final boolean DEFAULT_SHOW_FLOATWINDOW_RIGHT 	= true;
	
	public static final String SHOW_POPUP_NOTIFICATION 			= "showPopupNotification";
	public static final boolean DEFAULT_SHOW_POPUP_NOTIFICATION = false;

	public static final String APP_THEME 			= "appTheme";
	public static final String DEFAULT_APP_THEME 	= "light";
	
	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);

		// Tell it where to read/write preferences
		PreferenceManager preferenceManager = getPreferenceManager();
		preferenceManager.setSharedPreferencesName(SETTINGS_NAME);
		preferenceManager.setSharedPreferencesMode(0);

		addPreferencesFromResource(R.xml.preferences);
	}
}
