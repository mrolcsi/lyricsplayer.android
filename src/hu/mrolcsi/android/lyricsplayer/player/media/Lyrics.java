package hu.mrolcsi.android.lyricsplayer.player.media;

import java.util.ArrayList;
import java.util.Arrays;
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

    private String LRC_TAG_REGEXP = "\\[[0-9]{2}:[0-9]{2}\\.[0-9]{2}]";

    public Lyrics(String lrc) {
        //separate lines
        String[] split = lrc.replace("[", "\n[").split("\n");
        split = Arrays.copyOfRange(split, 1, split.length - 1);

        //get lines with valid tag
        List<String> lrcLines = new ArrayList<String>();
        for (String line : split) {
            Pattern pattern = Pattern.compile(LRC_TAG_REGEXP);

            Matcher matcher = pattern.matcher(line);

            while (matcher.find()) {
                lrcLines.add(line);
            }
        }
    }
}
