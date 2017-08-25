package cn.zmy.mjwparser;

import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;
import android.support.v7.widget.DividerItemDecoration;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.text.TextUtils;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.ListView;
import android.widget.TextView;

import org.json.JSONArray;
import org.json.JSONObject;

import java.io.IOException;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.List;

import cn.zmy.mjwparser.base.SimpleTextAdapter;
import cn.zmy.mjwparser.constant.IntentKeys;
import cn.zmy.mjwparser.model.Video;
import cn.zmy.mjwparser.model.VideoGroup;
import cn.zmy.mjwparser.util.IOUtil;

public class MainActivity extends Activity
{
    private List<VideoGroup> mVideoGroups;

    @Override
    protected void onCreate(Bundle savedInstanceState)
    {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        InputStream inputStreamVideoGroups = null;
        try
        {
            inputStreamVideoGroups = getAssets().open("source.json");
        }
        catch (IOException ignored)
        {
            return;
        }
        String videoGroupsJson = IOUtil.toString(inputStreamVideoGroups);
        if (TextUtils.isEmpty(videoGroupsJson))
        {
            return;
        }

        //减少apk体积，就不用其他json解析库了
        try
        {
            JSONArray jsonArrayVideoGroups = new JSONArray(videoGroupsJson);
            mVideoGroups = new ArrayList<>(jsonArrayVideoGroups.length());
            for (int i = 0; i < jsonArrayVideoGroups.length(); i++)
            {
                JSONObject jsonObjectVideoGroup = jsonArrayVideoGroups.getJSONObject(i);
                VideoGroup videoGroup = new VideoGroup();
                videoGroup.setName(jsonObjectVideoGroup.getString("name"));
                videoGroup.setUrl(jsonObjectVideoGroup.getString("url"));
                mVideoGroups.add(videoGroup);
            }
        }
        catch (Exception ignored)
        {
            return;
        }

        ListView listViewVideoGroups = (ListView) findViewById(R.id.listViewVideoGroups);
        VideoGroupsAdapter adapter = new VideoGroupsAdapter();
        listViewVideoGroups.setAdapter(adapter);
        listViewVideoGroups.setOnItemClickListener(new AdapterView.OnItemClickListener()
        {
            @Override
            public void onItemClick(AdapterView<?> parent, View view, int position, long id)
            {
                Intent intent = new Intent(MainActivity.this, VideosActivity.class);
                intent.putExtra(IntentKeys.KEY_VIDEO_GROUP, mVideoGroups.get(position));
                MainActivity.this.startActivity(intent);
            }
        });
        adapter.refresh(mVideoGroups);
    }

    private static class VideoGroupsAdapter extends SimpleTextAdapter<VideoGroup>
    {
        @Override
        protected String getText(VideoGroup videoGroup)
        {
            return videoGroup.getName();
        }
    }
}
