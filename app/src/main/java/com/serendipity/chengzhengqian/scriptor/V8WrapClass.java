package com.serendipity.chengzhengqian.scriptor;

import java.sql.Wrapper;

public class V8WrapClass {
    public static int a;
    public int i;
    public V8WrapClass(){
        MainActivityState.sendMain(0,"print('V8WrapClass()');");
    }
    public V8WrapClass(Integer i){
        this.i=i;
        MainActivityState.sendMain(0,String.format("print('V8WrapClass(%s)');",i.toString()));
    }
    public V8WrapClass(V8WrapClass t){
        this.i=t.i;
    }
    public V8WrapClass(double a){
        MainActivityState.sendMain(0,String.format("print('V8WrapClass(%f)');",a));
        i= (int) (a);
        V8WrapClass.a= (int) a;
    }

    public void add(V8WrapClass w){
        MainActivityState.sendMain(0,String.format("print('add(w:%s)');",String.valueOf(w.i)));
        this.i+=w.i;
    }
    public void add(int a){
        MainActivityState.sendMain(0,String.format("print('add(int:%s)');",String.valueOf(a)));
        this.i=a;
    }
    public int get(){
        return i;
    }
    public float getFloat(){return (float) (i+0.01);}
    public V8WrapClass copyNew(int a){
        return new V8WrapClass(a+i);
    }
    public char getChar(){return 'c';}
    public void run(V8WrapInterface t){
        t.Run(this);
    }
    public static void staticTest(int a){
        MainActivityState.sendMain(0,String.format("print('staticTest(int:%s)');",String.valueOf(a)));
//        Class<?>c= V8WrapClass.class;
//        JavaV8Interface.addLog(c.getName()+"\n");
//        JavaV8Interface.addLog(c.getClass().getName()+"\n");
//        Class<?>d=c.getClass();
//        JavaV8Interface.addLog(d.getClass().getName()+"\n");

    }
}
