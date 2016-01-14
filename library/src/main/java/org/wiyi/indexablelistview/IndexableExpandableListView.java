/*******************************************************************************
 * Copyright 2011-2014 Sergey Tarasevich
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 *
 * https://github.com/xingty/IndexableExpandableListView
 *
 *******************************************************************************/

package org.wiyi.indexablelistview;

import android.content.Context;
import android.content.res.Resources;
import android.content.res.TypedArray;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.graphics.RectF;
import android.util.AttributeSet;
import android.view.MotionEvent;
import android.widget.ExpandableListView;

/**
 * blog:http://wiyi.org
 * @author xing
 * 实现ExpandableListView使用快速索引功能
 */
public class IndexableExpandableListView extends ExpandableListView {
    private int mCurrentSection = -1;
    private boolean mIsIndexing ;

    /**
     * 是否显示中间的预览
     */
    private boolean mIsShowPreview ;
    /**
     * 索引栏和预览的背景颜色
     */
    private int mBarBackground ;
    /**
     * 索引栏和预览的透明度,当透明度为0时,预览将自动隐藏
     */
    private float mBarAlpha ;
    private float mBarPadding ;
    private float mBarMargin ;
    private float mTextSize ;
    private int mTextColor ;
    private float mLeading ;

    private String[] mSections ;
    private IndexBar bar ;
    private Paint mTextPaint ;
    private Paint mBgPaint ;

    public IndexableExpandableListView(Context context) {
        super(context);
    }

    public IndexableExpandableListView(Context context, AttributeSet attrs) {
        super(context, attrs);
        initAttrs(context, attrs) ;
    }

    private void initAttrs(Context context,AttributeSet attrs) {
        Resources res = getResources() ;
        mTextSize = res.getDimension(R.dimen.fontSize) ;
        mBarPadding = res.getDimension(R.dimen.barPadding) ;
        mBarMargin = res.getDimension(R.dimen.barMargin) ;
        mLeading = res.getDimension(R.dimen.textLeading) ;
        if (attrs == null) {
            mIsShowPreview = true ;
            mBarBackground = Color.BLACK ;
            mBarAlpha = 0.5f ;
            mTextColor = Color.WHITE ;
            return ;
        }

        TypedArray a = context.obtainStyledAttributes(attrs,R.styleable.IELV,0,0) ;
        try {
            mTextSize = a.getDimension(R.styleable.IELV_indexbarFontSize,mTextSize) ;
            mBarPadding = a.getDimension(R.styleable.IELV_indexbarPadding,mBarPadding) ;
            mBarMargin = a.getDimension(R.styleable.IELV_indexbarMargin,mBarMargin) ;
            mIsShowPreview = a.getBoolean(R.styleable.IELV_showPreview, true) ;
            mBarAlpha = a.getFloat(R.styleable.IELV_indexbarAlpha,0.5f) ;
            mBarBackground = a.getColor(R.styleable.IELV_indexbarBackground, Color.BLACK) ;
            mTextColor = a.getColor(R.styleable.IELV_indexbarTextColor,Color.WHITE) ;
        } finally {
            a.recycle();
        }
    }

    private void initPaint() {
        mTextPaint = new Paint() ;
        mTextPaint.setTextSize(mTextSize);
        mTextPaint.setColor(mTextColor);
        mTextPaint.setAntiAlias(true);

        mBgPaint = new Paint() ;
        mBgPaint.setAntiAlias(true);
        mBgPaint.setColor(mBarBackground);
        mBgPaint.setAlpha((int) (mBarAlpha * 255));
    }

    private void initIndexBar() {
        bar = new IndexBar(getWidth(),getHeight(),mTextPaint,mBgPaint) ;
    }

    @Override
    public boolean onTouchEvent(MotionEvent event) {
        switch (event.getAction()) {
            case MotionEvent.ACTION_DOWN : {
                float x = event.getX() ;
                float y = event.getY() ;

                if (bar != null && bar.isTouchInside(x,y)) {
                    mIsIndexing = true ;
                    mCurrentSection = bar.getSectionByPoint(event.getY()) ;
                    setSelectedGroup(mCurrentSection);
                    return true ;
                }

                break ;
            }

            case MotionEvent.ACTION_MOVE : {
                if (bar != null && mIsIndexing) {
                    mCurrentSection = bar.getSectionByPoint(event.getY()) ;

                    setSelectedGroup(mCurrentSection);

                    return true ;
                }
                break ;
            }

            case MotionEvent.ACTION_UP : {
                if (mIsIndexing) {
                    mIsIndexing = false ;
                    mCurrentSection = -1 ;
                    invalidate();
                    return true ;
                }

                break ;
            }
        }

        return super.onTouchEvent(event) ;
    }

    @Override
    protected void onSizeChanged(int w, int h, int oldw, int oldh) {
        super.onSizeChanged(w, h, oldw, oldh);
        if (bar != null) {
            bar = null ; //宽高改变,要创建新的bar
            initIndexBar();
        }
    }

    @Override
    public void draw(Canvas canvas) {
        super.draw(canvas);
        if (bar != null) {
            bar.draw(canvas);
        }
    }

    public void setIndexableAdapter(IndexableListAdapter adapter) {
        setAdapter(adapter);
        initPaint();
        mSections = adapter.getSections() ;
        initIndexBar();
    }

    private class IndexBar {
        private float width;
        private float height;
        private Paint bgPaint;
        private Paint textPaint ;
        private RectF rect;
        private RectF previewRect ;

        public IndexBar(float lwidth,float lHeight,Paint textPaint,Paint bgPaint) {
            this.textPaint = textPaint ;
            this.bgPaint = bgPaint ;

            width = calculateIndexBarWidth() ;
            height = calculateIndexBarHeight() ;
            initRect(lwidth,lHeight);
            initPreviewRect(lwidth,lHeight) ;
        }

        private void initRect(float listviewWidth,float listviewHeight) {
            float left = listviewWidth - width - mBarMargin;
            float top = (listviewHeight - height) / 2 ;
            rect = new RectF(left,top,left + width,top + height) ;
        }

        private void initPreviewRect(float listviewWidth,float listviewHeight) {
            float fontSize = textPaint.getTextSize() ;
            textPaint.setTextSize(20 * getResources().getDisplayMetrics().scaledDensity);

            float size = textPaint.descent() - textPaint.ascent() ;
            float left = listviewWidth / 2 - size;
            float right = listviewWidth / 2 + size * 2;
            float top = listviewHeight / 2 - size;
            float bottom = listviewHeight / 2 + size * 2;
            previewRect = new RectF(left,top,right,bottom) ;

            textPaint.setTextSize(fontSize);
        }

        private float calculateIndexBarWidth() {
            if (mSections == null || mSections.length <= 0) {
                return 0 ;
            }

            float maxWidth = 0 ;
            for (String section : mSections) {
                float textWidth = textPaint.measureText(section) ;
                if (textWidth > maxWidth) {
                    maxWidth = textWidth ;
                }
            }

            return maxWidth + mBarPadding ;
        }

        private float calculateIndexBarHeight() {
            if (mSections == null || mSections.length <= 0) {
                return 0;
            }

            float textHeight = textPaint.descent() - textPaint.ascent() + mLeading;

            return textHeight * mSections.length + mBarPadding ;
        }

        public boolean isTouchInside(float x, float y) {
            //稍微加大右边的点击范围
            boolean isInHorizontal = x >= rect.left && x <= rect.right ;
            boolean isInVertical = y >= rect.top && y <= rect.bottom ;

            return isInHorizontal && isInVertical ;
        }

        public void draw(Canvas canvas) {
            drawIndexBar(canvas);
            if (mIsShowPreview && mIsIndexing) {
                drawPreview(canvas);
            }
        }

        public void drawIndexBar(Canvas canvas) {
            //透明就不用绘制了
            if (bgPaint.getAlpha() >= 0) {
                canvas.drawRoundRect(rect, mTextSize / 5, mTextSize / 5, bgPaint);
            }
            drawText(canvas);
        }

        /**
         * draw text on index bar
         * @param canvas 画布
         */
        public void drawText(Canvas canvas) {
            float textHeight = textPaint.descent() - textPaint.ascent() + mLeading;
            for (int i = 0; i < mSections.length; i++) {
                String text = mSections[i] ;
                //算出可用的范围
                float left = rect.left + (width - textPaint.measureText(text)) / 2;
                float top = textHeight * i + rect.top - textPaint.ascent() + mBarPadding / 2 ;

                canvas.drawText(text,left,top, textPaint);
            }
        }

        /**
         * draw preview
         * @param canvas ExpandableListview's canvas
         */
        public void drawPreview(Canvas canvas) {
            if (mCurrentSection < 0 && bgPaint.getAlpha() <= 0) {
                return ;
            }

            canvas.drawRoundRect(previewRect, 10, 10, bgPaint);

            float textSize = textPaint.getTextSize() ;
            textPaint.setTextSize(22 * getResources().getDisplayMetrics().scaledDensity);

            String text = mSections[mCurrentSection] ;
            float width = previewRect.right - previewRect.left ;
            float textHeight = textPaint.descent() - textPaint.ascent() ;
            float previewHeight = previewRect.bottom - previewRect.top ;
            float x = previewRect.left + (width - textPaint.measureText(text)) / 2;
            float y = previewRect.top + (previewHeight - textHeight) / 2 - textPaint.ascent();
            canvas.drawText(text, x, y, textPaint);

            textPaint.setTextSize(textSize);
        }

        /**
         * 根据当前y坐标转换为position
         * @param y 当前y坐标
         * @return 返回当前坐标对应的position
         */
        public int getSectionByPoint(float y) {
            if (y <= rect.top - mBarPadding){
                return 0 ;
            } else if (y >= rect.bottom - mBarPadding) {
                return mSections.length - 1 ;
            } else {
                float textHeight = textPaint.descent() - textPaint.ascent() + mLeading;
                float top = rect.top - mBarPadding - textPaint.ascent() ; //rect 顶部坐标
                int section = (int) ((y - top) / textHeight);

                //防止极端情况出现数组越界
                return Math.min(Math.max(0,section),mSections.length - 1);
            }
        }
    }

    public Paint getTextPaint() {
        return mTextPaint ;
    }

    public void setTextPaint(Paint mTextPaint) {
        this.mTextPaint = mTextPaint ;
    }

    public Paint getBackgroundPaint() {
        return this.mBgPaint ;
    }

    public void setBackgroundPaint(Paint mBgPaint) {
        this.mBgPaint = mBgPaint ;
    }

    public boolean isShowPreview() {
        return this.mIsShowPreview ;
    }

    public void isSHowPreview(boolean mIsShowPreview) {
        this.mIsShowPreview = mIsShowPreview ;
    }
}
