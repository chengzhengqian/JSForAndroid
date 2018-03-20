package com.serendipity.chengzhengqian.scriptor;

import com.eclipsesource.v8.*;

import java.lang.reflect.InvocationHandler;
import java.lang.reflect.Method;
import java.lang.reflect.Proxy;
import java.util.HashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;

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
    @Override
    public Object invoke(Object o, Method method, Object[] objects) throws Throwable {
        //JavaV8Interface.addLog("this is called from proxy:"+method.getName()+"\n");

        String methodName = method.getName();
        String funcCode = funcs.get(methodName);
        Object result=null;
        try {
            if(funcCode==null){
                JavaV8Interface.addLog(methodName+" is not defined!");
            }
            else {
                Object v8func = v8.executeObjectScript(funcCode);
                if (v8func instanceof V8Function) {
                    V8Array para = new V8Array(v8);
                    List<Object> para_list = new LinkedList<>();
                    for (Object obj : objects) {
                        Object value = JavaV8Interface.convertToJavaScriptObject(obj, v8);
                        para.push((V8Value) value);
                        para_list.add(value);
                    }
                    result=((V8Function) v8func).call(null, para);
                    JavaV8Interface.release(para);
                    JavaV8Interface.releaseParams(para_list);
                } else {
                    JavaV8Interface.addLog(funcCode + " should return a function");
                }
                JavaV8Interface.release(v8func);
            }
        }
        catch (Exception e){
            JavaV8Interface.addLog(e.toString());
        }
        return result;
    }
}


