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

import android.text.TextUtils
import android.util.Log
import okhttp3.*
import okio.Buffer
import java.io.IOException
import java.util.*


/**
 * 网络请求公共参数封装

 * @author : yyfly / developer@yyfly.com
 * @version : 1.0
 */
class BasicParamsInterceptor private constructor() : Interceptor {

    internal var queryParamsMap: MutableMap<String, String> = HashMap()
    internal var paramsMap: MutableMap<String, String> = HashMap()
    internal var headerParamsMap: MutableMap<String, String> = HashMap()
    internal var headerLinesList: MutableList<String> = ArrayList()

    @Throws(IOException::class)
    override fun intercept(chain: Interceptor.Chain): Response {

        var request = chain.request()
        val requestBuilder = request.newBuilder()

        // process header params inject
        val headerBuilder = request.headers().newBuilder()
        if (headerParamsMap.isNotEmpty()) {
            headerParamsMap.forEach {
                headerBuilder.add(it.key, it.value)
            }
            requestBuilder.headers(headerBuilder.build())
        }

        if (headerLinesList.isNotEmpty()) {
            headerLinesList.forEach {
                headerBuilder.add(it)
            }
            requestBuilder.headers(headerBuilder.build())
        }
        // process header params end


        // process queryParams inject whatever it's GET or POST
        if (queryParamsMap.isNotEmpty()) {
            request = injectParamsIntoUrl(request.url().newBuilder(), requestBuilder, queryParamsMap)
        }

        // process post body inject
        if (paramsMap.isNotEmpty()) {
            if (canInjectIntoBody(request)) {
                val formBodyBuilder = FormBody.Builder()
                for ((key, value) in paramsMap) {
                    formBodyBuilder.add(key, value)
                }

                val formBody = formBodyBuilder.build()
                var postBodyString = bodyToString(request.body())
                postBodyString += (if (postBodyString.isNotEmpty()) "&" else "") + bodyToString(formBody)
                requestBuilder.post(RequestBody.create(MediaType.parse("application/x-www-form-urlencoded;charset=UTF-8"), postBodyString))
            }
        }

        request = requestBuilder.build()

        val headers = request.headers()
        headers?.let {
            if (HttpApp.logFlag) Log.i(TAG, "Request Headers " +
                    " \n Auth = " + it.get("Auth") +
                    " \n App-id = " + it.get("App-id") +
                    " \n Version = " + it.get("Version") +
                    " \n Client-id = " + it.get("Client-id"))
        }

        return chain.proceed(request)
    }

    private fun canInjectIntoBody(request: Request?): Boolean {
        if (request == null) {
            return false
        }
        if (!TextUtils.equals(request.method(), "POST")) {
            return false
        }
        val body = request.body() ?: return false
        val mediaType = body.contentType() ?: return false
        if (!TextUtils.equals(mediaType.subtype(), "x-www-form-urlencoded")) {
            return false
        }
        return true
    }

    // func to inject params into url
    private fun injectParamsIntoUrl(httpUrlBuilder: HttpUrl.Builder, requestBuilder: Request.Builder, paramsMap: Map<String, String>): Request? {

        if (paramsMap.isNotEmpty()) {
            paramsMap.forEach {
                httpUrlBuilder.addQueryParameter(it.key, it.value)
            }
            requestBuilder.url(httpUrlBuilder.build())
            return requestBuilder.build()
        }
        return null
    }

    class Builder {

        internal var interceptor: BasicParamsInterceptor = BasicParamsInterceptor()

        fun addParam(key: String, value: String): Builder {
            interceptor.paramsMap.put(key, value)
            return this
        }

        fun addParamsMap(paramsMap: Map<String, String>): Builder {
            interceptor.paramsMap.putAll(paramsMap)
            return this
        }

        fun addHeaderParam(key: String, value: String): Builder {
            interceptor.headerParamsMap.put(key, value)
            return this
        }

        fun addHeaderParamsMap(headerParamsMap: Map<String, String>): Builder {
            interceptor.headerParamsMap.putAll(headerParamsMap)
            return this
        }

        fun addHeaderLine(headerLine: String): Builder {
            val index = headerLine.indexOf(":")
            if (index == -1) {
                throw IllegalArgumentException("Unexpected header: " + headerLine)
            }
            interceptor.headerLinesList.add(headerLine)
            return this
        }

        fun addHeaderLinesList(headerLinesList: List<String>): Builder {
            for (headerLine in headerLinesList) {
                val index = headerLine.indexOf(":")
                if (index == -1) {
                    throw IllegalArgumentException("Unexpected header: " + headerLine)
                }
                interceptor.headerLinesList.add(headerLine)
            }
            return this
        }

        fun addQueryParam(key: String, value: String): Builder {
            interceptor.queryParamsMap.put(key, value)
            return this
        }

        fun addQueryParamsMap(queryParamsMap: Map<String, String>): Builder {
            interceptor.queryParamsMap.putAll(queryParamsMap)
            return this
        }

        fun build(): BasicParamsInterceptor {
            return interceptor
        }

    }

    companion object {

        private val TAG = "ParamsInterceptor"

        private fun bodyToString(request: RequestBody?): String {
            try {
                val copy = request
                val buffer = Buffer()
                if (copy != null)
                    copy.writeTo(buffer)
                else
                    return ""
                return buffer.readUtf8()
            } catch (e: IOException) {
                return "did not work"
            }

        }
    }
}
