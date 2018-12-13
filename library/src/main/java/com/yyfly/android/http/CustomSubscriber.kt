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

import android.content.Intent
import android.os.Build
import android.support.annotation.RequiresApi
import android.util.Log
import io.reactivex.disposables.Disposable
import io.reactivex.exceptions.Exceptions
import io.reactivex.functions.Action
import io.reactivex.functions.Consumer
import io.reactivex.internal.functions.Functions
import io.reactivex.internal.operators.flowable.FlowableInternalHelper
import io.reactivex.internal.subscriptions.SubscriptionHelper
import org.reactivestreams.Subscriber
import org.reactivestreams.Subscription
import retrofit2.HttpException
import java.io.IOException
import java.net.ConnectException
import java.net.SocketTimeoutException
import java.net.UnknownHostException
import java.util.concurrent.atomic.AtomicReference

/**
 * 封装Subscriber
 *
 * @param <T> the type parameter
 * @author : yyfly / developer@yyfly.com
 * @version : 1.0
 */
class CustomSubscriber<T : BaseResponseData> : AtomicReference<Subscription>, Subscriber<T>, Subscription, Disposable {
    internal val onNext: Consumer<T>
    internal val onError: Consumer<Throwable>
    internal val onComplete: Action
    internal val onSubscribe: Consumer<in Subscription>

    constructor(onNext: Consumer<T>,
                onError: Consumer<Throwable> = Functions.ERROR_CONSUMER,
                onComplete: Action = Functions.EMPTY_ACTION,
                onSubscribe: Consumer<in Subscription> = FlowableInternalHelper.RequestMax.INSTANCE) : super() {
        this.onNext = onNext
        this.onError = onError
        this.onComplete = onComplete
        this.onSubscribe = onSubscribe
    }

    @RequiresApi(api = Build.VERSION_CODES.GINGERBREAD)
    override fun onSubscribe(subscription: Subscription) {
        if (SubscriptionHelper.setOnce(this, subscription)) {
            try {
                onSubscribe.accept(this)
            } catch (ex: Throwable) {
                Exceptions.throwIfFatal(ex)
                subscription.cancel()
                onError(ex)
            }

        }
    }

    @RequiresApi(api = Build.VERSION_CODES.GINGERBREAD)
    override fun onNext(responseData: T) {
        if (!isDisposed) {
            try {
                if (responseData.isSuccess()!!) {
                    onNext.accept(responseData)
                } else {
                    if (responseData.isOauth()!!) {
                        val intent = Intent()
                        intent.addCategory(Intent.CATEGORY_DEFAULT)
                        intent.putExtra("from", "server")
                        intent.action = HttpApp.context.packageName + ".login"
                        intent.flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK
                        HttpApp.context.startActivity(intent)
                    } else {
                        onNext.accept(responseData)
                        if (HttpApp.logFlag) Log.e(TAG, "BaseResponseData（响应错误）-- 错误码：" + responseData.responseCode() + "-- >" + responseData.responseMessage())
                    }
                }
            } catch (e: Throwable) {
                Exceptions.throwIfFatal(e)
                get().cancel()
                onError(e)
            }
        }
    }


    @RequiresApi(api = Build.VERSION_CODES.GINGERBREAD)
    override fun onError(throwable: Throwable) {
        if (get() !== SubscriptionHelper.CANCELLED) {
            lazySet(SubscriptionHelper.CANCELLED)
            try {
                onError.accept(throwable)
            } catch (e: Throwable) {
                e.printStackTrace()
                Exceptions.throwIfFatal(e)
                //RxJavaPlugins.onError(new CompositeException(throwable, e));
            }
        } else {
            if (HttpApp.logFlag) Log.e(TAG, "SubscriptionHelper.CANCELLED")
        }

        if (throwable is HttpException) {
            val exception = throwable
            val code = exception.code()
            if (code in 200..299) {
                if (HttpApp.logFlag) Log.e(TAG, "Code:$code Request success!")
            } else if (code == 401) {
                if (HttpApp.logFlag) Log.e(TAG, CONNECT_EXCEPTION_401 + "\r\n" + exception.message() + "\r\n" + exception.toString())
            } else if (code == 403) {
                if (HttpApp.logFlag) Log.e(TAG, CONNECT_EXCEPTION_403 + "\r\n" + exception.message() + "\r\n" + exception.toString())
            } else if (code == 404) {
                if (HttpApp.logFlag) Log.e(TAG, CONNECT_EXCEPTION_404 + "\r\n" + exception.message() + "\r\n" + exception.toString())
            } else if (code in 405..499) {
                if (HttpApp.logFlag) Log.e(TAG, "Code:" + code + " Client Error" + exception.message() + "\r\n" + exception.toString())
            } else if (code == 500) {
                if (HttpApp.logFlag) Log.e(TAG, CONNECT_EXCEPTION_500 + "\r\n" + exception.message() + "\r\n" + exception.toString())
            } else if (code == 501) {
                if (HttpApp.logFlag) Log.e(TAG, CONNECT_EXCEPTION_501 + "\r\n" + exception.message() + "\r\n" + exception.toString())
            } else if (code == 502) {
                if (HttpApp.logFlag) Log.e(TAG, CONNECT_EXCEPTION_502 + "\r\n" + exception.message() + "\r\n" + exception.toString())
            } else if (code == 504) {
                if (HttpApp.logFlag) Log.e(TAG, CONNECT_EXCEPTION_504 + "\r\n" + exception.message() + "\r\n" + exception.toString())
            } else if (code == 505) {
                if (HttpApp.logFlag) Log.e(TAG, CONNECT_EXCEPTION_505 + "\r\n" + exception.message() + "\r\n" + exception.toString())
            } else if (code in 506..599) {
                if (HttpApp.logFlag) Log.e(TAG, "Code:" + code + " Server Error" + exception.message() + "\r\n" + exception.toString())
            }
        } else if (throwable is SocketTimeoutException) {
            if (HttpApp.logFlag) Log.e(TAG, "onError: SocketTimeoutException----" + SOCKET_TIMEOUT_EXCEPTION)
        } else if (throwable is ConnectException) {
            if (HttpApp.logFlag) Log.e(TAG, "onError: ConnectException-----" + CONNECT_EXCEPTION)
        } else if (throwable is UnknownHostException) {
            if (HttpApp.logFlag) Log.e(TAG, "onError: UnknownHostException-----" + UNKNOWN_HOST_EXCEPTION)
        } else if (throwable is IOException) {
            if (HttpApp.logFlag) Log.e(TAG, "onError: IOException-----" + throwable.toString())
        } else {
            if (HttpApp.logFlag) Log.e(TAG, "onError:----" + throwable.message + "\r\n" + throwable.toString())
        }
        throwable.printStackTrace()
        onComplete()
    }

    @RequiresApi(api = Build.VERSION_CODES.GINGERBREAD)
    override fun onComplete() {
        if (get() !== SubscriptionHelper.CANCELLED) {
            lazySet(SubscriptionHelper.CANCELLED)
            try {
                onComplete.run()
            } catch (e: Throwable) {
                e.printStackTrace()
                if (HttpApp.logFlag) Log.e(TAG, e.message, e)
            }

        }
    }

    override fun dispose() {
        cancel()
    }

    override fun isDisposed(): Boolean {
        return get() === SubscriptionHelper.CANCELLED
    }

    override fun request(n: Long) {
        get().request(n)
    }

    override fun cancel() {
        SubscriptionHelper.cancel(this)
    }

    companion object {

        private val TAG = "CustomSubscriber"

        private val SOCKET_TIMEOUT_EXCEPTION = "网络连接超时，请检查您的网络状态，稍后重试"
        private val CONNECT_EXCEPTION = "网络连接异常，请检查您的网络状态"
        private val CONNECT_EXCEPTION_CLIENT = "客户端请求异常，请联系管理员！"
        private val CONNECT_EXCEPTION_401 = "401 未授权 — 未授权客户机访问数据"
        private val CONNECT_EXCEPTION_403 = "403 禁止访问"
        private val CONNECT_EXCEPTION_404 = "404 服务器找不到给定的资源;文档不存在"

        private val CONNECT_EXCEPTION_500 = "500 内部服务器错误"
        private val CONNECT_EXCEPTION_501 = "501 未实现"
        private val CONNECT_EXCEPTION_502 = "502 网关错误"
        private val CONNECT_EXCEPTION_504 = "504 网关超时"
        private val CONNECT_EXCEPTION_505 = "505 HTTP 版本不受支持"

        private val CONNECT_EXCEPTION_SERVER = "服务端请求异常，请联系管理员！"
        private val UNKNOWN_HOST_EXCEPTION = "未知的Host异常，请检查您的网络状态"

    }

}