package com.fadai.nodeprogress

import android.content.Context

/**
 * 作者：miaoyongyong on 2019/11/28 10:34

 * 邮箱：i_fadai@163.com
 */
object SizeUtils {

    /**
     * Value of dp to value of px.
     *
     * @param dpValue The value of dp.
     * @return value of px
     */
    fun dp2px(context: Context, dpValue: Float): Int {
        val scale = context.getResources().getDisplayMetrics().density
        return (dpValue * scale + 0.5f).toInt()
    }

}