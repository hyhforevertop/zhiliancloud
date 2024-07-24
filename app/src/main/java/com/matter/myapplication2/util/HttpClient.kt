package com.matter.myapplication2.util

import okhttp3.Callback
import okhttp3.MediaType.Companion.toMediaType
import okhttp3.OkHttpClient
import okhttp3.Request
import okhttp3.RequestBody.Companion.toRequestBody

object HttpClient {
    private val client = OkHttpClient()

    private val JSON = "application/json; charset=utf-8".toMediaType()


    fun get(url: String, token: String?, callback: Callback) {
        val requestBuilder = Request.Builder()
            .url(url)

        // Add Authorization header if token is provided
        token?.let {
            requestBuilder.addHeader("Authorization", "$token")
        }

        val request = requestBuilder.build()

        client.newCall(request).enqueue(callback)
    }

      fun post(url: String, json: String, token: String?, callback: Callback) {
        val body = json.toRequestBody(JSON)

        val requestBuilder = Request.Builder()
            .url(url)
            .post(body)


        // Add Authorization header if token is provided
        token?.let {
            requestBuilder.addHeader("Authorization", "$token")
        }

        val request = requestBuilder.build()

        client.newCall(request).enqueue(callback)
    }

    const val TAG = "HttpClient"
}
