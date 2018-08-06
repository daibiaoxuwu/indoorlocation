package com.example.dell.indoorlocation.tools;

import android.os.Environment;

import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * Created by DELL on 2018/1/12.
 */

public class Constant {
    //项目存储文件的路径
    public static String PROJECT_FILE_PATH
            = Environment.getExternalStorageDirectory() + "/AndroidCamera/";

    // 照片路径
    public static String IMG_FILE_PATH
            = Environment.getExternalStorageDirectory() + "/AndroidCamera/Camera/";

    // 视频保存路径
    public static String VIDEO_FILE_PATH = Environment.getExternalStorageDirectory() + "/AndroidCamera/Video/";

    // 视频缓存路径
    public static String VIDEO_TEMP_FILE_PATH
            = Environment.getExternalStorageDirectory() + "/Video/TEMP/";

    //传感器数据路径
    public static String SENSOR_RECORD_PATH
            =Environment.getExternalStorageDirectory()+"/AndroidCamera/Sensor/";

    //传感器数据路径
    public static String TESS4J_FILE_PATH
            =Environment.getExternalStorageDirectory()+"/";

    //图像处理的中间图像路径
    public static String PROCESSED_FILE_PATH
            =Environment.getExternalStorageDirectory()+"/AndroidCamera/ProcessImage/";

    //处理结果的文件存储路径
    public static String OUTPUT_FILE_PATH
            //=Environment.getExternalStorageDirectory()+"/OutputPath/";
            =Environment.getExternalStorageDirectory()+"/AndroidCamera/OutputPath/";

    //图片缩放比例
    public static float IMAGE_SCALE=0.25f;//0.14f

    //判断是否在原地的阈值
    public static int STEP_UP_BOUND=3;

    /**
     * @brief 判断两个String字符串的相似程度（因为用户输入的字符可能和兴趣点的名字不能完全匹配）
     * 即计算两个字符串的距离
     * 定义一套操作方法来把两个不相同的字符串变为相同的的方法，忽略字符串大小写
     * 修改／增加／删除
     * 返回的是字符的相似度(百分数)
     */
    public static int calculateStringDistance(String str1,String str2){
        //比较的时候过滤空格
        Pattern p = Pattern.compile("\\s*");
        Matcher m1 = p.matcher(str1);
        Matcher m2 = p.matcher(str2);
        String s1 = m1.replaceAll("").toLowerCase();
        String s2 = m2.replaceAll("").toLowerCase();
        int len1 = s1.length();
        int len2 = s2.length();

        //建立上面说的数组，比字符长度大一个空间
        int[][] dif = new int[len1 + 1][len2 + 1];
        //赋初值
        for (int a = 0; a <= len1; a++) {
            dif[a][0] = a;
        }
        for (int a = 0; a <= len2; a++) {
            dif[0][a] = a;
        }
        //计算两个字符是否一样，计算左上的值
        int temp;
        for (int i = 1; i <= len1; i++) {
            for (int j = 1; j <= len2; j++) {
                if (s1.charAt(i - 1) == s2.charAt(j - 1)) {
                    temp = 0;
                } else {
                    temp = 1;
                }
                //取三个值中最小的
                dif[i][j] = minValue(dif[i - 1][j - 1] + temp, dif[i][j - 1] + 1,
                        dif[i - 1][j] + 1);
            }
        }

        //计算相似度
        float similarity =1 - (float) dif[len1][len2] / Math.max(s1.length(), s2.length());
//        System.out.println("相似度："+similarity);
        return (int)(similarity*100);
    }

    public static int minValue(int a,int b,int c)
    {
        if(a>b)
        {
            a=b;
        }
        return a<c?a:c;
    }

}
