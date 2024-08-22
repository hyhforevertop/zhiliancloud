package com.matter.myapplication2.util

import android.util.Log
import okhttp3.ConnectionSpec
import okhttp3.OkHttpClient
import okhttp3.Request
import okhttp3.WebSocket
import okhttp3.WebSocketListener
import okio.ByteString

class WebSocketClient(private val serverUrl: String, private val listener: WebSocketListener) {
    private val client = OkHttpClient.Builder()
        .connectionSpecs(listOf(ConnectionSpec.CLEARTEXT))
        .build()

    private var webSocket: WebSocket? = null

    fun connect() {
        val request = Request.Builder().url(serverUrl).build()
        webSocket = client.newWebSocket(request, listener)
    }

    fun sendMessage(message: String) {
        webSocket?.send(message)
    }

    fun close() {
        webSocket?.close(1000, "Client closing connection")
    }

    companion object {
        const val TAG = "WebSocketClient"
    }
}

interface WebSocketResponseListener {
    fun onMessageReceived(message: String)
    fun onBytesReceived(bytes: ByteString)
}

// Usage example
class MyWebSocketListener(private val responseListener: WebSocketResponseListener,
                          private val setCurrentStatus: (String) -> Unit) : WebSocketListener() {

    override fun onOpen(webSocket: WebSocket, response: okhttp3.Response) {
        Log.d(WebSocketClient.TAG, "WebSocket connected")
    }

    override fun onMessage(webSocket: WebSocket, text: String) {
       // Log.d(WebSocketClient.TAG, "Received message: $text")
        responseListener.onMessageReceived(text)
    }

    override fun onMessage(webSocket: WebSocket, bytes: ByteString) {
       // Log.d(WebSocketClient.TAG, "Received bytes: ${bytes.hex()}")
        responseListener.onBytesReceived(bytes)
    }

    override fun onClosing(webSocket: WebSocket, code: Int, reason: String) {
        //Log.d(WebSocketClient.TAG, "WebSocket closing: $code / $reason")
        webSocket.close(1000, null)
    }

    override fun onClosed(webSocket: WebSocket, code: Int, reason: String) {
        Log.d(WebSocketClient.TAG, "WebSocket closed: $code / $reason")
    }

    override fun onFailure(webSocket: WebSocket, t: Throwable, response: okhttp3.Response?) {
        Log.e(WebSocketClient.TAG, "WebSocket failure", t)
        setCurrentStatus("failed")
    }
}
