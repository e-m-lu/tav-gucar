package Test;

import org.junit.*;
import Core.GUCar;
import Core.Data;
import Core.Bitstream;

public class GUCarTest {
	GUCar car;
	
	@Before
	public void readyCar(){
		car = new GUCar();
	}


	@Test
	public void testCheckSpeedPass() {
		Assert.assertTrue(GUCar.checkSpeed(10.0));
	}
	
	@Test
	public void testCheckSpeedMin(){
		Assert.assertFalse(GUCar.checkSpeed(-300.0));
	}
	
	@Test
	public void testCheckSpeedMax(){
		Assert.assertFalse(GUCar.checkSpeed(300.0));
	}
	@Test
	public void testCheckTorquePass() {
		Assert.assertTrue(GUCar.checkTorque(10.0));
	}
	
	@Test
	public void testCheckTorqueMin(){
		Assert.assertFalse(GUCar.checkTorque(-31.0));
	}
	
	@Test
	public void testCheckTorqueMax(){
		Assert.assertFalse(GUCar.checkTorque(100.0));
	}
	
	@Test
	public void testDoubleToBinaryAndBack(){
		Double start = 42.42;
		String middle = GUCar.doubleToBinary(start);
		Double end = GUCar.binaryToDouble(middle);
		Assert.assertEquals(start, end);
	}
	
	@Test
	public void testSendSensorDataTorqueFail(){
		car.sendSensorData(40.0, -1, -1);
		Assert.assertEquals("", car.getOutputBitstream());		
	}
	
	@Test
	public void testSendSensorDataTorquePass(){
		car.sendSensorData(20.0, -1, -1);
		Assert.assertNotEquals("", car.getOutputBitstream());		
	}
	
	@Test
	public void testSendSensorDataStartDelimeterExists(){
		car.sendSensorData(10.0, 11, -12);
		String output = car.getOutputBitstream();
		Assert.assertTrue(output.indexOf(car.getStartDelimeter()) > -1);		
	}
	
	@Test
	public void testSendSensorDataFirstDelimeterExists(){
		car.sendSensorData(10.0, 11, -12);
		String output = car.getOutputBitstream();
		Assert.assertTrue(output.indexOf(car.getFirstDelimeter()) > -1);		
	}
	
	@Test
	public void testSendSensorDataSecondDelimeterExists(){
		car.sendSensorData(10.0, 11, -12);
		String output = car.getOutputBitstream();
		Assert.assertTrue(output.indexOf(car.getSecondDelimeter()) > -1);		
	}

	@Test
	public void testSendSensorDataEndDelimeterExists(){
		car.sendSensorData(10.0, 11, -12);
		String output = car.getOutputBitstream();
		Assert.assertTrue(output.indexOf(car.getEndDelimeter()) > -1);		
	}
	
	@Test
	public void testReadSpeedAndTorqueDelimeterOrderFail(){
		String inputStream = "";
		inputStream += GUCar.getEndDelimeter() + "0101010" + GUCar.getFirstDelimeter() + "101010101" + GUCar.getStartDelimeter();
		car.setOutputBitstream(inputStream);

		Assert.assertEquals(null, car.readSpeedAndTorque());		
	}
	
	@Test
	public void testReadSpeedAndTorqueWrongValues(){
		String inputBitstream = GUCar.getInputStream(320.0, 50.0);
		inputBitstream += GUCar.getInputStream(57, 11.5);
		car.setInputBitstream(inputBitstream);
		Data data = car.readSpeedAndTorque();
		Data expectedData = new Data(57, 11.5);
		Assert.assertTrue(data.getSpeed() == expectedData.getSpeed() 
				&& data.getTorque() == expectedData.getTorque());		
	}
	
	@Test
	public void testReadSpeedAndTorquePass(){
		String inputBitstream = GUCar.getInputStream(112, 3.14);
		car.setInputBitstream(inputBitstream);
		Data data = car.readSpeedAndTorque();
		Data expectedData = new Data(112, 3.14);
		
		Assert.assertTrue(data.getSpeed() == expectedData.getSpeed() 
				&& data.getTorque() == expectedData.getTorque());	
	}
	
	@Test
	public void testWriteToBufferNegative(){
		int result = car.writeToBuffer(-1, "01010");
		Assert.assertEquals(10, result);
	}
	
	@Test
	public void testWriteToBufferExceed(){
		int result = car.writeToBuffer(10, "1110");
		Assert.assertEquals(11, result);
	}
	
	@Test
	public void testWriteToBufferEmpty(){
		int result = car.writeToBuffer(3, "1010");
		Assert.assertTrue(result == 1 && car.getInputBitstream().equals("101"));
	}
	
	@Test
	public void testWriteToBufferUsed(){
		String inputBitstream = GUCar.getInputStream(15, 15);
		car.setInputBitstream(inputBitstream);
		
		int result = car.writeToBuffer(5, "1110111");
		Assert.assertTrue(result == 1 && car.getInputBitstream().equals(inputBitstream + "11101"));
	}
	
	@Test
	public void testReadFromBufferNegative(){
		Bitstream result = car.readFromBuffer(-1);
		Assert.assertEquals(12, result.getErrorCode());	
	}
	
	@Test
	public void testReadFromBufferExceed(){
		Bitstream result = car.readFromBuffer(car.getOutputBitstream().length() + 1);
		Assert.assertEquals(13, result.getErrorCode());
	}
	
	@Test
	public void testReadFromBufferPass(){
		car.sendSensorData(10, 11, 12);
		String inputBitstream = car.getOutputBitstream();
		Bitstream result = car.readFromBuffer(10);
		
		Assert.assertEquals(result.getBistream(), inputBitstream.substring(0, 10));
	}
	
	@Test
	public void testUSBNotConnected(){
		Assert.assertFalse(car.getIsConnectedToUSB());
	}
	
	@Test
	public void testUSBConnected(){
		car.connectToUsb();
		Assert.assertTrue(car.getIsConnectedToUSB());
	}
		
}
