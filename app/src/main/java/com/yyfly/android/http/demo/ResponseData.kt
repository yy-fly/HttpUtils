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
package com.yyfly.android.http.demo

import com.yyfly.android.http.BaseResponseData


class ResponseData<T> : BaseResponseData() {

    var code: Int? = null

    /**
     * 返回的消息
     */
    var msg: String? = null

    /**
     * 返回的数据
     */
    var data: T? = null

    override fun isSuccess(): Boolean {
        if (code!!.toInt() == 0) {
            return true
        }
        return false
    }

    override fun responseCode(): Int? {
        return code
    }

    override fun responseMessage(): String {
        return msg ?: ""
    }

    override fun isOauth(): Boolean? {
        return true
    }
}