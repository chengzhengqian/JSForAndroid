package com.serendipity.chengzhengqian.scriptor;

import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.LinearLayout;
import com.eclipsesource.v8.V8Array;
import com.eclipsesource.v8.V8Function;
import com.eclipsesource.v8.V8Object;

public class V8Button extends Button implements V8Wrapper{
    public V8Button() {
        super(MainActivityState.currentActivity);
    }
    public void setText(String s){
        super.setText(s);
    }

    @Override
    public  void pushJavaObject() {
        MainActivityState.setObject(this);
    }

    public void onClick(final String funcName){

        this.setOnClickListener(new OnClickListener() {
            @Override
            public void onClick(View view) {
                V8Object obj=AndroidV8.castFrom(view,V8Button.class);
                V8Array array=new V8Array(MainActivityState.v8);
                array.push(obj);
                V8Function func = (V8Function) MainActivityState.v8.getObject(funcName);
                func.call(null,array);
                func.release();
                AndroidV8.release(obj,array);

            }
        });
        //func.release();
    }
    public void setLayoutParams(){
        super.setLayoutParams((ViewGroup.LayoutParams) MainActivityState.getObject());
    }
}
