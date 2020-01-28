package com.txt.flow

import android.content.Context
import android.graphics.Paint
import android.util.AttributeSet
import android.util.TypedValue
import android.view.Gravity
import android.view.View
import android.view.ViewTreeObserver.OnGlobalLayoutListener
import android.view.animation.*
import android.view.animation.Animation.AnimationListener
import android.widget.FrameLayout
import android.widget.TextView
import java.util.*
import kotlin.math.abs
import kotlin.math.ceil

open class KeywordsFlow @JvmOverloads constructor(
    context: Context,
    attrs: AttributeSet,
    defStyleAttr: Int = 0
) : FrameLayout(context, attrs, defStyleAttr), OnGlobalLayoutListener {
    private var itemClickListener: OnClickListener? = null
    private var vecKeywords: Vector<String>? = null  // 存储显示的关键字
    private var width: Int? = 0
    private var height: Int? = 0

    /**
     * @see go2Show()中被赋值为true，标识开发人员触发其开始动画显示。
     * 本标识的作用是防止在填充关键词未完成的过程中获取到width和height后提前启动动画。
     * @see show()方法中其被赋值为false。
     * 真正能够动画显示的另一必要条件：
     * @see width  和
     * @see height 不为0
     */
    private var enableShow = false

    private var random: Random? = null
    /**
     * @see ANIMATION_IN
     * @see ANIMATION_OUT
     * @see OUTSIDE_TO_LOCATION
     * @see LOCATION_TO_OUTSIDE
     * @see LOCATION_TO_CENTER
     * @see CENTER_TO_LOCATION
     */
    private var txtAnimInType = 0
    private var txtAnimOutType = 0
    private var lastStartAnimationTime: Long = 0 // 最近一次启动动画显示的时间
    private var duration: Long = 0 // 动画运行时间

    init {
        lastStartAnimationTime = 0L
        duration = ANIM_DURATION
        random = Random()
        vecKeywords = Vector(MAX)
        viewTreeObserver.addOnGlobalLayoutListener(this)
        interpolator =
            AnimationUtils.loadInterpolator(context, android.R.anim.decelerate_interpolator)
        animAlpha2Opaque = AlphaAnimation(0.0f, 1.0f)
        animAlpha2Transparent = AlphaAnimation(1.0f, 0.0f)
        animScaleLarge2Normal = ScaleAnimation(2.0F, 1.0F, 2.0F, 1.0F)
        animScaleNormal2Large = ScaleAnimation(1.0F, 2.0F, 1.0F, 2.0F)
        animScaleZero2Normal = ScaleAnimation(0F, 1.0F, 0F, 1.0F)
        animScaleNormal2Zero = ScaleAnimation(1.0F, 0F, 1.0F, 0F)
    }

    fun feedKeyword(keyword: String) {
        if (vecKeywords!!.size < MAX) {
            vecKeywords!!.add(keyword)
        }
    }

    /**
     * 开始动画显示。
     * 之前已经存在的TextView将会显示退出动画。
     */
    fun go2Show(animType: Int) {
        if (System.currentTimeMillis() - lastStartAnimationTime > duration) {
            enableShow = true
            if (animType == ANIMATION_IN) {
                txtAnimInType = OUTSIDE_TO_LOCATION
                txtAnimOutType = LOCATION_TO_CENTER
            } else if (animType == ANIMATION_OUT) {
                txtAnimInType = CENTER_TO_LOCATION
                txtAnimOutType = LOCATION_TO_OUTSIDE
            }
            disappear()
            show()
        }
    }

    private fun disappear() {
        val size = childCount
        for (i in size - 1 downTo 0) {
            val txt = getChildAt(i) as TextView
            if (txt.visibility == View.GONE) {
                removeView(txt)
                continue
            }
            val layParams = txt.layoutParams as LayoutParams
            val xy = intArrayOf(layParams.leftMargin, layParams.topMargin, txt.width)
            val animSet = getAnimationSet(xy, width!! shr 1, height!! shr 1, txtAnimOutType)
            txt.startAnimation(animSet)
            animSet.setAnimationListener(object : AnimationListener {
                override fun onAnimationStart(animation: Animation) {}
                override fun onAnimationRepeat(animation: Animation) {}
                override fun onAnimationEnd(animation: Animation) {
                    txt.setOnClickListener(null)
                    txt.isClickable = false
                    txt.visibility = View.GONE
                }
            })
        }
    }

    private fun show(): Boolean {
        if (width!! > 0 && height!! > 0 && vecKeywords != null && vecKeywords!!.size > 0 && enableShow) {
            enableShow = false
            lastStartAnimationTime = System.currentTimeMillis()
            // 找到中心点
            val xCenter = width!! shr 1
            val yCenter = height!! shr 1
            // 关键字的个数。
            val size = vecKeywords!!.size
            val xItem = width!! / size
            val yItem = height!! / size
            val listX = LinkedList<Int>()
            val listY = LinkedList<Int>()
            for (i in 0 until size) { // 准备随机候选数，分别对应x/y轴位置
                listX.add(i * xItem)
                listY.add(i * yItem + (yItem shr 2))
            }
            val listTxtTop = LinkedList<TextView>()
            val listTxtBottom = LinkedList<TextView>()
            for (i in 0 until size) {
                val keyword = vecKeywords!![i]
                // 随机颜色
                val ranColor = -0x1000000 or random!!.nextInt(0x0077ffff)
                // 随机位置，糙值
                val xy = randomXY(random, listX, listY)
                // 随机字体大小
                val txtSize =
                    TEXT_SIZE_MIN + random!!.nextInt(TEXT_SIZE_MAX - TEXT_SIZE_MIN + 1)
                // 实例化TextView
                val txt = TextView(context)
                txt.setOnClickListener(itemClickListener)
                txt.text = keyword
                txt.setTextColor(ranColor)
                txt.setTextSize(TypedValue.COMPLEX_UNIT_SP, txtSize.toFloat())
                txt.setShadowLayer(1f, 1f, 1f, -0x22969697)
                txt.gravity = Gravity.CENTER
                // 获取文本长度
                val paint: Paint = txt.paint
                val strWidth = ceil(paint.measureText(keyword).toDouble()).toInt()
                xy[IDX_TXT_LENGTH] = strWidth
                // 第一次修正:修正x坐标
                if (xy[IDX_X] + strWidth > width!! - (xItem shr 1)) {
                    val baseX = width!! - strWidth
                    // 减少文本右边缘一样的概率
                    xy[IDX_X] =
                        baseX - xItem + random!!.nextInt(xItem shr 1)
                } else if (xy[IDX_X] == 0) { // 减少文本左边缘一样的概率
                    xy[IDX_X] =
                        random!!.nextInt(xItem).coerceAtLeast(xItem / 3)
                }
                xy[IDX_DIS_Y] =
                    abs(xy[IDX_Y] - yCenter)
                txt.tag = xy
                if (xy[IDX_Y] > yCenter) {
                    listTxtBottom.add(txt)
                } else {
                    listTxtTop.add(txt)
                }
            }
            attach2Screen(listTxtTop, xCenter, yCenter, yItem)
            attach2Screen(listTxtBottom, xCenter, yCenter, yItem)
            return true
        }
        return false
    }

    /**
     * 修正TextView的Y坐标将将其添加到容器上。
     */
    private fun attach2Screen(
        listTxt: LinkedList<TextView>,
        xCenter: Int,
        yCenter: Int,
        yItem: Int
    ) {
        val size = listTxt.size
        sortXYList(listTxt, size)
        for (i in 0 until size) {
            val txt = listTxt[i]
            val iXY = txt.tag as IntArray
            // Log.d("ANDROID_LAB", "fix[  " + txt.getText() + "  ] x:" +
// iXY[IDX_X] + " y:" + iXY[IDX_Y] + " r2="
// + iXY[IDX_DIS_Y]);
// 第二次修正:修正y坐标
            val yDistance = iXY[IDX_Y] - yCenter
            // 对于最靠近中心点的，其值不会大于yItem
// 对于可以一路下降到中心点的，则该值也是其应调整的大小
            var yMove = abs(yDistance)
            for (k in i - 1 downTo 0) {
                val kXY = listTxt[k].tag as IntArray
                val startX = kXY[IDX_X]
                val endX = startX + kXY[IDX_TXT_LENGTH]
                // y轴以中心点为分隔线，在同一侧
                if (yDistance * (kXY[IDX_Y] - yCenter) > 0) { // Log.d("ANDROID_LAB", "compare:" +
// listTxt.get(k).getText());
                    if (isXMixed(
                            startX,
                            endX,
                            iXY[IDX_X],
                            iXY[IDX_X] + iXY[IDX_TXT_LENGTH]
                        )
                    ) {
                        val tmpMove = abs(
                            iXY[IDX_Y] - kXY[IDX_Y]
                        )
                        if (tmpMove > yItem) {
                            yMove = tmpMove
                        } else if (yMove > 0) { // 取消默认值。
                            yMove = 0
                        }
                        // Log.d("ANDROID_LAB", "break");
                        break
                    }
                }
            }
            if (yMove > yItem) {
                val maxMove = yMove - yItem
                val randomMove = random!!.nextInt(maxMove)
                val realMove =
                    randomMove.coerceAtLeast(maxMove shr 1) * yDistance / abs(
                        yDistance
                    )
                iXY[IDX_Y] =
                    iXY[IDX_Y] - realMove
                iXY[IDX_DIS_Y] =
                    abs(iXY[IDX_Y] - yCenter)
                // 已经调整过前i个需要再次排序
                sortXYList(listTxt, i + 1)
            }
            val layParams = LayoutParams(
                LayoutParams.WRAP_CONTENT,
                LayoutParams.WRAP_CONTENT
            )
            layParams.gravity = Gravity.START or Gravity.TOP
            layParams.leftMargin = iXY[IDX_X]
            layParams.topMargin = iXY[IDX_Y]
            addView(txt, layParams)
            // 动画
            val animSet = getAnimationSet(iXY, xCenter, yCenter, txtAnimInType)
            txt.startAnimation(animSet)
        }
    }

    private fun getAnimationSet(xy: IntArray, xCenter: Int, yCenter: Int, type: Int): AnimationSet {
        val animSet = AnimationSet(true)
        animSet.interpolator = interpolator
        when (type) {
            OUTSIDE_TO_LOCATION -> {
                animSet.addAnimation(animAlpha2Opaque)
                animSet.addAnimation(animScaleLarge2Normal)
                val translate = TranslateAnimation(
                    (xy[IDX_X] + (xy[IDX_TXT_LENGTH] shr 1) - xCenter shl 1).toFloat(),
                    0F,
                    (xy[IDX_Y] - yCenter shl 1).toFloat(),
                    0F
                )
                animSet.addAnimation(translate)
            }
            LOCATION_TO_OUTSIDE -> {
                animSet.addAnimation(animAlpha2Transparent)
                animSet.addAnimation(animScaleNormal2Large)
                val translate = TranslateAnimation(
                    0F,
                    (xy[IDX_X] + (xy[IDX_TXT_LENGTH] shr 1) - xCenter shl 1).toFloat(),
                    0F,
                    (xy[IDX_Y] - yCenter shl 1).toFloat()
                )
                animSet.addAnimation(translate)
            }
            LOCATION_TO_CENTER -> {
                animSet.addAnimation(animAlpha2Transparent)
                animSet.addAnimation(animScaleNormal2Zero)
                val translate = TranslateAnimation(
                    0F,
                    (-xy[IDX_X] + xCenter).toFloat(),
                    0F,
                    (-xy[IDX_Y] + yCenter).toFloat()
                )
                animSet.addAnimation(translate)
            }
            CENTER_TO_LOCATION -> {
                animSet.addAnimation(animAlpha2Opaque)
                animSet.addAnimation(animScaleZero2Normal)
                val translate = TranslateAnimation(
                    (-xy[IDX_X] + xCenter).toFloat(),
                    0F,
                    (-xy[IDX_Y] + yCenter).toFloat(),
                    0F
                )
                animSet.addAnimation(translate)
            }
        }
        animSet.duration = duration
        return animSet
    }

    /**
     * 根据与中心点的距离由近到远进行冒泡排序。
     * @param endIdx  起始位置。
     * @param listTxt 待排序的数组。
     */
    private fun sortXYList(listTxt: LinkedList<TextView>, endIdx: Int) {
        for (i in 0 until endIdx) {
            for (k in i + 1 until endIdx) {
                if ((listTxt[k].tag as IntArray)[IDX_DIS_Y] < (listTxt[i].tag as IntArray)[IDX_DIS_Y]
                ) {
                    val iTmp = listTxt[i]
                    val kTmp = listTxt[k]
                    listTxt[i] = kTmp
                    listTxt[k] = iTmp
                }
            }
        }
    }

    // A线段与B线段所代表的直线在X轴映射上是否有交集
    private fun isXMixed(startA: Int, endA: Int, startB: Int, endB: Int): Boolean {
        var result = false
        when {
            startB in startA..endA -> result = true
            endB in startA..endA -> result = true
            startA in startB..endB -> result = true
            endA in startB..endB -> result = true
        }
        return result
    }

    private fun randomXY(
        ran: Random?,
        listX: LinkedList<Int>,
        listY: LinkedList<Int>
    ): IntArray {
        val arr = IntArray(4)
        arr[IDX_X] = listX.removeAt(ran!!.nextInt(listX.size))
        arr[IDX_Y] = listY.removeAt(ran.nextInt(listY.size))
        return arr
    }

    override fun onGlobalLayout() {
        val tmpW = getWidth()
        val tmpH = getHeight()
        if (width != tmpW || height != tmpH) {
            width = tmpW
            height = tmpH
            show()
        }
    }

    fun rubKeywords() {
        vecKeywords!!.clear()
    }

    fun setOnItemClickListener(listener: OnClickListener?) {
        itemClickListener = listener
    }

    companion object {
        const val IDX_X = 0
        const val IDX_Y = 1
        const val IDX_TXT_LENGTH = 2
        const val IDX_DIS_Y = 3
        const val ANIMATION_IN = 1 // 由外至内的动画
        const val ANIMATION_OUT = 2 // 由内至外的动画
        /* 位移动画类型 */
        const val OUTSIDE_TO_LOCATION = 1 // 从外围移动到坐标点
        const val LOCATION_TO_OUTSIDE = 2 // 从坐标点移动到外围
        const val CENTER_TO_LOCATION = 3 // 从中心点移动到坐标点
        const val LOCATION_TO_CENTER = 4 // 从坐标点移动到中心点
        const val ANIM_DURATION = 800L
        const val TEXT_SIZE_MAX = 25
        const val TEXT_SIZE_MIN = 15

        @JvmField
        var MAX = 10

        private var interpolator: Interpolator? = null
        private var animAlpha2Opaque: AlphaAnimation? = null
        private var animAlpha2Transparent: AlphaAnimation? = null
        private var animScaleLarge2Normal: ScaleAnimation? = null
        private var animScaleNormal2Large: ScaleAnimation? = null
        private var animScaleZero2Normal: ScaleAnimation? = null
        private var animScaleNormal2Zero: ScaleAnimation? = null
    }
}