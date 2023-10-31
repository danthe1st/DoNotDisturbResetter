package io.github.danthe1st.dndreset;

import androidx.activity.result.contract.ActivityResultContract;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;
import androidx.work.Constraints;
import androidx.work.ExistingPeriodicWorkPolicy;
import androidx.work.PeriodicWorkRequest;
import androidx.work.WorkInfo;
import androidx.work.WorkManager;

import android.Manifest;
import android.app.NotificationManager;
import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.TextView;

import com.google.common.util.concurrent.ListenableFuture;

import java.util.List;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.TimeUnit;
import java.util.stream.Collectors;

public class MainActivity extends AppCompatActivity {

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_main);
		findViewById(R.id.reloadButton).setOnClickListener(e->reload());
		findViewById(R.id.setDNDbutton).setOnClickListener(e->activateDND());
		findViewById(R.id.cancelButton).setOnClickListener(e->cancelAll());
	}

	@Override
	protected void onResume() {
		super.onResume();

		reload();
	}

	private void reload() {
		findViewById(R.id.cancelButton).setVisibility(View.INVISIBLE);
		if(ActivityCompat.checkSelfPermission(this, Manifest.permission.BLUETOOTH_ADMIN) == PackageManager.PERMISSION_GRANTED) {
			bluetoothAllowed();
		}else{
			requestPermissions(new String[]{Manifest.permission.BLUETOOTH_ADMIN},1337);
		}
	}

	@Override
	public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
		super.onRequestPermissionsResult(requestCode, permissions, grantResults);
		reload();
	}

	private void bluetoothAllowed() {
		NotificationManager notifManager = (NotificationManager) getSystemService(Context.NOTIFICATION_SERVICE);
		if(notifManager.isNotificationPolicyAccessGranted()) {
			Log.i(getClass().getCanonicalName(), "policy access granted");
			accessGranted(notifManager);
		} else {
			Log.i(getClass().getCanonicalName(), "no policy access, requesting permission");
			registerForActivityResult(new ActivityResultContract<Object, Intent>() {
				@NonNull
				@Override
				public Intent createIntent(@NonNull Context context, Object o) {
					return new Intent(android.provider.Settings.ACTION_NOTIFICATION_POLICY_ACCESS_SETTINGS);
				}

				@Override
				public Intent parseResult(int i, @Nullable Intent intent) {
					return intent;
				}
			}, result -> {
				reload();
			}).launch(null);
		}
	}

	private void cancelAll() {
		WorkManager workManager = WorkManager.getInstance(this);
		workManager.cancelAllWorkByTag("DND_CONTROL");
		DNDControl.deactivateDNDAndBluetooth(this);
		reload();
	}

	private void accessGranted(NotificationManager notifManager) {
		TextView infoText = findViewById(R.id.infoText);
		infoText.setText("can activate do not disturb");
		findViewById(R.id.setDNDbutton).setVisibility(View.VISIBLE);
		WorkManager workManager = WorkManager.getInstance(this);
		ListenableFuture<List<WorkInfo>> workInfos = workManager.getWorkInfosByTag("DND_CONTROL");
		try {
			List<WorkInfo> infos = workInfos.get();
			String workText = infos.stream()
					.filter(info -> !info.getState().isFinished())
					.map(info ->
							info.getTags().stream()
							.collect(Collectors.joining("\n")))
					.collect(Collectors.joining("\n\n"));
			if(!workText.trim().isEmpty()){

				infoText.setText("Active: \n"+workText);
				findViewById(R.id.setDNDbutton).setVisibility(View.INVISIBLE);
				findViewById(R.id.cancelButton).setVisibility(View.VISIBLE);
			}

		} catch(ExecutionException e) {
			throw new RuntimeException(e);
		} catch(InterruptedException e) {
			Thread.currentThread().interrupt();
		}
	}

	private void activateDND() {
		DNDControl.activateDNDAndBluetooth(this);
		PeriodicWorkRequest req = new PeriodicWorkRequest(
				new PeriodicWorkRequest.Builder(WaitForWifiRemoveWorkRequest.class, 15, TimeUnit.MINUTES)
						.setInitialDelay(5,TimeUnit.MINUTES)
						.addTag("WAIT_FOR_WIFI_REMOVE")
						.addTag("DND_CONTROL")
						.setConstraints(new Constraints.Builder().build()));
		WorkManager.getInstance(this)
				.enqueueUniquePeriodicWork("WAIT_FOR_WIFI_REMOVE", ExistingPeriodicWorkPolicy.KEEP,req);
		TextView infoText = findViewById(R.id.infoText);
		infoText.setText("do not disturb activated");
		findViewById(R.id.setDNDbutton).setVisibility(View.INVISIBLE);
		findViewById(R.id.cancelButton).setVisibility(View.VISIBLE);
	}



}