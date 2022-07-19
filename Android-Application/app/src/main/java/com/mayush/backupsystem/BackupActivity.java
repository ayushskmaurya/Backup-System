package com.mayush.backupsystem;

import androidx.appcompat.app.AppCompatActivity;

import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.widget.TextView;

import com.android.volley.Request;
import com.android.volley.toolbox.StringRequest;
import com.android.volley.toolbox.Volley;

public class BackupActivity extends AppCompatActivity {
	private static final String TAG = "AppLogBackupActivity";
	private static final String ENTERED_DIR_PATH = "com.mayush.backupsystem.enteredDirPath";

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_backup);

		Intent intent = getIntent();
		String enteredDirPath = intent.getStringExtra(ENTERED_DIR_PATH);
		TextView enteredDirPathView = findViewById(R.id.enteredDirPath_activityBackup);
		enteredDirPathView.setText(enteredDirPath);

		TextView stepInfoView = findViewById(R.id.stepInfo_activityBackup);
		getPcDirList(stepInfoView);
	}

	// Retrieving list of all files & folders of PC.
	private void getPcDirList(TextView stepInfoView) {
		String stepInfo = "Retrieving list of all files & folders of PC...";
		stepInfoView.setText(stepInfo);

		Volley.newRequestQueue(this).add(
				new StringRequest(
						Request.Method.GET,
						Base.getBaseUrl() + "/get_pc_dir_list",
						response -> Log.d(TAG, response),
						error -> Log.e(TAG, error.toString())
				)
		);
	}
}