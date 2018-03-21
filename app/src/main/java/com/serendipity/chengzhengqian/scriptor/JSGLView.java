package com.serendipity.chengzhengqian.scriptor;

import android.content.Context;
import android.opengl.GLSurfaceView;

public class JSGLView extends GLSurfaceView {
    public JSRender mRenderer;

    public JSGLView(Context context,GLSurfaceView.Renderer render) {
        super(context);
        setEGLContextClientVersion(2);

        setRenderer(render);
        // Render the view only when there is a change in the drawing data
        setRenderMode(GLSurfaceView.RENDERMODE_WHEN_DIRTY);

    }
}
