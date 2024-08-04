package com.matter.myapplication2.DeviceRepository

import com.matter.myapplication2.ui.Device

object DeviceRepository {
    private var devices: List<Device> = emptyList() // 使用不可变的空列表初始化

    fun setDevices(deviceList: List<Device>) {
        devices = deviceList.toList() // 使用 toList() 创建一个新的 List
    }

    fun getDevices(): List<Device> {
        return devices // 返回不可变的 devices 列表
    }
}
