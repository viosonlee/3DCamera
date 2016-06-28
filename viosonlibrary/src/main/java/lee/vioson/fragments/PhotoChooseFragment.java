/**
 * author 胡俊杰 Todo
 */
package lee.vioson.fragments;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.database.Cursor;
import android.graphics.Bitmap;
import android.net.Uri;
import android.provider.MediaStore;
import android.support.v4.app.Fragment;
import android.text.TextUtils;


import java.io.File;
import java.io.IOException;

import lee.vioson.utils.BitmapCompress;
import lee.vioson.utils.DebugLog;
import lee.vioson.utils.PicturePickerUtil;
import lee.vioson.utils.ScreenUtil;


/**
 * 打开相册或者相机，保存图片
 * <p/>
 * author Administrator
 */
public abstract class PhotoChooseFragment extends Fragment {
    // protected String mFilePath;
    protected final static int CODE_REQUEST_CAMER = 100;    // 打开相机请求码
    protected final static int CODE_REQUEST_GALLERY = 101;    // 打开相册请求码
    protected final static int CODE_CROP_PHOTO = 102;    // 裁切图片
    protected String cropCachePath;                    // 切割图片
    protected int outputX = 400;
    protected int outputY = 400;

    String fileString;

    public PhotoChooseFragment() {

    }

    public abstract void chooseSuccess(String filePath);

    /**
     * 是否裁切图片
     *
     * @return true 裁切
     */
    public abstract boolean toCropPhoto();

    public abstract String getCameraPath();

    /**
     * 裁切保存图片地址
     */
    protected abstract String getCropCacheFilePath();

    @Override
    public void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (resultCode != Activity.RESULT_OK) {
            return;
        }
        switch (requestCode) {
            case CODE_REQUEST_CAMER:// 打开相机
                // 是否裁切
                if (toCropPhoto()) {
                    File file = new File(getCameraPath());
                    startPhotoZoom(Uri.fromFile(file), outputX, outputY);
                } else {
                    try {
                        fileString = getFilePath(getActivity(),fileString);
                        chooseSuccess(fileString);
                    } catch (IOException e) {
                        e.printStackTrace();
                    }
                }
                break;
            case CODE_REQUEST_GALLERY:// 打开相册
                if (toCropPhoto()) {
                    startPhotoZoom(data.getData(), outputX, outputY);
                } else {
//                    chooseSuccess(convertFileUri(getActivity(), data.getData()));

                    //修改 2016年2月15日 胡俊杰
                    String filePath;
                    if(android.os.Build.VERSION.SDK_INT>=android.os.Build.VERSION_CODES.KITKAT){
                        try {
                            filePath = getFilePath(getActivity(), PicturePickerUtil.getPath(getContext(), data));
                            chooseSuccess(filePath);
                        } catch (IOException e) {
                            e.printStackTrace();
                        }
//                        chooseSuccess(PicturePickerUtil.getPath(getContext(), data));
                    }else {
                        try {
                            filePath = getFilePath(getActivity(),PicturePickerUtil.selectImage(getContext(), data));
                            chooseSuccess(filePath);
                        } catch (IOException e) {
                            e.printStackTrace();
                        }
//                        chooseSuccess(PicturePickerUtil.selectImage(getContext(), data));
                    }
                }

                break;
            case CODE_CROP_PHOTO:
                chooseSuccess(cropCachePath);
                break;
        }
    }


    /**
     * 打开相机
     */
    @SuppressWarnings("ResultOfMethodCallIgnored")
    public void getPicFromCamera() {
        fileString = getCameraPath();
        File takePhotoFile = new File(fileString);
        if (!takePhotoFile.getParentFile().exists()) {
            takePhotoFile.getParentFile().mkdirs();
        }
        Intent intent = new Intent(MediaStore.ACTION_IMAGE_CAPTURE);
        intent.putExtra(MediaStore.EXTRA_OUTPUT, Uri.fromFile(takePhotoFile));
        startActivityForResult(intent, CODE_REQUEST_CAMER);
    }

    /**
     * 打开相册
     */
    public void getPicFromContent() {
        Intent intent = new Intent();
        intent.setType("image/*");
        intent.setAction(Intent.ACTION_PICK);
//        startActivityForResult(intent, CODE_REQUEST_GALLERY);

        //修改 2016年2月15日 胡俊杰
//        intent.setAction(Intent.ACTION_GET_CONTENT);
        startActivityForResult(intent, CODE_REQUEST_GALLERY);
    }

    /**
     * uri解析本地路径
     *
     * @param mContext
     * @param uri
     * @return
     */
    private String convertFileUri(Context mContext, Uri uri) {
        if (uri != null && "content".equals(uri.getScheme())) {
            Cursor cursor = mContext
                    .getContentResolver()
                    .query(uri,
                            new String[]{MediaStore.Images.ImageColumns.DATA},
                            null, null, null);
            if (cursor != null && cursor.moveToFirst()) {
                String mFilePath = cursor.getString(0);
                cursor.close();
                return mFilePath;
            }
        }
        return "";
    }

    /**
     * 裁剪图片方法实现
     *
     * @param uri
     */
    public void startPhotoZoom(Uri uri, int outputX, int outputY) {

        Intent intent = new Intent("com.android.camera.action.CROP");
        intent.setDataAndType(uri, "image/*");
        // 下面这个crop=true是设置在开启的Intent中设置显示的VIEW可裁剪
        intent.putExtra("crop", "true");
        intent.putExtra("scale", true);
        // aspectX aspectY 是宽高的比例
        intent.putExtra("aspectX", 1);
        intent.putExtra("aspectY", 1);
        // outputX outputY 是裁剪图片宽高
        intent.putExtra("outputX", outputX);
        intent.putExtra("outputY", outputY);
        String cropPath = getCropCacheFilePath();
        if (TextUtils.isEmpty(cropPath)) {
            throw new NullPointerException("crop path is null");
        }
        intent.putExtra(MediaStore.EXTRA_OUTPUT,
                Uri.fromFile(new File(cropPath)));
        intent.putExtra("return-data", false);
        this.startActivityForResult(intent, CODE_CROP_PHOTO);

    }

    /**
     * @param context
     * @param sourcePath
     * @return
     * @throws IOException
     */
    public String getFilePath(Context context, String sourcePath) throws IOException {
        int degree = BitmapCompress.readPictureDegree(sourcePath);
        DebugLog.i("J.Log", "degree:" + degree);
        Bitmap bitmap = BitmapCompress.compressAndGetImgBitmap(sourcePath, ScreenUtil.getScreenWidth(context), ScreenUtil.getScreenHeight(context));
        @SuppressWarnings("ConstantConditions")
        String path = context.getExternalCacheDir().getPath() + "/" + System.currentTimeMillis() / 1000 + "" + (int) (Math.random() * 9000 + 1000) + ".jpg";
//        BitmapCompress.compressImageToSpecifySizeAndSaveFile(bitmap, 480, 800, path, 90,degree);
        BitmapCompress.compressImageToSpecifySizeAndSaveFile(sourcePath, path, 90, degree);
        bitmap.recycle();
        return path;
    }


}
