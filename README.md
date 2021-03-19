# DynamicVerification
First step of the proof of concept Dynamic Verification.

The focus actually is on the analyze of a trace to determine the potential code smells, and on the different ways to generate such trace.

Are actually under testing :
- Aspect Programming
- Soot Instrumentation
- Generation of Debug breakpoints 

####Global usage
```
usage: ddcf [-h] {instrumentation,analyse} ...

A dynamic analysis tool to detect Android Code smells

positional arguments:
  {instrumentation,analyse}
    instrumentation      Instrumentalize an app
    analyse              Analyse the execution trace of an app

named arguments:
  -h, --help             show this help message and exit
```

####Instrumentation mode usage
```
usage: ddcf instrumentation [-h] -a ANDROIDJARS -o OUTPUT -p PACKAGE apk

positional arguments:
  apk                    Path of the APK to analyze

named arguments:
  -h, --help             show this help message and exit
  -a ANDROIDJARS, --androidJars ANDROIDJARS
                         Path to android platforms jars
  -o OUTPUT, --output OUTPUT
                         Path to  the  folder  where  the  instrumented APK
                         output is generated
  -p PACKAGE, --package PACKAGE
                         Main package of the app
```

####Analyse mode usage
```
usage: ddcf analyse [-h] -a ANDROIDJARS -t TRACE -o OUTPUT -p PACKAGE apk

positional arguments:
  apk                    Path of the APK to analyze

named arguments:
  -h, --help             show this help message and exit
  -a ANDROIDJARS, --androidJars ANDROIDJARS
                         Path to android platforms jars
  -t TRACE, --trace TRACE
                         Path to the execution trace
  -o OUTPUT, --output OUTPUT
                         Path to the folder  for  the  .csv  results of the
                         detection
  -p PACKAGE, --package PACKAGE
                         Main package of the app
```

Example of usage :
```
java -jar ddcf.jar instrumentation -a android-platforms/ -o apkOutputs/ -p com.core.package application.apk
```

Analyse :
```
java -jar ddcf.jar analyse -a android-platforms/ -t trace.txt -o csvOutputs/ -p com.core.package application.apk
```