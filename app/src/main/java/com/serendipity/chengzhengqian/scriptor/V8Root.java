package com.serendipity.chengzhengqian.scriptor;

import android.view.View;
import android.widget.LinearLayout;
import com.eclipsesource.v8.V8Array;
import com.eclipsesource.v8.V8Object;

public class V8Root implements V8Wrapper {
    public LinearLayout rootLayout;

    @Override
    public void pushJavaObject() {

        MainActivityState.setObject(this);
    }
    public void removeAllViews(){rootLayout.removeAllViews();}
    public V8Root(){
        rootLayout=MainActivityState.currentActivity.ui_ll;
    }
    public void addView(){
        rootLayout.addView((View) MainActivityState.getObject());
    }
    public void init(){
        this.removeAllViews();
    }



}
