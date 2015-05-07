
package de.klimek.spacecurl.game.universal3D;

import java.nio.ByteBuffer;
import java.nio.ByteOrder;
import java.nio.FloatBuffer;

import javax.microedition.khronos.opengles.GL10;

public class Target {
    private static double DEG = Math.PI / 180;
    private static final float DISTANCE_TO_CENTER = 0.5f;
    private float mRadius;
    private FloatBuffer mTargetVertex;

    /**
     * Sphere constructor.
     * 
     * @param inclination is the angle with the horizontal plane
     * @param azimuth is the "left-right" angle
     */
    public Target(float inclination, float azimuth) {
        float theta = (float) (inclination * DEG);
        float phi = (float) (azimuth + DEG);
        this.mRadius = 80.0f;
        ByteBuffer byteBuffer = ByteBuffer.allocateDirect(100);
        byteBuffer.order(ByteOrder.nativeOrder());
        mTargetVertex = byteBuffer.asFloatBuffer();
        // X Y Z from spherical to Cartesian coordinates
        mTargetVertex.put((float) (DISTANCE_TO_CENTER * Math.sin(theta) *
                Math.cos(phi)));
        mTargetVertex.put((float) (DISTANCE_TO_CENTER * Math.sin(theta) *
                Math.sin(phi)));
        mTargetVertex.put((float) (DISTANCE_TO_CENTER * Math.cos(theta)));

        mTargetVertex.position(0);
    }

    public void draw(GL10 gl) {
        // enable back facing polygons
        // gl.glFrontFace(GL10.GL_CW);
        gl.glEnable(GL10.GL_POINT_SMOOTH);
        gl.glPointSize(mRadius);

        gl.glEnableClientState(GL10.GL_VERTEX_ARRAY);
        gl.glVertexPointer(3, GL10.GL_FLOAT, 0, mTargetVertex);

        // gl.glEnable(GL10.GL_BLEND); // Transparency
        // gl.glBlendFunc(GL10.GL_SRC_ALPHA, GL10.GL_ONE_MINUS_SRC_ALPHA);
        gl.glColor4f(1.0f, 0.0f, 0.0f, 1.0f);
        gl.glDrawArrays(GL10.GL_POINTS, 0, 1);

        gl.glDisableClientState(GL10.GL_VERTEX_ARRAY);
        gl.glColor4f(1.0f, 1.0f, 1.0f, 1.0f);
    }
}
