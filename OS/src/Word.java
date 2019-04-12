import java.util.Arrays;

public class Word {
	char data[];
	
	public Word() {
		data = new char[] {'0','0','0','0'}; // musu zodzio ilgis=4 ir pradzioje 0liai galbut geriau butu definint size 4 ir tada inicializtuot
											 // su kokiu ciklu
		// galbut reikia inicijuotu nul tais o ne nuliais
	}
	
	public Word(Word src) {
		data = src.data.clone();
	}
	
	public Word(char[] data) { // kazkaip uztikrinti kad 4 paduotume
		this.data=data;
	}
	
	public char getByte(int index) {
		return data[index];
	}
	
	public void setByte(int index, char info) {
		data[index] = info;
	}
	
	char[] getBytes() {
		return Arrays.copyOf(data, 4);
	}
	
	void setBytes(char[] data) {
		this.data=data;
	}
	
	public String toString() {
		//return this.data.toString();
		return new String(data);
	}
	
	public boolean isEqualToString(String command) {
		if(String.valueOf(data).equalsIgnoreCase(command)) {
			return true;
		}
		else {
			return false;
		}
		
	
	}
	public int toInt() {
		 return Integer.parseUnsignedInt(String.valueOf(data),16); // buvo su signed
		 
	}
	
	public void fromInt(int number) {
		//this.data[3]=Integer.toHexString(number)
		this.data = (Integer.toHexString(number).toCharArray()); // damn kazkaip reikia atgal i sesioliktaine
	}
}

