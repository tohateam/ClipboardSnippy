package ua.tohateam.clipboardsnippy;

import android.app.*;
import android.content.*;
import android.database.*;
import android.os.*;
import android.util.*;
import android.view.*;
import android.widget.*;
import ua.tohateam.clipboardsnippy.data.*;
import ua.tohateam.clipboardsnippy.settings.*;

public class EditClipActivity extends Activity {
	private final String TAG = "CM-EditNote";

	private EditText mNoteLabel;
	private EditText mNote;
	private CheckBox mBlocked;

	private int rowId;
	private String mClipTitle;
	private String mClipBody;
	private boolean mClipFixed;

	protected void onCreate(Bundle bundle) {
		super.onCreate(bundle);
		ThemeUtils.onActivityCreateSetTheme(this, MainActivity.mTheme);
		this.setContentView(R.layout.editnote_activity);

		mNoteLabel = (EditText) findViewById(R.id.title_note);
		mNote = (EditText) findViewById(R.id.body_note);
		mBlocked = (CheckBox) findViewById(R.id.blocked_note);
		
		CliperHelper dbHelper = new CliperHelper(this);
		dbHelper.open();
		rowId = getIntent().getIntExtra("currentId", 0);

		Cursor cursor = dbHelper.fetchIdClips(rowId);

		if (cursor != null) {
			if (cursor.moveToFirst()) {
				mClipTitle = cursor.getString(cursor.getColumnIndexOrThrow("title"));
				mClipBody = cursor.getString(cursor.getColumnIndexOrThrow("body"));
				mClipFixed = cursor.getInt(cursor.getColumnIndexOrThrow("fixed")) > 0;
			}
		} else {
			Log.e(TAG, "Current cursor is empty");
		}

		mNote.setText(mClipBody);
		mNoteLabel.setText(mClipTitle);
		mBlocked.setChecked(mClipFixed);
		dbHelper.close();
	}

	@Override
	public void onStart() {
		super.onStart();
	}

	@Override
	public void onStop() {
		super.onStop();
	}

	/*********************************************************************
	 * Основное меню
	 *********************************************************************/
	@Override
	public boolean onCreateOptionsMenu(Menu menu) {
		new MenuInflater(this).inflate(R.menu.edit_note_menu, menu);
		return (super.onCreateOptionsMenu(menu));
	}

	@Override
	public boolean onOptionsItemSelected(MenuItem item) {		
		CliperHelper dbHelper = new CliperHelper(this);
		dbHelper.open();
		Intent intent = new Intent();
		
		int fix = 0;
		if(mBlocked.isChecked()) {
			fix = 1;
		} else {
			fix = 0;
		}

		switch (item.getItemId()) {
			case R.id.action_item_save:
				dbHelper.updateClip(
					rowId, mNoteLabel.getText().toString(), 
					mNote.getText().toString().toString(), fix);
				setResult(RESULT_OK, intent);
				finish();
				break;
			case R.id.action_item_cancel:
				setResult(RESULT_CANCELED, intent);
				finish();
				break;
		}
		dbHelper.close();
		return false;
	}

}
