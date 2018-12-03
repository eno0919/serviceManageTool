package com.example.demo.service;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;

import com.alibaba.fastjson.JSONArray;
import com.alibaba.fastjson.JSONObject;

/***
 * 启动 关闭 重启 查看服务状态 实现类
 * 
 * 以下三个方法属于控制器调用 如果是人为操作 请调用这三个接口 
 *	restartService(String[] services)
 *	closeService(String[] services) 
 *	openService(String[] services)
 * @author Administrator
 *
 */
public class RestartServiceImpl {
	
	//用于记录服务的操作状态 0代表机器操作 1代表人工操作
	protected static Map<String, Integer> serviceOpt = new HashMap<>();
	
	private RestartServiceImpl() {};
	
	private static RestartServiceImpl service = new RestartServiceImpl();
	
	public static RestartServiceImpl getInstace() {
		if(service == null) {
			service = new RestartServiceImpl();
		}
		return service;
	}
	
	private final static String jarRunSetPrefix = "jps@";//jar运行服务config.json中配置的前缀 配置如:jps@xxxx.jar

	/***
	 * 	查询状态list
	 * @return
	 */
	public Map<String, Object> queryStateList(){
		//返回的结果集
		Map<String, Object> result = new HashMap<>();
		//返回结果集里面的data元素,代表表格数据
		List<PidStateInfo> data = new ArrayList<>();
		result.put("data", data);
		//循环获取服务的状态
		for(Entry<String, JSONObject> entry : ConfigManage.getServiceMap().entrySet()) {
			String pidToName = entry.getKey();
			Integer pid = queryPid(pidToName);
			Integer runState = queryServiceState(pidToName);
			PidStateInfo obj = new PidStateInfo(pid, pidToName, entry.getValue().getString("viewShowName"), runState);
			data.add(obj);
		}
		result.put("log", RestartLogServiceImpl.getInstance().getCache());
		return result;
	}
	
	/***
	 *	 查询服务状态
	 *	0:未启动	1:运行当中	2:运行当中 存在部分端口未开启	3:端口检测异常
	 * @param pidToName
	 * @return
	 */
	protected Integer queryServiceState(String pidToName) {
		Integer pid = queryPid(pidToName);
		Integer runState = 0;//未启动
		if(pid != null) {
			Integer[] sportAry;
			try {
				sportAry = checkPort(pidToName);
				if(sportAry == null || sportAry.length == 0) {
					runState = 1;//启动成功
				}else {
					runState = 2;//执行中... 存在关闭所有时查看所有情况 进程还在端口已关闭
				}
				if(runState == 1 && pidToName.indexOf(jarRunSetPrefix) == 0) {
					//如果配置的为jar服务并且启动成功 还需检查校验http
					String httpCheckURL = ConfigManage.getServiceMap().get(pidToName).getString("httpCheckURL");
					String httpResponse = ConfigManage.getServiceMap().get(pidToName).getString("httpResponse");
					if(httpCheckURL != null && httpResponse != null && !httpCheckURL.isEmpty() && !httpResponse.isEmpty()) {
						String result = HttpClientService.doGet(httpCheckURL);
						if(httpResponse.equals(result)) {
							runState = 1;
						}else {
							runState = 2;
							RestartLogServiceImpl.writeInfoLog(pidToName + "服务配置httpResponse与实际响应有误,当前" + httpCheckURL + "响应为:" + result);
						}
					}
				}
			} catch (Exception e) {
				RestartLogServiceImpl.writeInfoLog("服务" + pidToName + "端口检测发生异常.");
				e.printStackTrace();
				runState = 3;//端口检测异常
			}
		}
		return runState;
	}
	
	/***
	 * 开启服务 控制器里面调用 设置状态为人为操作
	 * @param services
	 */
	public void openService(String[] services) {
		for(int i = 0; i < services.length; i++) {
			JSONObject serviceProperty = ConfigManage.getServiceMap().get(services[i]);
			if(serviceProperty != null) {
				serviceOpt.put(services[i], 1);
				openService(services[i]);
				Boolean isWaitStartSucc = serviceProperty.getBoolean("isWaitStartSucc");
				isWaitStartSucc = (isWaitStartSucc == null) ? false : isWaitStartSucc;
				if(isWaitStartSucc) {
					//while(queryPid(services[i]) == null) {
					while(queryServiceState(services[i]) != 1) {
						try {
							Thread.sleep(1000);
						} catch (InterruptedException e) {
							e.printStackTrace();
						}
					}
				}
				serviceOpt.put(services[i], 0);
			}
		}
	}
	
	/***
	 * 	开启服务
	 * @param pidToName
	 * @return -1:未找到配置服务	0:启动失败	1:启动成功	2:启动中...
	 */
	public Integer openService(String pidToName) {
		if(queryPid(pidToName) != null) return null;//如果PID不等于null,说明进程存在,不进行启动服务操作 
		Integer state = 0;
		JSONObject serviceProperty = ConfigManage.getServiceMap().get(pidToName);
		if(serviceProperty == null) { 
			state = -1;//通过传入的参数未找到相应的配置信息
		}
		String startFile = serviceProperty.getString("startFile");
		String startCommand = serviceProperty.getString("startCommand");
		if(startCommand == null || startCommand.trim().isEmpty()) startCommand = startFile;
		try {
			RestartLogServiceImpl.getInstance().OptionRecordAdd(serviceOpt.get(pidToName), "开启" + pidToName + "服务");
			Runtime.getRuntime().exec(startCommand);
			RestartLogServiceImpl.writeInfoLog("执行了开启服务:" + startCommand);
			Integer startTimeLength = serviceProperty.getInteger("startTimeLength");
			Thread.sleep(startTimeLength==null ? 1000 : startTimeLength*1000);
			RestartLogServiceImpl.writeInfoLog("开始检查" + pidToName + "服务启动后端口开放情况.");
			Integer[] sportAry = checkPort(pidToName);
			if(sportAry != null) RestartLogServiceImpl.writeInfoLog("端口还未开放数量为:" + sportAry.length);
			if(sportAry != null && sportAry.length > 0) {
				state = 2;//启动中
			}else {
				state = 1;//启动成功
			}
			if(state == 1 && pidToName.indexOf(jarRunSetPrefix) == 0) {
				//如果配置的为jar服务并且启动成功 还需检查校验http
				String httpCheckURL = serviceProperty.getString("httpCheckURL");
				String httpResponse = serviceProperty.getString("httpResponse");
				if(httpCheckURL != null && httpResponse != null && !httpCheckURL.isEmpty() && !httpResponse.isEmpty()) {
					RestartLogServiceImpl.writeInfoLog("进行http请求方式查看" + pidToName + "活跃状态:" + httpCheckURL);
					String result = HttpClientService.doGet(httpCheckURL);
					RestartLogServiceImpl.writeInfoLog(httpCheckURL + "响应结果为:" + result);
					if(httpResponse.equals(result)) {
						state = 1;
					}else {
						state = 2;
						RestartLogServiceImpl.writeInfoLog(pidToName + "服务配置httpResponse与实际响应有误,当前" + httpCheckURL + "响应为:" + result);
					}
				}
			}
			if(state == 2) {				
				RestartLogServiceImpl.writeInfoLog("服务" + pidToName + "正在启动当中.");
				RestartLogServiceImpl.getInstance().OptionRecordAdd(serviceOpt.get(pidToName), pidToName + "服务正在启动当中");
			}
			else if(state == 1) {
				RestartLogServiceImpl.writeInfoLog("服务" + pidToName + "启动成功.");
				RestartLogServiceImpl.getInstance().OptionRecordAdd(serviceOpt.get(pidToName), pidToName + "服务启动成功");
			}
		} catch (Exception e) {
			RestartLogServiceImpl.writeInfoLog("启动服务" + pidToName + "发生异常.");
			e.printStackTrace();
			state = 0;//启动失败
			RestartLogServiceImpl.getInstance().OptionRecordAdd(serviceOpt.get(pidToName), pidToName + "服务启动失败");
		}
		Boolean isWaitStartSucc = serviceProperty.getBoolean("isWaitStartSucc");
		isWaitStartSucc = (isWaitStartSucc == null) ? false : isWaitStartSucc;
		if(isWaitStartSucc && state == 2) {
			RestartLogServiceImpl.writeInfoLog("进入等待服务" + pidToName + "启动成功后继续后面执行");
			while(queryServiceState(pidToName) != 1) {
				try {
					Thread.sleep(1000);
				} catch (InterruptedException e) {
					e.printStackTrace();
				}
			}
			state = 1;
			RestartLogServiceImpl.writeInfoLog("服务" + pidToName + "启动成功.");
		}
		RestartTimer.addTimer(pidToName);
		return state;
	}

	/***
	 * 检查端口 用于启动服务后进行端口检测
	 * @param pidToName 镜像名称
	 * @return 是否镜像名称对应配置的端口都已开启
	 * 	return null 代表未配置端口,不作端口检查
	 * 	return Integer[] 存在未开启的端口
	 * 	return [] 配置端口都已开启
	 * @throws Exception 
	 */
	private Integer[] checkPort(String pidToName) throws Exception {
		JSONObject jsonObj = ConfigManage.getServiceMap().get(pidToName);
		JSONArray serverPort = (JSONArray)jsonObj.get("servicePort");
		if(serverPort == null || serverPort.size() == 0) {//如果未配置端口 默认找到进程就代表服务启动
			return null;
		}else {
			List<Integer> notOpenPort = new ArrayList<>();
			try {
				for(int i = 0; i < serverPort.size(); i++) {
					Integer port = serverPort.getInteger(i);
					//Process p = Runtime.getRuntime().exec("NETSTAT -ANO|FINDSTR " + port); 此命令java直接调用执行会失败 所以采用调用文件方式
					Process p = Runtime.getRuntime().exec(ConfigManage.userScriptDir + "checkPort.bat " + port);
					BufferedReader bReader=new BufferedReader(new InputStreamReader(p.getInputStream(),"gbk"));
					String line=null;
					int t = 0;
					boolean openState = false;
					while((line=bReader.readLine())!=null) {
						if(t++ > 1 && line.indexOf(":"+port) != -1) {//如果端口查询有记录 则检查其他端口
							openState = true;
							break;
						}
				    }
					p.destroy();
					if(openState == false) notOpenPort.add(port);
				}
				return (Integer[]) notOpenPort.toArray(new Integer[notOpenPort.size()]);
			} catch (Exception e) {
				RestartLogServiceImpl.writeInfoLog("服务" + pidToName + "检查端口发生异常.");
				e.printStackTrace();
				throw new Exception("启动服务检查端口发生异常");
			}
		}
	}
	
	/***
	 * 关闭服务 控制器中调用 设置操作为人工方式
	 * @param services
	 */
	public void closeService(String[] services) {
		for(int i = services.length - 1; i >= 0; i--) {
			if(ConfigManage.getServiceMap().get(services[i]) != null) {
				serviceOpt.put(services[i], 1);
				closeService(services[i]);
				serviceOpt.put(services[i], 0);
			}
		}
	}
	
	/***
	 * 	关闭服务
	 * @param pidToName
	 * @return
	 */
	public Boolean closeService(String pidToName) {
		Boolean state = null;
		JSONObject serviceProperty = ConfigManage.getServiceMap().get(pidToName);
		if(serviceProperty == null) {
			state = null;//通过传入的参数未找到相应的配置信息
		}else {
			RestartTimer.quitTimer(pidToName);
			String stopFile = serviceProperty.getString("stopFile");
			String stopCommand = serviceProperty.getString("stopCommand");
			try {  
				RestartLogServiceImpl.getInstance().OptionRecordAdd(serviceOpt.get(pidToName), "关闭" + pidToName + "服务");
				if(stopCommand != null && !stopCommand.trim().isEmpty()) {
					Runtime.getRuntime().exec(stopCommand);
				}else if(stopFile != null && !stopFile.trim().isEmpty()) {
					Runtime.getRuntime().exec(stopFile);
				}else if(pidToName.indexOf(jarRunSetPrefix) == 0) {
					//如果是jar运行服务
					Integer pid = queryPid(pidToName);
					if(pid != null) {						
						String command = "TASKKILL /F /T /PID " + pid;
						Runtime.getRuntime().exec(command);
					}
				}else {				
					String command = "TASKKILL /F /T /FI \"IMAGENAME EQ " + pidToName + "\"";
					Runtime.getRuntime().exec(command);
				}
				while(queryPid(pidToName) != null) {
					try {
						Thread.sleep(1000);
					} catch (InterruptedException e) {
						RestartLogServiceImpl.writeInfoLog("服务" + pidToName + "关闭过程中sleep发生异常.");
						e.printStackTrace();
					}
				}
				state = true;
				RestartLogServiceImpl.getInstance().OptionRecordAdd(serviceOpt.get(pidToName), pidToName + "服务关闭成功");
			} catch (IOException e) {
				RestartLogServiceImpl.writeInfoLog("服务" + pidToName + "关闭发生异常.");
				e.printStackTrace();
				state = false;
				RestartLogServiceImpl.getInstance().OptionRecordAdd(serviceOpt.get(pidToName), pidToName + "服务关闭失败");
			}
		}
		return state;
	}
	
	/***
	 * 	重启服务 重启服务时是一个接着一个重启的 要确定进程不存在后才进行启动服务 控制器中调用 设置操作方式为人工
	 * @param services
	 */
	public void restartService(String[] services) {
		closeService(services);
		openService(services);
	}
	
	/***
	 * 	通过映像名称查询对应的PID
	 * @param pidToName
	 * @return
	 */
	protected Integer queryPid(String pidToName) {
		Integer pid = null;
		JSONObject serviceObj = ConfigManage.getServiceMap().get(pidToName);
		if(serviceObj != null) {
			if(pidToName.indexOf(jarRunSetPrefix) == 0) {
				//JAVA的自己服务使用jps@做为前缀配置pidToName
				pid = jpsQueryPid(pidToName);
			}else {
				try {
					String command = "TASKLIST /FI \"IMAGENAME EQ "+ pidToName +"\"";
					Process  p = Runtime.getRuntime().exec(command);
					BufferedReader bReader=new BufferedReader(new InputStreamReader(p.getInputStream(),"gbk"));
					String line=null;
					int i = 0;
					while((line=bReader.readLine())!=null) {
						if(i++ == 3) {
							String trimLine = line.trim();
							String isoName = trimLine.substring(0, trimLine.indexOf(" "));
							String tmp = trimLine.substring(isoName.length()).trim();
							pid = Integer.valueOf(tmp.substring(0, tmp.indexOf(" ")));
							break;
				    	}
				    }    
				} catch (IOException e) {
					RestartLogServiceImpl.writeInfoLog("查询" + pidToName + "服务进程时发生异常.");
					e.printStackTrace();
				}
			}
		}
		return pid;
	}


	private Integer jpsQueryPid(String pidToName) {
		Integer pid = null;
		String[] param = pidToName.split("@");
		String command = param[0];
		String jarName = param[1];
		try {
			Process  p = Runtime.getRuntime().exec(command);
			BufferedReader bReader=new BufferedReader(new InputStreamReader(p.getInputStream(),"utf8"));
			String line=null;
			while((line=bReader.readLine())!=null) {
				if(!line.trim().isEmpty()) {
					String[] recordAry = line.split(" ");
					if(recordAry[recordAry.length - 1].equals(jarName)) {
						pid = Integer.valueOf(recordAry[0]);
					}
				}
		    }  
		} catch (Exception e) {
			RestartLogServiceImpl.writeInfoLog("服务" + pidToName + "查询PID发生异常.");
			e.printStackTrace();
		}
		return pid;
	}


	/***
	 * 	状态实体
	 * @author Administrator
	 *
	 */
	private class PidStateInfo {
		public PidStateInfo(Integer pid, String runName, String runAliasName,Integer runState) {
			this.pid = pid;
			this.runName = runName;
			this.runAliasName = runAliasName;
			this.runState = runState;
			this.checkDate = new Date();
			this.checkDateStr = ConfigManage.logSdf.format(this.checkDate);
		}
		
		private Integer pid;
		private String runName;
		private String runAliasName;
		private Integer runState;
		private Date checkDate;
		private String checkDateStr;
		
		public Integer getPid() { return this.pid; }
		public String getRunName() {return this.runName; }
		public String getRunAliasName() {return this.runAliasName;}
		public Integer getRunState() {return this.runState; }
		public Date getCheckDate() { return this.checkDate; }
		public String getCheckDateStr() { return this.checkDateStr;}
	}

}
 
