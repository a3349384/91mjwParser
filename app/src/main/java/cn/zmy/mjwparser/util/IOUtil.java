package cn.zmy.mjwparser.util;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;

/**
 * Created by zmy on 2017/8/25 0025.
 */

public class IOUtil
{
    public static String toString(InputStream inputStream)
    {
        try
        {
            BufferedReader r = new BufferedReader(new InputStreamReader(inputStream));
            StringBuilder total = new StringBuilder();
            String line;
            while ((line = r.readLine()) != null) {
                total.append(line).append('\n');
            }
            return total.toString();
        }
        catch (Exception ignored)
        {
            return null;
        }
        finally
        {
            try
            {
                inputStream.close();
            }
            catch (IOException ignored){}
        }
    }
}
