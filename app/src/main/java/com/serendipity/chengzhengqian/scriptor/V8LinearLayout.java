package com.serendipity.chengzhengqian.scriptor;

import android.view.View;
import android.view.ViewGroup;
import android.widget.LinearLayout;

public class V8LinearLayout extends LinearLayout implements V8Wrapper {
    public V8LinearLayout() {
        super(MainActivityState.currentActivity);
    }

    @Override
    public void pushJavaObject() {
        MainActivityState.setObject(this);
    }

    public void setOrientation(String orientation){
        if(orientation.startsWith("h"))
            this.setOrientation(LinearLayout.HORIZONTAL);
        else if(orientation.startsWith("v")){
            this.setOrientation(LinearLayout.VERTICAL);
        }
    }
    public void addView(){
        this.addView((View) MainActivityState.getObject());
    }
    public void setLayoutParams(){
        super.setLayoutParams((ViewGroup.LayoutParams) MainActivityState.getObject());
    }
}
