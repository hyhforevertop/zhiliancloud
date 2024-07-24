

import android.util.Log
import org.java_websocket.client.WebSocketClient
import org.java_websocket.handshake.ServerHandshake
import java.io.IOException
import java.net.NetworkInterface
import java.net.Socket
import java.net.URI
import java.util.Collections
import java.util.concurrent.Executors
import java.util.concurrent.TimeUnit
import java.util.concurrent.atomic.AtomicBoolean

object NetworkUtils {
     internal val httpThreadPool = Executors.newFixedThreadPool(50)
    internal val wsThreadPool = Executors.newFixedThreadPool(50)

    fun getLocalIpAddress(): String? {
        try {
            val interfaces = Collections.list(NetworkInterface.getNetworkInterfaces())
            for (networkInterface in interfaces) {
                val addresses = Collections.list(networkInterface.inetAddresses)
                for (address in addresses) {
                    if (!address.isLoopbackAddress) {
                        val hostAddress = address.hostAddress
                        if (hostAddress.contains('.')) {
                            return hostAddress
                        }
                    }
                }
            }
        } catch (ex: Exception) {
            ex.printStackTrace()
        }
        return null
    }

    fun getSubnet(ip: String): String? {
        val parts = ip.split(".")
        return if (parts.size == 4) {
            "${parts[0]}.${parts[1]}.${parts[2]}"
        } else {
            null
        }
    }

    fun scanOpenPorts(subnet: String, port: Int, callback: (String?) -> Unit) {
        val found = AtomicBoolean(false)

        for (i in 1..254) {
            val ip = "$subnet.$i"
            if (!httpThreadPool.isShutdown) {
                httpThreadPool.execute {
                    if (!found.get() && !httpThreadPool.isShutdown) {
                        try {
                            Socket(ip, port).use {
                                if (found.compareAndSet(false, true)) {
                                    callback(ip)
                                    httpThreadPool.shutdownNow()
                                    Log.i("NetworkUtils", "HTTP Port Open at $ip")
                                }
                            }
                        } catch (e: IOException) {
                            // Port is closed or IP is not reachable
                        }
                    }
                }
            }
        }

        httpThreadPool.shutdown()
        try {
            if (!httpThreadPool.awaitTermination(60, TimeUnit.SECONDS)) {
                httpThreadPool.shutdownNow()
            }
            if (!found.get()) {
                callback(null)
                Log.i("NetworkUtils", "No HTTP Port Open found")
            }
        } catch (e: InterruptedException) {
            httpThreadPool.shutdownNow()
        }
    }

    fun scanWebSocketPorts(subnet: String, port: Int, path: String, callback: (String?) -> Unit) {
        val found = AtomicBoolean(false)

        for (i in 1..254) {
            val ip = "$subnet.$i"
            if (!wsThreadPool.isShutdown) {
                wsThreadPool.execute {
                    if (!found.get() && !wsThreadPool.isShutdown) {
                        try {
                            val uri = URI("ws://$ip:$port$path")
                            val client = object : WebSocketClient(uri) {
                                override fun onOpen(handshakedata: ServerHandshake?) {
                                    if (found.compareAndSet(false, true)) {
                                        callback(ip)
                                        wsThreadPool.shutdownNow()
                                        Log.i("NetworkUtils", "WebSocket Port Open at $ip")
                                    }
                                    close()
                                }

                                override fun onMessage(message: String?) {}
                                override fun onClose(code: Int, reason: String?, remote: Boolean) {}
                                override fun onError(ex: Exception?) {}
                            }
                            client.connectBlocking(2, TimeUnit.SECONDS)
                        } catch (e: Exception) {
                            // Connection failed, port is closed or not a WebSocket server
                        }
                    }
                }
            }
        }

        wsThreadPool.shutdown()
        try {
            if (!wsThreadPool.awaitTermination(60, TimeUnit.SECONDS)) {
                wsThreadPool.shutdownNow()
            }
            if (!found.get()) {
                callback(null)
                Log.i("NetworkUtils", "No WebSocket Port Open found")
            }
        } catch (e: InterruptedException) {
            wsThreadPool.shutdownNow()
        }
    }
}