// Brandon Zettek z2015083
// Memory.java
//started in mod 1 extended by mod 2, mod 3, mod 4, mod 5, mod 6
//MMU translates logical(virtual) addrs to physical
//uses PhysicalMemory raw byte array
//and uses Page[] pageTbl that maps logical to physical

public class Memory
{
    private int nextAddress; //stores where next instuct during prgm load mod 1
    private int[] pageTbl; //mod 2 and mod 6 now stores vpage nums, -1 = unallocated
    private int PAGE_SIZE = 256; //mod 2
    private int virtSize; //mod 6
    private int processId; //mod 6 owner pid for vpage alloc
    private VirtualMemory vm; //mod 6

    //mod 5 heap tracking one slot per heap page
    private int heapBegin;
    private int heapStop;
    private boolean[] heapTaken;
    private int[] heapSizes;

    //mod 2
    //mod 6 new constructor uses VirtualMemory
    public Memory(VirtualMemory vm, int virtSize, int processId)
    {
        this.vm = vm;
        this.physMem = vm.getPhysMem(); //mod 6
        this.virtSize = virtSize;
        this.processId = processId;
        this.nextAddress = 0;
        int numPages = virtSize / PAGE_SIZE; //mod 2
        pageTbl = new int[numPages]; //mod 2
        for (int i = 0; i < numPages; i++) //mod 2
        {
            pageTbl[i] = -1; //unallocated
        }
    }

    public int getSize() //mod 1
    {
        return virtSize; //mod 6 process sees virt size
    }

    public int getNextAddress() //mod 1
    {
        return nextAddress;
    }

    //allocates logical pages from 0 asks physicalMemory for a free page
    //records it to page table mod 2
    public void allocPages(int numOfPages)
    {
        int logPage = 0;
        while (logPage < numOfPages)
        {
            int vpage = vm.getVPage(processId); // mod 6
            if (vpage == -1)
            {
                System.out.println("out of memory");
                return;
            }
            pageTbl[logPage] = vpage; // mod 6
            logPage++;
        }
    }

    //same as allocPages but for specific page num which is used by
    // the data sedment heap and stack mod 2
    public void addPageAt(int logicalPage)
    {
        int vpage = vm.getVPage(processId); // mod 6
        if (vpage == -1)
        {
            System.out.println("out of memory");
            return;
        }
        pageTbl[logicalPage] = vpage; // mod 6
    }

    //addr translation whic encodes two things page num and offset mod 2
    private int translate(int input, boolean isWrite) //mod 6 isWrite
    {
        int page = (input >> 8) & 0xFFFFFF;
        int offset = input & 0xFF;

        //make sure page is in range
        if (page < 0 || page >= pageTbl.length)
        {
            System.out.println("segfault: page " + page + " out of bounds");
            System.exit(1);
        }

        //make sure the page was actually allocated unollcated memory is fatal
        if (pageTbl[page] == -1) // mod 6
        {
            System.out.println("segfault: page " + page + " not allocated");
            System.exit(1);
        }

        //mod 6 may cause a swap in and mark pages dirty and use VirualMemory
        //instead of direct physical mem
        int frame = vm.usePage(pageTbl[page], isWrite);
        return frame * PAGE_SIZE + offset;
    }

    //one instruction 9 bytes total in big endian mod 1
    public void storeInstruction(int opcode, int param1, int param2)
    {
        //opcode 1 byte
        rawWriteByte(nextAddress, (byte) opcode); //mod 6
        nextAddress++;

        //param1 4 bytes
        rawWriteByte(nextAddress, (byte) (param1 >> 24)); //msb
        nextAddress++;
        rawWriteByte(nextAddress, (byte) (param1 >> 16));
        nextAddress++;
        rawWriteByte(nextAddress, (byte) (param1 >> 8));
        nextAddress++;
        rawWriteByte(nextAddress, (byte) param1);
        nextAddress++;

        //param2 4 bytes
        rawWriteByte(nextAddress, (byte) (param2 >> 24)); //msb
        nextAddress++;
        rawWriteByte(nextAddress, (byte) (param2 >> 16));
        nextAddress++;
        rawWriteByte(nextAddress, (byte) (param2 >> 8));
        nextAddress++;
        rawWriteByte(nextAddress, (byte) param2);
        nextAddress++;
    }

    // mod 6 helper to go through translate as a write
    private void rawWriteByte(int address, byte value)
    {
        physMem.writeByte(translate(address, true), value);
    }

    private PhysicalMemory physMem; //mod 6

    public byte readByte(int address) //mod 1
    {

        return physMem.readByte(translate(address, false)); //mod 6
    }

    //takes 4 bytes and combines into int
    public int readInt(int address) //mod 1
    {
        //changed by mod6
        byte b0 = physMem.readByte(translate(address, false));
        byte b1 = physMem.readByte(translate(address + 1, false));
        byte b2 = physMem.readByte(translate(address + 2, false));
        byte b3 = physMem.readByte(translate(address + 3, false));

        //converts signed to unsighed since java uses signed
        int b0Num = (int) (b0 & 0xFF) << 24;
        int b1Num = (b1 & 0xFF) << 16;
        int b2Num = (b2 & 0xFF) << 8;
        int b3Num = (b3 & 0xFF);

        //same as using or
        int result = b0Num + b1Num + b2Num + b3Num;

        return result;
    }

    //write int to memory at address
    public void writeInt(int address, int value) //mod 1 inital
    {
        //changed by mod 6
        physMem.writeByte(translate(address, true),     (byte) (value >> 24));
        physMem.writeByte(translate(address + 1, true), (byte) (value >> 16));
        physMem.writeByte(translate(address + 2, true), (byte) (value >> 8));
        physMem.writeByte(translate(address + 3, true), (byte) (value));
    }

    //test method
    public void printMemory(int start, int length) //mod 1 inital
    {
        System.out.println("Memory dump from " + start + " to " + (start + length - 1) + ":");
        for (int i = start; i < start + length && i < virtSize; i++) // mod 6
        {
            
            System.out.print(physMem.readByte(translate(i, false)) + " "); //mod 6
            if ((i - start + 1) % 9 == 0)
            {
                System.out.println();  //new line every 9 bytes
            }
        }
        System.out.println();
    }


    //mod 6 map a logical page to a specific shared vpage for shared mem
    public void mapPage(int logicalPage, int vpage)
    {
        pageTbl[logicalPage] = vpage;
    }

    //mod 4 find first unmapped logical page
    public int findFreePage()
    {
        for (int i = 0; i < pageTbl.length; i++)
        {
            if (pageTbl[i] == -1) // mod 6
            {
                return i;
            }
        }
        return -1;
    }

    //test method mod 2
    public void swapPages()
    {
    //swap logical page 1 and page 2's physical locations
    int temp = pageTbl[1]; //mod 6
    pageTbl[1] = pageTbl[2]; //mod 6
    pageTbl[2] = temp; //mod 6
    System.out.println("scrambled page table:");
    System.out.println("logical page 1 tovpage: " + pageTbl[1]); //mod 6
    System.out.println("logical Page 2 to vpage: " + pageTbl[2]); //mod 6
    }

    //mod 5 set heap range and init tracking arrays
    public void setupHeap(int start, int end)
    {
        heapBegin = start;
        heapStop = end;
        int numHeapPages = (end - start + 1) / PAGE_SIZE;
        heapTaken = new boolean[numHeapPages];
        heapSizes = new int[numHeapPages];
    }

    //mod 5 allocate n bytes on the heap, return start addr or 0 on fail
    public int heapAlloc(int size)
    {
        if (size <= 0)
        {
            return 0;
        }
        int pagesNeeded = (size + PAGE_SIZE - 1) / PAGE_SIZE;
        //find pagesNeeded free slots in a row
        for (int i = 0; i <= heapTaken.length - pagesNeeded; i++)
        {
            boolean fits = true;
            for (int j = 0; j < pagesNeeded; j++)
            {
                if (heapTaken[i + j])
                {
                    fits = false;
                }
            }
            if (fits)
            {
                for (int j = 0; j < pagesNeeded; j++)
                {
                    heapTaken[i + j] = true;
                }
                heapSizes[i] = pagesNeeded;
                return heapBegin + i * PAGE_SIZE;
            }
        }
        return 0;
    }

    //mod 5 free a previously allocated heap block
    public void heapFree(int addr)
    {
        if (addr < heapBegin || addr > heapStop)
        {
            return;
        }
        int idx = (addr - heapBegin) / PAGE_SIZE;
        int n = heapSizes[idx];
        if (n == 0)
        {
            return;
        }
        for (int j = 0; j < n; j++)
        {
            heapTaken[idx + j] = false;
        }
        heapSizes[idx] = 0;
    }
}
