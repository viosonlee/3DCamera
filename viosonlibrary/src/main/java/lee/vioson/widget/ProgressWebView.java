package lee.vioson.widget;

import android.content.Context;
import android.graphics.drawable.Drawable;
import android.util.AttributeSet;
import android.util.Log;
import android.webkit.WebView;
import android.widget.ProgressBar;

import lee.vioson.viosonlibrary.R;


/**
 * Author: wanghao
 * Date: 2015-05-18
 * FIXME
 * Todo
 */
public class ProgressWebView extends WebView {

    Context c;

    public ProgressWebView(Context context) {
        super(context);
        this.c = context;
        initView();
    }

    private ProgressBar progressbar;

    public ProgressWebView(Context context, AttributeSet attrs) {
        super(context, attrs);
        this.c = context;
        initView();
    }

    void initView() {
        progressbar = new ProgressBar(c, null,
                android.R.attr.progressBarStyleHorizontal);
        progressbar.setLayoutParams(new LayoutParams(LayoutParams.MATCH_PARENT,
                6, 0, 0));

        Drawable drawable = c.getResources().getDrawable(R.drawable.progress_bar_states);
        progressbar.setProgressDrawable(drawable);
        addView(progressbar);
        setWebChromeClient(new WebChromeClient());
    }

    public class WebChromeClient extends android.webkit.WebChromeClient {

        @Override
        public void onProgressChanged(WebView view, int newProgress) {
            Log.d("onProgressChanged", "onProgressChanged  : " + newProgress + "==");

            if (newProgress == 100) {
                progressbar.setVisibility(GONE);
            } else {
                if (progressbar.getVisibility() == GONE)
                    progressbar.setVisibility(VISIBLE);
                progressbar.setProgress(newProgress);
            }

            super.onProgressChanged(view, newProgress);
        }
    }

    @Override
    protected void onScrollChanged(int l, int t, int oldl, int oldt) {
        LayoutParams lp = (LayoutParams) progressbar.getLayoutParams();
        lp.x = l;
        lp.y = t;
        progressbar.setLayoutParams(lp);
        super.onScrollChanged(l, t, oldl, oldt);
    }

}
