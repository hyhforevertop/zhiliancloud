package com.matter.myapplication2.ui

import DisplayMjpegStream
import MainScreen
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Button
import androidx.compose.material3.Divider
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.navigation.NavHostController
import com.matter.myapplication2.DeviceFunction.AirCondition
import com.matter.myapplication2.DeviceFunction.Device_Brightness
import com.matter.myapplication2.DeviceFunction.Device_Switch
import com.matter.myapplication2.DeviceFunction.QRcodeDisplay
import com.matter.myapplication2.DeviceFunction.TemperatureSensor
import com.matter.myapplication2.DeviceRepository.DeviceRepository


@Composable
fun DeviceDetail(
    modifier: Modifier = Modifier,
    deviceId: String,
    navController: NavHostController
) {
    val deviceFind = DeviceRepository.getDevices().find { it.deviceId == deviceId }
    val device by remember { mutableStateOf(deviceFind) }
    val deviceType = deviceMap(device?.deviceType.toString())

    var isVisible by remember { mutableStateOf(false) }

    MainScreen(navController = navController, paddingContent =
    { paddingValues ->
        Surface(
            modifier = Modifier
                .padding(paddingValues)
                .padding(vertical = 10.dp, horizontal = 10.dp)
                .shadow(2.dp, RoundedCornerShape(16.dp))
                .clip(shape = RoundedCornerShape(16.dp))
                .fillMaxSize(),
            color = Color.White
        ) {
            LazyColumn(
                modifier = Modifier.padding(10.dp),
                verticalArrangement = Arrangement.spacedBy(10.dp)
            ) {

                item {
                    Row {
                        Text("设备名称: ", fontWeight = FontWeight.Bold)
                        Text(device?.deviceName.toString())
                    }
                }
                item {
                    Row {
                        Text("设备类型: ", fontWeight = FontWeight.Bold)
                        Text(deviceType)
                    }
                }
                item {
                    Row {
                        Text("设备状态: ", fontWeight = FontWeight.Bold)
                        Text(device?.deviceStatus.toString())
                    }
                }
                if (deviceMap(device?.deviceType.toString()) == "无线调光灯") {
                    item {
                        val coroutineScope = rememberCoroutineScope()
                        val context = LocalContext.current

                        Device_Switch(device, coroutineScope, context)
                        Device_Brightness(device, coroutineScope, context)
                    }
                }
                else if (deviceMap(device?.deviceType.toString()) == "温度传感器") {
                    item {
                        val coroutineScope = rememberCoroutineScope()

                        TemperatureSensor(deviceFind, coroutineScope)
                    }
                }
                else if (deviceMap(device?.deviceType.toString()) == "空气质量传感器")
                {
                    item { 
                        val coroutineScope = rememberCoroutineScope()
                        AirCondition(device = deviceFind, coroutineScope = coroutineScope)
                    }
                }
                else if (deviceMap(device?.deviceType.toString()) == "摄像头")
                {
                    item {
                        DisplayMjpegStream()
                    }
                }



                item {
                    Button(onClick = { isVisible = !isVisible }) {
                        Text(text = if (isVisible) "隐藏二维码" else "显示二维码", color = Color.White)
                    }
                }

                if (isVisible)
                item {
                    Spacer(modifier = Modifier.height(16.dp))
                    Divider(modifier = Modifier.height(2.dp))
                    QRcodeDisplay(qrcode = device?.qrcode ?: "")
                }

            }

        }
    }, currentPage = "deviceDetail"
    )
}
