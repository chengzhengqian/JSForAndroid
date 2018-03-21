package com.serendipity.chengzhengqian.scriptor;

import com.android.dx.*;

import java.io.File;
import java.io.PrintStream;
import java.lang.reflect.Method;
import java.lang.reflect.Modifier;

public final class CodeMaker {
    public static String codeDir="/generated";
    public static Object generate()  {
        DexMaker dexMaker = new DexMaker();

        // Generate a HelloWorld class.
        TypeId<?> helloWorld = TypeId.get("LHelloWorld;");
        dexMaker.declare(helloWorld, "HelloWorld.generated", Modifier.PUBLIC, TypeId.OBJECT);
        generateHelloMethod(dexMaker, helloWorld);

        // Create the dex file and load it.
        File outputDir = new File(MainActivityState.currentActivity.getFilesDir().toString()+codeDir);
        outputDir.mkdir();
        ClassLoader loader = null;
        try {
            loader = dexMaker.generateAndLoad(CodeMaker.class.getClassLoader(),
                    outputDir);
            Class<?> helloWorldClass = loader.loadClass("HelloWorld");

            // Execute our newly-generated code in-process.
            Method a=helloWorldClass.getMethod("hello");
            Object s=a.invoke(null);
            JavaV8Interface.addLog(s);
        }

        catch (Exception e) {
            return e.toString();
        }
        return "generated and run";
    }

    /**
     * Generates Dalvik bytecode equivalent to the following method.
     *    public static void hello() {
     *        int a = 0xabcd;
     *        int b = 0xaaaa;
     *        int c = a - b;
     *        String s = Integer.toHexString(c);
     *        System.out.println(s);
     *        return;
     *    }
     */
    private static void generateHelloMethod(DexMaker dexMaker, TypeId<?> declaringType) {
        // Lookup some types we'll need along the way.
//        TypeId<System> systemType = TypeId.get(System.class);
//        TypeId<PrintStream> printStreamType = TypeId.get(PrintStream.class);

        // Identify the 'hello()' method on declaringType.
        MethodId hello = declaringType.getMethod(TypeId.STRING, "hello");

        // Declare that method on the dexMaker. Use the returned Code instance
        // as a builder that we can append instructions to.
        Code code = dexMaker.declare(hello, Modifier.STATIC | Modifier.PUBLIC);

        // Declare all the locals we'll need up front. The API requires this.
        Local<Integer> a = code.newLocal(TypeId.INT);
        Local<Integer> b = code.newLocal(TypeId.INT);
        Local<Integer> c = code.newLocal(TypeId.INT);
        Local<String> s = code.newLocal(TypeId.STRING);
//        Local<PrintStream> localSystemOut = code.newLocal(printStreamType);

        // int a = 0xabcd;
        code.loadConstant(a, 0xabcd);

        // int b = 0xaaaa;
        code.loadConstant(b, 0xaaaa);

        // int c = a - b;
        code.op(BinaryOp.SUBTRACT, c, a, b);

        // String s = Integer.toHexString(c);
        MethodId<Integer, String> toHexString
                = TypeId.get(Integer.class).getMethod(TypeId.STRING, "toHexString", TypeId.INT);
        code.invokeStatic(toHexString, s, c);
        code.returnValue(s);
        // System.out.println(s);
//        FieldId<System, PrintStream> systemOutField = systemType.getField(printStreamType, "out");
//        code.sget(systemOutField, localSystemOut);
//        MethodId<PrintStream, Void> printlnMethod = printStreamType.getMethod(
//                TypeId.VOID, "println", TypeId.STRING);
//        code.invokeVirtual(printlnMethod, null, localSystemOut, s);
//        MethodId<JavaV8Interface,Void> print=TypeId.get(JavaV8Interface.class).getMethod(TypeId.VOID,"addLog",TypeId.OBJECT);
//        code.invokeStatic(print,null,s);
        // return;
       // code.returnValue(s);
    }
}