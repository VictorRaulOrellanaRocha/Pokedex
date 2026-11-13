@rem Gradle startup script for Windows
@echo off
set DIR=%~dp0
if exist "%DIR%gradle\wrapper\gradle-wrapper.jar" (
  java -classpath "%DIR%gradle\wrapper\gradle-wrapper.jar" org.gradle.wrapper.GradleWrapperMain %*
) else (
  echo Wrapper JAR not found. Open the project in Android Studio and use its Gradle integration.
  exit /b 1
)
