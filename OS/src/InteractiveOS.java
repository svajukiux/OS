import java.io.File;
import java.io.IOException;
import java.util.Arrays;

public class InteractiveOS {
	
	public static void main(String[] args) {
		// pati pradzia sukuria RM
		System.out.println("Kuriama RM");
		RM rm = new RM();
		// tada galimybes:
		//1. uzloadinti programa
		//2. paleisti programa
		//3. sekantis zingsnis
		//4. isvalyti(delete VM)
		
		// ofc pirma reikia uzloadinti. Tada:
		// 1. paima faila
		
		File file = new File("program.txt");
		//2. sukuria VM
		System.out.println("Kuriama VM");
		//VM vm = new VM();
		
		System.out.println("Uznkraunama VM"); // manau reikia laodint kartu kai pasirenki faila taip ir darom nvm failas vm kurimas programos uzloadinimas
		VM vm =rm.loadProgram(file);
		
		System.out.println("Kuriamas Parser");
		ProgramParser parser = new ProgramParser();
		
		//try {
		//	System.out.println("Skaitomas ir uzkraunamas failas i atminti");
		//	parser.parseFile(file, vm);
		//} catch (IOException e) {
		//	// TODO Auto-generated catch block
		//	e.printStackTrace();
		//}
		// running program
		
		while(true) {
			String command = "Unknown";
			rm.setMODE('0'); // 0 is user mode
			while(rm.getInterrupt()==0) {
				 command = rm.processCommand(vm);
			}
			
			rm.setMODE('1'); // supervisor mode
			if(rm.processInterrupt(vm)==1) { // HALT
				break;
			}
		}
		
		
		
		//String command="Labas";
		for(int i=0; i<10; i++) {
			rm.processCommand(vm);
			//System.out.println(command);
			Word[][] memory = vm.getMemory();
			for(int j=0; j<16; j++) {
				System.out.println();
				for(int k=0; k<16; k++) {
					System.out.print(memory[j][k].data);
					System.out.print(" ");
					
				}
			}
			System.out.println("End ");
		}
		
		//System.out.println(Arrays.deepToString(vm.getMemory()));
		
		
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
		
	}
	
	
}
