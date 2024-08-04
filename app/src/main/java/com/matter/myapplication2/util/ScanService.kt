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
import kotlinx.coroutines.cancelChildren
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext

class ScanService : Service() {
    private val serviceScope = CoroutineScope(Dispatchers.IO + Job())

    override fun onStartCommand(intent: Intent?, flags: Int, startId: Int): Int {
        // Cancel any existing jobs to avoid creating multiple concurrent jobs
        serviceScope.coroutineContext.cancelChildren()

        serviceScope.launch {
            try {
                val localIp = NetworkUtils.getLocalIpAddress()
                println("Local IP Address: $localIp")

                withContext(Dispatchers.Main) {
                    Toasty.success(applicationContext, "当前 IP: $localIp", Toast.LENGTH_SHORT).show()
                }

                val subnet = localIp?.let { NetworkUtils.getSubnet(it) }
                if (subnet != null) {
                    val httpPort = 8080
                    val wsPort = 5580
                    val path = "/ws"

                    val httpScanJob = launch {
                        NetworkUtils.scanOpenPorts(subnet, httpPort) { openIp ->

                            if (openIp != null) {
                                println("Open HTTP IP: $openIp")
//                                withContext(Dispatchers.Main) {
//                                    Toasty.success(applicationContext, "HOST IP: $openIp", Toast.LENGTH_SHORT).show()
//                                }
                                TokenManager.setHostIpv4(openIp)
                            } else {
                                println("No HTTP Port Open found")
//                                withContext(Dispatchers.Main) {
//                                    Toasty.error(applicationContext, "HOST IP: 无", Toast.LENGTH_SHORT).show()
//                                }
                            }
                        }
                    }

                    val wsScanJob = launch {
                        NetworkUtils.scanWebSocketPorts(subnet, wsPort, path) { openIp ->
                            if (openIp != null) {
                                println("Open WebSocket IP: $openIp")
//                                withContext(Dispatchers.Main) {
//                                    Toasty.success(applicationContext, "MatterServer IP: $openIp", Toast.LENGTH_SHORT).show()
//                                }
                                TokenManager.setMatterIpv4(openIp)
                            } else {
                                println("No WebSocket Port Open found")
//                                withContext(Dispatchers.Main) {
//                                    Toasty.error(applicationContext, "MatterServer IP: 无", Toast.LENGTH_SHORT).show()
//                                }
                            }
                        }
                    }

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
    }

    override fun onBind(intent: Intent?): IBinder? {
        return null
    }
}
