package lee.vioson.utils;

import android.app.Activity;
import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.drawable.BitmapDrawable;
import android.graphics.drawable.Drawable;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.view.View;
import android.widget.ImageView;

import com.squareup.picasso.Picasso;
import com.squareup.picasso.Target;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.lang.ref.WeakReference;

import lee.vioson.viosonlibrary.R;


/**
 * Author:李烽
 * Date:2016-05-27
 * FIXME
 * Todo
 */
public class PicassoUtil {

    public static void setImage(Context context, String url, ImageView imageView) {
        Picasso.with(context).load(url)
                .error(R.drawable.pic_icon)
                .placeholder(R.drawable.pic_icon)
                .into(imageView);
    }

    private static SaveBitmapListener listener;

    public static void setBackground(final Context context, String url, final View view) {
        Picasso.with(context).load(url)
                .error(R.drawable.pic_icon)
                .placeholder(R.drawable.pic_icon)
                .into(new Target() {
                    @Override
                    public void onBitmapLoaded(Bitmap bitmap, Picasso.LoadedFrom from) {
                        view.setBackgroundDrawable(new BitmapDrawable(context.getResources(), bitmap));
                    }

                    @Override
                    public void onBitmapFailed(Drawable errorDrawable) {
                        view.setBackgroundDrawable(errorDrawable);
                    }

                    @Override
                    public void onPrepareLoad(Drawable placeHolderDrawable) {
                        view.setBackgroundDrawable(placeHolderDrawable);
                    }
                });
    }


    public static void saveBitmapSDCard(final Context context, String url, final String filePath,
                                        final SaveBitmapListener listener) {
        PicassoUtil.listener = listener;
        Target target = new Target() {
            @Override
            public void onBitmapLoaded(final Bitmap bitmap, Picasso.LoadedFrom from) {
                final File saveFile = new File(filePath);
                File fileDir = new File(saveFile.getParent());
                if (!fileDir.exists())
                    fileDir.mkdirs();
                new Thread(new Runnable() {
                    @Override
                    public void run() {
                        FileOutputStream fos = null;
                        try {
                            fos = new FileOutputStream(saveFile);
                            bitmap.compress(Bitmap.CompressFormat.PNG, 100, fos);
                            fos.flush();
                            fos.close();
                            Message msg = new Message();
                            msg.what = SAVED;
                            Bundle b = new Bundle();
                            b.putString(PATH, saveFile.getParent());
                            msg.setData(b);
                            handler.sendMessage(msg);
                        } catch (IOException e) {
                            e.printStackTrace();
                            Message msg = new Message();
                            msg.what = SAVE_FAILURE;
                            Bundle b = new Bundle();
                            b.putString(MSG, e.getMessage());
                            msg.setData(b);
                            handler.sendMessage(msg);
                        }
                    }
                }).start();
            }

            @Override
            public void onBitmapFailed(Drawable errorDrawable) {

            }

            @Override
            public void onPrepareLoad(Drawable placeHolderDrawable) {

            }

        };
        Picasso.with(context).load(url).into(target);
    }


    public interface SaveBitmapListener {
        void onSaved(String path);

        void onFailure(String message);
    }

    private static final int SAVED = 1;
    private static final int SAVE_FAILURE = 2;
    private static final java.lang.String PATH = "path";

    private static final java.lang.String MSG = "msg";

    private static Handler handler = new Handler();

    static class Handler extends android.os.Handler {
        @Override
        public void handleMessage(Message msg) {
            switch (msg.what) {
                case SAVED:
                    if (listener != null)
                        listener.onSaved(msg.getData().getString(PATH));
                    break;

                case SAVE_FAILURE:
                    if (listener != null)
                        listener.onFailure(msg.getData().getString(MSG));
                    break;

            }
        }
    }
}
