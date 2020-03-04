package com.gz.goodneighbor.widget.progress

import android.content.Context
import android.util.AttributeSet
import android.view.View
import android.R.attr.path
import android.animation.ValueAnimator
import android.content.pm.PackageInstaller.STATUS_FAILURE
import android.content.pm.PackageInstaller.STATUS_SUCCESS
import android.graphics.*
import android.util.Log
import com.fadai.nodeprogress.R
import com.fadai.nodeprogress.SizeUtils
import java.util.ArrayList


/**
 * 作者：miaoyongyong on 2019/10/17 16:15

 * 邮箱：i_fadai@163.com
 */
class NodeProgressBar : View {
    val TAG = javaClass.simpleName

    // 是否需要重新配置路径
    var mNeedResetPath = true

    // 圆圈半
    var circleWidth = SizeUtils.dp2px(context, 20F)
        set(value) {
            field = value
            mNeedResetPath = true
        }
    // 圆圈内容宽度
    var circleContentWidth = circleWidth / 3
        set(value) {
            field = value
            mNeedResetPath = true
        }
    // 背景条颜色
    var bgColor: Int = 0xFFD0D0D0.toInt()
        set(value) {
            field = value
            initBgPaint()
        }
    // 进度条高度
    var progressHeight = SizeUtils.dp2px(context, 2F)
        set(value) {
            field = value
            initProgressPaint()
            initBgPaint()
        }
    // 进度条颜色
    var progressColor: Int = Color.RED
        set(value) {
            field = value
            initProgressPaint()
        }

    // 每一节横线的动画时间
    var lineProgressTime = 200L
        set(value) {
            field = value
        }
    // 圆圈的动画时间
    var circleTime = 600L
        set(value) {
            field = value
        }
    // 圆圈内容的动画时间
    var circleContentTime = 400L
        set(value) {
            field = value
        }

    // 背景画笔
    private var mBgPaint: Paint? = null
    // 进度画笔
    private var mProgressPaint: Paint? = null

    // 节点数量
    private var mCount = 1

    // 背景路径
    private var mBgPath: Path? = null

    // 最后一段路径
    private var mLineEndPath: Path? = null
    // 圆圈路径列表
    private var mCirclePathList: MutableList<Path> = ArrayList()
    // 圆圈内容路径列表
    private var mCircleContentPathList: MutableList<Path> = ArrayList()
    // 圆圈内容路径列表（true
    private var mCircleContentTruePathList: MutableList<Path> = ArrayList()
    // 圆圈内容路径列表（false
    private var mCircleContentFalsePathList: MutableList<Path> = ArrayList()
    // 横线路径列表
    private var mLinePathList: MutableList<Path> = ArrayList()


    // 请求状态列表
    private var mRequestStatusList: MutableList<Int> = ArrayList()

    // 横线的动画列表
    private var mAnimatorLineList: MutableList<ValueAnimator> = ArrayList()
    // 圆圈动画的列表
    private var mAnimatorCircleList: MutableList<ValueAnimator> = ArrayList()
    // 圆圈内内容的动画列表
    private var mAnimatorCircleContentList: MutableList<ValueAnimator> = ArrayList()
    // 最后一段线的动画列表
    private var mAnimatorEnd: ValueAnimator? = null

    // 当前状态
    private var mStatus = STATUS_NONE
    // 当前节点
    private var mNode = 0
    // 当前阶段
    private var mStage = STAGE_LINE
    // 当前进度
    private var mCurrentProgress = 0

    // 最大进度
    private val MAX_PROGRESS = 100
    // 测量Path
    private var mMeasure: PathMeasure? = null

    companion object {

        // 空状态
        val STATUS_NONE = -1
        // 进行中状态
        val STATUS_PROGRESS = -2
        // 完成状态
        val STATUS_COMPLE = -3
        // 失败状态
        val STATUS_FAILURE = -4

        // 请求状态-请求中
        val REQUEST_STATUS_REQUESTING = 1
        // 请求状态-请求成功
        val REQUEST_STATUS_SUCCESS = 2
        // 请求状态-请求成功
        val REQUEST_STATUS_FAILURE = 3

        // 横线阶段
        val STAGE_LINE = 1
        // 圆圈阶段
        val STAGE_CIRCLE = 2
        // 圆圈内容阶段
        val STAGE_CIRCLE_CONTENT = 3
    }

    constructor(context: Context) : super(context) {
        init()
    }

    constructor(context: Context, attrs: AttributeSet) : super(context, attrs) {
        init()
    }

    constructor(context: Context, attrs: AttributeSet, defStyleAttr: Int) : super(context, attrs, defStyleAttr) {
        init()
        var typedArray = context?.obtainStyledAttributes(attrs, R.styleable.NodeProgressBar)
        circleWidth = typedArray.getDimensionPixelSize(R.styleable.NodeProgressBar_np_circleWidth, circleWidth)
        bgColor = typedArray.getColor(R.styleable.NodeProgressBar_np_backgroundBarColor, bgColor)
        progressHeight = typedArray.getDimensionPixelSize(R.styleable.NodeProgressBar_np_progressHeight, progressHeight)
        progressColor = typedArray.getColor(R.styleable.NodeProgressBar_np_progressColor, progressColor)
        lineProgressTime = typedArray.getInt(R.styleable.NodeProgressBar_np_lineAnimDuration, lineProgressTime.toInt()).toLong()
        circleTime = typedArray.getColor(R.styleable.NodeProgressBar_np_circleAnimDuration, circleTime.toInt()).toLong()
        circleContentTime = typedArray.getColor(R.styleable.NodeProgressBar_np_circleContentAnimDuration, circleContentTime.toInt()).toLong()
    }

    private fun init() {
        mMeasure = PathMeasure()
        initBgPaint()
        initProgressPaint()
        initAnimator()
    }

    private fun initProgressPaint() {
        mProgressPaint = Paint()
        mProgressPaint?.color = progressColor
        mProgressPaint?.strokeWidth = progressHeight.toFloat()
        mProgressPaint?.isAntiAlias = true
        mProgressPaint?.style = Paint.Style.STROKE
        mProgressPaint?.setStrokeCap(Paint.Cap.ROUND);
    }

    private fun initBgPaint() {
        mBgPaint = Paint()
        mBgPaint?.color = bgColor
        mBgPaint?.strokeWidth = progressHeight.toFloat()
        mBgPaint?.isAntiAlias = true
        mBgPaint?.style = Paint.Style.STROKE
    }

    /**
     * 初始化动画
     */
    private fun initAnimator() {

        mRequestStatusList.clear()
        mAnimatorLineList.clear()
        mAnimatorCircleList.clear()
        mAnimatorCircleContentList.clear()

        for (i in 0 until mCount) {
            // 请求状态默认为请求中
            mRequestStatusList.add(REQUEST_STATUS_REQUESTING)

            // 横线动画
            var lineAnimator = ValueAnimator.ofFloat(0F, 1F).setDuration(lineProgressTime)
            lineAnimator?.addUpdateListener {
                if (mStage == STAGE_LINE) {
                    var progress = MAX_PROGRESS * (it.getAnimatedValue() as Float)
                    mCurrentProgress = progress.toInt()
                    // 动画结束后，由横线阶段->圆圈阶段
                    if (mCurrentProgress == MAX_PROGRESS) {
                        mStage = STAGE_CIRCLE
                        onStatusChange()
                    }
                    postInvalidate()
                }
            }
            mAnimatorLineList.add(lineAnimator)

            // 圆圈动画
            var circleAnimator = ValueAnimator.ofFloat(0F, 1F).setDuration(circleTime)
            // 无限循环
            circleAnimator?.repeatCount = ValueAnimator.INFINITE
            circleAnimator?.addUpdateListener {
                if (mStage == STAGE_CIRCLE) {
                    var progress = MAX_PROGRESS * (it.getAnimatedValue() as Float)
                    mCurrentProgress = progress.toInt()

                    // 无限动画最后的进度可能不是max值
                    if (mCurrentProgress == MAX_PROGRESS || mCurrentProgress == MAX_PROGRESS - 1) {
                        // 动画一圈结束后，判断请求状态是否仍是请求中
                        if (mRequestStatusList[mNode] != REQUEST_STATUS_REQUESTING) {
                            // 不是请求中的话，则停止动画，开始圆圈内容动画
                            circleAnimator?.cancel()
                            mStage = STAGE_CIRCLE_CONTENT
                            onStatusChange()
                        }
                    }
                    postInvalidate()
                }
            }
            mAnimatorCircleList.add(circleAnimator)

            // 圆圈内容动画
            var circleContentAnimator = ValueAnimator.ofFloat(0F, 1F).setDuration(circleContentTime)
            circleContentAnimator?.addUpdateListener {
                if (mStage == STAGE_CIRCLE_CONTENT) {
                    var progress = MAX_PROGRESS * (it.getAnimatedValue() as Float)
                    mCurrentProgress = progress.toInt()
                    // 动画结束后
                    if (mCurrentProgress == MAX_PROGRESS) {
                        // 如果请求成功了，执行回调，进入下一个节点，再次进入横线阶段
                        if (mRequestStatusList[mNode] == REQUEST_STATUS_SUCCESS) {
                            progressListener?.onRequestScuccess(mNode)
                            mStage = STAGE_LINE
                            mNode++
                            onStatusChange()
                        } else {
                            // 请求失败了，状态更新为失败状态，执行回调
                            mStatus = STATUS_FAILURE
                            progressListener?.onRequestFailure(mNode)
                        }

                    }
                    postInvalidate()
                }
            }
            mAnimatorCircleContentList.add(circleContentAnimator)
        }

        // 最后一段横线动画，单独处理
        mAnimatorEnd = ValueAnimator.ofFloat(0F, 1F).setDuration(lineProgressTime)
        mAnimatorEnd?.addUpdateListener {
            if (mNode == mCount) { // 如果当前节点超过最后一个节点
                var progress = MAX_PROGRESS * (it.getAnimatedValue() as Float)
                mCurrentProgress = progress.toInt()
                // 动画结束后，状态为完成状态，执行回调
                if (mCurrentProgress == MAX_PROGRESS) {
                    mStatus = STATUS_COMPLE
                    progressListener?.onComplete()
                }
                postInvalidate()
            }
        }
    }

    override fun onDraw(canvas: Canvas) {
        super.onDraw(canvas)
        drawBg(canvas)
        drawProgress(canvas)
    }

    /**
     * 绘制背景条
     */
    private fun drawBg(canvas: Canvas) {
        // 如果路径的数量等于节点数量，则初始化路径
        if (isDataAbnormal()) {
            mNeedResetPath = false
            initPath()
        }
        canvas.drawPath(mBgPath, mBgPaint)
    }

    private fun isDataAbnormal(): Boolean {
        return (mLinePathList.size != mCount
                || mCirclePathList.size != mCount
                || mCircleContentPathList.size != mCount
                || mCircleContentTruePathList.size != mCount
                || mCircleContentFalsePathList.size != mCount
                || mNeedResetPath)
    }

    /**
     * 初始化路径
     */
    private fun initPath() {
        mLinePathList.clear()
        mCirclePathList.clear()
        mCircleContentTruePathList.clear()
        mCircleContentFalsePathList.clear()
        mCircleContentPathList.clear()

        if (mBgPath == null) {
            mBgPath = Path()
        }
        if (mLineEndPath == null) {
            mLineEndPath = Path()
        }
        mBgPath?.reset()
        mLineEndPath?.reset()

        var startY = height / 2F
        var startX = 0F
        // 每一节线的宽度=（总宽度-节点宽度*数量）/(节点数量+1)
        var progressWidth = (width - circleWidth * mCount) / (mCount + 1)

        // 移动到开始位置
        mBgPath?.moveTo(startX, startY)

        // 遍历所有节点
        for (i in 0 until mCount) {

            // 线
            var linePath = Path()
            linePath.moveTo(startX, startY)
            startX += progressWidth
            linePath?.lineTo(startX, startY)
            mBgPath?.lineTo(startX, startY)

            // 圈
            var ciclePath = Path()
            var radius = circleWidth / 2F
            var centerX1 = startX + (radius)
            var centerY1 = height / 2F
            var rectfCircle1 = RectF(startX, centerY1 - radius, startX + circleWidth, centerY1 + radius)
            ciclePath?.addArc(rectfCircle1, 180F, 359.9F)
            mBgPath?.addCircle(centerX1, centerY1, radius, Path.Direction.CW)

            // 圆圈内容 对号
            var contentTruePath = Path()
            var startContentX1 = centerX1 - circleContentWidth / 2
            var startContentY1 = centerY1 - circleContentWidth / 2
            var contentPoint11 = PointF(startContentX1, startContentY1 + circleContentWidth / 2)
            var contentPoint12 = PointF(startContentX1 + circleContentWidth / 2, startContentY1 + circleContentWidth)
            var contentPoint13 = PointF(startContentX1 + circleContentWidth, startContentY1)
            contentTruePath?.moveTo(contentPoint11.x, contentPoint11.y)
            contentTruePath?.lineTo(contentPoint12.x, contentPoint12.y)
            contentTruePath?.lineTo(contentPoint13.x, contentPoint13.y)

            // 圆圈内容 叉号
            var contentFalsePath = Path()
            var contentPoint14 = PointF(startContentX1, startContentY1)
            var contentPoint15 = PointF(startContentX1 + circleContentWidth, startContentY1 + circleContentWidth)
            var contentPoint16 = PointF(startContentX1 + circleContentWidth, startContentY1)
            var contentPoint17 = PointF(startContentX1, startContentY1 + circleContentWidth)
            contentFalsePath?.moveTo(contentPoint14.x, contentPoint14.y)
            contentFalsePath?.lineTo(contentPoint15.x, contentPoint15.y)
            contentFalsePath?.moveTo(contentPoint16.x, contentPoint16.y)
            contentFalsePath?.lineTo(contentPoint17.x, contentPoint17.y)

            mLinePathList.add(linePath)
            mCirclePathList.add(ciclePath)
            mCircleContentTruePathList.add(contentTruePath)
            mCircleContentFalsePathList.add(contentFalsePath)
            mCircleContentPathList.add(contentFalsePath)

            startX += circleWidth
        }

        // 最后一段横线
        mLineEndPath?.moveTo(startX, startY)
        mBgPath?.moveTo(startX, startY)
        startX += progressWidth
        mLineEndPath?.lineTo(startX, startY)
        mBgPath?.lineTo(startX, startY)
    }

    // 绘制进度
    private fun drawProgress(canvas: Canvas) {
        when (mStatus) {
            STATUS_NONE -> { // 不绘制进度
            }

            STATUS_COMPLE -> { // 完成样式
                canvas.drawPath(mBgPath, mProgressPaint)
                for (i in 0 until mCount) {
                    canvas.drawPath(mCircleContentPathList[i], mProgressPaint)
                }
            }
            STATUS_PROGRESS, STATUS_FAILURE -> {// 绘制中状态、错误状态
                for (i in 0..mNode) {
                    if (i == mCount) { // 所有阶段结束后的最后一条线
                        drawLastLine(canvas)
                    } else {// 正常阶段
                        if (i < mNode) { // 已经过去的阶段
                            drawPastNode(canvas, i)
                        } else if (i == mNode) { // 请求中的阶段
                            drawCurrentNode(i, canvas)
                        }
                    }
                }
            }
        }
    }

    /**
     * 绘制当前进行中的阶段
     */
    private fun drawCurrentNode(i: Int, canvas: Canvas) {
        when (mStage) {
            STAGE_LINE -> {
                mMeasure!!.setPath(mLinePathList[i], false)
                var path = Path()
                var start = 0F
                var stop = mMeasure!!.length * (mCurrentProgress.toFloat() / MAX_PROGRESS)
                mMeasure!!.getSegment(start, stop, path, true)
                canvas.drawPath(path, mProgressPaint)
            }
            STAGE_CIRCLE -> {
                canvas.drawPath(mLinePathList[i], mProgressPaint)
                mMeasure!!.setPath(mCirclePathList[i], false)
                var path = Path()
                var start = 0F
                var stop = mMeasure!!.length * (mCurrentProgress.toFloat() / MAX_PROGRESS)
                mMeasure!!.getSegment(start, stop, path, true)
                canvas.drawPath(path, mProgressPaint)
            }
            STAGE_CIRCLE_CONTENT -> {
                canvas.drawPath(mLinePathList[i], mProgressPaint)
                canvas.drawPath(mCirclePathList[i], mProgressPaint)

                mMeasure!!.setPath(mCircleContentPathList[i], false)
                var path = Path()
                var start = 0F
                when (mRequestStatusList[mNode]) {
                    REQUEST_STATUS_SUCCESS -> {
                        var stop = (mMeasure!!.length
                                ?: 0F) * (mCurrentProgress.toFloat() / MAX_PROGRESS)
                        mMeasure!!.getSegment(start, stop, path, true)
                        canvas.drawPath(path, mProgressPaint)
                    }
                    REQUEST_STATUS_FAILURE -> {
                        if (mCurrentProgress > 50) {// 进度后50%时
                            mMeasure!!.getSegment(0F, mMeasure!!.length
                                    ?: 0F, path, true)
                            canvas.drawPath(path, mProgressPaint)
                            mMeasure!!.nextContour()
                            var stop = (mMeasure!!.length
                                    ?: 0F) * ((mCurrentProgress.toFloat() - 50) / (MAX_PROGRESS / 2))
                            mMeasure!!.getSegment(start, stop, path, true)
                            canvas.drawPath(path, mProgressPaint)
                        } else { // 进度前50%时，只绘制叉号的一条线
                            var stop = (mMeasure!!.length
                                    ?: 0F) * (mCurrentProgress.toFloat() / (MAX_PROGRESS / 2))
                            mMeasure!!.getSegment(start, stop, path, true)
                            canvas.drawPath(path, mProgressPaint)
                        }
                    }
                }
            }
        }
    }

    /**
     * 绘制已经过去的阶段
     */
    private fun drawPastNode(canvas: Canvas, i: Int) {
        canvas.drawPath(mLinePathList[i], mProgressPaint)
        canvas.drawPath(mCirclePathList[i], mProgressPaint)
        canvas.drawPath(mCircleContentPathList[i], mProgressPaint)
    }

    /**
     * 绘制最后一条线
     */
    private fun drawLastLine(canvas: Canvas) {
        mMeasure!!.setPath(mLineEndPath, false)
        var path = Path()
        var start = 0F
        var stop = mMeasure!!.length * (mCurrentProgress.toFloat() / MAX_PROGRESS)
        mMeasure!!.getSegment(start, stop, path, true)
        canvas.drawPath(path, mProgressPaint)
    }

    /**
     * 状态改变
     */
    private fun onStatusChange() {
        when (mStatus) {
            STATUS_PROGRESS -> {
                if (mNode < mCount) {
                    when (mStage) {
                        STAGE_LINE -> {
                            var animator = mAnimatorLineList[mNode]
                            animator.start()
                        }
                        STAGE_CIRCLE -> {
                            var animator = mAnimatorCircleList[mNode]
                            animator.start()
                        }
                        STAGE_CIRCLE_CONTENT -> {
                            var animator = mAnimatorCircleContentList[mNode]
                            animator.start()
                        }
                    }
                } else {
                    mAnimatorEnd?.start()
                }
            }
            STATUS_NONE -> {
                postInvalidate()
            }
            STATUS_COMPLE -> {
                postInvalidate()

            }
            STATUS_FAILURE -> {
                postInvalidate()

            }
        }
    }

    /**
     * 开始动画
     */
    fun start() {
        for (anim in mAnimatorLineList) {
            anim.cancel()
        }
        for (anim in mAnimatorCircleContentList) {
            anim.cancel()
        }
        for (anim in mAnimatorCircleList) {
            anim.cancel()
        }
        initAnimator()
        mNode = 0
        mStatus = STATUS_PROGRESS
        mStage = STAGE_LINE
        onStatusChange()
    }

    /**
     * 重置
     */
    fun reset() {
        for (anim in mAnimatorLineList) {
            anim.cancel()
        }
        for (anim in mAnimatorCircleContentList) {
            anim.cancel()
        }
        for (anim in mAnimatorCircleList) {
            anim.cancel()
        }
        initAnimator()
        mNode = 0
        mStage = STAGE_LINE
        mStatus = STATUS_NONE
        onStatusChange()
    }

    // 回调
    var progressListener: OnProgressListener? = null

    interface OnProgressListener {
        fun onRequestScuccess(index: Int)
        fun onRequestFailure(index: Int)
        fun onComplete()
    }

    /**
     * 设置请求状态
     */
    fun setRequestStatus(isSuccess: Boolean, index: Int) {
        if (!isDataAbnormal() && index < mCount) {
            if (isSuccess) {
                mCircleContentPathList[index] = mCircleContentTruePathList[index]
                mRequestStatusList[index] = REQUEST_STATUS_SUCCESS
            } else {
                mCircleContentPathList[index] = mCircleContentFalsePathList[index]
                mRequestStatusList[index] = REQUEST_STATUS_FAILURE
            }
        } else {
            Log.e(TAG, "index 不能大于或等于总节点数")
        }
    }

    /**
     * 设置节点数量
     */
    fun setCount(count: Int) {
        if (count <= 0) {
            throw Exception("count 不能小于1")
        } else {
            mCount = count
        }
    }

    fun getCount(): Int {
        return mCount
    }
}