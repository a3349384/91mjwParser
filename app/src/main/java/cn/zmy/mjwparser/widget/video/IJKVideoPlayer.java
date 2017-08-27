package cn.zmy.mjwparser.widget.video;

import android.content.Context;
import android.media.MediaPlayer;
import android.net.Uri;
import android.view.Surface;

import java.io.IOException;
import java.util.Map;
import java.util.concurrent.TimeUnit;

import rx.Observable;
import rx.Subscription;
import rx.android.schedulers.AndroidSchedulers;
import rx.internal.util.SubscriptionList;
import rx.schedulers.Schedulers;
import tv.danmaku.ijk.media.player.IjkMediaPlayer;

/**
 * Created by zmy on 2016/8/19 0019.
 * IJK Player 实现
 */
public class IJKVideoPlayer implements IVideoPlayer
{
    private Context context;
    private IjkMediaPlayer player;

    private Surface surface;
    private OnVideoEventsListener videoEventsListener;
    private SubscriptionList subscriptionList;

    public IJKVideoPlayer(Context context)
    {
        this.context = context;
        this.player = new IjkMediaPlayer();
        this.subscriptionList = new SubscriptionList();
    }

    @Override
    public void reset()
    {
        this.player.resetListeners();
        this.player.reset();
        this.player.setScreenOnWhilePlaying(true);
        this.player.setKeepInBackground(false);

        this.player.setOnPreparedListener(player ->
        {
            if (videoEventsListener != null)
            {
                videoEventsListener.onBufferingEnd();
                videoEventsListener.onPrepared();
            }
        });

        this.player.setOnBufferingUpdateListener((iMediaPlayer, i) ->
        {
            if (videoEventsListener != null)
            {
                videoEventsListener.onBufferingUpdated(i);
            }
        });

        this.player.setOnVideoSizeChangedListener((mp, width, height, sar_num, sar_den) ->
        {
            if (videoEventsListener != null)
            {
                videoEventsListener.onVideoSizeChanged(width, height);
            }
        });

        this.player.setOnInfoListener((iMediaPlayer, what, extra) ->
        {
            if (videoEventsListener == null)
            {
                return true;
            }
            if (what == MediaPlayer.MEDIA_INFO_BUFFERING_START)
            {
                videoEventsListener.onBufferingStart();
            }
            else if (what == MediaPlayer.MEDIA_INFO_BUFFERING_END)
            {
                videoEventsListener.onBufferingEnd();
            }
            return true;
        });

        this.player.setOnCompletionListener(iMediaPlayer ->
        {
            if (videoEventsListener != null)
            {
                videoEventsListener.onCompeted();
            }
        });
    }

    @Override
    public void setSurface(Surface surface)
    {
        this.surface = surface;
    }

    @Override
    public void setUri(Uri uri, Map<String, String> header)
    {
        try
        {
            this.player.reset();
            this.player.setDataSource(this.context, uri, header);
        }
        catch (IOException e)
        {
            e.printStackTrace();
        }
    }

    @Override
    public void prepare()
    {
        if (this.videoEventsListener != null)
        {
            this.videoEventsListener.onBufferingStart();
        }
        this.startProgressReport();
        this.player.prepareAsync();
    }

    @Override
    public void play()
    {
        this.player.setSurface(this.surface);
        this.player.start();
        if (this.videoEventsListener != null)
        {
            this.videoEventsListener.onPlay();
        }
    }

    @Override
    public void seekTo(long time)
    {
        this.player.seekTo(time);
    }

    @Override
    public void pause()
    {
        this.player.pause();
        if (this.videoEventsListener != null)
        {
            this.videoEventsListener.onPause();
        }
    }

    @Override
    public void stop()
    {
        this.player.stop();
    }

    @Override
    public void release()
    {
        this.player.release();
        this.releaseSubscriptionList();
    }

    @Override
    public boolean isPlaying()
    {
        return this.player.isPlaying();
    }

    @Override
    public long getDuration()
    {
        return this.player.getDuration();
    }

    @Override
    public void setVideoEventsListener(OnVideoEventsListener videoEventsListener)
    {
        this.videoEventsListener = videoEventsListener;
    }

    private void startProgressReport()
    {
        Subscription subscription = Observable.interval(200, TimeUnit.MILLISECONDS).filter(l -> videoEventsListener != null && this.isPlaying()).map(l -> getPlayPosition()).filter(position -> position >= 0).subscribeOn(Schedulers.newThread()).observeOn(AndroidSchedulers.mainThread()).subscribe(position -> videoEventsListener.onPlayProgressUpdated(position));
        this.releaseSubscriptionList();
        this.subscriptionList = new SubscriptionList();
        this.subscriptionList.add(subscription);
    }

    private long getPlayPosition()
    {
        try
        {
            return this.player.getCurrentPosition();
        }
        catch (IllegalStateException e)
        {
            return -1;
        }
    }

    private void releaseSubscriptionList()
    {
        if (subscriptionList == null)
        {
            return;
        }
        this.subscriptionList.unsubscribe();
        this.subscriptionList.clear();
        this.subscriptionList = null;
    }
}
