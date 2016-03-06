package concurrent;

import net.CallbackHandler;
import net.Channel;
import net.Session;

public class Dispatcher implements Stoppable{    
    private final ThreadPool tp;
    private final Channel<Session> ch;
    private Thread t;
    private volatile boolean isRunning;
    
    public Dispatcher(Channel ch, int startSize, int maxSize, int timeout, CallbackHandler c) {
        this.ch = ch;
        tp = new ThreadPool(startSize, maxSize, timeout);  
        tp.setCHandler(c);
        isRunning = false;
    }   
    
    public void initStart() {
        this.t = new Thread(this, "Disp");
        this.t.start();
    }
    
    @Override
    public void run(){ 
        isRunning = true;
        try{
            while(isRunning){             
                tp.addTask(ch.get());
            }
        }catch(IllegalArgumentException e) {}
        finally{
            stop();
        }
    }
    @Override    
    public void stop(){
        if(isRunning) {
            isRunning = false;
            if(t != null)
                t.interrupt();
            tp.stop();
        }
    }
    
    public boolean stopByName(String s) {
        return tp.stopByName(s);
    }
}
