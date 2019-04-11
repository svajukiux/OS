import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.util.ArrayList;

// ideja dar daryti musu atminti is string tiesiog o ne char butu lengviau parsinti
public class VM {
	private Word memory[][];
	private char SP;
	private int PC; // int nes nera unsigned short 
	private char SF; // cia bitus kazkaip reikes nustatinet
	private char DS; // vietoj char gallima naudot short reikes ziuret kaip patogiau galbut short patogiau
	private char CS; // short o ne byte nes nesamone su signed unsigned javoj PC galimai reiktu int nes jam reikia 2 baitu
	private int vmIndex;
	//private SharedMemoryManager;
	
	
	public int getVmIndex() {
		return vmIndex;
	}


	public void setVmIndex(int vmIndex) {
		this.vmIndex = vmIndex;
	}


	public VM() {
		// atminties uzkrovimas nuliais (kiekviena baita) 16 bloku 16 zodziu 4 baitai zodyje
		memory = new Word[16][16];
		for(int i=0; i<16; ++i) {
			for(int j=0; j<16; ++j) {
					memory[i][j]= new Word(); // default konstruktorius sukuria su nuliais
				
			}
		}
		SP=0xDF; // top = -1
		DS=0x00;
		CS=0x70;
		PC=(int)CS; //  i CS pakeisti reike visur 
		SF=0;
		
		//System.out.println("DS: " +DS);
	}


	public Word[][] getMemory() {
		return memory;
	}


	public void setMemory(Word[][] memory) {
		this.memory = memory;
	}


	public char getSP() {
		return SP;
	}


	public void setSP(char sP) {
		SP = sP;
	}


	public int getPC() {
		return PC;
	}


	public void setPC(int pC) {
		PC = pC;
	}


	public char getSF() {
		return SF;
	}


	public void setSF(char sF) {
		SF = sF;
	}


	public char getDS() {
		return DS;
	}


	public void setDS(char dS) {
		DS = dS;
	}


	public char getCS() {
		return CS;
	}


	public void setCS(char cS) {
		CS = cS;
	}
	
	
	
	public void assignMemoryBlock(RM rm, int virtualNumber,int realNumber){
		Word[][] realMemory = rm.getMemory();
		//System.out.println("Number " + realNumber +" "+ virtualNumber);
		this.memory[virtualNumber]=realMemory[realNumber];
	}
	
	public void setSF(boolean CF, boolean ZF, boolean OF) {
		SF = (char) (CF==true ? (SF | (1 << 0)) : (SF & ~(1 << 0)));
		SF = (char) (ZF==true ? (SF | (1 << 1)) : (SF & ~(1 << 1)));
		SF = (char) (OF==true ? (SF | (1 << 2)) : (SF & ~(1 << 2)));
	}

}
