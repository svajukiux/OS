import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.util.ArrayList;

public class InputDevice {
	public Word readWord() {
		BufferedReader br = new BufferedReader(new InputStreamReader(System.in));
		
			String s;
			try {
				s = br.readLine();
				int length =s.length();
				if(length>4) {
				throw new IOException("Klaida. Ivestis ilgesne nei 4 baitai");
					
				}
				else if(length==4) {
					return new Word(s.toCharArray());
				}
				else if(length<4) {
					int position = 4-length;
					Word newWord = new Word();
					for(int i=0;i<length; i++) {
						newWord.setByte(position, s.charAt(i));
						position++;
					}
					return newWord;
				}
			} catch (IOException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
			return null; //? neturetu siaip cia niekad ateit
		
	}
	
	public ArrayList <Word> readBytes(int numberOfBytes) {
		String s;
		BufferedReader br = new BufferedReader(new InputStreamReader(System.in));
		ArrayList<Word> words = new ArrayList<>();
		
			try {
				s= br.readLine();
				if(s.length()<numberOfBytes) { //  buvo vietoj <  !=
					System.out.println("Neuzpildytas visas buferis gale bus zvaigzdutes simbolis");
					//int doneBytes=0;
					//int length
					//while(s.length)
					//char end='\u0000';
					s=s.concat("*");
					numberOfBytes=s.length();
					System.out.println(s);
				}
				if(s.length()>numberOfBytes) {
					System.out.println("Ivestis per ilga. Nebus nuskaitytas visas zodis");
				}
				
				int doneBytes=0;
				while(numberOfBytes-4 >=0) {
					
					Word word = new Word(s.substring(0+doneBytes,4+doneBytes).toCharArray());
					//vmMemory[dsValue+startingPlace/16][dsValue+startingPlace%16]=new Word(word);
					//startingPlace++;
					doneBytes+=4;
					numberOfBytes-=4;
					words.add(word);
				}
				if(numberOfBytes<4 && numberOfBytes>0) {
					Word word = new Word();
					word.setBytes(new char[]{'\u0000','\u0000','\u0000','\u0000'});
					int left= numberOfBytes;
					int position = 0;
					for(int i=0; i<left; i++) {
						word.setByte(position, s.charAt(doneBytes));
						doneBytes++;
						position++;
					}
					words.add(word);
				}
				//loadDataAsBytes(words,startingPlace,vm);
				
			} catch (IOException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
			
		return words;
		
	}
	
}
