package io.github.danthe1st.dndreset;

import android.content.Context;

import androidx.annotation.NonNull;
import androidx.work.Worker;
import androidx.work.WorkerParameters;

public class WaitForWifiConnectedRequest extends Worker {
	public WaitForWifiConnectedRequest(@NonNull Context context, @NonNull WorkerParameters workerParams) {
		super(context, workerParams);
	}

	@NonNull
	@Override
	public Result doWork() {
		DNDControl.deactivateDNDAndBluetooth(getApplicationContext());
		return Result.success();
	}
}
