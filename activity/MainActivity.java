package com.example.dell.indoorlocation.activity;

import android.Manifest;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.os.Build;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.v7.app.AppCompatActivity;
import android.text.method.ScrollingMovementMethod;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.TextView;
import android.widget.Toast;

import com.baidu.ocr.sdk.OCR;
import com.baidu.ocr.sdk.OnResultListener;
import com.baidu.ocr.sdk.exception.OCRError;
import com.baidu.ocr.sdk.model.AccessToken;
import com.baidu.ocr.sdk.model.GeneralResult;
import com.baidu.ocr.sdk.model.Word;
import com.baidu.ocr.sdk.model.WordSimple;
import com.example.dell.indoorlocation.R;
import com.example.dell.indoorlocation.model.LocationInfo;
import com.example.dell.indoorlocation.model.StandardLocationInfo;
import com.example.dell.indoorlocation.service.RecognizeService;
import com.example.dell.indoorlocation.tools.CalculationTest;
import com.example.dell.indoorlocation.tools.Constant;
import com.example.dell.indoorlocation.tools.FileUtil;
import com.example.dell.indoorlocation.tools.TextDetection;


import static org.opencv.core.Core.sort;
import static org.opencv.features2d.FastFeatureDetector.TYPE_9_16;
import static org.opencv.imgcodecs.Imgcodecs.imread;
import static org.opencv.imgcodecs.Imgcodecs.imwrite;
import static org.opencv.imgproc.Imgproc.COLOR_BGR2GRAY;
import static org.opencv.imgproc.Imgproc.COLOR_RGB2GRAY;
import static org.opencv.imgproc.Imgproc.cvtColor;
import static org.opencv.imgproc.Imgproc.resize;



import org.opencv.android.BaseLoaderCallback;
import org.opencv.android.CameraBridgeViewBase;
import org.opencv.android.LoaderCallbackInterface;
import org.opencv.android.OpenCVLoader;
import org.opencv.android.CameraBridgeViewBase.CvCameraViewFrame;
import org.opencv.android.CameraBridgeViewBase.CvCameraViewListener2;
import org.opencv.android.Utils;
import org.opencv.core.Core;
import org.opencv.core.CvType;
import org.opencv.core.DMatch;
import org.opencv.core.Mat;
import org.opencv.core.MatOfDMatch;
import org.opencv.core.MatOfKeyPoint;
import org.opencv.core.MatOfPoint;
import org.opencv.core.Rect;
import org.opencv.core.Scalar;
import org.opencv.features2d.BOWImgDescriptorExtractor;
import org.opencv.features2d.DescriptorExtractor;
import org.opencv.features2d.DescriptorMatcher;
import org.opencv.features2d.FastFeatureDetector;
import org.opencv.features2d.FeatureDetector;
import org.opencv.features2d.Features2d;
import org.opencv.features2d.FlannBasedMatcher;

import org.opencv.features2d.ORB;
import org.opencv.imgproc.Imgproc;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.HashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;


import org.opencv.core.*;
import org.opencv.imgcodecs.*;
import org.opencv.imgproc.*;

import static org.opencv.imgcodecs.Imgcodecs.imread;
import static org.opencv.imgcodecs.Imgcodecs.imwrite;
import static org.opencv.imgproc.Imgproc.COLOR_BGR2GRAY;
import static org.opencv.imgproc.Imgproc.COLOR_RGB2GRAY;
import static org.opencv.imgproc.Imgproc.cvtColor;
import static org.opencv.imgproc.Imgproc.resize;


public class MainActivity extends AppCompatActivity implements View.OnClickListener{

    private static final String TAG = "MainActivity";
    private Button btn_video;
    private Button btn_camera_single;
    private Button btn_camera_many;
    private Button btn_calculate_location_image;
    private Button btn_calculate_location_video;
    private Button btn_clear_data;
    private Button btn_compress_image;
    private TextView content;

    private boolean hasGotToken = false;

    /**
     * 权限请求值
     */
    private static final int PERMISSION_REQUEST_CODE = 0;

    private static final int PICK_REQUEST_CODE = 10;

    //每个POI被识别时出现的照片对应的时间戳
    private static Map<String,String[]> POITimeStamp=new HashMap<>();

    //两个点求位置时需要的水平轴向右方向对应的指南针角度
    double startAngle=195.0;

    //是否是顺时针
    boolean isClockWise =true;

    //输入图像的宽度、高度
    double resizeWidth = 0.0;
    double resizeHeight = 0.0;

    //百度api参数
    String[] APIKeys = {"CeGtacQjmOyCSciOfqDVCk1E","I4sv90hRQAksb7DGZfvG6RMj","dlPot2hEysVufIKGUwPfIz4d","FqBdZw75pqSvDS53lzwrjkhP","Kl8WxhkPl43f4N6rVn0CGGrD","4ARn3uAYpQlut5ZRlskvlQ3M"};
    String[] secretKeys = {"ve5O8Rs9l9M6nVETVYAIcsUqC8iAEUi4","mfy417EDw4d6o00RF2GLekGrVGGqHcAd","pb7CQ0OhGGEGMVqOaIOnpm7L8d7PUFg2","tMnYQw4Aht41j0Wx0iaG1FsVxUXyKkws","xXbGUGlSuwI7pGjiUIzwpsLdkAYtugzS","QZtoTKipwVU6GmfiVRGMmCtAnYL7kHxH"};
    int currentIndex = 1;

    String wordRecognization = "";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        btn_video = (Button) findViewById(R.id.btn_video);
        btn_video.setOnClickListener(this);

        btn_camera_single= (Button) findViewById(R.id.btn_camera_single);
        btn_camera_single.setOnClickListener(this);

        btn_camera_many= (Button) findViewById(R.id.btn_camera_many);
        btn_camera_many.setOnClickListener(this);

        btn_calculate_location_image= (Button) findViewById(R.id.btn_calculate_location_image);
        btn_calculate_location_image.setOnClickListener(this);

        btn_calculate_location_video= (Button) findViewById(R.id.btn_calculate_location_video);
        btn_calculate_location_video.setOnClickListener(this);

        btn_clear_data= (Button) findViewById(R.id.btn_clear_data);
        btn_clear_data.setOnClickListener(this);

        btn_compress_image = (Button)findViewById(R.id.btn_compress_image);
        btn_compress_image.setOnClickListener(this);

        content= (TextView) findViewById(R.id.tv_content);
        content.setMovementMethod(ScrollingMovementMethod.getInstance());
        if (Build.VERSION.SDK_INT >= 23) {
            if (checkSelfPermission(Manifest.permission.READ_EXTERNAL_STORAGE) != PackageManager.PERMISSION_GRANTED ||
                    checkSelfPermission(Manifest.permission.WRITE_EXTERNAL_STORAGE) != PackageManager.PERMISSION_GRANTED) {
                requestPermissions(new String[]{Manifest.permission.WRITE_EXTERNAL_STORAGE, Manifest.permission.READ_EXTERNAL_STORAGE}, PERMISSION_REQUEST_CODE);
            }
        }
        initAccessTokenWithAkSk();
    }

    private static long lastClickTime;  //双击清空数据变量

    @Override
    public void onClick(View v) {
        switch (v.getId())
        {
            case R.id.btn_video:
                startActivity(new Intent(MainActivity.this,VideoActivity.class));
                break;
            case R.id.btn_camera_single:
                startActivity(new Intent(MainActivity.this,CameraSingleActivity.class));
                break;
            case R.id.btn_camera_many:
                startActivity(new Intent(MainActivity.this,CameraManyActivity.class));
                break;
            case R.id.btn_clear_data:
                if(isFastClick(1000))
                {
                    File dir=new File(Constant.PROJECT_FILE_PATH);
                    boolean isSuccess=FileUtil.deleteDir(dir);
                    Log.d(TAG, "delete dir:"+isSuccess);
                    content.setText("");
                    toast("delete dir:"+isSuccess);
                }
                else
                {
                    toast("click again to clear data!");
                }
                break;
            case R.id.btn_calculate_location_image:
//                new Thread(
//                        new Runnable() {
//                            @Override
//                            public void run() {
//                                calculateLocationByImage();
//                            }
//                        }
//                ).start();
                calculateLocationByImage();
                break;
            case R.id.btn_calculate_location_video:
                new Thread()
                {
                    @Override
                    public void run() {
                        calculateLocationByVideo();
                    }
                }.start();
                break;
            case R.id.btn_compress_image:
                compressImages();
                break;

        }
    }

    //压缩图片
    public void compressImages()
    {
        //获取照片路径下的所有照片
        final List<String> fileNameList= FileUtil.getFileName(Constant.IMG_FILE_PATH);
        for(final String fileName:fileNameList)
        {
            //图片预处理,将图片尺寸压缩到4分之1
            new Thread(new Runnable() {
                @Override
                public void run() {
                    Mat image =  imread(Constant.IMG_FILE_PATH + fileName);
                    compressImage(image,fileName);
                }
            }).start();
        }

    }

    //判断是否是快速点击
    public static boolean isFastClick(long ClickIntervalTime) {
        long ClickingTime = System.currentTimeMillis();
        if ( ClickingTime - lastClickTime < ClickIntervalTime) {
            return true;
        }
        lastClickTime = ClickingTime;
        return false;
    }

    //通过视频计算位置
    public void calculateLocationByVideo()
    {

    }
    //通过图片计算位置
    public void calculateLocationByImage(){
        List<String> fileNameList= FileUtil.getFileName(Constant.IMG_FILE_PATH);//获取照片路径下的所有照片
        Map<String,StandardLocationInfo> standardLocationInfoMap=getLocationStandardInfoMap();//标准的位置信息
        Map<String,LocationInfo> locationInfoMap=new HashMap<>();//待识别的位置信息
        getWordAndLocationByImages(fileNameList,standardLocationInfoMap,locationInfoMap);
    }
    //根据图像识别结果推测目标位置
    public void inferenceLocation(Map<String,LocationInfo> locationInfoMap,Map<String,StandardLocationInfo> standardLocationInfoMap,final long startTime)
    {
        String isStepStr="是否有行走：";
        try {
            List<String> sensorFileName= FileUtil.getFileName(Constant.SENSOR_RECORD_PATH);//获得多个文件名
            getSensorLocationInfo(locationInfoMap,sensorFileName.get(0));//获得第一个文件名，//获取每一张照片拍摄时对应的传感器数据
            System.out.println("inferenceLocation locationInfoMap："+locationInfoMap.size()+"---" +locationInfoMap );
            boolean isStep=FileUtil.isStep(Constant.SENSOR_RECORD_PATH,sensorFileName.get(0));//判断是否有行走事件
            if(isStep)isStepStr+="是\n";
            else isStepStr+="否\n";
        } catch (IOException e) {
            e.printStackTrace();
            System.out.println("inferenceLocation 异常："+e.getMessage() );
        }
        if(locationInfoMap.size()>=3){ //三个以上POI，通过梯度下降计算当前坐标(拍照者的坐标)
            List<String> locationNameList=new ArrayList<>();
            List<Double> angleList=getAngleByInfoMap(locationInfoMap,locationNameList);
            List<Double[]> coordinateList=new ArrayList<>();//获取已识别的角标位置信息
            for(String locationName:locationNameList){
                Double[] coordinate=new Double[2];
                coordinate[0]=standardLocationInfoMap.get(locationName).getX();
                coordinate[1]=standardLocationInfoMap.get(locationName).getY();
                coordinateList.add(coordinate);
                System.out.println("coordinate：x="+coordinate[0]+" y="+coordinate[1]);
            }
            System.out.println("angleList："+angleList+"locationNameList："+locationNameList);

            final String result=isStepStr+printLocationInfoMap(locationInfoMap);
            final Double[] answer= TextDetection.cal_corrdinate(angleList, coordinateList);
            System.out.println("answer："+answer);
            final String mediumString=getMediumString(angleList,locationNameList);
            System.out.println("mediumString："+mediumString);
            runOnUiThread(new Runnable() {
                @Override
                public void run()
                {
                    long finishTime = System.currentTimeMillis();
                    String s="中间信息：\n"+mediumString+result+"location answer:"+answer[0]+","+answer[1]+ " 耗时" + (finishTime - startTime) + "毫秒";
                    content.setText(s);
                    FileUtil.writeStrToFile(s+"\n"+wordRecognization);
                    System.out.println(s);
                }
            });
        }  else if(locationInfoMap.size()==2){//当POI数量为2时，采用指南针相对信息计算位置
            final StandardLocationInfo locationInfo= CalculationTest.getLocaionBy2Points(startAngle,locationInfoMap,standardLocationInfoMap,isClockWise);
            final String result=isStepStr+printLocationInfoMap(locationInfoMap);
            runOnUiThread(new Runnable() {
                //@Override
                public void run()
                {
                    long finishTime = System.currentTimeMillis();
                    String s=result+"location answer:("+locationInfo.getX()+","+locationInfo.getY()+")"+" 耗时" + (finishTime - startTime) + "毫秒";
                    content.setText(s); //真正的坐标为（137,140）
                    FileUtil.writeStrToFile(s+"\n"+wordRecognization);
                }
            });
        }  else{
            final String result=isStepStr+printLocationInfoMap(locationInfoMap);
            String tagNames="";
            for(String key:locationInfoMap.keySet())  tagNames+=key+"\n";
            final String result2=tagNames;
            runOnUiThread(new Runnable() {
                @Override
                public void run() {
                    long finishTime = System.currentTimeMillis();
                    String s=result+"\n耗时" + (finishTime - startTime) + "毫秒"+"\n" +result2;
                    content.setText(s);
                    FileUtil.writeStrToFile(s );
                }}
            );
        }
    }

    /**
     * 用明文ak，sk初始化
     */
    private void initAccessTokenWithAkSk() {
        OCR.getInstance(this).initAccessTokenWithAkSk(new OnResultListener<AccessToken>() {
            @Override
            public void onResult(AccessToken result) {
                String token = result.getAccessToken();
                hasGotToken = true;
            }

            @Override
            public void onError(OCRError error) {
                error.printStackTrace();
                toast("AK，SK方式获取token失败");
            }
        },getApplicationContext(),APIKeys[currentIndex],secretKeys[currentIndex]);
    }

    private boolean checkTokenStatus() {
        if (!hasGotToken) {
            Toast.makeText(getApplicationContext(), "token还未成功获取", Toast.LENGTH_LONG).show();
        }
        return hasGotToken;
    }

    //通过图片名称列表获取文字以及位置信息
    public void getWordAndLocationByImages(final  List<String> fileNames, final Map<String,StandardLocationInfo> standardLocationInfoMap, final Map<String,LocationInfo> locationInfoMap)
    {
        if(!checkTokenStatus())
        {
            return;
        }
        final long startTime = System.currentTimeMillis();
        final HashMap<String,List<LocationInfo>> resultMap = new HashMap<>();
        final String finalFileName = fileNames.get(fileNames.size()-1);
        Mat image = imread(Constant.IMG_FILE_PATH + finalFileName);
        resizeWidth = image.width();
        resizeHeight = image.height();
        System.out.println("resizeWidth:"+resizeWidth+",resizeHeight:"+resizeHeight); //Log.d(TAG, "finalImageName:" + finalFileName);





/*

        //1）输入图像，并变灰
        Mat im1= imread(Constant.IMG_FILE_PATH + "1a.jpg");
        cvtColor(im1,im1,COLOR_BGR2GRAY);
        imwrite(Constant.OUTPUT_FILE_PATH + "a1a.jpg",im1);

        Mat im2= imread(Constant.IMG_FILE_PATH + "1b.jpg");
        cvtColor(im2,im2,COLOR_RGB2GRAY);
        imwrite(Constant.OUTPUT_FILE_PATH + "a1b.jpg",im2);
        //2）对灰度图像计算特征点、描述点
        //FastFeatureDetector fd = FastFeatureDetector.create( 10,  true, TYPE_9_16);
        ORB fd = ORB.create( );
        MatOfKeyPoint kp1=new MatOfKeyPoint(),kp2=new MatOfKeyPoint();
        Mat des1=new Mat(),des2=new Mat(),mask=new Mat();
        fd.detectAndCompute(im1,mask, kp1,des1);//fd.detect(im2, kp2);
        fd.detectAndCompute(im2,mask, kp2,des2);
        //3）根据描述点，进行匹配
        FlannBasedMatcher matcher=new FlannBasedMatcher();
        //DescriptorMatcher matcher = DescriptorMatcher .create(DescriptorMatcher.FLANNBASED);//.BRUTEFORCE_HAMMING);
        // DescriptorMatcher Matcher = DescriptorMatcher.create(DescriptorMatcher.BRUTEFORCE_L1);
        MatOfDMatch matches = new MatOfDMatch();
        matcher.match(des1, des2, matches);
        //4.2）输出：好的匹配点，输出到图像
        int yy=get_goodMatches(matches);
        DMatch[] mats = matches.toArray();
        for (int i = 0; i < mats.length; i++) {
            System.out.println("matches2 i="+i+",queryIdx:"+mats[i].queryIdx+",trainIdx:"+mats[i].trainIdx
                    +",imgIdx:"+mats[i].imgIdx+",distance:"+mats[i].distance);
        }

        //4.1）输出：匹配点输出到图像
        Mat output=new Mat();
        Features2d.drawMatches(im1,kp1,im2, kp2, matches, output);
        imwrite(Constant.OUTPUT_FILE_PATH + "aoutput.jpg",output);
        //matchbitmap=Bitmap.createScaledBitmap(testimg, output.width(),  output.height(), false);
        //Utils.matToBitmap(output, matchbitmap);1111

















*/






        for(String fileName:fileNames){
            System.out.println("fileName:"+fileName   ); //Log.d(TAG, "imageName:" + fileName);
            RecognizeService.recGeneral(MainActivity.this,fileName,
                    new RecognizeService.ServiceListener() {
                        @Override
                        public void onResult(String fileName,GeneralResult result) {
                            ArrayList<LocationInfo> locationInfoList = new ArrayList<LocationInfo>();
                            if(result!=null){
                                wordRecognization += result.getJsonRes()+"\n";
                                for (WordSimple wordSimple : result.getWordList()) {
                                    Word word = (Word) wordSimple;
                                    LocationInfo location = new LocationInfo();
                                    location.setContent(word.toString());
                                    location.setTop(word.getLocation().getTop());
                                    location.setLeft(word.getLocation().getLeft());
                                    location.setWidth(word.getLocation().getWidth());
                                    location.setHeight(word.getLocation().getHeight());
                                    location.setFileName(fileName);
                                    locationInfoList.add(location);
                                }
                            }
                            resultMap.put(fileName,locationInfoList);
                            if(fileName.equals(finalFileName)){
                                produceInput(standardLocationInfoMap,resultMap,locationInfoMap);
                                System.out.println("------- produceInput");
                                resultMap.clear();
                                inferenceLocation(locationInfoMap,standardLocationInfoMap,startTime);
                                System.out.println("------- inferenceLocation");
                            }
                        }
                    });
            try {
                //20180727
                Thread.sleep(1000);
            } catch (InterruptedException e) {
                e.printStackTrace();
            }
        }
    }
    private boolean jud_ge(String  s1,String s2){//s1 识别出的字符串 s2 poi预设字符串 s2被处理过而s1没有
        if(s1==null||s2==null)return false;
        int len1=s1.length();
        int len2=s2.length();
        s1=chan_ge(s1);
        if(len1==0||len2==0 )return false;
        if(len1!=len2){
            if(len1>len2){
            //    System.out.println("include: ori: "+s1+"new: " + s2);
                if(s1.contains(s2)) return true;
            }
            return false;
        }
        char c1,c2;
        for(int i=0;i<len1;i++){
            c1=s1.charAt(i);
            c2=s2.charAt(i);
            if(c1!=c2){
                if(c1=='s'&&(c2=='S' ) )continue;
                if(c2=='s'&&(c1=='S' ) )continue;

                if(c1=='0'&&(c2=='o'||c2=='O') )continue;
                if(c2=='0'&&(c1=='o'||c1=='O') )continue;
                if(c1=='1'&&(c2=='i'||c2=='I'||c2=='l'||c2=='L') )continue;
                if(c2=='1'&&(c1=='i'||c1=='I'||c1=='l'||c1=='L') )continue;
                return false;
            }
        }
        return true;
    }
    private String chan_ge(String POIName){
        if(POIName==null )return null;
        int len1=POIName.length();
        if(len1==0)return "";
        char c1;
        String ss="";
        for(int i=0;i<len1;i++){
            c1=POIName.charAt(i);
            if(c1!='0'){
                if( c1=='o'||c1=='O'  )c1='0';
            }else if(c1!='1'){
                if( c1=='i'||c1=='I'||c1=='l'||c1=='L'  )c1='1';
            }else if(c1=='S'){
                c1='s';
            }
            ss+=c1;
        }
        return ss;
    }
    //通过文字识别的结果构造下一步输入
    public void produceInput(Map<String,StandardLocationInfo> standardLocationInfoMap, HashMap<String,List<LocationInfo>> resultMap, Map<String,LocationInfo> locationInfoMap)
    {
        for(String fileName:resultMap.keySet()){
            List<LocationInfo> locationInfos = resultMap.get(fileName);
            for(LocationInfo locationInfo:locationInfos){
                for(String POIName:standardLocationInfoMap.keySet()){
                    //if(locationInfo.getContent().contains(POIName)||Constant.calculateStringDistance(locationInfo.getContent(),POIName)>50)
                    if(jud_ge(locationInfo.getContent(),POIName)  ){
                        //不包含（初次加入系统） 或者 包含的距离更大
                        if(!locationInfoMap.containsKey(POIName) ||locationInfoMap.get(POIName).getCenterDistance()>getCenterDistance(locationInfo,resizeWidth,resizeHeight))
                        {
                            locationInfo.setCenterDistance(getCenterDistance(locationInfo,resizeWidth,resizeHeight));
                            locationInfoMap.put(POIName,locationInfo);
                            System.out.println("\n"+"POIName:" +  POIName +" x="+standardLocationInfoMap.get(POIName).getX()+" y=" +standardLocationInfoMap.get(POIName).getY() );
                        }
                    }
                }
            }
        }
    }

    //通过POI名称、夹角信息获取中间输出信息
    public static String getMediumString(List<Double> angleList,List<String> locationNameList)
    {
        StringBuilder stringBuilder=new StringBuilder();
        for(int i=0;i<locationNameList.size()-1;i++)
        {
            stringBuilder.append(locationNameList.get(i)+"->"+locationNameList.get(i+1)+":");
            stringBuilder.append(angleList.get(i)+"\n");
        }
        return stringBuilder.toString();
    }


    //返回固定位置的标准数据
    public Map getLocationStandardInfoMap(){
        Map<String,StandardLocationInfo> hashMap=new HashMap<>();
        StandardLocationInfo standardLocationInfo;




        //室内拍照点（4,1）
        standardLocationInfo=new StandardLocationInfo(-20,-30);
        hashMap.put(chan_ge("MIshA"),standardLocationInfo);


//        standardLocationInfo=new StandardLocationInfo(0,75);
        standardLocationInfo=new StandardLocationInfo(0,65);
        hashMap.put(chan_ge("HAIN"),standardLocationInfo);

        standardLocationInfo=new StandardLocationInfo(0,130);
        hashMap.put(chan_ge("GUCCI"),standardLocationInfo);

        standardLocationInfo=new StandardLocationInfo(0,150);
        hashMap.put(chan_ge("NOBLE SIGHT"),standardLocationInfo);

//        standardLocationInfo=new StandardLocationInfo(75,20);
//        hashMap.put(chan_ge("HUAWEI"),standardLocationInfo);

        standardLocationInfo=new StandardLocationInfo(75,70);
        hashMap.put(chan_ge("植物医生"),standardLocationInfo);
        standardLocationInfo=new StandardLocationInfo(75,65);
        hashMap.put(chan_ge("医"),standardLocationInfo);


       //室内拍照点（0,0）
        standardLocationInfo=new StandardLocationInfo(-8,-8.5);
        hashMap.put(chan_ge("COMEL"),standardLocationInfo);

        standardLocationInfo=new StandardLocationInfo(-12.5,-5);
        hashMap.put(chan_ge("VERO"),standardLocationInfo);

        standardLocationInfo=new StandardLocationInfo(-5,10);
        hashMap.put(chan_ge("国"),standardLocationInfo);

        standardLocationInfo=new StandardLocationInfo(0,-8);
        hashMap.put(chan_ge("BHG"),standardLocationInfo);

       standardLocationInfo=new StandardLocationInfo(4,0.5);
        hashMap.put(chan_ge("ove"),standardLocationInfo);

       // standardLocationInfo=new StandardLocationInfo(11,8.2);
       // hashMap.put(chan_ge("UN"),standardLocationInfo);

        standardLocationInfo=new StandardLocationInfo(11,8.5);
        hashMap.put(chan_ge("QLO"),standardLocationInfo);


      /*  standardLocationInfo=new StandardLocationInfo(50.0,70.0);
        hashMap.put(chan_ge("MICHAEL KORS"),standardLocationInfo);//"MICHAEL KORS"
        standardLocationInfo=new StandardLocationInfo(10,0);
        hashMap.put(chan_ge("CERRUTI 1881"),standardLocationInfo);//"CERRUTI 188l"
        standardLocationInfo=new StandardLocationInfo(0,60);
        hashMap.put(chan_ge("FURLA"),standardLocationInfo);//"FURLA"
        standardLocationInfo=new StandardLocationInfo(0.0,88);
        hashMap.put(chan_ge("CAPEL"),standardLocationInfo);//"CAPEL"
*/
/*

        standardLocationInfo=new StandardLocationInfo(58,0);
        hashMap.put(chan_ge("McDonald"),standardLocationInfo);//"McDonald"  McDonad's  "McDonals"},"McDonald"}]}"OnilsulkaTiger"},
        standardLocationInfo=new StandardLocationInfo(70,0);//麦当劳
        hashMap.put(chan_ge("watsons"),standardLocationInfo);//"watsons醒氏"}watsons氏"wATsOns臣氏"

        standardLocationInfo=new StandardLocationInfo(76.0,5);
        hashMap.put(chan_ge("BEANPOLE"),standardLocationInfo);

        standardLocationInfo=new StandardLocationInfo(76.0,19);
        hashMap.put(chan_ge("HAZZYS"),standardLocationInfo);

        standardLocationInfo=new StandardLocationInfo(76.0,33);
        hashMap.put(chan_ge("MILANO"),standardLocationInfo);

        standardLocationInfo=new StandardLocationInfo(76.0,47);
        hashMap.put(chan_ge("MANGANO"),standardLocationInfo);// "MANGAN""MANGANO"

        standardLocationInfo=new StandardLocationInfo(76.0,61);
        hashMap.put(chan_ge("FYNCH-HATTON"),standardLocationInfo);//"FYNCH-I" "FYNCH-hattOn""FYNCH-HATTON"

        standardLocationInfo=new StandardLocationInfo(76.0,75);
        hashMap.put(chan_ge("LACOSTE"),standardLocationInfo);//"LACOSTE K""LACOSTE F""LACOSTE KF" "LACOSTE"


        standardLocationInfo=new StandardLocationInfo(76.0,97);
        hashMap.put(chan_ge("CASIO"),standardLocationInfo);//"CASI"  "CAsIO"

        standardLocationInfo=new StandardLocationInfo(34.0,100);
        hashMap.put(chan_ge("GANT"),standardLocationInfo);//"GANT"
        standardLocationInfo=new StandardLocationInfo(48.0,100);
        hashMap.put(chan_ge("Timberland"),standardLocationInfo);//"Timberland""Timberland@"
        standardLocationInfo=new StandardLocationInfo(62.0,109);
        hashMap.put(chan_ge("AIGLE"),standardLocationInfo);//"③ AIGLE"  "AIGLE"    "OAIGLE"}]}
*/

        return hashMap;
    }
    //压缩图片到指定比例
    public void compressImage(Mat originalImage, String fileName)
    {
        if (!new File(Constant.PROCESSED_FILE_PATH).exists()) {
            new File(Constant.PROCESSED_FILE_PATH).mkdirs();
        }
        Mat compressImage=originalImage.clone();
        float width=originalImage.width();
        float height=originalImage.height();
        resizeWidth = originalImage.width();
        resizeHeight = originalImage.height();
        System.out.println("oooofileName: "+fileName);//System.out.println("width:"+ resizeWidth +",height:" + resizeHeight);
        float scale=Constant.IMAGE_SCALE;
        //scale= (float) (originalImage.width()/640.0);
        resize(originalImage,compressImage,new Size(width * scale, height * scale));
        imwrite(Constant.PROCESSED_FILE_PATH + fileName,compressImage);
    }

    //计算目标中心与图片中心的距离
    public double getCenterDistance(LocationInfo locationInfo, double width, double height)
    {
//        double distance=0;
//        double centerX=(double)locationInfo.getLeft()+(double)locationInfo.getWidth()/2.0;
//        //rect.x+(rect.width)/2;
//        double centerY=(double)locationInfo.getTop()+(double)locationInfo.getHeight()/2.0;
//                //rect.y+(rect.height)/2;
//        distance=(width/2-centerX)*(width/2-centerX)+(height/2-centerY)*(height/2-centerY);
//        return distance;

        double distance = 0;
        double centerX = (double) locationInfo.getLeft()+(double)locationInfo.getWidth()/2.0;
        distance = (width/2 - centerX)*(width/2 - centerX);
        return distance;

    }

    //输出当前记录的数据
    public String printLocationInfoMap(Map<String,LocationInfo> locationInfoMap)
    {
        String result="";
        for(String key:locationInfoMap.keySet())
        {
            Log.d(TAG, "已识别文字："+key);
            Log.d(TAG, "fileName:"+locationInfoMap.get(key).getFileName());
            Log.d(TAG, "centerDistance:"+locationInfoMap.get(key).getCenterDistance());
            Log.d(TAG, "timeStamp:"+locationInfoMap.get(key).getTimeStamp());
            Log.d(TAG, "angle:"+locationInfoMap.get(key).getAngle());
            result+="已识别文字："+key+",fileName:"+locationInfoMap.get(key).getFileName()+",centerDistance:"+locationInfoMap.get(key).getCenterDistance()+",timeStamp:"+locationInfoMap.get(key).getTimeStamp()+",angle:"+locationInfoMap.get(key).getAngle()
                    +"(top,left):"+"("+locationInfoMap.get(key).getTop()+","+locationInfoMap.get(key).getLeft()+"),"+"width:"+locationInfoMap.get(key).getWidth()+",height:"+locationInfoMap.get(key).getHeight()
                    +"\n";
        }
        return result;
    }

    //通过图片名得到该图片的部分位置信息
    public void getSensorLocationInfo(Map<String,LocationInfo> locationInfoMap,String sensorFileName) throws IOException {
        for(String key:locationInfoMap.keySet())
        {
            LocationInfo locationInfo=locationInfoMap.get(key);
            String fileName=locationInfo.getFileName();

            String[] fileNames_=fileName.split("_");
            String[] fileNamesPoint=fileNames_[1].split("\\.");
            Log.d(TAG, "calculateLocation->timestamp:"+fileNamesPoint[0]);
            //填充时间戳信息
            locationInfo.setTimeStamp(fileNamesPoint[0]);

            String sensorInfo=FileUtil.getSensorInfo(Constant.SENSOR_RECORD_PATH,sensorFileName,fileNamesPoint[0]);
            Log.d(TAG, "calculateLocation->sensorInfo:"+sensorInfo);
            //填充方向传感器信息
            locationInfo.setSensorInfo(sensorInfo);

            if(sensorInfo!=null&&!sensorInfo.equals(""))
            {
                String[] oriSensorInfoArray=sensorInfo.split(",");
                double angle=0;
                if(oriSensorInfoArray.length==2)
                {
                    for(String oriSensorInfo:oriSensorInfoArray)
                    {
                        String[] elements=oriSensorInfo.split(" ");
                        angle+=Double.parseDouble(elements[2]);
                    }
                    angle/=2.0;
                }
                else if(oriSensorInfoArray.length==1)
                {
                    String[] elements=oriSensorInfoArray[0].split(" ");
                    angle=Double.parseDouble(elements[2]);
                }
                Log.d(TAG, "calculateLocation->angle:"+angle);
                //填充角度信息
                locationInfo.setAngle(angle);

                locationInfoMap.put(key,locationInfo);
            }
        }

    }

    public List<Double> getAngleByInfoMap(Map<String,LocationInfo> locationInfoMap,List<String> locationNameList)
    {
        List<Double> angleList=new ArrayList<>();
        Map<String,Double> locationAngleMap=new HashMap<>();
        for(String key:locationInfoMap.keySet())
        {
            locationAngleMap.put(key,locationInfoMap.get(key).getAngle());
        }
        //对角度进行排序
        List<Map.Entry<String, Double>> locationInfos =
                new ArrayList<>(locationAngleMap.entrySet());
//        for(int i=0;i<locationInfos.size();i++)
//        {
//            System.out.println("key:"+locationInfos.get(i).getKey()+",value:"+locationInfos.get(i).getValue());
//        }
        Collections.sort(locationInfos, new Comparator<Map.Entry<String, Double>>() {
            @Override
            public int compare(Map.Entry<String, Double> stringDoubleEntry, Map.Entry<String, Double> t1) {
                return stringDoubleEntry.getValue().compareTo(t1.getValue());
            }
        });
//        for(int i=0;i<locationInfos.size();i++)
//        {
//            System.out.println("key:"+locationInfos.get(i).getKey()+",value:"+locationInfos.get(i).getValue());
//        }
        int curPos=-1;
        for(int i=0;i<locationInfos.size()-1;i++)
        {
            Double angle=locationInfos.get(i+1).getValue()-locationInfos.get(i).getValue();
            if(angle>180)
            {
                curPos=i+1;
                break;
            }
        }
//        System.out.println("curPos:"+curPos);
        //两两夹角均在180度以内
        if(curPos==-1)
        {
            for(int i=0;i<locationInfos.size()-1;i++)
            {
                locationNameList.add(locationInfos.get(i).getKey());
                angleList.add(locationInfos.get(i+1).getValue()-locationInfos.get(i).getValue());
            }
            locationNameList.add(locationInfos.get(locationInfos.size()-1).getKey());
            return angleList;
        }
        //有一个夹角大于180 需要调整顺序
        else
        {
            List<Map.Entry<String, Double>> locationInfos_copy=new ArrayList<>();
            for(int i=curPos;i<locationInfos.size();i++)
            {
                locationInfos_copy.add(locationInfos.get(i));
            }
            for(int i=0;i<curPos;i++)
            {
                locationInfos_copy.add(locationInfos.get(i));
            }
            for(int i=0;i<locationInfos_copy.size()-1;i++)
            {
                locationNameList.add(locationInfos_copy.get(i).getKey());
                Double angle=locationInfos_copy.get(i+1).getValue()-locationInfos_copy.get(i).getValue();
                angleList.add(angle>=0?angle:angle+360);
            }
            locationNameList.add(locationInfos_copy.get(locationInfos_copy.size()-1).getKey());
            return angleList;
        }
    }



    @Override
    protected void onResume() {
        super.onResume();
        //load OpenCV engine and init OpenCV library
        OpenCVLoader.initAsync(OpenCVLoader.OPENCV_VERSION_3_4_0, getApplicationContext(), mLoaderCallback);
        Log.i(TAG, "onResume sucess load OpenCV...");
    }


    //OpenCV库加载并初始化成功后的回调函数
    private BaseLoaderCallback mLoaderCallback = new BaseLoaderCallback(this) {

        @Override
        public void onManagerConnected(int status) {
            // TODO Auto-generated method stub
            switch (status){
                case BaseLoaderCallback.SUCCESS:
                    Log.i(TAG, "成功加载");
                    break;
                default:
                    super.onManagerConnected(status);
                    Log.i(TAG, "加载失败");
                    break;
            }
        }
    };

    /**
     * 请求到权限后在这里复制识别库
     *
     * @param requestCode
     * @param permissions
     * @param grantResults
     */
    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        Log.i(TAG, "onRequestPermissionsResult: " + grantResults[0]);
        switch (requestCode) {
            case PERMISSION_REQUEST_CODE:
                if (grantResults.length > 0 && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                    Log.i(TAG, "onRequestPermissionsResult: copy");
                }
                break;
            default:
                break;
        }
    }

    public void toast(String content)
    {
        Toast.makeText(this,content,Toast.LENGTH_SHORT).show();
    }

    int get_goodMatches( MatOfDMatch allMatches )
    {//欧氏距离distance小于3倍最小欧氏距离,且小于0.2 的方法，取出最好匹配）
        int minNum15 = 15, minNum4 = 4;//DMatch[] mats2 = allMatches.toArray();Matches.fromList(goodMatch);List<DMatch> mch=matches.toList();

        if(allMatches==null)return 0;
        DMatch[] mats = allMatches.toArray();
        if (mats==null|| mats.length < minNum15) return 0;

        double minDist = Double.MAX_VALUE;
        for (int i = 0; i < mats.length; i++) {
            double dist = mats[i].distance;
            if (dist < minDist)           minDist = dist;//if (dist > maxDist)           maxDist = dist;
        }
        List<DMatch> goodMatch=new LinkedList<>();// 图像的关键点匹配  ;
        for (int i = 0; i < mats.length; i++) {
            double dist = mats[i].distance;
            if (dist < 3 * minDist && dist < 0.2f) {
                goodMatch.add(mats[i]);
            }
        }
        if(goodMatch.size()<=minNum4)return 0;

        minDist = Double.MAX_VALUE;
        DMatch dch0=new DMatch();
        for (DMatch dch:goodMatch) {
            double dist = dch.distance;
            if (dist < minDist){
                minDist = dist;
                dch0=dch;
            }
            System.out.println("goodMatch  queryIdx:"+dch.queryIdx+",trainIdx:"+dch.trainIdx
                    +",imgIdx:"+dch.imgIdx+",distance:"+dch.distance);
        }

        allMatches.fromList(goodMatch);
        System.out.println("dch0:"+dch0+",goodMatch:"+goodMatch);
        return dch0.queryIdx;//返回一个
    }


}
