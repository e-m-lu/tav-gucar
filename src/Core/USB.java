package Core;

public class USB {
	
	private boolean isConnected;
	
	public USB(){
		isConnected = false;
	}
	
	public USB(boolean status){
		isConnected = status;
	}
	
	public boolean getIsConnected(){
		return isConnected;
	}
	
	public void setIsConnected(boolean status){
		isConnected = status;
	}
}
