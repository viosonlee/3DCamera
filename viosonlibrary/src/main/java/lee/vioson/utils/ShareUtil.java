package lee.vioson.utils;

import android.content.Context;
import android.content.Intent;

/**
 * Author:李烽
 * Date:2016-06-01
 * FIXME
 * Todo
 */
public class ShareUtil {
    /**
     * 分享
     *
     * @param context
     * @param content
     */
    public static void share(Context context, String content) {
        Intent intent = new Intent(Intent.ACTION_SEND);
        intent.setType("text/plain");
        intent.putExtra(Intent.EXTRA_TEXT, content);
        intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
        context.startActivity(intent);
    }
}
