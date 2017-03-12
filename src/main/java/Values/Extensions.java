package Values;

/**
 * Created by Ryan on 3/11/2017.
 */
public class Extensions {
        /*
        JavaFX media support:
        https://docs.oracle.com/javafx/2/media/overview.htm
        MP3;
        AIFF containing uncompressed PCM;
        WAV containing uncompressed PCM;
        MPEG-4 multimedia container with Advanced Audio Coding (AAC) audio

        JaudioTagger media support:
        http://www.jthink.net/jaudiotagger/
        It currently fully supports Mp3, Mp4 (Mp4 audio, M4a and M4p audio)
        Ogg Vorbis, Flac and Wma, there is limited support for Wav and Real formats.
     */

    public final static String[] SUPPORTED = {
            ".mp3",".mp4",".wmv",".mpeg", ".aac",
            ".pcm", ".aif", ".aiff", ".flv", ".fxm",
            ".wav", ".m4a", ".m4v", ".m4p", ".m4r", ".3gp"
    };

}
