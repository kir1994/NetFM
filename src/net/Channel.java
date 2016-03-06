package net;

import java.util.LinkedList;

public class Channel<T> {
    public static int nMax = 5;
    public LinkedList<T> list = new LinkedList();
    private final Object lock = new Object();
           
    public void put(T obj){
        if (obj == null) {
            throw new IllegalArgumentException();
        }
        try{
            synchronized(lock){
                while (list.size() >= nMax) {
                        lock.wait();
                }
                list.addFirst(obj);
                lock.notify();
            }
        }catch(InterruptedException e) {}
    }
    public T get(){
        try{
            synchronized(lock){
                 while (list.size() <= 0)  {
                         lock.wait();
                 }
                 lock.notify();

                return list.removeLast();
            }
        }catch(InterruptedException e) { 
            return null; 
        }
    }
}

