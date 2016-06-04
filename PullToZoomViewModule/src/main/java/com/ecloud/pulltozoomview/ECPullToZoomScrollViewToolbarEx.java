package com.ecloud.pulltozoomview;

import android.content.Context;
import android.content.res.TypedArray;
import android.graphics.drawable.Drawable;
import android.os.Build;
import android.os.SystemClock;
import android.support.annotation.DrawableRes;
import android.util.AttributeSet;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.animation.Interpolator;
import android.widget.FrameLayout;
import android.widget.LinearLayout;
import android.widget.ScrollView;

/**
 * Author:    ZhuWenWu
 * Version    V1.0
 * Date:      2014/11/10  14:25.
 * Description:
 * Modification  History:
 * Date         	Author        		Version        	Description
 * -----------------------------------------------------------------------------------
 * 2014/11/10        ZhuWenWu            1.0                    1.0
 * Why & What is modified:
 * ////////////
 * Date:    2016/06/04 19:49 修改，增加对Toolbar和沉浸式状态栏的支持
 * <p/>
 * 非常感谢原作者 ZhuWenWu
 *
 * @author ZhuWenWu
 * @author EC
 */
public class ECPullToZoomScrollViewToolbarEx extends PullToZoomBase<ScrollView> {
    private static final String TAG = ECPullToZoomScrollViewToolbarEx.class.getSimpleName();
    private boolean isCustomHeaderHeight = false;
    private FrameLayout mHeaderContainer;
    private LinearLayout mRootContainer;
    private View mContentView;
    private int mHeaderHeight;
    private ScalingRunnable mScalingRunnable;
    ////
    private View mToolbar;
    //初始情况下的背景
    private Drawable mToolbarInitDrawable;
    //随着滚动条往上移动，最后变的颜色
    private Drawable mToolbarChangeDrawable;
    //Toobar padding top
    private float mToolbarPaddingTop = -1;
    //ToolbarHeight
    public int mToolbarHeight = -1;
    //自定义的滑动监听
    private OnScrollViewChangedOutSizeListener mOnScrollViewChangedOutSizeListener;

    private static final Interpolator sInterpolator = new Interpolator() {
        public float getInterpolation(float paramAnonymousFloat) {
            float f = paramAnonymousFloat - 1.0F;
            return 1.0F + f * (f * (f * (f * f)));
        }
    };

    public ECPullToZoomScrollViewToolbarEx(Context context) {
        this(context, null);
    }

    public ECPullToZoomScrollViewToolbarEx(Context context, AttributeSet attrs) {
        super(context, attrs);
        mScalingRunnable = new ScalingRunnable();
        ((InternalScrollView) mRootView).setOnScrollViewChangedListener(new OnScrollViewChangedListener() {

            @Override
            public void onInternalScrollChanged(int left, int top, int oldLeft, int oldTop) {
                if (isPullToZoomEnabled() && isParallax()) {
                    Log.d(TAG, "onScrollChanged --> getScrollY() = " + mRootView.getScrollY());
                    float f = mHeaderHeight - mHeaderContainer.getBottom() + mRootView.getScrollY();
                    Log.d(TAG, "onScrollChanged --> f = " + f);
                    if ((f > 0.0F) && (f < mHeaderHeight)) {
                        int i = (int) (0.65D * f);
                        mHeaderContainer.scrollTo(0, -i);
                    } else if (mHeaderContainer.getScrollY() != 0) {
                        mHeaderContainer.scrollTo(0, 0);
                    }
                }
                ///////////////////
                //Toolbar的处理在这里
                if (mToolbar != null) {
                    //固定高度，有些Toolbar设置时是wrap_content的，避免设置mToolbarChangeDrawable时，图片太大导致Toolbar被撑破
                    if (mToolbarHeight < 0) {
                        mToolbarHeight = mToolbar.getHeight();
                        //
                        ViewGroup.LayoutParams layoutParams = mToolbar.getLayoutParams();

                        layoutParams.height = mToolbarHeight;

                        mToolbar.setLayoutParams(layoutParams);
                    }
                    /////
                    //获取在整个屏幕内的绝对坐标
                    int[] location = new int[2];
                    mContentView.getLocationOnScreen(location);

                    int contentTop = location[1];
                    //
                    float toolbarPaddingTop = getToolbarPaddingTop();

                    int toolbarHeight = mToolbar.getHeight();

                    float v1 = toolbarPaddingTop + toolbarHeight;

                    if (contentTop <= v1) {
                        if (mToolbarChangeDrawable != null) {
                            mToolbar.setBackgroundDrawable(mToolbarChangeDrawable);
                        }
                    } else {
                        if (mToolbarInitDrawable != null) {
                            mToolbar.setBackgroundDrawable(mToolbarInitDrawable);
                        }
                    }

                }
                //
                if (mOnScrollViewChangedOutSizeListener != null) {
                    mOnScrollViewChangedOutSizeListener.onScrollChanged(left, top, oldLeft, oldTop);
                }
            }
        });
    }

    @Override
    protected void pullHeaderToZoom(int newScrollValue) {
        Log.d(TAG, "pullHeaderToZoom --> newScrollValue = " + newScrollValue);
        Log.d(TAG, "pullHeaderToZoom --> mHeaderHeight = " + mHeaderHeight);
        if (mScalingRunnable != null && !mScalingRunnable.isFinished()) {
            mScalingRunnable.abortAnimation();
        }

        ViewGroup.LayoutParams localLayoutParams = mHeaderContainer.getLayoutParams();
        localLayoutParams.height = Math.abs(newScrollValue) + mHeaderHeight;
        mHeaderContainer.setLayoutParams(localLayoutParams);

        if (isCustomHeaderHeight) {
            ViewGroup.LayoutParams zoomLayoutParams = mZoomView.getLayoutParams();
            zoomLayoutParams.height = Math.abs(newScrollValue) + mHeaderHeight;
            mZoomView.setLayoutParams(zoomLayoutParams);
        }
    }

    /**
     * 是否显示headerView
     *
     * @param isHideHeader true: show false: hide
     */
    @Override
    public void setHideHeader(boolean isHideHeader) {
        if (isHideHeader != isHideHeader() && mHeaderContainer != null) {
            super.setHideHeader(isHideHeader);
            if (isHideHeader) {
                mHeaderContainer.setVisibility(GONE);
            } else {
                mHeaderContainer.setVisibility(VISIBLE);
            }
        }
    }

    @Override
    public void setHeaderView(View headerView) {
        if (headerView != null) {
            mHeaderView = headerView;
            updateHeaderView();
        }
    }

    @Override
    public void setZoomView(View zoomView) {
        if (zoomView != null) {
            mZoomView = zoomView;
            updateHeaderView();
        }
    }

    private void updateHeaderView() {
        if (mHeaderContainer != null) {
            mHeaderContainer.removeAllViews();

            if (mZoomView != null) {
                mHeaderContainer.addView(mZoomView);
            }

            if (mHeaderView != null) {
                mHeaderContainer.addView(mHeaderView);
            }
        }
    }

    public void setScrollContentView(View contentView) {
        if (contentView != null) {
            if (mContentView != null) {
                mRootContainer.removeView(mContentView);
            }
            mContentView = contentView;
            mRootContainer.addView(mContentView);
        }
    }

    @Override
    protected ScrollView createRootView(Context context, AttributeSet attrs) {
        ScrollView scrollView = new InternalScrollView(context, attrs);
        scrollView.setId(R.id.scrollview);
        return scrollView;
    }

    @Override
    protected void smoothScrollToTop() {
        Log.d(TAG, "smoothScrollToTop --> ");
        mScalingRunnable.startAnimation(200L);
    }

    @Override
    protected boolean isReadyForPullStart() {
        return mRootView.getScrollY() == 0;
    }

    @Override
    public void handleStyledAttributes(TypedArray a) {
        mRootContainer = new LinearLayout(getContext());
        mRootContainer.setOrientation(LinearLayout.VERTICAL);
        mHeaderContainer = new FrameLayout(getContext());

        if (mZoomView != null) {
            mHeaderContainer.addView(mZoomView);
        }
        if (mHeaderView != null) {
            mHeaderContainer.addView(mHeaderView);
        }
        int contentViewResId = a.getResourceId(R.styleable.PullToZoomView_contentView, 0);
        if (contentViewResId > 0) {
            LayoutInflater mLayoutInflater = LayoutInflater.from(getContext());
            mContentView = mLayoutInflater.inflate(contentViewResId, null, false);
        }

        mRootContainer.addView(mHeaderContainer);
        if (mContentView != null) {
            mRootContainer.addView(mContentView);
        }

        mRootContainer.setClipChildren(false);
        mHeaderContainer.setClipChildren(false);

        mRootView.addView(mRootContainer);
    }

    /**
     * 设置HeaderView高度
     *
     * @param width
     * @param height
     */
    public void setHeaderViewSize(int width, int height) {
        if (mHeaderContainer != null) {
            Object localObject = mHeaderContainer.getLayoutParams();
            if (localObject == null) {
                localObject = new ViewGroup.LayoutParams(width, height);
            }
            ((ViewGroup.LayoutParams) localObject).width = width;
            ((ViewGroup.LayoutParams) localObject).height = height;
            mHeaderContainer.setLayoutParams((ViewGroup.LayoutParams) localObject);
            mHeaderHeight = height;
            isCustomHeaderHeight = true;
        }
    }

    /**
     * 设置HeaderView LayoutParams
     *
     * @param layoutParams LayoutParams
     */
    public void setHeaderLayoutParams(LinearLayout.LayoutParams layoutParams) {
        if (mHeaderContainer != null) {
            mHeaderContainer.setLayoutParams(layoutParams);
            mHeaderHeight = layoutParams.height;
            isCustomHeaderHeight = true;
        }
    }

    protected void onLayout(boolean paramBoolean, int paramInt1, int paramInt2,
                            int paramInt3, int paramInt4) {
        super.onLayout(paramBoolean, paramInt1, paramInt2, paramInt3, paramInt4);
        Log.d(TAG, "onLayout --> ");
        if (mHeaderHeight == 0 && mZoomView != null) {
            mHeaderHeight = mHeaderContainer.getHeight();
        }
    }

    class ScalingRunnable implements Runnable {
        protected long mDuration;
        protected boolean mIsFinished = true;
        protected float mScale;
        protected long mStartTime;

        ScalingRunnable() {
        }

        public void abortAnimation() {
            mIsFinished = true;
        }

        public boolean isFinished() {
            return mIsFinished;
        }

        public void run() {
            if (mZoomView != null) {
                float f2;
                ViewGroup.LayoutParams localLayoutParams;
                if ((!mIsFinished) && (mScale > 1.0D)) {
                    float f1 = ((float) SystemClock.currentThreadTimeMillis() - (float) mStartTime) / (float) mDuration;
                    f2 = mScale - (mScale - 1.0F) * ECPullToZoomScrollViewToolbarEx.sInterpolator.getInterpolation(f1);
                    localLayoutParams = mHeaderContainer.getLayoutParams();
                    Log.d(TAG, "ScalingRunnable --> f2 = " + f2);
                    if (f2 > 1.0F) {
                        localLayoutParams.height = ((int) (f2 * mHeaderHeight));
                        mHeaderContainer.setLayoutParams(localLayoutParams);
                        if (isCustomHeaderHeight) {
                            ViewGroup.LayoutParams zoomLayoutParams;
                            zoomLayoutParams = mZoomView.getLayoutParams();
                            zoomLayoutParams.height = ((int) (f2 * mHeaderHeight));
                            mZoomView.setLayoutParams(zoomLayoutParams);
                        }
                        post(this);
                        return;
                    }
                    mIsFinished = true;
                }
            }
        }

        public void startAnimation(long paramLong) {
            if (mZoomView != null) {
                mStartTime = SystemClock.currentThreadTimeMillis();
                mDuration = paramLong;
                mScale = ((float) (mHeaderContainer.getBottom()) / mHeaderHeight);
                mIsFinished = false;
                post(this);
            }
        }
    }

    /**
     * 设置Toolbar
     *
     * @return
     * @author EC
     */
    public void setToolbar(View view) {
        mToolbar = view;
    }

    /**
     * 设置Toolbar初始的背景
     *
     * @return
     * @author EC
     */
    public void setToolbarInitBackgroundResource(@DrawableRes int resid) {
        mToolbarInitDrawable = getResources().getDrawable(resid);
    }

    /**
     * 设置Toolbar初始的背景
     *
     * @return
     * @author EC
     */
    public void setToolbarInitBackgroundDrawable(Drawable drawable) {
        mToolbarInitDrawable = drawable;
    }

    /**
     * 设置Toolbar变化的背景
     *
     * @return
     * @author EC
     */
    public void setToolbarChangeBackgroundResource(@DrawableRes int resid) {
        mToolbarChangeDrawable = getResources().getDrawable(resid);
    }

    /**
     * 设置Toolbar变化的背景
     *
     * @return
     * @author EC
     */
    public void setToolbarChangeBackgroundDrawable(Drawable drawable) {
        mToolbarChangeDrawable = drawable;
    }

    /**
     * 获取Toolbar padding top，如果不设置，默认根据系统版本，取25或0
     *
     * @return
     * @author EC
     */
    public float getToolbarPaddingTop() {
        if (mToolbarPaddingTop >= 0) {
            return mToolbarPaddingTop;
        }
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.KITKAT) {
            mToolbarPaddingTop = 0;
        } else {
            mToolbarPaddingTop = 25;
        }
        return mToolbarPaddingTop;
    }

    /**
     * 设置Toolbar padding top
     *
     * @return
     * @author EC
     */
    public void setToolbarPaddingTop(float i) {
        mToolbarPaddingTop = i;
    }

    /**
     * 设置滑动监听
     *
     * @param listener
     * @author EC
     */
    public void setOnScrollViewChangedOutSideListener(OnScrollViewChangedOutSizeListener listener) {
        this.mOnScrollViewChangedOutSizeListener = listener;
    }

    protected class InternalScrollView extends ScrollView {
        private OnScrollViewChangedListener onScrollViewChangedListener;

        public InternalScrollView(Context context) {
            this(context, null);
        }

        public InternalScrollView(Context context, AttributeSet attrs) {
            super(context, attrs);
        }

        public void setOnScrollViewChangedListener(OnScrollViewChangedListener onScrollViewChangedListener) {
            this.onScrollViewChangedListener = onScrollViewChangedListener;
        }

        @Override
        protected void onScrollChanged(int l, int t, int oldl, int oldt) {
            super.onScrollChanged(l, t, oldl, oldt);
            if (onScrollViewChangedListener != null) {
                onScrollViewChangedListener.onInternalScrollChanged(l, t, oldl, oldt);
            }
        }
    }

    protected interface OnScrollViewChangedListener {
        void onInternalScrollChanged(int left, int top, int oldLeft, int oldTop);
    }

    public interface OnScrollViewChangedOutSizeListener {
        void onScrollChanged(int left, int top, int oldLeft, int oldTop);
    }
}
