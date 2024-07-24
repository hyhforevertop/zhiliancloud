package com.matter.myapplication2.DeviceRepository

import TokenManager
import android.util.Log
import com.matter.myapplication2.ui.Device
import com.matter.myapplication2.util.HttpApi
import com.matter.myapplication2.util.HttpClient
import kotlinx.coroutines.suspendCancellableCoroutine
import okhttp3.Call
import okhttp3.Callback
import okhttp3.Response
import org.json.JSONObject
import java.io.IOException
import kotlin.coroutines.resume

object DeviceRepository {
     var devices: List<Device>? = null

    suspend fun getDevices(): List<Device> {
        return devices ?: fetchDevices().also { devices = it }
    }

    suspend fun refreshDevices(): List<Device> {
        Log.e("refreshDevices", "fetchDevices")
        return fetchDevices().also { devices = it }
    }
}

// Function to fetch devices
private suspend fun fetchDevices(): List<Device> {
    return suspendCancellableCoroutine { continuation ->
        HttpClient.get(
            url = "${HttpApi.BASE_URL.value}/device/list/1/100",
            token = TokenManager.getToken(),
            callback = object : Callback {
                override fun onFailure(call: Call, e: IOException) {
                    Log.e("fetchDevices", "onFailure: ${e.message}")
                    continuation.resume(emptyList())
                }

                override fun onResponse(call: Call, response: Response) {
                    if (response.isSuccessful) {
                        val responseBody = response.body?.string()
                        try {
                            val jsonObject = responseBody?.let { JSONObject(it) }
                            val dataObject = jsonObject?.getJSONObject("data")
                            val deviceList = dataObject?.getJSONArray("list")

                            val devices = mutableListOf<Device>()
                            deviceList?.let { array ->
                                for (i in 0 until array.length()) {
                                    val deviceJson = array.getJSONObject(i)
                                    val device = Device(
                                        deviceId = deviceJson.optString("deviceId"),
                                        userId = deviceJson.getInt("userId"),
                                        deviceName = deviceJson.getString("deviceName"),
                                        deviceType = deviceJson.getString("deviceType"),
                                        deviceModel = deviceJson.optString("deviceModel"),
                                        deviceManufacturer = deviceJson.getString("deviceManufacturer"),
                                        deviceStatus = deviceJson.optString("deviceStatus"),
                                        createDate = deviceJson.getString("createDate"),
                                        updateDate = deviceJson.optString("updateDate"),
                                        location = deviceJson.getString("location"),
                                        qrcode = deviceJson.getString("qrcode")?:""
                                    )
                                    devices.add(device)
                                }
                            }
                            Log.e("fetchDevices", "devices: $devices")
                            continuation.resume(devices)
                        }catch (e: Exception)
                        {
                            Log.e("fetchDevices", "onResponse: ${e.message}")
                            continuation.resume(emptyList())
                        }

                    } else {
                        Log.e("fetchDevices", "Response not successful: ${response.body?.string()}")
                        continuation.resume(emptyList())
                    }
                }
            }
        )
    }
}
