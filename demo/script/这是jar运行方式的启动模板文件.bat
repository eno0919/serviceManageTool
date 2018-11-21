::所在盘符
SET TMP_PATH=C: 
::所在文件夹
SET TMP_DIR=C:\Users\Administrator\Desktop\TEST\ServiceManageTools
%TMP_PATH% 
cd %TMP_DIR% 
::START "程序名<建议使用jar包名,输入英文>" CMD /C java -jar 文件所在全路径
::例:START "web-station-0.0.1-SNAPSHOT" CMD /C java -jar C:\Users\Administrator\Desktop\TEST\ServiceManageTools\web-station-0.0.1-SNAPSHOT.jar