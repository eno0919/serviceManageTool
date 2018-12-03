package com.example.demo;

import java.io.File;
import java.io.IOException;
import java.util.UUID;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseBody;
import org.springframework.web.multipart.MultipartFile;

import com.example.demo.service.RestartServiceImpl;


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
	
	@RequestMapping("/test")
	public Object reload() {
		return "test";
	}
	
	@RequestMapping("/upload")
	@ResponseBody
	public String getfile(@RequestParam("myfile") MultipartFile file){
        System.out.println("file name = "+file.getOriginalFilename());
 
        // 获取文件名
        String fileName = file.getOriginalFilename();
        // 获取后缀
        String suffixName = fileName.substring(fileName.lastIndexOf("."));
        // 文件上产的路径
        String filePath = "d:/upload/";
        // fileName处理
        fileName = filePath+ UUID.randomUUID()+fileName;
        // 文件对象
        File dest = new File(fileName);
        // 创建路径
        if(!dest.getParentFile().exists()){
            dest.getParentFile().mkdir();
        }
 
        try {
            //保存文件
            file.transferTo(dest);
            return "上传成功";
        } catch (IOException e) {
            e.printStackTrace();
        }
 
        return "上传失败";
    }
}


