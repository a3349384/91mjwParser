package cn.zmy.mjwparser.widget.video;

import android.net.Uri;
import android.view.Surface;

import java.util.Map;

/**
 * Created by zmy on 2016/8/19 0019.
 */
public interface IVideoPlayer
{
    void reset();

    void setSurface(Surface surface);

    void setUri(Uri uri, Map<String, String> header);

    void prepare();

    void play();

    void seekTo(long time);

    void pause();

    void stop();

    void release();

    boolean isPlaying();

    long getDuration();

    long getCurrentPosition();

    void setVideoEventsListener(OnVideoEventsListener videoEventsListener);

    interface OnErrorListener {
        boolean onError(IVideoPlayer player, int var2, int var3);
    }

    interface OnVideoSizeChangedListener {
        void onVideoSizeChanged(IVideoPlayer player, int var2, int var3, int var4, int var5);
    }

    interface OnSeekCompleteListener {
        void onSeekComplete(IVideoPlayer player);
    }

    interface OnBufferingUpdateListener {
        void onBufferingUpdate(IVideoPlayer player, int var2);
    }

    interface OnCompletionListener {
        void onCompletion(IVideoPlayer player);
    }

    interface OnPreparedListener {
        void onPrepared(IVideoPlayer player);
    }
}
