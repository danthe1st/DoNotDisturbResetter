package io.github.danthe1st.dndreset;

import android.Manifest;
import android.annotation.SuppressLint;
import android.app.NotificationManager;
import android.bluetooth.BluetoothAdapter;
import android.content.Context;
import android.content.pm.PackageManager;

import androidx.annotation.RequiresPermission;
import androidx.core.app.ActivityCompat;

public class DNDControl {
	public static void activateDNDAndBluetooth(Context ctx) {
		NotificationManager notifManager = (NotificationManager) ctx.getSystemService(Context.NOTIFICATION_SERVICE);
		notifManager.setInterruptionFilter(NotificationManager.INTERRUPTION_FILTER_NONE);

		BluetoothAdapter bluetoothAdapter = BluetoothAdapter.getDefaultAdapter();
		if(ActivityCompat.checkSelfPermission(ctx, Manifest.permission.BLUETOOTH_ADMIN) == PackageManager.PERMISSION_GRANTED) {
			bluetoothAdapter.enable();
		}
	}
	public static void deactivateDNDAndBluetooth(Context ctx) {
		NotificationManager notifManager = (NotificationManager) ctx.getSystemService(Context.NOTIFICATION_SERVICE);
		notifManager.setInterruptionFilter(NotificationManager.INTERRUPTION_FILTER_ALL);

		BluetoothAdapter bluetoothAdapter = BluetoothAdapter.getDefaultAdapter();
		if(ActivityCompat.checkSelfPermission(ctx, Manifest.permission.BLUETOOTH_ADMIN) == PackageManager.PERMISSION_GRANTED) {
			bluetoothAdapter.disable();
		}
	}
}
