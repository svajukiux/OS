import java.util.Arrays;

public class Word {
	char data[];
	
	public Word() {
		data = new char[] {'0','0','0','0'}; // musu zodzio ilgis=4 ir pradzioje 0liai galbut geriau butu definint size 4 ir tada inicializtuot
											 // su kokiu ciklu
		//data = {'0','0','0','0'};
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
}
