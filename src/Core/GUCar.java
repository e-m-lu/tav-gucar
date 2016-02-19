package Core;

import java.math.BigInteger;

/* Assumptions:
 * The wiki pages says that the bitstream implementation up to us. We implemented it as a binary string. 
 * This would make it easy to send over network, because then we can just take 1 character at a time
 * parse for either 0 or 1 and send it as a bit over network.
 * 
 * We are assuming that the delimeters between the data in the output stream should be unique
 * 
 * We are assuming that there should be no problems that we are re-using delimeters for
 * both input and output bitstreams, since they are isolated from each other.
 * 
 * We are assuming that the angle cannot be  greater than 30, nor lesser than -30
 * 
 * We are assuming that the speed cannot be greater than 200, nor lesser than -200
 * 
 * If there is a corrupt character, which is interpreted as a double, there is no way around
 * because basically any binary string can be interpreted as a double. The only error detection
 * around it, is to control the value range on the double, such as from -30 to 30.
 * 
 */

/*
 * Error codes:
 * Error 10: Attempting to write to buffer, first argument was negative
 * Error 11: Attempting to write to buffer, first argument value exceed the length of the second argument
 * Error 12: Attempting to read from buffer, the first argument value was negative
 * Error 13: Attempting to read from buffer, the first argument value is greater than the length of the buffer
 * Error 14: Attempting to encode an invalid torque value, that exceeds the limits
 */

public class GUCar {
	private String outputBitstream = "";
	private String inputBitstream = "";
	private boolean isConnectedToUsb = false;
	private static String startDelimeter = "00100001"; // !
	private static String firstDelimeter = "00101000"; // (
	private static String secondDelimeter = "00101001"; // )
	private static String endDelimeter = "01000000"; // @
	// This is used to manually increase the complexity of delimeters, making them 
	// less likely to appear as a random numbers
	private static final String delimeterPostfix = "1010111100001";
	private static final int speedMinLimit = -200;
	private static final int speedMaxLimit = 200;
	private static final int angleMinLimit = -30;
	private static final int angleMaxLimit  = 30;
	
	public GUCar(){
		startDelimeter += delimeterPostfix;
		firstDelimeter += delimeterPostfix;
		secondDelimeter += delimeterPostfix;
		endDelimeter += delimeterPostfix;	
	}
	
	public void connectToUsb(){
		USB usb = new USB(true);
		isConnectedToUsb = usb.getIsConnected();
	}

	public static String doubleToBinary(double n) {
		long toLong = Double.doubleToLongBits(n);
		String binaryString = Long.toBinaryString(toLong);
		return binaryString;
	}

	public static Double binaryToDouble(String s) {
		BigInteger bigInteger = new BigInteger(s, 2);
		long longInteger = bigInteger.longValue();
		double doubleNumber = Double.longBitsToDouble(longInteger);

		return doubleNumber;

	}
	
	public static boolean checkSpeed(Double speed){
		if(speed < speedMinLimit){
			System.out.println("Received an invalid speed. Speed (" + speed + ") exceed the min limit of " + speedMinLimit);
			return false;
		}
		if(speed > speedMaxLimit){
			System.out.println("Received an invalid speed. Speed (" + speed + ") exceed the max limit of " + speedMaxLimit);
			return false;
		}
		
		return true;
	}
	
	public static boolean checkTorque(Double torque){
		if(torque < angleMinLimit){
			System.out.println("Received an invalid torque. Torque (" + torque + ") exceed the min limit of " + angleMaxLimit);
			return false;
		}
		
		if(torque > angleMaxLimit){
			System.out.println("Received an invalid torque. Torque (" + torque + ") exceed the max limit of " + angleMinLimit);
			return false;
		}
		
		return true;
	}
	// Used for testing, since there is no official way to get the input buffer without network functionality
	public static String getInputStream(double speed, double torque){
		String s = startDelimeter + doubleToBinary(speed) + firstDelimeter + doubleToBinary(torque) + endDelimeter;		
		return s;
	}

	// Writes sensor data to outputBitstream
	public void sendSensorData(double torque, double ultra_distance, double ir_distance) {
		if(!checkTorque(torque)){
			System.out.println("Error 14");
		}
		else{
		outputBitstream += startDelimeter + doubleToBinary(torque) + firstDelimeter + doubleToBinary(ultra_distance)
				+ secondDelimeter + doubleToBinary(ir_distance) + endDelimeter;
		}
		
	}
	
	
	// Reads instructions from inputBitstream
	public Data readSpeedAndTorque() {
		int startDelimeterPos = inputBitstream.indexOf(startDelimeter);
		if (startDelimeterPos < 0) {
			System.out.println("ERROR: Tried to read speed and torque from input stream. No start delimeter found.");
			return null;
		}
		System.out.println("Found start delimeter at " + startDelimeterPos);
		int firstDelimeterPos = inputBitstream.indexOf(firstDelimeter, startDelimeterPos);
		if (firstDelimeterPos < 0) {
			System.out.println("ERROR: Tried to read speed and torque from input stream. "
					+ "after the start delimeter.");
			return null;
		}
		System.out.println("Found first delimeter at " + firstDelimeterPos);

		int endDelimeterPos = inputBitstream.indexOf(endDelimeter, firstDelimeterPos+firstDelimeter.length());
		if (endDelimeterPos < 0) {
			System.out.println("ERROR: Tried to read speed and torque from input stream. "
					+ "No end delimeter found after the first delimeter.");
			return null;
		}
		System.out.println("Found the end delimeter at " + endDelimeterPos);
		System.out.println();
		
		System.out.println("Bitstream: " + inputBitstream);
		System.out.println("Bitstream size: " + inputBitstream.length());
		System.out.println("Start delimeter pos: " + startDelimeterPos + " + "+ startDelimeter.length());
		System.out.println("First delimeter pos: " + firstDelimeterPos + " + " + firstDelimeter.length());
		System.out.println("End delimeter pos: " + endDelimeterPos);
		System.out.println();

		System.out.println("Found speed, between " + startDelimeterPos + startDelimeter.length() + " and " + firstDelimeterPos);
		String speed = inputBitstream.substring(startDelimeterPos + startDelimeter.length(), firstDelimeterPos);
		System.out.println("Speed: " + speed);
		System.out.println();

		System.out.println("Found torque, between " + (firstDelimeterPos + firstDelimeter.length()) + " and " + endDelimeterPos);
		String torque = inputBitstream.substring(firstDelimeterPos + firstDelimeter.length(), endDelimeterPos);
		System.out.println("Torque: " + torque);
		System.out.println();

		double speedDouble;
		double torqueDouble;
		// need to handle if double conversion fails
		try{
			speedDouble = binaryToDouble(speed);
			if(!checkSpeed(speedDouble)){
				// Try to find another data packet after the current one has invalid data
				return readSpeedAndTorque(startDelimeterPos + startDelimeter.length());
			}
		}catch(Exception e){
			System.out.println("Conversion of speed string" + speed + " to double failed."
					+ "Looking for a new starting point");
			return readSpeedAndTorque(startDelimeterPos + startDelimeter.length());
		}
		
		try{
			torqueDouble = binaryToDouble(torque);
			if(!checkSpeed(torqueDouble)){
				// Try to find another data packet after the current one has invalid data
				return readSpeedAndTorque(startDelimeterPos + startDelimeter.length());
			}
		}catch(Exception e){
			System.out.println("Conversion of torque string" + speed + " to double failed."
					+ "Looking for a new starting point");
			return readSpeedAndTorque(startDelimeterPos + startDelimeter.length());
		}
		
		Data data = new Data(speedDouble, torqueDouble);
		return data;

	}
	// A wrapper function for readSpeedAndTorque() that tries to find a data packet, after removing the starting
	// of the last attempt
	private Data readSpeedAndTorque(int startPos){
		inputBitstream = inputBitstream.substring(startPos+1);
		Data data = readSpeedAndTorque();
		
		return data;
	}

	// Writes to inputBitstream
	public int writeToBuffer(int n, String s) {
		
		if(n < 0){
			System.out.println("Error 10");
			return 10;
		}
		
		if(n  > s.length()){
			System.out.println("Error 11");
			return 11;
		}
		
		String substring = s.substring(0, n);
		inputBitstream += substring;		
		return 1;
	}

	// Pops from outputBitstream
	public Bitstream readFromBuffer(int n) {
		if(n < 0){
			System.out.println("Error 12");
			return new Bitstream(12, outputBitstream);
		}
		
		if(n > outputBitstream.length()){
			System.out.println("Error 13");
			return new Bitstream(13, outputBitstream);
		}
		
		String readValue = outputBitstream.substring(0, n);
		outputBitstream = outputBitstream.substring(n);

		return new Bitstream(0, readValue);
	}

	public String getOutputBitstream(){
		return outputBitstream;
	}
	
	public void setOutputBitstream(String s){
		outputBitstream = s;
	}
	
	public String getInputBitstream(){
		return inputBitstream;
	}
	
	public void setInputBitstream(String s){
		inputBitstream = s;
	}
	
	public static String getStartDelimeter(){
		return startDelimeter;
	}
	
	public static String getFirstDelimeter(){
		return firstDelimeter;
	}
	
	public static String getSecondDelimeter(){
		return secondDelimeter;
	}
	
	public static String getEndDelimeter(){
		return endDelimeter;
	}
	
	public boolean getIsConnectedToUSB(){
		return isConnectedToUsb;
	}
}
