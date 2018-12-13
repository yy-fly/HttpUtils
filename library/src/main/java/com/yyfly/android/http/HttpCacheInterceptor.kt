/*
 * Copyright (C) 2017-2018 yyfly, Inc.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package com.yyfly.android.http

import android.content.Context
import android.net.ConnectivityManager
import android.net.NetworkInfo
import android.util.Log
import okhttp3.CacheControl
import okhttp3.Interceptor
import okhttp3.Response
import java.io.IOException

/**
 * Http缓存拦截器
 *
 * @author : yyfly / developer@yyfly.com
 * @version : 1.0
 * @date : 2016/3/18
 */
class HttpCacheInterceptor : Interceptor {

    @Throws(IOException::class)
    override fun intercept(chain: Interceptor.Chain): Response {
        var request = chain.request()
        if (!isConnected()) {
            request = request.newBuilder()
                    .cacheControl(CacheControl.FORCE_CACHE)
                    .build()
            if (HttpApp.logFlag)  Log.d("Okhttp", "no network")
        }

        val originalResponse = chain.proceed(request)
        if (isConnected()) {
            //有网的时候读接口上的@Headers里的配置，你可以在这里进行统一的设置
            val cacheControl = request.cacheControl().toString()
            return originalResponse.newBuilder()
                    .header("Cache-Control", cacheControl)
                    .removeHeader("Pragma")
                    .build()
        } else {
            return originalResponse.newBuilder()
                    .header("Cache-Control", "public, only-if-cached, max-stale=2419200")
                    .removeHeader("Pragma")
                    .build()
        }
    }

    /**
     * 获取活动网络信息
     *
     * 需添加权限 `<uses-permission android:name="android.permission.ACCESS_NETWORK_STATE"/>`

     * @return NetworkInfo
     */
    private fun getActiveNetworkInfo(): NetworkInfo? {
        return (HttpApp.context.getSystemService(Context.CONNECTIVITY_SERVICE) as ConnectivityManager).activeNetworkInfo
    }

    /**
     * 判断网络是否连接
     *
     * 需添加权限 `<uses-permission android:name="android.permission.ACCESS_NETWORK_STATE"/>`

     * @return `true`: 是<br></br>`false`: 否
     */
    fun isConnected(): Boolean {
        val info = getActiveNetworkInfo()
        return info != null && info.isConnected
    }

}