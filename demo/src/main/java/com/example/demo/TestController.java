package com.example.demo;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.ResponseBody;

import com.example.demo.service.ConfigManage;
import com.example.demo.service.RestartServiceImpl;
import com.example.demo.service.RestartTimer;


@Controller
public class TestController {

	@RequestMapping("/")
	public String home(){
		return "ctrlService";
	}
	
	@RequestMapping("/query")
	@ResponseBody
	public Object query(){
		return RestartServiceImpl.getInstace().queryStateList();
	}
	
	@RequestMapping("/close")
	@ResponseBody
	public Object closeService(HttpServletRequest request, HttpServletResponse response){
		String[] services = request.getParameterValues("services");
		if(services != null && services.length > 0) {
			//关闭传入的服务
			RestartServiceImpl.getInstace().closeService(services);
			return true;
		}
		return null;
	}
	
	@RequestMapping("/start")
	@ResponseBody
	public Object startService(HttpServletRequest request, HttpServletResponse response){
		String[] services = request.getParameterValues("services");
		if(services != null && services.length > 0) {
			//开启传入的服务
			RestartServiceImpl.getInstace().openService(services);
			return true;
		}
		return null;
	}
	
	@RequestMapping("/restart")
	@ResponseBody
	public Object restartService(HttpServletRequest request, HttpServletResponse response) {
		String[] services = request.getParameterValues("services");
		if(services != null && services.length > 0) {
			//重启传入的服务
			RestartServiceImpl.getInstace().restartService(services);
			return true;
		}
		return null;
	}
	
//	@RequestMapping("/reload")
//	@ResponseBody
//	public Object reload() {
//		//加载配置文件
//		ConfigManage.getInstance().loadConfig();
//		//启动定时服务检测
//		RestartTimer.run();
//		return true;
//	}
	
}


