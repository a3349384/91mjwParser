package cn.zmy.mjwparser.model;

import android.os.Parcel;
import android.os.Parcelable;

/**
 * Created by zmy on 2017/8/25 0025.
 */

public class Video implements Parcelable
{
    private String id;
    private String name;
    private String url;

    public String getId()
    {
        return id;
    }

    public void setId(String id)
    {
        this.id = id;
    }

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
        dest.writeString(this.id);
        dest.writeString(this.name);
        dest.writeString(this.url);
    }

    public Video()
    {
    }

    protected Video(Parcel in)
    {
        this.id = in.readString();
        this.name = in.readString();
        this.url = in.readString();
    }

    public static final Creator<Video> CREATOR = new Creator<Video>()
    {
        @Override
        public Video createFromParcel(Parcel source)
        {
            return new Video(source);
        }

        @Override
        public Video[] newArray(int size)
        {
            return new Video[size];
        }
    };
}
