package lee.vioson.utils;

import android.app.Activity;
import android.content.Context;
import android.content.DialogInterface;
import android.support.v7.app.AlertDialog;


/**
 * Author:李烽
 * Date:2016-05-31
 * FIXME
 * Todo
 */
public class DialogShower {

    public static void showNetWorkDisconnect(final Context context) {
        new AlertDialog.Builder(context).setMessage("无网络连接，请检查网络设置")
                .setCancelable(false).setNegativeButton("好的",
                new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        //去设置网络
                        NetWorkUtils.toSetNetWork(context);
                        dialog.dismiss();
                    }
                })
                .setPositiveButton("退出", new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        try {
                            ((Activity) context).finish();
                        } catch (Exception e) {
                            e.printStackTrace();
                        }
                    }
                })
                .create().show();
    }
}
