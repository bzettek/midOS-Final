// Brandon Zettek z2015083
// PhysicalMemory.java
// PCB stores saved state for a process

public class Process
{
    public static final int NEW = 0; //created
    public static final int READY = 1; //waiting in queue for cpu
    public static final int RUNNING = 2; //currently excuting cpu
    public static final int WAITING_ASLEEP = 3;
    public static final int WAITING_LOCK = 4;
    public static final int WAITING_EVENT = 5;
    public static final int TERMINATED = 6;

    private int processId;
    private int codeLength;
    private int stackMax;
    private int dataMax;
    private int heapStart;
    private int heapEnd;
    private int totalMemory;

    private int[] registers;
    private boolean signFlag;
    private boolean zeroFlag;

    private int state;
    private int priority;
    private int quantum;
    private int totalCycles;
    private int sleepLeft;
    private int switches;
    private int stackFloor;
    private int waitId; //mod 4 which lock or event we are waiting on

    private Memory memory;

    public Process(int processId, Memory memory)
    {
        this.processId = processId;
        this.memory = memory;
        this.registers = new int[15];
        registers[11] = 0;
        registers[12] = processId; //mod 4 set process id register for lock ownership
        registers[13] = memory.getSize() - 4;
        registers[14] = memory.getNextAddress();
        this.signFlag = false;
        this.zeroFlag = false;
        this.state = NEW;
        this.priority = 1;
        this.quantum = 20;
        this.totalCycles = 0;
        this.switches = 0;
        this.sleepLeft = 0;
        this.stackFloor = 0;
    }

    //identity
    public int getProcessId()
    {
        return processId;
    }


    public int getCodeLength()
    {
        return codeLength;
    }

    public void setCodeLength(int codeLength)
    {
        this.codeLength = codeLength;
    }

    public int getStackMax()
    {
        return stackMax;
    }


    public void setStackMax(int stackMax)
    {
        this.stackMax = stackMax;
    }

    public int getDataMax()
    {
        return dataMax;
    }

    public void setDataMax(int dataMax)
    {
        this.dataMax = dataMax;
    }

    public int getHeapStart()
    {
        return heapStart;
    }

    public void setHeapStart(int heapStart)
    {
        this.heapStart = heapStart;
    }

    public int getHeapEnd()
    {
        return heapEnd;
    }

    public void setHeapEnd(int heapEnd)
    {
        this.heapEnd = heapEnd;
    }

    public int getTotalMemory()
    {
        return totalMemory;
    }

    public void setTotalMemory(int totalMemory)
    {
        this.totalMemory = totalMemory;
    }

    //cpu
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

    // schedul
    public int getState()
    {
        return state;
    }

    public void setState(int state)
    {
        this.state = state;
    }

    public int getPriority()
    {
        return priority;
    }

    public void setPriority(int priority)
    {
        this.priority = priority;
    }

    public int getQuantum()
    {
        return quantum;
    }

    public void setQuantum(int quantum)
    {
        this.quantum = quantum;
    }

    public int getTotalCycles()
    {
        return totalCycles;
    }

    public void setTotalCycles(int totalCycles)
    {
        this.totalCycles = totalCycles;
    }

    public int getSleepLeft()
    {
        return sleepLeft;
    }

    public void setSleepLeft(int sleepLeft)
    {
        this.sleepLeft = sleepLeft;
    }

    public int getSwitches()
    {
        return switches;
    }

    public void setSwitches(int switches)
    {
        this.switches = switches;
    }

    //mem
    public Memory getMem()
    {
        return memory;
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

    //mod 4 getter/setter for what lock or event process is waiting on
    public int getWaitId()
    {
        return waitId;
    }

    public void setWaitId(int waitId)
    {

        this.waitId = waitId;
    }
    
}
