package hu.mrolcsi.android.lyricsplayer.media;

import android.util.Log;

import java.util.Comparator;

public class LyricLine {

    public static final Comparator<LyricLine> COMPARATOR = new Comparator<LyricLine>() {
        @Override
        public int compare(LyricLine lyricLine, LyricLine lyricLine2) {
            return (int) (lyricLine.getTime() - lyricLine2.getTime());
        }
    };

    private double time;
    private String lyric;

    LyricLine(double time, String lyric) {
        this.time = time;
        this.lyric = lyric;
    }

    @Override
    public String toString() {
        return "LyricLine{" +
                time +
                ", '" + lyric + '\'' +
                '}';
    }

    public double getTime() {
        return time;
    }

    public void setTime(double time) {
        this.time = time;
        Log.d("LyricsPlayer.LyricsLine", "Time set to = " + time);
    }

    public String getLyric() {
        return lyric;
    }

    public void setLyric(String lyric) {
        this.lyric = lyric;
    }

    public String getTimeTag() {
        int t = (int) (getTime() * 100);
        int minutes = (t / 6000);
        t -= minutes * 6000;
        int seconds = t / 100;
        t -= seconds * 100;
        return String.format("[%02d:%02d.%02d]", minutes, seconds, t);
    }
}
