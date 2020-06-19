/*
 * Copyright (c) 2018-2020 Karlatemp. All rights reserved.
 * @author Karlatemp <karlatemp@vip.qq.com> <https://github.com/Karlatemp>
 * @create 2020/06/18 18:03:54
 *
 * Mirai-KC/Mirai-KC.mirai-kc.main/json.kt
 */

package io.github.karlatemp.miraikc

import com.google.gson.JsonArray
import com.google.gson.JsonObject
import com.google.gson.JsonPrimitive

@DslMarker
annotation class JsonElementBuilderDsl

@JsonElementBuilderDsl
open class JsonElementBuilder

@JsonElementBuilderDsl
open class JsonArrayBuilder(val array: JsonArray) : JsonElementBuilder() {
    @JsonElementBuilderDsl
    fun value(value: String) {
        value(JsonPrimitive(value))
    }

    @JsonElementBuilderDsl
    fun value(value: Number) {
        value(JsonPrimitive(value))
    }

    @JsonElementBuilderDsl
    fun value(value: Boolean) {
        value(JsonPrimitive(value))
    }

    @JsonElementBuilderDsl
    fun value(value: Char) {
        value(JsonPrimitive(value))
    }

    @JsonElementBuilderDsl
    fun value(value: JsonPrimitive) {
        array.add(value)
    }

    @JsonElementBuilderDsl
    fun obj(value: JsonObjectBuilder.() -> Unit) {
        array.add(JsonObject().also { value(JsonObjectBuilder(it)) })
    }

    @JsonElementBuilderDsl
    fun array(value: JsonArrayBuilder.() -> Unit) {
        array.add(JsonArray().also { value(JsonArrayBuilder(it)) })
    }
}

@JsonElementBuilderDsl
open class JsonObjectBuilder(val obj: JsonObject) : JsonElementBuilder() {
    @JsonElementBuilderDsl
    infix fun String.value(value: String) {
        value(JsonPrimitive(value))
    }

    @JsonElementBuilderDsl
    infix fun String.value(value: Number) {
        value(JsonPrimitive(value))
    }

    @JsonElementBuilderDsl
    infix fun String.value(value: Boolean) {
        value(JsonPrimitive(value))
    }

    @JsonElementBuilderDsl
    infix fun String.value(value: Char) {
        value(JsonPrimitive(value))
    }

    @JsonElementBuilderDsl
    infix fun String.value(value: JsonPrimitive) {
        obj.add(this, value)
    }

    @JsonElementBuilderDsl
    infix fun String.obj(value: JsonObjectBuilder.() -> Unit) {
        obj.add(this, JsonObject().also { value(JsonObjectBuilder(it)) })
    }

    @JsonElementBuilderDsl
    infix fun String.array(value: JsonArrayBuilder.() -> Unit) {
        obj.add(this, JsonArray().also { value(JsonArrayBuilder(it)) })
    }
}

fun JsonArray.build(invoke: JsonArrayBuilder.() -> Unit): JsonArray {
    return JsonArrayBuilder(this).apply { invoke() }.array
}

fun JsonObject.build(invoke: JsonObjectBuilder.() -> Unit): JsonObject {
    return JsonObjectBuilder(this).apply { invoke() }.obj
}
