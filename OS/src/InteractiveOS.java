import java.io.File;
import java.io.IOException;
import java.util.Arrays;
import java.util.Scanner;

public class InteractiveOS {
	
	public static void main(String[] args) {
		// pati pradzia sukuria RM
		
		RM rm = new RM();
		System.out.println("Kuriama VM");
		VM vm=rm.createAndLoadVirtualMachine();
		System.out.println("Sukurta");
		// tada galimybes:
		//1. uzloadinti programa
		//2. paleisti programa
		//3. sekantis zingsnis
		//4. isvalyti(delete VM)
		
		// ofc pirma reikia uzloadinti. Tada:
		// 1. paima faila
		boolean running =true;
		System.out.println("Virtual Machine created. Available commands:");
		System.out.println("\"load program\" command to load a program. You will be asked for a number");
		System.out.println("\"run\" loads program");
		System.out.println("\"step\" executes one step only");
		//System.out.println("\"restart\" restarts  to start program from beggining");
		System.out.println("\"RM memory\" prints out real memory");
		System.out.println("\"VM memory\" prints out virtual memory");
		System.out.println("\"exit\" closes real machine");
		
		File file = new File("programs.txt");
		//TODO kai bus failo struktura kitokia prasysi ivesti programos numeri kuria uzloadint
		//2. sukuria VM
		Scanner scanner = new Scanner(System.in);
		boolean success =false;
		boolean finished=false;
		
		while (running) {
			System.out.print("command: ");
			String input = scanner.nextLine();
			String command = "Unknown";
			
			
			switch(input) {
			case "load program":
				//if(vm!=null) {
					clean(vm);
					clean(rm);
				//}
				//vm=null;
				System.out.println("Please insert program number");
				int programNumber = scanner.nextInt();
				scanner.nextLine();
				String program = "program".concat(Integer.toString(programNumber));
				/// tada skaitymas ir loadinimas
				System.out.println("Kuriama VM");
				//VM vm = new VM();
				
				System.out.println("Uznkraunama programa"); // manau reikia laodint kartu kai pasirenki faila taip ir darom nvm failas vm kurimas programos uzloadinimas
				//printMemory(vm);
				success = rm.loadProgram(file,program,vm);
				//printMemory(vm);
				if(success==true) {
					System.out.println("Programa uzkrauta");
					command = rm.getNextCommand(vm);
					System.out.println("Next Command: " +command);
					printRegisters(rm);
					printRegisters(vm);
					printMemory(vm);
					finished=false;
				}
				else {
					System.out.println("Nepavyko uzkrauti programos");
				}
				break;
			//
			case "run":
				if(success==false) {
					System.out.println("VM not yet created");
				}
				
				else if(finished==true) {
					System.out.println("Programa baigus darba");
				}
				else {
					
					while(true) {
						
						String returnedCommand;
						returnedCommand = "Unknown";
						rm.setMODE('0'); // 0 is user mode tai kaip cia negaliu as ju keitaliot o viduj reikes
						
						
						while(rm.getInterrupt()==0) {
							//System.out.println("Interupt " +rm.getInterrupt());
							//command=rm.getNextCommand(vm);
							//System.out.println("NextCommand" + command);
							
							 returnedCommand = rm.processCommand(vm);
							 
							 command=rm.getNextCommand(vm);
								///System.out.println("NextCommand" + command);
							//String command="Labas";
								//for(int i=0; i<10; i++) {
									//rm.processCommand(vm);
									//System.out.println(command);
									
								//}
							 ///System.out.println("executed command: " + returnedCommand);
						}
						
						
						rm.setMODE('1'); // supervisor mode
						if(rm.processInterrupt(vm)==1) { // HALT
							if(rm.getSI()==7) {
								finished=true;
								System.out.println("Programa baigus darba");
							}
							//Word[][] memory = vm.getMemory();
							printRegisters(rm);
							printRegisters(vm);
							//printChannelStates(rm);
							printMemory(vm);
							System.out.println("End ");
							break;
							
						}
						
					}
				}
				break;
				
			case "step":
				
				if(success==false) {
					System.out.println("Neuzkrauta programa");
				}
				else if(finished==true) {
					System.out.println("Programa pasiekus gala");
				}
				else {
					//command=rm.getNextCommand(vm);
					//System.out.println("Next Command: " +command);
					if(rm.getInterrupt()==0) {
						String returnedCommand = rm.processCommand(vm);
						System.out.println("executed command: " + returnedCommand);
						
					}
					else {
						//rm.setMODE('1'); // reiks issimt nes cia negaliu keitaliot rm lol
						if(rm.processInterrupt(vm)==1) {
							if(rm.getSI()==7) {
								System.out.println("Program End");
								finished=true;
							}
							
						}
					}
					printRegisters(rm);
					printRegisters(vm);
					//printChannelStates(rm);
					printMemory(vm);
					command = rm.getNextCommand(vm);
					System.out.println("Next Command: " +command);
				}
				break;
				
			
				
			case "RM memory":
				printMemory(rm);
				break;
				
			case "VM memory":
				printMemory(vm);
				break;
				
			case "exit":
				running=false;
				break;
				
			default:
				System.out.println("Tokios komandos nera");
				break;
			}
			
			
			
			}
			
		}
		//System.out.println("Kuriama VM");
		//VM vm = new VM();
		
		//System.out.println("Uznkraunama VM"); // manau reikia laodint kartu kai pasirenki faila taip ir darom nvm failas vm kurimas programos uzloadinimas
		//VM vm =rm.loadProgram(file);
		
		//System.out.println("Kuriamas Parser");
		//ProgramParser parser = new ProgramParser();
		
		//try {
		//	System.out.println("Skaitomas ir uzkraunamas failas i atminti");
		//	parser.parseFile(file, vm);
		//} catch (IOException e) {
		//	// TODO Auto-generated catch block
		//	e.printStackTrace();
		//}
		// running program
		

		
		
		//System.out.println(Arrays.deepToString(vm.getMemory()));
		
		/*
		Word[][] memory2 = rm.getMemory();
		
		
		for(int i=0; i<64; i++) {
			System.out.println();
			for(int j=0; j<16; j++) {
				System.out.print(memory2[i][j].data);
				System.out.print(" ");
				
			}
		}
		System.out.println("End ");
		
		//memory[1][1].setBytes(new char[]{'a','b','c','d'}); 
		//System.out.println(Integer.toHexString(520));
		
		for(int i=0; i<64; i++) {
			System.out.println();
			for(int j=0; j<16; j++) {
				System.out.print(memory2[i][j].data);
				System.out.print(" ");
				
			}
		}
		*/
	
	public static void printMemory(RM rm) {
		
		Word[][] rmMemory = rm.getMemory();
		for(int i=0; i<64; i++) {
			System.out.println();
			for(int j=0; j<16; j++) {
				System.out.print(rmMemory[i][j].getBytes());
				System.out.print(" ");
				
			}
		}
		System.out.println("");
		
	}
	
	public static void printMemory(VM vm) {
		Word[][] vmMemory = vm.getMemory();
		for(int i=0; i<16; i++) {
			System.out.println();
			for(int j=0; j<16; j++) {
				System.out.print(vmMemory[i][j].getBytes());
				System.out.print(" ");
				
			}
		}
		System.out.println("");
	}
	
	public static void printRegisters(RM rm) {
		System.out.println("RM Registrai:");
		System.out.println("PTR    "+ Integer.toHexString(rm.getPTR().toInt()));
		System.out.println("MODE   "+ rm.getMODE());
		System.out.println("PC     "+ Integer.toHexString((int)rm.getPC()));
		System.out.println("SP     "+ Integer.toHexString((int)rm.getSP()));
		System.out.println("PI     "+ Integer.toHexString((int)rm.getPI()));
		System.out.println("SI     "+ Integer.toHexString((int)rm.getSI()));
		System.out.println("TI     "+ Integer.toHexString((int)rm.getTI()));
		System.out.println("CH     "+ Arrays.toString(rm.getCH()));
		System.out.println("SF     "+ Integer.toHexString((int)rm.getSF()));
		
	}
	
	public static void printRegisters(VM vm) {
		System.out.println("VM Registrai:");
		System.out.println("PC     "+ Integer.toHexString((int)vm.getPC()));
		System.out.println("SP     "+ Integer.toHexString((int)vm.getSP()));
		System.out.println("SF     "+ Integer.toHexString((int)vm.getSF()));
		System.out.println("DS     "+ Integer.toHexString((int)vm.getDS()));
		System.out.println("CS     "+ Integer.toHexString((int)vm.getCS()));
		
	}
	
	public static void clean(VM vm) {
		vm.setPC((int)vm.getCS());
		vm.setSP((char)0xDF);
		vm.setSF((char)0);
		Word[][] vmMemory = vm.getMemory();
		for(int i=0; i<16; i++) {
			for(int j=0; j<16; j++) {
				for(int k=0; k<4; k++) {
					vmMemory[i][j].setByte(k, '0');
				}
				
				
			}
		}
	}
	public static void clean(RM rm) {
		rm.setSI(0);
		rm.setPI(0);
		rm.setMODE('0');
		rm.setTI(10);
		rm.setCH(new char[] {'0','0','0'});
		
	}
	/*
	public static void printChannelStates(RM rm) {
		char [] channels= rm.getCH();
		String busena = "Neaktyvus";
		int siValue= (int)rm.getSI();
		if(int)rm.getSI()=='1') {
			busena = "Aktyvus";
		}
		System.out.println("Ivesties irenginys    "+ busena);
		if(channels[1]=='1') {
			busena= "Aktyvus";
		}
		else {
			busena = "Neaktyvus";
		}
		System.out.println("Isvesties irenginys    "+ busena);
	}
	*/
	
	}
	
	
//}
