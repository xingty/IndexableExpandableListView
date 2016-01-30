/*******************************************************************************
 * Copyright 2015-2016 xing
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
    private boolean mIsShowPreview = true;
    /**
     * 索引栏和预览的背景颜色
     */
    private int mBarBackground = Color.BLACK;
    private int mTextColor = Color.WHITE;
    /**
     * 索引栏和预览的透明度,当透明度为0时,预览将自动隐藏
     */
    private float mBarAlpha = 0.5f;
    private float mBarPadding ;
    private float mBarMargin ;
    private float mTextSize ; //可能失效,当屏幕空间装不下所有的字,就会改变字体大小,这个属性失效
    private float mLeading ;

    private String[] mSections ;
    private IndexBar bar ;

    private Paint mBarTextPaint ;
    private Paint mPreviewTextPaint ;
    private Paint mBarBgPaint ;
    private Paint mPreviewBgPaint ;

    private boolean mIndexable ;


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
        /* 预览框背景用的画笔 */
        float defaultSize = getResources().getDimension(R.dimen.preview_text_size_default) ;
        if (mPreviewBgPaint == null) {
            mPreviewBgPaint = new Paint() ;
        }
        mPreviewBgPaint.setAntiAlias(true);
        mPreviewBgPaint.setColor(mBarBackground);
        mPreviewBgPaint.setTextSize(defaultSize);
        mPreviewBgPaint.setAlpha((int) (mBarAlpha * 255));

        /* 预览框内字体 */
        if (mPreviewTextPaint == null) {
            mPreviewTextPaint = new Paint() ;
        }
        mPreviewTextPaint.setAlpha((int) (mBarAlpha * 255));
        mPreviewTextPaint.setColor(mTextColor);
        mPreviewTextPaint.setTextSize(defaultSize);

        /* 索引栏内文字画笔 */
        if (mBarTextPaint == null) {
            mBarTextPaint = new Paint(Paint.ANTI_ALIAS_FLAG) ;
        }
        mBarTextPaint.setColor(mTextColor);
        mBarTextPaint.setTextSize(mTextSize); //may be unavailable

        /* 索引栏背景画笔 */
        if (mBarBgPaint == null) {
            mBarBgPaint = new Paint(Paint.ANTI_ALIAS_FLAG) ;
        }
        mBarBgPaint.setColor(mBarBackground);
        mBarBgPaint.setAlpha((int) (mBarAlpha * 255));
    }

    private void initIndexBar(float listviewWidth,float listviewHeight) {
        bar = new IndexBar(listviewWidth,listviewHeight) ;
    }

    @Override
    protected void onMeasure(int widthMeasureSpec, int heightMeasureSpec) {
        super.onMeasure(widthMeasureSpec, heightMeasureSpec);
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
        if (bar == null) {
            //如果indexbar为空,mIndexable为true,就初始化index bar
            if (mIndexable) {
                initIndexBar(w,h);
            }
        } else {
            //如果存在bar又重新调用sizechange,那么有可能是转屏了
            bar = null ; //宽高改变,要创建新的bar
            initPaint();
            initIndexBar(w,h);
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
        mIndexable = true ;
        //initIndexBar(get,getHeight()); 可能还没measure完成,无法获取listview 宽高
    }

    private class IndexBar {
        private float width;
        private float height;
        private RectF rect;
        private RectF previewRect ;

        public IndexBar(float lwidth,float lHeight) {
            width = calculateIndexBarWidth() ;
            height = calculateIndexBarHeight(lHeight) ;
            initRect(lwidth,lHeight);
            initPreviewRect(lwidth,lHeight) ;
        }

        private void initRect(float listviewWidth,float listviewHeight) {
            float left = listviewWidth - width - mBarMargin;
            float top = (listviewHeight - height) / 2 ;
            rect = new RectF(left,top,left + width,top + height + mBarPadding) ;
        }

        private void initPreviewRect(float listviewWidth,float listviewHeight) {
            float size = mPreviewBgPaint.descent() - mPreviewBgPaint.ascent() ;
            float left = listviewWidth / 2 - size;
            float right = listviewWidth / 2 + size * 2;
            float top = listviewHeight / 2 - size;
            float bottom = listviewHeight / 2 + size * 2;
            previewRect = new RectF(left,top,right,bottom) ;
        }

        private float calculateIndexBarWidth() {
            if (mSections == null || mSections.length <= 0) {
                return 0 ;
            }

            float maxWidth = 0 ;
            for (String section : mSections) {
                float textWidth = mBarTextPaint.measureText(section) ;
                if (textWidth > maxWidth) {
                    maxWidth = textWidth ;
                }
            }

            return maxWidth + mBarPadding ;
        }

        private float calculateIndexBarHeight(float listviewHeight) {
            if (mSections == null || mSections.length <= 0) {
                return 0;
            }

            //屏幕可以为每个item分配的最大高度
            float maxHeight = listviewHeight / mSections.length ;
            //在用户设置的字体下,每个item需要占用的高度
            float textHeight = mBarTextPaint.descent() - mBarTextPaint.ascent() + mLeading;

            if (maxHeight < textHeight) {
                float fontSize = getTextSize(maxHeight, textHeight) ;
                mBarTextPaint.setTextSize(fontSize);
                //新字体大小的文字高度
                textHeight = mBarTextPaint.descent() - mBarTextPaint.ascent() + mLeading ;
            }

            return textHeight * mSections.length + mBarPadding ;
        }

        private float getTextSize(float maxHeight,final float textHeight) {
            final float sp = getResources().getDisplayMetrics().scaledDensity ;
            final int currentSize = (int) (mBarTextPaint.getTextSize() / sp);

            float measureHeight = textHeight ;
            Paint tempPaint = new Paint() ;

            for (int i=currentSize;i>0;i--) {
                tempPaint.setTextSize(i * sp);
                measureHeight = tempPaint.descent() - tempPaint.ascent() + mLeading ;

                if (measureHeight <= maxHeight) {
                    return i * sp ; //return text size
                }
            }

            return 10 ; //default text size
        }

        public boolean isTouchInside(float x, float y) {
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
            if (mBarBgPaint.getAlpha() >= 0) {
                canvas.drawRoundRect(rect, mTextSize / 5, mTextSize / 5, mBarBgPaint);
            }
            drawText(canvas);
        }

        /**
         * draw text on index bar
         * @param canvas 画布
         */
        public void drawText(Canvas canvas) {
            float textHeight = mBarTextPaint.descent() - mBarTextPaint.ascent() + mLeading;
            for (int i = 0; i < mSections.length; i++) {
                String text = mSections[i] ;
                //算出可用的范围
                float left = rect.left + (width - mBarTextPaint.measureText(text)) / 2;
                float top = textHeight * i + rect.top - mBarTextPaint.ascent() + mBarPadding  ;

                canvas.drawText(text,left,top, mBarTextPaint);
            }
        }

        /**
         * draw preview
         * @param canvas ExpandableListview's canvas
         */
        public void drawPreview(Canvas canvas) {
            if (mCurrentSection < 0 && mPreviewBgPaint.getAlpha() <= 0) {
                return ;

            }

            canvas.drawRoundRect(previewRect, 10, 10, mPreviewBgPaint);

            String text = mSections[mCurrentSection] ;
            float width = previewRect.right - previewRect.left ;
            float textHeight = mPreviewTextPaint.descent() - mPreviewTextPaint.ascent() ;
            float previewHeight = previewRect.bottom - previewRect.top ;
            float x = previewRect.left + (width - mPreviewTextPaint.measureText(text)) / 2;
            float y = previewRect.top + (previewHeight - textHeight) / 2 - mPreviewTextPaint.ascent();
            canvas.drawText(text, x, y, mPreviewTextPaint);
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
                float textHeight = mBarTextPaint.descent() - mBarTextPaint.ascent() + mLeading;
                float top = rect.top - mBarPadding - mBarTextPaint.ascent() ; //rect 顶部坐标
                int section = (int) ((y - top) / textHeight);

                //防止极端情况出现数组越界
                return Math.min(Math.max(0,section),mSections.length - 1);
            }
        }
    }


    public boolean isShowPreview() {
        return this.mIsShowPreview ;
    }

    public void isSHowPreview(boolean mIsShowPreview) {
        this.mIsShowPreview = mIsShowPreview ;
    }
}
