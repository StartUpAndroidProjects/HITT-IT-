package com.wolffincdevelopment.hiit_it.activity;

import android.annotation.TargetApi;
import android.app.ActivityManager;
import android.app.ProgressDialog;
import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.content.ServiceConnection;
import android.content.SharedPreferences;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.media.AudioManager;
import android.os.Bundle;
import android.os.Handler;
import android.os.IBinder;
import android.os.Message;
import android.support.design.widget.FloatingActionButton;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.app.NotificationCompat;
import android.support.v7.widget.DefaultItemAnimator;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.support.v7.widget.Toolbar;
import android.view.View;
import android.widget.ImageButton;
import android.widget.ImageView;

import com.wolffincdevelopment.hiit_it.adapter.BaseAdapter;
import com.wolffincdevelopment.hiit_it.DividerItemDecoration;
import com.wolffincdevelopment.hiit_it.FirstTimePreference;
import com.wolffincdevelopment.hiit_it.handler.MessageHandler;
import com.wolffincdevelopment.hiit_it.R;
import com.wolffincdevelopment.hiit_it.TrackDBAdapter;
import com.wolffincdevelopment.hiit_it.TrackData;
import com.wolffincdevelopment.hiit_it.What;
import java.util.ArrayList;

import butterknife.BindView;
import butterknife.ButterKnife;
import butterknife.OnClick;

/**
 * Create by Kyle Wolff
 */
public class BaseActivity extends AppCompatActivity {

	public static final int ADD_ACTIVITY_RESULT_CODE = 232;

    private Intent playIntent;
    private NotificationCompat style;

    private RecyclerView.LayoutManager mLayoutManager;
    private BaseAdapter baseAdapter;
    private Handler handler;
    private What whatIntegers;
    private MessageHandler messageHandler;
    private Message setSoundIcon;
    private Bundle data;

    private TrackDBAdapter trackDBAdapter;

    private ArrayList<TrackData> songList;
    private ArrayList<TrackData> trackDataList;

    private FirstTimePreference prefFirstTime;

    private MusicService musicService;
    private ServiceConnection musicConnection;

    private MusicService.MusicBinder binder;

    private ProgressDialog progress;

    private boolean musicBound = false;
    private boolean musicConnected = false;

    @BindView( R.id.recycler_view )
    RecyclerView recyclerView;

    @BindView( R.id.first_time_user_add_image )
    ImageView firstTimeUserImageSwitcher;

    @BindView( R.id.play )
    ImageButton playButton;

    @BindView( R.id.next )
    ImageButton nextButton;

    @BindView( R.id.prev )
    ImageButton prevButton;

    @BindView( R.id.fab )
    FloatingActionButton fab;

    ///Click Handlers

    @OnClick(R.id.fab)
    protected void onFabPressed()
    {
        prefFirstTime.runCheckFirstTime( getString(R.string.firstTimeFabPressed) );

        Intent addTrackIntent = new Intent(BaseActivity.this, AddTrackActivity.class);
        startActivityForResult(addTrackIntent, ADD_ACTIVITY_RESULT_CODE);
    }

    @OnClick(R.id.play)
    protected void onPlayPressed()
    {
        if (musicService != null)
        {
            if (musicService.getSongs().isEmpty())
            {

            }
            else
            {
                TrackData currentSong = musicService.getCurrentSong();

                if(!musicService.isPlaying() && !musicService.isPaused())
                {
                    musicService.playSong(currentSong.getStartTime2(), currentSong.getStopTime3(), currentSong.getId());
                }
                else
                {
                    pauseResume();
                }
            }
        }
    }

    @OnClick(R.id.next)
    protected void onNextPressed()
    {
        nextSong();
    }

    @OnClick(R.id.prev)
    protected void onPrevPressed()
    {
        prevSong();
    }


	@Override
	protected void onActivityResult( int requestCode, int resultCode, Intent data )
	{
		super.onActivityResult( requestCode, resultCode, data );

		if (requestCode == ADD_ACTIVITY_RESULT_CODE )
		{
			setSongList("none", null);
		}
	}

	@Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        setContentView(R.layout.base_activity);
        ButterKnife.bind(this);

        Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);

        // Hides the default title for the activity so we can use our custom one
        getSupportActionBar().setDisplayShowTitleEnabled(false);

		prefFirstTime = new FirstTimePreference(this);
		trackDBAdapter = new TrackDBAdapter(this);
		mLayoutManager = new LinearLayoutManager(this);
		trackDataList = new ArrayList<>();
        whatIntegers = new What();
        data = new Bundle();

        handler = new Handler(new Handler.Callback() {
            @Override
            public boolean handleMessage(Message msg) {

                if(msg.what == whatIntegers.getRefreshSongList()) {

                    if(msg.getData().getSerializable("TrackData") != null) {
                        setSongList(msg.getData().getString("action"), (TrackData) msg.getData().getSerializable("TrackData"));
                    }

                } else if(msg.what == whatIntegers.getUpdatePlayControls()) {
                    updatePlayPauseButtons();
                } else if(msg.what == whatIntegers.getSetSoundIconVisible()) {
                    baseAdapter.updateSoundIcon( (String) msg.getData().get("id"), msg.getData().getBoolean("boolean"));
                } else if(msg.what == whatIntegers.getSetSoundIconNonVisible()) {
                    baseAdapter.setSoundIconInvisible( (String) msg.getData().get("id"));
                    updatePlayPauseButtons();
                } else if(msg.what == whatIntegers.getPlayThisSong()) {

                    for(TrackData trackData : trackDataList) {

                        if(msg.getData().getString("id") != null && trackData.getId().compareTo(msg.getData().getString("id")) == 0) {
                            musicService.playSong(trackData.getStartTime2(), trackData.getStopTime3(), trackData.getId());
                        }
                    }
                } else if(msg.what == whatIntegers.pauseResumeCurrentSong()) {
                    pauseResume();
                } else if(msg.what == whatIntegers.getCurrentSong()) {

                    if(msg.getData().getString("NULL") != null)
                    {
                        if(msg.getData().getString("NULL").compareTo("NULL") == 0)
                        {
                            baseAdapter.setCurrentSong(null);
                        }
                    }
                    else
                    {
                        baseAdapter.setCurrentSong(musicService.getCurrentSong());
                    }
                } else if(msg.what == whatIntegers.getNextOrPrev()) {

                    if(msg.getData().getString("next") != null && (msg.getData().getString("next").compareTo("next") ==0 )) {
                            nextSong();
                    }else {
                            prevSong();
                    }
                }
                return false;
            }
        });

        messageHandler = new MessageHandler(handler);

        setVolumeControlStream(AudioManager.STREAM_MUSIC);

        trackDBAdapter.open();
        trackDBAdapter.checkForStorageDeletion();
        trackDBAdapter.close();
    }

	@Override
	protected void onResume()
    {
		super.onResume();

        musicBound = false;

		init();

        if(musicService != null && ( musicService.isPlaying() || musicService.isPaused() ))
        {

            setSoundIconData();

            if (musicService.isPlaying())
            {
                data.putSerializable("boolean", true);
            }
            else if(musicService.isPaused())
            {
                data.putSerializable("boolean", false);
            }

            sendSoundIconMessage();
        }

		if(musicService != null && musicConnected)
        {
			dismissDialog();
		}
	}

    @Override
    protected void onPause()
    {
        super.onPause();

        if(musicService != null && musicService.isPlaying())
        {
            musicService.setNotification();
        }
    }

    @Override
    protected void onDestroy()
    {
        super.onDestroy();

        if(playIntent != null)
        {
            musicService.stopPlayer();
            stopService(playIntent);

            if(musicBound)
            {
                unbindService(musicConnection);
            }
        }
    }

    private void init()
    {
        //storageDeletion.getStorageAndDeletePlaylistItems();

		showDialog();

		checkFirstTimePreference();

		initRecyclerView();

		initMusicService();

        setSongList("none", null);

    }

    private void showDialog()
    {
        progress = new ProgressDialog(this);
        progress.setMessage("Loading...");
        progress.show();
    }

    private void dismissDialog() {
        progress.dismiss();
    }


	private void initRecyclerView()
	{
        // Recycler View Adapter, passing the arrayList
        baseAdapter = new BaseAdapter(trackDataList);
        baseAdapter.setHandler(messageHandler);

		recyclerView.setLayoutManager(mLayoutManager);
		recyclerView.setItemAnimator(new DefaultItemAnimator());
		recyclerView.setAdapter(baseAdapter);

		// Adding the divider lines to the recycler view
		recyclerView.addItemDecoration(new DividerItemDecoration(this));
	}

	private void initMusicService()
    {

        if(musicConnection == null) {

            musicConnection = new ServiceConnection() {
                @Override
                public void onServiceConnected(ComponentName name, IBinder service) {

                    binder = (MusicService.MusicBinder) service;
                    //get service
                    musicService = binder.getService();
                    //pass list
                    musicService.setList(trackDataList, "none", null);

                    musicService.setHandler(messageHandler);

                    progress.dismiss();
                    musicConnected = true;

                }

                @Override
                public void onServiceDisconnected(ComponentName name) {
                    musicBound = false;
                    musicConnected = false;
                }
            };
        }

        if(playIntent == null) {

            playIntent = new Intent(this, MusicService.class);
            bindService(playIntent, musicConnection, Context.BIND_AUTO_CREATE);
            startService(playIntent);

        }

        musicBound = true;

	}

    public void setSongList(String action, TrackData trackData)
    {
        trackDBAdapter.open();
        trackDataList = trackDBAdapter.getAllTracks();
        trackDBAdapter.close();

        if(musicService != null) {
            musicService.setList(trackDataList, action, trackData);
        }

        if(trackDataList != null)
        {
            baseAdapter.refresh(trackDataList);
        }
    }

    private void checkFirstTimePreference()
    {
        SharedPreferences sharedPreferences = getSharedPreferences("FirstKeyPreferences", Context.MODE_PRIVATE);

        if(sharedPreferences.contains(getBaseContext().getString(R.string.firstTimeFabPressed))) {
            firstTimeUserImageSwitcher.setVisibility(View.INVISIBLE);
        }else {
            firstTimeUserImageSwitcher.setVisibility(View.VISIBLE);
        }
    }

    public void updatePlayPauseButtons() {

        if (musicService.isPlaying()) {
            playButton.setImageResource(R.drawable.ic_pause_circle_outline_white_48dp);
            musicService.updateNotificationView();
        } else {
            playButton.setImageResource(R.drawable.ic_play_circle_outline_white);
            musicService.updateNotificationView();
        }
    }

    public void pauseResume()
    {
        setSoundIconData();

        if (musicService.isPaused() && !musicService.isPlaying()) {

            data.putSerializable("boolean", true);
            musicService.resume();
            updatePlayPauseButtons();

        } else {

            data.putSerializable("boolean", false);
            musicService.pausePlayer();
            updatePlayPauseButtons();
        }

        sendSoundIconMessage();
    }

    public void nextSong()
    {
        if(musicService != null) {
            TrackData song = musicService.getNextSong();

            if(song != null) {
                musicService.playNext(song.getStartTime2(), song.getStopTime3(), song.getId());
            }
        }
    }

    public void prevSong()
    {
        if(musicService != null) {
            TrackData song = musicService.getPreviousSong();

            if(song != null) {
                musicService.playPrev(song.getStartTime2(), song.getStopTime3(), song.getId());
            }
        }
    }

    public void sendSoundIconMessage()
    {
        setSoundIcon = messageHandler.createMessage(setSoundIcon, whatIntegers.getSetSoundIconVisible(), data);
        messageHandler.sendMessage(setSoundIcon);
    }

    public void setSoundIconData()
    {
        data.clear();
        if(!trackDataList.isEmpty()) {
            if (musicService.getCurrentSong() != null) {
                data.putSerializable("id", musicService.getCurrentSong().getId());
            }
        }
    }
}
