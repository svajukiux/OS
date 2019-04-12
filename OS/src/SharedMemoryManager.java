
public class SharedMemoryManager {
	
	private int startingSharedAddress;
	private int numberOfSharedBlocks;
	private int vmIndex;
	private int blockNr;
	Word [][] sharedMemory;
	
	
	
	public int getStartingSharedAddress() {
		return startingSharedAddress;
	}


	public void setStartingSharedAddress(int startingSharedAddress) {
		this.startingSharedAddress = startingSharedAddress;
	}


	public int getNumberOfSharedBlocks() {
		return numberOfSharedBlocks;
	}


	public void setNumberOfSharedBlocks(int numberOfSharedBlocks) {
		this.numberOfSharedBlocks = numberOfSharedBlocks;
	}


	public int getVmIndex() {
		return vmIndex;
	}


	public void setVmIndex(int vmIndex) {
		this.vmIndex = vmIndex;
	}


	public int getBlockNr() {
		return blockNr;
	}


	public void setBlockNr(int blockNr) {
		this.blockNr = blockNr;
	}


	public SharedMemoryManager(int startingSharedAddress, int numberOfSharedBlocks) {
		super();
		this.startingSharedAddress = startingSharedAddress;
		this.numberOfSharedBlocks = numberOfSharedBlocks;
		//sharedMemory = new Word[numberOfSharedBlocks][16];
		//Word [][] rmMemory =rm.getMemory();
		//for(int i=0 ; i< numberOfSharedBlocks+1; i++) { // +1 ne dar reikia paimti ta informacini bloka
	//		this.sharedMemory[startingSharedAddress+i]=rmMemory[startingSharedAddress+i];
	//	}
		this.vmIndex=0;
		this.blockNr=0;
	}


	public boolean LC(RM rm, VM vm) {
		Word[][] rmMemory = rm.getMemory();
		Word[][] vmMemory = vm.getMemory();
		Word command = vmMemory[(vm.getPC()-1)/16][(vm.getPC()-1)%16];
		vmIndex= vm.getVmIndex();
		char charIndex = (char)(vmIndex + '0');
		blockNr= Integer.parseInt(command.toString().substring(2,3));
		if(blockNr>3 || blockNr<0) {
			rm.setPI(1);
			return false;
		}
		if(rmMemory[startingSharedAddress+numberOfSharedBlocks][blockNr-1].toInt()==0) { // 63 reiktu turbut while nes juk turetu laukti kol atsiblokuos
			rmMemory[startingSharedAddress+numberOfSharedBlocks][blockNr-1].setByte(3,charIndex);// 63 last blokas kuriame sudeta info apie sharedmemory ??? buvo su Word metodu vietoj void
			rm.setSI(0);
			return true;
		}
		else if(rmMemory[startingSharedAddress+numberOfSharedBlocks][blockNr-1].toInt()!=0 && rmMemory[startingSharedAddress+numberOfSharedBlocks][blockNr-1].toInt()!=vmIndex) {
			rm.setSI(0); 
			System.out.println("Blokas jau uzrakintas kitos VM");
			return false;
			
		}
		else if(rmMemory[startingSharedAddress+numberOfSharedBlocks][blockNr-1].toInt()==vmIndex) {
			rm.setSI(0);
			System.out.println("Blokas jau uzrakintas. Galima rasyti");
			return true;
		}
		System.out.println("Nenumatytas atvejis");
		return false; // nenumatytas atvejis
	}
	
	public boolean UC(RM rm, VM vm) {
		
		Word[][] rmMemory = rm.getMemory();
		Word[][] vmMemory = vm.getMemory();
		Word command = vmMemory[(vm.getPC()-1)/16][(vm.getPC()-1)%16];
		vmIndex= vm.getVmIndex();
		blockNr= Integer.parseInt(command.toString().substring(2,3));
		if(blockNr>3 || blockNr<0) {
			rm.setPI(1);
			return false;
		}
		if(rmMemory[startingSharedAddress+numberOfSharedBlocks][blockNr-1].toInt()==0) { // 63 reiktu turbut while nes juk turetu laukti kol atsiblokuos
			System.out.println("Blokas jau atrakintas");
			rm.setSI(0);
			return true;
		}
		else if(rmMemory[startingSharedAddress+numberOfSharedBlocks][blockNr-1].toInt()!=0 && rmMemory[startingSharedAddress+numberOfSharedBlocks][blockNr-1].toInt()!=vmIndex) {
			rm.setSI(0); 
			System.out.println("Blokas uzrakintas kitos VM. Jums negalima atrakinti");
			return false;
			
		}
		else if(rmMemory[startingSharedAddress+numberOfSharedBlocks][blockNr-1].toInt()==vmIndex) {
			rmMemory[startingSharedAddress+numberOfSharedBlocks][blockNr-1].setByte(3,'0');
			rm.setSI(0);
			System.out.println("Blokas sekmingai atrakintas.");
			return true;
		}
		System.out.println("Nenumatytas atvejis");
		return false; // nenumatytas atvejis
	}
}
