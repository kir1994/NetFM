package client;

import app.Commands;
import app.Packet;
import concurrent.Stoppable;
import java.io.DataInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.ObjectOutputStream;
import java.io.OutputStream;
import java.net.InetSocketAddress;
import java.net.Socket;
import java.net.SocketException;
import net.CallbackHandler;

public class Client implements Stoppable {
    private Socket socket;
    private OutputStream dos;
    private InputStream dis;
    private final CallbackHandler cH;
    private Thread tState;
    private volatile Object obj;
    private final int connTimeout = 10; // секунды
    private boolean isRunning;
    
    public Client(CallbackHandler c){
        cH = c;
        dos = null;
        dis = null;
        tState = null;
        obj = null;
        isRunning = false;
    }
    public boolean start(String host, int port) throws IllegalStateException{
        try {
            InetSocketAddress ia = new InetSocketAddress(host, port);
            if(ia.isUnresolved())
                throw new SocketException();
            socket = new Socket(host, port);
            dos = new ObjectOutputStream(socket.getOutputStream());
            dis = new DataInputStream(socket.getInputStream());       
            tState = new Thread(this, "Cl");
            tState.start();
        } catch(SocketException se){            
            throw new IllegalStateException();
        }catch (IOException e) {
            stop();   
            return false;
        }
        long time = System.currentTimeMillis();
        while(obj == null)
            if(System.currentTimeMillis() - time > connTimeout * 1000){
                System.out.println("Server is full. Try again later");
                stop();
                return false;
            }  
        obj = null;
        return true;                               
    }
    
    @Override
    public void run(){
        isRunning = true;
        try{ 
            String str = ((DataInputStream)dis).readUTF();
            obj = new Object();
            if(cH != null)
                cH.handle("Connected", this.toString());
            while(!str.equalsIgnoreCase(" e x i t  t i m e ") && isRunning){
                System.out.println(str);
                str = ((DataInputStream)dis).readUTF();
            }                        
        }catch (IOException e) {}
        finally {
            stop();
        }
    }
    
    
    public void sendCmd(String s){
        try{
            Packet p = new Packet(Commands.valueOf(s.split(" ")[0].toUpperCase()), s.replaceFirst("[a-zA-Z]* ", ""));
            try{
                ((ObjectOutputStream)dos).writeObject(p);
                dos.flush();
            } catch(IOException e) {
                if(cH != null)
                    cH.handle("Disconnected", this.toString());
            }
        }catch(IllegalArgumentException e) {
            System.out.println("Wrong command");
        }
    }
    
    @Override
    public void stop(){
        if(isRunning){
            isRunning = false;
            try{
                if(dos != null)
                    dos.close();
                if(dis != null)
                    dis.close(); 
                if(socket != null)
                    socket.close();
            } catch(IOException e) {}
            if(cH != null)
                cH.handle("Disconnected", this.toString());
        }
    }
}

