// Brandon Zettek z2015083
// CPU.java
// Executes instruction 1 at a time read/write Memory
// uses Memory to get opcodes and read/write data
// takes opcode consts to find the correct instruction

import java.util.Scanner;

public class CPU
{
    public static final int QUANTUM_EXPIRED = 0;
    public static final int PROCESS_EXIT = 1;
    public static final int PROCESS_SLEEP = 2;
    public static final int PROCESS_IO = 3;
    public static final int PROCESS_WAIT_LOCK = 4;  //mod 4
    public static final int PROCESS_WAIT_EVENT = 5;  //mod 4

    //14 registers dont use index 0 since starts with 1
    private int sleepAmnt;
    private int stackFloor;
    private int[] registers;

    //mod 4 lockList eventList and shared memory
    private int[] lockList;
    private boolean[] eventList;
    private int[] sharedPages; //mod 6
    private int waitOn;

    //two flags for comparisons
    private boolean signFlag;
    private boolean zeroFlag;

    //each instruction = 1 tick
    private int clock;

    //ref to memory so CPU can read/write
    private Memory memory;

    //from mod 1
    public CPU(Memory memory)
    {
        this.registers = new int[15];  //use 1-14
        this.signFlag = false; //is compare negative?
        this.zeroFlag = false; //is compare equal?
        this.clock = 0;
        this.memory = memory;
        this.stackFloor = 0;

        registers[11] = 0; //IP
        registers[13] = memory.getSize() - 4; //stack start at top of mem
        registers[14] = memory.getNextAddress(); //point to gobal data

    }

    public int run(int quantum)
    {
        int cycleCount = 0;
        boolean running = true;
        int retReason = QUANTUM_EXPIRED;

        while (running && cycleCount < quantum)
        {
            //get the instruct at the current IP
            int ip = registers[11];

            int opcode = memory.readByte(ip); //fetch instruction
            int param1 = memory.readInt(ip + 1);
            int param2 = memory.readInt(ip + 5);


            switch (opcode)
            {
                case Opcodes.MOVI:
                    registers[param1] = param2;
                    break;

                case Opcodes.INCR:
                    registers[param1] += 1;
                    break;

                case Opcodes.PRINTR:
                    System.out.println(registers[param1]);
                    break;

                case Opcodes.EXIT:
                    running = false;
                    retReason = PROCESS_EXIT;
                    break;

                case Opcodes.ADDI:
                    registers[param1] += param2;
                    break;

                case Opcodes.ADDR:
                    registers[param1] += registers[param2];
                    break;
                case Opcodes.MOVR:
                    registers[param1] = registers[param2];
                    break;

                case Opcodes.CMPI:
                signFlag = false;
                zeroFlag = false;
                //will remember result for next compare and jmp instructs
                if (registers[param1] < param2)
                {
                    signFlag = true;
                }
                if (registers[param1] == param2)
                {
                    zeroFlag = true;
                }
                    break;

                case Opcodes.CMPR:
                signFlag = false;
                zeroFlag = false;

                if (registers[param1] < registers[param2])
                {
                    signFlag = true;
                }
                if (registers[param1] == registers[param2])
                {
                    zeroFlag = true;
                }
                    break;

                //all jumps subtract 9 since cpu will add 9 to IP each time

                case Opcodes.JLT:
                    if (signFlag)
                    {
                        registers[11] = registers[11] + registers[param1] - 9;
                    }
                    break;

                case Opcodes.JGT:
                    if (signFlag == false && zeroFlag == false)
                    {
                        registers[11] = registers[11] + registers[param1] - 9;
                    }
                    break;

                case Opcodes.JE:
                    if (zeroFlag)
                    {
                        registers[11] = registers[11] + registers[param1] - 9;
                    }
                    break;

                case Opcodes.JMPI: //need -9 since each iterate adds 9 bytes
                    registers[11] = registers[11] + param1 - 9;
                    break;

                case Opcodes.PRINTCR:
                    char c1 = (char) registers[param1];
                    System.out.print(c1);
                    break;

                case Opcodes.JMP:
                    registers[11] = registers[11] + registers[param1] - 9;
                    break;

                case Opcodes.JMPA:
                    registers[11] = param1 - 9;
                    break;

                case Opcodes.JLTI:
                    if (signFlag)
                    {
                        registers[11] += param1 - 9;
                    }
                    break;

                case Opcodes.JLTA:
                    if (signFlag)
                    {
                        registers[11] = param1 - 9;
                    }
                    break;

                case Opcodes.JGTI:
                    if (signFlag == false && zeroFlag == false)
                    {
                        registers[11] += param1 - 9;
                    }
                    break;

                case Opcodes.JGTA:
                    if (signFlag == false && zeroFlag == false)
                    {
                        registers[11] = param1 - 9;
                    }
                    break;

                case Opcodes.JEI:
                    if (zeroFlag)
                    {
                        registers[11] += param1 - 9;
                    }
                    break;

                case Opcodes.JEA:
                    if (zeroFlag)
                    {
                        registers[11] = param1 - 9;
                    }
                    break;

                case Opcodes.PUSHR: //push sp -4
                    if (registers[13] - 4 < stackFloor)
                    {
                        System.out.println("stack overflow");
                        running = false;
                        retReason = PROCESS_EXIT;
                        break;
                    }
                    memory.writeInt(registers[13], registers[param1]);
                    registers[13] -= 4;
                    break;

                case Opcodes.PUSHI:
                    if (registers[13] - 4 < stackFloor)
                    {
                        System.out.println("stack overflow");
                        running = false;
                        retReason = PROCESS_EXIT;
                        break;
                    }
                    memory.writeInt(registers[13], param1);
                    registers[13] -= 4;
                    break;

                case Opcodes.POPR: //pop sp +4
                    registers[13] += 4;
                    registers[param1] = memory.readInt(registers[13]);
                    break;

                case Opcodes.POPM:
                    registers[13] += 4;
                    int value = memory.readInt(registers[13]);
                    memory.writeInt(registers[param1], value);
                    break;

                case Opcodes.MOVMR:
                    registers[param1] = memory.readInt(registers[param2]);
                    break;

                case Opcodes.MOVRM:
                    memory.writeInt(registers[param1], registers[param2]);
                    break;

                case Opcodes.MOVMM:
                    int temp = memory.readInt(registers[param2]);
                    memory.writeInt(registers[param1], temp);
                    break;
                    

                case Opcodes.PRINTM:
                    System.out.println(memory.readInt(registers[param1]));
                    break;

                case Opcodes.PRINTCM:
                    char c2 = (char) memory.readInt(registers[param1]);
                    System.out.print(c2);
                    break;

                case Opcodes.CALL: //push ip+9 for next instruczt
                    if (registers[13] - 4 < stackFloor)
                    {
                        System.out.println("stack overflow");
                        running = false;
                        retReason = PROCESS_EXIT;
                        break;
                    }
                    memory.writeInt(registers[13], registers[11] + 9);
                    registers[13] -= 4; //deincment sp
                    //jmp to subroutine
                    registers[11] = registers[11] + registers[param1] - 9;
                    break;

                case Opcodes.CALLM:
                    if (registers[13] - 4 < stackFloor)
                    {
                        System.out.println("stack overflow");
                        running = false;
                        retReason = PROCESS_EXIT;
                        break;
                    }
                    memory.writeInt(registers[13], registers[11] + 9);
                    registers[13] -= 4;
                    registers[11] = registers[11] + memory.readInt(registers[param1]) - 9;
                    break;

                case Opcodes.RET:
                    registers[13] += 4;
                    //pop and jmp to return addr
                    registers[11] = memory.readInt(registers[13]) - 9;
                    break;

                case Opcodes.SLEEP:
                    sleepAmnt = registers[param1];
                    //need to add 9 b/c the process will return early
                    registers[11] += 9;
                    running = false;
                    retReason = PROCESS_SLEEP;
                    break;

                case Opcodes.INPUT:
                    Scanner scanner = new Scanner(System.in);
                    int inputNum = scanner.nextInt();
                    registers[param1] = inputNum;
                    registers[11] += 9; //same here proccess return early
                    running = false;
                    retReason = PROCESS_IO;
                    break;

                case Opcodes.INPUTC:
                    Scanner scanner2 = new Scanner(System.in);
                    String input = scanner2.nextLine();
                    registers[param1] = (int) input.charAt(0);
                    registers[11] += 9;
                    running = false;
                    retReason = PROCESS_IO;
                    break;

                case Opcodes.SETPRIORITY:
                    break;

                case Opcodes.SETPRIORITYI:
                    break;

                //mod 4 shared memory instruction
                case Opcodes.MAPSHAREDMEM:
                    int regNum = registers[param1];
                    if (regNum >= 0 && regNum < sharedPages.length) //mod 6
                    {
                        int lp = memory.findFreePage();
                        if (lp != -1)
                        {
                            memory.mapPage(lp, sharedPages[regNum]); //mod 6
                            registers[param2] = lp * 256;
                        }
                    }
                    break;

                //mod 4 lock instructions
                case Opcodes.ACQUIRELOCK:
                    int lkNum = registers[param1];
                    if (lkNum >= 0 && lkNum < 10)
                    {

                        if (lockList[lkNum] == -1)
                        {
                            lockList[lkNum] = registers[12];
                        }
                        else if (lockList[lkNum] != registers[12])
                        {
                            waitOn = lkNum;
                            registers[11] += 9;
                            running = false;
                            retReason = PROCESS_WAIT_LOCK;
                        }
                    }
                    break;

                case Opcodes.ACQUIRELOCKI:
                    if (param1 >= 0 && param1 < 10)
                    {
                        if (lockList[param1] == -1)
                        {
                            lockList[param1] = registers[12];
                        }
                        
                        else if (lockList[param1] != registers[12])
                        {
                            waitOn = param1;
                            registers[11] += 9;
                            running = false;
                            retReason = PROCESS_WAIT_LOCK;
                        }
                    }
                    break;

                case Opcodes.RELEASELOCK:
                    int rl = registers[param1];
                    if (rl >= 0 && rl < 10)
                    {
                        if (lockList[rl] == registers[12])
                        {
                            lockList[rl] = -1;
                        }
                    }
                    break;

                case Opcodes.RELEASELOCKI:
                    if (param1 >= 0 && param1 < 10)
                    {
                        if (lockList[param1] == registers[12])
                        {
                            lockList[param1] = -1;
                        }
                    }
                    break;

                //mod 4 event instructions
                case Opcodes.SIGNALEVENT:
                    int se = registers[param1];
                    if (se >= 0 && se < 10)
                    {
                        eventList[se] = true;
                    }
                    break;

                case Opcodes.SIGNALEVENTI:
                    if (param1 >= 0 && param1 < 10)
                    {
                        eventList[param1] = true;
                    }
                    break;

                case Opcodes.WAITEVENT:
                    int we = registers[param1];
                    if (we >= 0 && we < 10)
                    {
                        if (!eventList[we])
                        {
                            waitOn = we;
                            registers[11] += 9;
                            running = false;
                            retReason = PROCESS_WAIT_EVENT;
                        }
                    }
                    break;

                case Opcodes.WAITEVENTI:
                    if (param1 >= 0 && param1 < 10)
                    {
                        if (!eventList[param1])
                        {
                            waitOn = param1;
                            registers[11] += 9;
                            running = false;
                            retReason = PROCESS_WAIT_EVENT;
                        }
                    }
                    break;

                //mod 5 heap allocation
                case Opcodes.ALLOC:
                    int numBytes = registers[param1];
                    int hAddr = memory.heapAlloc(numBytes);
                    registers[param2] = hAddr;
                    break;

                case Opcodes.FREEMEMORY:
                    memory.heapFree(registers[param1]);
                    break;

                default:
                    System.out.println("Unknown opcode: " + opcode);
                    running = false;
                    break;
            }

            if (running)
            {
                registers[11] += 9; //mod 1 advance instucts
            }
            clock++;
            cycleCount++;
        }

        return retReason;
    }

    public int[] getRegs()
    {
        return registers;
    }

    public void setRegs(int[] registers)
    {
        this.registers = registers;
    }

    public boolean getSignFlag()
    {
        return signFlag;
    }

    public void setSignFlag(boolean signFlag)
    {
        this.signFlag = signFlag;
    }

    public boolean getZeroFlag()
    {
        return zeroFlag;
    }

    public void setZeroFlag(boolean zeroFlag)
    {
        this.zeroFlag = zeroFlag;
    }

    public int getClock()
    {
        return clock;
    }

    public void setClock(int clock)
    {
        this.clock = clock;
    }

    public Memory getMem()
    {
        return memory;
    }

    public int getSleepTime()
    {
        return sleepAmnt;
    }

    public void setMem(Memory memory)
    {
        this.memory = memory;
    }

    public int getStackFloor()
    {
        return stackFloor;
    }

    
    public void setStackFloor(int stackFloor)
    {
        this.stackFloor = stackFloor;
    }

    //mod 4 getters and setters for lockList eventList shared mem
    public void setLockList(int[] lockList)
    {
        this.lockList = lockList;
    }

    public void setEventList(boolean[] eventList)
    {
        this.eventList = eventList;
    }

    public void setSharedPages(int[] sharedPages) //mod 6
    {
        this.sharedPages = sharedPages;
    }

    public int getWaitOn()
    {
        return waitOn;
    }

    public void printRegisters()
    {
        System.out.println("registers");
        for (int i = 1; i <= 10; i++)
        {
            System.out.println("r" + i + " = " + registers[i]);
        }
        System.out.println("IP (r11) = " + registers[11]);
        System.out.println("clock = " + clock);
    }
}
