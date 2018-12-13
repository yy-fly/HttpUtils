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

import android.os.Bundle
import android.support.v7.app.AppCompatActivity
import android.util.Log
import com.yyfly.android.http.CustomSubscriber
import io.reactivex.android.schedulers.AndroidSchedulers
import io.reactivex.functions.Consumer
import io.reactivex.schedulers.Schedulers

class MainActivity : AppCompatActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)
        HttpUtils.retrofit("")
                .create(Api::class.java)
                .list(0, 10, 0)
                .subscribeOn(Schedulers.io())
                .observeOn(AndroidSchedulers.mainThread())
                .subscribe(CustomSubscriber(Consumer<ResponseData<ListData<User>>> {
                    responseData ->
                    if (responseData.isSuccess()) {
                        responseData.data?.list?.let {
                            it.forEach {
                                Log.i("User", it.name)
                            }
                        }
                    }

                }, Consumer {

                }))

    }
}
