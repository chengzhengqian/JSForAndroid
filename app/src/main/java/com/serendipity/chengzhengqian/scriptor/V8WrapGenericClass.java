package com.serendipity.chengzhengqian.scriptor;

public class V8WrapGenericClass<T> {
    T a;
    V8WrapGenericClass(T a){
        this.a=a;
        JavaV8Interface.addLog("called from specific type "+a.getClass().getName()+" "+a.toString()+"\n");
    }
    T get(){
        return a;
    }

}
