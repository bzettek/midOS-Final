// Brandon Zettek z2015083
// Opcodes.java
//created during mod 1
//constants used by OS and CPU had trouble wit enums
//uses public static final int instead of enum
//each opcode has a fexed num that won't change
//works the same as enum for matching in switch stments
//used runtime validation in OS.java
//\opcodeNum == 0 will catch bad instucts at load time

public class Opcodes 
{
    public static final int INCR = 1;
    public static final int ADDI = 2;
    public static final int ADDR = 3;
    public static final int PUSHR = 4;
    public static final int PUSHI = 5;
    public static final int MOVI = 6;
    public static final int MOVR = 7;
    public static final int MOVMR = 8;
    public static final int MOVRM = 9;
    public static final int MOVMM = 10;
    public static final int PRINTR = 11;
    public static final int PRINTM = 12;
    public static final int PRINTCR = 13;
    public static final int PRINTCM = 14;
    public static final int JMP = 15;
    public static final int JMPI = 16;
    public static final int JMPA = 17;
    public static final int CMPI = 18;
    public static final int CMPR = 19;
    public static final int JLT = 20;
    public static final int JLTI = 21;
    public static final int JLTA = 22;
    public static final int JGT = 23;
    public static final int JGTI = 24;
    public static final int JGTA = 25;
    public static final int JE = 26;
    public static final int JEI = 27;
    public static final int JEA = 28;
    public static final int CALL = 29;
    public static final int CALLM = 30;
    public static final int RET = 31;
    public static final int EXIT = 32;
    public static final int POPR = 33;
    public static final int POPM = 34;
    public static final int SLEEP = 35;
    public static final int INPUT = 36;
    public static final int INPUTC = 37;
    public static final int SETPRIORITY = 38;
    public static final int SETPRIORITYI = 39;

    //mod 4 adds shared memorylocks events
    public static final int MAPSHAREDMEM = 40;
    public static final int ACQUIRELOCK = 41;
    public static final int ACQUIRELOCKI = 42;
    public static final int RELEASELOCK = 43;
    public static final int RELEASELOCKI = 44;
    public static final int SIGNALEVENT = 45;
    public static final int SIGNALEVENTI = 46;
    public static final int WAITEVENT = 47;
    public static final int WAITEVENTI = 48;

    //module 5 for heap allocation
    public static final int ALLOC = 49;
    public static final int FREEMEMORY = 50;

    public static boolean isValid(int opcode)
    {
        return opcode >= 1 && opcode <= 50; //makes sure non valid ops aren't used
    }

}