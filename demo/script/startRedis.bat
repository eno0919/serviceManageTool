@ECHO OFF
SET TMP_DIR=C:\ims-depends\Redis-x64-3.2
CD /D %TMP_DIR%
redis-server --service-start