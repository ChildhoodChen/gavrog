@echo off
setlocal EnableExtensions EnableDelayedExpansion

if defined JAVA_HOME (
  set "JAVA=%JAVA_HOME%\bin\java"
) else (
  set "JAVA=java"
)
set "BASE=$INSTALL_PATH"

set "GAVROG=%BASE%\Systre.jar;%BASE%\3dt-Main.jar"
set "JREAL=%BASE%\jReality\jReality.jar"
set "JREAL_PLUS=%BASE%\jReality\bsh.jar;%BASE%\jReality\jtem-beans.jar;%BASE%\jReality\jterm.jar"
set "XSTREAM=%BASE%\XStream\xpp3.jar;%BASE%\XStream\xstream.jar"
set "SUNFLOW=%BASE%\sunflow\sunflow.jar;%BASE%\sunflow\janino.jar"
set "CLASSPATH=%GAVROG%;%JREAL%;%JREAL_PLUS%;%XSTREAM%;%SUNFLOW%"

set "RENDERER_MODE=%GAVROG_3DT_RENDERER%"
if not defined RENDERER_MODE set "RENDERER_MODE=auto"

set "APP_ARGS="
:parse_args
if "%~1"=="" goto args_done
if /I "%~1"=="--renderer" (
  if not "%~2"=="" (
    set "RENDERER_MODE=%~2"
    shift
    shift
    goto parse_args
  )
)
set "ARG1=%~1"
if /I "!ARG1:~0,11!"=="--renderer=" (
  set "RENDERER_MODE=!ARG1:~11!"
  shift
  goto parse_args
)
set "APP_ARGS=!APP_ARGS! "%~1""
shift
goto parse_args

:args_done
for %%A in (%RENDERER_MODE%) do set "RENDERER_MODE=%%~A"
if /I not "%RENDERER_MODE%"=="software" if /I not "%RENDERER_MODE%"=="auto" if /I not "%RENDERER_MODE%"=="opengl" (
  echo 3dt warning: unknown renderer mode '%RENDERER_MODE%'; using 'auto'.
  set "RENDERER_MODE=auto"
)

set "PLATFORM_KEY=windows-x86_64"
set "ARCH_TOKEN=%PROCESSOR_ARCHITECTURE%"
if defined PROCESSOR_ARCHITEW6432 set "ARCH_TOKEN=%PROCESSOR_ARCHITEW6432%"
if /I "%ARCH_TOKEN%"=="AMD64" set "PLATFORM_KEY=windows-x86_64"
if /I "%ARCH_TOKEN%"=="X86_64" set "PLATFORM_KEY=windows-x86_64"
if /I "%ARCH_TOKEN%"=="ARM64" set "PLATFORM_KEY=windows-arm64"
if /I "%ARCH_TOKEN%"=="X86" set "PLATFORM_KEY=windows-x86"

set "HW_ROOT=%GAVROG_3DT_ACCEL_ROOT%"
if not defined HW_ROOT set "HW_ROOT=%BASE%\hardware"

set "JAVA_OPTS=-D3dt.home=%BASE%"
set "REQUEST_HW=0"
if /I "%RENDERER_MODE%"=="auto" set "REQUEST_HW=1"
if /I "%RENDERER_MODE%"=="opengl" set "REQUEST_HW=1"

if "%REQUEST_HW%"=="1" if exist "%BASE%\jogl\jogl.jar" if exist "%BASE%\jogl\gluegen-rt.jar" if exist "%HW_ROOT%\%PLATFORM_KEY%\" (
  set "CLASSPATH=%CLASSPATH%;%BASE%\jogl\jogl.jar;%BASE%\jogl\gluegen-rt.jar"
  set "JAVA_OPTS=%JAVA_OPTS% -Djava.library.path=%HW_ROOT%\%PLATFORM_KEY% -Dorg.gavrog.3dt.renderer=opengl"
) else (
  if /I "%RENDERER_MODE%"=="opengl" (
    echo 3dt warning: OpenGL renderer was requested, but no matching hardware bundle was found for '%PLATFORM_KEY%'. Falling back to software mode.
  )
  set "JAVA_OPTS=%JAVA_OPTS% -Dorg.gavrog.3dt.renderer=software"
)

"%JAVA%" -Xmx512m %JAVA_OPTS% -cp "%CLASSPATH%" org.gavrog.apps._3dt.Main %APP_ARGS%

endlocal
exit /b %errorlevel%
