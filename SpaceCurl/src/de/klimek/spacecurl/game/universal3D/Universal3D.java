
package de.klimek.spacecurl.game.universal3D;

import java.util.ArrayList;

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
    private Universal3DSettings mSettings;

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        mSettings = (Universal3DSettings) getSettings();
        // mGame = new GameUniversal3DView(getActivity());
        mGame = new GLSurfaceView(getActivity());
        GameUniversal3DRenderer renderer = new GameUniversal3DRenderer(this, getResources(),
                mSettings.getDrawableResId(), mSettings.getTargets());
        mGame.setRenderer(renderer);
        return mGame;
    }

    @Override
    public void doPauseGame() {
        mGame.onPause();
    }

    @Override
    public void doResumeGame() {
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
        private static final float FOV = 75.0f;
        private Sphere mSphere;
        private Target mTarget;
        private long mStartTime;
        private long mEndTime;
        private long mSleepTime;
        private GameFragment mFragment;
        private Resources mResources;
        private int mDrawableResId;

        public GameUniversal3DRenderer(GameFragment fragment, Resources res, int drawableResId,
                ArrayList<Target> targets) {
            if (!targets.isEmpty()) {
                mTarget = targets.get(0);
            }
            mResources = res;
            mDrawableResId = drawableResId;
            mFragment = fragment;
            mSphere = new Sphere(3);
        }

        public void onSurfaceCreated(GL10 gl, EGLConfig config) {
            // initialize all the things required for openGL configurations
            // gl.glEnable(GL10.GL_POINT_SMOOTH);
            // gl.glPointSize(20.0f);
            mSphere.loadGLTexture(gl, mResources, mDrawableResId);
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
            GLU.gluPerspective(gl, FOV, (float) width / (float) height, 0.1f, 100.0f);

            gl.glMatrixMode(GL10.GL_MODELVIEW); // Select The Modelview Matrix
            gl.glLoadIdentity(); // Reset The Modelview Matrix
        }

        public void onDrawFrame(GL10 gl) {
            mStartTime = System.currentTimeMillis();

            // gl.glClear(GL10.GL_COLOR_BUFFER_BIT | GL10.GL_DEPTH_BUFFER_BIT);
            gl.glClear(GL10.GL_COLOR_BUFFER_BIT | GL10.GL_DEPTH_BUFFER_BIT);

            // Sensor dependent rotation
            gl.glLoadIdentity();
            // gl.glTranslatef(0.0f, 0.0f, -5.0f);
            gl.glMultMatrixf(mFragment.getRotationMatrix(), 0);
            gl.glRotatef(90.0f, 1, 0, 0);
            if (mTarget != null) {
                mTarget.draw(gl);
            }
            mSphere.draw(gl);
            // Log.d("RENDER", "DRAW");

            mEndTime = System.currentTimeMillis();
            mSleepTime = (1000 / FPS) - (mEndTime - mStartTime);
            try {
                Thread.sleep(mSleepTime < 0 ? 0 : mSleepTime);
            } catch (InterruptedException e) {
                // TODO Auto-generated catch block
                e.printStackTrace();
            }
        }
    }

}
