# Useful commands
## 1. Instrumentation of the application mobile
```
java -jar DynamicVerification.jar instrumentation -a G:/Sdk/platforms/ -o test app-debug.apk
 ```

## 2. Run the application
### 2.1. Sign the application
```
jarsigner -keystore testkey.jks -storepass testtest test/app-debug.apk client
```
### 2.2. Install the application
```
G:/Sdk/platform-tools/adb.exe install test/app-debug.apk
```
### 2.3. Run the application
```
adb shell monkey -p cherief.houcine.myapplication -v 500
```
#### 2.3.1. Manually
```
G:/Sdk/platform-tools/adb.exe shell am start -n cherief.houcine.myapplication/cherief.houcine.myapplication.Controlleur.MedecinDrawerActivityActivity
```
#### 2.3.2. Using monkey
```
adb shell monkey -p cherief.houcine.myapplication -v 500
```
### 2.4. Save the execution trace
```
adb logcat -d > trace.txt
```
### 2.5. Stop the execution
#### 2.5.1. Force stop the application
```
adb shell am force-stop cherief.houcine.myapplication
```
#### 2.5.1. Force stop the application
```
adb shell am kill cherief.houcine.myapplication
```

## 3. Analyse the test trace
```
java -jar DynamicVerification.jar analyse -a G:/Sdk/platforms/ -t test/trace.txt -o csvOutputs/ app-debug.apk
```