package fr.fluxon.adeunis;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.nio.ByteBuffer;
import java.util.Scanner;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.Future;

import gnu.io.CommPort;
import gnu.io.CommPortIdentifier;
import gnu.io.SerialPort;

public class AdeunisDemonstratorCom
{
	SerialPort serialPort;
	CommPort commPort;

	public void FirstStep(){
	}

	void connect ( String portName ) throws Exception
	{
		CommPortIdentifier portIdentifier = CommPortIdentifier.getPortIdentifier(portName);
		if ( portIdentifier.isCurrentlyOwned() )
		{
			System.out.println("Error: Port is currently in use");
		}
		else
		{
			//			System.out.println("Connect 1/2");
			commPort = portIdentifier.open(this.getClass().getName(),115200);

			if ( commPort instanceof SerialPort )
			{
				//				System.out.println("Connect 2/2");
				serialPort = (SerialPort) commPort;
				serialPort.setSerialPortParams(115200,SerialPort.DATABITS_8,SerialPort.STOPBITS_1,SerialPort.PARITY_NONE);
				serialPort.setFlowControlMode(SerialPort.FLOWCONTROL_NONE);
			}
			else
			{
				System.out.println("Error: Only serial ports are handled by this example.");
			}
		}
	}

	final protected static char[] hexArray = "0123456789ABCDEF".toCharArray();
	public static String bytesToHex(byte[] bytes) {
		char[] hexChars = new char[bytes.length * 2];
		for ( int j = 0; j < bytes.length; j++ ) {
			int v = bytes[j] & 0xFF;
			hexChars[j * 2] = hexArray[v >>> 4];
			hexChars[j * 2 + 1] = hexArray[v & 0x0F];
		}
		return new String(hexChars);
	}

	/** */
	private static boolean serialReader(InputStream in){
		int value;
		int previousValue = -1;
		BufferedReader br = new BufferedReader(new InputStreamReader(in));
		ByteBuffer buf = ByteBuffer.allocate(50);
		try
		{
			while ((value = in.read()) != 13)
			{
				if(value != -1){
					previousValue = value;
					buf.put((byte) value);
				}
			}
			if(value == 13 && previousValue == 69){
				br.close();
				System.out.println("problem error E");
				return false;
			}
			if(value == 13){
				br.close();
				buf.clear();
				System.out.println(new String(buf.array()));
				return true;
			}
			br.close();
			return false;
		}
		catch ( IOException e )
		{
			e.printStackTrace();
			return false;
		}
	}

	/** */
	public void serialWriter(OutputStream out, String toSend) {
		try
		{
			byte[] array = toSend.getBytes();
			out.write(array);
			out.flush();
		}
		catch ( IOException e )
		{
			e.printStackTrace();
		}
	}

	private static final String DISPLAY_VERSION = "AT/V\r\n";
	private static final String CONTENT_REGISTER = "ATS<n>?\r\n";
	private static final String RESET_FACTORY = "ATR\r\n";
	private static final String SAVE_NEW_CONFIG = "AT&W\r\n";
	private static final String SIMPLE_RESET = "AT&RST\r\n";
	private static final String EXIT_COMMAND_MODE = "ATO\r\n";
	private static final String COMMAND_MODE = "+++";
	private static final String ASSIGN_REGISTER = "ATS<n>=<m>\r\n";	


	private static final String COMMAND_REGISTERS_UNLOCK = "ATT63 ROOT\r\n";

	private static final int ACTIVATION_MODE_REGISTER = 221; // 0 : ABP 1 : OTAA
	private static final int LORA_OPTIONS_REGISTER = 220;
	private static final int DEVADDR_REGISTER = 281;
	private static final int[] APPSKEY_REGISTER = {226, 227, 228, 229};
	private static final int[] NWKSKEY_REGISTER = {222, 223, 224, 225};
	private static final int[] APPKEY_REGISTER = {216, 217, 218, 219};
	private static final int[] APPEUI_REGISTER = {214, 215};

	private static final int UPLINK_PORT_REGISTER = 383;
	private static final int ACK_REQUEST_AND_CLASS_REGISTER = 382;
	private static final int TX_PERIODICITY_REGISTER = 380;

	private static final int GPS_REGISTER = 371;

	public static void main ( String[] args )
	{
		String port;

		Scanner scan = new Scanner(System.in);

		System.out.println("Enter the COM port to use : ");
		port = scan.next().toUpperCase();

		String listeOptions = "***********************************************\n" +
				"Choose one of the following options : \n" +
				"\t -Choose activation mode : ACT\n" +
				"\t -Activate ADR : ADR -> 1 / 0\n" +
				"\t -Enter DevAddr (4 octets) : DEVADDR\n" +
				"\t -Enter AppSkey (16 octets) : APPSKEY\n" +
				"\t -Enter NwkSkey (16 octets) : NWKSKEY\n" +
				"\t -Enter Appkey (16 octets) : APPKEY\n" +
				"\t -Enter AppEui (8 octets) : APPEUI\n" +
				"\t -Modify fPort (1 octet) : FPORT\n" +
				"\t -Choose ACK and CLASS of the device : AKLS\n" +
				"\t -Change TX period (int) : TX\n"+
				"\t -Read a register : READ\n"+
				"\t -Exit program : EXIT\n"+
				"***********************************************";
		try
		{
			AdeunisDemonstratorCom firstStep = new AdeunisDemonstratorCom();

			firstStep.connect(port);
			InputStream in = firstStep.serialPort.getInputStream();
			OutputStream out = firstStep.serialPort.getOutputStream();
			
			System.out.println("PLEASE WAIT...");

			firstStep.serialWriter(out, COMMAND_MODE);
			verifExecution("COMMAND_MODE - OK", "COMMAND_MODE - KO", in);

			firstStep.serialWriter(out, COMMAND_REGISTERS_UNLOCK);
			verifExecution("COMMAND_REGISTERS_UNLOCK - OK", "COMMAND_REGISTERS_UNLOCK - KO", in);

			//PROGRAM
			boolean loop = true;
			while(loop){
				System.out.println(listeOptions);
				boolean launch = false;
				String command = ASSIGN_REGISTER;
				System.out.println("Choose one option : ");
				switch(scan.next().toUpperCase()){
				case "ACT":
					System.out.println("OTAA or ABP ?");
					String act = scan.next().toUpperCase();
					command = command.replace("<n>", String.valueOf(ACTIVATION_MODE_REGISTER));
					if(act.equals("OTAA")){
						command = command.replace("<m>", "1");
						launch = true;
					}else if(act.equals("ABP")){
						command= command.replace("<m>", "0");
						launch = true;
					}else{
						System.out.println("no such option");
					}
					if(launch){
						System.out.println(command);
						firstStep.serialWriter(out, command);
						verifExecution("ACTIVATION_MODE - OK", "ACTIVATION_MODE - KO", in);
					}
					break;
				case "ADR":
					System.out.println("0 ou 1 (activé) ?");
					String adr = scan.next().toUpperCase();
					command = command.replace("<n>", String.valueOf(LORA_OPTIONS_REGISTER));
					if(adr.equals("1")){
						command = command.replace("<m>", "1");
						launch = true;
					}else if(adr.equals("0")){
						command= command.replace("<m>", "0");
						launch = true;
					}else{
						System.out.println("no such option");
					}
					if(launch){
						System.out.println(command);
						firstStep.serialWriter(out, command);
						verifExecution("ADR_MODE - OK", "ADR_MODE - KO", in);
					}
					break;
				case "DEVADDR":
					System.out.println("Add your DEVADDR (hexa):");
					String devAddr = scan.next().toUpperCase();
					if(devAddr.length() == 8){
						command = command.replace("<n>", String.valueOf(DEVADDR_REGISTER));
						command = command.replace("<m>", devAddr);
						System.out.println(command);
						firstStep.serialWriter(out, command);
						verifExecution("DEVADDR_MODIFY - OK", "DEVADDR_MODIFY - KO", in);
					}else{
						System.out.println("not a good size");
					}
					break;
				case "APPSKEY":
					System.out.println("Add your APPSKEY (hexa):");
					String appSKey = scan.next().toUpperCase();
					if(appSKey.length() == 32){
						for(int i=0; i<4; i++){
							String _command = new String(ASSIGN_REGISTER);
							_command = _command.replace("<n>", String.valueOf(APPSKEY_REGISTER[i]));
							_command = _command.replace("<m>", appSKey.substring(0+i*8, 8+ i*8));
							System.out.println(_command);
							firstStep.serialWriter(out, _command);
							verifExecution("APPSKEY_MODIFY " + i + " - OK", "APPSKEY_MODIFY " + i + " - KO", in);
						}
					}else{
						System.out.println("not a good size");
					}
					break;
				case "NWKSKEY":
					System.out.println("Add your NWKSKEY (hexa):");
					String nwkSKey = scan.next().toUpperCase();
					if(nwkSKey.length() == 32){
						for(int i=0; i<4; i++){
							String _command = new String(ASSIGN_REGISTER);
							_command = _command.replace("<n>", String.valueOf(NWKSKEY_REGISTER[i]));
							_command = _command.replace("<m>", nwkSKey.substring(0+i*8, 8+ i*8));
							System.out.println(_command);
							firstStep.serialWriter(out, _command);
							verifExecution("NWKSKEY_MODIFY " + i + " - OK", "NWKSKEY_MODIFY " + i + " - KO", in);
						}
					}else{
						System.out.println("not a good size");
					}
					break;
				case "APPKEY":
					System.out.println("Add your APPKEY (hexa):");
					String appKey = scan.next().toUpperCase();
					if(appKey.length() == 32){
						for(int i=0; i<4; i++){
							String _command = new String(ASSIGN_REGISTER);
							_command = _command.replace("<n>", String.valueOf(APPKEY_REGISTER[i]));
							_command = _command.replace("<m>", appKey.substring(0+i*8, 8+ i*8));
							System.out.println(_command);
							firstStep.serialWriter(out, _command);
							verifExecution("APPKEY_MODIFY " + i + " - OK", "APPKEY_MODIFY " + i + " - KO", in);
						}
					}else{
						System.out.println("not a good size");
					}
					break;
				case "APPEUI":
					System.out.println("Add your APPEUI (hexa):");
					String appEUI = scan.next().toUpperCase();
					if(appEUI.length() == 16){
						for(int i=0; i<2; i++){
							String _command = new String(ASSIGN_REGISTER);
							_command = _command.replace("<n>", String.valueOf(APPEUI_REGISTER[i]));
							_command = _command.replace("<m>", appEUI.substring(0+i*8, 8+ i*8));
							System.out.println(_command);
							firstStep.serialWriter(out, _command);
							verifExecution("APPEUI_MODIFY " + i + " - OK", "APPEUI_MODIFY " + i + " - KO", in);
						}
					}else{
						System.out.println("not a good size");
					}
					break;
				case "FPORT":
					System.out.println("What fPort ?");
					int fPort = Integer.parseInt(scan.next().toUpperCase());
					command = command.replace("<n>", String.valueOf(UPLINK_PORT_REGISTER));
					if(fPort < 244 && fPort > 0){
						command = command.replace("<m>", String.valueOf(fPort));
						launch = true;
					}else{
						System.out.println("no such option");
					}
					if(launch){
						System.out.println(command);
						firstStep.serialWriter(out, command);
						verifExecution("UPLINK_FPORT_MODIFY - OK", "UPLINK_FPORT_MODIFY - KO", in);
					}
					break;
				case "AKLS":
					System.out.println("0 = Class A unconfirmed 1 = Class A confirmed 2 = Class C unconfirmed 3 = Class C confirmed ?");
					int ackClass = Integer.parseInt(scan.next().toUpperCase());
					command = command.replace("<n>", String.valueOf(ACK_REQUEST_AND_CLASS_REGISTER));
					if(ackClass < 4 && ackClass >= 0){
						command = command.replace("<m>", String.valueOf(ackClass));
						launch = true;
					}else{
						System.out.println("no such option");
					}
					if(launch){
						System.out.println(command);
						firstStep.serialWriter(out, command);
						verifExecution("ACK&CLASS - OK", "ACK&CLASS - KO", in);
					}
					break;
				case "TX":
					System.out.println("How many seconds between two messages (in secondes) ?");
					int period = Integer.parseInt(scan.next().toUpperCase());
					command = command.replace("<n>", String.valueOf(TX_PERIODICITY_REGISTER));
					if(period <= 86400 && period >= 0){
						command = command.replace("<m>", String.valueOf(period));
						launch = true;
					}else{
						System.out.println("no such option");
					}
					if(launch){
						System.out.println(command);
						firstStep.serialWriter(out, command);
						verifExecution("UPLINK_PERIOD_MODIFY - OK", "UPLINK_PERIOD_MODIFY - KO", in);
					}
					break;
				case "READ":
					System.out.println("What register ?");
					int registre = Integer.parseInt(scan.next().toUpperCase());
					command = CONTENT_REGISTER;
					command = command.replace("<n>", String.valueOf(registre));
					System.out.println(command);
					firstStep.serialWriter(out, command);
					verifExecution("READ_REGISTER - OK", "READ_REGISTER - KO", in);
					break;
				case "EXIT":
					loop = false;
					break;
				default:
					System.out.println("no such option");
					break;
				}
				firstStep.serialWriter(out, SAVE_NEW_CONFIG);
				verifExecution("SAVE_NEW_CONFIG - OK", "SAVE_NEW_CONFIG - KO", in);
			}

			firstStep.serialWriter(out, EXIT_COMMAND_MODE);
			verifExecution("EXIT_COMMAND_MODE - OK", "EXIT_COMMAND_MODE - KO", in);

			in.close();
			out.close();
			firstStep.commPort.close();
			firstStep.serialPort.close();
		}
		catch ( Exception e )
		{
			e.printStackTrace();
		}
	}

	private static void verifExecution(String messageSuccess, String messageError, InputStream in){
		if(AdeunisDemonstratorCom.serialReader(in)){
			System.out.println(messageSuccess);
		}else{
			System.err.println(messageError);
		}
	}
	private void modifyADR(byte register, boolean ADR){
		if(ADR){
			register |= (1 << 0);
		}else{
			register &= ~(1 << 0);
		}
	}
}
