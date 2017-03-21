# AdeunisDemonstrator
Java tool to configure Adeunis LoRa demonstrator v2

![alt tag](https://github.com/onesse/AdeunisDemonstrator/blob/master/LoRa_demonstrator.png?raw=true)

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
Then when you run you program add the following argument to your JVM :
```
-Djava.library.path=/usr/lib/jni/
```
:warning: gnu.io.CommPortIdentifier serial devices does not include ttyACM. A symbolik link must be created to use ttyS[x]. Exemple : 
First find the port number with the command
```
dmesg | grep ACM
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

## Program menu
```
Enter the COM port to use : 
/dev/ttyS80
Stable Library
=========================================
Native lib Version = RXTX-2.2pre2
Java lib Version   = RXTX-2.1-7
WARNING:  RXTX Version mismatch
	Jar version = RXTX-2.1-7
	native lib Version = RXTX-2.2pre2
PLEASE WAIT...
COMMAND_MODE - OK

O
COMMAND_REGISTERS_UNLOCK - OK
***********************************************
Choose one of the following options : 
	 -Choose activation mode :              ACT
	 -Activate ADR :                        ADR -> 1 / 0
	 -Enter DevAddr (4 octets) :            DEVADDR
	 -Enter AppSkey (16 octets) :           APPSKEY
	 -Enter NwkSkey (16 octets) :           NWKSKEY
	 -Enter Appkey (16 octets) :            APPKEY
	 -Enter AppEui (8 octets) :             APPEUI
	 -Modify fPort (1 octet) :              FPORT
	 -Choose ACK and CLASS of the device :  AKLS
	 -Change TX period (int) :              TX
	 -Read a register :                     READ
	 -Write a register :                    WRITE
	 -Exit program :                        EXIT
***********************************************
Choose one option :
```

Enjoy !
