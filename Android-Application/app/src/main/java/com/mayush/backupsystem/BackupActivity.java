package com.mayush.backupsystem;

import androidx.appcompat.app.AppCompatActivity;

import android.content.Intent;
import android.os.Bundle;
import android.widget.TextView;

public class BackupActivity extends AppCompatActivity {
	public static final String ENTERED_DIR_PATH = "com.mayush.backupsystem.enteredDirPath";

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_backup);

		Intent intent = getIntent();
		String enteredDirPath = intent.getStringExtra(ENTERED_DIR_PATH);
		TextView enteredDirPathView = findViewById(R.id.enteredDirPath_activityBackup);
		enteredDirPathView.setText(enteredDirPath);
	}
}