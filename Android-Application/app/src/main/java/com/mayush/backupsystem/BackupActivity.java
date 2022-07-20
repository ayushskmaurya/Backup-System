package com.mayush.backupsystem;

import androidx.appcompat.app.AppCompatActivity;

import android.content.Intent;
import android.os.Bundle;
import android.os.Environment;
import android.util.Log;
import android.widget.TextView;
import android.widget.Toast;

import com.android.volley.Request;
import com.android.volley.toolbox.StringRequest;
import com.android.volley.toolbox.Volley;

import org.json.JSONException;
import org.json.JSONObject;

import java.io.File;

public class BackupActivity extends AppCompatActivity {
	private static final String TAG = "AppLogBackupActivity";
	private static final String ENTERED_DIR_PATH = "com.mayush.backupsystem.enteredDirPath";
	private File dir;

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_backup);

		Intent intent = getIntent();
		String enteredDirPath = intent.getStringExtra(ENTERED_DIR_PATH);
		dir = new File(Environment.getExternalStorageDirectory(), enteredDirPath);
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
						response -> {
							Log.d(TAG, response);
							getAndroidDirList(stepInfoView);
						},
						error -> Log.e(TAG, error.toString())
				)
		);
	}

	// Retrieving list of all files & folders of Android.
	private void getAndroidDirList(TextView stepInfoView) {
		String stepInfo = "Retrieving list of all files & folders of Android...";
		stepInfoView.setText(stepInfo);

		if(dir.exists() && dir.isDirectory()) {
			JSONObject androidDirList = new JSONObject();
			android_files(dir, "", androidDirList);
			Log.d(TAG, androidDirList.toString());
		}
		else {
			Toast.makeText(this, "Please enter valid directory path", Toast.LENGTH_SHORT).show();
			this.finish();
		}
	}

	// Listing all the files and folders of the entered directory path.
	private void android_files(File dir, String path, JSONObject androidDirList) {
		File[] files = dir.listFiles();
		for(File file: files) {
			boolean isDir;
			String lastModified = null;

			if(file.isDirectory()) {
				isDir = true;
				android_files(file, file.getName() + "\\", androidDirList);
			}
			else {
				isDir = false;
				lastModified = String.valueOf(file.lastModified() / 1000);
			}

			JSONObject fileInfo = new JSONObject();
			try {
				fileInfo.put("isDir", isDir);
				fileInfo.put("lastModified", lastModified);
				androidDirList.put(path + file.getName(), fileInfo);
			} catch (JSONException e) {
				Log.e(TAG, e.toString());
			}
		}
	}
}