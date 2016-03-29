package ua.tohateam.clipboardsnippy.services;

import ua.tohateam.clipboardsnippy.SnippyConstants;
import ua.tohateam.clipboardsnippy.SnippySettings;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.util.Log;

public class BootReceiver extends BroadcastReceiver {
	@Override
	public void onReceive(Context context, Intent intent) {
		if (Intent.ACTION_BOOT_COMPLETED.equals(intent.getAction())) {
			SharedPreferences prefs = context.getSharedPreferences(SnippySettings.SETTINGS_NAME, 0);
			if (prefs != null) {
				// Check if clipboard monitoring service should be started at boot
				boolean isMonitoringEnabled = prefs.getBoolean(SnippySettings.CLIPBOARD_MONITORING,
						SnippySettings.DEFAULT_CLIPBOARD_MONITORING);
				boolean startOnBoot = prefs.getBoolean(SnippySettings.START_ON_BOOT,
						SnippySettings.DEFAULT_START_ON_BOOT);
				if (isMonitoringEnabled && startOnBoot) {
					context.startService(new Intent(context, ClipboardMonitorService.class));
				}
			}
		}
	}
}
