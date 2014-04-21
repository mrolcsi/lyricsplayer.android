package hu.mrolcsi.android.lyricsplayer.player.media;

import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import org.jaudiotagger.audio.AudioFile;
import org.jaudiotagger.audio.AudioFileIO;
import org.jaudiotagger.audio.exceptions.CannotReadException;
import org.jaudiotagger.audio.exceptions.InvalidAudioFrameException;
import org.jaudiotagger.audio.exceptions.ReadOnlyFileException;
import org.jaudiotagger.tag.FieldKey;
import org.jaudiotagger.tag.Tag;
import org.jaudiotagger.tag.TagException;

import java.io.File;
import java.io.IOException;

import static com.un4seen.bass.BASS.*;

/**
 * Created with IntelliJ IDEA.
 * User: Matusinka Roland
 * Date: 2014.04.21.
 * Time: 20:13
 */

public class Song {

    public static final String TIME_FORMAT = "%02d:%02d";
    private String filePath;

    private AudioFile audioFile;

    private int stream;
    private SYNCPROC syncProc;


    //region Constructor

    public Song(String path) throws TagException, ReadOnlyFileException, CannotReadException, InvalidAudioFrameException, IOException {
        this.filePath = path;
        this.stream = BASS_StreamCreateFile(filePath, 0, 0, 0);
        audioFile = AudioFileIO.read(new File(this.filePath));
    }

    //endregion

    //region Properties

    public double getTotalTimeSeconds() {
        return BASS_ChannelBytes2Seconds(this.stream, BASS_ChannelGetLength(this.stream, BASS_POS_BYTE));
    }

    public double getElapsedTimeSeconds() {
        return BASS_ChannelBytes2Seconds(this.stream, BASS_ChannelGetPosition(this.stream, BASS_POS_BYTE));
    }

    public double getRemainingTimeSeconds() {
        return this.getTotalTimeSeconds() - this.getElapsedTimeSeconds();
    }

    public long getTotalTimeBytes() {
        return BASS_ChannelGetLength(this.stream, BASS_POS_BYTE);
    }

    public long getElapsedTimeBytes() {
        return BASS_ChannelGetPosition(this.stream, BASS_POS_BYTE);
    }

    public long getRemainingTimeBytes() {
        return this.getTotalTimeBytes() - this.getElapsedTimeBytes();
    }

    public String getTotalTimeString() {
        int seconds = (int) this.getTotalTimeSeconds();
        int minutes = seconds / 60;
        seconds = seconds % 60;

        return String.format(TIME_FORMAT, minutes, seconds);
    }

    public String getElapsedTimeString() {
        int seconds = (int) this.getElapsedTimeSeconds();
        int minutes = seconds / 60;
        seconds = seconds % 60;

        return String.format(TIME_FORMAT, minutes, seconds);
    }

    public String getRemainingTimeString() {
        int seconds = (int) this.getRemainingTimeSeconds();
        int minutes = seconds / 60;
        seconds = seconds % 60;

        return String.format("-" + TIME_FORMAT, minutes, seconds);
    }

    public int getStatus() {
        return BASS_ChannelIsActive(this.stream);
    }

    public Tag getTag() {
        return audioFile.getTag();
    }

    public String getArtist() {
        return audioFile.getTag().getFirst(FieldKey.ARTIST);
    }

    public String getTitle() {
        return audioFile.getTag().getFirst(FieldKey.TITLE);
    }

    public String getAlbum() {
        return audioFile.getTag().getFirst(FieldKey.ALBUM);
    }

    public Bitmap getCover() throws IOException {
        final byte[] binaryData = audioFile.getTag().getFirstArtwork().getBinaryData();
        return BitmapFactory.decodeByteArray(binaryData, 0, binaryData.length);
    }

//endregion

//region Playback Control

    public void stop() {
        BASS_ChannelStop(this.stream);
        BASS_SampleFree(this.stream);
    }

    public void play() {
        stop();
        this.stream = BASS_StreamCreateFile(filePath, 0, 0, 0);
        //BASS_ChannelSetSync(stream, BASS_SYNC_END, 0, syncProc, this);
        BASS_ChannelPlay(stream, true);
    }

    public void pause() {
        BASS_ChannelPause(this.stream);
    }

    public void resume() {
        BASS_ChannelPlay(stream, false);
    }

    public void setVolume(float volume) {
        //min=0; max=1
        BASS_ChannelSetAttribute(stream, BASS_ATTRIB_VOL, volume);
    }

    public void seekSeconds(double seconds) {
        final long bytes = BASS_ChannelSeconds2Bytes(this.stream, seconds);
        BASS_ChannelSetPosition(stream, bytes, BASS_POS_BYTE);
    }

    public void SeekBytes(long bytes) {
        BASS_ChannelSetPosition(this.stream, bytes, BASS_POS_BYTE);
    }

    //endregion
}
