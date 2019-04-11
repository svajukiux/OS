import java.util.ArrayList;

public class ChannelDevice{
    private int SB; // is kurio takelio kopijuojam 1,2,3
    private int DB; // i kuri takeli kopijuojam 1,2,3 DB ir SB sutaps musu atveju (naudosime 11, 22, nes dar neturim isorines atminties)
    private int ST; // objekto numeris kuri kopijuojam 1 vartotojo atmintis, 2 supervizorine atmintis, 3 isorine atmintis, 4 ivedimo irenginys (14)
    private int DT; // objekto numeris i kuri kopijuojam 1 vartotojo atmintis, 2 supervizorine atmintis, 3 isorine atmintis, 4 isvedimo irenginys (41)
    private boolean blocked;
    
    public ChannelDevice(){
        blocked = false;
    }
    
    public Word xchg(InputDevice inputDevice){
        return inputDevice.readWord();
    }
    
    public ArrayList <Word> xchg(InputDevice inputDevice, int numberOfBytes){
        return inputDevice.readBytes(numberOfBytes);
    }
    
    public void xchg(OutputDevice outputDevice, char[] isvestis){
        outputDevice.printBytes(isvestis);
    }
    
    public void xchg(OutputDevice outputDevice, Word[][] vmMemory, int stack){
        outputDevice.printWord(vmMemory[stack/16][stack%16]);
    }
    
    public void setSB(int sb){
        SB = sb;
    }
    
    public int getSB(){
        return SB;
    }
    
    public void setDB(int db){
        DB = db;
    }
    
    public int getDB(){
        return DB;
    }
    
    public void setST(int st){
        ST = st;
    }
    
    public int getST(){
        return ST;
    }
    
    public void setDT(int dt){
        DT = dt;
    }
    
    public int getDT(){
        return DT;
    }
}
