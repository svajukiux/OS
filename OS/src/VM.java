
// ideja dar daryti musu atminti is string tiesiog o ne char butu lengviau parsinti
public class VM {
	private Word memory[][];
	private short SP;
	private short PC;
	private short SF;
	private short DS;
	private short CS; // short o ne byte nes nesamone su signed unsigned javoj PC galimai reiktu int nes jam reikia 2 baitu
	//private SharedMemoryManager;
	
	
	public VM() {
		// atminties uzkrovimas nuliais (kiekviena baita) 16 bloku 16 zodziu 4 baitai zodyje
		memory = new Word[16][16];
		for(int i=0; i<16; ++i) {
			for(int j=0; j<16; ++j) {
					memory[i][j]= new Word(); // default konstruktorius sukuria su nuliais
				
			}
		}
		SP=0xE0; // cia prob negerai nes ne tokie bus bent jau su paging mechanizmu prob( paduot turbut reiktu i konstruktoriu)
		DS=0x00;
		CS=0x07;
		PC=0;
		SF=0;
	}


	public Word[][] getMemory() {
		return memory;
	}


	public void setMemory(Word[][] memory) {
		this.memory = memory;
	}


	public short getSP() {
		return SP;
	}


	public void setSP(short sP) {
		SP = sP;
	}


	public short getPC() {
		return PC;
	}


	public void setPC(short pC) {
		PC = pC;
	}


	public short getSF() {
		return SF;
	}


	public void setSF(short sF) {
		SF = sF;
	}


	public short getDS() {
		return DS;
	}


	public void setDS(short dS) {
		DS = dS;
	}


	public short getCS() {
		return CS;
	}


	public void setCS(short cS) {
		CS = cS;
	}
	
	

}
