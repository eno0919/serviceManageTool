package com.example.demo.service;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStreamReader;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

import com.alibaba.fastjson.JSONArray;
import com.alibaba.fastjson.JSONObject;

public class ConfigManage {
	
	private ConfigManage() {}
	
	private static ConfigManage pm = new ConfigManage();
	
	public static ConfigManage getInstance() {
		if(pm == null)
			pm = new ConfigManage();
		return pm;
	}
	
	public final static SimpleDateFormat logSdf = new SimpleDateFormat("[yyyy-MM-dd HH:mm:ss.SSS]");
	
	public final static SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd");
	
	public final static String userDir = System.getProperty("user.dir");
	
	public final static String userLogDir = userDir + File.separator + "log" + File.separator;
	
	public final static String userScriptDir = userDir + File.separator + "script" + File.separator;

	//检测服务频率 单位秒（默认90秒）
	private static Integer checkServiceRateSeconds = 90;
	public static Integer getCheckServiceRateSeconds() { return checkServiceRateSeconds; }
	
	//尝试启动最大次数 超过后不再进行尝试重启 默认为0 会一直尝试重启
	private static Integer tryStartMaxTime = 0;
	public static Integer getTryStartMaxTime() { return tryStartMaxTime; }
	
	//http检查服务活跃连接主机服务器的超时时间 默认15秒
	private static Integer connectTimeOut = 15;
	public static Integer getConnectTimeOut() { return connectTimeOut; }
	
	//http检查服务读取远程返回的数据超时时间	默认60秒
	private static Integer readTimeOut = 60;
	public static Integer getReadTimeOut() { return readTimeOut; }
	
	//用于存储配置的服务进程名称
	private static List<String> serviceImageNameList = new ArrayList<>();
	public static List<String> getServiceImageNameList(){ return serviceImageNameList; }
	
	//用于存储服务进程名对应的服务配置信息
	private static Map<String, JSONObject> serviceMap = new LinkedHashMap<String, JSONObject>();
	public static Map<String, JSONObject> getServiceMap(){ return serviceMap; }
	
	/***
	 * 加载配置
	 */
	public void loadConfig() {
		String path = userDir + File.separator + "config.json";
		RestartLogServiceImpl.writeInfoLog("开始准备加载config.json配置信息......");
		try {
			String fileContext = readFile(path);
			if(!fileContext.equals("{}")) {				
				JSONObject config = JSONObject.parseObject(fileContext);
				setPublicConfig(config);
				setServiceConfig(config.getJSONArray("services"));
			}
		} catch (Exception e) {
			RestartLogServiceImpl.writeInfoLog("配置文件内容有误或加载时发生异常.");
			e.printStackTrace();
		}
	}
	
	/***
	 * 设置服务配置信息
	 * @param services
	 */
	private void setServiceConfig(JSONArray services) {
		serviceImageNameList.clear();
		for(int i = 0; i < services.size(); i++) {
			JSONObject jsonObj = (JSONObject) services.get(i);
			String pidToName = jsonObj.getString("pidToName");
			if(pidToName != null && !pidToName.isEmpty()) {
				serviceMap.put(pidToName, jsonObj);
				serviceImageNameList.add(pidToName);
				RestartServiceImpl.serviceOpt.put(pidToName, 0);
			}
		}
	}

	/***
	 * 设置公共配置属性
	 * @param config
	 */
	private void setPublicConfig(JSONObject config) {
		checkServiceRateSeconds = config.getInteger("checkServiceRateSeconds");
		tryStartMaxTime = config.getInteger("tryStartMaxTime");
		connectTimeOut = config.getInteger("connectTimeOut");
		readTimeOut = config.getInteger("readTimeOut");
	}

	private String readFile(String path) throws IOException {
		File file = new File(path);
		if(file.exists()) {
			BufferedReader buffReader = new BufferedReader(new InputStreamReader(new FileInputStream(file), "utf-8"));
			StringBuilder sd = new StringBuilder();
			String line = null;
			String winUserDir = userDir.replaceAll("\\\\", "\\\\\\\\");
			while((line = buffReader.readLine()) != null)
				sd.append(line.replace("${currentDir}", winUserDir).trim());
			buffReader.close();
			return sd.toString();
		}else {
			RestartLogServiceImpl.writeInfoLog(path + "配置文件未找到.");
			return "{}";
		}
	}
	
}
