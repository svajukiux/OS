import java.io.BufferedReader;
import java.io.File;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.UnsupportedEncodingException;
import java.util.ArrayList;
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
    
    private SharedMemoryManager manager;
    private ProgramParser parser;
    private int lastCreatedVm;
    //private VM[] vms = {null, null, null};
    
	public SharedMemoryManager getManager() {
		return manager;
	}

	public void setManager(SharedMemoryManager manager) {
		this.manager = manager;
	}

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
		case 5: return 8; // command LCx
		case 6: return 9; // command UCx
		case 7: return 10; // command HALT
		}
		
		if(TI==0) {
			return 11; // timer interrupt
		}
		
		return 0;
	}
	
	public int processInterrupt(VM vm) {
		switch(getInterrupt()) {
		
		case 1:{
			System.out.println("Out of bounds");
			return 1;
		}
		case 2: {
			System.out.println("Bad operation code"); // jau yra
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
			// LCxy
			boolean blokavimas =manager.LC(this,vm);
			if(blokavimas==true) { // jei pavyko
				break;
			}
			else {
				return 1; // jei nepavyko baigiame darba
			}
		}
		case 9: {
			// UCx
			boolean atblokavimas = manager.UC(this,vm);
			if(atblokavimas==true) { // jei pavyko
				break;
			}
			else {
				return 1; // jei nepavyko 
			}
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
		SF = '0';
		lastCreatedVm=0; 
		manager = new SharedMemoryManager(60,3); // bendra atmintis prasideda 60 bloke ir yra 3 bendri blokai
		memory = new Word[64][16];
		for(int i=0; i<64; ++i) {
			for(int j=0; j<16; ++j) {
				
				memory[i][j]= new Word();	
				
			}
		}
		parser = new ProgramParser();
	}
	
	public VM loadProgram(File file) {
		VM vm = new VM();
		this.lastCreatedVm+=1;
		vm.setVmIndex(lastCreatedVm); // suteikiam vm numeri
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
			
			try {
				parser.parseFile(file, vm);
				this.PC=vm.getPC(); // vel nzn cia gal turi buti PC kur reali masina rodo
				this.SF= vm.getSF();
				this.SP = vm.getSP();
			} catch (IOException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
		return vm;	
		
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
	
	public String processCommand(VM vm) { // return processed command
		int startingAddress = (int)vm.getCS()+(int)PC;
		Word[][] vmMemory = vm.getMemory(); // gauname subindinta puslapiu mechanizmu virtualia atminti
		Word startingPosition = vmMemory[startingAddress/16][startingAddress%16];   // TODO memory atskira parasyti klase su metodais manau
		System.out.println("startingAddress " +startingAddress);
		//if(rm.getSI()==7) { // programos pabaigos kodas is karto kazin ar false reikia
		//	
		//	return "HALT";
		//}
		String command = String.valueOf(startingPosition.getBytes());
		switch (command) {
			case "ADD ": {  // arba pakeisti kad visos butu 4 daliklio ilgumo 
				PC++;
				int stack = (int)SP;//Integer.parseInt(String.valueOf(SP),16);
				//int stackAddress = Integer.parseInt(String.valueOf(SP));
				Word pirmas = vmMemory[stack/16][stack%16];
				Word antras = vmMemory[(stack-1)/16][(stack-1)%16];
				
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
				
				return command;
			}
				
			case "SUB ": {
				PC++;
				int stack = (int)SP;//Integer.parseInt(String.valueOf(SP),16);
				//int stackAddress = Integer.parseInt(String.valueOf(SP));
				Word pirmas = vmMemory[stack/16][stack%16];
				Word antras = vmMemory[(stack-1)/16][(stack-1)%16];
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
				vmMemory[(stack-1)/16][(stack-1)%16]=toPut;
				SP--;	
				return command;
			}
			case "MUL ": {
				PC++;
				int stack = (int)SP;//Integer.parseInt(String.valueOf(SP),16);
				//int stackAddress = Integer.parseInt(String.valueOf(SP));
				Word pirmas = vmMemory[stack/16][stack%16];
				Word antras = vmMemory[(stack-1)/16][(stack-1)%16];
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
				vmMemory[(stack-1)/16][(stack-1)%16]=toPut;
				SP--;	
				return command;
				
			}
			case "DIV ": {
				PC++;
				int stack = (int)SP;//Integer.parseInt(String.valueOf(SP),16);
				//int stackAddress = Integer.parseInt(String.valueOf(SP));
				Word pirmas = vmMemory[stack/16][stack%16];
				Word antras = vmMemory[(stack-1)/16][(stack-1)%16];
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
				vmMemory[(stack-1)/16][(stack-1)%16]=toPut;
				SP--;	
				return command;
			}
			case "CMP ": {
				PC++;
				int stack = (int)SP;//Integer.parseInt(String.valueOf(SP),16);
				//int stackAddress = Integer.parseInt(String.valueOf(SP));
				Word pirmas = vmMemory[stack/16][stack%16];
				Word antras = vmMemory[(stack-1)/16][(stack-1)%16];
				boolean CF=false,OF=false,ZF = false;
				if(antras.toInt()==pirmas.toInt()) {
					ZF=true;
				}
				if(antras.toInt()<pirmas.toInt()) {
					CF=true;
				}
				/// TODO kiekvienoj komandoj timeri decrementint
				setSF(CF,ZF,OF);
				return command;
			}
			
			case "PSH ": {
				PC++; // nes sekantis zodis argumentas skaicius
				String skaicius = vmMemory[(vm.getCS()+PC)/16][(vm.getCS()+PC)%16].toString(); // grazina hex string
				command.concat(skaicius);
				SP++;
				int stack = (int) SP;//Integer.parseInt(String.valueOf(SP),16);
				vmMemory[stack/16][stack%16]=memory[(vm.getCS()+PC)/16][(vm.getCS()+PC)%16];
				PC++; // prieiti prie kitos komandos
				//number
				return command;
				
			}
			
			case "PPOP": {
				setMODE('1');
				setSI(4);
				setCHByte(1); // 1asis channel (nuo nulio skaiciuojame)
				int stack = (int)SP;//Integer.parseInt(String.valueOf(SP),16);
				System.out.print(vmMemory[stack/16][stack%16]);
				SP--;
				return command;
				
			}
			
			case "IPSH": {
				setMODE('1');
				setSI(3);
				setCHByte(0);
				SP++;
				int stack = (int)SP;//Integer.parseInt(String.valueOf(SP),16);
				BufferedReader br = new BufferedReader(new InputStreamReader(System.in));
				try {
					String s = br.readLine();
					int length =s.length();
					if(length>4) {
						System.out.println("Klaida. Ivestis ilgesne nei 4 baitai");
					}
					
					else if(length==4) {
						vmMemory[stack/16][stack%16]= new Word(s.toCharArray());
						}
					else if(length<4) {
						//int left=length;
						int position = 4-length;
						Word newWord = new Word();
						for(int i=0;i<length; i++) {
							newWord.setByte(position, s.charAt(i));
							position++;
						}
						vmMemory[stack/16][stack%16]= new Word(newWord);
					}
					
				} catch (IOException e) {
					// TODO Auto-generated catch block
					e.printStackTrace();
				}
				return command;
				
				
			}
			case "HALT": {
				System.out.println("Programos pabaiga");
				setSI(10);
				// decr timer 
				// pertraukimas
				PC++;
				return command;
			}
				
			
		}
		
		if(command.startsWith("WR")) {
			PC++;
			int sharedBlockNr=Integer.parseInt(command.substring(2, 3));
			int sharedBlockWord = Integer.parseInt(command.substring(3,4));
			// TODO patikrinimas su RM ar uzrakintas blokas su numeriu sharedBlockNr
			int stack = (int)SP;//Integer.parseInt(String.valueOf(SP),16);
			//Word[][] rmMemory=rm.getMemory();
			int address=(60+sharedBlockNr-1)*16+(sharedBlockWord-1);
			memory[address/16][address%16]= vmMemory[stack/16][stack%16];
			SP--;// ? ar reikia?
			return command;
		}
		
		if(command.startsWith("RD")) {
			PC++;
			int sharedBlockNr=Integer.parseInt(command.substring(2, 3));
			int sharedBlockWord = Integer.parseInt(command.substring(3,4));
			// TODO patikrinimas su RM ar uzrakintas blokas su numeriu sharedBlockNr
			SP++;// ? ar reikia?
			int stack = (int)SP;//Integer.parseInt(String.valueOf(SP),16);
			//Word[][] rmMemory=rm.getMemory();
			int address=(60+sharedBlockNr-1)*16+(sharedBlockWord-1); // ta 60 kazkaip apsirasyt reiktu aka kur prasideda bendra atmintis
			vmMemory[stack/16][stack%16]= memory[address/16][address%16];
			return command;
		}
		
		if(command.startsWith("LC")) {
			setMODE('1'); // supervisor mode
			setSI(5); // LC pertraukimas lock bendra atminti
			//int sharedBlockNr = Integer.parseInt(command.substring(2, 3));
			//rm.getManager().setBlockNr(sharedBlockNr);
			getManager().setVmIndex(vm.getVmIndex());
			// vm numerio komandoje nebera int vmNumber = Integer.parseInt(command.substring(3, 4)); // arba taip arba kazkoki turet auto nr priskyreja nereiktu i komanda rasyt
			PC++;
			vm.setPC(PC);
			//command.concat(Integer.toString(vm.getVmIndex())); // gale bus nr vm
			return command;
		}
		
		if(command.startsWith("UC")) {
			int sharedBlockNr = Integer.parseInt(command.substring(2, 3));
			int vmNumber = vm.getVmIndex();// kuriant vm sugeneruoti
			//Word[][] rmMemory=rm.getMemory();
			int intValue= memory[63][sharedBlockNr-1].toInt(); // reiksme zodzio su info
			if(intValue!=0 && intValue==vmNumber ) { // reiktu turbut while nes juk turetu laukti kol atsiblokuos
				memory[63][sharedBlockNr-1]= new Word();// 63 last blokas kuriame sudeta info apie sharedmemory
			}
			else if(intValue!=0 && intValue!=vmNumber) {
				System.out.println("Si masina negali atblokuoti sio zodzio"); // turut reikt pertraukimo?
			}
			else if(intValue==0) {
				System.out.println("Sritis jau atblokuota");	
			}
			PC++;
			return command;
			
		}
		
		if(command.startsWith("LD")) {
			
			String stringAddress = command.substring(2,4);
			int poslinkis = Integer.parseInt(stringAddress,16);
			int dsValue = (int)vm.getDS();
			//SP= vm.getSP();
			SP++;
			//vm.setSP(SP);
			int stack = (int)SP;//Integer.parseInt(String.valueOf(SP),16);
			System.out.println(" Stack: " +stack);
			vmMemory[stack/16][stack%16]=vmMemory[dsValue+poslinkis/16][dsValue+poslinkis%16]; 
			PC++;
			return command;
		}
		
		if(command.startsWith("PT")) {
			String stringAddress = command.substring(2,4);
			int address = Integer.parseInt(stringAddress,16);
			int dsValue = (int)vm.getDS();
			int stack = (int)SP;//Integer.parseInt(String.valueOf(SP),16);
			vmMemory[dsValue+address/16][dsValue+address%16]=vmMemory[stack/16][stack/16];
			SP--;
			PC++;
			return command;
		}
		
		if(command.startsWith("JP")) {
			int posl = Integer.parseInt(command.substring(2,4),16); // nera tikrinimo geru reiksmiu 
			PC=posl;
			return command;
		}
		if(command.startsWith("JE")){
			char tempSF=SF;
			if ((tempSF & 0b00000100) == 0b00000100){ // ZF=1
				int posl = Integer.parseInt(command.substring(2,4),16);
				PC=posl;
			}
			return command;
		}
		if(command.startsWith("JB")) {
			char tempSF=SF;
			if((tempSF & 0b00000001) == 0b000000001) { // CF=1
				int posl = Integer.parseInt(command.substring(2,4),16);
				PC=posl; // DS+posl;
				
			
			}
			return command;
		}
		if(command.startsWith("JA")){ // ZF = 0 and CF = 0
			char tempSF=SF;
			if((tempSF & 0b00000101) == 0b000000000) {
				int posl = Integer.parseInt(command.substring(2,4),16);
				PC=posl;
				
				
			}
			return command;
		}
		if(command.startsWith("JN")) {
			char tempSF=SF;
			System.out.println("SF "+(int)SF);
			if((tempSF & 0b00000100) == 0b000000000) { // ZF=0
				int posl = Integer.parseInt(command.substring(2,4),16);
				PC=posl;
				
				
			}
			return command;
		}
		
		if(command.startsWith("RI")) {
			int dsValue = (int)vm.getDS();
			setMODE('1'); // supervizoriaus
			// kolkas skaitymas cia bet reiks perkelti prie RM
			setSI(1);
			setCHByte(0); // uzsetinam 0lini channel
			int stack = (int)SP;//Integer.parseInt(String.valueOf(SP),16);
			int numberOfBytes = vmMemory[stack/16][stack%16].toInt();
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
						vmMemory[dsValue+startingPlace/16][dsValue+startingPlace%16]=new Word(word);
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
					loadDataAsBytes(words,startingPlace,vm);
				//}
				
				//for(int i=0; i<numberOfBytes; i++) {
				//	memory[startingPlace/16][startingPlace%16] // visur kur data reikia DS + statingPlace pvz nes nurodom tik poslinki
				//}
			} catch (IOException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
			setMODE('0'); // supervizoriaus
			
			setSI(0);
			unsetCHByte(0); // uzsetinam 0lini channel
			
			return command;
			
		}
		
		if(command.startsWith("PR")) {
			int dsValue = (int)vm.getDS();
			setMODE('1');
			setSI(2);
			setCHByte(1);
			int stack = (int)SP;// Integer.parseInt(String.valueOf(SP),16);
			int numberOfBytes = vmMemory[stack/16][stack%16].toInt();
			int startingPlace=Integer.parseInt(command.substring(2,4),16);
			
			SP--;
			int tempi=0;
			for(int i=0; i<numberOfBytes; i++) {
				System.out.print(vmMemory[dsValue+startingPlace/16][dsValue+startingPlace%16].getByte(tempi));
				tempi++;
				if(tempi==3) {
					tempi=0;
					startingPlace++;
					System.out.println("");
				}
			}
			setMODE('0');
			setSI(0);
			unsetCHByte(1);
			return command;
		}
		
		setPI(2);
		return command; // kazkokia bad komanda grazins
		
		
		
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
	
	private void setSF(boolean CF, boolean ZF, boolean OF) {
		SF = (char) (CF==true ? (SF | (1 << 0)) : (SF & ~(1 << 0)));
		SF = (char) (ZF==true ? (SF | (1 << 1)) : (SF & ~(1 << 1)));
		SF = (char) (OF==true ? (SF | (1 << 2)) : (SF & ~(1 << 2)));
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


