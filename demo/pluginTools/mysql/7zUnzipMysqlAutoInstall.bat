@ECHO OFF
CD /d %~dp0
TITLE ʹ��7z�Զ�����ѹ��װmysql
COLOR 0A
RD "%WinDir%\system32\rainss_perm" >NUL 2>NUL
MD "%WinDir%\System32\rainss_perm" 2>NUL||(ECHO ��ʹ���Ҽ�����Ա������У�&&PAUSE >NUL&&EXIT)
RD "%WinDir%\System32\rainss_perm" 2>NUL
COPY /y 7z.dll "%WinDir%\System32" >NUL
COPY /y 7z.exe "%WinDir%\System32" >NUL
ECHO ��ѹmysql-5.7.24-winx64.zip:
7z x -tzip -y mysql-5.7.24-winx64.zip
CD mysql-5.7.24-winx64
ECHO ���뵽mysql-5.7.24-winx64
set currentDir=%cd%
ECHO ��ʼд�������ļ�my.ini
(
ECHO [mysqld]
ECHO port = 3306
ECHO basedir = %currentDir%
ECHO datadir = %currentDir%\data
ECHO sql_mode=NO_ENGINE_SUBSTITUTION,STRICT_TRANS_TABLES
)>my.ini
ECHO �����ļ�my.iniд�����
cd bin
ECHO ע��MYSQL����
mysqld install mysql
mysqld --initialize-insecure
mysqld install
ECHO ��ʼ����MySQL
net start mysql
ECHO ����root����Ϊroot
mysql -u root -e "use mysql;update user set authentication_string = password('root'), password_expired = 'N', password_last_changed = now() where user = 'root';flush privileges;"
ECHO ��װ�ű��������
ECHO ��������˳�......
PAUSE>NUL