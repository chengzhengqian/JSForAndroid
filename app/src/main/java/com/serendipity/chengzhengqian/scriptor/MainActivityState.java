package com.serendipity.chengzhengqian.scriptor;

import android.os.Bundle;
import android.os.ResultReceiver;
import com.eclipsesource.v8.V8;
import java.util.Calendar;
import java.util.HashMap;
import java.util.Map;
import java.util.Stack;

public class MainActivityState {
    public static boolean isServerRunning=false;
    public static ResultReceiver mainReceiver;
    public static boolean isMainRunning;
    public static V8 v8;
    // a place to hold v8JavaObjectReturn, assuming single thread
    //public static Object v8JavaObjectReturn;
    public static int CurrentIndex=0;
    public static int MaximumIndex=10000;
    public static int getCurrentId(){
        //for multit-head only
        return 0;
    }
    public static void setObject(Object obj){
        javaObjects.push(obj);

    }
    public static Object getObject(){

        return javaObjects.pop();

    }
    public static Stack<Object> javaObjects=new Stack<>();
    public static void sendMain(int code, String content){
        if(isMainRunning)
        {
            Bundle bundle = new Bundle();
            bundle.putString(String.valueOf(code), content);
            mainReceiver.send(code, bundle);}
    }
    public static String serverIndexHtml="";
    public static Boolean isFileInited=false;
    public static MainActivity currentActivity;
}
