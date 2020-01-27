package com.txt.flow;

import android.content.Context;
import android.util.AttributeSet;
import android.view.MotionEvent;
import android.view.VelocityTracker;

import java.util.Random;

public class KeywordsFlowView extends KeywordsFlow {
    public static final int SNAP_VELOCITY = 50;   // 手势滑动
    float yMove;
    private float yDown;
    private String[] words;
    private boolean shouldScrollFlow = true;
    private VelocityTracker mVelocityTracker;  // 用于计算手指滑动的速度。

    public KeywordsFlowView(Context context, AttributeSet attrs, int defStyle) {
        super(context, attrs, defStyle);
    }

    public KeywordsFlowView(Context context, AttributeSet attrs) {
        super(context, attrs);
    }

    public KeywordsFlowView(Context context) {
        super(context);
    }

    private static void feedKeywordsFlow(KeywordsFlow keywordsFlow, String[] words) {
        Random random = new Random();
        for (int i = 0; i < KeywordsFlow.MAX; i++) {
            int ran = random.nextInt(words.length);
            String tmp = words[ran];
            keywordsFlow.feedKeyword(tmp);
        }
    }

    public void setWords(String[] words) {
        this.words = words;
    }

    public void show(String[] words, int animType) {
        setWords(words);
        rubKeywords();
        feedKeywordsFlow(this, words);
        go2Show(animType);
    }

    public void setTextShowSize(int size) {
        MAX = size;
    }

    public void shouldScrollFlow(boolean shouldScrollFlow) {
        this.shouldScrollFlow = shouldScrollFlow;
    }

    @Override
    public boolean performClick() {
        return super.performClick();
    }

    @Override
    public boolean onTouchEvent(MotionEvent event) {
        if (!shouldScrollFlow) {
            return super.onTouchEvent(event);
        }
        createVelocityTracker(event);
        switch (event.getAction()) {
            case MotionEvent.ACTION_DOWN:
                performClick();
                yDown = event.getRawY();
                break;
            case MotionEvent.ACTION_MOVE:
                performClick();
                yMove = event.getRawY();
                break;
            case MotionEvent.ACTION_UP:
                performClick();
                float yUp = event.getRawY();
                float distance = yUp - yDown;
                if (distance < -100 && getScrollVelocity() > SNAP_VELOCITY) {
                    rubKeywords();
                    feedKeywordsFlow(this, words);
                    go2Show(KeywordsFlow.ANIMATION_OUT);
                } else if (distance > 30 && getScrollVelocity() > SNAP_VELOCITY) {
                    rubKeywords();
                    feedKeywordsFlow(this, words);
                    go2Show(KeywordsFlow.ANIMATION_IN);
                }
                break;
        }
        return true;
    }

    private void createVelocityTracker(MotionEvent event) {
        if (mVelocityTracker == null) {
            mVelocityTracker = VelocityTracker.obtain();
        }
        mVelocityTracker.addMovement(event);
    }

    private int getScrollVelocity() {
        mVelocityTracker.computeCurrentVelocity(1000);
        int velocity = (int) mVelocityTracker.getXVelocity();
        return Math.abs(velocity);
    }
}
