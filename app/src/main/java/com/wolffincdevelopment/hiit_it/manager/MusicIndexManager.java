package com.wolffincdevelopment.hiit_it.manager;

/**
 * Created by Kyle Wolff on 11/2/16.
 */

public class MusicIndexManager {

    private static MusicIndexManager manager = null;

    private int index;
    private int trackListLength;

    private MusicIndexManager(){};

    public static MusicIndexManager getInstance() {

        if(manager == null) {
            manager = new MusicIndexManager();
        }

        return manager;
    }

    public void setTrackListLength(int trackListLength) {
        this.trackListLength = trackListLength;
    }

    public int getIndex() {

        if(index != trackListLength) {
            return index;
        } else {
            return index = 0;
        }
    }

    public int getPrevIndex() {

        if (trackListLength != 0) {

            index--;

            if (index == trackListLength) {
                index = 0;
            } else if (index > trackListLength - 1) {
                index = 0;
            } else if (index == -1) {
                index = trackListLength - 1;
            }
        }

        return getIndex();
    }

    public int getNextIndex() {

        if(trackListLength != 0) {

            index++;

            if(index >= trackListLength) {
                index = 0;
            }
        }

        return getIndex();
    }

}