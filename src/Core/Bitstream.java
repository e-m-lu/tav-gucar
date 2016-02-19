package Core;

public class Bitstream {
	private int errorCode;
	private String bitstream;
	
	public Bitstream(int errorCode, String bitstream){
		this.errorCode = errorCode;
		this.bitstream = bitstream;		
	}
	
	public int getErrorCode(){
		return errorCode;
	}
	
	public String getBistream(){
		return bitstream;
	}

}
