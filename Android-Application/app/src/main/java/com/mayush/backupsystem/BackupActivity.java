package com.mayush.backupsystem;

import androidx.appcompat.app.AppCompatActivity;

import android.content.Intent;
import android.os.Bundle;
import android.os.Environment;
import android.util.Base64;
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
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.util.HashMap;
import java.util.Map;

public class BackupActivity extends AppCompatActivity {
	private static final String TAG = "AppLogBackupActivity";
	private static final String ENTERED_DIR_PATH = "com.mayush.backupsystem.enteredDirPath";
	private File dir;
	private TextView stepInfoView;
	private String stepInfo;
	private JSONObject pcDirList;
	private HashMap<String, JSONObject> androidDirList;
	private JSONArray filesToKeep, filesToBackup;
	private static boolean shouldContProcess = true, isCurrFileDir = true;
	private InputStream inputStream;
	private int noOfBytesRead;
	private byte[] buffer = new byte[1048576];

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_backup);

		Intent intent = getIntent();
		String enteredDirPath = intent.getStringExtra(ENTERED_DIR_PATH);
		dir = new File(Environment.getExternalStorageDirectory(), enteredDirPath);
		TextView enteredDirPathView = findViewById(R.id.enteredDirPath_activityBackup);
		enteredDirPathView.setText(enteredDirPath);

		if(shouldContProcess) {
			stepInfoView = findViewById(R.id.stepInfo_activityBackup);
			getPcDirList();
		}
		else {
			String msg = "Wait for few seconds and then click on Proceed button or Restart the app.";
			Toast.makeText(this, msg, Toast.LENGTH_LONG).show();
			this.finish();
		}
	}

	// Retrieving list of all files & folders of PC.
	private void getPcDirList() {
		stepInfo = "Retrieving list of all files & folders of PC...";
		stepInfoView.setText(stepInfo);

		Volley.newRequestQueue(this).add(
				new StringRequest(
						Request.Method.GET,
						Base.getBaseUrl() + "/get_pc_dir_list",
						response -> {
							try {
								pcDirList = new JSONObject(response);
								getAndroidDirList();
							} catch (JSONException e) {
								Log.e(TAG, e.toString());
							}
						},
						error -> Log.e(TAG, error.toString())
				)
		);
	}

	// Retrieving list of all files & folders of Android.
	private void getAndroidDirList() {
		stepInfo = "Retrieving list of all files & folders of Android...";
		stepInfoView.setText(stepInfo);

		if(dir.exists() && dir.isDirectory()) {
			androidDirList = new HashMap<>();
			android_files(dir, "");
			compareList();
		}
		else {
			Toast.makeText(this, "Please enter valid directory path", Toast.LENGTH_SHORT).show();
			this.finish();
		}
	}

	// Listing all the files and folders of the entered directory path.
	private void android_files(File dir, String path) {
		File[] files = dir.listFiles();
		for(File file : files) {
			String filePath = (new File(path, file.getName())).toString();
			boolean isDir;
			String lastModified = null;

			if(file.isDirectory()) {
				isDir = true;
				android_files(file, filePath);
			}
			else {
				isDir = false;
				lastModified = String.valueOf(file.lastModified() / 1000);
			}

			JSONObject fileInfo = new JSONObject();
			try {
				fileInfo.put("isDir", isDir);
				fileInfo.put("lastModified", lastModified);
				androidDirList.put(filePath, fileInfo);
			} catch (JSONException e) {
				Log.e(TAG, e.toString());
			}
		}
	}

	// Comparing the retrieved list of PC with that of Android's.
	private void compareList() {
		stepInfo = "Comparing the retrieved list of PC with that of Android's...";
		stepInfoView.setText(stepInfo);

		filesToKeep = new JSONArray();
		filesToBackup = new JSONArray();
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

		delete_files();
	}

	// Deleting unwanted files.
	private void delete_files() {
		stepInfo = "Deleting unwanted files...";
		stepInfoView.setText(stepInfo);

		Volley.newRequestQueue(this).add(
				new StringRequest(
						Request.Method.POST,
						Base.getBaseUrl() + "/delete_files",
						response -> {
							if(response.equals("1"))
								backup_files();
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

	// Taking backup of the files.
	private void backup_files() {
		stepInfo = "Taking backup of the files...";
		stepInfoView.setText(stepInfo);
		getNewFile();
	}

	// Saving chunk of a file in PC.
	private void save_chunk(String filePath, String isDir) {
		String encFileChunkStr = isCurrFileDir ? "" : android.util.Base64.encodeToString(buffer, 0, noOfBytesRead, Base64.DEFAULT);

		Volley.newRequestQueue(this).add(
				new StringRequest(
						Request.Method.POST,
						Base.getBaseUrl() + "/backup_files",
						response -> {
							if(shouldContProcess) {
								try {
									if(!isCurrFileDir && ((noOfBytesRead = inputStream.read(buffer)) != -1))
										save_chunk(filePath, isDir);
									else
										getNewFile();
								} catch (IOException e) {
									Log.e(TAG, e.toString());
								}
							}
							else
								shouldContProcess = true;
						},
						error -> Log.e(TAG, error.toString())
				) {
					@Override
					protected Map<String, String> getParams() {
						Map<String, String> params = new HashMap<>();
						params.put("file_path", filePath);
						params.put("enc_file_chunk_str", encFileChunkStr);
						params.put("is_dir", isDir);
						return params;
					}
				}
		);
	}

	// Getting new file to backup.
	private void getNewFile() {
		try {
			String fileToBackup = filesToBackup.getString(0);
			filesToBackup.remove(0);
			String isDir = androidDirList.get(fileToBackup).getString("isDir");
			isCurrFileDir = Boolean.parseBoolean(isDir);
			if(!isCurrFileDir) {
				inputStream = new FileInputStream(new File(dir, fileToBackup));
				noOfBytesRead = inputStream.read(buffer);
			}
			save_chunk(fileToBackup, isDir);

		} catch (JSONException e) {
			Toast.makeText(this, "Backup Completed.", Toast.LENGTH_SHORT).show();
			this.finish();
		} catch (IOException e) {
			Log.e(TAG, e.toString());
		}
	}

	@Override
	public void onBackPressed() {
		shouldContProcess = false;
		super.onBackPressed();
		finish();
	}
}