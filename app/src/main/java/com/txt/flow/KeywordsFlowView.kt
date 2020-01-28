package com.txt.flow

import android.content.Context
import android.util.AttributeSet
import android.view.MotionEvent
import android.view.VelocityTracker
import java.util.*
import kotlin.math.abs

class KeywordsFlowView @JvmOverloads constructor(
    context: Context,
    attrs: AttributeSet,
    defStyleAttr: Int = 0
) : KeywordsFlow(context, attrs, defStyleAttr) {
    private var yMove = 0F
    private var yDown = 0F
    private lateinit var words: Array<String>
    private var shouldScrollFlow = true
    private var tracker: VelocityTracker? = null // 用于计算手指滑动的速度

    fun show(words: Array<String>, animType: Int) {
        this@KeywordsFlowView.words = words
        rubKeywords()
        feedKeywordsFlow(this, words)
        go2Show(animType)
    }

    fun setTextShowSize(size: Int) {
        MAX = size
    }

    fun shouldScrollFlow(shouldScrollFlow: Boolean) {
        this.shouldScrollFlow = shouldScrollFlow
    }

    override fun performClick(): Boolean {
        super.performClick()
        return true
    }

    override fun onTouchEvent(event: MotionEvent): Boolean {
        if (!shouldScrollFlow) return super.onTouchEvent(event)
        createVelocityTracker(event)
        when (event.action) {
            MotionEvent.ACTION_DOWN -> {
                performClick()
                yDown = event.rawY
            }
            MotionEvent.ACTION_MOVE -> {
                performClick()
                yMove = event.rawY
            }
            MotionEvent.ACTION_UP -> {
                performClick()
                val yUp = event.rawY
                val distance = yUp - yDown
                when {
                    distance < -100 && scrollVelocity > SNAP_VELOCITY -> {
                        rubKeywords()
                        feedKeywordsFlow(this, words)
                        go2Show(ANIMATION_OUT)
                    }
                    distance > 30 && scrollVelocity > SNAP_VELOCITY -> {
                        rubKeywords()
                        feedKeywordsFlow(this, words)
                        go2Show(ANIMATION_IN)
                    }
                }
            }
        }
        return true
    }

    private fun createVelocityTracker(event: MotionEvent) {
        if (tracker == null) tracker = VelocityTracker.obtain()
        tracker!!.addMovement(event)
    }

    private val scrollVelocity: Int
        get() {
            tracker!!.computeCurrentVelocity(1000)
            val velocity = tracker!!.xVelocity.toInt()
            return abs(velocity)
        }

    companion object {
        const val SNAP_VELOCITY = 50 // 手势滑动
        private fun feedKeywordsFlow(
            keywordsFlow: KeywordsFlow,
            words: Array<String>
        ) {
            val random = Random()
            for (i in 0 until MAX) {
                val ran = random.nextInt(words.size)
                val tmp = words[ran]
                keywordsFlow.feedKeyword(tmp)
            }
        }
    }
}