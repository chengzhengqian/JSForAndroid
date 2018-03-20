package com.serendipity.chengzhengqian.scriptor;

import com.eclipsesource.v8.*;

import java.lang.reflect.*;
import java.util.*;

/*
a new interface aim at fully reflective generate javascript interface to java classes
 */
public class JavaV8Interface  {
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
                    Object v=v8Array.get(i);
                    addLog(v);
                    release(v);
                }
                if(v8Array.length()>1){
                    addLog("]");
                }
                addLog("\n");


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
        release(obj);
    }
    private static JavaVoidCallback inspect(){
        return new JavaVoidCallback() {
            @Override
            public void invoke(V8Object v8Object, V8Array v8Array) {
                addLog("receriver:\n");
                printObject(v8Object);
                addLog("\nparameters:\n");
                printObject(v8Array);
                addLog("\n");
                //release(v8Object,v8Array);

            }
        };
    }
    public static V8 createRuntime(){
        V8 v8=V8.createV8Runtime();
        v8.registerJavaMethod(print(),"print");
        v8.registerJavaMethod(inspect(),"inspect");
        v8.registerJavaMethod(inspectClass(v8,FULL),"inspectFull");
        v8.registerJavaMethod(inspectClass(v8,SIMPLE),"inspectJava");
        v8.registerJavaMethod(importClassTypeAsObject(v8),"loadClass");
        v8.registerJavaMethod(importClass(v8),"load");
        v8.registerJavaMethod(proxyInterface(v8),"proxy");
        registerJavaObject(v8,MainActivityState.currentActivity,"app",INSTANTIALMETHODS);
        registerJavaObject(v8,MainActivityState.currentActivity.ui_ll,"root",INSTANTIALMETHODS);

//        v8.registerJavaMethod(generateFromObject(v8,MainActivityState.currentActivity,INSTANTIALMETHODS),"App");
//        v8.registerJavaMethod(generateFromObject(v8,MainActivityState.currentActivity.ui_ll,INSTANTIALMETHODS),"Root");
//            v8.registerJavaMethod(generateFromClass(v8,"com.serendipity.chengzhengqian.scriptor.V8WrapClass"),
//
        return v8;
    }
    public static void runJavascript(String s){
        try {
            V8 v8= createRuntime();

//            v8.registerJavaMethod(generateFromClass(v8,"com.serendipity.chengzhengqian.scriptor.V8WrapClass"),
//                    "V8WrapClass");
            v8.executeVoidScript(s);

            v8.release();
        }catch (Exception e){
            MainActivityState.currentActivity.addLog(e.toString());
        }
    }
    public static String inspectMethod(Method m){
        StringBuilder b=new StringBuilder();
        b.append(m.getReturnType().getName()+" ");
        b.append(m.getName());
        b.append("(");
        int i=0;
        for(Class t:m.getParameterTypes()){
            if(i>0){
                b.append(", ");
            }
            b.append(t.getName());
        }
        b.append(");\n");
        return b.toString();

    }
    public static void release(Object v){
        if(v instanceof Releasable)
            ((Releasable) v).release();
    }
    public static int FULL=1;
    public static int SIMPLE=2;
    //some demos
    public static JavaCallback inspectClass(final V8 v8, final int style){
        return new JavaCallback() {
            @Override
            public Object invoke( V8Object v8Object, V8Array v8Array) {
                StringBuilder s=new StringBuilder();
                for(int i=0;i<v8Array.length();i++){
                    Object v=v8Array.get(i);
                    Object jv=convertToJavaObject(v);
                    Class c= jv.getClass();
                    if(style==FULL) {
                        Method[] ms=c.getMethods();
                        for (Method m : ms) {
                            s.append(inspectMethod(m));

                        }
                        s.append("------------------\n");
                    }
                    release(v);

                    s.append(c.getName()+":\n"+jv.toString()+"\n");
                }

                return s.toString();
            }
        };
    }
    /*
    We need some flexibility here, as we need to cast object
    ?/
     */
    public static boolean isPrimitiveMatch(Class<?> c,List<Object> paras, int k){
        Object obj=paras.get(k);
        if(c.equals(double.class)){
            if(obj instanceof Double){
                return true;
            }
            else if(obj instanceof Integer){
                paras.set(k,Double.valueOf((Integer)obj));
                return true;
            }
            else {
                return false;
            }

        }
        else if(c.equals(int.class)){
            if(obj instanceof Integer){
                return true;
            }

            else if(obj instanceof Double){
                paras.set(k,Integer.valueOf((Integer) obj));
                return true;
            }
            else {
                return false;
            }

        }
        else if(c.equals(float.class)){
            if(obj instanceof Integer){
                paras.set(k,Float.valueOf((Integer)obj));
                return true;
            }

            else if(obj instanceof Double){
                paras.set(k,((Double) obj).floatValue());
                return true;
            }

            else {
                return false;
            }

        }
        return false;
    }
    public static  boolean isMatch(Constructor<?> ctr, List<Object> paras){
        Class<?>[] types=ctr.getParameterTypes();
        if(types.length==paras.size()){
            for(int k=0;k<types.length;k++){
                if(isPrimitiveMatch(types[k],paras,k)||types[k].isInstance(paras.get(k))){

                }
                else{
                    return false;
                }
            }
            return true;
        }
        else
            return false;
    }
    public static  boolean isMatch(Method m, List<Object> paras){
        Class<?>[] types=m.getParameterTypes();
        if(types.length==paras.size()){
            for(int k=0;k<types.length;k++){
                if(isPrimitiveMatch(types[k],paras,k)||types[k].isInstance(paras.get(k))){
                }
                else{
                    return false;
                }
            }
            return true;
        }
        else
            return false;
    }
    public static Method findMatched(List<Method> ms,List<Object> paras){
        for(Method m:ms){
            if(isMatch(m,paras)){
                return m;
            }
        }
        return null;
    }
    public static Constructor<?> findMatched(Constructor<?>[] ctrs,List<Object> paras){
        for(Constructor<?> ctr:ctrs){
            if(isMatch(ctr,paras)){
                return ctr;
            }
        }
        return null;
    }
    public static void addLog(Object s){
        MainActivityState.currentActivity.log.append(s.toString());
    }
    public static String getConstrutorsInfo(Constructor<?>[] ctrs){
        StringBuilder s=new StringBuilder();
        for(Constructor<?> c: ctrs){
            s.append("(");
            int i=0;
            for(Class<?> t:c.getParameterTypes()){
                if(i>0){
                    s.append(", ");
                }
                s.append(t.getName());
                i++;
            }
            s.append(")\n");

        }
        return s.toString();
    }
    public static String getMethodsInfo(List<Method> ctrs){
        StringBuilder s=new StringBuilder();
        for(Method c: ctrs){
            s.append("(");
            int i=0;
            for(Class<?> t:c.getParameterTypes()){
                if(i>0){
                    s.append(", ");
                }
                s.append(t.getName());
                i++;
            }
            s.append(")\n");

        }
        return s.toString();
    }
    public static String getParasInfo(List<Object> paras){
        StringBuilder s=new StringBuilder();
        s.append("(");
        for(Object obj:paras){
            s.append(obj.getClass().getName()+" ");
        }
        s.append(")");
        return s.toString();
    }
    private static HashMap<Integer,Object> javaObjects=new HashMap<>();
    private static Integer currentId=0;
    private static final Integer MAXIMUMID=10000;
    private static Integer getId(Object obj){
        currentId+=1;
        if(currentId>MAXIMUMID){
            currentId=0;
        }
        javaObjects.put(currentId,obj);
        return currentId;
    }
    private static Object getObj(Integer id){
        return javaObjects.remove(id);
    }
    public static JavaCallback generateGetId(final Object obj){
        return new JavaCallback() {
            @Override
            public Object invoke(V8Object v8Object, V8Array v8Array) {
                return getId(obj);
            }
        };
    }
    private static final String GETIDJAVASCRIPTFUNCNAME="__id__";
    public static Object convertToJavaObject(Object obj){

        if(obj instanceof V8Object){
            V8Object jsObj= (V8Object) obj;
            Integer id=jsObj.executeIntegerFunction(GETIDJAVASCRIPTFUNCNAME,null);
            return getObj(id);
        }
        else {
            return obj;
        }

    }
    public static List<Object> convertToJavaObjects(List<Object> params){
        ArrayList<Object> javaObjs=new ArrayList<>();
        for(Object para:params){
            if(para instanceof V8Object){
               V8Object jsObj= (V8Object) para;
               Integer id=jsObj.executeIntegerFunction(GETIDJAVASCRIPTFUNCNAME,null);
               javaObjs.add(getObj(id));
            }
            else {
                javaObjs.add(para);
            }
        }
        return javaObjs;

    }
    public static boolean isPremitiveType(Object t){
        if(t instanceof Integer){
            return true;
        }

        else if(t instanceof Double){
            return true;
        }
        else if(t instanceof Float){
            return true;
        }
        else if(t instanceof String){
            return true;
        }
        else {
            return false;
        }
    }
    public static boolean isConvertibleToString(Object t){
        if(t instanceof Character){
            return true;
        }
        else {
            return false;
        }
    }

    public static void registerMethods(V8 v8, Object obj,V8Object v8obj, Class<?> c,int type){
        Map<String,List<Method>> gms=groupOverloaded(c.getMethods(),type);
        for(String s:gms.keySet()){
            v8obj.registerJavaMethod(generateFromGroupMethods(v8,obj,gms.get(s)),s);
        }
    }
    public static void registerField(V8 v8, Object obj, V8Object v8obj, Class<?> c, int type){
        for(Field f:c.getFields()){
            if(Modifier.isStatic(f.getModifiers())){
                if(type==ALLMETHODS||type==STATICMETHEDS)
                    v8obj.registerJavaMethod(generateFromField(v8,f,null),f.getName());
            }
            else {
                if(type==ALLMETHODS||type==INSTANTIALMETHODS)
                    v8obj.registerJavaMethod(generateFromField(v8,f,obj),f.getName());
            }
        }
    }
    public static void registerGetId(Object obj,V8Object v8obj){
        v8obj.registerJavaMethod(generateGetId(obj),GETIDJAVASCRIPTFUNCNAME);
    }
    /*
    wrap a java object to a V8Obj
    * */

    public static Object wrapToJavaScript(Object obj,V8 v8,int type){
        V8Object v8obj=new V8Object(v8);
        //Method[] ms=obj.getClass().getDeclaredMethods();

        registerMethods(v8,obj,v8obj,obj.getClass(),type);
        registerField(v8,obj,v8obj,obj.getClass(),type);
        registerGetId(obj,v8obj);
//        Method[] ms=obj.getClass().getMethods();
//        for(Method m:ms){
//            v8obj.registerJavaMethod(generateFromMethod(obj,m,v8),m.getName());
//        }

        return v8obj;
    }
    public static Object convertToJavaScriptObject(Object b, V8 v8){
        if(b==null){
            return b;
        }
        if(isPremitiveType(b)){
            return b;
        }
        else if(isConvertibleToString(b)){
            return b.toString();
        }
        else {
            // just instantiated method
            return wrapToJavaScript(b,v8,1);
        }
    }


    public static List<Object> getParamsFromV8Array(V8Array v8Array){
        int length = v8Array.length();
        ArrayList<Object> paras=new ArrayList<>();

        for(int k=0;k<length;k++){
            paras.add(v8Array.get(k));
        }
        return paras;

    }
    public static void releaseParams(List<Object> paras){
        for(Object i:paras){
        if(i instanceof Releasable){
            ((Releasable) i).release();
        }
    }}
    public static void registerJavaObject(V8 v8,Object obj,String jsName, int type){
        Object jsObj=wrapToJavaScript(obj,v8,type);
        v8.add(jsName, (V8Value) jsObj);
        release(jsObj);
    }


    /*
    *  type=0 all methods
    *  type=1 just instantial methods
    *  type=2 just static methods
    * */
    final static int ALLMETHODS=0;
    final static int INSTANTIALMETHODS=1;
    final static int STATICMETHEDS=2;
    public static boolean isMethodBelongToType(Method m, int type){
        if(type==ALLMETHODS){
            return true;
        }
        else if(type==INSTANTIALMETHODS){
            if(Modifier.isStatic(m.getModifiers())){
                return false;
            }
            else {
                return true;
            }
        }
        else if(type==STATICMETHEDS){
            if(Modifier.isStatic(m.getModifiers())){
                return true;
            }
            else {
                return false;
            }
        }
        else
            return true;
    }
    public static Map<String,List<Method>> groupOverloaded(Method[] ms, int type){
        HashMap<String,List<Method>> gms=new HashMap<>();
        for(Method m:ms){
            if(isMethodBelongToType(m,type)){
                if(gms.get(m.getName())==null){
                    List<Method> gm=new ArrayList<>();
                    gm.add(m);
                    gms.put(m.getName(),gm);
                }
                else{
                    gms.get(m.getName()).add(m);
                }
            }
        }
        return gms;

    }
    public static JavaCallback generateFromGroupMethods(final V8 v8, final Object obj, final List<Method> ms){
        return new JavaCallback() {
            @Override
            public Object invoke(V8Object v8Object, V8Array v8Array) {
                List<Object> paras= getParamsFromV8Array(v8Array);
                List<Object> java_paras= convertToJavaObjects(paras);
                Method m=findMatched(ms,java_paras);
                Object result=null;
                if(m!=null){
                    try {
                       result=m.invoke(obj,java_paras.toArray());
                    } catch (Exception e) {
                        addLog(e.toString());
                    }
                }
                else{
                    addLog(ms.get(0).getName()+" does not an overloaed with signature\n"+ getParasInfo(java_paras)
                            +"\nFollowing are candidates:\n"+getMethodsInfo(ms));

                }
                releaseParams(paras);
                return convertToJavaScriptObject(result,v8);
            }
        };
    }
    /*
  Just for Class type, i.e static method associated with a class, but just register a object
   */
    public static void registerClassType(final V8 v8, final String className,final String jsName){

        try {
            Class<?> c = Class.forName(className);
            V8Object jsObj=new V8Object(v8);
            registerMethods(v8,null,jsObj,c,STATICMETHEDS);
            registerField(v8,null,jsObj,c,STATICMETHEDS);
            registerGetId(c,jsObj);
//            Map<String, List<Method>> gms = groupOverloaded(c.getMethods(), STATICMETHEDS);
//            for (String s : gms.keySet()) {
//                jsObj.registerJavaMethod(generateFromGroupMethods(v8, null, gms.get(s)), s);
//            }
//            jsObj.registerJavaMethod(generateGetId(c), GETIDJAVASCRIPTFUNCNAME);
//
//            Field[] fields=c.getFields();
//            for(Field f:fields){
//                if(Modifier.isStatic(f.getModifiers())){
//                    jsObj.registerJavaMethod(generateFromField(v8,f,null),f.getName());
//                }
//            }

            v8.add(jsName,jsObj);
            release(jsObj);
            //Method[] ms=c.getDeclaredMethods();
//                        Method[] ms=c.getMethods();
//                        //Collection<Method> ms=getAllMethods(c); this is not necessary
//                        for(Method m:ms){
//                           // v8Object.registerJavaMethod(obj,m.getName(),m.getName(),m.getParameterTypes());
//                            v8Object.registerJavaMethod(generateFromMethod(obj,m,v8),m.getName());
//                        }
            //v8Object.registerJavaMethod(generateGetId(c), GETIDJAVASCRIPTFUNCNAME);
        }
        catch (Exception e){
            addLog(e.toString());
        }


    }
    /*
    Just for Class type, i.e static method associated with a class
     */
    public static JavaCallback generateFromClassType(final V8 v8, final String className){
        return new JavaCallback() {
            @Override
            public Object invoke(V8Object v8Object, V8Array v8Array) {

              try {
                  Class<?> c = Class.forName(className);

                  Map<String, List<Method>> gms = groupOverloaded(c.getMethods(), STATICMETHEDS);
                  for (String s : gms.keySet()) {
                      v8Object.registerJavaMethod(generateFromGroupMethods(v8, null, gms.get(s)), s);
                  }

                  //Method[] ms=c.getDeclaredMethods();
//                        Method[] ms=c.getMethods();
//                        //Collection<Method> ms=getAllMethods(c); this is not necessary
//                        for(Method m:ms){
//                           // v8Object.registerJavaMethod(obj,m.getName(),m.getName(),m.getParameterTypes());
//                            v8Object.registerJavaMethod(generateFromMethod(obj,m,v8),m.getName());
//                        }
                  v8Object.registerJavaMethod(generateGetId(c), GETIDJAVASCRIPTFUNCNAME);
              }
              catch (Exception e){
                  addLog(e.toString());
              }



                return v8Object;
            }
        };
    }
    public static JavaCallback generateFromField(final V8 v8, final Field f, final Object obj){
        return new JavaCallback() {
            @Override
            public Object invoke(V8Object v8Object, V8Array v8Array) {
                try {
                    Object jObj=f.get(obj);
                    return convertToJavaScriptObject(jObj,v8);
                } catch (Exception e) {
                    addLog(e.toString());
                }
                return null;
            }
        };
    }
    public static JavaCallback generateFromClass(final V8 v8, final String className){
        return new JavaCallback() {
            @Override
            public Object invoke(V8Object v8Object, V8Array v8Array) {
                ArrayList<Object> paras= (ArrayList<Object>) getParamsFromV8Array(v8Array);
                List<Object> java_paras=  convertToJavaObjects(paras);
                try {
                    Class<?> c=Class.forName(className);
                    Constructor<?>[] ctrs=c.getConstructors();
                    Constructor<?> ctr=findMatched(ctrs,java_paras);
                    if(ctr!=null){
                        Object obj=ctr.newInstance(java_paras.toArray());
                        registerMethods(v8,obj,v8Object,c,INSTANTIALMETHODS);
                        registerField(v8,obj,v8Object,c,INSTANTIALMETHODS);
                        registerGetId(obj,v8Object);
//                        Map<String,List<Method>> gms=groupOverloaded(c.getMethods(),INSTANTIALMETHODS);
//                        for(String s:gms.keySet()){
//                            v8Object.registerJavaMethod(generateFromGroupMethods(v8,obj,gms.get(s)),s);
//                        }
//
//                        //Method[] ms=c.getDeclaredMethods();
////                        Method[] ms=c.getMethods();
////                        //Collection<Method> ms=getAllMethods(c); this is not necessary
////                        for(Method m:ms){
////                           // v8Object.registerJavaMethod(obj,m.getName(),m.getName(),m.getParameterTypes());
////                            v8Object.registerJavaMethod(generateFromMethod(obj,m,v8),m.getName());
////                        }
//                        v8Object.registerJavaMethod(generateGetId(obj),GETIDJAVASCRIPTFUNCNAME);
//
//                        /* Now, deal with fields*/
//                        Field[] fields=c.getFields();
//                        for(Field f:fields){
//                            if(!Modifier.isStatic(f.getModifiers())){
//                                v8Object.registerJavaMethod(generateFromField(v8,f,obj),f.getName());
//                            }
//                        }

                    }
                    else {
                        addLog(c.getName()+" does not a constructor with signature\n"+ getParasInfo(java_paras)+"\nFollowing are candidates:\n"+getConstrutorsInfo(ctrs));
                    }

                } catch (Exception e) {
                     addLog(e.toString());
                }
                releaseParams(paras);
                return v8Object;
            }
        };
    }
    public static String getDefaultName(String s){
        String[] sl=s.split("\\.");
        return sl[sl.length-1];
    }

    public static JavaCallback importClass(final V8 v8){
        return new JavaCallback() {
            @Override
            public Object invoke(V8Object v8Object, V8Array v8Array) {
                List<Object> paras=getParamsFromV8Array(v8Array);
                if(paras.size()>2 || paras.size()==0) {
                    releaseParams(paras);
                    return "load(full_name) or load(full_name,name)";
                }
                else if(paras.size()==2) {
                    if(paras.get(0) instanceof String || paras.get(1) instanceof String) {
                        v8.registerJavaMethod(generateFromClass(v8, (String) paras.get(0)),(String) paras.get(1));
                    }
                    else {
                        return "name should be String";
                    }
                }
                else if(paras.size()==1){
                    if(paras.get(0) instanceof String ) {
                        v8.registerJavaMethod(generateFromClass(v8, (String) paras.get(0)),getDefaultName((String) paras.get(0)));
                    }
                    else {
                        return "name should be String";
                    }
                }

                releaseParams(paras);

                return "load all!";
            }
        };
    }
    /*
    static method for a class, this instead creating a javascript object wrapping the class instance and all static methods.
    this is more reasonable than return a function
     */
    public static JavaCallback importClassTypeAsObject(final V8 v8){
        return new JavaCallback() {
            @Override
            public Object invoke(V8Object v8Object, V8Array v8Array) {
                List<Object> paras=getParamsFromV8Array(v8Array);
                if(paras.size()>2 || paras.size()==0) {
                    releaseParams(paras);
                    return "loadClass(full_name) or loadClass(full_name,name)";
                }
                else if(paras.size()==2) {
                    if(paras.get(0) instanceof String || paras.get(1) instanceof String) {
                       // v8.registerJavaMethod(generateFromClassType(v8, (String) paras.get(0)),(String) paras.get(1));
                        registerClassType(v8, (String) paras.get(0),(String) paras.get(1));
                    }
                    else {
                        return "name should be String";
                    }
                }
                else if(paras.size()==1){
                    if(paras.get(0) instanceof String ) {
                      //  v8.registerJavaMethod(generateFromClassType(v8, (String) paras.get(0)),getDefaultName((String) paras.get(0))+"_");
                        registerClassType(v8, (String) paras.get(0),getDefaultName((String) paras.get(0))+"_");
                    }
                    else {
                        return "name should be String";
                    }
                }

                releaseParams(paras);

                return "load all!";
            }
        };
    }

    public static JavaCallback proxyInterface(final V8 v8){
        return new JavaCallback() {
            @Override
            public Object invoke(V8Object v8Object, V8Array v8Array) {
                List<Object> paras=getParamsFromV8Array(v8Array);
                releaseParams(paras);
                if(paras.size()>2 || paras.size()==0) {
                    releaseParams(paras);
                    return "proxy(full_name) or proxy(full_name,name)";
                }
                else if(paras.size()==2) {
                    if(paras.get(0) instanceof String || paras.get(1) instanceof String) {
                        v8.registerJavaMethod(generateFromInterface(v8, (String) paras.get(0)),(String) paras.get(1));
                    }
                    else {
                        return "name should be String";
                    }
                }
                else if(paras.size()==1){
                    if(paras.get(0) instanceof String ) {
                        v8.registerJavaMethod(generateFromInterface(v8, (String) paras.get(0)),getDefaultName((String) paras.get(0)));
                    }
                    else {
                        return "name should be String";
                    }
                }
                return "load all inteface!";
            }
        };
    }

    private static JavaCallback generateFromInterface(final V8 v8, final String interfaceName) {
        return new JavaCallback() {
            @Override
            public Object invoke(V8Object v8Object, V8Array v8Array) {
                Object obj=null;
                try {
                    Class<?> c=Class.forName(interfaceName);
                    Class<?>[] cs=new Class[]{c};
                    Object v=v8Array.get(0);
                    obj=Proxy.newProxyInstance(c.getClassLoader(),cs,new ProxyListener((V8Object) v,v8));
                    JavaV8Interface.release(v);
                } catch (ClassNotFoundException e) {
                    addLog(interfaceName+"-->"+e.toString()+"\n");
                }
                return convertToJavaScriptObject(obj,v8);
            }
        };
    }



}
