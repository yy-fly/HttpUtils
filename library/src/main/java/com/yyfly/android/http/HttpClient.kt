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

import android.util.Log
import okhttp3.*
import okhttp3.logging.HttpLoggingInterceptor
import retrofit2.Retrofit
import retrofit2.adapter.rxjava2.RxJava2CallAdapterFactory
import retrofit2.converter.gson.GsonConverterFactory
import java.io.File
import java.io.IOException
import java.util.*

/**
 * 网络请求封装
 *
 * @author : yyfly / developer@yyfly.com
 * @version : 1.0
 */
object HttpClient {

    private val TAG = "HttpClient"
    //缓存大小
    private val DISK_CACHE_SIZE = 50 * 1024 * 1024

    val MEDIA_TYPE_JSON: MediaType = MediaType.parse("application/json; charset=utf-8")!!
    val MEDIA_TYPE_MARKDOWN: MediaType = MediaType.parse("text/x-markdown; charset=utf-8")!!
    var okHttpClient = OkHttpClient()

    private val hostSelectionInterceptor = HostSelectionInterceptor()
    private val loggingInterceptor = HttpLoggingInterceptor(HttpLoggingInterceptor.Logger {
        message -> if (HttpApp.logFlag) Log.i(TAG, message)
    })
    private val gsonConverterFactory = GsonConverterFactory.create(DateSerializer.defaultGson)
    private val rxJava2CallAdapterFactory = RxJava2CallAdapterFactory.create()

    /**
     * 获取retrofit实例
     *
     * @param baseUrl      请求地址
     * @param interceptors 拦截器
     * @return the retrofit
     * @author : yyfly / developer@yyfly.com
     */
    fun retrofit(baseUrl: String, vararg interceptors: Interceptor): Retrofit {
        return Retrofit.Builder()
                .baseUrl(baseUrl)
                .addConverterFactory(gsonConverterFactory)
                .addCallAdapterFactory(rxJava2CallAdapterFactory)
                .client(okHttpClient(*interceptors))
                .build()
    }

    /**
     * 设置拦截器
     *
     * @param interceptors
     * @return
     * @author : yyfly / developer@yyfly.com
     */
    fun okHttpClient(vararg interceptors: Interceptor): OkHttpClient {
        val builder = okHttpClient.newBuilder()
        builder.interceptors().addAll(buildInterceptors(*interceptors))
        builder.cache(builderCache())
        return builder.build()
    }

    /**
     * 构建拦截器数组
     *
     * @param interceptors
     * @return
     * @author : yyfly / developer@yyfly.com
     */
    fun buildInterceptors(vararg interceptors: Interceptor): List<Interceptor> {
        val list = ArrayList<Interceptor>()
        if (HttpApp.logFlag) {
            loggingInterceptor.level = HttpLoggingInterceptor.Level.BODY
        } else {
            loggingInterceptor.level = HttpLoggingInterceptor.Level.NONE
        }
        list.add(loggingInterceptor)
        list.add(hostSelectionInterceptor)
        list.addAll(interceptors)
        return list
    }

    /**
     * 构建缓存
     *
     * @return
     * @author : yyfly / developer@yyfly.com
     */
    private fun builderCache(): Cache {
        val cacheDir = File(HttpApp.context.cacheDir, "http_cache")
        val cache = Cache(cacheDir, DISK_CACHE_SIZE.toLong())
        return cache
    }

    /**
     * Post请求
     *
     * @param url    请求地址
     * @param json   发送Json数据
     * @return
     * @throws IOException
     * @author : yyfly / developer@yyfly.com
     */
    @Throws(IOException::class)
    fun post(url: String, json: String): String {
        val body = RequestBody.create(MEDIA_TYPE_JSON, json)
        val request = Request.Builder()
                .url(url)
                .post(body)
                .build()
        val response = okHttpClient().newCall(request).execute()
        return response.body()!!.string()
    }

    /**
     * POST请求
     *
     * @param url  请求地址
     * @param body 发送数据对象
     * @return
     * @throws IOException
     * @author : yyfly / developer@yyfly.com
     */
    @Throws(IOException::class)
    fun post(url: String, body: RequestBody): Response {
        val request = Request.Builder()
                .url(url)
                .post(body)
                .build()
        val response = okHttpClient().newCall(request).execute()
        return response
    }

    /**
     * 根据Json字符串创建RequestBody
     *
     * @param json
     * @return
     * @author : yyfly / developer@yyfly.com
     */
    fun buildJsonBody(json: String): RequestBody {
        val body = RequestBody.create(MEDIA_TYPE_JSON, json)
        return body
    }

    /**
     * 根据文件创建RequestBody
     *
     * @param file
     * @return
     * @author : yyfly / developer@yyfly.com
     */
    fun buildFileBody(file: File): RequestBody {
        val body = RequestBody.create(MEDIA_TYPE_MARKDOWN, file)
        return body
    }

    /**
     * 根据参数Map构建RequestBody
     *
     * @param params
     * @return
     * @author : yyfly / developer@yyfly.com
     */
    fun buildFormBody(params: Map<String, String>): RequestBody {
        val builder = FormBody.Builder()
        val keySet = params.keys
        for (key in keySet) {
            builder.add(key, params[key])
        }
        val body = builder.build()
        return body
    }
}
