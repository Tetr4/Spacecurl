
package de.klimek.spacecurl.game;

import java.nio.ByteBuffer;
import java.nio.ByteOrder;
import java.nio.FloatBuffer;

import javax.microedition.khronos.egl.EGL10;
import javax.microedition.khronos.egl.EGLConfig;
import javax.microedition.khronos.egl.EGLContext;
import javax.microedition.khronos.egl.EGLDisplay;
import javax.microedition.khronos.egl.EGLSurface;
import javax.microedition.khronos.opengles.GL11;

import android.content.Context;
import android.graphics.SurfaceTexture;
import android.opengl.GLES20;
import android.opengl.GLUtils;
import android.os.AsyncTask;
import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.TextureView;
import android.view.TextureView.SurfaceTextureListener;
import android.view.View;
import android.view.ViewGroup;
import de.klimek.spacecurl.R;

public class GameMaze extends GameFragment {
    public static final int DEFAULT_TITLE_RESOURCE_ID = R.string.game_maze;
    // private StatusBundle mStatusBundle;
    private GameDotView mGame;

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        mGame = new GameDotView(getActivity());
        return mGame;
    }

    @Override
    public void pauseGame() {
        mGame.pause();
    }

    @Override
    public void resumeGame() {
        mGame.resume();
    }

    @Override
    public FreeAxisCount getFreeAxisCount() {
        return FreeAxisCount.Two;
    }

    @Override
    public Effect[] getEffects() {
        Effect[] e = {
                Effect.Accuracy,
                Effect.Endurance
        };
        return e;
    }

    public class GameDotView extends TextureView implements SurfaceTextureListener {
        private static final String TAG = "GameDot";
        private AsyncTask<SurfaceTexture, Void, Void> _renderThread;
        private FloatBuffer mVertices;
        private final float[] mVerticesData = {
                0.0f, 0.5f, 0.0f, -0.5f, -0.5f, 0.0f, 0.5f, -0.5f, 0.0f
        };

        public GameDotView(Context context) {
            super(context);
            mVertices = ByteBuffer.allocateDirect(mVerticesData.length * 4)
                    .order(ByteOrder.nativeOrder()).asFloatBuffer();
            mVertices.put(mVerticesData).position(0);
            setSurfaceTextureListener(this);
        }

        @Override
        public void onSurfaceTextureAvailable(SurfaceTexture surface, int width, int height) {
            _renderThread = new RenderThread();
            _renderThread.execute(surface, null, null);
        }

        @Override
        public boolean onSurfaceTextureDestroyed(SurfaceTexture surface) {
            Log.d(TAG, "Destroyed");
            _renderThread.cancel(true);
            return true;
        }

        @Override
        public void onSurfaceTextureSizeChanged(SurfaceTexture surface, int width, int height) {

        }

        @Override
        public void onSurfaceTextureUpdated(SurfaceTexture surface) {

        }

        public void pause() {
            // _renderThread.cancel(true);
        }

        public void resume() {
            // _renderThread = new RenderThread();
            // _renderThread.execute(getSurfaceTexture(), null, null);
        }

        private class RenderThread extends AsyncTask<SurfaceTexture, Void, Void> {
            private static final int EGL_OPENGL_ES2_BIT = 4;
            private static final int EGL_CONTEXT_CLIENT_VERSION = 0x3098;
            private static final String TAG = "RenderThread";
            private SurfaceTexture _surface;
            private EGLDisplay _eglDisplay;
            private EGLSurface _eglSurface;
            private EGLContext _eglContext;
            private int _program;
            private EGL10 _egl;
            private GL11 _gl;

            @Override
            protected Void doInBackground(SurfaceTexture... surface) {
                _surface = surface[0];
                initGL();

                int attribPosition = GLES20.glGetAttribLocation(_program,
                        "position");
                checkGlError();

                GLES20.glEnableVertexAttribArray(attribPosition);
                checkGlError();

                GLES20.glUseProgram(_program);
                checkGlError();

                while (!isCancelled()) {
                    checkCurrent();

                    mVertices.position(0);
                    GLES20.glVertexAttribPointer(attribPosition, 3,
                            GLES20.GL_FLOAT, false, 0, mVertices);
                    checkGlError();

                    GLES20.glClearColor(1.0f, 1.0f, 0, 0);
                    checkGlError();

                    GLES20.glClear(GLES20.GL_COLOR_BUFFER_BIT);
                    checkGlError();

                    GLES20.glDrawArrays(GLES20.GL_TRIANGLES, 0, 3);
                    // Log.d(TAG, "draw!!");
                    checkGlError();

                    if (!_egl.eglSwapBuffers(_eglDisplay, _eglSurface)) {
                        Log.e(TAG, "cannot swap buffers!");
                    }
                    checkEglError();

                    try {
                        Thread.sleep(200);
                    } catch (InterruptedException e) {
                        // Ignore
                    }
                }
                return null;
            }

            private void checkCurrent() {
                if (!_eglContext.equals(_egl.eglGetCurrentContext())
                        || !_eglSurface.equals(_egl
                                .eglGetCurrentSurface(EGL10.EGL_DRAW))) {
                    checkEglError();
                    if (!_egl.eglMakeCurrent(_eglDisplay, _eglSurface,
                            _eglSurface, _eglContext)) {
                        throw new RuntimeException(
                                "eglMakeCurrent failed "
                                        + GLUtils.getEGLErrorString(_egl
                                                .eglGetError()));
                    }
                    checkEglError();
                }
            }

            private void checkEglError() {
                final int error = _egl.eglGetError();
                if (error != EGL10.EGL_SUCCESS) {
                    Log.e(TAG, "EGL error = 0x" + Integer.toHexString(error));
                }
            }

            private void checkGlError() {
                final int error = _gl.glGetError();
                if (error != GL11.GL_NO_ERROR) {
                    Log.e(TAG, "GL error = 0x" + Integer.toHexString(error));
                }
            }

            private int buildProgram(String vertexSource, String fragmentSource) {
                final int vertexShader = buildShader(GLES20.GL_VERTEX_SHADER,
                        vertexSource);
                if (vertexShader == 0) {
                    return 0;
                }

                final int fragmentShader = buildShader(
                        GLES20.GL_FRAGMENT_SHADER, fragmentSource);
                if (fragmentShader == 0) {
                    return 0;
                }

                final int program = GLES20.glCreateProgram();
                if (program == 0) {
                    return 0;
                }

                GLES20.glAttachShader(program, vertexShader);
                checkGlError();

                GLES20.glAttachShader(program, fragmentShader);
                checkGlError();

                GLES20.glLinkProgram(program);
                checkGlError();

                int[] status = new int[1];
                GLES20.glGetProgramiv(program, GLES20.GL_LINK_STATUS, status,
                        0);
                checkGlError();
                if (status[0] == 0) {
                    Log.e(TAG, GLES20.glGetProgramInfoLog(program));
                    GLES20.glDeleteProgram(program);
                    checkGlError();
                }

                return program;
            }

            private int buildShader(int type, String shaderSource) {
                final int shader = GLES20.glCreateShader(type);
                if (shader == 0) {
                    return 0;
                }

                GLES20.glShaderSource(shader, shaderSource);
                checkGlError();
                GLES20.glCompileShader(shader);
                checkGlError();

                int[] status = new int[1];
                GLES20.glGetShaderiv(shader, GLES20.GL_COMPILE_STATUS, status,
                        0);
                if (status[0] == 0) {
                    Log.e(TAG, GLES20.glGetShaderInfoLog(shader));
                    GLES20.glDeleteShader(shader);
                    return 0;
                }

                return shader;
            }

            private void initGL() {
                final String vertexShaderSource = "attribute vec4 position;\n"
                        +
                        "void main () {\n" +
                        "   gl_Position = position;\n" +
                        "}";

                final String fragmentShaderSource = "precision mediump float;\n"
                        +
                        "void main () {\n" +
                        "   gl_FragColor = vec4(1.0, 0.0, 0.0, 0.0);\n" +
                        "}";

                _egl = (EGL10) EGLContext.getEGL();

                _eglDisplay = _egl.eglGetDisplay(EGL10.EGL_DEFAULT_DISPLAY);
                if (_eglDisplay == EGL10.EGL_NO_DISPLAY) {
                    throw new RuntimeException("eglGetDisplay failed "
                            + GLUtils.getEGLErrorString(_egl.eglGetError()));
                }

                int[] version = new int[2];
                if (!_egl.eglInitialize(_eglDisplay, version)) {
                    throw new RuntimeException("eglInitialize failed "
                            + GLUtils.getEGLErrorString(_egl.eglGetError()));
                }

                int[] configsCount = new int[1];
                EGLConfig[] configs = new EGLConfig[1];
                int[] configSpec = {
                        EGL10.EGL_RENDERABLE_TYPE,
                        EGL_OPENGL_ES2_BIT,
                        EGL10.EGL_RED_SIZE, 8,
                        EGL10.EGL_GREEN_SIZE, 8,
                        EGL10.EGL_BLUE_SIZE, 8,
                        EGL10.EGL_ALPHA_SIZE, 8,
                        EGL10.EGL_DEPTH_SIZE, 0,
                        EGL10.EGL_STENCIL_SIZE, 0,
                        EGL10.EGL_NONE
                };

                EGLConfig eglConfig = null;
                if (!_egl.eglChooseConfig(_eglDisplay, configSpec, configs, 1,
                        configsCount)) {
                    throw new IllegalArgumentException(
                            "eglChooseConfig failed "
                                    + GLUtils.getEGLErrorString(_egl
                                            .eglGetError()));
                } else if (configsCount[0] > 0) {
                    eglConfig = configs[0];
                }
                if (eglConfig == null) {
                    throw new RuntimeException("eglConfig not initialized");
                }

                int[] attrib_list = {
                        EGL_CONTEXT_CLIENT_VERSION, 2, EGL10.EGL_NONE
                };
                _eglContext = _egl.eglCreateContext(_eglDisplay,
                        eglConfig, EGL10.EGL_NO_CONTEXT, attrib_list);
                checkEglError();
                _eglSurface = _egl.eglCreateWindowSurface(
                        _eglDisplay, eglConfig, _surface, null);
                checkEglError();
                if (_eglSurface == null || _eglSurface == EGL10.EGL_NO_SURFACE) {
                    int error = _egl.eglGetError();
                    if (error == EGL10.EGL_BAD_NATIVE_WINDOW) {
                        Log.e(TAG,
                                "eglCreateWindowSurface returned EGL10.EGL_BAD_NATIVE_WINDOW");
                        return;
                    }
                    throw new RuntimeException(
                            "eglCreateWindowSurface failed "
                                    + GLUtils.getEGLErrorString(error));
                }

                if (!_egl.eglMakeCurrent(_eglDisplay, _eglSurface,
                        _eglSurface, _eglContext)) {
                    throw new RuntimeException("eglMakeCurrent failed "
                            + GLUtils.getEGLErrorString(_egl.eglGetError()));
                }
                checkEglError();

                _gl = (GL11) _eglContext.getGL();
                checkEglError();

                _program = buildProgram(vertexShaderSource,
                        fragmentShaderSource);
            }
        }

    }

}
