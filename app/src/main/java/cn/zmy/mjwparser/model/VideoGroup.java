package cn.zmy.mjwparser.model;

import android.os.Parcel;
import android.os.Parcelable;

/**
 * Created by zmy on 2017/8/25 0025.
 */

public class VideoGroup implements Parcelable
{
    private String name;
    private String url;

    public String getName()
    {
        return name;
    }

    public void setName(String name)
    {
        this.name = name;
    }

    public String getUrl()
    {
        return url;
    }

    public void setUrl(String url)
    {
        this.url = url;
    }

    @Override
    public int describeContents()
    {
        return 0;
    }

    @Override
    public void writeToParcel(Parcel dest, int flags)
    {
        dest.writeString(this.name);
        dest.writeString(this.url);
    }

    public VideoGroup()
    {
    }

    protected VideoGroup(Parcel in)
    {
        this.name = in.readString();
        this.url = in.readString();
    }

    public static final Creator<VideoGroup> CREATOR = new Creator<VideoGroup>()
    {
        @Override
        public VideoGroup createFromParcel(Parcel source)
        {
            return new VideoGroup(source);
        }

        @Override
        public VideoGroup[] newArray(int size)
        {
            return new VideoGroup[size];
        }
    };
}
