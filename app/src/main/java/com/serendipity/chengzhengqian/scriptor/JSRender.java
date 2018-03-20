package com.serendipity.chengzhengqian.scriptor;

import android.opengl.GLES20;
import android.opengl.GLSurfaceView;

import javax.microedition.khronos.egl.EGLConfig;
import javax.microedition.khronos.opengles.GL10;
import java.nio.ByteBuffer;
import java.nio.ByteOrder;
import java.nio.FloatBuffer;
import java.util.HashMap;
import java.util.Map;

public class JSRender implements GLSurfaceView.Renderer {
    public HashMap<String,GLObject> objects=new HashMap<>();


    public static int loadShader(int type, String shaderCode){

        // create a vertex shader type (GLES20.GL_VERTEX_SHADER)
        // or a fragment shader type (GLES20.GL_FRAGMENT_SHADER)
        int shader = GLES20.glCreateShader(type);

        // addViews the source code to the shader and compile it
        GLES20.glShaderSource(shader, shaderCode);
        GLES20.glCompileShader(shader);

        return shader;
    }

    @Override
    public void onSurfaceCreated(GL10 gl10, EGLConfig eglConfig) {
        // Set the background frame color
        GLES20.glClearColor(0.0f, 0.0f, 0.0f, 1.0f);

    }

    @Override
    public void onSurfaceChanged(GL10 gl10, int width, int height) {
        GLES20.glViewport(0, 0, width, height);
    }

    public String command="";
    public String[] args;
    public static String triangle_string="triangle";
    @Override
    public void onDrawFrame(GL10 gl10) {
        // Redraw background color

        GLES20.glClear(GLES20.GL_COLOR_BUFFER_BIT | GL10.GL_DEPTH_BUFFER_BIT);

        // currently, it is strange that new GLxxx must be initialized in either onSurfacedCreate
        // or onDrawFrame (maybe onSurfacedChanged. If we call it from outside, it seems that no image
        // appears. Perhaps some GL function used in initializing the GLxxx must be called in these
        // contexts. (probablity the compile process?)
        if(!command.equals("")) {
            String[] commands = command.split(",");
            if (commands[0].equals(triangle_string)) {
                objects.put(args[0], new GLTriangle(args[1], args[2]));
            }

            command="";
        }
        for (GLObject i : objects.values()) {
            i.draw();
        }


    }
}
