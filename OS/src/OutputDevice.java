
public class OutputDevice {
	
	public void printWord(Word word) {
		System.out.println(word);
		
	}
	
	public void printBytes(char [] bytes) {
		for(int i=0; i<bytes.length; i++) {
			System.out.print(bytes[i]);
		}
		System.out.println("");
		
	}
	
	
}
