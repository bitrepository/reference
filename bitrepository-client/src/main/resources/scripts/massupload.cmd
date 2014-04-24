@echo off
set base=%0%
set collection=%1%
set prefix=%2%
shift
shift

if "%1%"=="" (
   echo Usage: %0% COLLECTION PREFIX FILES
   goto :eof
)

for %%x in ("%base%") do set base=%%~dpx

:next
set args=%1%
if ""=="%args%" goto :eof
for %%f in (%args%) do call :handle_file "%%~f"
shift
goto :next

:handle_file
set file=%*%
for %%g in (%file%) do set name=%%~nxg
echo *** uploading %file% as "%prefix%%name%" ***
call "%base%bitmag.cmd" put-file -c %collection% -f %file% -i "%prefix%%name%"
echo.
