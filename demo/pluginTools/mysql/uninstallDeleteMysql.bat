@ECHO OFF
CD /d %~dp0
TITLE ж��ɾ��mysql
COLOR 0A
RD "%WinDir%\system32\rainss_perm" >NUL 2>NUL
MD "%WinDir%\System32\rainss_perm" 2>NUL||(ECHO ��ʹ���Ҽ�����Ա������У�&&PAUSE >NUL&&EXIT)
RD "%WinDir%\System32\rainss_perm" 2>NUL
SET /p rains=�˲�����ж��mysql����ɾ��mysql��������,�Ƿ������(Y/N):
IF "%rains%" == "Y" (GOTO uninsdel) ELSE (IF "%rains%" == "y" (GOTO uninsdel) ELSE ( GOTO quitcommand))

:uninsdel
NET stop mysql
ECHO mysql������ֹͣ
ECHO=
SC delete mysql
ECHO mysql�����Ѵ�ע���ɾ��
ECHO=
RMDIR /s /q mysql-5.7.24-winx64
ECHO mysql���ݿ��ļ�����ɾ��
ECHO=
GOTO processOver

:quitcommand
ECHO ����ȡ��
GOTO processOver

:processOver
ECHO=
ECHO ��������˳�......
pause>nul