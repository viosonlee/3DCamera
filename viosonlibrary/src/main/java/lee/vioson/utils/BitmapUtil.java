package lee.vioson.utils;

import android.graphics.Bitmap;
import android.graphics.Canvas;
import android.util.Log;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;

/**
 * Author:李烽
 * Date:2016-06-22
 * FIXME
 * Todo
 */
public class BitmapUtil {
    /**
     * 拼接两张图片
     *
     * @param first
     * @param second
     * @param isVertical
     * @return
     */
    public static Bitmap fix2Bitmap(Bitmap first, Bitmap second, boolean isVertical) {
        int firstWidth = first.getWidth();
        int firstHeight = first.getHeight();
        int secondWidth = second.getWidth();
        int secondHeight = second.getHeight();
        Bitmap bitmap;
        int width;
        int height;
        if (isVertical) {
            width = Math.max(firstWidth, secondWidth);
            height = firstHeight + secondHeight;
            bitmap = Bitmap.createBitmap(width, height, Bitmap.Config.ARGB_8888);
            Canvas canvas = new Canvas(bitmap);
            canvas.drawBitmap(first, 0, 0, null);
            canvas.drawBitmap(second, 0, firstHeight, null);
        } else {
            width = firstWidth + secondWidth;
            height = Math.max(firstHeight, secondHeight);
            bitmap = Bitmap.createBitmap(width, height, Bitmap.Config.ARGB_8888);
            Canvas canvas = new Canvas(bitmap);
            canvas.drawBitmap(first, 0, 0, null);
            canvas.drawBitmap(second, firstWidth, 0, null);
        }
        return bitmap;
    }

    /**
     * 保存方法
     */
    public static void saveBitmap(Bitmap bm, String filePath) {
        File f = new File(filePath);
        File dir = new File(f.getParent());
        if (!dir.exists())
            dir.mkdirs();
        if (f.exists()) {
            f.delete();
        }
        try {
            FileOutputStream out = new FileOutputStream(f);
            bm.compress(Bitmap.CompressFormat.PNG, 100, out);
            out.flush();
            out.close();
        } catch (IOException e) {
            e.printStackTrace();
        }

    }

}
