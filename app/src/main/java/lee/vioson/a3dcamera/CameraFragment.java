package lee.vioson.a3dcamera;

import android.graphics.Bitmap;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.Toast;

import java.io.File;

import lee.vioson.fragments.Camera2BasicFragment;
import lee.vioson.utils.BitmapCompress;
import lee.vioson.utils.BitmapUtil;

/**
 * Author:李烽
 * Date:2016-06-27
 * FIXME
 * Todo
 */
public class CameraFragment extends Camera2BasicFragment implements View.OnClickListener {

    boolean second;
    private static final String PIC_1 = "PIC_1.jpg";
    private static final String PIC_2 = "PIC_2.jpg";
    String fileName = PIC_1;
    File dir;
    private Bitmap bitmap1;
    private Bitmap bitmap2;

    private static final int STATE_TAKING = 0x001;
    private static final int STATE_NORMAL = 0x002;
    private int state = STATE_NORMAL;
    private Button btn;

    @Override
    protected int getTextureViewLayoutId() {
        return R.layout.fragment_camera;
    }


    @Override
    public void onViewCreated(View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        view.findViewById(R.id.picture).setOnClickListener(this);
        btn = (Button) view.findViewById(R.id.picture);
    }

    @Override
    public void onActivityCreated(@Nullable Bundle savedInstanceState) {
        super.onActivityCreated(savedInstanceState);
        dir = new File(getActivity().getExternalFilesDir(null) + "/temp/");
        if (!dir.exists())
            dir.mkdir();
    }

    @Override
    protected int getTextureViewId() {
        return R.id.texture;
    }

    public CameraFragment() {

    }

    @Override
    protected String setFilePath() {
        return String.format("%slee.jpg", getActivity().getExternalFilesDir(null).getPath());
    }

    @Override
    public void onClick(View v) {
        if (state == STATE_NORMAL) {
            takePicture(dir, fileName);
            Log.d("take", second + "");
            Log.d("take", state + "");
            state = STATE_TAKING;
            btn.setEnabled(false);
        }
    }

    @Override
    protected void onFileSaved() {
        if (!second) {
            fileName = PIC_2;
            second = true;
            Toast.makeText(getActivity(), "向右平移1厘米点击拍照", Toast.LENGTH_SHORT).show();
        } else {
            fileName = PIC_1;
            second = false;
            Toast.makeText(getActivity(), "正在合成。。。", Toast.LENGTH_SHORT).show();
            bitmap1 = BitmapCompress.getSmallBitmap(dir.getPath() + "/" + PIC_1);
            bitmap2 = BitmapCompress.getSmallBitmap(dir.getPath() + "/" + PIC_2);
            Bitmap bitmap = BitmapUtil.fix2Bitmap(bitmap1, bitmap2, false);
            BitmapUtil.saveBitmap(bitmap, dir.getPath() + "/bitmap.jpg");
        }
        state = STATE_NORMAL;
        btn.setEnabled(true);
    }

}
