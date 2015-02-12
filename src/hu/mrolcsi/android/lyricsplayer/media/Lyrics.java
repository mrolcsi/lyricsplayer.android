package hu.mrolcsi.android.lyricsplayer.media;

import android.util.SparseArray;

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
    //private static final String LRC_LINE_REGEXP = "\\[([0-9]{2}:[0-9]{2}\\.[0-9]{2})](.*)";
    private static final String LRC_LINE_REGEXP = "\\[([0-9\\.:]+)](.*)";
    private static final String LRC_TIME_LONG_REGEXP = "^([0-9]{2}):([0-9]{2})\\.([0-9]{2})$";
    private static final String LRC_TIME_SHORT_REGEXP = "^([0-9]{2}):([0-9]{2})$";
    private List<LyricLine> lyricsList;
    private SparseArray<String> lyricsSparseArray;
    private double offset;

    public Lyrics(String lrc) {
        //separate lines
        String[] split = lrc.replace("[", "\n[").split("\n");
        split = Arrays.copyOfRange(split, 1, split.length - 1);

        //get offset tag if present
        Pattern offsetPattern = Pattern.compile("\\[offset: ([-0-9]+)].*");

        //get lines with valid tag
        lyricsList = new ArrayList<LyricLine>();
        lyricsSparseArray = new SparseArray<String>();

        Pattern linePattern = Pattern.compile(LRC_LINE_REGEXP);
        for (String line : split) {
            Matcher lineMatcher = linePattern.matcher(line);

            if (lineMatcher.matches()) {
                lyricsList.add(new LyricLine(getSecondsFromTag(lineMatcher.group(1)), lineMatcher.group(2)));
                lyricsSparseArray.put((int) (getSecondsFromTag(lineMatcher.group(1))), lineMatcher.group(2));
            }

            Matcher offsetMatcher = offsetPattern.matcher(line);
            if (offsetMatcher.matches()) {
                //get offset (ms)
                offset = Double.parseDouble(offsetMatcher.group(1)) / 1000d;
            }
        }
        Collections.sort(lyricsList, LyricLine.COMPARATOR);
    }

    private static double getSecondsFromTag(String tag) {
        double seconds = 0;
        if (tag.length() == 8) {
            //00:00.00
            seconds += Integer.parseInt(tag.substring(0, 2)) * 60;
            seconds += Integer.parseInt(tag.substring(3, 5));
            seconds += Integer.parseInt(tag.substring(6, 8)) / 100d;
            return seconds;
        } else if (tag.length() == 5) {
            //00:00
            seconds += Integer.parseInt(tag.substring(0, 2)) * 60;
            seconds += Integer.parseInt(tag.substring(3, 5));
            return seconds;
        } else throw new IllegalArgumentException("Not a valid time tag.");

    }

    public List<LyricLine> getLineList() {
        return lyricsList;
    }

    public SparseArray<String> getLyricsSparseArray() {
        return lyricsSparseArray;
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

