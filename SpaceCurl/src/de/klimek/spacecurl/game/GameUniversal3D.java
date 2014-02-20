
package de.klimek.spacecurl.game;

import java.nio.ByteBuffer;
import java.nio.ByteOrder;
import java.nio.FloatBuffer;
import java.util.ArrayList;
import java.util.List;

import javax.microedition.khronos.egl.EGLConfig;
import javax.microedition.khronos.opengles.GL10;

import android.content.res.Resources;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.opengl.GLSurfaceView;
import android.opengl.GLU;
import android.opengl.GLUtils;
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

    public static class Sphere2 {

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
        public Sphere2(float radius, double step) {
            this.mRadius = radius;
            this.mStep = step;
            // initialize vertex byte buffer for shape coordinates
            ByteBuffer byteBuffer = ByteBuffer.allocateDirect(40000);
            // use the device hardware's native byte order
            byteBuffer.order(ByteOrder.nativeOrder());
            // create a floating point buffer from the ByteBuffer
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
                // add the coordinates to the FloatBuffer

                for (double theta = 0.0; theta <= (Math.PI * 2); theta += dTheta) {
                    sphereVertex.put((float) (mRadius * Math.sin(phi) * Math.cos(theta)));
                    sphereVertex.put((float) (mRadius * Math.sin(phi) * Math.sin(theta)));
                    sphereVertex.put((float) (mRadius * Math.cos(phi)));
                    points++;
                }
            }
            // set the buffer to read the first coordinate
            sphereVertex.position(0);
            return points;
        }
    }

    /**
     * Render a sphere.
     * 
     * @author Jim Cornmell
     * @since July 2013
     */
    public static class Sphere {
        /** 180 in radians. */
        public static final double ONE_EIGHTY_DEGREES = Math.PI;

        /** 360 in radians. */
        public static final double THREE_SIXTY_DEGREES = ONE_EIGHTY_DEGREES * 2;

        /** 120 in radians. */
        public static final double ONE_TWENTY_DEGREES = THREE_SIXTY_DEGREES / 3;

        /** 90 degrees, North pole. */
        public static final double NINETY_DEGREES = Math.PI / 2;

        /** Maximum allowed depth. */
        private static final int MAXIMUM_ALLOWED_DEPTH = 5;

        /**
         * Used in vertex strip calculations, related to properties of a
         * icosahedron.
         */
        private static final int VERTEX_MAGIC_NUMBER = 5;

        /** Each vertex is a 2D coordinate. */
        private static final int NUM_FLOATS_PER_VERTEX = 3;

        /** Each texture is a 2D coordinate. */
        private static final int NUM_FLOATS_PER_TEXTURE = 2;

        /** Each vertex is made up of 3 points, x, y, z. */
        private static final int AMOUNT_OF_NUMBERS_PER_VERTEX_POINT = 3;

        /**
         * Each texture point is made up of 2 points, x, y (in reference to the
         * texture being a 2D image).
         */
        private static final int AMOUNT_OF_NUMBERS_PER_TEXTURE_POINT = 2;

        /** Buffer holding the vertices. */
        private final List<FloatBuffer> mVertexBuffer = new ArrayList<FloatBuffer>();

        /** The vertices for the sphere. */
        private final List<float[]> mVertices = new ArrayList<float[]>();

        /** Buffer holding the texture coordinates. */
        private final List<FloatBuffer> mTextureBuffer = new ArrayList<FloatBuffer>();

        /** Mapping texture coordinates for the vertices. */
        private final List<float[]> mTexture = new ArrayList<float[]>();

        /** The texture pointer. */
        private final int[] mTextures = new int[1];

        /** Total number of strips for the given depth. */
        private final int mTotalNumStrips;

        /**
         * Sphere constructor.
         * 
         * @param depth integer representing the split of the sphere.
         * @param radius The spheres radius.
         */
        public Sphere(final int depth, final float radius) {
            // Clamp depth to the range 1 to MAXIMUM_ALLOWED_DEPTH;
            final int d = Math.max(1, Math.min(MAXIMUM_ALLOWED_DEPTH, depth));

            // Calculate basic values for the sphere.
            this.mTotalNumStrips = (int) (Math.pow(2, d - 1) * VERTEX_MAGIC_NUMBER);
            final int numVerticesPerStrip = (int) (Math.pow(2, d) * 3);
            final double altitudeStepAngle = ONE_TWENTY_DEGREES / Math.pow(2, d);
            final double azimuthStepAngle = THREE_SIXTY_DEGREES / this.mTotalNumStrips;
            double x, y, z, h, altitude, azimuth;

            for (int stripNum = 0; stripNum < this.mTotalNumStrips; stripNum++) {
                // Setup arrays to hold the points for this strip.
                final float[] vertices = new float[numVerticesPerStrip * NUM_FLOATS_PER_VERTEX]; // NOPMD
                final float[] texturePoints = new float[numVerticesPerStrip
                        * NUM_FLOATS_PER_TEXTURE]; // NOPMD
                int vertexPos = 0;
                int texturePos = 0;

                // Calculate position of the first vertex in this strip.
                altitude = NINETY_DEGREES;
                azimuth = stripNum * azimuthStepAngle;

                // Draw the rest of this strip.
                for (int vertexNum = 0; vertexNum < numVerticesPerStrip; vertexNum += 2) {
                    // First point - Vertex.
                    y = radius * Math.sin(altitude);
                    h = radius * Math.cos(altitude);
                    z = h * Math.sin(azimuth);
                    x = h * Math.cos(azimuth);
                    vertices[vertexPos++] = (float) x;
                    vertices[vertexPos++] = (float) y;
                    vertices[vertexPos++] = (float) z;

                    // First point - Texture.
                    // U texture coordinate mirrored (view inside sphere)
                    texturePoints[texturePos++] = -(float) (1 - azimuth / THREE_SIXTY_DEGREES);
                    texturePoints[texturePos++] = (float) (1 - (altitude + NINETY_DEGREES)
                            / ONE_EIGHTY_DEGREES);

                    // Second point - Vertex.
                    altitude -= altitudeStepAngle;
                    azimuth -= azimuthStepAngle / 2.0;
                    y = radius * Math.sin(altitude);
                    h = radius * Math.cos(altitude);
                    z = h * Math.sin(azimuth);
                    x = h * Math.cos(azimuth);
                    vertices[vertexPos++] = (float) x;
                    vertices[vertexPos++] = (float) y;
                    vertices[vertexPos++] = (float) z;

                    // Second point - Texture.
                    texturePoints[texturePos++] = -(float) (1 - azimuth / THREE_SIXTY_DEGREES);
                    texturePoints[texturePos++] = (float) (1 - (altitude + NINETY_DEGREES)
                            / ONE_EIGHTY_DEGREES);

                    azimuth += azimuthStepAngle;
                }

                this.mVertices.add(vertices);
                this.mTexture.add(texturePoints);

                ByteBuffer byteBuffer = ByteBuffer.allocateDirect(numVerticesPerStrip
                        * NUM_FLOATS_PER_VERTEX * Float.SIZE);
                byteBuffer.order(ByteOrder.nativeOrder());
                FloatBuffer fb = byteBuffer.asFloatBuffer();
                fb.put(this.mVertices.get(stripNum));
                fb.position(0);
                this.mVertexBuffer.add(fb);

                // Setup texture.
                byteBuffer = ByteBuffer.allocateDirect(numVerticesPerStrip * NUM_FLOATS_PER_TEXTURE
                        * Float.SIZE);
                byteBuffer.order(ByteOrder.nativeOrder());
                fb = byteBuffer.asFloatBuffer();
                fb.put(this.mTexture.get(stripNum));
                fb.position(0);
                this.mTextureBuffer.add(fb);
            }
        }

        /**
         * Load the texture for the square.
         * 
         * @param gl Handle.
         * @param context Handle.
         * @param texture Texture map for the sphere.
         */
        public void loadGLTexture(final GL10 gl, Resources res, int texture) {
            final Bitmap bitmap = BitmapFactory.decodeResource(res, texture);

            // Generate one texture pointer, and bind it to the texture array.
            gl.glGenTextures(1, this.mTextures, 0);
            gl.glBindTexture(GL10.GL_TEXTURE_2D, this.mTextures[0]);

            // Create nearest filtered texture.
            gl.glTexParameterf(GL10.GL_TEXTURE_2D, GL10.GL_TEXTURE_MIN_FILTER, GL10.GL_NEAREST);
            gl.glTexParameterf(GL10.GL_TEXTURE_2D, GL10.GL_TEXTURE_MAG_FILTER, GL10.GL_LINEAR);

            // Use Android GLUtils to specify a two-dimensional texture image
            // from our bitmap.
            GLUtils.texImage2D(GL10.GL_TEXTURE_2D, 0, bitmap, 0);

            // Tidy up.
            bitmap.recycle();
        }

        /**
         * The draw method for the square with the GL context.
         * 
         * @param gl Graphics handle.
         */
        public void draw(final GL10 gl) {
            // bind the previously generated texture.
            gl.glBindTexture(GL10.GL_TEXTURE_2D, this.mTextures[0]);

            // Point to our buffers.
            gl.glEnableClientState(GL10.GL_VERTEX_ARRAY);
            gl.glEnableClientState(GL10.GL_TEXTURE_COORD_ARRAY);

            // Set the face rotation, clockwise in this case.
            gl.glFrontFace(GL10.GL_CW);

            // Point to our vertex buffer.
            for (int i = 0; i < this.mTotalNumStrips; i++) {
                gl.glVertexPointer(AMOUNT_OF_NUMBERS_PER_VERTEX_POINT, GL10.GL_FLOAT, 0,
                        this.mVertexBuffer.get(i));
                gl.glTexCoordPointer(AMOUNT_OF_NUMBERS_PER_TEXTURE_POINT, GL10.GL_FLOAT, 0,
                        this.mTextureBuffer.get(i));

                // Draw the vertices as triangle strip.
                gl.glDrawArrays(GL10.GL_TRIANGLE_STRIP, 0, this.mVertices.get(i).length
                        / AMOUNT_OF_NUMBERS_PER_VERTEX_POINT);
            }

            // Disable the client state before leaving.
            gl.glDisableClientState(GL10.GL_VERTEX_ARRAY);
            gl.glDisableClientState(GL10.GL_TEXTURE_COORD_ARRAY);
        }
    }

}
