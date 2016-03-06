package net;

public class Out {
    public static synchronized void print(String s) {
        System.out.println(s);
    }
    public static String msg(String s) {
        return "------------------------------\n" +
               s + "\n" + 
               "------------------------------\n\n";
    }
}
