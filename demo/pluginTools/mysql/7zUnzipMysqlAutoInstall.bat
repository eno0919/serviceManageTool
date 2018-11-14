@ECHO OFF
CD /d %~dp0
TITLE 使用7z自动化解压安装mysql
COLOR 0A
RD "%WinDir%\system32\rainss_perm" >NUL 2>NUL
MD "%WinDir%\System32\rainss_perm" 2>NUL||(ECHO 请使用右键管理员身份运行！&&PAUSE >NUL&&EXIT)
RD "%WinDir%\System32\rainss_perm" 2>NUL
COPY /y 7z.dll "%WinDir%\System32" >NUL
COPY /y 7z.exe "%WinDir%\System32" >NUL
ECHO 解压mysql-5.7.24-winx64.zip:
7z x -tzip -y mysql-5.7.24-winx64.zip
CD mysql-5.7.24-winx64
ECHO 进入到mysql-5.7.24-winx64
set currentDir=%cd%
ECHO 开始写入配置文件my.ini
(
ECHO [mysqld]
ECHO port = 3306
ECHO basedir = %currentDir%
ECHO datadir = %currentDir%\data
ECHO sql_mode=NO_ENGINE_SUBSTITUTION,STRICT_TRANS_TABLES
)>my.ini
ECHO 配置文件my.ini写入完成
cd bin
ECHO 注册MYSQL服务
mysqld install mysql
mysqld --initialize-insecure
mysqld install
ECHO 开始启动MySQL
net start mysql
ECHO 设置root密码为root
mysql -u root -e "use mysql;update user set authentication_string = password('root'), password_expired = 'N', password_last_changed = now() where user = 'root';flush privileges;"
ECHO 安装脚本处理完毕
ECHO 按任意键退出......
PAUSE>NUL