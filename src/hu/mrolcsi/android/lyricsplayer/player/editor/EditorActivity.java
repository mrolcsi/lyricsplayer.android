package hu.mrolcsi.android.lyricsplayer.player.editor;

import android.app.Activity;
import android.os.Bundle;
import android.os.Handler;
import android.text.Html;
import android.text.Spanned;
import android.util.Log;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.widget.ImageButton;
import android.widget.ListView;
import android.widget.SeekBar;
import android.widget.TextView;
import com.un4seen.bass.BASS;
import hu.mrolcsi.android.lyricsplayer.R;
import hu.mrolcsi.android.lyricsplayer.player.media.Song;
import org.jaudiotagger.audio.exceptions.CannotReadException;
import org.jaudiotagger.audio.exceptions.InvalidAudioFrameException;
import org.jaudiotagger.audio.exceptions.ReadOnlyFileException;
import org.jaudiotagger.tag.TagException;

import java.io.IOException;

/**
 * Created with IntelliJ IDEA.
 * User: Matusinka Roland
 * Date: 2014.04.23.
 * Time: 14:36
 */

public class EditorActivity extends Activity {

    public static final String TAG = "LyricsPlayer.Editor";
    Handler timerHandler = new Handler();
    Runnable timerRunnable = new Runnable() {
        //To start handler, call:
        //timerHandler.postDelayed(timerRunnable, 0);

        //To stop handler, call:
        //timerHandler.removeCallbacks(timerRunnable);

        @Override
        public void run() {
            //do stuff
            tvTime.setText(String.format(getString(R.string.editor_time_format), currentSong.getElapsedTimeString(), currentSong.getTotalTimeString()));
            sbProgress.setProgress((int) currentSong.getElapsedTimeSeconds());

            //500ms interval
            timerHandler.postDelayed(this, 500);
        }
    };
    private Song currentSong;
    private ListView lvLyrics;
    private ImageButton btnPlayPause;
    private TextView tvTitle;
    private SeekBar sbProgress;
    private TextView tvTime;

    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.editor_main);

        //get song to edit
        String path = null;
        if (getIntent().hasExtra("currentSong")) {
            path = getIntent().getStringExtra("currentSong");
            loadSong(path);
        }

        initViews();
        initListeners();
    }

    private void initViews() {
        lvLyrics = (ListView) findViewById(R.id.lvLyrics);
        btnPlayPause = (ImageButton) findViewById(R.id.btnPlayPause);
        tvTitle = (TextView) findViewById(R.id.tvTitle);
        sbProgress = (SeekBar) findViewById(R.id.sbProgress);
        tvTime = (TextView) findViewById(R.id.tvTime);


    }

    private void initListeners() {
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
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        MenuInflater inflater = getMenuInflater();
        inflater.inflate(R.menu.menu_editor, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()) {
            case R.id.menuAddLine:
                //TODO
                return true;
            case R.id.menuSaveLyrics:
                //TODO
                return true;
            default:
                return super.onOptionsItemSelected(item);
        }
    }

    private void loadSong(String path) {
        try {
            currentSong = new Song(path, new BASS.SYNCPROC() {
                @Override
                public void SYNCPROC(int handle, int channel, int data, Object user) {
                    BASS.BASS_ChannelStop(channel);

                    runOnUiThread(new Runnable() {
                        @Override
                        public void run() {
                            sbProgress.setProgress(0);
                            tvTime.setText(String.format(getString(R.string.editor_time_format), getString(R.string.player_starttime), currentSong.getTotalTimeString()));
                            btnPlayPause.setImageResource(R.drawable.player_play);
                        }
                    });
                }
            });

            //lyricsDownloader
            //lvLyrics.setAdapter(new LyricsAdapter())


            final Spanned title = Html.fromHtml(String.format(getString(R.string.editor_title_format), currentSong.getArtist(), currentSong.getTitle()));
            tvTitle.setText(title);
            sbProgress.setMax((int) currentSong.getTotalTimeSeconds());
            tvTime.setText(String.format(getString(R.string.editor_time_format), getString(R.string.player_starttime), currentSong.getTotalTimeString()));

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