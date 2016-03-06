package app;

public enum Commands {    
    NOP,
    CD,
    DIR,
    DEL,
    MKDIR,
    GET;    
    
    public static Commands get(int index) throws ArrayIndexOutOfBoundsException {
        return Commands.values()[index];
    }
    public int index() {
        return this.ordinal();
    } 
}
