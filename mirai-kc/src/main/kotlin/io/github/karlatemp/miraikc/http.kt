/*
 * Copyright (c) 2018-2020 Karlatemp. All rights reserved.
 * @author Karlatemp <karlatemp@vip.qq.com> <https://github.com/Karlatemp>
 * @create 2020/06/20 12:07:06
 *
 * Mirai-KC/Mirai-KC.mirai-kc.main/http.kt
 */

package io.github.karlatemp.miraikc

import kotlinx.coroutines.CompletableDeferred
import org.apache.hc.client5.http.async.methods.SimpleHttpRequest
import org.apache.hc.client5.http.async.methods.SimpleHttpResponse
import org.apache.hc.client5.http.classic.methods.HttpGet
import org.apache.hc.client5.http.impl.async.CloseableHttpAsyncClient
import org.apache.hc.client5.http.impl.async.HttpAsyncClients
import org.apache.hc.client5.http.impl.nio.PoolingAsyncClientConnectionManagerBuilder
import org.apache.hc.core5.concurrent.FutureCallback
import java.io.File

val httpClient = Http.client

private object Http {
    val client: CloseableHttpAsyncClient = HttpAsyncClients.custom()
        .disableCookieManagement()
        .setUserAgent("Java/" + System.getProperty("java.version"))
        .setConnectionManager(
            PoolingAsyncClientConnectionManagerBuilder.create()
                .setMaxConnTotal(500)
                .build()
        )
        .build()

    init {
        client.start()
    }
}

open class HttpResponse<T>(
    val cancelled: Boolean,
    val failed: Exception?,
    val result: T?
) {
    inline fun onCancelled(invoke: () -> Unit): HttpResponse<T> {
        if (cancelled) invoke()
        return this
    }

    inline fun onFailed(invoke: (error: Exception) -> Unit): HttpResponse<T> {
        failed?.run(invoke)
        return this
    }

    inline fun onSuccessful(invoke: (response: T) -> Unit): HttpResponse<T> {
        result?.run(invoke)
        return this
    }
}

suspend fun SimpleHttpRequest.execute(): HttpResponse<SimpleHttpResponse> {
    val cp = CompletableDeferred<HttpResponse<SimpleHttpResponse>>()

    httpClient.execute(this, object : FutureCallback<SimpleHttpResponse> {
        override fun cancelled() {
            cp.complete(
                HttpResponse(
                    cancelled = true,
                    failed = null,
                    result = null
                )
            )
        }

        override fun completed(result: SimpleHttpResponse?) {
            cp.complete(
                HttpResponse(
                    cancelled = false,
                    failed = null,
                    result = result
                )
            )
        }

        override fun failed(ex: java.lang.Exception?) {
            cp.complete(
                HttpResponse(
                    cancelled = false,
                    failed = ex,
                    result = null
                )
            )
        }

    })

    return cp.await()
}

val caches = File("data/cache")

suspend fun downloadCache(cacheName: String, cacheUrl: String): File? {
    val cacheFile = File(caches, cacheName)
    if (cacheFile.isFile)
        return cacheFile
    cacheFile.parentFile.mkdirs()
    val execute = SimpleHttpRequest.copy(HttpGet(cacheUrl)).execute()
    execute.onSuccessful {
        cacheFile.writeBytes(it.bodyBytes)
        return cacheFile
    }
    return null
}
