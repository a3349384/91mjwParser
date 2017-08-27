package cn.zmy.mjwparser.widget.video;

/**
 * Created by zmy on 2016/8/20.
 */
public interface OnVideoEventsListener
{
    void onPrepared();

    void onPlay();

    void onPause();

    void onCompeted();

    void onPlayProgressUpdated(long currentPosition);

    void onBufferingStart();

    void onBufferingEnd();

    void onBufferingUpdated(int percent);

    void onVideoSizeChanged(int width, int height);
}
