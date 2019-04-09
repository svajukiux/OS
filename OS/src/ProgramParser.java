import java.io.BufferedReader;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.IOException;
import java.util.ArrayList;

public class ProgramParser {
	boolean parsedDataGracefully;
	
	public ProgramParser() {
		super();
		parsedDataGracefully=false;
		
	}
	
	public boolean parseFile(File file,VM vm) throws FileNotFoundException, IOException{ //for now parses if there is only 1 program in a file
		//ArrayList<String> commands = new ArrayList<>();
		BufferedReader br = new BufferedReader(new FileReader(file));
		String line;
		//boolean isDataParsed=false;
		
		while((line = br.readLine()) !=null) {
			System.out.println(line);
			if(line.equalsIgnoreCase("DATA")) {
				parseData(br,vm);
				//isDataParsed=true;
				
				
			}
			if(line.equalsIgnoreCase("CODE")) {
				
					throw new IOException("DATA should be written before CODE");
					
			}
		
			System.out.println("Hi");
			parseCode(br,vm);
			
				
			
		}
		
		return true;
		
		
	}
	
	private void parseData(BufferedReader br, VM vm) throws IOException {
		String line;
		//int dataWords =0; // kolkas nezinau kam
		
		while(((line = br.readLine())!=null)) {
			if(line.equalsIgnoreCase("CODE")){
				//commands.add("[STC]"); // pasiekem code segment
				System.out.println("STC");
				parsedDataGracefully = true; // galim eit code segmen parsinima
				return;
			}
			
			String [] allSplits = line.split(" ", 0);
			int poslinkis = Integer.parseInt(allSplits[0],16); // poslinkis nuo data segmento pradzios in hex
			int currentAddress= vm.getDS()+poslinkis; 
			boolean arZodis = allSplits[1].equalsIgnoreCase("w"); // if word
			String data[] = allSplits[2].split(",",0); // grazins 1 ir 2 pvz arba stringus atskirtus
			// jauciu reikia hashset data ir poslinkiui kad kartu butu
			for(int i=0; i<data.length; i++) {
				data[i].trim();
			}
			
				ArrayList<Word> rawData = new ArrayList<Word>(); //raw data of char arrays
				for(int i=0; i<data.length; i++) {
					if(data[i].contains("\"")) { // ar stringas
							//char charData[] = data[i].toCharArray(); // not tested
							//for(int j=0; j<charData.length; j++) {
							//	rawData.add(charData[j]);
							//}
						
							int length=data[i].length();
							int lenghtCopied=0;
							//for(int j=0; j<data[i].length(); j++) {
							while(length>=4) {
								Word temp = new Word(data[i].substring(lenghtCopied,lenghtCopied+4).toCharArray()); // sukuria word is 4 char symboliu
								rawData.add(temp);
								lenghtCopied+=4;
								length-=4;
							}
							
							if(length!=0 && length<4) { // jei maziau nei 4 baitai
								Word temp = new Word();
								int left=length;
								if(arZodis==true) { // jei zodis aka w faile desim baitus nuo galo (faile bus skaicius paduotas)
									
									int position=4-left; // pradine pozicija 4 baitas, keliame nuo galo
									while(left>0) {
										temp.setByte(position, data[i].charAt(lenghtCopied));
										lenghtCopied++;
										left--;
										position++;
									}
									rawData.add(temp);
									
								}
								else { // tada rastas buvo "b". Galbut reiktu elgtis kaip cia ir su w( tada isvis nereiktu b rezimo)(nustatytume priekinius
									   // bitus o ne galinius baito kai atlieka nors tada su skaiciais butu ne kazka)
									 //  last zodis nuo priekio rasomas jei "b"
									/// TODO metodas rasyk i atminti likusius baitus (nes nereikia viso zodzio rasyti bet atskirus baitus)
									// bet ka tada det i rawData
									/// arba dedam i rawData ta zodi bet kai rasysim i atminti gale ziureti ir jei 0 nerasyti nu cia reiktu pasiu-
									//lymu
									/// rasydami zodi nunulinsim galbut reikalinga info
									int position = 0;
									temp.setBytes(new char[]{'\u0000','\u0000','\u0000','\u0000'}); // priskiriu null nes kai rasysim baitais rasysim iki null sitos reiksmes
									
									while(left>0) {
										temp.setByte(position, data[i].charAt(lenghtCopied));
										lenghtCopied++;
										left--;
										position++;
									}
									rawData.add(temp);
									
									
								}
							}
							
					}
					else { // ne stringas o skaicius dazniausiai o gal ir visada su "w"
						int length= data[i].length();
						if(length>4) { // max number FFFF kolkas i guess
							System.out.println("number too big");
						}
						else {
							if(arZodis==true) {
								if(length==4) {
									Word temp = new Word(data[i].toCharArray());
									rawData.add(temp);
								}
								if(length<4) {
									Word temp = new Word();
									int left=length;
									int position=4-left; // pradine pozicija 4 baitas, keliame nuo galo
									for(int k=0; k<left; k++) { // length?
										temp.setByte(position, data[i].charAt(i));
										position++;
									}
									rawData.add(temp);
								}
							}
							else { // again "b" reiktu deti nuo priekio nors manau kad retai kada prireiks su skaiciais 
								   // manau tiesiog reikia rasyti i atminti arba baitais arba zodziais priklausomai ar yra "w" ar "b" kiekvienai eillutei
								   // pasigaminam ir to arraylist char array ir tada po viena char dedam i atminti galbut ir tiktu
								System.out.println("Constants can only be in word format"); // cia reikes exceptionus arba interuptus
								
								
							}
						}
					}
			}	
			if(arZodis==true) {
				
				loadDataAsWords(rawData,currentAddress,vm); // loads line of data if it was words
			}
			else if(arZodis==false) {
				loadDataAsBytes(rawData,currentAddress,vm);
			}
			
		}
		
		
	}
	
	private void parseCode(BufferedReader br, VM vm) throws IOException {
		if(!parsedDataGracefully) {
			throw new IOException("Neteisingas programos formatavimas");
			
		}
		
		// ir ar reikia pries code segmenta parasyti pradine reiksme nes mes turim tuos registrus
		String line;
		ArrayList<Word> commands = new ArrayList<>();
		while((line = br.readLine())!=null) { // kiek zejau beveik visos musu komandos visos telpa i viena zodi su psh
			int length= line.length();
			int added = 0;
			
			if(length<4 && length!=0) { // jei 3 baitu komanda
				
				Word temp = new Word(line.substring(added,added+3).toCharArray()); // nezinau tiksliai ar pirmus 3 uzims ar galutinius 3
				temp.setByte(4, ' '); // gale tarpas jei mazesne nei 4 baitu komanda
				commands.add(temp);
			}
			
			while(length-4>=0) {
				// gali buti problemu nes ten x nurodyta bet manau kad galesim pushint tikrai daugiau nei F
				Word temp = new Word(line.substring(added, added+4).toCharArray());
				commands.add(temp);
				added+=4;
				length-=4;
				
			}
			
			if(length<4 && length!=0) { // PSH x komandai
				Word temp = new Word();
				int left=length;
				int position=4-left;
				System.out.println("position" + position);
				for(int k=0; k<left; k++) { // length?
					temp.setByte(position, line.charAt(added+k));
					position++;
				}
				commands.add(temp);
			}
			
			
			// kolkas nerasau kas jei lieka liekana geriau butu pasistengt kad dalintusi is 4 musu komandos ilgiai
											  
										// tai tiesiog skaityt line ir vienam line 1 zodis
			loadDataAsWords(commands,vm.getCS(),vm);
		}
	}
	
	private void loadDataAsWords(ArrayList<Word> words, int address, VM vm) { // address 2 baitai uzloadina eilute nuskaitytu zodziu
		
		for(int i=0; i<words.size();i++) {
			
			
			vm.getMemory()[address/16][address%16] = words.get(i);
			
			address++;
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
		}
		
	}
	
	
}


