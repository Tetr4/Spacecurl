
package de.klimek.spacecurl.util.collection.training;

import java.util.ArrayList;

import de.klimek.spacecurl.game.GameDescription;

public class Training extends ArrayList<GameDescription> {
    private static final long serialVersionUID = -4346310670231953747L;
    private String mTitle;

    public Training(String title) {
        setTitle(title);
    }

    public String getTitle() {
        return mTitle;
    }

    public void setTitle(String title) {
        mTitle = title;
    }

}
