//================================================================================================================================
//
//  Copyright (c) 2015-2017 VisionStar Information Technology (Shanghai) Co., Ltd. All Rights Reserved.
//  EasyAR is the registered trademark or trademark of VisionStar Information Technology (Shanghai) Co., Ltd in China
//  and other countries for the augmented reality technology developed by VisionStar Information Technology (Shanghai) Co., Ltd.
//
//================================================================================================================================

package cn.easyar.samples.helloar;

import javax.microedition.khronos.egl.EGL10;
import javax.microedition.khronos.egl.EGLConfig;
import javax.microedition.khronos.egl.EGLContext;
import javax.microedition.khronos.egl.EGLDisplay;
import javax.microedition.khronos.opengles.GL10;

import android.content.Context;
import android.opengl.GLSurfaceView;
import android.util.Log;
import android.view.MotionEvent;

import cn.easyar.engine.EasyAR;

public class GLView extends GLSurfaceView
{
    private HelloAR helloAR;
    private float moveX;
    private float moveY;


    public GLView(Context context)
    {
        super(context);
        setEGLContextFactory(new ContextFactory());
        setEGLConfigChooser(new ConfigChooser());

        helloAR = new HelloAR();

        this.setRenderer(new GLSurfaceView.Renderer() {
            @Override
            public void onSurfaceCreated(GL10 gl, EGLConfig config) {
                try {
                    synchronized (helloAR) {
                        helloAR.initGL();
                    }
                } catch (Throwable ex) {
                    Log.e("HelloAR", ex.getMessage());
                    Log.e("HelloAR", Log.getStackTraceString(ex));
                }
            }

            @Override
            public void onSurfaceChanged(GL10 gl, int w, int h) {
                try {
                    synchronized (helloAR) {
                        helloAR.resizeGL(w, h);
                    }
                } catch (Throwable ex) {
                    Log.e("HelloAR", ex.getMessage());
                    Log.e("HelloAR", Log.getStackTraceString(ex));
                }
            }

            @Override
            public void onDrawFrame(GL10 gl) {
                try {
                    synchronized (helloAR) {
                        helloAR.render(moveX,moveY);
                    }
                } catch (Throwable ex) {
                }
            }
        });
        this.setZOrderMediaOverlay(true);
    }

    @Override
    protected void onAttachedToWindow()
    {
        super.onAttachedToWindow();
        try {
            synchronized (helloAR) {
                if (helloAR.initialize()) {
                    helloAR.start();
                }
            }
        } catch (Throwable ex) {
            Log.e("HelloAR", ex.getMessage());
            Log.e("HelloAR", Log.getStackTraceString(ex));
        }
    }

    @Override
    protected void onDetachedFromWindow()
    {
        try {
            synchronized (helloAR) {
                helloAR.stop();
            }
        } catch (Throwable ex) {
            Log.e("HelloAR", ex.getMessage());
            Log.e("HelloAR", Log.getStackTraceString(ex));
        }
        try {
            synchronized (helloAR) {
                helloAR.dispose();
            }
        } catch (Throwable ex) {
            Log.e("HelloAR", ex.getMessage());
            Log.e("HelloAR", Log.getStackTraceString(ex));
        }
        super.onDetachedFromWindow();
    }

    public void setMotionXY(float x,float y){
        moveX += x/200.0f;
        moveY -= y/200.0f;
    }


    @Override
    public void onResume()
    {
        super.onResume();
        EasyAR.onResume();
    }

    @Override
    public void onPause()
    {
        EasyAR.onPause();
        super.onPause();
    }

    private static class ContextFactory implements GLSurfaceView.EGLContextFactory
    {
        private static int EGL_CONTEXT_CLIENT_VERSION = 0x3098;

        public EGLContext createContext(EGL10 egl, EGLDisplay display, EGLConfig eglConfig)
        {
            EGLContext context;
            int[] attrib = { EGL_CONTEXT_CLIENT_VERSION, 2, EGL10.EGL_NONE };
            context = egl.eglCreateContext(display, eglConfig, EGL10.EGL_NO_CONTEXT, attrib );
            return context;
        }

        public void destroyContext(EGL10 egl, EGLDisplay display, EGLContext context)
        {
            egl.eglDestroyContext(display, context);
        }
    }

    private static class ConfigChooser implements GLSurfaceView.EGLConfigChooser
    {
        public EGLConfig chooseConfig(EGL10 egl, EGLDisplay display)
        {
            final int EGL_OPENGL_ES2_BIT = 0x0004;
            final int[] attrib = { EGL10.EGL_RED_SIZE, 4, EGL10.EGL_GREEN_SIZE, 4, EGL10.EGL_BLUE_SIZE, 4,
                    EGL10.EGL_RENDERABLE_TYPE, EGL_OPENGL_ES2_BIT, EGL10.EGL_NONE };

            int[] num_config = new int[1];
            egl.eglChooseConfig(display, attrib, null, 0, num_config);

            int numConfigs = num_config[0];
            if (numConfigs <= 0)
                throw new IllegalArgumentException("fail to choose EGL configs");

            EGLConfig[] configs = new EGLConfig[numConfigs];
            egl.eglChooseConfig(display, attrib, configs, numConfigs,
                    num_config);

            for (EGLConfig config : configs)
            {
                int[] val = new int[1];
                int r = 0, g = 0, b = 0, a = 0, d = 0;
                if (egl.eglGetConfigAttrib(display, config, EGL10.EGL_DEPTH_SIZE, val))
                    d = val[0];
                if (d < 16)
                    continue;

                if (egl.eglGetConfigAttrib(display, config, EGL10.EGL_RED_SIZE, val))
                    r = val[0];
                if (egl.eglGetConfigAttrib(display, config, EGL10.EGL_GREEN_SIZE, val))
                    g = val[0];
                if (egl.eglGetConfigAttrib(display, config, EGL10.EGL_BLUE_SIZE, val))
                    b = val[0];
                if (egl.eglGetConfigAttrib(display, config, EGL10.EGL_ALPHA_SIZE, val))
                    a = val[0];
                if (r == 8 && g == 8 && b == 8 && a == 0)
                    return config;
            }

            return configs[0];
        }
    }
}
