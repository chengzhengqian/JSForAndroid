package com.serendipity.chengzhengqian.scriptor;

public class V8Math implements V8Wrapper {
    @Override
    public void pushJavaObject() {
         MainActivityState.setObject(this);
    }
    public V8Math(){

    }

    public double add(double x, double y){
        return x+y;
    }
}
