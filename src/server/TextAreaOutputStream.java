package server;

import java.io.IOException;
import java.io.OutputStream;
import javax.swing.JTextArea;

public class TextAreaOutputStream extends OutputStream {
    private JTextArea ta;
  
    public TextAreaOutputStream( JTextArea ta ) {
        this.ta = ta;
    }

    @Override
    public void write(int b) {
        ta.append(Byte.toString((byte)b));
    }
    
    @Override
    public void write(byte b[]) {
        write(b, 0, b.length);
    }
    @Override
    public void write( byte b[], int off, int len ) {
        ta.append(new String(b, off, len));
    }
    
    @Override
    public void close() throws IOException {
        super.close();
        ta = null;
    }        
}
