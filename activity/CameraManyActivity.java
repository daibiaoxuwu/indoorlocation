package com.example.dell.indoorlocation.activity;

import android.Manifest;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.os.Build;
import android.os.Bundle;
import android.os.Handler;
import android.support.v7.app.AppCompatActivity;
import android.view.SurfaceView;
import android.view.View;
import android.view.WindowManager;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.TextView;

import com.example.dell.indoorlocation.R;
import com.example.dell.indoorlocation.managers.CameraManager;
import com.example.dell.indoorlocation.service.SensorRecordService;

import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;

public class CameraManyActivity extends AppCompatActivity implements View.OnClickListener {

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
        setContentView(R.layout.activity_camera_many);
        initView();
        Intent intent=new Intent(CameraManyActivity.this,SensorRecordService.class);
        startService(intent);
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
            if ("autoTakePhoto".equals(btn_record.getText().toString())) {
                String timeString =System.currentTimeMillis()+"";
                SensorRecordService.instance().startLogging(timeString);
                //                //自动拍照逻辑
                //timer.schedule(task,200,200);
                handler.postDelayed(runnable, TIME); //每隔1s执行
                btn_record.setText("stopTakePhoto");
            } else {
                SensorRecordService.instance().stopLogging();
                btn_record.setText("autoTakePhoto");
            }
        } else if (id == R.id.btn_switch) {
            mCameraManager.switchCamera();
        }
    }

    private int TIME = 500;
    Handler handler = new Handler();
    Runnable runnable = new Runnable() {

        @Override
        public void run() {
            // handler自带方法实现定时器
            try {
                mCameraManager.takePhoto();
                handler.postDelayed(this, TIME);
            } catch (Exception e) {
                // TODO Auto-generated catch block
                e.printStackTrace();
                System.out.println("exception..."+e.getMessage());
            }
        }
    };

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
