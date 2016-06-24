package lee.vioson.a3dcamera;

import android.Manifest;
import android.graphics.Bitmap;
import android.graphics.ImageFormat;
import android.graphics.SurfaceTexture;
import android.hardware.camera2.CameraAccessException;
import android.hardware.camera2.CameraCaptureSession;
import android.hardware.camera2.CameraCharacteristics;
import android.hardware.camera2.CameraDevice;
import android.hardware.camera2.CameraManager;
import android.hardware.camera2.CaptureRequest;
import android.hardware.camera2.CaptureResult;
import android.hardware.camera2.TotalCaptureResult;
import android.hardware.camera2.params.StreamConfigurationMap;
import android.media.Image;
import android.media.ImageReader;
import android.os.Bundle;
import android.os.Environment;
import android.os.Handler;
import android.os.HandlerThread;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import android.util.Size;
import android.view.Surface;
import android.view.SurfaceHolder;
import android.view.TextureView;
import android.view.View;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.nio.ByteBuffer;
import java.util.Arrays;
import java.util.Collections;

import lee.vioson.apache.IOUtils;
import lee.vioson.utils.PermissionUtil;

public class MainActivity_2 extends AppCompatActivity implements TextureView.SurfaceTextureListener, View.OnClickListener {

    private static final String CAMERA2 = "camera2";
    private static final int STATE_PREVIEW = 1;
    private static final int STATE_WAITING_CAPTURE = 2;

//    SurfaceView surfaceView;

    private TextureView mPreviewView;
    private SurfaceHolder holder;

    private HandlerThread mHandlerThread;

    private Handler mHandler;

    private CameraManager mCameraManager;

    private String tempPath;
    private String tempPath2;
    private String filePath;
    private boolean isSecond = false;

    private Bitmap bitmap1 = null;
    private Bitmap bitmap2 = null;
    private Size mPreviewSize;

    private CaptureRequest.Builder mPreviewBuilder;


    private ImageReader mImageReader;
    private CameraDevice mCameraDevice;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main_2);

//        surfaceView = (SurfaceView) findViewById(R.id.surfaceView);
        mPreviewView = (TextureView) findViewById(R.id.texture_view);
        mPreviewView.setSurfaceTextureListener(this);
        mPreviewView.setOnClickListener(this);
//        holder = surfaceView.getHolder();
//        holder.addCallback(this);

        tempPath = Environment.getExternalStorageDirectory().getAbsolutePath() + "/temp1.jpg";
        tempPath2 = Environment.getExternalStorageDirectory().getAbsolutePath() + "/temp2.jpg";
        filePath = Environment.getExternalStorageDirectory().getAbsolutePath() +
                "/3DCamera/3DCamera" + System.currentTimeMillis() + ".jpg";

        initLooper();


    }

    private CameraCaptureSession.StateCallback mSessionStateCallback = new CameraCaptureSession.StateCallback() {

        @Override
        public void onConfigured(CameraCaptureSession session) {
            mSession = session;
            try {
                updatePreview(session);
            } catch (CameraAccessException e) {
                e.printStackTrace();
            }
        }

        @Override
        public void onConfigureFailed(CameraCaptureSession session) {

        }
    };

    private void updatePreview(CameraCaptureSession session) throws CameraAccessException {
        session.setRepeatingRequest(mPreviewBuilder.build(), null, mHandler);
    }

    private CameraCaptureSession mSession;
    private int mState;
    private CameraCaptureSession.CaptureCallback mCaptureCallback = new CameraCaptureSession.CaptureCallback() {
        @Override
        public void onCaptureProgressed(CameraCaptureSession session, CaptureRequest request, CaptureResult partialResult) {
            mSession = session;
            checkState(partialResult);
        }

        @Override
        public void onCaptureCompleted(CameraCaptureSession session, CaptureRequest request, TotalCaptureResult result) {
            mSession = session;
            checkState(result);
        }

        private void checkState(CaptureResult result) {
            switch (mState) {
                case STATE_PREVIEW:
                    break;
                case STATE_WAITING_CAPTURE:
                    int afState = result.get(CaptureResult.CONTROL_AF_STATE);
                    if (CaptureResult.CONTROL_AF_STATE_FOCUSED_LOCKED == afState ||
                            CaptureResult.CONTROL_AF_STATE_NOT_FOCUSED_LOCKED == afState
                            || CaptureResult.CONTROL_AF_STATE_PASSIVE_FOCUSED == afState
                            || CaptureResult.CONTROL_AF_STATE_PASSIVE_UNFOCUSED == afState) {
                        //do something like save picture

                    }
                    break;
            }
        }

    };


    //开始预览，主要是camera.createCaptureSession这段代码很重要，创建会话
    private void startPreview(CameraDevice camera) throws CameraAccessException {
        SurfaceTexture texture = mPreviewView.getSurfaceTexture();
        texture.setDefaultBufferSize(mPreviewSize.getWidth(), mPreviewSize.getHeight());
        Surface surface = new Surface(texture);
        try {
            mPreviewBuilder = camera.createCaptureRequest(CameraDevice.TEMPLATE_PREVIEW);
        } catch (CameraAccessException e) {
            e.printStackTrace();
        }
        mPreviewBuilder.addTarget(surface);
        camera.createCaptureSession(Arrays.asList(surface, mImageReader.getSurface()), mSessionStateCallback, mHandler);
    }

    private void initLooper() {
        mHandlerThread = new HandlerThread(CAMERA2);
        mHandlerThread.start();
        mHandler = new Handler(mHandlerThread.getLooper());
    }


    public void capture(View view) {

    }

    private CameraDevice.StateCallback mCameraDeviceStateCallback = new CameraDevice.StateCallback() {
        @Override
        public void onOpened(CameraDevice camera) {
            try {
                startPreview(camera);
            } catch (CameraAccessException e) {
                e.printStackTrace();
            }
        }

        @Override
        public void onDisconnected(CameraDevice camera) {

        }

        @Override
        public void onError(CameraDevice camera, int error) {

        }
    };

    @Override
    public void onSurfaceTextureAvailable(SurfaceTexture surface, int width, int height) {
        openCamera();
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        closeCamera();
    }

    private void closeCamera() {
        if (null != mSession) {
            mSession.close();
            mSession = null;
        }
        if (null != mCameraDevice) {
            mCameraDevice.close();
            mCameraDevice = null;
        }
        if (null != mImageReader) {
            mImageReader.close();
            mImageReader = null;
        }
    }

    private ImageReader.OnImageAvailableListener mOnImageAvailableListener = new ImageReader.OnImageAvailableListener() {
        @Override
        public void onImageAvailable(ImageReader reader) {
            mHandler.post(new ImageSaver(reader.acquireNextImage(), new File(filePath)));
        }
    };

    private void openCamera() {
        mCameraManager = (CameraManager) getSystemService(CAMERA_SERVICE);
        try {
            CameraCharacteristics characteristics = mCameraManager.getCameraCharacteristics("0");
            StreamConfigurationMap map = characteristics.get(CameraCharacteristics.SCALER_STREAM_CONFIGURATION_MAP);
            mPreviewSize = map.getOutputSizes(SurfaceTexture.class)[0];
            mImageReader = ImageReader.newInstance(mPreviewSize.getWidth(), mPreviewSize.getHeight(), ImageFormat.JPEG
                    , 2);
            mImageReader.setOnImageAvailableListener(mOnImageAvailableListener, mHandler);
            boolean b = PermissionUtil.checkPermission(this, Manifest.permission.CAMERA);
            if (b)
                mCameraManager.openCamera("0", mCameraDeviceStateCallback, mHandler);
            else PermissionUtil.requestPermission(this, Manifest.permission.CAMERA, "相机");
        } catch (CameraAccessException e) {
            e.printStackTrace();
        }
    }

    @Override
    public void onSurfaceTextureSizeChanged(SurfaceTexture surface, int width, int height) {

    }

    @Override
    public boolean onSurfaceTextureDestroyed(SurfaceTexture surface) {
        return true;
    }

    @Override
    public void onSurfaceTextureUpdated(SurfaceTexture surface) {

    }

    @Override
    public void onClick(View v) {
        if (v.getId() == R.id.texture_view) {
            try {
                Log.i("linc", "take picture");
                mState = STATE_WAITING_CAPTURE;
                mSession.setRepeatingRequest(mPreviewBuilder.build(), mCaptureCallback, mHandler);
            } catch (CameraAccessException e) {
                e.printStackTrace();
            }
        }
    }

    /**
     * Saves a JPEG {@link Image} into the specified {@link File}.
     */
    private static class ImageSaver implements Runnable {

        /**
         * The JPEG image
         */
        private final Image mImage;
        /**
         * The file we save the image into.
         */
        private final File mFile;

        public ImageSaver(Image image, File file) {
            mImage = image;
            mFile = file;
        }

        @Override
        public void run() {
            ByteBuffer buffer = mImage.getPlanes()[0].getBuffer();
            byte[] bytes = new byte[buffer.remaining()];
            buffer.get(bytes);
            FileOutputStream output = null;
            try {
                output = new FileOutputStream(mFile);
                output.write(bytes);
            } catch (IOException e) {
                e.printStackTrace();
            } finally {
                mImage.close();
                if (null != output) {
                    try {
                        output.close();
                    } catch (IOException e) {
                        e.printStackTrace();
                    }
                }
            }
        }

    }

}
