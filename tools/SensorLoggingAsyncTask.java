package com.example.dell.indoorlocation.tools;

import android.hardware.Sensor;
import android.os.AsyncTask;
import android.util.Log;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.PrintWriter;
import java.util.Set;

public class SensorLoggingAsyncTask extends AsyncTask<Object, Long, Boolean> {
	private static final String TAG = "SensorLoggingService";

	@SuppressWarnings("deprecation")
	@Override
	protected Boolean doInBackground(Object... params) {
		File file = (File) params[1];
		@SuppressWarnings("unchecked")
        Set<ComparableSensorEvent> sensorEventSet = (Set<ComparableSensorEvent>) params[0];
		try {
			PrintWriter printWriter = new PrintWriter(file);
			for (ComparableSensorEvent event : sensorEventSet) {
				StringBuilder sb = new StringBuilder();
				switch (event.sensor.getType()) {
				case Sensor.TYPE_ACCELEROMETER:
					sb.append("acc").append(" ");
					break;
				case Sensor.TYPE_MAGNETIC_FIELD:
					sb.append("mag").append(" ");
					break;
				case Sensor.TYPE_GYROSCOPE:
					sb.append("gyro").append(" ");
					break;
				case Sensor.TYPE_ORIENTATION:
					sb.append("ori").append(" ");
					break;
				case Sensor.TYPE_GRAVITY:
					sb.append("grav").append(" ");
					break;
				case Sensor.TYPE_LINEAR_ACCELERATION:
					sb.append("linear_acc").append(" ");
					break;
				case Sensor.TYPE_AMBIENT_TEMPERATURE:
					sb.append("ambi").append(" ");
					break;
				case Sensor.TYPE_LIGHT:
					sb.append("light").append(" ");
					break;
				case Sensor.TYPE_PRESSURE:
					sb.append("press").append(" ");
					break;
				case Sensor.TYPE_STEP_COUNTER:
					sb.append("step").append(" ");
					break;

				}
				// sb.append(event.sensor.getName().replaceAll(" ",
				// "")).append(" ");
				sb.append(event.timestamp).append(" ");
				for (int i = 0; i < 3; i++) {
					if (i < event.values.length) {
						sb.append(event.values[i]).append(" ");
					} else {
						sb.append(0).append(" ");
					}
				}
				Log.d(TAG, "doInBackground:content->"+sb.toString());
				printWriter.println(sb.toString());
			}
			printWriter.flush();
			printWriter.close();
		} catch (FileNotFoundException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}

		return false;
	}
}
