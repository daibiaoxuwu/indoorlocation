package com.example.dell.indoorlocation.tools;

import android.content.Context;
import android.support.annotation.NonNull;
import android.util.Log;

import com.example.dell.indoorlocation.model.Step;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.math.BigInteger;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

public class FileUtil {
	private static final String TAG = "FileUtil";
	private static final String LOG_FILE_SUFFIX = ".log";
	private static String sLogBasePath;

	/**
	 * 读写文件的线程池，单线程模型
	 */
	private static ExecutorService sExecutorService;

	static {
		sExecutorService = Executors.newSingleThreadExecutor();
	}

	/**
	 * 设置Log存放位置，同时删除超过存放时长的Log
	 *
	 * @param basePath
	 */
	public static void initBasePath(String basePath, int maxSaveDays) {
		sLogBasePath = basePath;
		if (!new File(basePath).exists()) {
			new File(basePath).mkdirs();
		}
		delOldFiles(new File(basePath), maxSaveDays);
	}

	/**
	 * 删除文件夹下所有的 N 天前创建的文件
	 * 注意: 由于拿不到文件的创建时间，这里暂且拿最后修改时间比较
	 *
	 * @param dir
	 * @param days
	 */
	public static void delOldFiles(File dir, int days) {
		long daysMillis = days * 24 * 60 * 60 * 1000L;
		if (dir.exists()) {
			File[] files = dir.listFiles();
			if (files != null) {
				for (int i = 0; i < files.length; i++) {
					if (files[i].isFile() && System.currentTimeMillis() - files[i].lastModified() > daysMillis) {
						files[i].delete();
					}
				}
			}
		}
	}

	/**
	 * 把文本写入文件中
	 *
	 * @param file       目录文件
	 * @param content    待写内容
	 * @param isOverride 写入模式，true - 覆盖，false - 追加
	 */
	public static void write(@NonNull final File file, @NonNull final String content, final boolean isOverride) {
		sExecutorService.execute(new Runnable() {
			@Override
			public void run() {
				FileOutputStream fos = null;
				try {
					boolean isExist = file.exists();
					fos = new FileOutputStream(file, !(!isExist || isOverride));
					fos.write(content.getBytes("UTF-8"));
				} catch (IOException e) {
					Log.e(TAG, e.getMessage());
				} finally {
					if (fos != null) {
						try {
							fos.close();
						} catch (IOException e) {
							Log.e(TAG, e.getMessage());
						}
					}
				}
			}
		});
	}

	public static void writeLog(String content) {
		write(getLogFile(), "\n[" + getFormattedSecond() + "] " + content + "\n\n", false);
	}

	/**
	 * 拿到最新的Log文件
	 *
	 * @return
	 */
	public static File getLogFile() {
		File dir = new File(sLogBasePath);
		if (!dir.exists()) {
			dir.mkdirs();
		}
		File logFile = new File(dir, getFormattedDay() + LOG_FILE_SUFFIX);
		if (!logFile.exists()) {
			try {
				logFile.createNewFile();
			} catch (IOException e) {
				e.printStackTrace();
			}
		}
		return logFile;
	}

	//==================================== TimeUtil =============================================//
	public static final String FORMATTER_DAY = "yy_MM_dd";
	public static final String FORMATTER_SECOND = "yy-MM-dd HH:mm:ss";

	public static SimpleDateFormat sSecondFormat = new SimpleDateFormat(FORMATTER_SECOND);

	public static String getFormattedDay() {
		return new SimpleDateFormat(FORMATTER_DAY).format(new Date());
	}

	public static String getFormattedSecond() {
		return sSecondFormat.format(new Date());
	}


	//==================================== new add =============================================//
	//将指定字符串写入文件
	public static void writeStrToFile(String fileContent)
	{
		SimpleDateFormat df = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");//设置日期格式
		String outputFileName="输出结果："+df.format(new Date());// new Date()为获取当前系统时间
		if (!new File(Constant.OUTPUT_FILE_PATH).exists()) {
			new File(Constant.OUTPUT_FILE_PATH).mkdirs();
		}
		FileOutputStream fos= null;
		try {
			fos = new FileOutputStream(Constant.OUTPUT_FILE_PATH+outputFileName);
			fos.write(fileContent.getBytes());
			fos.close();
		} catch (IOException e) {
			e.printStackTrace();
		}

	}

	//获取某个文件夹下所有文件的名称
	public static List<String> getFileName(String path) {
		List<String> fileNameList=new ArrayList<>();
		File f = new File(path);
		if (!f.exists()) {
			return null;
		}
		File fa[] = f.listFiles();
		for (int i = 0; i < fa.length; i++) {
			File fs = fa[i];
			if (!fs.isDirectory()) {
				fileNameList.add(fs.getName());
			}
		}
		return fileNameList;
	}

	//获取某个文件夹下某张照片后面的照片
	public  static String getNextFileName(String path,String fileName)
	{
		List<String> fileNameList=getFileName(path);
		String answer=null;
		for(int i=0;i<fileNameList.size();i++)
		{
			if(fileNameList.get(i).equals(fileName))
			{
				answer = fileNameList.get(i+1);
			}
		}
		return answer;
	}
	//读取某个时间戳前后的传感器数据
	public static String getSensorInfo(String path,String fileName,String timeStamp) throws IOException {
		String answer="";
		File file=new File(path,fileName);
		//BufferedReader是可以按行读取文件
		FileInputStream inputStream = new FileInputStream(file);
		BufferedReader bufferedReader = new BufferedReader(new InputStreamReader(inputStream));

		String lastStr=null;
		String str = null;
		while((str = bufferedReader.readLine()) != null)
		{
			String[] elements=str.split(" ");
			//方向传感器数据
			if(elements[0].equals("ori"))
			{
				BigInteger findTime=new BigInteger(timeStamp);
				BigInteger curTime=new BigInteger(elements[1]);
				if(findTime.compareTo(curTime)<=0)
				{
					answer=lastStr+","+str;
					break;
				}
				lastStr=str;
			}
		}

		//close
		inputStream.close();
		bufferedReader.close();
		return answer;
	}

	//判断当前的传感器数据是否包含行走路径
	public static boolean isStep(String path,String fileName) throws IOException {
		List<Integer> stepList=new ArrayList<>();

		File file=new File(path,fileName);
		//BufferedReader是可以按行读取文件
		FileInputStream inputStream = new FileInputStream(file);
		BufferedReader bufferedReader = new BufferedReader(new InputStreamReader(inputStream));

		String lastStr=null;
		String str = null;
		while((str = bufferedReader.readLine()) != null)
		{
			String[] elements=str.split(" ");
			//方向传感器数据
			if(elements[0].equals("step"))
			{
				if(elements[2].indexOf(".")!=-1)
				{
					elements[2]=elements[2].substring(0,elements[2].indexOf("."));
				}
				stepList.add(Integer.valueOf(elements[2]));
			}
		}
		//close
		inputStream.close();
		bufferedReader.close();
		if(stepList==null||stepList.size()==0||stepList.size()==1)return false;
		if(stepList.get(stepList.size()-1)-stepList.get(0)<Constant.STEP_UP_BOUND)return false;
		return true;
	}

	//读取两个时间戳之间的步长以及方向
	public static Step getStepCounterAndAngle(String path,String fileName,String startTimeStamp, String endTimeStamp) throws IOException {
		Step step=new Step();
		List<Integer> stepCounterList=new ArrayList<>();
		List<Double> angleList=new ArrayList<>();


		File file=new File(path,fileName);
		//BufferedReader是可以按行读取文件
		FileInputStream inputStream = new FileInputStream(file);
		BufferedReader bufferedReader = new BufferedReader(new InputStreamReader(inputStream));

		String lastStr=null;
		String str = null;
		while((str = bufferedReader.readLine()) != null)
		{
			String[] elements=str.split(" ");
			//方向传感器数据
			if(elements[0].equals("ori"))
			{
				BigInteger startTime=new BigInteger(startTimeStamp);
				BigInteger endTime=new BigInteger(endTimeStamp);
				BigInteger curTime=new BigInteger(elements[1]);
				if(startTime.compareTo(curTime)<=0&&curTime.compareTo(endTime)<=0)
				{
					angleList.add(Double.parseDouble(elements[2]));
				}
			}
			//计步传感器
			if(elements[0].equals("step"))
			{
				BigInteger startTime=new BigInteger(startTimeStamp);
				BigInteger endTime=new BigInteger(endTimeStamp);
				BigInteger curTime=new BigInteger(elements[1]);
				if(startTime.compareTo(curTime)<=0&&curTime.compareTo(endTime)<=0)
				{
					stepCounterList.add(Integer.parseInt(elements[2]));
				}
			}
		}

		//对于步长传感器数据，只需要计算最后一次与第一次差值即可
		int stepCounter=stepCounterList.get(stepCounterList.size()-1)-stepCounterList.get(0);
		//对于方向传感器信息，暂定计算路径上所有方向角的平均
		double totalAngle=0.0;
		for(Double angle:angleList)
		{
			totalAngle+=angle;
		}
		totalAngle/=angleList.size();

		step.setAngle(totalAngle);
		step.setStepCounter(stepCounter);
		//close
		inputStream.close();
		bufferedReader.close();
		return step;
	}

	/**
	 * 将assets中的识别库复制到SD卡中
	 *
	 * @param path 要存放在SD卡中的 完整的文件名。这里是"/storage/emulated/0//tessdata/chi_sim.traineddata"
	 * @param name assets中的文件名 这里是 "chi_sim.traineddata"
	 */
	public static void assets2SD(Context context, String path, String name) {
		Log.i(TAG, "assets2SD: " + path);
		Log.i(TAG, "assets2SD: " + name);

		//如果存在就删掉
		File f = new File(path);
		if (f.exists()) {
			f.delete();
		}
		if (!f.exists()) {
			File p = new File(f.getParent());
			if (!p.exists()) {
				p.mkdirs();
			}
			try {
				f.createNewFile();
			} catch (IOException e) {
				e.printStackTrace();
			}
		}

		InputStream is = null;
		OutputStream os = null;
		try {
			is = context.getAssets().open(name);
			File file = new File(path);
			os = new FileOutputStream(file);
			byte[] bytes = new byte[2048];
			int len = 0;
			while ((len = is.read(bytes)) != -1) {
				os.write(bytes, 0, len);
			}
			os.flush();
		} catch (IOException e) {
			e.printStackTrace();
		} finally {
			try {
				if (is != null)
					is.close();
				if (os != null)
					os.close();
			} catch (IOException e) {
				e.printStackTrace();
			}
		}

	}

	//删除某个文件夹下所有文件（包括子文件夹及文件）
	public static boolean deleteDir(File dir)
	{
		if(dir.isDirectory())
		{
			String[] children=dir.list();
			for(int i=0;i<children.length;i++)
			{
				boolean isSuccess=deleteDir(new File(dir,children[i]));
				if(!isSuccess)
				{
					return false;
				}
			}
		}
		return dir.delete();
	}
}
