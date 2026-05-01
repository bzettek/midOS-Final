// Brandon Zettek z2015083
// created during mod 1
// PhysicalMemory.java
// raw storage  byte array and page alloc tracker
//mod 2 uses it in memory.java and mod 6 wraps it in virtual mem

public class PhysicalMemory
{
    private byte[] memory; //ram all data is in here
    private int[] pageUsed; //0=free 1=used
    private int PAGE_SIZE = 256;

    public PhysicalMemory(int size)
    {
        memory = new byte[size]; //4096 gives us 16 pages
        int numPhysPage = size / PAGE_SIZE;
        pageUsed = new int[numPhysPage];
    }

    public int getSize()
    {
        return memory.length;
    }

    //must mark page taken b4 return so in mod 3 2 processes dont use the
    //same free frame added in mod 2
    public int getFreeFrame()
    {
        int i = 0;
        while(i < pageUsed.length)
        {
            if (pageUsed[i] == 0)
            {
                pageUsed[i] = 1; //taken
                return i; //physical page num
            }
            i++;
        }
        return -1; //out of mem
    }


    public byte readByte(int address)
    {
        return memory[address];
    }

    public void writeByte(int address, byte value)
    {
        memory[address] = value;
    }

    //takes 4 bytes and combines into int
    public int readInt(int address)
    {
        byte b0 = memory[address];
        byte b1 = memory[address + 1];
        byte b2 = memory[address + 2];
        byte b3 = memory[address + 3];

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
    public void writeInt(int address, int value)
    {
        memory[address]     = (byte) (value >> 24);
        memory[address + 1] = (byte) (value >> 16);
        memory[address + 2] = (byte) (value >> 8);
        memory[address + 3] = (byte) (value);
    }

    public void printMemory(int start, int length)
    {
        System.out.println("Memory dump from " + start + " to " + (start + length - 1) + ":");
        for (int i = start; i < start + length && i < memory.length; i++)
        {
            System.out.print(memory[i] + " ");
            if ((i - start + 1) % 9 == 0)
            {
                System.out.println();  //new line every 9 bytes
            }
        }

        System.out.println();
        
    }

}
