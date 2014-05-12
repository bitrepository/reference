@ECHO OFF

set JAVA=c:\Program Files (x86)\Java\jre7\bin\java.exe

set BASEDIR=%0%
for %%x in (%BASEDIR%) DO set BASEDIR=%%~dpx..
set CONFDIR=%BASEDIR%\conf
set KEYFILE=%CONFDIR%\mycert-key.pem
set JAVA_OPTS=-classpath  "%BASEDIR%\lib\*"
set JAVA_OPTS=%JAVA_OPTS% "-Dlogback.configurationFile=%CONFDIR%\logback.xml"
set JAVA_OPTS=%JAVA_OPTS% "-DBASEDIR=%BASEDIR%"

set CMD=help
if /I x%1%==xdelete        set CMD=DeleteFile
if /I x%1%==xget-checksums set CMD=GetChecksums
if /I x%1%==xget-file      set CMD=GetFile
if /I x%1%==xget-file-ids  set CMD=GetFileIDs
if /I x%1%==xput-file      set CMD=PutFile -d
if /I x%1%==xreplace-file  set CMD=ReplaceFile

if not "%CMD%"=="help" goto run
echo. usage: %0% CMD PARAMS
echo.   CMD is one of
echo.     delete
echo.     get-checksums
echo.     get-file
echo.     get-file-ids
echo.     put-file
echo.     replace-file
goto :eof
:run
set CMD=org.bitrepository.commandline.%CMD%

"%JAVA%" %JAVA_OPTS% %CMD% "-k%KEYFILE%" "-s%CONFDIR%" %*%
