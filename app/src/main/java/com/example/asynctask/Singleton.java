package com.example.asynctask;

public class Singleton {

    private static Singleton INSTANCE = null;

    //can be anything, cast as needed (be careful!)
    private Object mt=null;

    //track what the state of the start button is
    private boolean startEnabled =true;

    private Singleton() {};

    //creates a static ref to a new or existing singleton object
    //will not be garbage collected until the classloader is gc'ed
    public static Singleton getInstance() {
        if (INSTANCE == null) {
            INSTANCE = new Singleton();
        }
        return(INSTANCE);
    }

    //track the thread, slightly better way using getters/setters
    public Object get_thread(){return mt;}
    public void set_thread(Object mt){this.mt=mt;}

    //track the state of the buttons
    public boolean get_startState(){return startEnabled;}
    public void set_startState(boolean startState){this.startEnabled =startState;}


}