package hu.mrolcsi.android.lyricsplayer.editor;

import android.app.Activity;
import android.app.AlertDialog;
import android.content.DialogInterface;
import android.os.Bundle;
import android.os.Handler;
import android.text.Html;
import android.text.Spanned;
import android.util.Log;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.widget.*;
import com.un4seen.bass.BASS;
import hu.mrolcsi.android.lyricsplayer.R;
import hu.mrolcsi.android.lyricsplayer.media.LyricLine;
import hu.mrolcsi.android.lyricsplayer.media.Song;
import hu.mrolcsi.android.lyricsplayer.player.PlayerActivity;
import org.jaudiotagger.audio.exceptions.CannotReadException;
import org.jaudiotagger.audio.exceptions.InvalidAudioFrameException;
import org.jaudiotagger.audio.exceptions.ReadOnlyFileException;
import org.jaudiotagger.tag.TagException;

import java.io.File;
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
    private BASS.SYNCPROC onSongEnd = new BASS.SYNCPROC() {
        @Override
        public void SYNCPROC(int handle, int channel, int data, Object user) {
            timerHandler.removeCallbacks(timerRunnable);
            tvTime.setText(String.format(getString(R.string.editor_time_format), getString(R.string.player_starttime), currentSong.getTotalTimeString()));
            runOnUiThread(new Runnable() {
                public void run() {
                    btnPlayPause.setImageResource(R.drawable.player_play);
                    sbProgress.setProgress(0);

                    getWindow().clearFlags(android.view.WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON);
                }
            });
        }
    };


    private Song currentSong;
    private ListView lvLyrics;
    private ImageButton btnPlayPause;
    private TextView tvTitle;
    private SeekBar sbProgress;
    private TextView tvTime;
    private TextView tvNoLyrics;

    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.editor_main);

        initViews();
        initListeners();

        //get song to edit
        String path;
        if (getIntent().hasExtra(PlayerActivity.CURRENT_SONG)) {
            path = getIntent().getStringExtra(PlayerActivity.CURRENT_SONG);
            loadSong(path);
        }
    }

    private void initViews() {
        lvLyrics = (ListView) findViewById(R.id.lvLyrics);
        btnPlayPause = (ImageButton) findViewById(R.id.btnPlayPause);
        tvTitle = (TextView) findViewById(R.id.tvTitle);
        sbProgress = (SeekBar) findViewById(R.id.sbProgress);
        tvTime = (TextView) findViewById(R.id.tvTime);
        tvNoLyrics = (TextView) findViewById(R.id.tvNoLyrics);
    }

    private void initListeners() {
        btnPlayPause.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                if (currentSong != null) {
                    final int status = currentSong.getStatus();
                    if (status == BASS.BASS_ACTIVE_PAUSED) {
                        currentSong.resume(sbProgress.getProgress());
                        timerHandler.postDelayed(timerRunnable, 0);

                        btnPlayPause.setImageResource(R.drawable.player_pause);
                        getWindow().addFlags(android.view.WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON);
                    } else if (status == BASS.BASS_ACTIVE_STOPPED) {
                        currentSong.play();
                        timerHandler.postDelayed(timerRunnable, 0);

                        btnPlayPause.setImageResource(R.drawable.player_pause);
                        getWindow().addFlags(android.view.WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON);
                    }
                    if (status == BASS.BASS_ACTIVE_PLAYING) {
                        currentSong.pause();
                        timerHandler.removeCallbacks(timerRunnable);

                        btnPlayPause.setImageResource(R.drawable.player_play);
                        getWindow().clearFlags(android.view.WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON);
                    }
                }
            }
        });

        sbProgress.setOnSeekBarChangeListener(new SeekBar.OnSeekBarChangeListener() {
            @Override
            public void onProgressChanged(SeekBar seekBar, int i, boolean b) {
                if (b && currentSong != null && currentSong.getStatus() == BASS.BASS_ACTIVE_PLAYING) {
                    currentSong.seekSeconds(i);
                }
            }

            @Override
            public void onStartTrackingTouch(SeekBar seekBar) {
            }

            @Override
            public void onStopTrackingTouch(SeekBar seekBar) {
            }
        });

        lvLyrics.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> adapterView, View view, int i, long l) {
                //get current time
                //set current time on current line
                //refresh views

                final LyricLine item = (LyricLine) adapterView.getItemAtPosition(i);
                item.setTime(currentSong.getElapsedTimeSeconds());
                ((LRCAdapter) adapterView.getAdapter()).notifyDataSetChanged();
                //adapterView.invalidate();
            }
        });

        lvLyrics.setOnItemLongClickListener(new AdapterView.OnItemLongClickListener() {
            @Override
            public boolean onItemLongClick(final AdapterView<?> adapterView, View view, int i, long l) {
                final LRCAdapter adapter = (LRCAdapter) adapterView.getAdapter();
                final LyricLine item = (LyricLine) adapterView.getItemAtPosition(i);

                final View lineEditorView = View.inflate(EditorActivity.this, R.layout.editor_lineditordialog, null);
                final EditText etLine = (EditText) lineEditorView.findViewById(R.id.etLine);
                etLine.setText(item.getLyric());

                AlertDialog.Builder builder = new AlertDialog.Builder(EditorActivity.this);
                builder.setView(lineEditorView)
                        .setIcon(R.drawable.editor_edit)
                        .setTitle("Edit line " + i)
                        .setNegativeButton(android.R.string.cancel, new DialogInterface.OnClickListener() {
                            @Override
                            public void onClick(DialogInterface dialogInterface, int i) {
                                dialogInterface.dismiss();
                            }
                        })
                        .setNeutralButton("Delete", new DialogInterface.OnClickListener() {
                            @Override
                            public void onClick(DialogInterface dialogInterface, int i) {
                                adapter.remove(item);
                                adapter.notifyDataSetChanged();
                                //adapterView.invalidate();
                            }
                        })
                        .setPositiveButton("Save", new DialogInterface.OnClickListener() {
                            @Override
                            public void onClick(DialogInterface dialogInterface, int i) {
                                item.setLyric(etLine.getText().toString());
                                adapter.notifyDataSetChanged();
                                //adapterView.invalidate();
                            }
                        });
                final AlertDialog dialog = builder.create();
                dialog.show();
                return true;
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
                saveLRC();
                return true;
            default:
                return super.onOptionsItemSelected(item);
        }
    }

    private void saveLRC() {
        Toast.makeText(this, "Not yet.", Toast.LENGTH_SHORT).show();
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

            final Spanned title = Html.fromHtml(String.format(getString(R.string.editor_title_format), currentSong.getTitle(), currentSong.getArtist()));
            tvTitle.setText(title);
            sbProgress.setMax((int) currentSong.getTotalTimeSeconds());
            tvTime.setText(String.format(getString(R.string.editor_time_format), getString(R.string.player_starttime), currentSong.getTotalTimeString()));

            if (new File(currentSong.getLRCPath()).exists()) {
                lvLyrics.setAdapter(new LRCAdapter(this, currentSong.getLRCPath()));
                tvNoLyrics.setVisibility(View.GONE);
                lvLyrics.setVisibility(View.VISIBLE);
            }

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