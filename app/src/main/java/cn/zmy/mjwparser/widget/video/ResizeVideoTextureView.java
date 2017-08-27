package cn.zmy.mjwparser.widget.video;

import android.content.Context;
import android.util.AttributeSet;
import android.util.Log;
import android.view.TextureView;

/**
 * Created by zmy on 2016/8/19.
 * 根据Video的Size动态调节TextureView的Size，防止视频播放变形
 */
public class ResizeVideoTextureView extends TextureView
{
    public static final String TAG = "ResizeableTextureView";

    private int videoWidth;
    private int videoHeight;

    public ResizeVideoTextureView(Context context)
    {
        this(context,null);
    }

    public ResizeVideoTextureView(Context context, AttributeSet attrs)
    {
        this(context, attrs,0);
    }

    public ResizeVideoTextureView(Context context, AttributeSet attrs, int defStyleAttr)
    {
        super(context, attrs, defStyleAttr);
    }

    @Override
    protected void onMeasure(int widthMeasureSpec, int heightMeasureSpec)
    {
        int width = getDefaultSize(videoWidth, widthMeasureSpec);
        int height = getDefaultSize(videoHeight, heightMeasureSpec);

        Log.i(TAG, String.format("videoWidth=%d,videoHeight=%d,width=%d,height=%d", videoWidth, videoWidth, width, height));
        if (videoWidth <= 0 && videoHeight <= 0)
        {
            setMeasuredDimension(width, height);
            return;
        }
        int widthSpecMode = MeasureSpec.getMode(widthMeasureSpec);
        int widthSpecSize = MeasureSpec.getSize(widthMeasureSpec);
        int heightSpecMode = MeasureSpec.getMode(heightMeasureSpec);
        int heightSpecSize = MeasureSpec.getSize(heightMeasureSpec);

        if (widthSpecMode == MeasureSpec.EXACTLY && heightSpecMode == MeasureSpec.EXACTLY)
        {
            // the size is fixed
            width = widthSpecSize;
            height = heightSpecSize;

            // for compatibility, we adjust size based on aspect ratio
            if (videoWidth * height < width * videoHeight)
            {
                width = height * videoWidth / videoHeight;
            }
            else if (videoWidth * height > width * videoHeight)
            {
                height = width * videoHeight / videoWidth;
            }
        }
        else if (widthSpecMode == MeasureSpec.EXACTLY)
        {
            // only the width is fixed, adjust the height to match aspect ratio if possible
            width = widthSpecSize;
            height = width * videoHeight / videoWidth;
            if (heightSpecMode == MeasureSpec.AT_MOST && height > heightSpecSize)
            {
                // couldn't match aspect ratio within the constraints
                height = heightSpecSize;
            }
        }
        else if (heightSpecMode == MeasureSpec.EXACTLY)
        {
            // only the height is fixed, adjust the width to match aspect ratio if possible
            height = heightSpecSize;
            width = height * videoWidth / videoHeight;
            if (widthSpecMode == MeasureSpec.AT_MOST && width > widthSpecSize)
            {
                // couldn't match aspect ratio within the constraints
                width = widthSpecSize;
            }
        }
        else
        {
            // neither the width nor the height are fixed, try to use actual video size
            width = videoWidth;
            height = videoHeight;
            if (heightSpecMode == MeasureSpec.AT_MOST && height > heightSpecSize)
            {
                // too tall, decrease both width and height
                height = heightSpecSize;
                width = height * videoWidth / videoHeight;
            }
            if (widthSpecMode == MeasureSpec.AT_MOST && width > widthSpecSize)
            {
                // too wide, decrease both width and height
                width = widthSpecSize;
                height = width * videoHeight / videoWidth;
            }
        }
        setMeasuredDimension(width, height);
    }

    public void setVideoWH(int videoWidth,int videoHeight)
    {
        this.videoWidth = videoWidth;
        this.videoHeight = videoHeight;
        this.requestLayout();
    }
}
