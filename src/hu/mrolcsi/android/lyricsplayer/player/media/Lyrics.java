package hu.mrolcsi.android.lyricsplayer.player.media;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * Created with IntelliJ IDEA.
 * User: Matusinka Roland
 * Date: 2014.04.21.
 * Time: 23:48
 */

public class Lyrics {

    public static final String LRC_CACHE_DIR = "Lyrics";
    public static final String LRC_EXTENSION = ".lrc";
    private static final String LRC_LINE_REGEXP = "\\[[0-9]{2}:[0-9]{2}\\.[0-9]{2}].*";
    private final ArrayList<String> lrcLines;
    private List<LyricLine> lyrics;
    private double offset;

    public Lyrics(String lrc) {
        //separate lines
        String[] split = lrc.replace("[", "\n[").split("\n");
        split = Arrays.copyOfRange(split, 1, split.length - 1);

        //get offset tag if present
        Pattern offsetPattern = Pattern.compile("\\[offset: ([-0-9]+)].*");

        //get lines with valid tag
        lrcLines = new ArrayList<String>();
        Pattern linePattern = Pattern.compile(LRC_LINE_REGEXP);
        for (String line : split) {
            Matcher timeMatcher = linePattern.matcher(line);

            if (timeMatcher.matches()) {
                lrcLines.add(line);
            }

            Matcher offsetMatcher = offsetPattern.matcher(line);
            if (offsetMatcher.matches()) {
                //get offset (ms)
                offset = Double.parseDouble(offsetMatcher.group(1)) / 1000d;
            }
        }

        buildLyrics();
    }

    private static double getSecondsFromTag(String tag) {
        //[00:00.00]asdasdasd
        double seconds = 0;
        seconds += Integer.parseInt(tag.substring(1, 3)) * 60;
        seconds += Integer.parseInt(tag.substring(4, 6));
        seconds += Integer.parseInt(tag.substring(7, 9)) / 100d;
        return seconds;
    }

    private void buildLyrics() {
        lyrics = new ArrayList<LyricLine>();
        for (String line : lrcLines) {
            final double secondsFromTag = getSecondsFromTag(line.substring(0, 10));
            final String lyric = line.substring(10);

            lyrics.add(new LyricLine(secondsFromTag /*- offset*/, lyric));  //ignore offset for now...
        }
        Collections.sort(lyrics, LyricLine.COMPARATOR);
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

