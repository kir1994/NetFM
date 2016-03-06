package app;

import java.io.IOException;
import java.nio.file.*;
import java.nio.file.attribute.BasicFileAttributes;
import net.Out;
import net.Handler;
import net.Message;

public class CmdHandler implements Handler{
    private Message m;
    private Path curDir;
    private static final String defaultDir = "D:\\";
    private static final String[] denyList = {
            //"C:",
            //"D:\\Programms"
            };
 /*   private static final String[] accessList = {
            "D:\\Temp"
            };*/
   
    @Override
    public String init(/*String user*/){     
        m = null;
        
        curDir = Paths.get(defaultDir);
        return execute(Commands.DIR, null);
    }    
    
    @Override
    public Message getMessage(){
        return m;
    }
    
    @Override
    public String execute(Object a) {
        Packet p = (Packet)a;
        m = p;
        return execute(p.getCmd(), p.getParam());     
    }
    public String execute(Commands cmd, String param){
        if(cmd == Commands.DIR){
            DirectoryStream<Path> stream = null;
            try{                
                stream = Files.newDirectoryStream(curDir);                
                String res = "Current: " + curDir.toAbsolutePath() + "\n\n";
                for(Path p : stream)
                    res += (p.toFile().isDirectory() ? "d: " : "f: ") + p.getFileName().toString() + "\n";                
                stream.close();
                return Out.msg(res);
            }catch(IOException e) {   
                try{
                    if(stream != null) 
                        stream.close();
                } catch(IOException ie) {}
                Out.print("Wrong dir...");
                return null;
            }
        }else if(cmd == Commands.CD){
            if(param.equals("..")){
                curDir = (curDir.getParent() != null) ? curDir.getParent() : curDir;
                return execute(Commands.DIR, null);
            } else if(param.matches("(.*):")) {
                return execute(Commands.CD, param + "/");
            }
            else {
                try{                     
                    Files.newDirectoryStream(curDir.resolve(param)).close();
                    curDir = curDir.resolve(param); 
                }
                catch(InvalidPathException | IOException e) {
                    return Out.msg("Error: No such dir");                  
                }
                return execute(Commands.DIR, null);
            }
        } else if(cmd == Commands.MKDIR) {
            for(String i: denyList){
                if(curDir.toAbsolutePath().toString().contains(i) || param.contains(i)){
                    return Out.msg("Error: Access Denied");
                }
            }
            try{
                if(Files.exists(curDir.resolve(param)) && Files.isDirectory(curDir.resolve(param))){
                    return Out.msg("Error: Already exists");
                }
                Files.createDirectories(curDir.resolve(param));
                return Out.msg("Successfully created");
            } catch(FileAlreadyExistsException e) {
                return Out.msg("Error: There is a file with such name. Try another one");
            } catch(AccessDeniedException e) {
                return Out.msg("Error: Access denied");
            } catch (IOException ex) {
                return Out.msg("Error: Something went wrong during dir create...Pray more and try again");
            }
        } else if(cmd == Commands.DEL) {
            for(String i: denyList){
                if(curDir.toAbsolutePath().toString().contains(i) || param.contains(i)){
                    return Out.msg("Error: Access denied");
                }
            }
            try{
                if(!Files.exists(curDir.resolve(param))){
                    return Out.msg("Error: No such file or directory");
                }                
                removeRecursive(curDir.resolve(param));
                return Out.msg("Successfully deleted");
            } catch(AccessDeniedException e) {
                return Out.msg("Error: Access denied");
            } catch (IOException ex) {
                return Out.msg("Error: Cannot delete directory because of devillish power...");
            }
        } else if(cmd == Commands.NOP) {
            return "";
        }
        return Out.msg("Feature under construction...");
        //return null;
    }
    
    
    private void removeRecursive(Path path) throws IOException
    {
        Files.walkFileTree(path, new SimpleFileVisitor<Path>()
        {
            @Override
            public FileVisitResult visitFile(Path file, BasicFileAttributes attrs)
                    throws IOException
            {
                Files.delete(file);
                return FileVisitResult.CONTINUE;
            }
            @Override
            public FileVisitResult postVisitDirectory(Path dir, IOException exc) throws IOException
            {
                if (exc == null)
                {
                    Files.delete(dir);
                    return FileVisitResult.CONTINUE;
                }
                throw exc;
            }
        });
    }
}
