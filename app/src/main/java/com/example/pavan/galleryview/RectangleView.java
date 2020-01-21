package com.example.pavan.galleryview;

import android.content.Context;
import android.util.AttributeSet;
import android.widget.FrameLayout;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;

public class RectangleView extends FrameLayout {

    @Override
    protected void onMeasure(int widthMeasureSpec, int heightMeasureSpec) {
        setMeasuredDimension(getMeasuredWidth(), (int) (getMeasuredWidth()*1.4f));
        super.onMeasure(widthMeasureSpec, heightMeasureSpec);
    }

    public RectangleView(@NonNull Context context, @Nullable AttributeSet attrs) {
        super(context, attrs);
    }
}
