
// ideja dar daryti musu atminti is string tiesiog o ne char butu lengviau parsinti
public class VM {
	private Word memory[][];
	private char SP;
	private int PC; // int nes nera unsigned short 
	private char SF; // cia bitus kazkaip reikes nustatinet
	private char DS; // vietoj char gallima naudot short reikes ziuret kaip patogiau galbut short patogiau
	private char CS; // short o ne byte nes nesamone su signed unsigned javoj PC galimai reiktu int nes jam reikia 2 baitu
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
		CS=0x70;
		PC=0;
		SF=0;
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
	
	
	public boolean processCommand(RM rm) { // true jei pavyko ivykdyti komanda false jei ne
		int startingAddress = CS;
		Word startingPosition = memory[startingAddress/16][startingAddress%16];   // TODO memory atskira parasyti klase su metodais manau
		System.out.println(startingPosition);
		if(rm.getSI()==7) { // programos pabaigos kodas is karto kazin ar false reikia
			return false;
		}
		switch (String.valueOf(startingPosition.getBytes())) {
			case "ADD ":  // arba pakeisti kad visos butu 4 daliklio ilgumo
				PC++;
				int stack = Integer.parseInt(String.valueOf(SP));
				int stackAddress = Integer.parseInt(String.valueOf(SP));
				Word pirmas = memory[stack/16][stack%16];
				Word antras = memory[(stack-1)/16][(stack-1)%16];
				int suma = pirmas.toInt() + antras.toInt();
				// TODO flagu nustatymai pagal rezultata;
				Word toPut = new Word(Integer.toHexString(suma).toCharArray());
				memory[(stack-1)/16][(stack-1)%16]=toPut;
				SP--;	
				return true;
			
		}
		
		
		return true;
	}
	

}
