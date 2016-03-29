package ua.tohateam.clipboardsnippy.settings;

import android.app.*;
import android.content.*;
import ua.tohateam.clipboardsnippy.*;

public class ThemeUtils {
	private static int sTheme;
	
	public final static int THEME_LIGHT = 0;
	public final static int THEME_DARK = 1;
	public final static int THEME_BLUE = 2;

	/**
	 * Set the theme of the Activity, and restart it by creating a new Activity of the same type.
	 */
	public static void changeToTheme(Activity activity, int theme) {
		sTheme = theme;
		activity.finish();
		activity.startActivity(new Intent(activity, activity.getClass()));
	}

	/** Set the theme of the activity, according to the configuration. */
	public static void onActivityCreateSetTheme(Activity activity, String theme) {
		if (theme.equals("light")) {
			sTheme = THEME_LIGHT;
		} else {
			sTheme = THEME_DARK;
		}
		
		switch (sTheme) {
			default:
			case THEME_LIGHT:
				activity.setTheme(R.style.AppTheme);
				break;
			case THEME_DARK:
				activity.setTheme(R.style.AppThemeDark);
				break;
		}
	}
}
