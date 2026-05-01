// Brandon Zettek z2015083
// Page.java
// mod 6 added the new fields needed for virtual mem
// each entry in the virtual page table is one of these
// tracks if the page is in ram and if it was written to
// also tracks LRU

public class Page
{
    //mod 6 these got added for virtual memory
    private boolean isValid;    //currently in phys mem?
    private boolean isDirty;    //written to after load
    private boolean pinned;
    private int frameNum;
    private int processId;      //process owns this page
    private int lastUsed;       //last time it used for LRU

    public Page()
    {
        //defaults
        this.isValid = false;
        this.isDirty = false;
        this.pinned = false;
        this.frameNum = -1;  //-1 means not loaded
        this.processId = -1;  //-1 means free
        this.lastUsed = 0;
    }


    //getters and setters mod 6
    public boolean isValid()
    {
        return isValid;
    }

    public void setValid(boolean v)
    {
        this.isValid = v;
    }

    public boolean isDirty()
    {
        return isDirty;
    }

    public void setDirty(boolean d)
    {
        this.isDirty = d;
    }

    public boolean isPinned()
    {
        return pinned;
    }

    public void setPinned(boolean p)
    {
        this.pinned = p;
    }

    public int getPhysFrame()
    {
        return frameNum;
    }

    public void setPhysFrame(int f)
    {
        this.frameNum = f;
    }

    public int getProcessId()
    {
        return processId;
    }

    public void setProcessId(int pid)
    {
        this.processId = pid;
        
    }

    public long getLastUsed()
    {
        return lastUsed;
    }

    public void setLastUsed(long t)
    {
        this.lastUsed = (int) t; //cast since we store as int
    }
}
