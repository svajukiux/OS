import java.io.UnsupportedEncodingException;
import java.util.Arrays;
import java.util.Random;

import org.apache.commons.codec.DecoderException;
import org.apache.commons.codec.binary.Hex;

public class RM {
	private Word[][] memory;
	private char MODE;
	private Word PTR; // cia nereikia imti dvigubo nes char unsigned in java
	private int PC;
	private char SP;
	private int TI; // TI PI SI galbut uztenka vieno baito?
	private int PI;
	private int SI;
    private char CH[]; // galima atskirus 3 char registrus 
    private char SF; // kur multiple baitai galima char arrays
    
	
	
	public Word[][] getMemory() {
		return memory;
	}

	public void setMemory(Word[][] memory) {
		this.memory = memory;
	}

	public char getMODE() {
		return MODE;
	}

	public void setMODE(char mODE) {
		MODE = mODE;
	}

	public Word getPTR() {
		return PTR;
	}

	public void setPTR(Word pTR) {
		PTR = pTR;
	}

	public int getPC() {
		return PC;
	}

	public void setPC(int pC) {
		PC = pC;
	}

	public char getSP() {
		return SP;
	}

	public void setSP(char sP) {
		SP = sP;
	}

	public int getTI() {
		return TI;
	}

	public void setTI(int tI) {
		TI = tI;
	}

	public int getPI() {
		return PI;
	}

	public void setPI(int pI) {
		PI = pI;
	}

	public char[] getCH() {
		return CH;
	}

	public void setCH(char[] cH) {
		CH = cH;
	}
	public void setCHByte(int position) {
		CH[position]=1;
	}
	public void unsetCHByte(int position) {
		CH[position]=0;
	}

	public char getSF() {
		return SF;
	}

	public void setSF(char sF) {
		SF = sF;
	}

	public int getSI() {
		return SI;
	}

	public void setSI(int sI) {
		SI = sI;
	}
	
	public int getInterrupt() {
		switch(PI) {
			case 1: return 1; // memory access interrupt
			case 2: return 2; // bad operation code
			case 3: return 3; // bad assign
		}
		switch(SI) {
		case 1: return 4; // command RIx1x2
		case 2: return 5; // command PRx1x2
		case 3: return 6; // command IPSH
		case 4: return 7; // command PPOP
		case 5: return 8; // command LCxy
		case 6: return 9; // command UCx
		case 7: return 10; // command HALT
		}
		
		if(TI==0) {
			return 11; // timer interrupt
		}
		
		return 0;
	}
	
	public int processInterrupt() {
		switch(getInterrupt()) {
		
		case 1:{
			System.out.println("Out of bounds");
			return 1;
		}
		case 2: {
			System.out.println("Bad operation code");
			return 1;
		}
		
		case 3:{
			System.out.println("Negeras priskyrimas");
			return 1;
		}
		
		case 4: {
			// perkelt readinima
			break;
		}
		case 5: {
			// perkelt printinima
			break;
		}
		case 6: {
			// perkelt IPSH 
			break;
		}
		case 7: {
			// perkelt PPOP
		}
		case 8: {
			// perkelt LCxy?
		}
		case 9: {
			// perkelt UCx?
		}
		case 10: {
			// reset interupts as well?
			return 1; // HALT
		}
		case 11: {
			TI=10; // reset TI
			break;
		}
		
		}
		return 0;
	}

	public RM() {
		CH = new char[] {'0','0','0'};
		PTR = new Word();
		memory = new Word[64][16];
		for(int i=0; i<64; ++i) {
			for(int j=0; j<16; ++j) {
				
				memory[i][j]= new Word();	
				
			}
		}
	}
	
	public void loadVM(VM vm) {
		int pagingBlock = new Random().nextInt(60); // 4 paskutiniai blokai uzimti bendrai atminciai ir jos info
		System.out.println("Isskirta puslapiu lentele su PTR: " + pagingBlock);
		int allocatedBlocks=0;
		Word[] pagingTable = new Word[16];
		for(int i=0; i<16; i++) {
			pagingTable[i]= new Word();
		}
		int index = 0;
		
		while (allocatedBlocks<16) {
			boolean validBlock = true;
			int intBlock = new Random().nextInt(60); // naujas galimas blokas
			if(intBlock==pagingBlock) {
				validBlock=false;
			}
			else {
				for (Word block : pagingTable) { // perziurim visus
					if(block.toInt()==intBlock) {
						validBlock = false;
					}
				}
				if (validBlock) {
					
					pagingTable[index].fromInt(intBlock); // priskiria tinkama bloka i atitinkama vieta
					//System.out.println(pagingTable[index]);
					index++;
					allocatedBlocks++;
				}
			}
			System.out.println("Sekmingai iskirta" + index + "  virtualiu bloku");
		}
			
			PTR.setBytes(Integer.toHexString(pagingBlock).toCharArray()); // priskiriam PTR
			
			// binding virtual words to real words
			int currentVirtualBlock=0;
			for(Word pagingWords : pagingTable) {
				int currentRealBlock = pagingWords.toInt();
				//System.out.println(pagingWords);
				vm.assignMemoryBlock(this, currentVirtualBlock, currentRealBlock);
				currentVirtualBlock++; // priskiriu blokus bet galbut reiktu atskirai zodzius bus matyt kaip veiks
			}
			
			
		
	}
	
	public static String bytesToHex(byte[] bytes) {
	    final char[] hexArray = {'0','1','2','3','4','5','6','7','8','9','A','B','C','D','E','F'};
	    char[] hexChars = new char[bytes.length * 2];
	    int v;
	    for ( int j = 0; j < bytes.length; j++ ) {
	        v = bytes[j] & 0xFF;
	        hexChars[j * 2] = hexArray[v >>> 4];
	        hexChars[j * 2 + 1] = hexArray[v & 0x0F];
	    }
	    return new String(hexChars);
	}
	
	public static byte[] hexStringToByteArray(String s) {
	    int len = s.length();
	    byte[] data = new byte[len / 2];
	    for (int i = 0; i < len; i += 2) {
	        data[i / 2] = (byte) ((Character.digit(s.charAt(i), 16) << 4)
	                             + Character.digit(s.charAt(i+1), 16));
	    }
	    return data;
	}
	/*
	public static void main(String args[]) {
		String test = "HALT";
		char[] test2= test.toCharArray();
		
		try {
			byte[] bytes;
			//bytes = test.getBytes("UTF-16BE"); // big edian
			bytes = test.getBytes("UTF-8");
			String hex = bytesToHex(bytes);
			byte hexBytes[] = hexStringToByteArray(hex);
			try {
				byte hexLibraryBytes[] = Hex.decodeHex(hex);
				for(int i=0; i< hexLibraryBytes.length; i++) {
					System.out.println(hexLibraryBytes[i]);
				}
			} catch (DecoderException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
			System.out.println(Arrays.toString(bytes));
			System.out.println(bytes.length);
			System.out.println(hex);
			
		} catch (UnsupportedEncodingException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		
		
		int a = 0xFFFFFFFF;
		int b = Integer.parseUnsignedInt("FFFFFFFF",16);
		System.out.println(Integer.toUnsignedString(b));
		
		//System.out.println()
		
	}
	
*/	
}
