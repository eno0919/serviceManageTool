package com.example.demo.service;

import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.io.PrintWriter;
import java.text.SimpleDateFormat;
import java.util.Date;


/***
 * 日志文件输出 输出地址为当前程序目录 产生一个log文件 按日期进行创建文件并输出
 * @author Administrator
 *
 */
public class RestartLogFileWrite {
	
	private SimpleDateFormat sd = ConfigManage.sdf;
	
	private RestartLogFileWrite() {}
	
	private static String tmpFileName = "";
	
	public static RestartLogFileWrite logFileWrite = new RestartLogFileWrite();
	
	public static RestartLogFileWrite getInstance() {
		if(logFileWrite == null)
			logFileWrite = new RestartLogFileWrite();
		return logFileWrite;
	}
	
	private File getLogFile() {
		String fileName = sd.format(new Date()) + "-startService.log";
		if(tmpFileName.indexOf(fileName) != -1) {
			return new File(tmpFileName);
		}else {
			File logFileDir = new File(ConfigManage.userLogDir);
			if(!logFileDir.exists()) 
				logFileDir.mkdirs();
			String logFilePath = ConfigManage.userLogDir + fileName;
			File logFile = new File(logFilePath);
			if(!logFile.exists()) {
				try {
					logFile.createNewFile();
					tmpFileName = logFilePath;
				} catch (IOException e) {
					RestartLogServiceImpl.writeInfoLog("创建日志文件" + logFilePath + "发生异常.");
					e.printStackTrace();
				}
			}
			return logFile;
		}
	}
	
	protected void write(RestartLogServiceImpl.LogEntity log) {
		File logFile = getLogFile();
		if(logFile != null) {
			try {
				FileWriter fw = new FileWriter(logFile, true);
				PrintWriter pw = new PrintWriter(fw);
				pw.println(log.toString());
				pw.flush();
				fw.flush();
				pw.close();
				fw.close();
			} catch (Exception e) {
				RestartLogServiceImpl.writeInfoLog("输出到日志文件"+tmpFileName+"发生错误.");
				e.printStackTrace();
			}
		}else {
			RestartLogServiceImpl.writeInfoLog("获取不到当前日志文件.");
		}
	}

}
