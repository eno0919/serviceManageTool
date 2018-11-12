package com.example.demo.service;
/***
 * 	日志记录类
 * @author Administrator
 *
 */

import java.util.ArrayList;
import java.util.Date;
import java.util.List;

public class RestartLogServiceImpl {
	
	private RestartLogServiceImpl() {}
	
	private static RestartLogServiceImpl log = new RestartLogServiceImpl();
	
	public static RestartLogServiceImpl getInstance() {
		if(log == null)
			log = new RestartLogServiceImpl();
		return log;
	}
	
	public static void writeInfoLog(String info) {
		System.out.println(ConfigManage.logSdf.format(new Date()) + info);
	}
	
	/***
	 * 日志实体类 用于存储服务启停操作 供界面显示
	 * @author Administrator
	 *
	 */
	class LogEntity{
		
		public LogEntity(Integer optType, String optInfo) {
			this.opertionDate = new Date();
			this.opertionDateStr = ConfigManage.logSdf.format(opertionDate);
			this.opertionType = optType;
			this.opertionInfo = optInfo;
		}
		
		private Date opertionDate;//操作时间
		private String opertionDateStr;//操作时间字符串	[yyyy-MM-dd HH:mm:ss.SSS]
		private Integer opertionType;//操作类型 当前分为人工  机器 0代表机器操作 1代表人工操作
		private String opertionInfo;//操作信息 如:启动xxx服务 启动xxx服务成功
		
		public Date getOpertionDate() { return opertionDate; }
		public String getOpertionDateStr() { return opertionDateStr; }
		public Integer getOpertionType() { return opertionType; }
		public String getOpertionInfo() { return opertionInfo; }
		
		@Override
		public String toString() {
			super.toString();
			return this.getOpertionDateStr() + "\t" + (this.getOpertionType() == 0? "机器操作":"人工操作") + "\t" + this.getOpertionInfo();
		}
		
	}
	
	private static List<LogEntity> cache = new ArrayList<>(180);
	
	/***
	 * 获取缓存日志 用于界面显示
	 * @return
	 */
	public List<LogEntity> getCache(){ return cache;}
	
	public void OptionRecordAdd(Integer optType, String optInfo) {
		LogEntity logEn = new LogEntity(optType, optInfo);
		int delNum = cache.size() - 180;
		if(delNum > 0) {
			for(int i = 0; i < delNum; i++)
				cache.remove(i);	
		}
		//此处存入文件
		RestartLogFileWrite.getInstance().write(logEn);
		cache.add(logEn);
	}

}
