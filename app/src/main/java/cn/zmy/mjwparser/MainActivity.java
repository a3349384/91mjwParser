package cn.zmy.mjwparser;

import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.support.v7.widget.DividerItemDecoration;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.text.TextUtils;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;
import android.widget.Toast;

import org.json.JSONArray;
import org.json.JSONObject;

import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.StringReader;
import java.util.ArrayList;
import java.util.List;

import cn.zmy.mjwparser.model.VideoGroup;
import cn.zmy.mjwparser.util.IOUtil;

public class MainActivity extends AppCompatActivity
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

        RecyclerView recyclerViewVideoGroups = (RecyclerView) findViewById(R.id.recyclerViewVideoGroups);
        recyclerViewVideoGroups.setHasFixedSize(true);
        recyclerViewVideoGroups.setLayoutManager(new LinearLayoutManager(this));
        DividerItemDecoration divider = new DividerItemDecoration(this, DividerItemDecoration.VERTICAL);
        divider.setDrawable(getDrawable(R.drawable.divider_1dp_gray));
        recyclerViewVideoGroups.addItemDecoration(divider);
        recyclerViewVideoGroups.setAdapter(new RecyclerView.Adapter()
        {
            @Override
            public RecyclerView.ViewHolder onCreateViewHolder(ViewGroup parent, int viewType)
            {
                return new VideoGroupViewHolder(LayoutInflater.from(MainActivity.this).inflate(R.layout.item_video_group, parent, false));
            }

            @Override
            public void onBindViewHolder(RecyclerView.ViewHolder holder, int position)
            {
                ((VideoGroupViewHolder)holder).textViewName.setText(mVideoGroups.get(position).getName());
            }

            @Override
            public int getItemCount()
            {
                return mVideoGroups.size();
            }

            class VideoGroupViewHolder extends RecyclerView.ViewHolder
            {
                public TextView textViewName;

                public VideoGroupViewHolder(View itemView)
                {
                    super(itemView);
                    textViewName = (TextView) itemView;
                }
            }
        });
    }
}
