package com.example.demo.service;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.MalformedURLException;
import java.net.URL;
/***
 *  这个类用于部分配置http验证活跃的服务方式
 *  代码借鉴与:https://www.cnblogs.com/hhhshct/p/8523697.html 
 * @author Administrator
 *
 */
public class HttpClientService {
	
	protected static int connectTimeOut = ConfigManage.getConnectTimeOut() * 1000;
	protected static int readTimeOut = ConfigManage.getReadTimeOut() * 1000;
	
	private HttpClientService() {}
	
	private static HttpClientService hcs = new HttpClientService();
	
	public HttpClientService getInstance() {
		if(hcs == null)
			hcs = new HttpClientService();
		return hcs;
	}
	
	public static String doGet(String httpurl) {
        HttpURLConnection connection = null;
        InputStream is = null;
        BufferedReader br = null;
        String result = null;// 返回结果字符串
        try {
            // 创建远程url连接对象
            URL url = new URL(httpurl);
            // 通过远程url连接对象打开一个连接，强转成httpURLConnection类
            connection = (HttpURLConnection) url.openConnection();
            // 设置连接方式：get
            connection.setRequestMethod("GET");
            // 设置连接主机服务器的超时时间：15000毫秒
            connection.setConnectTimeout(connectTimeOut);
            // 设置读取远程返回的数据时间：60000毫秒
            connection.setReadTimeout(readTimeOut);
            // 发送请求
            connection.connect();
            // 通过connection连接，获取输入流
            if (connection.getResponseCode() == 200) {
                is = connection.getInputStream();
                // 封装输入流is，并指定字符集
                br = new BufferedReader(new InputStreamReader(is, "UTF-8"));
                // 存放数据
                StringBuffer sbf = new StringBuffer();
                String temp = null;
                while ((temp = br.readLine()) != null) {
                    sbf.append(temp);
                    //sbf.append("\r\n");
                }
                result = sbf.toString();
            }
        } catch (MalformedURLException e) {
            //e.printStackTrace();
        	RestartLogServiceImpl.writeInfoLog("错误的 URL或者找不到任何合法协议或者无法解析字符串");
        } catch (IOException e) {
            //e.printStackTrace();
        	RestartLogServiceImpl.writeInfoLog("遭到拒绝远程连接,没有任何进程在远程地址/端口上进行侦听");
        } finally {
            // 关闭资源
            if (null != br) {
                try {
                    br.close();
                } catch (IOException e) {
                    e.printStackTrace();
                }
            }

            if (null != is) {
                try {
                    is.close();
                } catch (IOException e) {
                    e.printStackTrace();
                }
            }

            connection.disconnect();// 关闭远程连接
        }

        return result;
    }

}
