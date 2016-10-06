# AdeunisDemonstrator
Java tool to configure Adeunis LoRa demonstrator v2

![alt tag](https://github.com/onesse/AdeunisDemonstrator/blob/master/adeunis/LoRa_demonstrator.png?raw=true)

## On Windows

Before running for the first time this program make sure :
- you connected your device,
- you turned on the device,
- you downloaded the RXTX Java serial controller and RXTX native driver at this address : http://jlog.org/rxtx-win.html
- you added the external JAR on your favorite IDE and the DLL library 

In Eclipse, right click on the project Properties->Java Build Path->Libraries->
- add the external jar
- expands the imported jar and edit Native library location -> choose the directory where you saved your rxtxSerial.dll

## On linux
Install the package librxtx-java
```
apt-get install librxtx-java
```
Then when you run you program add the following argument :
```
-Djava.library.path=/usr/lib/jni/
```
:warning: gnu.io.CommPortIdentifier serial devices does not include ttyACM. A symbolik link must be created to use ttyS[x]. Exemple : 
First find the port number with the command
```
demsg | grep ACM
```
Then create a symbolik link (in my case the port was 0)

```
sudo ln -s /dev/ttyACM0 /dev/ttyS80
```
Run the program and enter :
```
/dev/ttyS80
```
as the COM port to use.

Enjoy !
