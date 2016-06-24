package lee.vioson.widget;

import android.app.ProgressDialog;
import android.content.Context;
import android.support.annotation.Nullable;
import android.support.v7.widget.GridLayoutManager;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.support.v7.widget.StaggeredGridLayoutManager;
import android.util.AttributeSet;

/**
 * Author:李烽
 * Date:2016-05-06
 * FIXME
 * Todo 上拉加载更多的 RecyclerView
 */
public class UpLoadRecyclerView extends RecyclerView {

    private static boolean isLoading = false;

    public void setHasMore(boolean hasMore) {
        this.hasMore = hasMore;
    }

    public boolean isHasMore() {
        return hasMore;
    }

    private boolean hasMore = true;


    public UpLoadRecyclerView(Context context) {
        this(context, null);
    }

    private ProgressDialog dialog;

    private String dialogMessage = "正在加载更多";

    public UpLoadRecyclerView(Context context, @Nullable AttributeSet attrs) {
        this(context, attrs, 0);
    }

    public UpLoadRecyclerView(Context context, @Nullable AttributeSet attrs, int defStyle) {
        super(context, attrs, defStyle);
        dialog = new ProgressDialog(getContext());
        dialog.setMessage(dialogMessage);
        this.addOnScrollListener(new RecyclerView.OnScrollListener() {
            boolean isSlidingToLast;
            LinearLayoutManager layoutManager;

            @Override
            public void onScrollStateChanged(RecyclerView recyclerView, int newState) {
                super.onScrollStateChanged(recyclerView, newState);
                if (onScrollListener != null)
                    onScrollListener.onScrollStateChanged(UpLoadRecyclerView.this, newState);
//                try {
                //当不滚动的时候
                if (newState == RecyclerView.SCROLL_STATE_IDLE) {
                    int lastCompletelyVisibleItemPosition = getLastVisiblePosition();
                    int itemCount = getAdapter().getItemCount();
                    if (lastCompletelyVisibleItemPosition == (itemCount - 1) && isSlidingToLast) {
                        if (onLoadMoreCallBack != null)
                            if (!isLoading && hasMore) {
                                onLoadMoreCallBack.onLoadMore();
                                isLoading = true;
                                dialog.show();
                            }
                    }
                }
            }

            @Override
            public void onScrolled(RecyclerView recyclerView, int dx, int dy) {
                if (onScrollListener != null)
                    onScrollListener.onScrolled(UpLoadRecyclerView.this, dx, dy);
                super.onScrolled(recyclerView, dx, dy);
                if (dy > 0) {
                    isSlidingToLast = true;
                } else {
                    isSlidingToLast = false;
                }
            }
        });
    }

    public void setOnScrollListener(OnScrollListener onScrollListener) {
        this.onScrollListener = onScrollListener;
    }

    private OnScrollListener onScrollListener;

    public interface OnScrollListener {
        void onScrollStateChanged(UpLoadRecyclerView recyclerView, int newState);

        void onScrolled(RecyclerView recyclerView, int dx, int dy);
    }

    public void setOnLoadMoreCallBack(OnLoadMoreCallBack onLoadMoreCallBack) {
        this.onLoadMoreCallBack = onLoadMoreCallBack;
    }

    private OnLoadMoreCallBack onLoadMoreCallBack;

    public void loadComplete() {
        isLoading = false;
        dialog.dismiss();
    }

    public interface OnLoadMoreCallBack {

        void onLoadMore();

    }

    /**
     * 获取第一条展示的位置
     *
     * @return
     */
    public int getFirstVisiblePosition() {
        int position;
        if (getLayoutManager() instanceof LinearLayoutManager) {
            position = ((LinearLayoutManager) getLayoutManager()).findFirstVisibleItemPosition();
        } else if (getLayoutManager() instanceof GridLayoutManager) {
            position = ((GridLayoutManager) getLayoutManager()).findFirstVisibleItemPosition();
        } else if (getLayoutManager() instanceof StaggeredGridLayoutManager) {
            StaggeredGridLayoutManager layoutManager = (StaggeredGridLayoutManager) getLayoutManager();
            int[] lastPositions = layoutManager.findFirstVisibleItemPositions(new int[layoutManager.getSpanCount()]);
            position = getMinPositions(lastPositions);
        } else {
            position = 0;
        }
        return position;
    }

    /**
     * 获得当前展示最小的position
     *
     * @param positions
     * @return
     */
    private int getMinPositions(int[] positions) {
        int size = positions.length;
        int minPosition = Integer.MAX_VALUE;
        for (int i = 0; i < size; i++) {
            minPosition = Math.min(minPosition, positions[i]);
        }
        return minPosition;
    }

    /**
     * 获取最后一条展示的位置
     *
     * @return
     */
    public int getLastVisiblePosition() {
        int position;
        if (getLayoutManager() instanceof LinearLayoutManager) {
            position = ((LinearLayoutManager) getLayoutManager()).findLastCompletelyVisibleItemPosition();
        } else if (getLayoutManager() instanceof GridLayoutManager) {
            position = ((GridLayoutManager) getLayoutManager()).findLastCompletelyVisibleItemPosition();
        } else if (getLayoutManager() instanceof StaggeredGridLayoutManager) {
            StaggeredGridLayoutManager layoutManager = (StaggeredGridLayoutManager) getLayoutManager();
            int[] lastPositions = layoutManager.findLastCompletelyVisibleItemPositions(new int[layoutManager.getSpanCount()]);
            position = getMaxPosition(lastPositions);
        } else {
            position = getLayoutManager().getItemCount() - 1;
        }
        return position;
    }

    /**
     * 获得最大的位置
     *
     * @param positions
     * @return
     */
    private int getMaxPosition(int[] positions) {
        int size = positions.length;
        int maxPosition = Integer.MIN_VALUE;
        for (int i = 0; i < size; i++) {
            maxPosition = Math.max(maxPosition, positions[i]);
        }
        return maxPosition;
    }


    /**
     * 切换layoutManager
     * <p/>
     * 为了保证切换之后页面上还是停留在当前展示的位置，记录下切换之前的第一条展示位置，切换完成之后滚动到该位置
     * 另外切换之后必须要重新刷新下当前已经缓存的itemView，否则会出现布局错乱（俩种模式下的item布局不同），
     * RecyclerView提供了swapAdapter来进行切换adapter并清理老的itemView cache
     *
     * @param layoutManager
     */
    public void switchLayoutManager(LayoutManager layoutManager, RecyclerView.Adapter adapter) {
        int firstVisiblePosition = getFirstVisiblePosition();
        getLayoutManager().removeAllViews();
        setLayoutManager(layoutManager);
        super.swapAdapter(adapter, true);
        getLayoutManager().scrollToPosition(firstVisiblePosition);
    }

}
