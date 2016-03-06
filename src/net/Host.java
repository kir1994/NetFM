package net;

import concurrent.Stoppable;
import java.io.IOException;
import java.net.ServerSocket;
import java.net.Socket;
import java.net.SocketException;

public class Host implements Stoppable{
    public final ServerSocket serverSocket;
    private final Channel<Session> ch;
    private Thread t;
    private final Class<? extends Handler> h;
    private volatile boolean isRunning;
    
    public Host(int port, Channel ch, Class<? extends Handler> h) throws IOException {
        serverSocket = new ServerSocket(port);
        this.ch = ch;  
        isRunning = false;
        this.h = h;
    }
    public void initStart() {
        this.t = new Thread(this, "Host");
        this.t.start();
    }   
    
    @Override
    public void run() {
        this.isRunning = true;
        Socket socket;
        try {
            while(isRunning)
            {              
                socket = serverSocket.accept();  
                Session session = new Session(socket, h);                
                ch.put(session);            
            }
        } 
        catch (SocketException  e) {
        }
        catch (IOException e) {
            Out.print("Error: Sudden host exception");
        } 
        finally {
            stop();
        }
    }
    @Override
    public void stop() {
        if(isRunning == true){
            this.isRunning = false;
            if(t != null)
                t.interrupt();
            try{
                serverSocket.close();
            }catch (IOException ex) {}            
        }
    }
}
