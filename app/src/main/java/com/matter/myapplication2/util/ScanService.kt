package com.matter.myapplication2.util

import NetworkUtils
import TokenManager
import android.app.Service
import android.content.Intent
import android.os.IBinder
import android.util.Log
import android.widget.Toast
import es.dmoral.toasty.Toasty
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.Job
import kotlinx.coroutines.cancel
import kotlinx.coroutines.launch


class ScanService : Service() {
    private val serviceScope = CoroutineScope(Dispatchers.IO + Job())

    override fun onStartCommand(intent: Intent?, flags: Int, startId: Int): Int {
        serviceScope.launch {
            try {
                val localIp = NetworkUtils.getLocalIpAddress()
                println("Local IP Address: $localIp")

                val subnet = localIp?.let { NetworkUtils.getSubnet(it) }
                if (subnet != null) {
                    val httpPort = 8080
                    val wsPort = 5580
                    val path = "/ws"

                    // Launch HTTP scan in background
                    val httpScanJob = launch(Dispatchers.IO) {
                        NetworkUtils.scanOpenPorts(subnet, httpPort) { openIp ->
                            if (openIp != null) {
                                println("Open HTTP IP: $openIp")

                                serviceScope.launch(Dispatchers.Main) {
                                    Toasty.success(applicationContext, "HOST IP: ${openIp}", Toast.LENGTH_SHORT).show()
                                }

                                TokenManager.setHostIpv4(openIp)

                            } else {

                                println("No HTTP Port Open found")
                                serviceScope.launch(Dispatchers.Main) {
                                    Toasty.success(applicationContext, "HOST IP: 无", Toast.LENGTH_SHORT).show()
                                }
                            }
                        }
                    }

                    // Launch WebSocket scan in background
                    val wsScanJob = launch(Dispatchers.IO) {
                        NetworkUtils.scanWebSocketPorts(subnet, wsPort, path) { openIp ->
                            if (openIp != null) {
                                println("Open WebSocket IP: $openIp")
                                serviceScope.launch(Dispatchers.Main) {
                                    Toasty.success(applicationContext, "MatterServer IP: $openIp", Toast.LENGTH_SHORT).show()
                                }
                                TokenManager.setMatterIpv4(openIp)
                            } else {
                                println("No WebSocket Port Open found")
                                serviceScope.launch(Dispatchers.Main) {
                                    Toasty.error(applicationContext, "MatterServer IP: 无", Toast.LENGTH_SHORT).show()
                                }
                            }
                        }
                    }

                    // Wait for both jobs to complete
                    httpScanJob.join()
                    wsScanJob.join()

                    stopSelf()
                } else {
                    println("Could not determine subnet")
                }
            } catch (e: Exception) {
                e.printStackTrace()
                Log.e("ScanService", "Error in startScanning: ${e.message}")
            }
        }
        return START_STICKY
    }

    override fun onDestroy() {
        super.onDestroy()
        serviceScope.cancel()
        NetworkUtils.httpThreadPool.shutdownNow()
        NetworkUtils.wsThreadPool.shutdownNow()
    }

    override fun onBind(intent: Intent?): IBinder? {
        return null
    }
}