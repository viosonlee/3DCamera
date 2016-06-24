package lee.vioson.a3dcamera;

import android.content.Context;
import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.ImageFormat;
import android.hardware.Camera;
import android.hardware.camera2.CameraDevice;
import android.hardware.camera2.CameraManager;
import android.os.Bundle;
import android.os.Environment;
import android.os.Handler;
import android.os.HandlerThread;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import android.view.SurfaceHolder;
import android.view.SurfaceView;
import android.view.View;
import android.widget.LinearLayout;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;

import butterknife.BindView;
import butterknife.ButterKnife;
import lee.vioson.utils.BitmapCompress;
import lee.vioson.utils.BitmapUtil;

public class MainActivity extends AppCompatActivity implements SurfaceHolder.Callback {

    private static final java.lang.String CAMERA2 = "camera2";

    SurfaceView surfaceView;

    private SurfaceHolder holder;

    private HandlerThread mHandlerThread;

    private Handler mHandler;
    private Camera mCamera;
    private Camera.PictureCallback pictureCallback;
    private String tempPath;
    private String tempPath2;
    private String filePath;
    private boolean isSecond = false;

    private Bitmap bitmap1 = null;
    private Bitmap bitmap2 = null;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        surfaceView = (SurfaceView) findViewById(R.id.surfaceView);

        holder = surfaceView.getHolder();
        holder.addCallback(this);

        tempPath = Environment.getExternalStorageDirectory().getAbsolutePath() + "/temp1.jpg";
        tempPath2 = Environment.getExternalStorageDirectory().getAbsolutePath() + "/temp2.jpg";
        filePath = Environment.getExternalStorageDirectory().getAbsolutePath() +
                "/3DCamera/3DCamera" + System.currentTimeMillis() + ".jpg";

        pictureCallback = new Camera.PictureCallback() {
            @Override
            public void onPictureTaken(byte[] data, Camera camera) {
                try {
                    FileOutputStream fileOutputStream = new FileOutputStream(new File(isSecond ? tempPath2 : tempPath));
                    fileOutputStream.write(data);
                    fileOutputStream.flush();
                    fileOutputStream.close();
                    if (isSecond) {
                        bitmap2 = BitmapFactory.decodeByteArray(data, 0, data.length);
                        Bitmap bitmap = BitmapUtil.fix2Bitmap(bitmap1, bitmap2, false);
                        BitmapUtil.saveBitmap(bitmap, filePath);
                        isSecond = false;
                    } else {
                        bitmap1 = BitmapFactory.decodeByteArray(data, 0, data.length);
                        isSecond = true;
                    }
                } catch (IOException | OutOfMemoryError e) {
                    e.printStackTrace();
                }

                relaseCamera();
                initCamera();
            }
        };
    }

    @Override
    protected void onPause() {
        super.onPause();
        relaseCamera();
    }

    private void relaseCamera() {
        if (mCamera != null) {
            mCamera.setPreviewCallback(null);
            mCamera.stopPreview();
            mCamera.release();
            mCamera = null;
        }
    }

    @Override
    protected void onResume() {
        super.onResume();
        initCamera();
    }

    private void initCamera() {
        if (mCamera == null) {
            mCamera = getCamera();
            if (holder != null) {
                setStartPreview(mCamera, holder);
            }

        }
    }

    private void setStartPreview(Camera camera, SurfaceHolder holder) {
        try {
            camera.setPreviewDisplay(holder);
            //默认是横屏，设置为竖屏
            camera.setDisplayOrientation(90);
            camera.startPreview();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    private Camera getCamera() {
        Camera camera;
        int cameraId = 0;//0为后置，1为前置
//        int cameraCount = 0;
//        Camera.CameraInfo cameraInfo = new Camera.CameraInfo();
//        cameraCount = Camera.getNumberOfCameras();//得到摄像头的个数

        try {
            camera = Camera.open(cameraId);
        } catch (Exception e) {
            camera = null;
            e.printStackTrace();
        }
        return camera;
    }

    public void capture(View view) {
        Camera.Parameters parameters = mCamera.getParameters();
        parameters.setPictureFormat(ImageFormat.JPEG);//设置图片格式
        parameters.setPreviewSize(1280, 720);//设置预览尺寸
        parameters.setPictureSize(1920, 1080);
        parameters.setFocusMode(Camera.Parameters.FOCUS_MODE_AUTO);//设置自动对焦
        mCamera.autoFocus(new Camera.AutoFocusCallback() {
            @Override
            public void onAutoFocus(boolean success, Camera camera) {
                if (success) {
                    mCamera.takePicture(null, null, pictureCallback);
                }
            }
        });
    }

    @Override
    public void surfaceCreated(SurfaceHolder holder) {
        setStartPreview(mCamera, this.holder);
    }

    @Override
    public void surfaceChanged(SurfaceHolder holder, int format, int width, int height) {
        mCamera.stopPreview();
        setStartPreview(mCamera, this.holder);
    }

    @Override
    public void surfaceDestroyed(SurfaceHolder holder) {
        relaseCamera();
    }
}
