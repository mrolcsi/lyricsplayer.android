package hu.mrolcsi.android.lyricsplayer.net;

import android.content.Context;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.net.Uri;
import android.os.AsyncTask;
import android.util.Log;
import hu.mrolcsi.android.lyricsplayer.R;
import hu.mrolcsi.android.lyricsplayer.media.Lyrics;
import org.apache.http.HttpEntity;
import org.apache.http.HttpResponse;
import org.apache.http.client.ClientProtocolException;
import org.apache.http.client.HttpClient;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.impl.client.DefaultHttpClient;

import java.io.*;
import java.net.ConnectException;

/**
 * Created with IntelliJ IDEA.
 * User: Matusinka Roland
 * Date: 2014.04.21.
 * Time: 23:49
 */

public class LyricsDownloaderTask extends AsyncTask<String, String, Lyrics> {

    public static final String TAG = "LyricsPlayer.Downloader";
    private static final String BASE_URL = "http://users.atw.hu/mrolcsi/lrc/get.php";
    private final Context context;

    public LyricsDownloaderTask(Context context) {
        this.context = context;
    }

    @Override
    protected void onPreExecute() {
        super.onPreExecute();
    }

    @Override
    protected Lyrics doInBackground(String... strings) {

        //check network state
        ConnectivityManager conMgr = (ConnectivityManager) context.getSystemService(Context.CONNECTIVITY_SERVICE);

        if (conMgr.getNetworkInfo(ConnectivityManager.TYPE_WIFI).getState() == NetworkInfo.State.CONNECTED
                || conMgr.getNetworkInfo(ConnectivityManager.TYPE_MOBILE).getState() == NetworkInfo.State.CONNECTED) {

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

                            if (result.startsWith("<html>")) {
                                //not the actual lyrics, something else
                                strings[2] += ".bad";
                                writeToFile(result, strings[2]);
                                publishProgress(context.getString(R.string.downloader_error), null, context.getString(R.string.downloader_error_nolyricsfound));
                                cancel(true);
                            }

                            writeToFile(result, strings[2]);
                            publishProgress(null, null, context.getString(R.string.player_downloadinglyrics));
                            return new Lyrics(result);
                        }
                        break;
                    case 404:
                        //no lyrics
                        publishProgress(context.getString(R.string.downloader_error), null, context.getString(R.string.downloader_error_nolyricsfound));
                        cancel(true);
                    default:
                        break;
                }
            } catch (ClientProtocolException e) {
                Log.w(TAG, e);
            } catch (ConnectException e) {
                publishProgress(context.getString(R.string.downloader_error), context.getString(R.string.downloader_error_couldntconnect), context.getString(R.string.downloader_error_tryagainlater));
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

    private void writeToFile(String data, String filename) {
        try {
            OutputStreamWriter outputStreamWriter = new OutputStreamWriter(new FileOutputStream(filename));
            outputStreamWriter.write(data);
            outputStreamWriter.close();
        } catch (IOException e) {
            Log.e(TAG, "File write failed: " + e.toString());
        }
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

        String progress = "";
        String line;
        try {
            while ((line = reader.readLine()) != null) {
                sb.append(line).append("\n");

                progress += ".";
                publishProgress(null, null, progress);
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
