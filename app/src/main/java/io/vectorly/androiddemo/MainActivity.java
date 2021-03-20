package io.vectorly.androiddemo;

import android.os.Bundle;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.Button;
import android.widget.ListView;
import android.widget.RelativeLayout;
import android.widget.SeekBar;

import androidx.appcompat.app.AppCompatActivity;


import io.vectorly.glnnrender.GlPlayerView;
import io.vectorly.glnnrender.networks.NetworkTypes;

import com.google.android.exoplayer2.MediaItem;
import com.google.android.exoplayer2.SimpleExoPlayer;
import com.google.android.exoplayer2.source.DefaultMediaSourceFactory;
import com.google.android.exoplayer2.source.MediaSource;
import com.google.android.exoplayer2.upstream.DataSource;
import com.google.android.exoplayer2.upstream.DefaultDataSourceFactory;
import com.google.android.exoplayer2.util.Util;

import java.util.List;


public class MainActivity extends AppCompatActivity {

    private GlPlayerView ePlayerView;
    private SimpleExoPlayer player;
    private Button button;
    private SeekBar seekBar;
    private PlayerTimer playerTimer;
    private Button changeBtn;
    private String currentUrl = Constant.STREAM_URL_JELLYFISH;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        setUpViews();

    }

    @Override
    protected void onResume() {
        super.onResume();
        setUpSimpleExoPlayer();
        setUoGlPlayerView();
        setUpTimer();
    }

    @Override
    protected void onPause() {
        super.onPause();
        releasePlayer();
        if (playerTimer != null) {
            playerTimer.stop();
            playerTimer.removeMessages(0);
        }
    }

    private void setUpViews() {
        // play pause
        button = (Button) findViewById(R.id.btn);
        button.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (player == null) return;

                if (button.getText().toString().equals(MainActivity.this.getString(R.string.pause))) {
                    player.setPlayWhenReady(false);
                    button.setText(R.string.play);
                } else {
                    player.setPlayWhenReady(true);
                    button.setText(R.string.pause);
                }
            }
        });

        // seek
        seekBar = (SeekBar) findViewById(R.id.seekBar);
        seekBar.setOnSeekBarChangeListener(new SeekBar.OnSeekBarChangeListener() {
            @Override
            public void onProgressChanged(SeekBar seekBar, int progress, boolean fromUser) {
                if (player == null) return;

                if (!fromUser) {
                    // We're not interested in programmatically generated changes to
                    // the progress bar's position.
                    return;
                }

                player.seekTo(progress * 1000);
            }

            @Override
            public void onStartTrackingTouch(SeekBar seekBar) {
                // do nothing
            }

            @Override
            public void onStopTrackingTouch(SeekBar seekBar) {
                // do nothing
            }
        });

        changeBtn = (Button) findViewById(R.id.chngbtn);
        changeBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                player.setPlayWhenReady(true);
                onPause();
                if (currentUrl.equals(Constant.STREAM_URL_JELLYFISH)) {
                    currentUrl = Constant.STREAM_URL_DASH;
                } else {
                    currentUrl = Constant.STREAM_URL_JELLYFISH;
                }
                onResume();
                player.setPlayWhenReady(true);
                changeBtn.setText(R.string.change);

            }
        });


        // list
        ListView listView = (ListView) findViewById(R.id.list);

        final List<NetworkTypes> filterTypes = NetworkTypes.createFilterList();
        listView.setAdapter(new NetworkAdapter(this, R.layout.row_text, filterTypes));
        listView.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
                ePlayerView.setGlFilter(NetworkTypes.createGlFilter(filterTypes.get(position), getApplicationContext()));
            }
        });


    }


    private void setUpSimpleExoPlayer() {


        // Produces DataSource instances through which media data is loaded.
        DataSource.Factory dataSourceFactory = new DefaultDataSourceFactory(this, Util.getUserAgent(this, "superstream"));

        MediaItem mediaItem = new MediaItem.Builder()
                .setUri(currentUrl).build();
        MediaSource videoSource = new DefaultMediaSourceFactory(dataSourceFactory)
                .createMediaSource(mediaItem);


        // SimpleExoPlayer
        player = new SimpleExoPlayer.Builder(this).build();

        // Prepare the player with the source.
        player.setMediaSource(videoSource);
        player.setPlayWhenReady(true);
        player.prepare();

    }


    private void setUoGlPlayerView() {
        ePlayerView = new GlPlayerView(this);
        ePlayerView.setSimpleExoPlayer(player);
        ePlayerView.setLayoutParams(new RelativeLayout.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.MATCH_PARENT));
        ((MovieWrapperView) findViewById(R.id.layout_movie_wrapper)).addView(ePlayerView);
        ePlayerView.onResume();
    }


    private void setUpTimer() {
        playerTimer = new PlayerTimer();
        playerTimer.setCallback(new PlayerTimer.Callback() {
            @Override
            public void onTick(long timeMillis) {
                long position = player.getCurrentPosition();
                long duration = player.getDuration();

                if (duration <= 0) return;

                seekBar.setMax((int) duration / 1000);
                seekBar.setProgress((int) position / 1000);
            }
        });
        playerTimer.start();
    }


    private void releasePlayer() {
        ePlayerView.onPause();
        ((MovieWrapperView) findViewById(R.id.layout_movie_wrapper)).removeAllViews();
        ePlayerView = null;
        player.stop();
        player.release();
        player = null;
    }


}