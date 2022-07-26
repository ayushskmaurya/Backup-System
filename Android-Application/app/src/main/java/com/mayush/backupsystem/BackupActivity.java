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

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.File;
import java.util.HashMap;
import java.util.Map;

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
							try {
								getAndroidDirList(stepInfoView, new JSONObject(response));
							} catch (JSONException e) {
								Log.e(TAG, e.toString());
							}
						},
						error -> Log.e(TAG, error.toString())
				)
		);
	}

	// Retrieving list of all files & folders of Android.
	private void getAndroidDirList(TextView stepInfoView, JSONObject pcDirList) {
		String stepInfo = "Retrieving list of all files & folders of Android...";
		stepInfoView.setText(stepInfo);

		if(dir.exists() && dir.isDirectory()) {
			HashMap<String, JSONObject> androidDirList = new HashMap<>();
			android_files(dir, "", androidDirList);
			compareList(pcDirList, androidDirList, stepInfoView);
		}
		else {
			Toast.makeText(this, "Please enter valid directory path", Toast.LENGTH_SHORT).show();
			this.finish();
		}
	}

	// Listing all the files and folders of the entered directory path.
	private void android_files(File dir, String path, HashMap<String, JSONObject> androidDirList) {
		File[] files = dir.listFiles();
		for(File file : files) {
			boolean isDir;
			String lastModified = null;

			if(file.isDirectory()) {
				isDir = true;
				android_files(file, file.getName(), androidDirList);
			}
			else {
				isDir = false;
				lastModified = String.valueOf(file.lastModified() / 1000);
			}

			JSONObject fileInfo = new JSONObject();
			try {
				fileInfo.put("isDir", isDir);
				fileInfo.put("lastModified", lastModified);
				androidDirList.put((new File(path, file.getName())).toString(), fileInfo);
			} catch (JSONException e) {
				Log.e(TAG, e.toString());
			}
		}
	}

	// Comparing the retrieved list of PC with that of Android's.
	private void compareList(JSONObject pcDirList, HashMap<String, JSONObject> androidDirList, TextView stepInfoView) {
		String stepInfo = "Comparing the retrieved list of PC with that of Android's...";
		stepInfoView.setText(stepInfo);

		JSONArray filesToKeep = new JSONArray();
		JSONArray filesToBackup = new JSONArray();
		for(Map.Entry<String, JSONObject> androidFile : androidDirList.entrySet()) {
			String androidFileName = androidFile.getKey();

			try {
				JSONObject pcFileInfo = pcDirList.getJSONObject(androidFileName);
				if(pcFileInfo.getString("is_dir").equals("false")) {
					long androidFileLastModified = Long.parseLong(androidFile.getValue().getString("lastModified"));
					long pcFileLastModified = Long.parseLong(pcFileInfo.getString("last_modified"));
					if(androidFileLastModified > pcFileLastModified)
						filesToBackup.put(androidFileName);
					else
						filesToKeep.put(androidFileName);
				}
				else
					filesToKeep.put(androidFileName);
			} catch (JSONException e) {
				filesToBackup.put(androidFileName);
			}
		}

		delete_files(pcDirList, filesToKeep, filesToBackup, stepInfoView);
	}

	// Deleting unwanted files.
	private void delete_files(JSONObject pcDirList, JSONArray filesToKeep, JSONArray filesToBackup, TextView stepInfoView) {
		String stepInfo = "Deleting unwanted files...";
		stepInfoView.setText(stepInfo);

		Volley.newRequestQueue(this).add(
				new StringRequest(
						Request.Method.POST,
						Base.getBaseUrl() + "/delete_files",
						response -> {
							if(response.equals("1"))
								Log.d(TAG, "filesToBackup: " + filesToBackup);
						},
						error -> Log.e(TAG, error.toString())
				) {
					@Override
					protected Map<String, String> getParams() {
						Map<String, String> params = new HashMap<>();
						params.put("pc_dir_list", pcDirList.toString());
						params.put("files_to_keep", filesToKeep.toString());
						return params;
					}
				}
		);
	}
}