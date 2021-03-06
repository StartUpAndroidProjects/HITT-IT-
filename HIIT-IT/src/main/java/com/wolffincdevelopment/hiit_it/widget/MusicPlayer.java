package com.wolffincdevelopment.hiit_it.widget;

import android.media.MediaPlayer;

/**
 * Created by Kyle Wolff on 11/3/16.
 */

public class MusicPlayer extends MediaPlayer {

    private MediaControllerView mediaControllerView;

    public MusicPlayer() {
        super();
    }

    public void setMediaControllerView(MediaControllerView mediaControllerView) {
        this.mediaControllerView = mediaControllerView;
    }

    @Override
    public void start() throws IllegalStateException {
        super.start();

        if (mediaControllerView != null) {
            mediaControllerView.updatePlayButton(false, false);
        }
    }

    @Override
    public void pause() throws IllegalStateException {
        super.pause();

        if (mediaControllerView != null) {
            mediaControllerView.updatePlayButton(true, false);
        }
    }

    @Override
    public void stop() throws IllegalStateException {
        super.stop();

        if (mediaControllerView != null) {
            mediaControllerView.updatePlayButton(true, true);
        }

    }
}
