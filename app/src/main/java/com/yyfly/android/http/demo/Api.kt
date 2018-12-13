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

import io.reactivex.Flowable
import retrofit2.http.GET
import retrofit2.http.Query

/**
 * 接口定义
 *
 * @author : yyfly / developer@yyfly.com
 * @version : 1.0
 */
interface Api {

    @GET("/users")
    fun list(@Query("start") start: Int,
             @Query("limit") limit: Int,
             @Query("state") state: Int): Flowable<ResponseData<ListData<User>>>
}