package com.example.demo;

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
	}
}
