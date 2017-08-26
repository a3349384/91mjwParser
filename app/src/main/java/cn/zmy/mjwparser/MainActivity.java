package cn.zmy.mjwparser;

import android.app.Activity;
import android.content.Intent;
import android.os.Build;
import android.os.Bundle;
import android.text.TextUtils;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ListView;

import org.json.JSONArray;
import org.json.JSONObject;

import java.io.IOException;
import java.io.InputStream;
import java.lang.reflect.Field;
import java.net.CookieHandler;
import java.net.URI;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Map;

import cn.zmy.mjwparser.base.SimpleTextAdapter;
import cn.zmy.mjwparser.constant.IntentKeys;
import cn.zmy.mjwparser.model.VideoGroup;
import cn.zmy.mjwparser.util.IOUtil;

public class MainActivity extends Activity
{
    private List<VideoGroup> mVideoGroups;

    @Override
    protected void onCreate(Bundle savedInstanceState)
    {
        super.onCreate(savedInstanceState);
        //hook cookie handler,解决VideoView播放链接不携带cookie的问题
        try
        {
            Field fieldCookieHandler = null;
            if (Build.VERSION.SDK_INT <= Build.VERSION_CODES.M)
            {
                fieldCookieHandler = Class.forName("java.net.CookieHandler").getDeclaredField("systemWideCookieHandler");
            }
            else
            {
                fieldCookieHandler = Class.forName("java.net.CookieHandler").getDeclaredField("cookieHandler");
            }
            fieldCookieHandler.setAccessible(true);
            fieldCookieHandler.set(null, new CookieHandler()
            {
                private List<String> mCookies;

                @Override
                public Map<String, List<String>> get(URI uri, Map<String, List<String>> requestHeaders) throws IOException
                {
                    Map<String, List<String>> cookieMap =
                            new java.util.HashMap<String, List<String>>();
                    if (uri.getHost().endsWith("1suplayer.me") && mCookies != null && mCookies.size() > 0)
                    {
                        cookieMap.put("Cookie", mCookies);
                    }
                    return Collections.unmodifiableMap(cookieMap);
                }

                @Override
                public void put(URI uri, Map<String, List<String>> responseHeaders) throws IOException
                {
                    if (uri.getHost().contentEquals("file.1suplayer.me") && responseHeaders.containsKey("Set-Cookie"))
                    {
                        mCookies = responseHeaders.get("Set-Cookie");
                    }
                }
            });
        }
        catch (Exception e)
        {
            e.printStackTrace();
        }

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
