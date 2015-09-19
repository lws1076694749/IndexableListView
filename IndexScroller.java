package com.lenovo.myapplication;

import android.content.Context;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.graphics.RectF;
import android.os.Handler;
import android.os.Message;
import android.os.SystemClock;
import android.view.MotionEvent;
import android.widget.Adapter;
import android.widget.ListView;
import android.widget.SectionIndexer;

/**
 * Created by lenovo on 2015/9/19.
 */
public class IndexScroller {
    private static final int STATE_HIDDEN = 1;
    private static final int STATE_SHOWING = 2;
    private static final int STATE_SHOWN = 3;
    private static final int STATE_HIDING = 4;

    private float mIndexbarWidth;       //索引条的宽度
    private float mIndexbarMargin;      //索引条距离右侧边缘的距离
    private float mPreviewPadding;      //在中心显示的预览文本到四周的距离
    private float mDensity;             //当前屏幕密度除以160
    private float mScaledDensity;       //当前屏幕密度除以160（字体尺寸）
    private float mAlphaRate;           //透明度（用于显示和隐藏索引条）0...1
    private int mState = STATE_HIDDEN;  //索引条的状态
    private int mListViewWidth;
    private int mListViewHeight;
    private int mCurrentSection = -1;
    private boolean mIsIndexing = false;
    private ListView mListView = null;
    private SectionIndexer mIndexer = null;
    private String[] mSections = null;
    private RectF mIndexbarRect;

    private Handler mHandler = new Handler() {
        @Override
        public void handleMessage(Message msg) {
            super.handleMessage(msg);
            switch (mState) {
                case STATE_HIDDEN:
                    mHandler.removeMessages(0);
                    break;
                case STATE_SHOWING:
                    mAlphaRate += (1 - mAlphaRate) * 0.2;
                    if (mAlphaRate > 0.9) {
                        mAlphaRate = 1;
                        setState(STATE_SHOWN);
                    }
                    mListView.invalidate();
                    fade(10);
                    break;
                case STATE_SHOWN:
                    setState(STATE_HIDING);
                    break;
                case STATE_HIDING:
                    mAlphaRate -= mAlphaRate * 0.2;
                    if (mAlphaRate < 0.1) {
                        mAlphaRate = 0;
                        setState(STATE_HIDDEN);
                    }
                    mListView.invalidate();
                    fade(10);
                    break;
            }
        }
    };

    //索引条初始化与尺寸本地化
    public IndexScroller(Context context, ListView listView) {
        //获取屏幕密度的比值
        mDensity = context.getResources().getDisplayMetrics().density;
        mScaledDensity = context.getResources().getDisplayMetrics().scaledDensity;
        mListView = listView;
        setAdapter(listView.getAdapter());
        //根据屏幕密度计算索引条的宽度、边距等（单位：像素）
        mIndexbarWidth = 20 * mDensity;
        mIndexbarMargin = 10 * mDensity;
        mPreviewPadding = 5 * mDensity;
    }

    public void setAdapter(Adapter adapter) {
        if (adapter instanceof SectionIndexer) {
            mIndexer = (SectionIndexer) adapter;
            mSections = (String[]) mIndexer.getSections();
        }
    }

    //绘制索引条和预览文本
    public void draw(Canvas canvas) {
        //1.绘制索引条的背景和文本
        //2.绘制预览的文本和背景
        if (mState == STATE_HIDDEN) {
            return;
        }
        //设置画笔属性
        Paint indexbarPaint = new Paint();
        indexbarPaint.setColor(Color.BLACK);
        indexbarPaint.setAlpha((int) (64 * mAlphaRate));
        //绘制索引条,圆角矩形
        canvas.drawRoundRect(mIndexbarRect, 5 * mDensity, 5 * mDensity, indexbarPaint);
        if (mSections != null && mSections.length > 0) {
            //绘制预览背景和文本
            if (mCurrentSection >= 0) {
                Paint previewPaint = new Paint();
                previewPaint.setColor(Color.BLACK);
                previewPaint.setAlpha(96);

                Paint previewTextPaint = new Paint();
                previewTextPaint.setColor(Color.WHITE);
                previewTextPaint.setTextSize(50 * mScaledDensity);
                float previewTextWidth = previewTextPaint.measureText(mSections[mCurrentSection]);
                //预览区域的宽度和高度（正方形）
                float previewSize = 2 * mPreviewPadding + previewTextPaint.descent() - previewTextPaint.ascent();
                RectF previewRect = new RectF(
                        (mListViewWidth - previewSize) / 2,
                        (mListViewHeight - previewSize) / 2,
                        (mListViewWidth + previewSize) / 2,
                        (mListViewHeight + previewSize) / 2);
                canvas.drawRoundRect(previewRect, 5 * mDensity, 5 * mDensity, previewPaint);
                canvas.drawText(mSections[mCurrentSection],
                        previewRect.left + (previewSize - previewTextWidth) / 2 - 1,
                        previewRect.top + mPreviewPadding - previewTextPaint.ascent() + 1,
                        previewTextPaint);
            }
            //绘制索引条
            Paint indexPaint = new Paint();
            indexPaint.setColor(Color.WHITE);
            indexPaint.setAlpha((int) (255 * mAlphaRate));
            indexPaint.setTextSize(12 * mScaledDensity);
            float sectionHeight = (mIndexbarRect.height() - 2 * mIndexbarMargin) / mSections.length;
            float paddingTop = (sectionHeight - (indexPaint.descent() - indexPaint.ascent())) / 2;
            for (int i = 0; i < mSections.length; i++) {
                float paddingLeft = (mIndexbarWidth - indexPaint.measureText(mSections[i])) / 2;
                canvas.drawText(mSections[i], mIndexbarRect.left + paddingLeft,
                        mIndexbarRect.top + mIndexbarMargin + sectionHeight * i + paddingTop - indexPaint.ascent(), indexPaint);
            }
        }
    }

    public void onSizeChanged(int w, int h, int oldw, int oldh) {
        mListViewWidth = w;
        mListViewHeight = h;
        mIndexbarRect = new RectF(w - mIndexbarMargin - mIndexbarWidth, mIndexbarMargin, w - mIndexbarMargin, h - mIndexbarMargin);
    }

    private void setState(int state) {
        if (mState < STATE_HIDDEN || mState > STATE_HIDING) {
            return;
        }
        mState = state;
        switch (mState) {
            case STATE_HIDDEN:
                mHandler.removeMessages(0);
                break;
            case STATE_SHOWING:
                mAlphaRate = 0;
                fade(0);
                break;
            case STATE_SHOWN:
                mHandler.removeMessages(0);
                break;
            case STATE_HIDING:
                mAlphaRate = 1;
                fade(3000);
                break;
        }
    }

    private void fade(long delay) {
        mHandler.removeMessages(0);
        mHandler.sendEmptyMessageAtTime(0, SystemClock.uptimeMillis() + delay);
    }

    //管理索引条的触摸事件
    public boolean onTouchEvent(MotionEvent ev) {
        switch (ev.getAction()) {
            case MotionEvent.ACTION_DOWN:
                if (mState != STATE_HIDDEN && contains(ev.getX(), ev.getY())) {
                    setState(STATE_SHOWN);

                    mIsIndexing = true;
                    mCurrentSection = getSectionByPoint(ev.getY());
                    mListView.setSelection(mIndexer.getPositionForSection(mCurrentSection));
                    return true;
                }
                break;
            case MotionEvent.ACTION_MOVE:
                if (mIsIndexing) {
                    if (contains(ev.getX(), ev.getY())) {
                        mCurrentSection = getSectionByPoint(ev.getY());
                        mListView.setSelection(mIndexer.getPositionForSection(mCurrentSection));
                    }
                    return true;
                }
                break;
            case MotionEvent.ACTION_UP:
                if (mIsIndexing) {
                    mIsIndexing = false;
                    mCurrentSection = -1;
                }
                if (mState == STATE_SHOWN) {
                    setState(STATE_HIDING);
                }
                break;
        }
        return false;
    }

    private int getSectionByPoint(float y) {
        float h = mIndexbarRect.height() / mSections.length;
        int index = (int) ((y - mIndexbarRect.top) / h);
        return index;
    }

    private boolean contains(float x, float y) {
        if (x > mIndexbarRect.left
                && x < mIndexbarRect.right
                && y > mIndexbarRect.top
                && y < mIndexbarRect.bottom)
            return true;
        return false;
    }

    public void hide() {
        setState(STATE_HIDDEN);
    }

    public void show() {
        setState(STATE_SHOWING);
    }
}
