// Brandon Zettek z2015083
// VirtualMemory.java
// mod 6 added this for virtual memory
// needed so we can use more memory than we actually have
// disk is a big byte arraay

public class VirtualMemory
{
    private int PAGE_SIZE = 256;

    private PhysicalMemory physMem;
    private Page[] pageList;     //all virtual pages one per virt page
    private byte[] disk;         //fake disk
    private int frameCount;
    private int[] frameMap;      //-1 if empty
    private int useCount;        //goes up each time a page is used for LRU

    //keep track of swaps
    private int faults;
    private int diskReads;
    private int diskWrites;

    public VirtualMemory(PhysicalMemory physMem, int virtSize)
    {
        this.physMem = physMem;
        int numVPages = virtSize / PAGE_SIZE;
        pageList = new Page[numVPages];
        //init all pages
        int i = 0;
        while (i < numVPages)
        {
            pageList[i] = new Page();
            i++;
        }
        disk = new byte[virtSize];
        frameCount = physMem.getSize() / PAGE_SIZE;
        frameMap = new int[frameCount];
        //mark all frames as empty
        int j = 0;
        while (j < frameCount)
        {
            frameMap[j] = -1;
            j++;
        }
        useCount = 0;
    }

    //find a free vpage and mark it as owned by the process mod 6
    public int getVPage(int processId)
    {
        int i = 0;
        while (i < pageList.length)
        {
            Page p = pageList[i];
            if (p.getProcessId() == -1)
            {
                p.setProcessId(processId);
                return i;
            }
            i++;
        }
        return -1; //no free vpages left
    }

    //same as above but for shared mem pages
    //pinned so LRU cant kick them out mod 6
    public int getSharedPage()
    {
        int i = 0;
        while (i < pageList.length)
        {
            Page p = pageList[i];
            if (p.getProcessId() == -1)
            {
                p.setProcessId(-2); //-2 means shared not a real process
                p.setPinned(true);
                return i;
            }
            i++;
        }
        return -1;
    }

    //main thing used by Memory.java to get the phys frame for a vpage
    //does the swap in if the page isnt in ram mod 6
    public int usePage(int vpage, boolean isWrite)
    {
        Page p = pageList[vpage];

        //page not in ram, need to bring it in
        if (p.isValid() == false)
        {
            swapIn(vpage);
            faults++;
        }

        //update the time it was last touched for LRU
        useCount++;
        p.setLastUsed(useCount);

        //if writing mark the page dirty so we save it later
        if (isWrite == true)
        {
            p.setDirty(true);
        }

        return p.getPhysFrame();
    }

    //loads a page from disk into a phys frame mod 6
    private void swapIn(int vpage)
    {
        //look for an empty frame first
        int frame = -1;
        int i = 0;
        while (i < frameCount)
        {
            if (frameMap[i] == -1)
            {
                frame = i;
                break;
            }
            i++;
        }

        //no empty frame
        if (frame == -1)
        {
            frame = evictOldest();
        }

        //copy the page bytes from disk into the phys frame
        int frameStart = frame * PAGE_SIZE;
        int diskStart = vpage * PAGE_SIZE;
        int k = 0;
        while (k < PAGE_SIZE)
        {
            physMem.writeByte(frameStart + k, disk[diskStart + k]);
            k++;
        }
        diskReads++;

        //update the page to say its valid and which frame its in
        Page p = pageList[vpage];
        p.setValid(true);
        p.setDirty(false); //just loaded so its clean
        p.setPhysFrame(frame);
        frameMap[frame] = vpage;
    }

    //finds the least recently used frame to kick out mod 6
    //pinned pages cant be evicted
    private int evictOldest()
    {
        int victimFrame = -1;
        int victimVPage = -1;
        long oldestTime = Long.MAX_VALUE;

        int i = 0;
        while (i < frameCount)
        {
            int vp = frameMap[i];
            if (vp == -1)
            {
                i++;
                continue; //frame is empty skip it
            }
            Page p = pageList[vp];
            if (p.isPinned() == true)
            {
                i++;
                continue; //cant evict shared pages
            }
            //track the oldest one
            if (p.getLastUsed() < oldestTime)
            {
                oldestTime = p.getLastUsed();
                victimFrame = i;
                victimVPage = vp;
            }
            i++;
        }
        

        Page victim = pageList[victimVPage];

        //only write back to disk if the page was written to
        //clean pages not saved since they havent changed
        if (victim.isDirty() == true)
        {
            int frameStart = victimFrame * PAGE_SIZE;
            int diskStart = victimVPage * PAGE_SIZE;
            int k = 0;
            while (k < PAGE_SIZE)
            {
                disk[diskStart + k] = physMem.readByte(frameStart + k);
                k++;
            }
            diskWrites++;
        }

        //clear the frame so someone else can use it
        victim.setValid(false);
        victim.setPhysFrame(-1);
        frameMap[victimFrame] = -1;

        return victimFrame;
    }

    public PhysicalMemory getPhysMem() { return physMem; }
    public int pageCount() { return pageList.length; }
    public int getFaults() { return faults; }
    public int getDiskReads() { return diskReads; }
    public int getDiskWrites() { return diskWrites; }
}
