package server;

import app.CmdHandler;
import concurrent.Dispatcher;
import java.io.IOException;
import net.CallbackHandler;
import net.Channel;
import net.Host;
import net.Out;
import net.Session;

public class Server {
    private Host h;
    private Dispatcher d;
    private boolean isRunning;
    private final CallbackHandler cH;
    
    public Server(CallbackHandler c) {    
        cH = c;
        isRunning = false;
    }
    public boolean start(int port){  
        if(!isRunning){
            try {
                Channel<Session> ch = new Channel();
                h = new Host(port, ch, CmdHandler.class);
                d = new Dispatcher(ch, 1, 2, 120, cH);
                h.initStart();
                d.initStart();
                isRunning = true;
                Out.print("Server: started");
                return true;
            } catch (IOException e) {
                this.stop();
                Out.print("Error: Something crashed...");
                return false;
            }
        } else throw new IllegalStateException("Error: Already starteds");
    }
    
    public void stop()
    {     
        if(isRunning){
            isRunning = false;
            if(d != null)
                d.stop();
            if(h != null)
                h.stop();
            Out.print("Server: stopped");
        }
    }
    
    public void stopSession(String name){
        if(!d.stopByName(name))
            Out.print("Error: Wrong session name");
    }    
}
