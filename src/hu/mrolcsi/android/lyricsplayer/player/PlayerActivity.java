package hu.mrolcsi.android.lyricsplayer.player;

import android.app.Activity;
import android.content.SharedPreferences;
import android.content.res.Configuration;
import android.os.Bundle;
import android.os.Environment;
import android.os.Handler;
import android.preference.PreferenceManager;
import android.util.Log;
import android.view.View;
import android.widget.*;
import com.un4seen.bass.BASS;
import hu.mrolcsi.android.filebrowser.BrowserDialog;
import hu.mrolcsi.android.lyricsplayer.R;
import hu.mrolcsi.android.lyricsplayer.player.media.Song;
import org.jaudiotagger.audio.exceptions.CannotReadException;
import org.jaudiotagger.audio.exceptions.InvalidAudioFrameException;
import org.jaudiotagger.audio.exceptions.ReadOnlyFileException;
import org.jaudiotagger.tag.TagException;

import java.io.File;
import java.io.IOException;

/**
 * Created with IntelliJ IDEA.
 * User: Matusinka Roland
 * Date: 2014.04.21.
 * Time: 18:44
 */

public class PlayerActivity extends Activity {

    private static final String TAG = "LyricsPlayer.Player";
    private static final String PREF_LASTSONG = "LyricsPlayer.lastSong";
    Handler timerHandler = new Handler();
    Runnable timerRunnable = new Runnable() {
        //To start handler, call:
        //timerHandler.postDelayed(timerRunnable, 0);

        //To stop handler, call:
        //timerHandler.removeCallbacks(timerRunnable);

        @Override
        public void run() {
            //do stuff
            tvElapsedTime.setText(currentSong.getElapsedTimeString());
            tvRemainingTime.setText(currentSong.getRemainingTimeString());
            sbProgress.setProgress((int) currentSong.getElapsedTimeSeconds());

            //500ms interval
            timerHandler.postDelayed(this, 500);
        }
    };
    private Song currentSong;
    private SharedPreferences sharedPrefs;
    private SharedPreferences.Editor editor;
    private ImageButton btnPlayPause;
    private ImageView imgCover;
    private ImageButton btnOpen;
    private ImageButton btnPrev;
    private ImageButton btnNext;
    private TextView tvTitle;
    private TextView tvArtistAlbum;
    private TextView tvElapsedTime;
    private TextView tvRemainingTime;
    private SeekBar sbProgress;

    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        //first of all: init BASS
        BASS.BASS_Init(1, 44100, 0);

        setContentView(R.layout.player);
        sharedPrefs = PreferenceManager.getDefaultSharedPreferences(this);
        editor = sharedPrefs.edit();

        initViews();
        initListeners();

        if (sharedPrefs.contains(PREF_LASTSONG)) {
            loadSong(sharedPrefs.getString(PREF_LASTSONG, null));
        }
    }

    @Override
    public void onConfigurationChanged(Configuration newConfig) {
        super.onConfigurationChanged(newConfig);
        // TODO: implement method
    }

    private void initViews() {
        btnOpen = (ImageButton) findViewById(R.id.btnOpen);
        btnPlayPause = (ImageButton) findViewById(R.id.btnPlayPause);
        btnPrev = (ImageButton) findViewById(R.id.btnPrev);
        btnNext = (ImageButton) findViewById(R.id.btnNext);

        imgCover = (ImageView) findViewById(R.id.imgCover);
        tvTitle = (TextView) findViewById(R.id.tvTitle);
        tvArtistAlbum = (TextView) findViewById(R.id.tvArtistAlbum);

        tvElapsedTime = (TextView) findViewById(R.id.tvElapsedTime);
        tvRemainingTime = (TextView) findViewById(R.id.tvRemainingTime);
        sbProgress = (SeekBar) findViewById(R.id.sbProgress);
    }

    private void initListeners() {
        btnOpen.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                BrowserDialog bd = new BrowserDialog();
                bd.setBrowseMode(BrowserDialog.MODE_OPEN_FILE)
                        .setExtensionFilter("mp3;wma;ogg;wav");

                String startPath = Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_MUSIC).getAbsolutePath();
                if (sharedPrefs.contains(PREF_LASTSONG)) {
                    startPath = new File(sharedPrefs.getString(PREF_LASTSONG, null)).getParent();
                }

                bd.setStartPath(startPath)
                        .setOnDialogResultListener(new BrowserDialog.OnDialogResultListener() {
                            @Override
                            public void onPositiveResult(String path) {
                                loadSong(path);
                            }

                            @Override
                            public void onNegativeResult() {
                                Toast.makeText(PlayerActivity.this, "ERROR: Couldn't open file.", Toast.LENGTH_SHORT).show();

                            }
                        });
                bd.show(getFragmentManager(), BrowserDialog.TAG);
            }
        });

        btnPlayPause.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                if (currentSong != null) {
                    final int status = currentSong.getStatus();
                    if (status == BASS.BASS_ACTIVE_PAUSED) {
                        currentSong.resume();
                        timerHandler.postDelayed(timerRunnable, 0);

                        btnPlayPause.setImageResource(R.drawable.player_pause);
                    } else if (status == BASS.BASS_ACTIVE_STOPPED) {
                        currentSong.play();
                        timerHandler.postDelayed(timerRunnable, 0);

                        btnPlayPause.setImageResource(R.drawable.player_pause);
                    }
                    if (status == BASS.BASS_ACTIVE_PLAYING) {
                        currentSong.pause();
                        timerHandler.removeCallbacks(timerRunnable);

                        btnPlayPause.setImageResource(R.drawable.player_play);
                    }
                }
            }
        });

        sbProgress.setOnSeekBarChangeListener(new SeekBar.OnSeekBarChangeListener() {
            @Override
            public void onProgressChanged(SeekBar seekBar, int i, boolean b) {
                //TODO
                if (b && currentSong != null && currentSong.getStatus() == BASS.BASS_ACTIVE_PLAYING) {
                    currentSong.seekSeconds(i);
                }
            }

            @Override
            public void onStartTrackingTouch(SeekBar seekBar) {
                //TODO:
                // show little time dialog like in walkman?
            }

            @Override
            public void onStopTrackingTouch(SeekBar seekBar) {
            }
        });
    }

    private void loadSong(String path) {
        if (currentSong != null) {
            currentSong.stop();
            timerHandler.removeCallbacks(timerRunnable);
            btnPlayPause.setImageResource(R.drawable.player_play);
        }
        if (path != null) {
            try {
                currentSong = new Song(path);

                imgCover.setImageBitmap(currentSong.getCover());
                tvTitle.setText(currentSong.getTitle());
                tvArtistAlbum.setText(String.format("%s - %s", currentSong.getArtist(), currentSong.getAlbum()));
                sbProgress.setMax((int) currentSong.getTotalTimeSeconds());

                editor.putString(PREF_LASTSONG, path);
                editor.apply();
            } catch (TagException e) {
                Log.w(TAG, e);
            } catch (ReadOnlyFileException e) {
                Log.w(TAG, e);
            } catch (CannotReadException e) {
                Log.w(TAG, e);
            } catch (InvalidAudioFrameException e) {
                Log.w(TAG, e);
            } catch (IOException e) {
                Log.w(TAG, e);
            }
        }
    }
}