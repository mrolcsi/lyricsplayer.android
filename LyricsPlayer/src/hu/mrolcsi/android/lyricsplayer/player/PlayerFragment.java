package hu.mrolcsi.android.lyricsplayer.player;

import android.app.Activity;
import android.app.AlertDialog;
import android.app.Fragment;
import android.content.DialogInterface;
import android.content.SharedPreferences;
import android.content.res.Configuration;
import android.graphics.Bitmap;
import android.graphics.Color;
import android.os.Bundle;
import android.os.Environment;
import android.os.Handler;
import android.preference.PreferenceManager;
import android.support.annotation.Nullable;
import android.util.Log;
import android.util.SparseArray;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.SeekBar;
import android.widget.TextView;
import com.un4seen.bass.BASS;
import hu.mrolcsi.android.filebrowser.BrowserDialog;
import hu.mrolcsi.android.lyricsplayer.MainActivity;
import hu.mrolcsi.android.lyricsplayer.R;
import hu.mrolcsi.android.lyricsplayer.editor.EditorFragment;
import hu.mrolcsi.android.lyricsplayer.media.Lyrics;
import hu.mrolcsi.android.lyricsplayer.media.OnLineReached;
import hu.mrolcsi.android.lyricsplayer.media.Song;
import hu.mrolcsi.android.lyricsplayer.net.LyricsDownloaderTask;
import org.jaudiotagger.audio.exceptions.CannotReadException;
import org.jaudiotagger.audio.exceptions.InvalidAudioFrameException;
import org.jaudiotagger.audio.exceptions.ReadOnlyFileException;
import org.jaudiotagger.tag.TagException;

import java.io.*;

/**
 * Created with IntelliJ IDEA.
 * User: Matusinka Roland
 * Date: 2015.02.27.
 * Time: 13:27
 */

public class PlayerFragment extends Fragment {

    public static final String CURRENT_SONG = "LyricsPlayer.currentSong";
    private static final String TAG = "LyricsPlayer.Player";
    private static final String PREF_LASTSONG = "LyricsPlayer.lastSong";
    Handler timerHandler = new Handler();
    private Song currentSong;
    private SharedPreferences sharedPrefs;
    private SparseArray<String> lyricsSparseArray;
    private TextView tvTopLine;
    private TextView tvMiddleLine;
    private TextView tvBottomLine;
    private ImageButton btnEditLyrics;
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
    private BASS.SYNCPROC onSongEnd = new BASS.SYNCPROC() {
        @Override
        public void SYNCPROC(int handle, int channel, int data, Object user) {
            currentSong.stop();
            timerHandler.removeCallbacks(timerRunnable);

            getView().post(new Runnable() {
                public void run() {
                    btnPlayPause.setImageResource(R.drawable.player_play);
                    sbProgress.setProgress(0);
                    tvElapsedTime.setText(currentSong.getElapsedTimeString());
                    tvRemainingTime.setText(currentSong.getRemainingTimeString());

                    getActivity().getWindow().clearFlags(android.view.WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON);
                }
            });
        }
    };

    //region L I F E C Y C L E


    @Override
    public void onAttach(Activity activity) {
        super.onAttach(activity);
        setHasOptionsMenu(true);
    }

    @Override
    public void onConfigurationChanged(Configuration newConfig) {
        super.onConfigurationChanged(newConfig);
        // TODO: implement method
    }

    @Override
    public void onStart() {
        super.onStart();

        if (sharedPrefs.contains(PREF_LASTSONG)) {
            loadSong(sharedPrefs.getString(PREF_LASTSONG, null));
        }
    }

    @Override
    public void onResume() {
        super.onResume();
        //TODO
    }

    @Override
    public void onPause() {
        super.onPause();
        //TODO
    }

    @Override
    public void onStop() {
        super.onStop();
        if (currentSong != null)
            currentSong.stop();
    }

    //endregion

    @Nullable
    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        sharedPrefs = PreferenceManager.getDefaultSharedPreferences(getActivity());

        return inflater.inflate(R.layout.player_main, container, false);
    }

    @Override
    public void onViewCreated(View view, Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        btnOpen = (ImageButton) view.findViewById(R.id.btnOpen);
        btnEditLyrics = (ImageButton) view.findViewById(R.id.btnEditLyrics);

        btnPlayPause = (ImageButton) view.findViewById(R.id.btnPlayPause);
        btnPrev = (ImageButton) view.findViewById(R.id.btnPrev);
        btnNext = (ImageButton) view.findViewById(R.id.btnNext);

        imgCover = (ImageView) view.findViewById(R.id.imgCover);

        tvTitle = (TextView) view.findViewById(R.id.tvTitle);
        tvArtistAlbum = (TextView) view.findViewById(R.id.tvArtistAlbum);

        tvElapsedTime = (TextView) view.findViewById(R.id.tvElapsedTime);
        tvRemainingTime = (TextView) view.findViewById(R.id.tvRemainingTime);
        sbProgress = (SeekBar) view.findViewById(R.id.sbProgress);

        tvTopLine = (TextView) view.findViewById(R.id.tvTopLine);
        tvMiddleLine = (TextView) view.findViewById(R.id.tvMiddleLine);
        tvBottomLine = (TextView) view.findViewById(R.id.tvBottomLine);

        initListeners();
    }

    private void initListeners() {
        btnOpen.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                BrowserDialog bd = new BrowserDialog();
                bd.setBrowseMode(BrowserDialog.MODE_OPEN_FILE)
                        .setExtensionFilter("mp3;wma;ogg;wav;aac");

                String startPath = Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_MUSIC).getAbsolutePath();
                bd.setStartPath(startPath);
                if (sharedPrefs.contains(PREF_LASTSONG)) {
                    bd.setCurrentPath(new File(sharedPrefs.getString(PREF_LASTSONG, null)).getParent());
                }

                bd.setStartPath(startPath);
                bd.setOnDialogResultListener(new BrowserDialog.OnDialogResultListener() {
                    @Override
                    public void onPositiveResult(String path) {
                        loadSong(path);
                    }

                    @Override
                    public void onNegativeResult() {
                    }
                });
                bd.setStartIsRoot(false);
                bd.show(getChildFragmentManager(), BrowserDialog.TAG);
            }
        });

        btnPlayPause.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                if (currentSong != null) {
                    final int status = currentSong.getStatus();
                    if (status == BASS.BASS_ACTIVE_PAUSED) {
                        currentSong.resume(sbProgress.getProgress());
                        timerHandler.postDelayed(timerRunnable, 0);

                        btnPlayPause.setImageResource(R.drawable.player_pause);

                        getActivity().getWindow().addFlags(android.view.WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON);
                    } else if (status == BASS.BASS_ACTIVE_STOPPED) {
                        currentSong.play();
                        timerHandler.postDelayed(timerRunnable, 0);

                        btnPlayPause.setImageResource(R.drawable.player_pause);

                        getActivity().getWindow().addFlags(android.view.WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON);
                    }
                    if (status == BASS.BASS_ACTIVE_PLAYING) {
                        currentSong.pause();
                        timerHandler.removeCallbacks(timerRunnable);

                        btnPlayPause.setImageResource(R.drawable.player_play);

                        getActivity().getWindow().clearFlags(android.view.WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON);
                    }
                }
            }
        });

        sbProgress.setOnSeekBarChangeListener(new SeekBarChangeListener());

        btnEditLyrics.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                if (currentSong.getStatus() != BASS.BASS_ACTIVE_PLAYING) {
                    currentSong.pause();
                    timerHandler.removeCallbacks(timerRunnable);
                    btnPlayPause.setImageResource(R.drawable.player_play);

                    openEditor();
                } else {
                    AlertDialog.Builder builder = new AlertDialog.Builder(getActivity());
                    builder.setTitle(R.string.player_alert_songisplaying_title)
                            .setMessage(R.string.player_alert_songisplaying_message)
                            .setPositiveButton(android.R.string.yes, new DialogInterface.OnClickListener() {
                                @Override
                                public void onClick(DialogInterface dialogInterface, int i) {
                                    currentSong.pause();
                                    timerHandler.removeCallbacks(timerRunnable);
                                    btnPlayPause.setImageResource(R.drawable.player_play);

                                    openEditor();
                                }
                            })
                            .setNegativeButton(android.R.string.no, new DialogInterface.OnClickListener() {
                                @Override
                                public void onClick(DialogInterface dialogInterface, int i) {
                                    dialogInterface.dismiss();
                                }
                            });
                    final AlertDialog dialog = builder.create();
                    dialog.show();
                }
            }
        });
    }

    public void openEditor() {
        Bundle args = new Bundle();
        args.putString(CURRENT_SONG, currentSong.getPath());

        Fragment editorFragment = new EditorFragment();
        editorFragment.setArguments(args);
        ((MainActivity) getActivity()).swapFragment(editorFragment);
    }

    private void loadSong(String path) {
        if (currentSong != null) {
            currentSong.stop();
            timerHandler.removeCallbacks(timerRunnable);
            btnPlayPause.setImageResource(R.drawable.player_play);
            sbProgress.setProgress(0);
        }
        if (path != null) try {
            currentSong = new Song(path, onSongEnd);

            final Bitmap cover = currentSong.getCover();
            if (cover != null) {
                imgCover.setImageBitmap(cover);

//                Palette.generateAsync(cover, new Palette.PaletteAsyncListener() {
//                    @Override
//                    public void onGenerated(Palette palette) {
//                        final int color = palette.getLightVibrantColor(Color.WHITE);
//                        getActivity().getWindow().getDecorView().setBackgroundColor(color);
//                    }
//                });
            } else {
                imgCover.setImageResource(R.drawable.player_nocover);
                getActivity().getWindow().getDecorView().setBackgroundColor(Color.WHITE);
            }

            tvTitle.setText(currentSong.getTitle());
            tvArtistAlbum.setText(String.format("%s - %s", currentSong.getArtist(), currentSong.getAlbum()));
            sbProgress.setMax((int) currentSong.getTotalTimeSeconds());

            tvElapsedTime.setText(currentSong.getElapsedTimeString());
            tvRemainingTime.setText(currentSong.getRemainingTimeString());

            sharedPrefs.edit().putString(PREF_LASTSONG, path).apply();

            //create lyrics dir
            File lyricsDir = new File(Environment.getExternalStorageDirectory().getPath() + File.separator + Lyrics.LRC_CACHE_DIR);
            if (!lyricsDir.exists()) lyricsDir.mkdirs();

            //try load lyrics from cache
            File lrcFile = new File(currentSong.getLRCPath());
            if (lrcFile.exists()) {
                loadLRCFromCache();
            } else {
                loadLRCFromNet();
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
        } catch (NullPointerException e) {
            //no cover

        }
    }

    private void loadLRCFromNet() {
        //download lyrics
        new LyricsDownloaderTask(getActivity()) {
            @Override
            protected void onPreExecute() {
                super.onPreExecute();

                tvTopLine.setText(R.string.player_pleasewait);
                tvMiddleLine.setText(R.string.player_fetchinglyrics);
            }

            @Override
            protected void onPostExecute(final Lyrics lyrics) {
                super.onPostExecute(lyrics);

                lrcLoaded(lyrics);
            }

            @Override
            protected void onProgressUpdate(final String... values) {
                super.onProgressUpdate(values);
                if (values[0] != null) tvTopLine.setText(values[0]);
                if (values[1] != null) tvMiddleLine.setText(values[1]);
                if (values[2] != null) tvBottomLine.setText(values[2]);
            }
        }.execute(currentSong.getArtist(), currentSong.getTitle(), currentSong.getLRCPath());
    }

    private void loadLRCFromCache() {
        try {
            String lrc = "";
            //load string from file
            InputStream inputStream = new FileInputStream(currentSong.getLRCPath());
            InputStreamReader inputStreamReader = new InputStreamReader(inputStream);
            BufferedReader bufferedReader = new BufferedReader(inputStreamReader);
            String receiveString = "";
            StringBuilder stringBuilder = new StringBuilder();

            while ((receiveString = bufferedReader.readLine()) != null) {
                stringBuilder.append(receiveString);
            }

            inputStream.close();
            lrc = stringBuilder.toString();

            lrcLoaded(new Lyrics(lrc));
        } catch (FileNotFoundException e) {
            Log.e(TAG, "File not found: " + e.toString());
        } catch (IOException e) {
            Log.e(TAG, "Can not read file: " + e.toString());
        }
    }

    private void lrcLoaded(Lyrics lyrics) {
        tvTopLine.setText(R.string.player_success);
        tvMiddleLine.setText(R.string.player_lyricsdownloaded);
        tvBottomLine.setText(R.string.player_enjoy);

        OnLineReached onLineReached = new OnLineReached() {
            @Override
            public void onLyricsReached(final String currentLine, final String previousLine, final String nextLine) {
                getView().post(new Runnable() {
                    @Override
                    public void run() {
                        tvTopLine.setText(previousLine);
                        tvMiddleLine.setText(currentLine);
                        tvBottomLine.setText(nextLine);
                    }
                });

            }
        };
        currentSong.setLyrics(lyrics, onLineReached);
        lyricsSparseArray = lyrics.getLyricsSparseArray();
    }

    private class SeekBarChangeListener implements SeekBar.OnSeekBarChangeListener {

        private int currentLineIndex = 0;

        @Override
        public void onProgressChanged(SeekBar seekBar, int i, boolean b) {
            if (b) {
                if (currentSong != null) {
                    if (currentSong.getStatus() == BASS.BASS_ACTIVE_PLAYING) {
                        currentSong.seekSeconds(i);
                    } else {
                        tvElapsedTime.setText(Song.getTimeString(i));
                        tvRemainingTime.setText("-" + Song.getTimeString(seekBar.getMax() - i));
                    }

                    currentLineIndex = lyricsSparseArray.indexOfKey(i);
                    if (currentLineIndex > 0) {
                        tvTopLine.setText(lyricsSparseArray.valueAt(currentLineIndex - 1));
                        tvMiddleLine.setText(lyricsSparseArray.valueAt(currentLineIndex));
                        tvBottomLine.setText(lyricsSparseArray.valueAt(currentLineIndex + 1));
                    }
                }
            }
        }

        @Override
        public void onStartTrackingTouch(SeekBar seekBar) {
            // TODO: show little time dialog like in walkman?
        }

        @Override
        public void onStopTrackingTouch(SeekBar seekBar) {
        }
    }

}
