package com.example.demo;

import java.io.File;
import java.io.IOException;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;

import com.example.demo.service.ConfigManage;
import com.example.demo.service.RestartTimer;

@SpringBootApplication
public class DemoApplication {

	public static void main(String[] args) {
		SpringApplication.run(DemoApplication.class, args);
		//加载配置文件
		ConfigManage.getInstance().loadConfig();
		//启动定时服务检测
		RestartTimer.run();
		System.out.println("可通过浏览器访问http://127.0.0.1:41180/查看或控制当前配置的服务状态,如果修改了端口请使用修改的端口访问");
		try {
			String startIeVisit = ConfigManage.userDir + File.separator + "script" + File.separator + "startIexplore.bat"; 
			Runtime.getRuntime().exec(startIeVisit);
		} catch (IOException e) {
			e.printStackTrace();
		}
	}
}
