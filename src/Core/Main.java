package Core;

import java.io.UnsupportedEncodingException;

public class Main {

	public static void main(String[] args) {
		// Use one of the below 4 methods to check if a main four interfaces are working as expected.
		
		//testWriteToBuffer();
		testReadingData();
		//testReadFromBuffer();
		//testSendingSensorData();
				

	}

	private static void testReadingData() {
		GUCar car = new GUCar();
		String yolo = car.getInputStream(11, 12);
		car.setInputBitstream(yolo);
		Data data = car.readSpeedAndTorque();
		System.out.println("Speed: " + data.getSpeed());
		System.out.println("Torque: " + data.getTorque());
	}
	
	private static void testWriteToBuffer(){
		GUCar car = new GUCar();
		car.writeToBuffer(3, "abcde");
		System.out.println(car.getInputBitstream());
	}
	
	private static void testReadFromBuffer(){
		GUCar car = new GUCar();
		car.setOutputBitstream("123abcd");
		// reads the first 4 characters
		System.out.println(car.readFromBuffer(4).getBistream());
		// shows what remains in the outputBitstream
		System.out.println(car.getOutputBitstream());
	}
	
	private static void testSendingSensorData(){
		GUCar car = new GUCar();
		car.sendSensorData(10, 20, 30);
		System.out.println(car.getOutputBitstream());
	}

}
