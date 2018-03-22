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

load("android.widget.Button","Btn");
proxy("android.view.View$OnClickListener","Clk");
load("android.widget.LinearLayout$LayoutParams","LLP");
load("android.widget.LinearLayout","LL");


root.removeAllViews();
lp=new LLP(0,-2,1.0);
l=new LL(app)
for(i=0;i<5;i++){
	b=new Btn(app);
        b.setText("btn");
	b.setLayoutParams(lp)
	b.setOnClickListener(new Clk({onClick:"(v)=>{v.setText('clicked');}"}));
	l.addView(b);
		    
}
root.addView(l)
```


Now, we can use @@(new)threadName@@ in the callback to indicates which thread the code should be run.

``` javascript
load("android.opengl.GLSurfaceView","GLV")
loadClass("android.opengl.GLSurfaceView","GLV_")
proxy("android.opengl.GLSurfaceView$Renderer","GLR")
loadClass("android.opengl.GLES20","GL")
load("android.widget.Button","Btn");
proxy("android.view.View$OnClickListener","Clk");
load("android.widget.LinearLayout$LayoutParams","LLP");
load("android.widget.LinearLayout","LL");

root.removeAllViews();
lp=new LLP(-1,-2,0.0);
b=new Btn(app);
b.setOnClickListener(new Clk({onClick:"(v)=>{gl.requestRender()}"}));
b.setText("chang color");
b.setLayoutParams(lp);

root.addView(b);

lp=new LLP(-1,0,8.0);



gl=new GLV(app);
gl.setEGLContextClientVersion(2);

gl.setRenderer(new GLR({
onSurfaceCreated:`
    @@newgl@@
        (x,config)=>{
	load("java.nio.ByteBuffer","BF");
	loadClass("java.nio.ByteBuffer","BF_");
	loadClass("java.nio.ByteOrder","BO_");
	a=0.2;

    triangleCoords=jArray([0,0.5,0   ,0.5,0,0,   -0.5,-0.5,0],"float");

    bf=BF_.allocateDirect( triangleCoords.length()*4)
    bf.order(BO_.nativeOrder());
    vf=bf.asFloatBuffer();
    vf.put(triangleCoords);
    vf.position(0);
    vertexShaderCode =
    "attribute vec4 vPosition;" +
    "void main() {" +
    "  gl_Position = vPosition;" +
    "}";
    fragmentShaderCode =
    "precision mediump float;" +
    "uniform vec4 vColor;" +
    "void main() {" +
    "  gl_FragColor = vColor;" +
    "}";
    loadShader=(type,code)=>{
	shader = GL.glCreateShader(type);
	GL.glShaderSource(shader, code);
	GL.glCompileShader(shader);
	return shader;
	};
    vertexShader = loadShader(GL.GL_VERTEX_SHADER(),vertexShaderCode);
        fragmentShader = loadShader(GL.GL_FRAGMENT_SHADER(),fragmentShaderCode);
	mProgram = GL.glCreateProgram();
	GL.glAttachShader(mProgram, vertexShader);
	GL.glAttachShader(mProgram, fragmentShader);
	GL.glLinkProgram(mProgram);
	COORDS_PER_VERTEX=3;
	vertexCount = triangleCoords.length() / COORDS_PER_VERTEX;
	vertexStride = COORDS_PER_VERTEX * 4;
	}
`,

onDrawFrame:`
    @@gl@@
        (x)=>{
	a=a+0.05
	GL.glClearColor(1.0, a, 1-a, 1.0);
	color=jArray([0.5-a,0.5+a,2*a,0.5],"float");
	c=GL.GL_COLOR_BUFFER_BIT();
	GL.glClear(c);
	GL.glUseProgram(mProgram);
	mPositionHandle = GL.glGetAttribLocation(mProgram, "vPosition");
	GL.glEnableVertexAttribArray(mPositionHandle);
	GL.glVertexAttribPointer(mPositionHandle, COORDS_PER_VERTEX,
						  GL.GL_FLOAT(), false,
						  vertexStride, vf);
	mColorHandle = GL.glGetUniformLocation(mProgram, "vColor");
        GL.glUniform4fv(mColorHandle, 1, color, 0);
	GL.glDrawArrays(GL.GL_TRIANGLES(), 0, vertexCount);
	GL.glDisableVertexAttribArray(mPositionHandle);

}
`,

onSurfaceChanged:`
    @@gl@@
        (x,w,h)=>{
	GL.glViewport(0, 0, w, h);
	};
`
}));

mode=GLV_.RENDERMODE_WHEN_DIRTY()
gl.setRenderMode(mode)
gl.setLayoutParams(lp);
root.addView(gl)

})"}"
}
}))

```