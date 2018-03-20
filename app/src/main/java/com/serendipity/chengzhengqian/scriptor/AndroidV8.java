package com.serendipity.chengzhengqian.scriptor;

import com.eclipsesource.v8.*;

import java.lang.reflect.Constructor;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.util.LinkedList;
import java.util.List;

public class AndroidV8 {
    public static void minimalV8init(V8 v8){
        v8.registerJavaMethod(print(),"print");
        v8.registerJavaMethod(inspect(),"inspect");
//
    }
    public static void  initV8(V8 v8){
        v8.registerJavaMethod(print(),"print");
        v8.registerJavaMethod(inspect(),"inspect");
//        wrapClass(v8,V8Button.class,"Button");
//        wrapClass(v8,V8Root.class,"Root");

        v8.registerJavaMethod(generateFrom(V8Button.class),"button");
        v8.registerJavaMethod(generateFrom(V8Root.class),"root");
        v8.registerJavaMethod(generateFrom(V8LinearLayout.class),"linearLayout");
        v8.registerJavaMethod(generateFrom(V8LayoutParams.class),"layoutParams");
        v8.registerJavaMethod(generateFrom(V8Math.class),"mvath");
    }
    private static void addLog(Object s){
        MainActivityState.currentActivity.log.append(s.toString());
    }

//    private static void wrapClass(V8 v8, Class<?> c, String name){
//        v8.registerJavaMethod(generateFrom(c),"__"+name);
//        v8.executeVoidScript(
//                "function "+name+"(){\n" +
//                        "tmp={};__"+name+".call(tmp);return tmp;\n" +
//
//                        "}"
//        );
//    }
    private static JavaVoidCallback generateFrom(final Method m, final Object o){
        return new JavaVoidCallback() {
            @Override
            public void invoke(V8Object v8Object, V8Array v8Array) {
                List<Object> para=new LinkedList<>();
                for(int i=0;i<v8Array.length();i++){
                    Object v=v8Array.get(i);
                    para.add(v);
                }

                try {
                    m.invoke(o,para.toArray());
                    release(v8Object,v8Array);

                } catch (Exception e)
                {
                    addLog(e.toString());
                }


            }
        };
    }



//    public static List<V8Object> generateFromObjects=new LinkedList<>();
//    public static void releaseGeneratedObjects(){
//        for(V8Object b:generateFromObjects){
//            b.release();
//        }
//        addLog("clear:"+generateFromObjects.size());
//        generateFromObjects.clear();
//    }
    //this seems that this way to generating code will lead memory leak
    private static JavaVoidCallback generateFrom(final Class<?> c){

        return new JavaVoidCallback(){
            @Override
            public void invoke(V8Object v8Object, V8Array v8Array) {

                Constructor<?> ctor = null;
                Object object;

                try {

                    ctor = c.getConstructor();
                    object = ctor.newInstance();

                    //StringBuilder s=new StringBuilder();
                    for(Method m:c.getDeclaredMethods()){
                        if(m.getName().equals("access$super")) {
                        }
                        //this method need to to consider specially.
                        else if(m.getName().equals("pushJavaObject")){
                                v8Object.registerJavaMethod(object,m.getName(),m.getName(),new Class<?>[]{});
                        }

                        else {
                            v8Object.registerJavaMethod(generateFrom(m, object), m.getName());
                        }
                    }

                } catch (Exception e) {
                    addLog(e.toString());
                }

                release(v8Object,v8Array);


            }
        };

    }

    public static V8Object castFrom( Object object,Class<?> c){
        V8Object v8Object=new V8Object(MainActivityState.v8);
        for(Method m:c.getDeclaredMethods()){
            if(m.getName().equals("access$super")) {
            }
            //this method need to to consider specially.
            else if(m.getName().equals("pushJavaObject")){
                v8Object.registerJavaMethod(object,m.getName(),m.getName(),new Class<?>[]{});
            }

            else {
                v8Object.registerJavaMethod(generateFrom(m, object), m.getName());
            }
        }
        //v8Object.release();
        //not necessary, as the object is returned
        return v8Object;
    }


    private static Object getObject()
    {
        return MainActivityState.getObject();
    }



    public static void release(V8Object obj,V8Array array){
        obj.release();
        array.release();
    }
    private static JavaVoidCallback print(){
        return new JavaVoidCallback() {
            @Override
            public void invoke(V8Object v8Object, V8Array v8Array) {
                if(v8Array.length()>1){
                    addLog("[");
                }
                for(int i=0;i<v8Array.length();i++){
                    if(i>0){
                        addLog(",");
                    }
                    addLog(v8Array.get(i));

                }
                if(v8Array.length()>1){
                    addLog("]");
                }
                addLog("\n");
                AndroidV8.release(v8Object,v8Array);

            }
        };
    }
    private static void printObject(Object obj){

        if(obj instanceof V8Array){
            addLog("<V8Array,");
            for(int i=0;i<((V8Array) obj).length();i++){
                printObject(((V8Array) obj).get(i));
            }
            addLog(">");
        }
        else if(obj instanceof V8Function){
            addLog("<V8Function,");
            addLog(obj.toString());
            addLog(">");
        }
        else if(obj instanceof V8Object){
            addLog("<V8Object,");
            addLog(obj.toString());
            addLog(">");
        }
        else {
            addLog("<"+obj.getClass().getName()+",");
            addLog(obj.toString());
            addLog(">");
        }
    }
    private static JavaVoidCallback inspect(){
        return new JavaVoidCallback() {
            @Override
            public void invoke(V8Object v8Object, V8Array v8Array) {
                addLog("receriver:\n");
                printObject(v8Object);
                addLog("\nparameters\n");
                printObject(v8Array);
                addLog("\n");
                release(v8Object,v8Array);

            }
        };
    }
}
