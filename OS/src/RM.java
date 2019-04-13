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
    private OutputDevice outputDevice;
    private InputDevice inputDevice;
    private int lastCreatedVm;
    private ChannelDevice channelDevice;
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
		CH[position]='1';
	}
	public void unsetCHByte(int position) {
		CH[position]='0';
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
			case 4: return 12; // bendros atminties pazeidimas
			
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
			System.out.println("Bad operation code"); // jau yra kas kviecia
			return 1;
		}
		
		case 3:{
			System.out.println("Negeras priskyrimas"); 
			return 1;
		}
		
		case 4: {
			// perkelt readinima
			MODE='1'; // supervizoriaus
			
			setCHByte((char)0);
			Word[][] vmMemory = vm.getMemory();
			int vmPC=vm.getPC();
			String command = vmMemory[(vm.getPC()-1)/16][(vm.getPC()-1)%16].toString();
			String stringPosl=command.substring(2,4);
			
			
			//stack--; // kodel?
			
			 // 2 paskutiniai sk
			for(int i=0; i<2; i++) {
				if(stringPosl.charAt(i)<'0' || stringPosl.charAt(i)>'F' || (stringPosl.charAt(i)>'9' && stringPosl.charAt(i)<'A') ) {
					setPI(3);
					vmPC++;
					vm.setPC(vmPC);
					PC=vmPC;
					decrTimer(1);
					//break;
					return 0;
				}
			}
			int posl = Integer.parseInt(command.substring(2,4),16);
			if(posl>=0x70 || posl <0) {
				setPI(1);
				vmPC++;
				vm.setPC(vmPC);
				PC=vmPC;
				decrTimer(1);
				return 0;
			}
			int stack = (int)vm.getSP();
			int numberOfBytes= vmMemory[stack/16][stack%16].toInt();
			stack--; // gal cia turetu buti?
			
			int address = vm.getDS()+posl;
			channelDevice.setSB(1);// kopijuojam pirma takeli// Zygimantas
            channelDevice.setDB(1);// dedam i pirma takeli// Zygimantas
            channelDevice.setST(1);// is ivedimo irenginio// Zygimantas
            channelDevice.setDT(4);// i vartotojo atminti (steka)// Zygimantas
			//ArrayList <Word> readWords = inputDevice.readBytes(numberOfBytes);
            ArrayList <Word> readWords = channelDevice.xchg(inputDevice, numberOfBytes);// vykdomas duomenu pakeitimas// Zygimantas
            for(int i=0; i<readWords.size(); i++) {
            	System.out.println("word " + readWords.get(i));
            }
			loadDataAsBytes(readWords,address,vm);
			SI=0;
			unsetCHByte(0);
			vm.setSP((char)stack);
			SP=(char)stack;
			decrTimer(3);
			MODE='0';
			
			
			
			break;
		}
		case 5: {
			// perkelt printinima
			Word[][] vmMemory = vm.getMemory();
			int vmPC = vm.getPC();
			MODE='1';
			setCHByte((char)1);
			
			
			String command = vmMemory[(vmPC-1)/16][(vmPC-1)%16].toString();
			String stringPosl = command.substring(2,4);
			for(int i=0; i<2; i++) {
				if(stringPosl.charAt(i)<'0' || stringPosl.charAt(i)>'F' || (stringPosl.charAt(i)>'9' && stringPosl.charAt(i)<'A') ) {
					setPI(3);
					vmPC++;
					vm.setPC(vmPC);
					PC=vmPC;
					decrTimer(1);
					//break;
					return 0;
				}
			}
			int posl = Integer.parseInt(command.substring(2,4),16); // posl/16 = blokas
			if(posl>=0x70 || posl <0) {
				setPI(1);
				vmPC++;
				vm.setPC(vmPC);
				PC=vmPC;
				decrTimer(1);
				return 0;
			}
			int stack = (int)vm.getSP();// Integer.parseInt(String.valueOf(SP),16);
			int numberOfBytes = vmMemory[stack/16][stack%16].toInt();
			stack--;
			
			//System.out.println("komanda " + command);
			
			char[] isvestis = new char[numberOfBytes];
			int tempi=0;
			//int posl2=posl % 16; // posl%16 = zodis
			for (int i=0; i<numberOfBytes; i++) {
				isvestis[i]=vmMemory[(posl +vm.getDS())/16][(posl + vm.getDS())%16].getByte(tempi);
				tempi++;
					if(tempi==4) {
						tempi=0;
					}
					
					if(tempi==0) {
						posl++; 
					}
					
					//else {
					//	posl++;
					//	posl2=0;
					//}
			}
			channelDevice.setSB(2);// kopijuojam antra takeli// Zygimantas
            channelDevice.setDB(2);// dedam i antra takeli// Zygimantas
            channelDevice.setST(4);// is ivedimo irenginio// Zygimantas
            channelDevice.setDT(1);// i vartotojo atminti (steka)// Zygimantas
            channelDevice.xchg(outputDevice, isvestis);// vykdomas duomenu pakeitimas// Zygimantas
			//outputDevice.printBytes(isvestis);
			SI=0;
			unsetCHByte((char)1);
			SP=(char)stack;
			vm.setSP((char)stack);
			decrTimer(3);
			//int startingPlace=Integer.parseInt(command.substring(2,4),16);
			break;
		}
		case 6: {
			// perkelt IPSH
			MODE='1';
			setCHByte((char)0);
			Word[][] vmMemory = vm.getMemory();
			int vmPC=vm.getPC();
			
			int stack = (int)vm.getSP();//Integer.parseInt(String.valueOf(SP),16);
			stack++;
			channelDevice.setSB(1);// kopijuojam pirma takeli// Zygimantas
            channelDevice.setDB(1);// dedam i pirma takeli// Zygimantas
            channelDevice.setST(1);// is ivedimo irenginio// Zygimantas
            channelDevice.setDT(4);// i vartotojo atminti (steka)// Zygimantas
            Word temp = channelDevice.xchg(inputDevice);// vykdomas duomenu pakeitimas// Zygimantas
			//Word temp = inputDevice.readWord();
			vmMemory[stack/16][stack%16] = temp;
			unsetCHByte((char)0);
			decrTimer(3);
			vm.setSP((char)stack);
			SP=(char)stack;
			MODE='0';
			SI=0;
			break;
		}
		case 7: {
			// perkelt PPOP
			
			MODE='1';
			setCHByte((char)1);
			int vmPC= vm.getPC();
			Word[][] vmMemory = vm.getMemory();
			int stack = (int)vm.getSP();//Integer.parseInt(String.valueOf(SP),16)
			channelDevice.setSB(2);// kopijuojam antra takeli// Zygimantas
            channelDevice.setDB(2);// dedam i antra takeli// Zygimantas
            channelDevice.setST(1);// is ivedimo irenginio// Zygimantas
            channelDevice.setDT(4);// i vartotojo atminti (steka)// Zygimantas
            channelDevice.xchg(outputDevice, vmMemory, stack);// vykdomas duomenu pakeitimas;
			//outputDevice.printWord(vmMemory[stack/16][stack%16]);
            stack--;
            
            
			vm.setSP((char)stack);
			SP = (char)stack;
			decrTimer(3);
			
			MODE='0';
			SI=0;
			unsetCHByte((char)1);
			break;
			
		}
		case 8: {
			// LCxy
			MODE='1';
			int vmPC = vm.getPC();
			boolean blokavimas =manager.LC(this,vm);
			if(blokavimas==true) { // jei pavyko
				
				MODE = '0';
				decrTimer(1);
				break;
			}
			else {
				return 1; // jei nepavyko baigiame darba
			}
		}
		case 9: {
			// UCx
			MODE = '1';
			int vmPC = vm.getPC();
			
			boolean atblokavimas = manager.UC(this,vm);
			if(atblokavimas==true) { // jei pavyko
				MODE='0';
				decrTimer(1);
				break;
			}
			else {
				return 1; // jei nepavyko 
			}
		}
		case 10: {
			//this.SI=0;
			//this.PI=0;
			//this.TI=10; // uzdet koki reset metoda 
			//decrTimer(1); // reikia?
			return 1; // HALT
		}
		case 11: {
			TI=10; // reset TI
			break;
		}
		case 12: {
			System.out.println("Bendros atminties pazeidimas");
			return 1;
			
		}
		
		}
		return 0;
	}

	public RM() {
		CH = new char[] {'0','0','0'};
		PTR = new Word();
		SF = 0;
		TI=10;
		lastCreatedVm=0; 
		MODE = '0';
		manager = new SharedMemoryManager(60,3); // bendra atmintis prasideda 60 bloke ir yra 3 bendri blokai
		memory = new Word[64][16]; // new byte [16*4*16*virtualiumasinukietis(pvz 5)] paskutinius 16 bloku supervizorine 
		// reikia i supervizorine uzkrauti patikrinti ir tik tada kurti virtualia masina 2 virtualias
		for(int i=0; i<64; ++i) {
			for(int j=0; j<16; ++j) {
				
				memory[i][j]= new Word();	
				
			}
		}
		parser = new ProgramParser();
		outputDevice = new OutputDevice();
		inputDevice = new InputDevice();
		channelDevice = new ChannelDevice();
	}
	
	public VM createAndLoadVirtualMachine() {
		VM vm = new VM();
		// suteikiam vm numeri
		this.lastCreatedVm+=1;
		vm.setVmIndex(lastCreatedVm);
		
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
			return vm;
	}
	
	public boolean loadProgram(File file, String program,VM vm) {
		
			try {
				//parser.hasParsedCode=false;
				//parser.parsedDataGracefully=false;
				parser.parseFile(file, vm, program);
				
				this.PC=vm.getPC(); // vel nzn cia gal turi buti PC kur reali masina rodo
				this.SF= vm.getSF();
				System.out.println("SF"+(int)vm.getSF());
				this.SP = vm.getSP();
				return true;
			} catch (IOException e) {
				// TODO Auto-generated catch block
				//vm=null;
				e.printStackTrace();
				 return false;
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
	
	public String processCommand(VM vm) { // return processed command
		//int startingAddress = (int)vm.getPC(); //(int)vm.getCS()+(int)PC;
		Word[][] vmMemory = vm.getMemory(); // gauname subindinta puslapiu mechanizmu virtualia atminti
		int vmPC = (int)vm.getPC();
		Word startingPosition = vmMemory[vmPC/16][vmPC%16];   // TODO memory atskira parasyti klase su metodais manau
		//System.out.println("startingAddress " +startingAddress);
		//if(rm.getSI()==7) { // programos pabaigos kodas is karto kazin ar false reikia
		//	
		//	return "HALT";
		//}
		String command = String.valueOf(startingPosition.getBytes());
		switch (command) {
			case "ADD ": {  // arba pakeisti kad visos butu 4 daliklio ilgumo 
				vmPC++;
				int stack = (int)vm.getSP();//Integer.parseInt(String.valueOf(SP),16);
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
				vm.setSF(CF,ZF,OF);
				Word toPut = new Word();
				int numberIndex=3;
				String result = Integer.toHexString(suma);
				for(int i=result.length()-1; i>=0; i--) {
					toPut.setByte(numberIndex,result.charAt(i));
					numberIndex--;
				}
				//toPut = new Word(Integer.toHexString(suma).toCharArray());
				vmMemory[(stack-1)/16][(stack-1)%16]=toPut;
				stack--;
				vm.setSP((char)stack);
				vm.setPC(vmPC);
				PC=vmPC;
				SP=(char)stack;
				SF= vm.getSF();
				decrTimer(1);
				
				return command;
			}
				
			case "SUB ": {
				vmPC++;
				int stack = (int)vm.getSP();//Integer.parseInt(String.valueOf(SP),16);
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
				vm.setSF(CF,ZF,OF);
				Word toPut = new Word();
				String result = Integer.toHexString(skirtumas);
				if(skirtumas<0) {
					result =result.substring(4,8);
				}
				int numberIndex=3;
				for(int i=result.length()-1; i>=0; i--) {
					toPut.setByte(numberIndex,result.charAt(i));
					numberIndex--;
				}
				//Word toPut = new Word(Integer.toHexString(skirtumas).toCharArray());
				vmMemory[(stack-1)/16][(stack-1)%16]=toPut;
				
				stack--;
				vm.setSP((char)stack);
				vm.setPC(vmPC);
				SP=(char)stack;
				SF=vm.getSF();
				PC=vmPC;
				decrTimer(1);
				return command;
			}
			case "MUL ": {
				vmPC++;
				int stack = (int)vm.getSP();//Integer.parseInt(String.valueOf(SP),16);
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
				
				Word toPut = new Word();
				String result = Integer.toHexString(sandauga);
				
				if(sandauga>0xFFFF) {
					setPI(3);
					System.out.println("Gautas didelis skaicius");
					return command;
				}
				int numberIndex=3;
				for(int i=result.length()-1; i>=0; i--) {
					toPut.setByte(numberIndex,result.charAt(i));
					numberIndex--;
				}
				
				vm.setSF(CF,ZF,OF);
				
				//Word toPut = new Word(Integer.toHexString(sandauga).toCharArray());
				vmMemory[(stack-1)/16][(stack-1)%16]=toPut;
				stack--;
				vm.setSP((char)stack);
				vm.setPC(vmPC);
				SP=(char)stack;
				SF=vm.getSF();
				PC=vmPC;
				decrTimer(1);
				return command;
				
			}
			case "DIV ": {
				vmPC++;
				int stack = (int)vm.getSP();//Integer.parseInt(String.valueOf(SP),16);
				//int stackAddress = Integer.parseInt(String.valueOf(SP));
				Word pirmas = vmMemory[stack/16][stack%16];
				Word antras = vmMemory[(stack-1)/16][(stack-1)%16];
				if(pirmas.toInt() == 0){// Zygimantas
                    setPI(3);// Zygimantas
                    
                }
				else {
					
					
					int dalyba = antras.toInt() / pirmas.toInt();
					boolean CF=false,OF=false,ZF = false;
					if(dalyba==0) {
						ZF=true;
					}
					if(antras.toInt() < pirmas.toInt()) {
						CF= true;
						OF=true;
						 
					}
					Word toPut = new Word();
					String result = Integer.toHexString(dalyba);
					
					
					int numberIndex=3;
					for(int i=result.length()-1; i>=0; i--) {
						toPut.setByte(numberIndex,result.charAt(i));
						numberIndex--;
					}
					
					//
					
					//Word toPut = new Word(Integer.toHexString(dalyba).toCharArray());
					vmMemory[(stack-1)/16][(stack-1)%16]=toPut;
					stack--;
					vm.setSF(CF,ZF,OF);
			}
				vm.setSP((char)stack);
				vm.setPC(vmPC);
				SP=(char)stack;
				SF=vm.getSF();
				PC=vmPC;
				decrTimer(1);
				return command;
			}
			case "CMP ": {
				vmPC++;
				int stack = (int)vm.getSP();//Integer.parseInt(String.valueOf(SP),16);
				//int stackAddress = Integer.parseInt(String.valueOf(SP));
				Word pirmas = vmMemory[stack/16][stack%16];
				Word antras = vmMemory[(stack-1)/16][(stack-1)%16];
				boolean CF=false,OF=false,ZF = false;
				//System.out.println("antras" + antras.toInt());
				//System.out.println("pirmas" +pirmas.toInt());
				if(antras.toInt()==pirmas.toInt()) {
					System.out.println("Lygus");
					ZF=true;
				}
				if(antras.toInt()<pirmas.toInt()) {
					CF=true;
				}
				/// TODO kiekvienoj komandoj timeri decrementint
				vm.setSF(CF,ZF,OF);
				vm.setSP((char)stack);
				vm.setPC(vmPC);
				SP=(char)stack;
				SF=vm.getSF();
				PC=vmPC;
				decrTimer(1);
				return command;
			}
			
			case "PSH ": {
				vmPC++; // nes sekantis zodis argumentas skaicius
				String skaicius = vmMemory[(vmPC)/16][(vmPC)%16].toString(); // grazina hex string
				for(int i=0; i<4; i++) {
					if(skaicius.charAt(i)<'0' || skaicius.charAt(i)>'F' || (skaicius.charAt(i)>'9' && skaicius.charAt(i)<'A')) {
						setPI(3);
						vmPC++;
						vm.setPC(vmPC);
						PC=vmPC;
						decrTimer(1);
						return command;
					}
				}
				command.concat(skaicius);
				int stack = (int) vm.getSP();
				stack++;
				
				//Integer.parseInt(String.valueOf(SP),16);
				vmMemory[stack/16][stack%16].setBytes(vmMemory[(vmPC)/16][(vmPC)%16].getBytes());
				vmPC++; // prieiti prie kitos komandos
				vm.setSP((char)stack);
				vm.setPC(vmPC);
				SP=(char)stack;
				PC=vmPC;
				decrTimer(1);
				//number
				return command;
				
			}
			
			case "PPOP": {
				//setMODE('1');
				setSI(4);
				vmPC++;
				vm.setPC(vmPC);
				PC=vmPC;
				//setCHByte(1); // 2asis channel (nuo nulio skaiciuojame)
				return command;
				
			}
			
			case "IPSH": {
				//setMODE('1');
				setSI(3);
				vmPC++;
				vm.setPC(vmPC);
				
				PC=vmPC;
				//setCHByte(0);
				
				//SP++;
				//int stack = (int)SP;//Integer.parseInt(String.valueOf(SP),16);
				//BufferedReader br = new BufferedReader(new InputStreamReader(System.in));
				//try {
				//	String s = br.readLine();
				//	int length =s.length();
				//	if(length>4) {
				//		System.out.println("Klaida. Ivestis ilgesne nei 4 baitai");
				//	}
					
				/*	else if(length==4) {
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
				*/
				return command;
				
				
			}
			case "HALT": {
				System.out.println("Programos pabaiga");
				setSI(7);
				// decr timer 
				// pertraukimas
				//PC++;
				return command;
			}
				
			
		}
		
		if(command.startsWith("WR")) {
			
			int sharedBlockNr=Integer.parseInt(command.substring(2, 3),16);
			int sharedBlockWord = Integer.parseInt(command.substring(3,4),16);
			if(sharedBlockNr>3 || sharedBlockNr<1) {
				setPI(1);
			}
			else if(sharedBlockWord>=16 || sharedBlockWord<0) {
				setPI(1);
			}
			else {
				if(memory[63][sharedBlockNr-1].toInt()==vm.getVmIndex()) {
					// TODO patikrinimas su RM ar uzrakintas blokas su numeriu sharedBlockNr
					int stack = (int)vm.getSP();//Integer.parseInt(String.valueOf(SP),16);
					//Word[][] rmMemory=rm.getMemory();
					int address=(60+sharedBlockNr-1)*16+(sharedBlockWord);
					System.out.println("adresas" +address);
					memory[address/16][address%16].setBytes(vmMemory[stack/16][stack%16].getBytes());
					stack--;// ? ar reikia?
					SP=(char)stack;
					vm.setSP((char)stack);
					
					
				}
				else {
					setPI(4);
				}
				
			}
			vmPC++;
			vm.setPC(vmPC);
			
			PC=vmPC;
			
			decrTimer(1);
			return command;
		}
		
		if(command.startsWith("RD")) {
			
			
				int sharedBlockNr=Integer.parseInt(command.substring(2, 3),16);
				int sharedBlockWord = Integer.parseInt(command.substring(3,4),16);
				if(sharedBlockNr>3 || sharedBlockNr<1) {
					setPI(1);
				}
				else if(sharedBlockWord>=16 || sharedBlockWord<0) {
					setPI(1);
				}
				else {
					if(memory[63][sharedBlockNr-1].toInt()==vm.getVmIndex()) {
						int stack = (int)vm.getSP();//Integer.parseInt(String.valueOf(SP),16);
						stack++;// ? ar reikia?
						//Word[][] rmMemory=rm.getMemory();
						int address=(60+sharedBlockNr-1)*16+(sharedBlockWord); // ta 60 kazkaip apsirasyt reiktu aka kur prasideda bendra atmintis
						vmMemory[stack/16][stack%16].setBytes(memory[address/16][address%16].getBytes());
						
						vm.setSP((char)stack);
						
						SP=(char)stack;
						
						
					}
						else {
							setPI(4);
						}
				}
				
				vmPC++;
				vm.setPC(vmPC);
				
				PC=vmPC;
				
				decrTimer(1);
				return command;
		}
		
		if(command.startsWith("LC")) {
			//setMODE('1'); // supervisor mode
			setSI(5); // LC pertraukimas lock bendra atminti
			vmPC++;
			PC=vmPC;
			vm.setPC(vmPC);
			//int sharedBlockNr = Integer.parseInt(command.substring(2, 3));
			//rm.getManager().setBlockNr(sharedBlockNr);
			//getManager().setVmIndex(vm.getVmIndex());
			// vm numerio komandoje nebera int vmNumber = Integer.parseInt(command.substring(3, 4)); // arba taip arba kazkoki turet auto nr priskyreja nereiktu i komanda rasyt
			//PC++;
			//vm.setPC(PC);
			//command.concat(Integer.toString(vm.getVmIndex())); // gale bus nr vm
			return command;
		}
		
		if(command.startsWith("UC")) {
			//setMODE('1');
			setSI(6);
			vmPC++;
			PC=vmPC;
			vm.setPC(vmPC);
			//getManager().set
			//PC++;
			//vm.setPC(PC);
			//int sharedBlockNr = Integer.parseInt(command.substring(2, 3));
			//int vmNumber = vm.getVmIndex();// kuriant vm sugeneruoti
			//Word[][] rmMemory=rm.getMemory();
			//int intValue= memory[63][sharedBlockNr-1].toInt(); // reiksme zodzio su info
			//if(intValue!=0 && intValue==vmNumber ) { // reiktu turbut while nes juk turetu laukti kol atsiblokuos
			//	memory[63][sharedBlockNr-1]= new Word();// 63 last blokas kuriame sudeta info apie sharedmemory
			//}
			//else if(intValue!=0 && intValue!=vmNumber) {
			//	System.out.println("Si masina negali atblokuoti sio zodzio"); // turut reikt pertraukimo?
			//}
		//	else if(intValue==0) {
			//	System.out.println("Sritis jau atblokuota");	
		//	}
		//	PC++;
			return command;
			
		}
		
		if(command.startsWith("LD")) {
			
			String stringAddress = command.substring(2,4);
			for(int i=0; i<2; i++) {
				if(stringAddress.charAt(i)<'0' || stringAddress.charAt(i)>'F' || (stringAddress.charAt(i)>'9' && stringAddress.charAt(i)<'A') ) {
					setPI(3);
					vmPC++;
					vm.setPC(vmPC);
					PC=vmPC;
					decrTimer(1);
					return command;
				}
			}
			int poslinkis = Integer.parseInt(stringAddress,16);
			if(poslinkis>=0x70 || poslinkis <0) {
				setPI(1);
				vmPC++;
				vm.setPC(vmPC);
				PC=vmPC;
				decrTimer(1);
				return command;
			}
			int dsValue = (int)vm.getDS();
			//SP= vm.getSP();
			
			//vm.setSP(SP);
			int stack = (int)vm.getSP();//Integer.parseInt(String.valueOf(SP),16);
			stack++;
			
			if(stack/16>=16) { // arba sakai kad pilnas stack arba eini is naujo
				vm.setSP((char)0xE0);
				SP=0xE0;
				stack=SP;
			}
			if(stack<223) { // nelabai reikia nes neatimineja cia SP
				vm.setSP((char)0xFF);
				SP=0xFF;
				stack=SP;
			}
			//System.out.println(" Stack: " +stack);
			vmMemory[stack/16][stack%16].setBytes(vmMemory[dsValue+poslinkis/16][dsValue+poslinkis%16].getBytes());
			vmPC++;
			vm.setPC(vmPC);
			vm.setSP((char)stack);
			PC=vmPC;
			SP=(char)stack;
			decrTimer(1);
			return command;
		}
		
		if(command.startsWith("PT")) {
			String stringAddress = command.substring(2,4);
			for(int i=0; i<2; i++) {
				if(stringAddress.charAt(i)<'0' || stringAddress.charAt(i)>'F' || (stringAddress.charAt(i)>'9' && stringAddress.charAt(i)<'A') ) {
					setPI(3);
					vmPC++;
					vm.setPC(vmPC);
					PC=vmPC;
					decrTimer(1);
					return command;
				}
			}
			int address = Integer.parseInt(stringAddress,16);
			if(address>=0x70 || address <0) {
				setPI(1);
				vmPC++;
				vm.setPC(vmPC);
				PC=vmPC;
				decrTimer(1);
				return command;
			}
			int dsValue = (int)vm.getDS();
			int stack = (int)vm.getSP();//Integer.parseInt(String.valueOf(SP),16);
			
			vmMemory[(dsValue+address)/16][(dsValue+address)%16].setBytes(vmMemory[stack/16][stack%16].getBytes());
			if(stack/16>=16) { // arba sakai kad pilnas stack arba eini is naujo
				vm.setSP((char)0xE0);
				SP=0xE0;
				stack=SP;
			}
			if(stack/16<=14) {
				vm.setSP((char)0xFF);
				SP=0xFF;
				stack=SP;
			}
			stack--;
			
			vmPC++;
			vm.setPC(vmPC);
			vm.setSP((char)stack);
			PC=vmPC;
			SP=(char)stack;
			decrTimer(1);
			 /// gal atvirksciai? keiciam virtualios ir gale nustatom realios turbut taip ir geriau bus reiks pakeist
			
			return command;
		}
		
		if(command.startsWith("JP")) {
			String stringAddress = command.substring(2,4);
			for(int i=0; i<2; i++) {
				if(stringAddress.charAt(i)<'0' || stringAddress.charAt(i)>'F' || (stringAddress.charAt(i)>'9' && stringAddress.charAt(i)<'A') ) {
					setPI(3);
					vmPC++;
					vm.setPC(vmPC);
					PC=vmPC;
					decrTimer(1);
					return command;
				}
			}
			int posl = Integer.parseInt(command.substring(2,4),16); // nera tikrinimo geru reiksmiu
			if(posl>=70 || posl <0) {
				setPI(1);
				vmPC++;
				vm.setPC(vmPC);
				PC=vmPC;
				decrTimer(1);
				return command;
			}
			int cs = (int)vm.getCS();
			vmPC=posl+cs;
			vm.setPC(vmPC);
			PC=vmPC;
			decrTimer(1);
			return command;
		}
		if(command.startsWith("JE")){
			String stringAddress = command.substring(2,4);
			for(int i=0; i<2; i++) {
				if(stringAddress.charAt(i)<'0' || stringAddress.charAt(i)>'F' || (stringAddress.charAt(i)>'9' && stringAddress.charAt(i)<'A') ) {
					setPI(3);
					vmPC++;
					vm.setPC(vmPC);
					PC=vmPC;
					decrTimer(1);
					return command;
				}
			}
			int cs = (int)vm.getCS();
			char tempSF=vm.getSF();
			if ((tempSF & 0b00000010) == 0b00000010){ // ZF=1
				int posl = Integer.parseInt(command.substring(2,4),16);
				if(posl>=0x70 || posl <0) {
					setPI(1);
					vmPC++;
					vm.setPC(vmPC);
					PC=vmPC;
					decrTimer(1);
					return command;
				}
				vmPC=posl+cs;
				
			}
			else {
				vmPC++;
				
			}
			vm.setPC(vmPC);
			PC=vmPC;
			decrTimer(1);
			return command;
		}
		if(command.startsWith("JB")) {
			String stringAddress = command.substring(2,4);
			for(int i=0; i<2; i++) {
				if(stringAddress.charAt(i)<'0' || stringAddress.charAt(i)>'F' || (stringAddress.charAt(i)>'9' && stringAddress.charAt(i)<'A') ) {
					setPI(3);
					vmPC++;
					vm.setPC(vmPC);
					PC=vmPC;
					decrTimer(1);
					return command;
				}
			}
			int cs = (int)vm.getCS();
			char tempSF=vm.getSF();
			if((tempSF & 0b00000100) == 0b0000000100) { // CF=1
				int posl = Integer.parseInt(command.substring(2,4),16);
				if(posl>=0x70 || posl <0) {
					setPI(1);
					vmPC++;
					vm.setPC(vmPC);
					PC=vmPC;
					decrTimer(1);
					return command;
				}
				vmPC=posl+cs;
				
				
			
			}
			else {
				vmPC++;
			}
			vm.setPC(vmPC);
			PC=vmPC;
			decrTimer(1);
			return command;
		}
		if(command.startsWith("JA")){ // ZF = 0 and CF = 0
			String stringAddress = command.substring(2,4);
			for(int i=0; i<2; i++) {
				if(stringAddress.charAt(i)<'0' || stringAddress.charAt(i)>'F' || (stringAddress.charAt(i)>'9' && stringAddress.charAt(i)<'A') ) {
					setPI(3);
					vmPC++;
					vm.setPC(vmPC);
					PC=vmPC;
					decrTimer(1);
					return command;
				}
			}
			int cs = (int)vm.getCS();
			char tempSF=vm.getSF();
			if((tempSF & 0b00000110) == 0b000000000) {
				int posl = Integer.parseInt(command.substring(2,4),16);
				if(posl>=0x70 || posl <0) {
					setPI(1);
					vmPC++;
					vm.setPC(vmPC);
					PC=vmPC;
					decrTimer(1);
					return command;
				}
				vmPC=posl+cs;
				
				
				
			}
			else {
				vmPC++;
			}
			vm.setPC(vmPC);
			PC=vmPC;
			decrTimer(1);
			
			return command;
		}
		if(command.startsWith("JN")) {
			String stringAddress = command.substring(2,4);
			for(int i=0; i<2; i++) {
				if(stringAddress.charAt(i)<'0' || stringAddress.charAt(i)>'F' || (stringAddress.charAt(i)>'9' && stringAddress.charAt(i)<'A') ) {
					setPI(3);
					vmPC++;
					vm.setPC(vmPC);
					PC=vmPC;
					decrTimer(1);
					return command;
				}
			}
			int cs = (int)vm.getCS();
			char tempSF=vm.getSF();
			//System.out.println("SF "+(int)SF);
			if((tempSF & 0b00000010) == 0b000000000) { // ZF=0
				int posl = Integer.parseInt(command.substring(2,4),16);
				if(posl>=0x70 || posl <0) {
					setPI(1);
					vmPC++;
					vm.setPC(vmPC);
					PC=vmPC;
					decrTimer(1);
					return command;
				}
				vmPC=posl+cs;
				
			}
			else {
				vmPC++;
			}
			vm.setPC(vmPC);
			PC=vmPC;
			decrTimer(1);
			return command;
		}
		
		if(command.startsWith("RI")) {
			
			
			//int dsValue = (int)vm.getDS();
			//setMODE('1'); // supervizoriaus
			// kolkas skaitymas cia bet reiks perkelti prie RM
			setSI(1);
			vmPC++;
			vm.setPC(vmPC);
			PC=vmPC;
			//setCHByte(0); // uzsetinam 1-a channel skaiciuojam 0
			//int stack = (int)SP;//Integer.parseInt(String.valueOf(SP),16);
			//int numberOfBytes = vmMemory[stack/16][stack%16].toInt();
			//SP--;
			//String s;
			//BufferedReader br = new BufferedReader(new InputStreamReader(System.in));
			//try {
			//	s= br.readLine();
			//	if(s.length()!=numberOfBytes) {
			//		System.out.println("Baitu skaicius neatitinka skaicaus steko virsuneje");
			//	}
				//int startingPlace=Integer.parseInt(command.substring(2,4),16);
				
				//if(numberOfBytes%4==0) {
					//int wordCount=numberOfBytes/4;
					//int doneBytes=0;
					//ArrayList<Word> words = new ArrayList<>();
					//while(numberOfBytes-4 >=0) {
						
					//	Word word = new Word(s.substring(0+doneBytes,4+doneBytes).toCharArray());
						//sito anyway nereikia? nes kitaip 2 kartus pridetumevmMemory[dsValue+startingPlace/16][dsValue+startingPlace%16]=new Word(word);
					//	startingPlace++;
					//	doneBytes+=4;
					//	numberOfBytes-=4;
					//	words.add(word);
					//}
					//if(numberOfBytes<4 && numberOfBytes>0) {
					//	Word word = new Word();
					//	word.setBytes(new char[]{'\u0000','\u0000','\u0000','\u0000'});
					//	int left= numberOfBytes;
					//	int position = 0;
					//	for(int i=0; i<left; i++) {
					//		word.setByte(position, s.charAt(doneBytes));
					//		doneBytes++;
					//		position++;
					//	}
					//	words.add(word);
					//}
					//loadDataAsBytes(words,startingPlace,vm);
				//}
				
				//for(int i=0; i<numberOfBytes; i++) {
				//	memory[startingPlace/16][startingPlace%16] // visur kur data reikia DS + statingPlace pvz nes nurodom tik poslinki
				//}
			//} catch (IOException e) {
				// TODO Auto-generated catch block
			//	e.printStackTrace();
			//}
			//setMODE('0'); // supervizoriaus
			
		//	setSI(0);
		//	unsetCHByte(0); // uzsetinam 0lini channel
			//PC++;
			//vm.setPC(PC);
			
			return command;
			
		}
		
		if(command.startsWith("PR")) {
			//int dsValue = (int)vm.getDS();
			//setMODE('1');
			setSI(2);
			vmPC++;
			vm.setPC(vmPC);
			PC=vmPC;
			//setCHByte(1);
			//int stack = (int)SP;// Integer.parseInt(String.valueOf(SP),16);
			//int numberOfBytes = vmMemory[stack/16][stack%16].toInt();
			//int startingPlace=Integer.parseInt(command.substring(2,4),16);
			
		//	SP--;
			//int tempi=0;
			//for(int i=0; i<numberOfBytes; i++) {
			//	System.out.print(vmMemory[dsValue+startingPlace/16][dsValue+startingPlace%16].getByte(tempi));
			//	tempi++;
			//	if(tempi==3) {
			//		tempi=0;
			//		startingPlace++; // bloko nereikia didint blogai cia
			//		System.out.println("");
				//}
			//}
			//setMODE('0');
			//setSI(0);
			//unsetCHByte(1);
			return command;
		}
		
		setPI(2);
		return command; // kazkokia bad komanda grazins
		
		
		
	}
	
	public String getNextCommand(VM vm) {
		int tempPC=vm.getPC();
		//int cs = vm.getCS();
		Word[][] vmMemory = vm.getMemory();
		//System.out.println("Temp pc " + tempPC);
		String nextCommand = vmMemory[(tempPC)/16][(tempPC)%16].toString();
		if(nextCommand=="0000") {
			return "END";
		}
		else {
			return nextCommand;
		}
	}
	
	private void loadDataAsBytes(ArrayList<Word> words, int address, VM vm) {
		for(int i=0; i<words.size(); i++) {
			for(int j=0; j<4; j++) {
				char currentByte = words.get(i).getByte(j);
				
				if(currentByte!='\u0000') {
					vm.getMemory()[address/16][address%16].setByte(j,currentByte);
				}

				
			}
			address++;
		}
		
		
	}
	
	
	public void decrTimer(int decr) {
		if(decr>=TI) {
			TI=0;
		}
		else {
			TI=TI-decr;
		}
	}
	
	
}


