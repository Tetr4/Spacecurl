
package de.klimek.spacecurl.util.collection;

import java.util.ArrayList;

import android.os.Bundle;
import de.klimek.spacecurl.game.GameLights;
import de.klimek.spacecurl.game.GameMaze;
import de.klimek.spacecurl.game.GamePong;
import de.klimek.spacecurl.game.GameUniversal;

/**
 * Singleton
 * 
 * @author Mike
 */
public class Database {
    private ArrayList<Training> mTrainings = new ArrayList<Training>();
    private ArrayList<Status> mStatuses = new ArrayList<Status>();
    private Orientation mOrientation = Orientation.Portrait;

    private static enum Orientation {
        Landscape, Portrait
    }

    private static Database sInstance;

    public static Database getInstance() {
        if (sInstance == null) {
            sInstance = new Database();
        }
        return sInstance;
    }

    private Database() {
        fillDatabase();
    }

    private void fillDatabase() {
        // TODO SQL
        Training training1 = new Training("Test Training");
        training1.add(new GameSettingsPair(GamePong.class.getName(), new Bundle()));
        training1.add(new GameSettingsPair(GameUniversal.class.getName(), new Bundle()));
        training1.add(new GameSettingsPair(GameLights.class.getName(), new Bundle()));
        training1.add(new GameSettingsPair(GameMaze.class.getName(), new Bundle()));
        mTrainings.add(training1);

        Training training2 = new Training("Test Training 2");
        training2.add(new GameSettingsPair(GameUniversal.class.getName(), new Bundle()));
        training2.add(new GameSettingsPair(GamePong.class.getName(), new Bundle()));
        training2.add(new GameSettingsPair(GameUniversal.class.getName(), new Bundle()));
        training2.add(new GameSettingsPair(GameUniversal.class.getName(), new Bundle()));
        training2.add(new GameSettingsPair(GameLights.class.getName(), new Bundle()));
        mTrainings.add(training2);

        Training training3 = new Training("Blubb Training");
        training3.add(new GameSettingsPair(GameUniversal.class.getName(), new Bundle()));
        training3.add(new GameSettingsPair(GamePong.class.getName(), new Bundle()));
        training3.add(new GameSettingsPair(GameUniversal.class.getName(), new Bundle()));
        training3.add(new GameSettingsPair(GameUniversal.class.getName(), new Bundle()));
        training3.add(new GameSettingsPair(GameLights.class.getName(), new Bundle()));
        mTrainings.add(training3);

        Training training4 = new Training("Bla Training");
        training4.add(new GameSettingsPair(GameUniversal.class.getName(), new Bundle()));
        training4.add(new GameSettingsPair(GamePong.class.getName(), new Bundle()));
        training4.add(new GameSettingsPair(GameUniversal.class.getName(), new Bundle()));
        training4.add(new GameSettingsPair(GameUniversal.class.getName(), new Bundle()));
        training4.add(new GameSettingsPair(GameLights.class.getName(), new Bundle()));
        mTrainings.add(training4);
    }

    public void saveDatabase() {
        // TODO SQL
    }

    public ArrayList<Training> getTrainings() {
        return mTrainings;
    }

    public ArrayList<Status> getStatuses() {
        return mStatuses;
    }

    public Orientation getOrientation() {
        return mOrientation;
    }

    public void setOrientation(Orientation orientation) {
        mOrientation = orientation;
    }

}
