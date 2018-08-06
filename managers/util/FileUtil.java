package com.example.dell.indoorlocation.managers.util;

import java.io.BufferedOutputStream;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;

import android.graphics.Bitmap;
import android.os.Environment;
import android.util.Log;

public class FileUtil {
    private static final String TAG = "FileUtil";
    private static final String DST_FOLDER_NAME = "0";
    private static String storagePath = "";
    /**初始化保存路径
     * @return
     */
    public static String getPath1(){
        try {
            if(storagePath.equals("")){
                storagePath = Environment.getExternalStorageDirectory().getAbsolutePath()+"/" + DST_FOLDER_NAME;
                File f = new File(storagePath);
                if(!f.exists())
                    f.mkdir();
            }
        } catch (Exception e) {
            storagePath = "";
        }
        return storagePath;
    }
    public static String getPathName1(){
        long dataTake = System.currentTimeMillis();
        return  getPath1() + "/" + dataTake +".jpg";
    }
    /**保存Bitmap到sdcard
     * @param b
     */
    public static void saveBitmap(Bitmap b,String jpegName){
        //String jpegName = getPathName1();
        //Log.i(TAG, "saveBitmap:jpegName = " + jpegName);
        try {
            FileOutputStream fout = new FileOutputStream(jpegName);
            BufferedOutputStream bos = new BufferedOutputStream(fout);
            b.compress(Bitmap.CompressFormat.JPEG, 100, bos);
            bos.flush();
            bos.close();
            Log.i(TAG, "saveBitmap,成功:"+jpegName);
        } catch (IOException e) {
            Log.i(TAG, "saveBitmap,失败,"+jpegName);
            e.printStackTrace();
        }
    }


}