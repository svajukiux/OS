
public class OutputDevice {
	
	public void printWord(Word word) { //skaiciai
		System.out.println(word);
		
	}
	
	public void printBytes(char [] bytes) { // 
		for(int i=0; i<bytes.length; i++) {
			if(bytes[i]=='0') {
				System.out.print(' ');
			}
			else if(bytes[i]=='*') {
				break;
			}
			else {
				System.out.print(bytes[i]);
			}
		}
		System.out.println("");
		
	}
	
	
}
