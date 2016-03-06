package net;

import concurrent.Stoppable;
import java.io.DataOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.ObjectInputStream;
import java.io.OptionalDataException;
import java.io.OutputStream;
import java.net.Socket;
import java.net.SocketException;

public class Session implements Stoppable{
    private final Socket socket;
    private Handler h;
    private OutputStream dos;
    private InputStream dis;
    private boolean isRunning;
    
    public Session(Socket socket, Class<? extends Handler> h)
    {
        try{
            this.h = h.newInstance();
        }catch(InstantiationException | IllegalAccessException e) {
            Out.print("Error: Wrong handler...");
            h = null;
        }
        this.socket = socket;
        dis = null;
        dos = null;        
        isRunning = false;
    }
    
    @Override
    public void run()
    {
        if(h == null)
            return;
        isRunning = true;
        try
        {
            dos = new DataOutputStream(socket.getOutputStream());            
            dis = new ObjectInputStream(socket.getInputStream());              
            String s = h.init();
            try{
                if(s != null)
                    ((DataOutputStream)dos).writeUTF(s);
                else
                    ((DataOutputStream)dos).writeUTF("");
            }catch(SocketException se) {
                stop();
                return;
            }
            Out.print(this.toString() + ": Connected");
            try{
                while(isRunning){
                    Object a;
                    try{
                        a = ((ObjectInputStream)dis).readObject();
                    } catch(OptionalDataException e){ 
                        continue;
                    }
                    if((s = h.execute(a)) != null) {
                        Out.print(this.toString() + ": " + h.getMessage().toString());
                        ((DataOutputStream)dos).writeUTF(s);
                    } else {
                        Out.print(this.toString() + ": Failed");
                        ((DataOutputStream)dos).writeUTF("Error: Wrong command");
                    }                                      
                }
            }catch(ClassNotFoundException | IOException io){
                
            }
        } 
        catch (IOException e){            
            Out.print("Error: Sudden Socket error");            
        } finally {
            stop();
        }
    }
    
    @Override
    public void stop() {
        if(isRunning){
            isRunning = false;
            try{
                
                if(dis != null){
                    dis.close();
                }
                if(dos != null){
                    try{                    
                        ((DataOutputStream)dos).writeUTF(" e x i t  t i m e ");
                    } catch(IOException e) {}
                    dos.close();
                }                
                socket.close();
                Out.print(this.toString() + ": Disconnected");
            }
            catch(IOException e) {}
        }
    }
}
