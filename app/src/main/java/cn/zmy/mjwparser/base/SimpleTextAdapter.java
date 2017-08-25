package cn.zmy.mjwparser.base;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.TextView;

import java.util.ArrayList;
import java.util.List;

import cn.zmy.mjwparser.R;
import cn.zmy.mjwparser.model.Video;

/**
 * Created by zmy on 2017/8/25 0025.
 */

public abstract class SimpleTextAdapter<T> extends BaseAdapter
{
    private List<T> mItems;

    public SimpleTextAdapter()
    {
        mItems = new ArrayList<T>();
    }

    @Override
    public int getCount()
    {
        return mItems.size();
    }

    @Override
    public T getItem(int position)
    {
        return mItems.get(position);
    }

    @Override
    public long getItemId(int position)
    {
        return position;
    }

    @Override
    public View getView(int position, View convertView, ViewGroup parent)
    {
        if (convertView == null)
        {
            convertView = LayoutInflater.from(parent.getContext()).inflate(R.layout.item_text, parent, false);
        }
        ((TextView)convertView).setText(getText(mItems.get(position)));
        return convertView;
    }

    public void refresh(List<T> items)
    {
        this.mItems.clear();
        if (items != null)
        {
            this.mItems.addAll(items);
        }

        notifyDataSetChanged();
    }

    protected abstract String getText(T t);
}
