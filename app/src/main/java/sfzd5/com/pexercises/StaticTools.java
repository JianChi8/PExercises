package sfzd5.com.pexercises;

import android.content.Context;
import android.os.Environment;

import java.io.File;
import java.io.IOException;
import java.io.InputStream;

/**
 * Created by fsp on 17-10-22.
 */

public class StaticTools {
    public static File getDiskShareDir(Context context) {
        String cachePath;
        if (Environment.MEDIA_MOUNTED.equals(Environment.getExternalStorageState())
                || !Environment.isExternalStorageRemovable()) {
            cachePath = context.getExternalCacheDir().getPath();
        } else {
            cachePath = context.getCacheDir().getPath();
        }
        return new File(cachePath + File.separator + "share");
    }

    static String[] hardArray = new String[]{
            "☆☆☆☆☆",
            "★☆☆☆☆",
            "★★☆☆☆",
            "★★★☆☆",
            "★★★★☆",
            "★★★★★"
    };
    public static String hard(int hard){
        return hardArray[hard];
    }


    public static String readRawAllText(Context context, int rawId){
        StringBuilder sb = new StringBuilder();
        InputStream in = context.getResources().openRawResource(rawId);
        try {
            int blockLen = 10240;
            byte block[] = new byte[blockLen];
            int readLen = 0;
            while ((readLen = in.read(block)) != -1) {
                sb.append(new String(block,0,readLen));
            }
        } catch (IOException e) {
            e.printStackTrace();
        } finally {
            try {
                in.close();
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
        return sb.toString();
    }

}
