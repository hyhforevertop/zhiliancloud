package com.matter.myapplication2.DeviceRepository

import android.content.Context
import com.google.gson.Gson
import com.matter.myapplication2.QRcodeScan.CHIPDeviceInfo
import java.io.File

object DeviceInfoStorage {

    private const val FILE_NAME = "device_info.json"

    fun saveDeviceInfo(context: Context, deviceInfo: CHIPDeviceInfo) {
        val file = File(context.filesDir, FILE_NAME)
        val json = Gson().toJson(deviceInfo)
        file.writeText(json)
    }

    fun getDeviceInfo(context: Context): CHIPDeviceInfo? {
        val file = File(context.filesDir, FILE_NAME)
        if (!file.exists()) return null
        val json = file.readText()
        return Gson().fromJson(json, CHIPDeviceInfo::class.java)
    }

    fun clearDeviceInfo(context: Context) {
        val file = File(context.filesDir, FILE_NAME)
        if (file.exists()) {
            file.delete()
        }
    }
}
