package io.github.danthe1st.dndreset;

import android.content.Context;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;

import androidx.annotation.NonNull;
import androidx.work.Constraints;
import androidx.work.NetworkType;
import androidx.work.OneTimeWorkRequest;
import androidx.work.WorkManager;
import androidx.work.Worker;
import androidx.work.WorkerParameters;

public class WaitForWifiRemoveWorkRequest extends Worker {
	public WaitForWifiRemoveWorkRequest(@NonNull Context appContext, @NonNull WorkerParameters workerParams) {
		super(appContext, workerParams);
	}

	@NonNull
	@Override
	public Result doWork() {
		ConnectivityManager connManager = (ConnectivityManager) getApplicationContext().getSystemService(Context.CONNECTIVITY_SERVICE);

		NetworkInfo mWifi = connManager.getNetworkInfo(ConnectivityManager.TYPE_WIFI);

		if(mWifi.isConnected()) {
			return Result.success();
		}
		WorkManager.getInstance(getApplicationContext()).enqueue(
				new OneTimeWorkRequest(new OneTimeWorkRequest.Builder(WaitForWifiConnectedRequest.class)
						.addTag("DND_CONTROL")
						.setConstraints(new Constraints.Builder()
								.setRequiredNetworkType(NetworkType.CONNECTED)
								.build())));
		WorkManager.getInstance(getApplicationContext()).cancelAllWorkByTag("WAIT_FOR_WIFI_REMOVE");
		return Result.success();
	}

}
