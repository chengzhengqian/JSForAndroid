package com.serendipity.chengzhengqian.scriptor;

import android.content.Context;
import android.util.AttributeSet;
import android.widget.LinearLayout;

public class V8LayoutParams extends LinearLayout.LayoutParams implements V8Wrapper{

    public V8LayoutParams() {
        super(0, 0, 0);
    }
    public void setLayoutParams(String w, String h, String weight){
        setWidth(w);
        setHeight(h);
        setWeight(weight);
    }
    public void setWidth(String w){
        if(w.startsWith("m")){
            this.width= LinearLayout.LayoutParams.MATCH_PARENT;
        }
        else if(w.startsWith("w")){
            this.width=LinearLayout.LayoutParams.WRAP_CONTENT;
        }
        else {
            this.width=Integer.valueOf(w);
        }
    }
    public void setHeight(String h){
        if(h.startsWith("m")){
            this.height= LinearLayout.LayoutParams.MATCH_PARENT;
        }
        else if(h.startsWith("w")){
            this.height=LinearLayout.LayoutParams.WRAP_CONTENT;
        }
        else {
            this.height=Integer.valueOf(h);
        }
    }
    public void setWeight(String w){
        this.weight=Float.valueOf(w);
    }

    @Override
    public void pushJavaObject() {
        MainActivityState.setObject(this);
    }
}
