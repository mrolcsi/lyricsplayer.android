package hu.mrolcsi.android.lyricsplayer.editor;

import android.content.Context;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.TextView;
import hu.mrolcsi.android.lyricsplayer.R;
import hu.mrolcsi.android.lyricsplayer.media.LyricLine;
import hu.mrolcsi.android.lyricsplayer.media.Lyrics;

import java.io.*;
import java.util.List;

/**
 * Created with IntelliJ IDEA.
 * User: Matusinka Roland
 * Date: 2014.04.30.
 * Time: 21:41
 */

public class LRCAdapter extends ArrayAdapter<LyricLine> {

    private static final String TAG = "LyricsPlayer.LRCEditor";

    private final LayoutInflater inflater;
    private List<LyricLine> lyrics;

    public LRCAdapter(Context context, String lrcPath) {
        super(context, R.layout.editor_listrow);
        inflater = LayoutInflater.from(context);

        //load lrc from file

        try {
            String lrcString = "";
            //load string from file
            InputStream inputStream = new FileInputStream(lrcPath);
            InputStreamReader inputStreamReader = new InputStreamReader(inputStream);
            BufferedReader bufferedReader = new BufferedReader(inputStreamReader);
            String receiveString = "";
            StringBuilder stringBuilder = new StringBuilder();

            while ((receiveString = bufferedReader.readLine()) != null) {
                stringBuilder.append(receiveString);
            }

            inputStream.close();
            lrcString = stringBuilder.toString();
            Lyrics lrc = new Lyrics(lrcString);
            lyrics = lrc.getAllLyrics();
        } catch (FileNotFoundException e) {
            Log.e(TAG, "File not found: " + e.toString());
        } catch (IOException e) {
            Log.e(TAG, "Can not read file: " + e.toString());
        }
    }

    @Override
    public void add(LyricLine object) {
        lyrics.add(object);
        notifyDataSetChanged();
    }

    @Override
    public void insert(LyricLine object, int index) {
        lyrics.add(index, object);
        notifyDataSetChanged();
    }

    @Override
    public void remove(LyricLine object) {
        lyrics.remove(object);
        notifyDataSetChanged();
    }

    @Override
    public int getCount() {
        return lyrics.size();
    }

    @Override
    public LyricLine getItem(int position) {
        return lyrics.get(position);
    }

    @Override
    public View getView(int position, View convertView, ViewGroup parent) {
        LyricsHolder holder;
        if (convertView == null) {
            convertView = inflater.inflate(R.layout.editor_listrow, parent, false);
            holder = new LyricsHolder();

            holder.tvTime = (TextView) convertView.findViewById(R.id.tvTime);
            holder.tvText = (TextView) convertView.findViewById(R.id.tvText);

            convertView.setTag(holder);
        } else holder = (LyricsHolder) convertView.getTag();

        final LyricLine item = getItem(position);

        holder.tvTime.setText(item.getTimeTag());
        holder.tvText.setText(item.getLyric());

        return convertView;
    }
}

class LyricsHolder {
    TextView tvTime;
    TextView tvText;
}
