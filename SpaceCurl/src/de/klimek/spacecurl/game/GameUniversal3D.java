
package de.klimek.spacecurl.game;

import java.nio.ByteBuffer;
import java.nio.ByteOrder;
import java.nio.FloatBuffer;

import javax.microedition.khronos.egl.EGLConfig;
import javax.microedition.khronos.opengles.GL10;

import android.opengl.GLSurfaceView;
import android.opengl.GLU;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import de.klimek.spacecurl.R;

public class GameUniversal3D extends GameFragment {
    public static final int DEFAULT_TITLE_RESOURCE_ID = R.string.game_universal;
    // private StatusBundle mStatusBundle;
    private GLSurfaceView mGame;

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        // mGame = new GameUniversal3DView(getActivity());
        mGame = new GLSurfaceView(getActivity());
        mGame.setRenderer(new GameUniversal3DRenderer(this));
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

    private static class GameUniversal3DRenderer implements GLSurfaceView.Renderer {
        private static final int FPS = 25;
        private Sphere mSphere;
        private long startTime;
        private long endTime;
        private long sleepTime;
        private GameFragment context;

        public GameUniversal3DRenderer(GameFragment context) {
            this.context = context;
            mSphere = new Sphere(1, 18);
        }

        public void onSurfaceCreated(GL10 gl, EGLConfig config) {
            // initialize all the things required for openGL configurations
            gl.glClearColor(0.0f, 0.0f, 0.0f, 0.0f);
            gl.glHint(GL10.GL_PERSPECTIVE_CORRECTION_HINT, GL10.GL_NICEST);
            gl.glClearColor(0.0f, 0.0f, 0.0f, 1.0f);
            gl.glEnable(GL10.GL_POINT_SMOOTH);
            gl.glPointSize(20.0f);
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
            gl.glClear(GL10.GL_COLOR_BUFFER_BIT);
            gl.glLoadIdentity();

            // Change this value in z if you want to see the image zoomed in
            gl.glTranslatef(0.0f, 0.0f, -5.0f);

            // Sensor dependent rotation
            gl.glMultMatrixf(context.getRotationMatrix(), 0);

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

    public static class Sphere {

        static private FloatBuffer sphereVertex;
        static float sphere_parms[] = new float[3];

        double mRadius;
        double mStep;
        float mVertices[];
        private static double DEG = Math.PI / 180;
        int mPoints;

        /**
         * The value of step will define the size of each facet as well as the
         * number of facets
         * 
         * @param radius
         * @param step
         */

        public Sphere(float radius, double step) {
            this.mRadius = radius;
            this.mStep = step;
            // sphereVertex = FloatBuffer.allocate(40000);
            ByteBuffer byteBuffer = ByteBuffer.allocateDirect(40000);
            byteBuffer.order(ByteOrder.nativeOrder());
            sphereVertex = byteBuffer.asFloatBuffer();
            mPoints = build();
        }

        public void draw(GL10 gl) {
            // enable back facing polygons
            // gl.glFrontFace(GL10.GL_CW);
            gl.glEnableClientState(GL10.GL_VERTEX_ARRAY);
            gl.glVertexPointer(3, GL10.GL_FLOAT, 0, sphereVertex);

            gl.glColor4f(1.0f, 1.0f, 1.0f, 1.0f);
            gl.glDrawArrays(GL10.GL_POINTS, 0, mPoints);
            gl.glDisableClientState(GL10.GL_VERTEX_ARRAY);
        }

        private int build() {

            /**
             * x = p * sin(phi) * cos(theta) y = p * sin(phi) * sin(theta) z = p
             * * cos(phi)
             */
            double dTheta = mStep * DEG;
            double dPhi = dTheta;
            int points = 0;

            for (double phi = -(Math.PI); phi <= 0; phi += dPhi) {
                // for each stage calculating the slices
                for (double theta = 0.0; theta <= (Math.PI * 2); theta += dTheta) {
                    sphereVertex.put((float) (mRadius * Math.sin(phi) * Math.cos(theta)));
                    sphereVertex.put((float) (mRadius * Math.sin(phi) * Math.sin(theta)));
                    sphereVertex.put((float) (mRadius * Math.cos(phi)));
                    points++;
                }
            }
            sphereVertex.position(0);
            return points;
        }
    }
}
