package com.tetris.pierre.music;

import android.content.Context;
import android.media.AudioManager;
import android.media.MediaPlayer;
import android.os.Handler;
import android.provider.MediaStore;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.ImageButton;
import android.widget.ListView;
import android.widget.SeekBar;
import android.widget.TextView;

import java.lang.reflect.Field;
import java.util.ArrayList;
import java.util.Timer;
import java.util.TimerTask;

public class MainActivity extends AppCompatActivity {

  private Timer timer;
  private TimerTask timerTask;

  private MediaPlayer mediaPlayer;
  private AudioManager audioManager;

  private TextView song;

  private ImageButton play;
  private ImageButton previous;
  private ImageButton next;

  private SeekBar volume;
  private SeekBar progress;

  private int actualIndex = 0;
  private ArrayList<Integer> songs = new ArrayList<>();
  private ArrayList<String> nameSongs = new ArrayList<>();
  private ListView list;

  private boolean paused = true;

  @Override
  protected void onCreate(Bundle savedInstanceState) {
    super.onCreate(savedInstanceState);
    setContentView(R.layout.activity_main);

    song = findViewById(R.id.cancion);
    play = findViewById(R.id.playStop);
    previous = findViewById(R.id.previous);
    next = findViewById(R.id.next);
    volume = findViewById(R.id.volume);
    progress = findViewById(R.id.progress);

    foundSongs();
    list = findViewById(R.id.lista);
    ArrayAdapter<String> adapter = new ArrayAdapter<>(this, android.R.layout.simple_list_item_1, nameSongs);
    list.setAdapter(adapter);

    audioManager = (AudioManager)getSystemService(Context.AUDIO_SERVICE);
    mediaPlayer = MediaPlayer.create(this, songs.get(0));

    progress.setMax(mediaPlayer.getDuration());
    song.setText(nameSongs.get(0));

    int maxVolume = audioManager.getStreamMaxVolume(AudioManager.STREAM_MUSIC);
    int actualVolume = audioManager.getStreamVolume(AudioManager.STREAM_MUSIC);
    volume.setMax(maxVolume);
    volume.setProgress(actualVolume);

    previous.setOnClickListener(new View.OnClickListener() {
      @Override
      public void onClick(View v) {
        newSong(-1);
      }
    });

    next.setOnClickListener(new View.OnClickListener() {
      @Override
      public void onClick(View v) {
        newSong(1);
      }
    });

    volume.setOnSeekBarChangeListener(new SeekBar.OnSeekBarChangeListener() {
      @Override
      public void onProgressChanged(SeekBar seekBar, int progress, boolean fromUser) {
        audioManager.setStreamVolume(AudioManager.STREAM_MUSIC, progress, 0);
      }

      @Override
      public void onStartTrackingTouch(SeekBar seekBar) {

      }

      @Override
      public void onStopTrackingTouch(SeekBar seekBar) {

      }
    });

    progress.setOnSeekBarChangeListener(new SeekBar.OnSeekBarChangeListener() {
      @Override
      public void onProgressChanged(SeekBar seekBar, int changed, boolean fromUser) {
        if(fromUser) {
          mediaPlayer.seekTo(changed);
        }
      }

      @Override
      public void onStartTrackingTouch(SeekBar seekBar) {

      }

      @Override
      public void onStopTrackingTouch(SeekBar seekBar) {

      }
    });

    list.setOnItemClickListener(new AdapterView.OnItemClickListener() {
      @Override
      public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
        reproduceSong(position);
      }
    });

    timer = new Timer();
    initTimer();
    timer.schedule(timerTask, 0, 1000);
  }

  public void playStop(View view) {
    if(mediaPlayer.isPlaying()) {
      mediaPlayer.pause();
      play.setImageResource(R.drawable.pause);
      paused = true;
    }
    else {
      mediaPlayer.start();
      play.setImageResource(R.drawable.play);
      paused = false;
    }
  }

  private void initTimer() {
    timerTask = new TimerTask() {
      public void run() {
        if(mediaPlayer.isPlaying()) {
          progress.setProgress(mediaPlayer.getCurrentPosition());
        }
        else if(!paused) {
          Handler mainHandler = new Handler(getApplicationContext().getMainLooper());
          Runnable myRunnable = new Runnable() {
            @Override
            public void run() {
              newSong(1);
            }
          };
          mainHandler.post(myRunnable);
        }
      }
    };
  }

  private void reproduceSong(int position) {
    progress.setProgress(0);
    if(mediaPlayer.isPlaying()) {
       mediaPlayer.stop();
    }
    mediaPlayer.reset();
    mediaPlayer = MediaPlayer.create(getApplicationContext(), songs.get(position));
    mediaPlayer.start();
    play.setImageResource(R.drawable.play);
    actualIndex = position;
    song.setText(nameSongs.get(actualIndex));
    progress.setMax(mediaPlayer.getDuration());
    paused = false;
  }

  private void newSong(int direction) {
    actualIndex += direction;
    if(actualIndex < 0) {
      actualIndex = songs.size() - 1;
    }
    else if(actualIndex == songs.size()) {
      actualIndex = 0;
    }
    reproduceSong(actualIndex);
  }

  private void foundSongs() {
    Field[] fields = R.raw.class.getFields();
    for(int i = 0; i < fields.length; i++){
      try {
        int resourceID = fields[i].getInt(fields[i]);
        songs.add(resourceID);
        nameSongs.add(getResources().getResourceEntryName(resourceID));
      }
      catch (IllegalAccessException e) {
        Log.i("Error", "No se encontraron las canciones");
      }
    }
  }

}
