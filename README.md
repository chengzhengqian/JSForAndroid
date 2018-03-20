# scriptor

## Introduction

This project aims to provide a simple but flexible platform to develop mobile apps on Android.
We use the V8 eigin to execuate the javascript code and implement a general way to dynamically expose java classes and interfaces by two simple function, load and proxy.
The user can seemlessly using the java code and pass javascript function arouud, so in principle, powered by the reflection ability of Java, one can explore the full capacilty of ANdroid native API and simplicity of Javascript.

For example, we first consider user define classes:

``` java

package com.serendipity.chengzhengqian.scriptor;

/* in  V8WrapClass.java */
public class V8WrapClass {
    public int i;
    public V8WrapClass(Integer i){this.i=i;}
    public int get(){return i;}
    public void run(V8WrapInterface t){t.Run(this);}
}

/* in   V8WrapInterface.java  */
public interface V8WrapInterface {
    public void Run(V8WrapClass x);
}

```

Then we could use them in Scriptor as

``` javascript
proxy("com.serendipity.chengzhengqian.scriptor.V8WrapInterface","V8I")
load("com.serendipity.chengzhengqian.scriptor.V8WrapClass","V8C")
i=new V8I({Run:"(v)=>{print(v.get())};"})
w=new V8C(12)
w.run(i)
```


Similary, we can easilty use Android API to create a GUI app

``` javascript

load("android.widget.Button","Btn")
proxy("android.view.View$OnClickListener","Clk")

app=new App()
root=new Root()
root.removeAllViews()
b=new Btn(app)
b.setText("sdfasd")
b.setOnClickListener(new Clk({onClick:"(v)=>{v.setText('123');}"}))
root.addView(b)

```

