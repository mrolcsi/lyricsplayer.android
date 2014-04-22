package hu.mrolcsi.android.lyricsplayer.player.media;

import android.util.Log;

import java.util.*;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * Created with IntelliJ IDEA.
 * User: Matusinka Roland
 * Date: 2014.04.21.
 * Time: 23:48
 */

public class Lyrics {

    private static final String LRC_LINE_REGEXP = "\\[[0-9]{2}:[0-9]{2}\\.[0-9]{2}].*";
    private List<LyricLine> lyrics;

    public Lyrics(String lrc) {
        //separate lines
        String[] split = lrc.replace("[", "\n[").split("\n");
        try {
            split = Arrays.copyOfRange(split, 1, split.length - 1);
        } catch (Exception e) {
            Log.w("LyricsPlayer.Lyrics", e);
            //FIXME
        }

        //get lines with valid tag
        List<String> lrcLines = new ArrayList<String>();
        Pattern linePattern = Pattern.compile(LRC_LINE_REGEXP);
        for (String line : split) {

            Matcher matcher = linePattern.matcher(line);

            if (matcher.matches()) {
                lrcLines.add(line);
            }
        }

        lyrics = new ArrayList<LyricLine>();
        for (String line : lrcLines) {
            final double secondsFromTag = getSecondsFromTag(line.substring(0, 10));
            final String lyric = line.substring(10);

            lyrics.add(new LyricLine(secondsFromTag, lyric));
        }
        Collections.sort(lyrics, LyricLine.COMPARATOR);
    }

    private static double getSecondsFromTag(String tag) {
        //[00:00.00]asdasdasd
        double seconds = 0;
        seconds += Integer.parseInt(tag.substring(1, 3)) * 60;
        seconds += Integer.parseInt(tag.substring(4, 6));
        seconds += Integer.parseInt(tag.substring(7, 9)) / 100d;
        return seconds;
    }

    public List<LyricLine> getAllLyrics() {
        return lyrics;
    }

    //    public String getCurrentLine() {
//        try {
//            return lyricsArray[currentLine];
//        } catch (ArrayIndexOutOfBoundsException e) {
//            return "";
//        }
//    }
//
//    public String getPreviousLine() {
//        try {
//            return lyricsArray[currentLine - 1];
//        } catch (ArrayIndexOutOfBoundsException e) {
//            return "";
//        }
//    }
//
//    public String getNextLine() {
//        try {
//            return lyricsArray[currentLine + 1];
//        } catch (ArrayIndexOutOfBoundsException e) {
//            return "";
//        }
//    }
}

class LyricLine {

    public static final Comparator<LyricLine> COMPARATOR = new Comparator<LyricLine>() {
        @Override
        public int compare(LyricLine lyricLine, LyricLine lyricLine2) {
            return (int) (lyricLine.time - lyricLine2.time);
        }
    };
    double time;
    String lyric;

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
}