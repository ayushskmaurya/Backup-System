package com.mayush.backupsystem;

import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;

import android.Manifest;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.os.Bundle;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Toast;

public class MainActivity extends AppCompatActivity {
	public static final String ENTERED_DIR_PATH = "com.mayush.backupsystem.enteredDirPath";

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_main);

		if(ContextCompat.checkSelfPermission(this,
				Manifest.permission.READ_EXTERNAL_STORAGE) != PackageManager.PERMISSION_GRANTED)
			reqPermissions();

		Button proceedButton = findViewById(R.id.proceed_activityMain);
		proceedButton.setOnClickListener(v -> {
			if(ContextCompat.checkSelfPermission(this,
					Manifest.permission.READ_EXTERNAL_STORAGE) != PackageManager.PERMISSION_GRANTED)
				reqPermissions();

			else {
				EditText enteredDirPathView = findViewById(R.id.dirPath_activityMain);
				String enteredDirPath = enteredDirPathView.getText().toString().trim();

				if(enteredDirPath.length() == 0)
					Toast.makeText(this, "Please enter directory path", Toast.LENGTH_SHORT).show();

				else {
					Intent intent = new Intent(this, BackupActivity.class);
					intent.putExtra(ENTERED_DIR_PATH, enteredDirPath);
					startActivity(intent);
				}
			}
		});
	}

	private void reqPermissions() {
		ActivityCompat.requestPermissions(this, new String[]{
				Manifest.permission.READ_EXTERNAL_STORAGE
		}, 1);
	}
}