/*
 * Copyright (C) 2017 Baidu, Inc. All Rights Reserved.
 */
package com.example.dell.indoorlocation.service;

import android.content.Context;

import com.baidu.ocr.sdk.OCR;
import com.baidu.ocr.sdk.OnResultListener;
import com.baidu.ocr.sdk.exception.OCRError;
import com.baidu.ocr.sdk.model.GeneralParams;
import com.baidu.ocr.sdk.model.GeneralResult;
import com.example.dell.indoorlocation.tools.Constant;

import java.io.File;

public class RecognizeService {

    public interface ServiceListener {
        public void onResult(String fileName, GeneralResult result);
    }

    public static void recGeneral(Context ctx, final String fileName, final ServiceListener listener) {
        GeneralParams param = new GeneralParams();
        param.setDetectDirection(true);
        param.setRecognizeGranularity(GeneralParams.GRANULARITY_BIG);
        //String filePath =  Constant.PROCESSED_FILE_PATH + fileName;
        String filePath = Constant.IMG_FILE_PATH + fileName;
        param.setImageFile(new File(filePath));
        OCR.getInstance(ctx).recognizeGeneral(param, new OnResultListener<GeneralResult>() {
            @Override
            public void onResult(GeneralResult result) {
                listener.onResult(fileName,result);
                System.out.println(result.getJsonRes());
            }

            @Override
            public void onError(OCRError error) {
                System.out.println(error.getErrorCode()+","+error.toString()+","+error.getMessage());
                listener.onResult(fileName,null);
            }
        });
    }

//    public static void recAccurate(Context ctx, String filePath, final ServiceListener listener) {
//        GeneralParams param = new GeneralParams();
//        param.setDetectDirection(true);
//        //param.setVertexesLocation(true);
//        param.setRecognizeGranularity(GeneralParams.GRANULARITY_BIG);
//        param.setImageFile(new File(filePath));
//        final long startTime=System.currentTimeMillis();
//        OCR.getInstance(ctx).recognizeAccurate(param, new OnResultListener<GeneralResult>() {
//            @Override
//            public void onResult(GeneralResult result) {
//                StringBuilder sb = new StringBuilder();
//                for (WordSimple wordSimple : result.getWordList()) {
//                    Word word = (Word) wordSimple;
//                    sb.append(word.getWords());
//                    sb.append("\n");
//                }
//                listener.onResult(result.getJsonRes());
//                System.out.println("消耗"+(System.currentTimeMillis()-startTime)/1000+"毫秒");
//            }
//
//            @Override
//            public void onError(OCRError error) {
//                listener.onResult(error.getMessage());
//            }
//        });
//    }

}
