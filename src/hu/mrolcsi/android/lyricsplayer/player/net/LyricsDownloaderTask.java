package hu.mrolcsi.android.lyricsplayer.player.net;

import android.content.Context;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.net.Uri;
import android.os.AsyncTask;
import android.util.Log;
import hu.mrolcsi.android.lyricsplayer.player.media.Lyrics;
import org.apache.http.HttpEntity;
import org.apache.http.HttpResponse;
import org.apache.http.client.ClientProtocolException;
import org.apache.http.client.HttpClient;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.impl.client.DefaultHttpClient;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;

/**
 * Created with IntelliJ IDEA.
 * User: Matusinka Roland
 * Date: 2014.04.21.
 * Time: 23:49
 */

public class LyricsDownloaderTask extends AsyncTask<String, String, Lyrics> {

    public static final String TAG = "LyricsPlayer.Downloader";
    private static final String BASE_URL = "http://mrolcsi.orgfree.com/lrc/get.php";
    private final Context context;

    public LyricsDownloaderTask(Context context) {
        this.context = context;
    }

    @Override
    protected void onPreExecute() {
        super.onPreExecute();
        // TODO: implement method
    }

    @Override
    protected Lyrics doInBackground(String... strings) {
        //
        //TODO:
        // check network state
        // fetch lyrics
        // check if lyrics present
        // if empty > tell GUI
        //

        //check network state
        ConnectivityManager conMgr = (ConnectivityManager) context.getSystemService(Context.CONNECTIVITY_SERVICE);

        if (conMgr.getNetworkInfo(ConnectivityManager.TYPE_WIFI).getState() == NetworkInfo.State.CONNECTED
                || conMgr.getNetworkInfo(ConnectivityManager.TYPE_MOBILE).getState() == NetworkInfo.State.CONNECTING) {

            //notify user you are online
            HttpClient httpclient = new DefaultHttpClient();

            String uri = null;
            try {
                uri = Uri.parse(BASE_URL)
                        .buildUpon()
                        .appendQueryParameter("artist", strings[0])
                        .appendQueryParameter("title", strings[1])
                        .build().toString();
            } catch (NullPointerException e) {
                Log.w(TAG, "message", e);
            }

            // Prepare a request object
            HttpGet httpget = new HttpGet(uri);

            // Execute the request
            HttpResponse response;
            try {
                response = httpclient.execute(httpget);
                // Examine the response status
                switch (response.getStatusLine().getStatusCode()) {
                    case 200:
                        // Get hold of the response entity
                        HttpEntity entity = response.getEntity();
                        // If the response does not enclose an entity, there is no need
                        // to worry about connection release

                        if (entity != null) {

                            // A Simple JSON Response Read
                            InputStream inStream = entity.getContent();
                            String result = convertStreamToString(inStream);
                            // now you have the string representation of the HTML request
                            inStream.close();

                            Lyrics l = new Lyrics();
                            l.setRawLRC(result);
                            return l;
                        }
                        break;
                    default:
                        break;
                }
            } catch (ClientProtocolException e) {
                Log.w(TAG, e);
            } catch (IOException e) {
                Log.w(TAG, e);
            }

        } else if (conMgr.getNetworkInfo(ConnectivityManager.TYPE_WIFI).getState() == NetworkInfo.State.DISCONNECTED
                || conMgr.getNetworkInfo(ConnectivityManager.TYPE_MOBILE).getState() == NetworkInfo.State.DISCONNECTED) {
            //notify user you are not online
            cancel(true);
        }


        return null;
    }

    private String convertStreamToString(InputStream is) {
    /*
     * To convert the InputStream to String we use the BufferedReader.readLine()
     * method. We iterate until the BufferedReader return null which means
     * there's no more data to read. Each line will appended to a StringBuilder
     * and returned as String.
     */
        BufferedReader reader = new BufferedReader(new InputStreamReader(is));
        StringBuilder sb = new StringBuilder();

        int i = 0;
        String line;
        try {
            while ((line = reader.readLine()) != null) {
                sb.append(line).append("\n");

                switch (i % 3) {
                    case 0:
                        publishProgress(".");
                        break;
                    case 1:
                        publishProgress("..");
                        break;
                    case 2:
                        publishProgress("...");
                        break;
                }
                i++;
            }
        } catch (IOException e) {
            e.printStackTrace();
        } finally {
            try {
                is.close();
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
        return sb.toString();
    }
}
