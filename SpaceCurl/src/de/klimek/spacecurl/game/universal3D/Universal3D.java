
package de.klimek.spacecurl.game.universal3D;

import javax.microedition.khronos.egl.EGLConfig;
import javax.microedition.khronos.opengles.GL10;

import android.content.res.Resources;
import android.opengl.GLSurfaceView;
import android.opengl.GLU;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import de.klimek.spacecurl.R;
import de.klimek.spacecurl.game.GameFragment;

public class Universal3D extends GameFragment {
    public static final int DEFAULT_TITLE_RESOURCE_ID = R.string.game_universal;
    // private StatusBundle mStatusBundle;
    private GLSurfaceView mGame;

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        // mGame = new GameUniversal3DView(getActivity());
        mGame = new GLSurfaceView(getActivity());
        mGame.setRenderer(new GameUniversal3DRenderer(this, getResources()));
        return mGame;
    }

    @Override
    public void pauseGame() {
        mGame.onPause();
    }

    @Override
    public void resumeGame() {
        mGame.onResume();
    }

    @Override
    public FreeAxisCount getFreeAxisCount() {
        return FreeAxisCount.Three;
    }

    @Override
    public Effect[] getEffects() {
        Effect[] e = {
                Effect.Accuracy,
                Effect.Endurance
        };
        return e;
    }

    private static class GameUniversal3DRenderer implements GLSurfaceView.Renderer {
        private static final int FPS = 25;
        private Sphere mSphere;
        private long startTime;
        private long endTime;
        private long sleepTime;
        private GameFragment mContext;
        private Resources mResources;

        public GameUniversal3DRenderer(GameFragment context, Resources res) {
            mResources = res;
            mContext = context;
            mSphere = new Sphere(3, 2);
        }

        public void onSurfaceCreated(GL10 gl, EGLConfig config) {
            // initialize all the things required for openGL configurations
            // gl.glEnable(GL10.GL_POINT_SMOOTH);
            // gl.glPointSize(20.0f);

            mSphere.loadGLTexture(gl, mResources, R.drawable.earth);
            gl.glEnable(GL10.GL_TEXTURE_2D);
            gl.glShadeModel(GL10.GL_SMOOTH);
            gl.glClearColor(0.0f, 0.0f, 0.0f, 0.5f);
            gl.glClearDepthf(1.0f);
            gl.glEnable(GL10.GL_DEPTH_TEST);
            gl.glDepthFunc(GL10.GL_LEQUAL);
            gl.glHint(GL10.GL_PERSPECTIVE_CORRECTION_HINT, GL10.GL_NICEST);

        }

        public void onSurfaceChanged(GL10 gl, int width, int height) {
            if (height <= 0) {
                height = 1;
            }

            gl.glViewport(0, 0, width, height);
            gl.glMatrixMode(GL10.GL_PROJECTION);
            gl.glLoadIdentity();

            // Calculate The Aspect Ratio Of The Window
            GLU.gluPerspective(gl, 45.0f, (float) width / (float) height, 0.1f, 100.0f);

            gl.glMatrixMode(GL10.GL_MODELVIEW); // Select The Modelview Matrix
            gl.glLoadIdentity(); // Reset The Modelview Matrix
        }

        public void onDrawFrame(GL10 gl) {
            startTime = System.currentTimeMillis();

            // gl.glClear(GL10.GL_COLOR_BUFFER_BIT | GL10.GL_DEPTH_BUFFER_BIT);
            gl.glClear(GL10.GL_COLOR_BUFFER_BIT | GL10.GL_DEPTH_BUFFER_BIT);

            // Sensor dependent rotation
            gl.glLoadIdentity();
            // gl.glTranslatef(0.0f, 0.0f, -5.0f);
            gl.glMultMatrixf(mContext.getRotationMatrix(), 0);
            gl.glRotatef(90.0f, 1, 0, 0);

            mSphere.draw(gl);
            // Log.d("RENDER", "DRAW");

            endTime = System.currentTimeMillis();
            sleepTime = (1000 / FPS) - (endTime - startTime);
            try {
                Thread.sleep(sleepTime < 0 ? 0 : sleepTime);
            } catch (InterruptedException e) {
                // TODO Auto-generated catch block
                e.printStackTrace();
            }
        }
    }

}
