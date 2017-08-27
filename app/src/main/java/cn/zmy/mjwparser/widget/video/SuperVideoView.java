package cn.zmy.mjwparser.widget.video;

import android.content.Context;
import android.graphics.SurfaceTexture;
import android.util.AttributeSet;
import android.view.LayoutInflater;
import android.view.Surface;
import android.view.TextureView;
import android.view.View;
import android.view.ViewGroup;
import android.widget.FrameLayout;
import android.widget.ProgressBar;

import cn.zmy.mjwparser.R;


/**
 * Created by zmy on 2016/8/19 0019.
 */
public class SuperVideoView extends FrameLayout
    implements TextureView.SurfaceTextureListener
{
    private final int ID_CONTROLLER = 1000;

    private FrameLayout viewContent;
    private ResizeVideoTextureView textureView;
    private SuperVideoController videoController;
    private ProgressBar progressBarLoading;
    private Surface surface;
    private IVideoPlayer videoPlayer;

    public SuperVideoView(Context context)
    {
        this(context,null);
    }

    public SuperVideoView(Context context, AttributeSet attrs)
    {
        this(context, attrs,0);
    }

    public SuperVideoView(Context context, AttributeSet attrs, int defStyleAttr)
    {
        super(context, attrs, defStyleAttr);
        viewContent = (FrameLayout) LayoutInflater.from(context).inflate(R.layout.widget_course_video,this,false);
        this.addView(viewContent,new LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.MATCH_PARENT));

        this.textureView = (ResizeVideoTextureView) viewContent.findViewById(R.id.textureView);
        this.progressBarLoading = (ProgressBar) viewContent.findViewById(R.id.progressBarLoading);

        if (!isInEditMode())
        {
            this.textureView.setSurfaceTextureListener(this);
            this.textureView.setOnClickListener(this::onTextureViewClick);
            this.videoPlayer = new IJKVideoPlayer(context);
        }
    }

    //region TextureView.SurfaceTextureListener接口实现
    @Override
    public void onSurfaceTextureAvailable(SurfaceTexture surface, int width, int height)
    {
        this.surface = new Surface(surface);
        this.videoPlayer.setSurface(this.surface);
    }

    @Override
    public void onSurfaceTextureSizeChanged(SurfaceTexture surface, int width, int height)
    {

    }

    @Override
    public boolean onSurfaceTextureDestroyed(SurfaceTexture surface)
    {
        surface.release();
        return true;
    }

    @Override
    public void onSurfaceTextureUpdated(SurfaceTexture surface)
    {

    }
    //endregion

    public SuperVideoController getVideoController()
    {
        return this.videoController;
    }

    public void setVideoController(SuperVideoController controller)
    {
        this.videoController = controller;
        this.videoController.setTextureView(this.textureView);
        this.videoController.setPlayer(this.videoPlayer);
        this.videoController.setProgressBarLoading(this.progressBarLoading);

        this.videoController.setId(ID_CONTROLLER);
        View old = this.viewContent.findViewById(ID_CONTROLLER);
        if (old != null)
        {
            this.viewContent.removeView(old);
        }

        this.viewContent.addView(this.videoController,
                new LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.MATCH_PARENT));
    }

    private void onTextureViewClick(View view)
    {
        this.videoController.show(true);
    }
}
