
package de.klimek.spacecurl.util.collection;

import java.util.ArrayList;

import android.os.Parcel;
import android.os.Parcelable;

public class Training extends ArrayList<GameSettingsPair> implements Parcelable {
    private static final long serialVersionUID = -4346310670231953747L;
    private String mTitle;

    public Training(String title) {
        setTitle(title);
    }

    public Training(Parcel in) {
        setTitle(in.readString());
        ArrayList<GameSettingsPair> list = new ArrayList<GameSettingsPair>();
        in.readList(list, Training.class.getClassLoader());
        addAll(list);
    }

    public String getTitle() {
        return mTitle;
    }

    public void setTitle(String title) {
        mTitle = title;
    }

    @Override
    public int describeContents() {
        // TODO Auto-generated method stub
        return 0;
    }

    @Override
    public void writeToParcel(Parcel dest, int flags) {
        dest.writeString(mTitle);
        dest.writeList((ArrayList<GameSettingsPair>) this);
    }

    public static final Parcelable.Creator<Training> CREATOR = new Parcelable.Creator<Training>() {
        public Training createFromParcel(Parcel in) {
            return new Training(in);
        }

        public Training[] newArray(int size) {
            return new Training[size];
        }
    };

}
