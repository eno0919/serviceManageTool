@ECHO OFF
CD /d %~dp0
TITLE 卸载删除mysql
COLOR 0A
RD "%WinDir%\system32\rainss_perm" >NUL 2>NUL
MD "%WinDir%\System32\rainss_perm" 2>NUL||(ECHO 请使用右键管理员身份运行！&&PAUSE >NUL&&EXIT)
RD "%WinDir%\System32\rainss_perm" 2>NUL
SET /p rains=此操作会卸载mysql服务并删除mysql所有数据,是否继续？(Y/N):
IF "%rains%" == "Y" (GOTO uninsdel) ELSE (IF "%rains%" == "y" (GOTO uninsdel) ELSE ( GOTO quitcommand))

:uninsdel
NET stop mysql
ECHO mysql服务已停止
ECHO=
SC delete mysql
ECHO mysql服务已从注册表删除
ECHO=
RMDIR /s /q mysql-5.7.24-winx64
ECHO mysql数据库文件夹已删除
ECHO=
GOTO processOver

:quitcommand
ECHO 操作取消
GOTO processOver

:processOver
ECHO=
ECHO 按任意键退出......
pause>nul