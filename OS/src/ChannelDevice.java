public class ChannelDevice{
    private int SB; // is kurio takelio kopijuojam 1,2,3
    private int DB; // i kuri takeli kopijuojam 1,2,3
    private int ST; // objekto numeris kuri kopijuojam 1 vartotojo atmintis, 2 supervizorine atmintis, 3 isorine atmintis, 4 ivedimo irenginys
    private int DT; // objekto numeris i kuri kopijuojam 1 vartotojo atmintis, 2 supervizorine atmintis, 3 isorine atmintis, 4 isvedimo irenginys
    private boolean blocked;
    
    public ChannelDevice(){
        blocked = false;
    }
    
    public void xchg(){// vykdome apsikeitima duomenimis
        // to do duomenu perkelimas
        /*switch(SB){??
            case 1:
                break;
            case 2:
                break;
            case 3:
                break;
        }*/
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

