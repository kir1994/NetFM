package concurrent;

import java.util.HashMap;
import net.CallbackHandler;
import net.Channel;

public class ThreadPool {
    private final int max;
    private final int timeout;
    private static final Object lock = new Object();
    private final Channel<WorkerThread> free = new Channel();
    private volatile CallbackHandler cH;
    
    private final HashMap<String, WorkerThread> work = new HashMap<>();
    public ThreadPool(int startSize, int maxSize, int time){
        if(startSize > maxSize)
            throw new IllegalArgumentException();
        max = maxSize;
        timeout = time * 1000;
        for(int i = 0; i < startSize; ++i){
            free.put(new WorkerThread(this, timeout));
        }
    }
    public void setCHandler(CallbackHandler c){
        cH = c;
    }
    
    public void addTask(Stoppable smth){   
        if(smth == null){
            throw new IllegalArgumentException();
        }
        synchronized(lock){
            if((free.list.size() + work.size()) < max && free.list.size() == 0)
                free.put(new WorkerThread(this, timeout));
        } 
        WorkerThread wt = free.get();
        synchronized(lock){
            work.put(smth.toString(), wt);
        }
        wt.setTask(smth);        
    }  
   public void stop(){
        synchronized(lock){
             while(free.list.size() > 0)
                 free.get().stop();     
             for(WorkerThread i : work.values()) {
                 i.stop();
             }
        }
   }   
   public boolean stopByName(String s) {
       if(s == null)
           throw new IllegalArgumentException();
       WorkerThread wt = work.get(s);
       if(wt == null)
           return false;
       else{
           wt.stopWork();
           return true;
       }
   }
   public int size()
   {
       synchronized(lock){
            return free.list.size() + work.size();
       }
   }
   public int max()
   {
       return max;
   }
   
   public void hEnd(WorkerThread a, String session, String state){    
        synchronized(lock){
            if(!state.equalsIgnoreCase("Timeout")){    
                if(cH != null)
                    cH.handle("Disconnected", session);
                free.put(a);                
            }
            else{
                free.list.remove(a);
                a.stop();
            }            
            work.remove(session);
        }
    }
    public void hStart(WorkerThread a, String session){       
        if(cH != null)
            cH.handle("Connected", session);
    }
}