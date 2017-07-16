/*
 * Copyright (C) 2017 yyfly, Inc.
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

import java.io.IOException

import okhttp3.HttpUrl
import okhttp3.Interceptor
import okhttp3.Request
import okhttp3.Response

/**
 * 动态Host
 *
 * @author : yyfly / developer@yyfly.com
 * @version : 1.0
 */
class HostSelectionInterceptor : Interceptor {

    @Volatile private var host: String? = null

    fun setHost(host: String) {
        this.host = host
    }

    @Throws(IOException::class)
    override fun intercept(chain: Interceptor.Chain): Response {
        var request = chain.request()
        val host = this.host
        if (host != null) {
            val newUrl = request.url().newBuilder()
                    .host(host)
                    .build()
            request = request.newBuilder()
                    .url(newUrl)
                    .build()
        }
        return chain.proceed(request)
    }
}
