@REM ==================================================================================
@REM Maven Wrapper - Version oficial para Windows
@REM ==================================================================================
@echo off
setlocal enabledelayedexpansion

for %%A in ("%~dp0.") do set "DIRNAME=%%~fA"

if "%DIRNAME%" == "" set "DIRNAME=."

set "MAVEN_WRAPPER_JAR=%DIRNAME%\.mvn\wrapper\maven-wrapper.jar"

@REM Find java.exe
if defined JAVA_HOME goto findJavaFromJavaHome

set JAVA_EXE=java.exe
%JAVA_EXE% -version >nul 2>&1
if "%ERRORLEVEL%"=="0" goto execute

echo.
echo ERROR: JAVA_HOME is not set and no 'java' command could be found in your PATH.
exit /b 1

:findJavaFromJavaHome
set JAVA_HOME=%JAVA_HOME:"=%
set JAVA_EXE=%JAVA_HOME%\bin\java.exe

if exist "%JAVA_EXE%" goto execute

echo.
echo ERROR: JAVA_HOME is set to an invalid directory: %JAVA_HOME%
exit /b 1

:execute
@REM Execute Maven using the wrapper jar
"%JAVA_EXE%" -cp "%MAVEN_WRAPPER_JAR%" "-Dmaven.multiModuleProjectDirectory=%DIRNAME%" org.apache.maven.wrapper.MavenWrapperMain %*

exit /b %ERRORLEVEL%





