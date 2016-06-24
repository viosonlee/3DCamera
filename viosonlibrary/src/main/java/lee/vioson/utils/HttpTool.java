package lee.vioson.utils;

import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.os.Handler;
import android.os.Looper;
import android.util.Log;
import android.widget.ImageView;

import com.google.gson.Gson;
import com.google.gson.JsonSyntaxException;
import com.google.gson.internal.$Gson$Types;
import com.squareup.okhttp.Call;
import com.squareup.okhttp.Callback;
import com.squareup.okhttp.FormEncodingBuilder;
import com.squareup.okhttp.Headers;
import com.squareup.okhttp.MediaType;
import com.squareup.okhttp.MultipartBuilder;
import com.squareup.okhttp.OkHttpClient;
import com.squareup.okhttp.Request;
import com.squareup.okhttp.RequestBody;
import com.squareup.okhttp.Response;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.lang.reflect.ParameterizedType;
import java.lang.reflect.Type;
import java.net.CookieManager;
import java.net.CookiePolicy;
import java.net.FileNameMap;
import java.net.URLConnection;
import java.util.Map;
import java.util.Set;

/**
 * Author:李烽
 * Date:2016-04-29
 * FIXME
 * Todo
 */
public class HttpTool {
    private Gson gson;
    private OkHttpClient mOkHttpClient;
    private Handler mHandler;
    private static HttpTool instance;

    private HttpTool() {
        gson = new Gson();
        mOkHttpClient = new OkHttpClient();
        //cookie enabled
        mOkHttpClient.setCookieHandler(new CookieManager(null, CookiePolicy.ACCEPT_ORIGINAL_SERVER));
        mHandler = new Handler(Looper.getMainLooper());
    }

    public static synchronized HttpTool getInstance() {
        if (instance == null)
            synchronized (HttpTool.class) {
                if (instance == null)
                    instance = new HttpTool();
            }
        return instance;
    }

    /**
     * 同步的GET请求
     *
     * @param url
     * @return
     * @throws IOException
     */
    private Response _getAsyn(String url) throws IOException {
        final Request request = new Request.Builder().url(url).build();
        Call call = mOkHttpClient.newCall(request);
        Response response = call.execute();
        return response;
    }

    /**
     * 同步的GET请求
     *
     * @param url
     * @return
     * @throws IOException
     */
    private String _getAsynString(String url) throws IOException {
        Response response = _getAsyn(url);
        return response.body().string();
    }

    /**
     * 异步GET请求
     *
     * @param url
     * @param resultCallBak
     */
    private void _getAsyn(String url, final ResultCallBack resultCallBak) {
        final Request request = new Request.Builder().url(url)
                .build();
        deliveryResult(request, resultCallBak);
    }

    /**
     * 异步GET请求添加header
     *
     * @param url
     * @param resultCallBak
     * @param header
     */
    private void _getAsynWithHeader(String url, final ResultCallBack resultCallBak, Param[] header) {
        Request.Builder builder = new Request.Builder();
        builder.url(url);
        for (Param param : header) {
            builder.addHeader(param.key, param.value);
        }
        final Request request = builder.build();
        deliveryResult(request, resultCallBak);
    }

    /**
     * 同步POST请求
     *
     * @param url
     * @param params
     * @return
     * @throws IOException
     */
    private Response _postAsyn(String url, Param... params) throws IOException {
        Request request = buildPostRequset(url, params);
        Response response = mOkHttpClient.newCall(request).execute();
        return response;
    }

    private String _postAsynString(String url, Param... params) throws IOException {
        Response response = _postAsyn(url, params);
        return response.body().string();
    }

    /**
     * 异步的post请求
     *
     * @param url
     * @param resultCallBak
     * @param params
     */
    private void _postAsyn(String url, final ResultCallBack resultCallBak, Param... params) {
        Request request = buildPostRequset(url, params);
        deliveryResult(request, resultCallBak);
    }

    /**
     * 异步post请求
     *
     * @param url
     * @param resultCallBak
     * @param params
     */
    private void _postAsyn(String url, final ResultCallBack resultCallBak, Map<String, String> params) {
        Param[] params1 = map2Param(params);
        Request request = buildPostRequset(url, params1);
        deliveryResult(request, resultCallBak);
    }

    /**
     * 同步基于post的文件上传
     *
     * @param url
     * @param files
     * @param fileKeys
     * @param params
     * @return
     * @throws IOException
     */
    private Response _post(String url, File[] files, String[] fileKeys, Param... params) throws IOException {
        Request request = buildMultipartFormRequest(url, files, fileKeys, params);
        return mOkHttpClient.newCall(request).execute();
    }

    private Response _post(String url, File file, String fileKey) throws IOException {
        Request request = buildMultipartFormRequest(url, new File[]{file}, new String[]{fileKey}, null);
        return mOkHttpClient.newCall(request).execute();
    }

    private Response _post(String url, File file, String fileKey, Param... params) throws IOException {
        Request request = buildMultipartFormRequest(url, new File[]{file}, new String[]{fileKey}, params);
        return mOkHttpClient.newCall(request).execute();
    }


    /**
     * 异步基于post的文件上传
     *
     * @param url
     * @param callback
     * @param files
     * @param fileKeys
     * @throws IOException
     */
    private void _postAsyn(String url, ResultCallBack callback, File[] files, String[] fileKeys, Param... params) throws IOException {
        Request request = buildMultipartFormRequest(url, files, fileKeys, params);
        deliveryResult(request, callback);
    }

    /**
     * 异步基于post的文件上传，单文件不带参数上传
     *
     * @param url
     * @param callback
     * @param file
     * @param fileKey
     * @throws IOException
     */
    private void _postAsyn(String url, ResultCallBack callback, File file, String fileKey) throws IOException {
        Request request = buildMultipartFormRequest(url, new File[]{file}, new String[]{fileKey}, null);
        deliveryResult(request, callback);
    }

    /**
     * 异步基于post的文件上传，单文件且携带其他form参数上传
     *
     * @param url
     * @param callback
     * @param file
     * @param fileKey
     * @param params
     * @throws IOException
     */
    private void _postAsyn(String url, ResultCallBack callback, File file, String fileKey, Param... params) throws IOException {
        Request request = buildMultipartFormRequest(url, new File[]{file}, new String[]{fileKey}, params);
        deliveryResult(request, callback);
    }

    /**
     * 异步下载文件
     *
     * @param url
     * @param destFileDir 本地文件存储的文件夹
     * @param callback
     */
    private void _downloadAsyn(final String url, final String destFileDir, final ResultCallBack callback) {
        final Request request = new Request.Builder()
                .url(url)
                .build();
        final Call call = mOkHttpClient.newCall(request);
        call.enqueue(new Callback() {
            @Override
            public void onFailure(final Request request, final IOException e) {
                sendFailedStringCallback(request, e, callback);
            }

            @Override
            public void onResponse(Response response) {
                InputStream is = null;
                byte[] buf = new byte[2048];
                int len = 0;
                FileOutputStream fos = null;
                try {
                    is = response.body().byteStream();
                    File file = new File(destFileDir, getFileName(url));
                    fos = new FileOutputStream(file);
                    while ((len = is.read(buf)) != -1) {
                        fos.write(buf, 0, len);
                    }
                    fos.flush();
                    //如果下载文件成功，第一个参数为文件的绝对路径
                    sendSuccessResultCallback(file.getAbsolutePath(), callback);
                } catch (IOException e) {
                    sendFailedStringCallback(response.request(), e, callback);
                } finally {
                    try {
                        if (is != null) is.close();
                    } catch (IOException e) {
                    }
                    try {
                        if (fos != null) fos.close();
                    } catch (IOException e) {
                    }
                }

            }
        });
    }

    private String getFileName(String path) {
        int separatorIndex = path.lastIndexOf("/");
        return (separatorIndex < 0) ? path : path.substring(separatorIndex + 1, path.length());
    }


    /**
     * 加载图片
     *
     * @param view
     * @param url
     * @throws IOException
     */
    private void _displayImage(final ImageView view, final String url, final int errorResId) {
        final Request request = new Request.Builder()
                .url(url)
                .build();
        Call call = mOkHttpClient.newCall(request);
        call.enqueue(new Callback() {
            @Override
            public void onFailure(Request request, IOException e) {
                setErrorResId(view, errorResId);
            }

            @Override
            public void onResponse(Response response) {
                InputStream is = null;
                try {
                    is = response.body().byteStream();
                    ImageUtils.ImageSize actualImageSize = ImageUtils.getImageSize(is);
                    ImageUtils.ImageSize imageViewSize = ImageUtils.getImageViewSize(view);
                    int inSampleSize = ImageUtils.calculateInSampleSize(actualImageSize, imageViewSize);
                    try {
                        is.reset();
                    } catch (IOException e) {
                        response = _getAsyn(url);
                        is = response.body().byteStream();
                    }

                    BitmapFactory.Options ops = new BitmapFactory.Options();
                    ops.inJustDecodeBounds = false;
                    ops.inSampleSize = inSampleSize;
                    final Bitmap bm = BitmapFactory.decodeStream(is, null, ops);
                    mHandler.post(new Runnable() {
                        @Override
                        public void run() {
                            view.setImageBitmap(bm);
                        }
                    });
                } catch (Exception e) {
                    setErrorResId(view, errorResId);

                } finally {
                    if (is != null) try {
                        is.close();
                    } catch (IOException e) {
                        e.printStackTrace();
                    }
                }
            }
        });


    }


    //*****************************************公开的方法***************************************************/

    public static Response getAsyn(String url) throws IOException {
        return getInstance()._getAsyn(url);
    }

    public static String getAsynString(String url) throws IOException {
        return getInstance()._getAsynString(url);
    }

    public static void getAsyn(String url, ResultCallBack resultCallBack) {
        getInstance()._getAsyn(url, resultCallBack);
    }

    public static void getAsynWithHeader(String url, Map<String, String> headers, ResultCallBack callBack) {
        getInstance()._getAsynWithHeader(url, callBack, getInstance().map2Param(headers));
    }

    public static Response post(String url, Param... params) throws IOException {
        return getInstance()._postAsyn(url, params);
    }

    public static String postAsString(String url, Param... params) throws IOException {
        return getInstance()._postAsynString(url, params);
    }

    public static void postAsyn(String url, final ResultCallBack callback, Param... params) {
        getInstance()._postAsyn(url, callback, params);
    }


    public static void postAsyn(String url, final ResultCallBack callback, Map<String, String> params) {
        getInstance()._postAsyn(url, callback, params);
    }


    public static Response post(String url, File[] files, String[] fileKeys, Param... params) throws IOException {
        return getInstance()._post(url, files, fileKeys, params);
    }

    public static Response post(String url, File file, String fileKey) throws IOException {
        return getInstance()._post(url, file, fileKey);
    }

    public static Response post(String url, File file, String fileKey, Param... params) throws IOException {
        return getInstance()._post(url, file, fileKey, params);
    }

    public static void postAsyn(String url, ResultCallBack callback, File[] files, String[] fileKeys, Param... params) throws IOException {
        getInstance()._postAsyn(url, callback, files, fileKeys, params);
    }


    public static void postAsyn(String url, ResultCallBack callback, File file, String fileKey) throws IOException {
        getInstance()._postAsyn(url, callback, file, fileKey);
    }


    public static void postAsyn(String url, ResultCallBack callback, File file, String fileKey, Param... params) throws IOException {
        getInstance()._postAsyn(url, callback, file, fileKey, params);
    }

    public static void displayImage(final ImageView view, String url, int errorResId) throws IOException {
        getInstance()._displayImage(view, url, errorResId);
    }


    public static void displayImage(final ImageView view, String url) {
        getInstance()._displayImage(view, url, -1);
    }

    public static void downloadAsyn(String url, String destDir, ResultCallBack callback) {
        getInstance()._downloadAsyn(url, destDir, callback);
    }

    //**********************************************************************************************/
    private void setErrorResId(final ImageView view, final int errorResId) {
        mHandler.post(new Runnable() {
            @Override
            public void run() {
                view.setImageResource(errorResId);
            }
        });
    }


    ///********************************************************************************/

    /**
     * map 转 param
     *
     * @param params
     * @return
     */
    private Param[] map2Param(Map<String, String> params) {
        if (params == null)
            return new Param[0];
        int size = params.size();
        Param[] param = new Param[size];
        Set<Map.Entry<String, String>> entries = params.entrySet();
        int i = 0;
        for (Map.Entry<String, String> entry : entries) {
            param[i++] = new Param(entry.getKey(), entry.getValue());
        }
        return param;
    }

    private void deliveryResult(final Request request, final ResultCallBack resultCallBak) {
        mOkHttpClient.newCall(request).enqueue(new Callback() {
            @Override
            public void onFailure(Request request, IOException e) {
                sendFailedStringCallback(request, e, resultCallBak);
            }

            @Override
            public void onResponse(Response response) throws IOException {
                try {
                    String string = response.body().string();
                    Log.i("HttpTool", string);
                    if (resultCallBak.mType == String.class) {
                        sendSuccessResultCallback(string, resultCallBak);
                    } else {
                        Object o = gson.fromJson(string, resultCallBak.mType);
                        sendSuccessResultCallback(o, resultCallBak);
                    }
                } catch (IOException | JsonSyntaxException e) {
                    sendFailedStringCallback(request, e, resultCallBak);
                }
            }
        });
    }

    private void sendSuccessResultCallback(final Object o, final ResultCallBack resultCallBak) {
        mHandler.post(new Runnable() {
            @Override
            public void run() {
                if (resultCallBak != null)
                    resultCallBak.onResponse(o);
            }
        });
    }

    private void sendFailedStringCallback(final Request request, final Exception e, final ResultCallBack resultCallBak) {
        mHandler.post(new Runnable() {
            @Override
            public void run() {
                if (resultCallBak != null)
                    resultCallBak.onError(request, e);
            }
        });
    }

    private Request buildPostRequset(String url, Param[] params) {
        params = validateParam(params);
        FormEncodingBuilder builder = new FormEncodingBuilder();
        for (int i = 0; i < params.length; i++) {
            builder.add(params[i].key, params[i].value);
        }
        RequestBody body = builder.build();
        return new Request.Builder().url(url).post(body).build();
    }

    private Request buildMultipartFormRequest(String url, File[] files, String[] fileKeys, Param[] params) {
        params = validateParam(params);
        MultipartBuilder builder = new MultipartBuilder().type(MultipartBuilder.FORM);
        for (int i = 0; i < params.length; i++) {
            builder.addPart(Headers.of("Content-Disposition", "form-data; name=\"" + params[i].key + "\""),
                    RequestBody.create(null, params[i].value));
        }

        if (files != null) {
            RequestBody fileBody = null;
            for (int i = 0; i < files.length; i++) {
                File file = files[i];
                String fileName = file.getName();
                fileBody = RequestBody.create(MediaType.parse(guessMimeType(fileName)), file);
                // TODO: 2016/4/29 根据文件名设置contentType
                builder.addPart(Headers.of("Content-Disposition",
                        "form-data; name=\"" + fileKeys[i] + "\"; filename=\"" + fileName + "\""),
                        fileBody);
            }

        }
        RequestBody requestBody = builder.build();
        return new Request.Builder().url(url).post(requestBody).build();

    }

    private String guessMimeType(String fileName) {

        FileNameMap fileNameMap = URLConnection.getFileNameMap();
        String contentTypeFor = fileNameMap.getContentTypeFor(fileName);
        if (contentTypeFor == null)
            contentTypeFor = "application/octet-stream";
        return contentTypeFor;

    }

    private Param[] validateParam(Param[] params) {
        if (params == null)
            return new Param[0];
        else return params;
    }

    //********************************************内部类****************************************************/
    public static abstract class ResultCallBack<T> {
        private Type mType;
        private Type type;
        public static final String TAG="HttpTool.ResultCallback";

        public ResultCallBack() {
            this.mType = getType(getClass());
        }

        protected abstract void onError(Request request, Exception e);

        protected abstract void onResponse(T t);

        static Type getType(Class<?> aClass) {
            Type genericSuperclass = aClass.getGenericSuperclass();
            if (genericSuperclass instanceof Class) {
                throw new RuntimeException("Missing type parameter.");
            }
            ParameterizedType parameterizedType = (ParameterizedType) genericSuperclass;

            return $Gson$Types.canonicalize(parameterizedType.getActualTypeArguments()[0]);
        }
    }

    public class Param {
        public Param() {
        }

        String key, value;

        public Param(String key, String value) {
            this.key = key;
            this.value = value;
        }
    }
}
