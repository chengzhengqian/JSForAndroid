package com.serendipity.chengzhengqian.scriptor;

import android.content.Context;
import android.opengl.GLSurfaceView;

public class JSGLView extends GLSurfaceView {
    public JSRender mRenderer;

    public JSGLView(Context context) {
        super(context);
        setEGLContextClientVersion(2);
        mRenderer=new JSRender();
        setRenderer(mRenderer);
        // Render the view only when there is a change in the drawing data
        setRenderMode(GLSurfaceView.RENDERMODE_WHEN_DIRTY);

    }
}
