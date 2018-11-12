package com.example.demo.service;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Timer;
import java.util.TimerTask;

import com.alibaba.fastjson.JSONObject;

/***
 *  服务检查类 用于服务挂掉后自己再次启动 
 *  如果人工关闭后 停止定时检查启动
 *  如果人工再次开启 再次进入定时检查启动
 * @author Administrator
 *
 */
public class RestartTimer {
	
	private static Map<String, Integer> tryStartTimeLog = new HashMap<>();  
	
	private static Map<String, Timer> timerMap = new HashMap<>(); 
	
	/***
	 *  取消定时任务 用于人工手动停止服务后取消
	 * @param service
	 */
	protected static void quitTimer(String service) {
		Timer timer = timerMap.get(service);
		if(timer != null) {			
			timer.cancel();
			timer = null;
			timerMap.remove(service);
		}
	}
	
	/***
	 *  添加定时服务 用于手动启动服务后注册
	 * @param service
	 */
	protected static void addTimer(String service) {
		quitTimer(service);
		TimerTask task = createTimerTask(service);
		registTimer(service, task);
	}
	
	private static TimerTask createTimerTask(String serviceName) {
		TimerTask result = new TimerTask() {
			
			@Override
			public void run() {
				try {
					if(RestartServiceImpl.getInstace().queryPid(serviceName) != null) {
						//说明服务进程存在
						tryStartTimeLog.put(serviceName, 0);
					}else {
						Integer num = tryStartTimeLog.get(serviceName);
						if(num == null) num = 0;
						if(num != 0 && num == ConfigManage.getTryStartMaxTime()) {
							//说明已经尝试过最大次数了 放弃自动尝试启动方式
							return ;
						}else {						
							RestartServiceImpl.getInstace().openService(serviceName);
							tryStartTimeLog.put(serviceName, ++num);
						}
					}
				} catch (Exception e) {
					RestartLogServiceImpl.writeInfoLog("定时检查进程["+serviceName+"服务]任务发生异常.");
					e.printStackTrace();
				}
			}
		};
		return result;
	}
	
	private static void registTimer(String service, TimerTask task) {
		Timer timer = new Timer();
		timer.schedule(task, ConfigManage.getCheckServiceRateSeconds() * 1000, ConfigManage.getCheckServiceRateSeconds() * 1000);
		timerMap.put(service, timer);
	}
	
	/***
	 * 程序包启动时开始进行检测服务
	 */
	public static void run() {
		RestartLogServiceImpl.writeInfoLog("服务定时检测任务正在启动中......");
		RestartServiceImpl rsi = RestartServiceImpl.getInstace();
		List<String> serviceList = ConfigManage.getServiceImageNameList();
		Map<String, JSONObject> serviceInfoMap = ConfigManage.getServiceMap();
		for(int i = 0; i < serviceList.size(); i++) {
			String serviceName = serviceList.get(i);
			RestartLogServiceImpl.writeInfoLog("加载" + serviceName + "服务......");
			JSONObject serviceInfo = serviceInfoMap.get(serviceName);
			rsi.openService(serviceName);
			Boolean isWaitStartSucc = serviceInfo.getBoolean("isWaitStartSucc");
			isWaitStartSucc = (isWaitStartSucc == null) ? false : isWaitStartSucc;
			if(isWaitStartSucc) {
				//while(rsi.queryPid(serviceName) == null) {
				while(rsi.queryServiceState(serviceName) != 1) {
					try {
						Thread.sleep(1000);
					} catch (InterruptedException e) {
						e.printStackTrace();
					}
				}
			}
		}
		RestartLogServiceImpl.writeInfoLog("服务定时检测任务注册启动完毕.");
	}

}
