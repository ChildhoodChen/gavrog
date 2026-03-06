@echo off
setlocal EnableExtensions

if not defined IZPACK set "IZPACK=%HOME%\My Documents\Software\IzPack"
if not defined GAVROG set "GAVROG=%CD%\.."

jar cmf "%GAVROG%\SYSTRE.MF" Systre.jar -C "%GAVROG%\bin" org -C "%GAVROG%\bin" buoy
jar cf 3dt-Main.jar -C "%GAVROG%\bin" org

for %%D in (hardware\linux-x86_64 hardware\windows-x86_64 hardware\windows-arm64 hardware\macos-x86_64 hardware\macos-arm64) do (
  if not exist "%%D\" echo warning: missing optional hardware bundle directory '%%D'
)

call "%IZPACK%\bin\compile" install.xml

del Systre.jar 3dt-Main.jar

endlocal
