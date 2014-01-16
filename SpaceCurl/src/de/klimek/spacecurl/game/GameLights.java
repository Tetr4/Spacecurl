
package de.klimek.spacecurl.game;

import android.content.Context;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import de.klimek.spacecurl.R;

public class GameLights extends GameFragment {
    public static final int DEFAULT_TITLE_RESOURCE_ID = R.string.game_lights;
    // private StatusBundle mStatusBundle;
    private GamePlaneView mGame;

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        mGame = new GamePlaneView(getActivity());
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
        return FreeAxisCount.Zero;
    }

    @Override
    public Effect[] getEffects() {
        Effect[] e = {};
        return e;
    }

    private class GamePlaneView extends View {

        public GamePlaneView(Context context) {
            super(context);
            // TODO Auto-generated constructor stub
        }

        public void pause() {
            // TODO Auto-generated method stub

        }

        public void resume() {
            // TODO Auto-generated method stub

        }

    }

}
