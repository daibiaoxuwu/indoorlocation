package com.example.dell.indoorlocation.activity;

import android.Manifest;
import android.content.pm.PackageManager;
import android.os.Build;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.view.SurfaceView;
import android.view.View;
import android.view.WindowManager;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.TextView;

import com.example.dell.indoorlocation.R;
import com.example.dell.indoorlocation.managers.CameraManager;

import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;

public class VideoActivity extends AppCompatActivity implements View.OnClickListener {

    private SurfaceView surfaceView;
    private Button btn_record;
    private Button btn_switch;
    private ImageView mFocusView;
    private TextView tv_record_time;

    private int intRecordTime = 0;

    private CameraManager mCameraManager;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        getWindow().setFlags(WindowManager.LayoutParams.FLAG_FULLSCREEN, WindowManager.LayoutParams.FLAG_FULLSCREEN);
        setContentView(R.layout.activity_video);
        initView();
//        Intent intent=new Intent(VideoActivity.this,SensorRecordService.class);
//        startService(intent);
    }

    private void initView() {
        btn_record = (Button) findViewById(R.id.btn_record);
        btn_switch = (Button) findViewById(R.id.btn_switch);
        mFocusView = (ImageView) findViewById(R.id.id_focusView);
        tv_record_time = (TextView) findViewById(R.id.tv_record_time);
        tv_record_time.setVisibility(View.GONE);

        btn_record.setOnClickListener(this);
        btn_switch.setOnClickListener(this);
        surfaceView = (SurfaceView) findViewById(R.id.surfaceView);

        if (surfaceView != null) {
            if(checkPermisson()){
                mCameraManager = new CameraManager(this,surfaceView);
            }
        }
    }

    private static final int REQUEST_PERMISSION_CAMERA_CODE = 1;

    private boolean checkPermisson(){
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
            if (!(checkSelfPermission(Manifest.permission.CAMERA)
                    == PackageManager.PERMISSION_GRANTED)) {
                requestPermissions(new String[]{Manifest.permission.CAMERA,Manifest.permission.RECORD_AUDIO},
                        REQUEST_PERMISSION_CAMERA_CODE);
                return false;
            }else
                return true;
        }else{
            return true;
        }
    }

    @Override
    protected void onResume() {
        super.onResume();
    }

    @Override
    protected void onPause() {
        super.onPause();
        if (mCameraManager != null)
            mCameraManager.stop();
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        if (mCameraManager != null)
            mCameraManager.stop();
    }


    private ScheduledExecutorService executorRecordVideoTime;

    /**
     * 开始记录录制视频的时间
     */
    private void executeRecordVideoTime() {
        executorRecordVideoTime = Executors.newScheduledThreadPool(1);
        executorRecordVideoTime.scheduleAtFixedRate(new Runnable() {
            @Override
            public void run() {
                intRecordTime++;
            }
        }, 0, 1000, TimeUnit.MILLISECONDS);
    }

    private void stopExecuRecordVideoTime() {
        executorRecordVideoTime.shutdown();
    }

    @Override
    public void onClick(View v) {
        int id = v.getId();
        if (id == R.id.btn_record) {
            if ("record".equals(btn_record.getText().toString())) {
                String timeString =System.currentTimeMillis()+"";
                //SensorRecordService.instance().startLogging(timeString);
                mCameraManager.startRecord();
                btn_record.setText("stop");
            } else {
//                btn_record.setText("录像");
                mCameraManager.stopRecord(new CameraManager.RecordResultCallBack() {
                    @Override
                    public void onSuccess() {
                        //SensorRecordService.instance().stopLogging();
                        btn_record.setText("record");
                    }
                    @Override
                    public void onError() {
                        //SensorRecordService.instance().stopLogging();
                        btn_record.setText("record");
                    }
                });
            }
        } else if (id == R.id.btn_switch) {
                 mCameraManager.switchCamera();
        }
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, String[] permissions, int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);

        if (requestCode == REQUEST_PERMISSION_CAMERA_CODE) {
            int grantResult = grantResults[0];
            boolean granted = grantResult == PackageManager.PERMISSION_GRANTED;
            if(granted){
                 mCameraManager = new CameraManager(this,surfaceView);
                 mCameraManager.startPreview();
            }
        }
    }
}
