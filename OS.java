// Brandon Zettek z2015083
// OS.java
// Starts here and loads .asm files, runs the scheduler loop
// calls loadProg which creates Memory and Process Objects
// controls scheduler loop, context switch, run contxt switch out
import java.io.File;
import java.util.Scanner;
import java.util.ArrayList;
import java.io.FileNotFoundException;

public class OS
{
    public static void main(String[] args)
    {
        //mod 6 need phys and virt sizes now
        if (args.length < 3)
        {
            System.out.println("Usage: java OS <physMemSize> <virtMemSize> <program1.asm> <program2.asm> ...");
            return;
        }

        int physMemSize = Integer.parseInt(args[0]); //mod 6
        int virtMemSize = Integer.parseInt(args[1]); //mod 6
        int memTotal = virtMemSize; //mod 6 processes see virt size

        //mod 4 one shared physical memory for all processes
        PhysicalMemory physMem = new PhysicalMemory(physMemSize); //mod 6

        //mod 6 virtual memory wrapper
        VirtualMemory vm = new VirtualMemory(physMem, virtMemSize);

        //mod 4 allocate 2 shared memory pages (
        int sharedPage0 = vm.getSharedPage(); //mod 6
        int sharedPage1 = vm.getSharedPage(); //mod 6
        int[] sharedPages = new int[]{sharedPage0, sharedPage1}; //mod 6

        //mod 4 10 lockList, -1 means free
        int[] lockList = new int[10];
        for (int i = 0; i < 10; i++)
        {
            lockList[i] = -1;
        }


        //mod 4 10 eventList, false means not signaled
        boolean[] eventList = new boolean[10];

        //load all programs
        ArrayList<Process> processes = new ArrayList<Process>();
        for (int i = 2; i < args.length; i++) //mod 6 args shift
        {
            Process p = loadProg(args[i], memTotal, i - 1, vm); //mod 6
            processes.add(p);
        }

        // set all to ready
        for (int i = 0; i < processes.size(); i++)
        {
            processes.get(i).setState(Process.READY);
        }

        //make cpu with first process memory
        CPU cpu = new CPU(processes.get(0).getMem());

        // scheduler loop
        boolean done = false;
        while (!done)
        {
            //tick down sleep counters
            for (int i = 0; i < processes.size(); i++)
            {
                Process p = processes.get(i);
                if (p.getState() == Process.WAITING_ASLEEP)
                {
                    p.setSleepLeft(p.getSleepLeft() - 1);
                    if (p.getSleepLeft() <= 0)
                    {
                        p.setState(Process.READY);
                    }
                }
            }

            //mod 4 wake up processes waiting on lockList
            for (int i = 0; i < processes.size(); i++)
            {
                Process p = processes.get(i);
                if (p.getState() == Process.WAITING_LOCK)
                {
                    int lockNum = p.getWaitId();
                    if (lockList[lockNum] == -1)
                    {
                        lockList[lockNum] = p.getProcessId();
                        p.setState(Process.READY);
                    }
                }
            }

            //mod 4 wake up processes waiting on eventList
            for (int i = 0; i < processes.size(); i++)
            {
                Process p = processes.get(i);
                if (p.getState() == Process.WAITING_EVENT)
                {
                    int eventNum = p.getWaitId();
                    if (eventList[eventNum])
                    {
                        p.setState(Process.READY);
                    }
                }
            }

            // find highest priority ready process
            Process current = null;
            for (int i = 0; i < processes.size(); i++)
            {
                Process p = processes.get(i);
                if (p.getState() == Process.READY)
                {
                    if (current == null || p.getPriority() > current.getPriority())
                    {
                        current = p;
                    }
                }
            }

            // if nobody is ready just keep looping so sleeps tick down
            if (current == null)
            {
                done = true;
                for (int i = 0; i < processes.size(); i++)
                {
                    if (processes.get(i).getState() != Process.TERMINATED)
                    {
                        done = false;
                    }
                }

                continue;
            }


            //clone is needed b/c process and cpu cannot share the same
            //modifying 1 would do the other corupting it
            cpu.setRegs(current.getRegs().clone());
            cpu.setSignFlag(current.getSignFlag());
            cpu.setZeroFlag(current.getZeroFlag());
            cpu.setMem(current.getMem());
            cpu.setStackFloor(current.getStackFloor());
            //mod 4 give cpu access to lockList eventList and shared mem
            cpu.setLockList(lockList);
            cpu.setEventList(eventList);
            cpu.setSharedPages(sharedPages); //mod 6
            current.setState(Process.RUNNING);

            // run it
            cpu.setClock(0);
            int stopCode = cpu.run(current.getQuantum());

            //context switch out save cpu state back to process
            current.setRegs(cpu.getRegs().clone());
            current.setSignFlag(cpu.getSignFlag());
            current.setZeroFlag(cpu.getZeroFlag());
            current.setTotalCycles(current.getTotalCycles() + cpu.getClock());
            current.setSwitches(current.getSwitches() + 1);

            // handle why it stopped
            if (stopCode == CPU.QUANTUM_EXPIRED)
            {
                
                current.setState(Process.READY);
            }
            else if (stopCode == CPU.PROCESS_EXIT)
            {
                current.setState(Process.TERMINATED);
                //mod 4 release any lockList held by this process
                for (int i = 0; i < 10; i++)
                {
                    if (lockList[i] == current.getProcessId())
                    {
                        lockList[i] = -1;
                    }
                }
            }
            else if (stopCode == CPU.PROCESS_SLEEP)
            {
                current.setState(Process.WAITING_ASLEEP);
                current.setSleepLeft(cpu.getSleepTime());
            }
            else if (stopCode == CPU.PROCESS_IO)
            {
                current.setState(Process.READY);
            }
            //mod 4 handle lock and event waits
            else if (stopCode == CPU.PROCESS_WAIT_LOCK)
            {
                current.setState(Process.WAITING_LOCK);
                current.setWaitId(cpu.getWaitOn());
            }
            else if (stopCode == CPU.PROCESS_WAIT_EVENT)
            {
                current.setState(Process.WAITING_EVENT);
                current.setWaitId(cpu.getWaitOn());
            }

            // check if everything is done
            done = true;
            for (int i = 0; i < processes.size(); i++)
            {
                if (processes.get(i).getState() != Process.TERMINATED)
                {
                    done = false;
                }
            }
        }

        // print stats
        System.out.println("Process Stats");
        for (int i = 0; i < processes.size(); i++)
        {
            Process p = processes.get(i);
            System.out.println("Process " + p.getProcessId());
            System.out.println("  Clock cycles: " + p.getTotalCycles());
            System.out.println("  Context switches: " + p.getSwitches());
            System.out.println("  State: " + p.getState());
        }
    }

    public static Process loadProg(String filename, int memTotal, int pid, VirtualMemory vm) //mod 6
    {
        Memory memory = new Memory(vm, memTotal, pid); //mod 6
        int codeSize = 0;
        int heapBegin = 0;
        int heapStop = 0;
        int stackBegin = 0;

        try
        {
            //count first since we don't know amnt to allocate mod 1
            Scanner preScan = new Scanner(new File(filename));
            int lineCount = 0;
            while (preScan.hasNextLine())
            {
                String line = preScan.nextLine();
                if (line.trim().isEmpty())
                {
                    continue;
                }
                if (line.contains(";"))
                {
                    line = line.substring(0, line.indexOf(";"));
                }
                if (line.trim().isEmpty())
                {
                    continue;
                }
                lineCount++;
            }
            preScan.close();

            //code segment
            codeSize = lineCount * 9;
            //below wil always round up mod 2
            int numPages = (codeSize + 255) / 256; //amnt of pages needed mod 2
            memory.allocPages(numPages); //mod 2

            //global data segment 1 page after code
            int dataStart = numPages;
            memory.addPageAt(dataStart);

            //heap segment mod 5 4 pages after data
            int heapIdx = dataStart + 1;
            int heapPages = 4; //mod 5
            for (int hp = 0; hp < heapPages; hp++) //mod 5
            {
                memory.addPageAt(heapIdx + hp); //mod 5
            }
            heapBegin = heapIdx * 256;
            heapStop = heapBegin + (heapPages * 256) - 1; //mod 5
            memory.setupHeap(heapBegin, heapStop); //mod 5

            //stack segment last page grows downward
            int stackPage = (memTotal / 256) - 1; //mod 1
            memory.addPageAt(stackPage);
            stackBegin = stackPage * 256;

            Scanner scan = new Scanner(new File(filename));
            while (scan.hasNextLine())
            {
                String line = scan.nextLine();
                if (line.trim().isEmpty())
                {
                    continue;
                }
                if (line.contains(";"))
                {
                    line = line.substring(0, line.indexOf(";"));
                }
                if (line.trim().isEmpty())
                {
                    continue;
                }

                String cl = line.replace(",", "").trim();
                String[] tokens = cl.split("\\s+");
                String opcode = tokens[0];
                String param1 = "";
                String param2 = "";
                if (tokens.length > 1)
                {
                    param1 = tokens[1];
                }
                if (tokens.length > 2)
                {
                    param2 = tokens[2];
                }

                int opcodeNum = 0;
                if (opcode.equalsIgnoreCase("incr"))
                {
                    opcodeNum = Opcodes.INCR;
                }
                else if (opcode.equalsIgnoreCase("addi"))
                {
                    opcodeNum = Opcodes.ADDI;
                }
                else if (opcode.equalsIgnoreCase("addr"))
                {
                    opcodeNum = Opcodes.ADDR;
                }
                else if (opcode.equalsIgnoreCase("pushr"))
                {
                    opcodeNum = Opcodes.PUSHR;
                }
                else if (opcode.equalsIgnoreCase("pushi"))
                {
                    opcodeNum = Opcodes.PUSHI;
                }
                else if (opcode.equalsIgnoreCase("movi"))
                {
                    opcodeNum = Opcodes.MOVI;
                }
                else if (opcode.equalsIgnoreCase("movr"))
                {
                    opcodeNum = Opcodes.MOVR;
                }
                else if (opcode.equalsIgnoreCase("movmr"))
                {
                    opcodeNum = Opcodes.MOVMR;
                }
                else if (opcode.equalsIgnoreCase("movrm"))
                {
                    opcodeNum = Opcodes.MOVRM;
                }
                else if (opcode.equalsIgnoreCase("movmm"))
                {
                    opcodeNum = Opcodes.MOVMM;
                }
                else if (opcode.equalsIgnoreCase("printr"))
                {
                    opcodeNum = Opcodes.PRINTR;
                }
                else if (opcode.equalsIgnoreCase("printm"))
                {
                    opcodeNum = Opcodes.PRINTM;
                }
                else if (opcode.equalsIgnoreCase("printcr"))
                {
                    opcodeNum = Opcodes.PRINTCR;
                }
                else if (opcode.equalsIgnoreCase("printcm"))
                {
                    opcodeNum = Opcodes.PRINTCM;
                }
                else if (opcode.equalsIgnoreCase("jmp"))
                {
                    opcodeNum = Opcodes.JMP;
                }
                else if (opcode.equalsIgnoreCase("jmpi"))
                {
                    opcodeNum = Opcodes.JMPI;
                }
                else if (opcode.equalsIgnoreCase("jmpa"))
                {
                    opcodeNum = Opcodes.JMPA;
                }
                else if (opcode.equalsIgnoreCase("cmpi"))
                {
                    opcodeNum = Opcodes.CMPI;
                }
                else if (opcode.equalsIgnoreCase("cmpr"))
                {
                    opcodeNum = Opcodes.CMPR;
                }
                else if (opcode.equalsIgnoreCase("jlt"))
                {
                    opcodeNum = Opcodes.JLT;
                }
                else if (opcode.equalsIgnoreCase("jlti"))
                {
                    opcodeNum = Opcodes.JLTI;
                }
                else if (opcode.equalsIgnoreCase("jlta"))
                {
                    opcodeNum = Opcodes.JLTA;
                }
                else if (opcode.equalsIgnoreCase("jgt"))
                {
                    opcodeNum = Opcodes.JGT;
                }
                else if (opcode.equalsIgnoreCase("jgti"))
                {
                    opcodeNum = Opcodes.JGTI;
                }
                else if (opcode.equalsIgnoreCase("jgta"))
                {
                    opcodeNum = Opcodes.JGTA;
                }
                else if (opcode.equalsIgnoreCase("je"))
                {
                    opcodeNum = Opcodes.JE;
                }
                else if (opcode.equalsIgnoreCase("jei"))
                {
                    opcodeNum = Opcodes.JEI;
                }
                else if (opcode.equalsIgnoreCase("jea"))
                {
                    opcodeNum = Opcodes.JEA;
                }
                else if (opcode.equalsIgnoreCase("call"))
                {
                    opcodeNum = Opcodes.CALL;
                }
                else if (opcode.equalsIgnoreCase("callm"))
                {
                    opcodeNum = Opcodes.CALLM;
                }
                else if (opcode.equalsIgnoreCase("ret"))
                {
                    opcodeNum = Opcodes.RET;
                }
                else if (opcode.equalsIgnoreCase("exit"))
                {
                    opcodeNum = Opcodes.EXIT;
                }
                else if (opcode.equalsIgnoreCase("popr"))
                {
                    opcodeNum = Opcodes.POPR;
                }
                else if (opcode.equalsIgnoreCase("popm"))
                {
                    opcodeNum = Opcodes.POPM;
                }
                else if (opcode.equalsIgnoreCase("sleep"))
                {
                    opcodeNum = Opcodes.SLEEP;
                }
                else if (opcode.equalsIgnoreCase("input"))
                {
                    opcodeNum = Opcodes.INPUT;
                }
                
                else if (opcode.equalsIgnoreCase("inputc"))
                {
                    opcodeNum = Opcodes.INPUTC;
                }
                
                //not implemented
                else if (opcode.equalsIgnoreCase("setpriority"))
                {
                    opcodeNum = Opcodes.SETPRIORITY;
                }
                //not implemented
                else if (opcode.equalsIgnoreCase("setpriorityi"))
                {
                    opcodeNum = Opcodes.SETPRIORITYI;
                }
                //mod 4 shared memory lockList and eventList parser
                else if (opcode.equalsIgnoreCase("mapsharedmem"))
                {
                    opcodeNum = Opcodes.MAPSHAREDMEM;
                }
                else if (opcode.equalsIgnoreCase("acquirelock"))
                {
                    opcodeNum = Opcodes.ACQUIRELOCK;
                }
                else if (opcode.equalsIgnoreCase("acquirelocki"))
                {
                    opcodeNum = Opcodes.ACQUIRELOCKI;
                }
                else if (opcode.equalsIgnoreCase("releaselock"))
                {
                    opcodeNum = Opcodes.RELEASELOCK;
                }
                else if (opcode.equalsIgnoreCase("releaselocki"))
                {
                    opcodeNum = Opcodes.RELEASELOCKI;
                }
                else if (opcode.equalsIgnoreCase("signalevent"))
                {
                    opcodeNum = Opcodes.SIGNALEVENT;
                }
                else if (opcode.equalsIgnoreCase("signaleventi"))
                {
                    opcodeNum = Opcodes.SIGNALEVENTI;
                }
                else if (opcode.equalsIgnoreCase("waitevent"))
                {
                    opcodeNum = Opcodes.WAITEVENT;
                }
                else if (opcode.equalsIgnoreCase("waiteventi"))
                {
                    opcodeNum = Opcodes.WAITEVENTI;
                }
                //mod 5 - heap allocation parser
                else if (opcode.equalsIgnoreCase("alloc"))
                {
                    opcodeNum = Opcodes.ALLOC;
                }
                else if (opcode.equalsIgnoreCase("freememory"))
                {
                    opcodeNum = Opcodes.FREEMEMORY;
                }

                //catches bad assembly at load time runtime error
                if (opcodeNum == 0)
                {
                    System.out.println("Unknown instruction " + opcode);
                    System.exit(1);
                }
                //mod 1
                int param1Value = 0;
                if (!param1.isEmpty())
                {
                    if (param1.startsWith("r"))
                    {
                        param1Value = Integer.parseInt(param1.substring(1));
                    }
                    else if (param1.startsWith("#"))
                    {
                        param1Value = Integer.parseInt(param1.substring(1));
                    }
                    else if (param1.startsWith("@"))
                    {
                        param1Value = (int) param1.charAt(1);
                    }
                }

                int param2Value = 0;
                if (!param2.isEmpty())
                {
                    if (param2.startsWith("r"))
                    {
                        param2Value = Integer.parseInt(param2.substring(1));
                    }
                    else if (param2.startsWith("#"))
                    {
                        param2Value = Integer.parseInt(param2.substring(1));
                    }
                    else if (param2.startsWith("@"))
                    {
                        param2Value = (int) param2.charAt(1);
                    }
                }

                memory.storeInstruction(opcodeNum, param1Value, param2Value);
            }
            scan.close();
        }
        catch (FileNotFoundException e)
        {
            System.out.println("File not found: " + filename);
        }

        Process process = new Process(pid, memory);
        process.setCodeLength(codeSize);
        process.setDataMax(256);
        process.setHeapStart(heapBegin);
        process.setHeapEnd(heapStop);
        process.setStackMax(256);
        process.setStackFloor(stackBegin);
        process.setTotalMemory(memTotal);
        return process;
    }
}
