package hu.mrolcsi.android.lyricsplayer.player;

import android.app.Fragment;
import android.content.SharedPreferences;
import android.os.Handler;
import android.util.SparseArray;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.SeekBar;
import android.widget.TextView;
import com.un4seen.bass.BASS;
import hu.mrolcsi.android.lyricsplayer.R;
import hu.mrolcsi.android.lyricsplayer.media.Song;

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
}
