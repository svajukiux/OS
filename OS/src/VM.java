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
		int startingAddress = CS+PC;
		Word startingPosition = memory[startingAddress/16][startingAddress%16];   // TODO memory atskira parasyti klase su metodais manau
		System.out.println(startingPosition);
		if(rm.getSI()==7) { // programos pabaigos kodas is karto kazin ar false reikia
			return false;
		}
		String command = String.valueOf(startingPosition.getBytes());
		switch (command) {
			case "ADD ": {  // arba pakeisti kad visos butu 4 daliklio ilgumo 
				PC++;
				int stack = Integer.parseInt(String.valueOf(SP),16);
				//int stackAddress = Integer.parseInt(String.valueOf(SP));
				Word pirmas = memory[stack/16][stack%16];
				Word antras = memory[(stack-1)/16][(stack-1)%16];
				
				int suma = pirmas.toInt() + antras.toInt();
				boolean CF=false,OF=false,ZF = false;
				if(suma==0) {
					ZF=true;
				}
				if(suma < pirmas.toInt() || suma < antras.toInt()) {
					CF= true;
					OF= true; 
				}
				setSF(CF,ZF,OF);
				
				Word toPut = new Word(Integer.toHexString(suma).toCharArray());
				memory[(stack-1)/16][(stack-1)%16]=toPut;
				SP--;	
				return true;
			}
				
			case "SUB ": {
				PC++;
				int stack = Integer.parseInt(String.valueOf(SP),16);
				//int stackAddress = Integer.parseInt(String.valueOf(SP));
				Word pirmas = memory[stack/16][stack%16];
				Word antras = memory[(stack-1)/16][(stack-1)%16];
				int skirtumas = antras.toInt() - pirmas.toInt();
				boolean CF=false,OF=false,ZF = false;
				if(skirtumas==0) {
					ZF=true;
				}
				if(antras.toInt() < pirmas.toInt()) {
					CF= true;
					 
				}
				if(skirtumas > antras.toInt()) {
					OF=true;
				}
				setSF(CF,ZF,OF);
				
				Word toPut = new Word(Integer.toHexString(skirtumas).toCharArray());
				memory[(stack-1)/16][(stack-1)%16]=toPut;
				SP--;	
				return true;
			}
			case "MUL ": {
				PC++;
				int stack = Integer.parseInt(String.valueOf(SP),16);
				//int stackAddress = Integer.parseInt(String.valueOf(SP));
				Word pirmas = memory[stack/16][stack%16];
				Word antras = memory[(stack-1)/16][(stack-1)%16];
				int sandauga = pirmas.toInt() * antras.toInt();
				boolean CF=false,OF=false,ZF = false;
				if(sandauga==0) {
					ZF=true;
				}
				if(sandauga < pirmas.toInt() || sandauga < antras.toInt()) {
					CF= true;
					OF=true;
					 
				}
				
				setSF(CF,ZF,OF);
				
				Word toPut = new Word(Integer.toHexString(sandauga).toCharArray());
				memory[(stack-1)/16][(stack-1)%16]=toPut;
				SP--;	
				return true;
				
			}
			case "DIV ": {
				PC++;
				int stack = Integer.parseInt(String.valueOf(SP),16);
				//int stackAddress = Integer.parseInt(String.valueOf(SP));
				Word pirmas = memory[stack/16][stack%16];
				Word antras = memory[(stack-1)/16][(stack-1)%16];
				int dalyba = antras.toInt() / pirmas.toInt();
				boolean CF=false,OF=false,ZF = false;
				if(dalyba==0) {
					ZF=true;
				}
				if(antras.toInt() < pirmas.toInt()) {
					CF= true;
					OF=true;
					 
				}
				
				setSF(CF,ZF,OF);
				
				Word toPut = new Word(Integer.toHexString(dalyba).toCharArray());
				memory[(stack-1)/16][(stack-1)%16]=toPut;
				SP--;	
				return true;
			}
			case "CMP ": {
				PC++;
				int stack = Integer.parseInt(String.valueOf(SP),16);
				//int stackAddress = Integer.parseInt(String.valueOf(SP));
				Word pirmas = memory[stack/16][stack%16];
				Word antras = memory[(stack-1)/16][(stack-1)%16];
				boolean CF=false,OF=false,ZF = false;
				if(antras.toInt()==pirmas.toInt()) {
					ZF=true;
				}
				if(antras.toInt()<pirmas.toInt()) {
					CF=true;
				}
				/// TODO kiekvienoj komandoj timeri decrementint
				setSF(CF,ZF,OF);
				
				return true;
			}
			
			case "PSH ": {
				PC++; // nes sekantis zodis argumentas skaicius
				//int skaicius = memory[(CS+PC)/16][(CS+PC)%16].toInt();
				SP++;
				int stack = Integer.parseInt(String.valueOf(SP),16);
				memory[stack/16][stack%16]=memory[(CS+PC)/16][(CS+PC)%16];
				PC++; // prieiti prie kitos komandos
				//number
				return true;
				
			}
			
			case "PPOP": {
				rm.setMODE('1');
				rm.setSI(4);
				rm.setCHByte(1); // 1asis channel (nuo nulio skaiciuojame)
				int stack = Integer.parseInt(String.valueOf(SP),16);
				System.out.print(memory[stack/16][stack%16]);
				SP--;
				return true;
				
			}
			
			case "IPSH": {
				rm.setMODE('1');
				rm.setSI(3);
				rm.setCHByte(0);
				SP++;
				int stack = Integer.parseInt(String.valueOf(SP),16);
				BufferedReader br = new BufferedReader(new InputStreamReader(System.in));
				try {
					String s = br.readLine();
					int length =s.length();
					if(length>4) {
						System.out.println("Klaida. Ivestis ilgesne nei 4 baitai");
					}
					
					else if(length==4) {
							memory[stack/16][stack%16]= new Word(s.toCharArray());
						}
					else if(length<4) {
						//int left=length;
						int position = 4-length;
						Word newWord = new Word();
						for(int i=0;i<length; i++) {
							newWord.setByte(position, s.charAt(i));
							position++;
						}
						memory[stack/16][stack%16]= new Word(newWord);
					}
					
				} catch (IOException e) {
					// TODO Auto-generated catch block
					e.printStackTrace();
				}
				
				return true;
				
				
			}
			case "HALT": {
				System.out.println("Programos pabaiga");
				// decr timer 
				// pertraukimas
				PC++;
				return true;
			}
				
			
		}
		
		if(command.startsWith("WR")) {
			PC++;
			int sharedBlockNr=Integer.parseInt(command.substring(2, 3));
			int sharedBlockWord = Integer.parseInt(command.substring(3,4));
			// TODO patikrinimas su RM ar uzrakintas blokas su numeriu sharedBlockNr
			int stack = Integer.parseInt(String.valueOf(SP),16);
			Word[][] rmMemory=rm.getMemory();
			int address=(60+sharedBlockNr-1)*16+(sharedBlockWord-1);
			rmMemory[address/16][address%16]= memory[stack/16][stack%16];
			SP--;// ? ar reikia?
			return true;
		}
		
		if(command.startsWith("RD")) {
			PC++;
			int sharedBlockNr=Integer.parseInt(command.substring(2, 3));
			int sharedBlockWord = Integer.parseInt(command.substring(3,4));
			// TODO patikrinimas su RM ar uzrakintas blokas su numeriu sharedBlockNr
			SP++;// ? ar reikia?
			int stack = Integer.parseInt(String.valueOf(SP),16);
			Word[][] rmMemory=rm.getMemory();
			int address=(60+sharedBlockNr-1)*16+(sharedBlockWord-1); // ta 60 kazkaip apsirasyt reiktu aka kur prasideda bendra atmintis
			memory[stack/16][stack%16]= rmMemory[address/16][address%16];
			return true;
		}
		
		if(command.startsWith("LC")) {
			int sharedBlockNr = Integer.parseInt(command.substring(2, 3));
			int vmNumber = Integer.parseInt(command.substring(3, 4)); // arba taip arba kazkoki turet auto nr priskyreja nereiktu i komanda rasyt
			Word[][] rmMemory=rm.getMemory();
			if(rmMemory[63][sharedBlockNr-1].toInt()==0) { // reiktu turbut while nes juk turetu laukti kol atsiblokuos
				rmMemory[63][sharedBlockNr-1]= new Word().fromInt(vmNumber);// 63 last blokas kuriame sudeta info apie sharedmemory
			}
			else {
				System.out.println("Jau uzrakina");
			}
			PC++;
			return true;
		}
		
		if(command.startsWith("UC")) {
			int sharedBlockNr = Integer.parseInt(command.substring(2, 3));
			int vmNumber = vmIndex;// kuriant vm sugeneruoti
			Word[][] rmMemory=rm.getMemory();
			int intValue= rmMemory[63][sharedBlockNr-1].toInt(); // reiksme zodzio su info
			if(intValue!=0 && intValue==vmNumber ) { // reiktu turbut while nes juk turetu laukti kol atsiblokuos
				rmMemory[63][sharedBlockNr-1]= new Word();// 63 last blokas kuriame sudeta info apie sharedmemory
			}
			else if(intValue!=0 && intValue!=vmNumber) {
				System.out.println("Si masina negali atblokuoti sio zodzio"); // turut reikt pertraukimo?
			}
			else if(intValue==0) {
				System.out.println("Sritis jau atblokuota");	
			}
			PC++;
			return true;
			
		}
		
		if(command.startsWith("LD")) {
			
			String stringAddress = command.substring(2,4);
			int address = Integer.parseInt(stringAddress,16);
			SP++;
			int stack = Integer.parseInt(String.valueOf(SP),16);
			memory[stack/16][stack/16]=memory[address/16][address%16];
			PC++;
			return true;
		}
		
		if(command.startsWith("PT")) {
			String stringAddress = command.substring(2,4);
			int address = Integer.parseInt(stringAddress,16);
			int stack = Integer.parseInt(String.valueOf(SP),16);
			memory[address/16][address%16]=memory[stack/16][stack/16];
			SP--;
			PC++;
			return true;
		}
		
		if(command.startsWith("JP")) {
			int posl = Integer.parseInt(command.substring(2,4),16); // nera tikrinimo geru reiksmiu 
			PC=CS+posl;
			return true;
		}
		if(command.startsWith("JE")){
			char tempSF=SF;
			if ((tempSF & 0b00000100) == 0b00000100){ // ZF=1
				int posl = Integer.parseInt(command.substring(2,4),16);
				PC=CS+posl;
				return true;
			}
		}
		if(command.startsWith("JB")) {
			char tempSF=SF;
			if((tempSF & 0b00000001) == 0b000000001) { // CF=1
				int posl = Integer.parseInt(command.substring(2,4),16);
				PC=CS+posl;
				return true;
			
			}
		}
		if(command.startsWith("JA")){ // ZF = 0 and CF = 0
			char tempSF=SF;
			if((tempSF & 0b00000101) == 0b000000000) {
				int posl = Integer.parseInt(command.substring(2,4),16);
				PC=CS+posl;
				return true;
				
			}
		}
		if(command.startsWith("JN")) {
			char tempSF=SF;
			if((tempSF & 0b00000100) == 0b000000000) { // ZF=0
				int posl = Integer.parseInt(command.substring(2,4),16);
				PC=CS+posl;
				return true;
				
			}
		}
		
		if(command.startsWith("RI")) {
			rm.setMODE('1'); // supervizoriaus
			// kolkas skaitymas cia bet reiks perkelti prie RM
			rm.setSI(1);
			rm.setCHByte(0); // uzsetinam 0lini channel
			int stack = Integer.parseInt(String.valueOf(SP),16);
			int numberOfBytes = memory[stack/16][stack%16].toInt();
			SP--;
			String s;
			BufferedReader br = new BufferedReader(new InputStreamReader(System.in));
			try {
				s= br.readLine();
				if(s.length()!=numberOfBytes) {
					System.out.println("Baitu skaicius neatitinka skaicaus steko virsuneje");
				}
				int startingPlace=Integer.parseInt(command.substring(2,4),16);
				
				//if(numberOfBytes%4==0) {
					//int wordCount=numberOfBytes/4;
					int doneBytes=0;
					ArrayList<Word> words = new ArrayList<>();
					while(numberOfBytes-4 >=0) {
						
						Word word = new Word(s.substring(0+doneBytes,4+doneBytes).toCharArray());
						memory[startingPlace/16][startingPlace%16]=new Word(word);
						startingPlace++;
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
					loadDataAsBytes(words,startingPlace,this);
				//}
				
				//for(int i=0; i<numberOfBytes; i++) {
				//	memory[startingPlace/16][startingPlace%16] // visur kur data reikia DS + statingPlace pvz nes nurodom tik poslinki
				//}
			} catch (IOException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
			rm.setMODE('0'); // supervizoriaus
			
			rm.setSI(0);
			rm.unsetCHByte(0); // uzsetinam 0lini channel
			
			return true;
			
			
		}
		
		if(command.startsWith("PR")) {
			rm.setMODE('1');
			rm.setSI(2);
			rm.setCHByte(1);
			int stack = Integer.parseInt(String.valueOf(SP),16);
			int numberOfBytes = memory[stack/16][stack%16].toInt();
			int startingPlace=Integer.parseInt(command.substring(2,4),16);
			
			SP--;
			int tempi=0;
			for(int i=0; i<numberOfBytes; i++) {
				System.out.print(memory[startingPlace/16][startingPlace%16].getByte(tempi));
				tempi++;
				if(tempi==3) {
					tempi=0;
					startingPlace++;
					System.out.println("");
				}
			}
			rm.setMODE('0');
			rm.setSI(0);
			rm.unsetCHByte(1);
			return true;
		}
		
		
		
		
		
		
		
		return true;
	}
	
	
	private void setSF(boolean CF, boolean ZF, boolean OF) {
		SF = (char) (CF==true ? (SF | (1 << 0)) : (SF & ~(1 << 0)));
		SF = (char) (ZF==true ? (SF | (1 << 1)) : (SF & ~(1 << 1)));
		SF = (char) (OF==true ? (SF | (1 << 2)) : (SF & ~(1 << 2)));
	}
	
	private void loadDataAsBytes(ArrayList<Word> words, int address, VM vm) {
		for(int i=0; i<words.size(); i++) {
			for(int j=0; j<4; j++) {
				char currentByte = words.get(i).getByte(j);
				
				if(currentByte!='\u0000') {
					vm.getMemory()[address/16][address%16].setByte(j,currentByte);
				}
				
			}
		}
		
	}
	

}
