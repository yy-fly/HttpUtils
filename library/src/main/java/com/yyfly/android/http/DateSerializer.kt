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

import com.google.gson.*
import com.google.gson.reflect.TypeToken
import java.lang.reflect.Type
import java.text.ParseException
import java.text.SimpleDateFormat
import java.util.*

/**
 * 日期序列化
 *
 * @author : yyfly / developer@yyfly.com
 * @version : 1.0
 */
class DateSerializer : JsonSerializer<Date>, JsonDeserializer<Date> {

    @Throws(JsonParseException::class)
    override fun deserialize(json: JsonElement, typeOfT: Type, context: JsonDeserializationContext): Date? {
        if (null != json.asString) {
            try {
                return simpleDateFormat.parse(json.asString)
            } catch (e: ParseException) {
                return null
            }

        }
        return null
    }

    override fun serialize(src: Date?, typeOfSrc: Type, context: JsonSerializationContext): JsonElement? {
        if (null != src) {
            return JsonPrimitive(src.time)
        }
        return null
    }

    companion object {

        val DEFAULT_DATE_FORMAT = "yyyy-MM-dd HH:mm:ss"
        val simpleDateFormat = SimpleDateFormat(DEFAULT_DATE_FORMAT, Locale.getDefault())
        val defaultGson = GsonBuilder().setDateFormat(DEFAULT_DATE_FORMAT).registerTypeAdapter(object : TypeToken<Date>() {

        }.type, DateSerializer()).create()
    }

}
