package com.serendipity.chengzhengqian.scriptor;

import com.eclipsesource.v8.*;

import java.lang.reflect.InvocationHandler;
import java.lang.reflect.Method;
import java.util.HashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class ProxyListener implements InvocationHandler {

    private Map<String,String>funcs;
    public V8 v8;
    ProxyListener(V8Object v8obj, V8 v8){
        funcs = new HashMap<>();
        String[] keys=v8obj.getKeys();
        for(String key: keys){
            Object b=v8obj.get(key);
            if(b instanceof String){
                funcs.put(key, (String) b);
            }
            JavaV8Interface.release(b);
        }
        this.v8=v8;


    }
    public static void pushObject(Object obj,V8Array para){
        if(obj instanceof Integer){
            para.push((Integer) obj);
        }
        else if(obj instanceof Boolean){
            para.push((Boolean)obj);
        }
        else if(obj instanceof Double){
            para.push((Double)obj);
        }
        else if(obj instanceof String){
            para.push((String)obj);
        }
        else {
            para.push((V8Value)obj);
        }
    }
    static class Result{
        public final boolean isRunOtherThread;
        public final String threadName;
        public final String code;
        public final boolean isForceNew;

        Result(boolean isMatched, String threadName, String code, boolean isForceNew){
            this.isRunOtherThread =isMatched;
            this.threadName=threadName;
            this.code=code;
            this.isForceNew=isForceNew;
        }
    }
    public static Result getResult(String funcCode){
        Pattern p=Pattern.compile("@@(.*)@@");
        Matcher m=p.matcher(funcCode);
        if(m.find()){

                String matchString=m.group(1);
                if(matchString.startsWith("new")) {
                    String threadName=matchString.substring(3,matchString.length());
                    return new Result(true, threadName,
                            funcCode.substring(m.end(), funcCode.length()), true);
                }
                else {
                    return new Result(true, matchString,
                            funcCode.substring(m.end(), funcCode.length()), false);
                }
        }
        return new Result(false,null,funcCode,false);
    }
    public static Map<String,V8> otherRuntimes=new HashMap<>();
    public static void releaseOtherRuntimes(){
        for(V8 r:otherRuntimes.values()){
            r.getLocker().acquire();
            r.release();
        }
        otherRuntimes.clear();
    }
    @Override
    public Object invoke(Object o, Method method, Object[] objects) {
        //JavaV8Interface.addLog("this is called from proxy:"+method.getName()+"\n");

        String methodName = method.getName();
        String funcCode = funcs.get(methodName);
        Object result=null;
        try {

            if(funcCode==null){
                JavaV8Interface.addLog(methodName+" is not defined!");
            }
            else {
                Result r=getResult(funcCode);
                if(!r.isRunOtherThread) {
                    Object v8func = v8.executeObjectScript(funcCode);
                    if (v8func instanceof V8Function) {
                        V8Array para = new V8Array(v8);
                        List<Object> para_list = new LinkedList<>();
                        for (Object obj : objects) {
                            Object value = JavaV8Interface.convertToJavaScriptObject(obj, v8);
                            pushObject(value,para);
                            para_list.add(value);
                        }
                        result = ((V8Function) v8func).call(null, para);
                        JavaV8Interface.release(para);
                        JavaV8Interface.releaseParams(para_list);
                    } else {
                        JavaV8Interface.addLog(funcCode + " should return a function");
                    }
                    JavaV8Interface.release(v8func);
                }
                else {
                    funcCode=r.code;
                    String threadName=r.threadName;
                    V8 localv8=null;
                    if(r.isForceNew || otherRuntimes.get(threadName)==null) {
                        if(r.isForceNew) {
                            /*  @@newxxx@@ in the function, force to restart the corresponding executor.
                            * */
                            V8 originalR = otherRuntimes.remove(threadName);
                            if (originalR != null) {
                                originalR.getLocker().acquire();
                                originalR.release();
                                MainActivityState.sendMain(4,"release original runtime : "+r.threadName+"\n");
                            }

                        }
                        otherRuntimes.put(threadName, JavaV8Interface.createRuntime(threadName));
                        MainActivityState.sendMain(4,"start new runtime : "+r.threadName+"\n");
                        localv8=otherRuntimes.get(threadName);

                    }
                    else {
                        localv8=otherRuntimes.get(threadName);
                        localv8.getLocker().acquire();
                    }


                    try {

                        Object v8func=localv8.executeObjectScript(funcCode);
                        if (v8func instanceof V8Function) {
                            V8Array para = new V8Array(localv8);
                            List<Object> para_list = new LinkedList<>();
                            for (Object obj : objects) {
                                Object value = JavaV8Interface.convertToJavaScriptObject(obj,localv8);
                                pushObject(value,para);
                                para_list.add(value);
                            }
                            result = ((V8Function) v8func).call(null, para);
                            JavaV8Interface.release(para);
                            JavaV8Interface.releaseParams(para_list);
                        } else {
                            JavaV8Interface.addLog(funcCode + " should return a function");
                        }
                        JavaV8Interface.release(v8func);
                        JavaV8Interface.release(result);
                        MainActivityState.sendMain(4,method.getName()+" is called\n");
                        //MainActivityState.sendMain(4, funcCode+"\n");
                    }
                    catch (Exception e){
                        MainActivityState.sendMain(4,e.toString()+"\n");
                    }
                    finally {
                        localv8.getLocker().release();
                        return null;
                    }

                }

            }
        }
        catch (Exception e){
            MainActivityState.sendMain(4,e.toString());
        }
        return result;
    }
}


