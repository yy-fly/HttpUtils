package com.yyfly.android.http

import android.content.Context
import kotlin.properties.Delegates

/**
 * 工具初始
 *
 * @author : yyfly / developer@yyfly.com
 * @version : 1.0
 */
object HttpApp {

    var context: Context by Delegates.notNull()
    /**
     * 日志开关
     */
    var logFlag = false

    /**
     * APP 初始化相关操作
     * @param cxt
     * *
     * @param debug      是否调试
     */
    fun init(cxt: Context, debug: Boolean) {
        context = cxt
        logFlag = debug
    }

}
