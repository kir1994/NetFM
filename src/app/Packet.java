package app;

import java.io.Serializable;
import net.Message;

public class Packet implements Message, Serializable {
    
    private Commands cmd;
    private String param;
    
    public Packet(Commands type, String str) {
        set(type, str);
    }
    
    public Packet() {
        set(Commands.NOP, "");
    }
    
    private void set(Commands cmd, String param) {
        if (cmd == null || param == null) {
            throw new IllegalArgumentException("Null arg");
        }
        this.cmd = cmd;
        if(cmd != Commands.DIR && cmd != Commands.NOP)
            this.param = param;
        else
            this.param = "";
    }
    
    public Commands getCmd() throws NullPointerException {
        if (cmd == null) {
            throw new NullPointerException("Null cmd");
        }
        
        return cmd;
    }
    
    public String getParam() throws NullPointerException {
        if (param == null) {
            throw new NullPointerException("null param");
        }
        
        return param;
    }
    
    @Override
    public String toString() {
        return cmd + " " + param;
    } 
}
