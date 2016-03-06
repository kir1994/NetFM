package concurrent;

public class WorkerThread implements Stoppable{
    private static final Object lock = new Object();    
    private volatile Stoppable curTask;
    private final Thread t;
    private final int timeout;
    private volatile boolean isRunning;
    private final ThreadPool tP;
    
    public WorkerThread(ThreadPool tp, int tout){
            tP = tp;
            timeout = tout;
            isRunning = false;
            t = new Thread(this); 
            t.start();   
            
    }
    @Override
    public void run() {
        isRunning = true;
        long time = System.currentTimeMillis();
        while(isRunning){   
            String session = null;
            if((System.currentTimeMillis() - time) > timeout){
                isRunning = false;
                tP.hEnd(this, session, "Timeout");
            }else{
                synchronized(lock){
                    if(curTask == null) 
                        continue;  
                }                
                try{
                    session = curTask.toString();
                    tP.hStart(this, session);
                    curTask.run();
                    curTask = null;        
                }finally{
                    tP.hEnd(this, session, "Finished");
                }
                time = System.currentTimeMillis();  
            }
        }          
     } 
    @Override
    public void stop(){
        if(isRunning){
            isRunning = false;
            if(curTask != null)
                curTask.stop(); 
            if(t != null)
                t.interrupt();  
        }
    }
    public void stopWork(){
        if(curTask != null)
            curTask.stop();   
    }
    public void setTask(Stoppable r){
        synchronized(lock){   
                if(curTask != null)
                    throw new IllegalStateException();
                curTask = r;              
            }  
    }
}
