package com.wolffincdevelopment.hiit_it.widget;

import android.content.Context;
import android.content.Intent;
import android.util.AttributeSet;
import android.view.View;
import android.widget.ImageButton;
import android.widget.LinearLayout;

import com.squareup.otto.Subscribe;
import com.wolffincdevelopment.hiit_it.MusicListener;
import com.wolffincdevelopment.hiit_it.R;
import com.wolffincdevelopment.hiit_it.TrackData;
import com.wolffincdevelopment.hiit_it.activity.MusicService;

import butterknife.BindView;
import butterknife.ButterKnife;

/**
 * Created by Kyle Wolff on 11/1/16.
 */

public class MediaControllerView extends LinearLayout implements View.OnClickListener {

    @BindView(R.id.play)
    ImageButton playButton;

    @BindView(R.id.next)
    ImageButton nextButton;

    @BindView(R.id.prev)
    ImageButton prevButton;

    private MediaControllerListener listener;

    @Override
    public void onClick(View v) {

      if(v.equals(playButton)) {
          if(listener != null) {
              listener.onPlay();
          }
      } else if(v.equals(nextButton)) {
          if(listener != null) {
              listener.onNext();
          }
      } else if(v.equals(prevButton)) {
          if(listener != null) {
              listener.onPrev();
          }
      }

    }

    @Subscribe
    public void musicListener(MusicListener event) {

        if (event.paused) {
            playButton.setImageResource(R.drawable.ic_play_circle_outline_white);
        } else {
            playButton.setImageResource(R.drawable.ic_pause_circle_outline_white_48dp);
        }
    }

    public interface MediaControllerListener {

        void onPlay();
        void onNext();
        void onPrev();
    }

    public MediaControllerView(Context context) {
        this(context, null);
    }

    public MediaControllerView(Context context, AttributeSet attrs) {
        super(context, attrs);

        inflate( context, R.layout.media_controller, this );
        ButterKnife.bind( this );

        initListener();

    }

    public void initListener() {

        playButton.setOnClickListener(this);
        nextButton.setOnClickListener(this);
        prevButton.setOnClickListener(this);
    }

    public void setListener(MediaControllerListener listener) {
         this.listener = listener;
    }
}