package com.matter.myapplication2.ui

import APICommand
import DisplayMjpegStream
import MainScreen
import android.os.Build
import android.util.Log
import androidx.annotation.RequiresApi
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
import androidx.compose.runtime.LaunchedEffect
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
import com.google.gson.JsonObject
import com.matter.myapplication2.DeviceFunction.AirCondition
import com.matter.myapplication2.DeviceFunction.DeviceBrightness
import com.matter.myapplication2.DeviceFunction.Fan
import com.matter.myapplication2.DeviceFunction.Light_Belt
import com.matter.myapplication2.DeviceFunction.QRcodeDisplay
import com.matter.myapplication2.DeviceFunction.TemperatureSensor
import com.matter.myapplication2.DeviceRepository.DeviceRepository
import com.matter.myapplication2.util.HttpApi
import com.matter.myapplication2.util.MyWebSocketListener
import com.matter.myapplication2.util.WebSocketClient
import com.matter.myapplication2.util.WebSocketResponseListener
import okio.ByteString
import org.json.JSONObject
import java.util.UUID


@RequiresApi(Build.VERSION_CODES.TIRAMISU)
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
    var setup_qr_code by remember {
        mutableStateOf("")
    }
    var isOk by remember {
        mutableStateOf(false)
    }
    if(isVisible)
    {
        val TAG="GET QR CODE"
        LaunchedEffect(Unit) {
            val WS_URL = HttpApi.WS_HOST.value
            val webSocketClient = WebSocketClient(WS_URL, MyWebSocketListener(object :
                WebSocketResponseListener {
                override fun onMessageReceived(message: String) {
                    Log.e(TAG, message)
                    try {
                        val response = JSONObject(message)
                        val result=response.getJSONObject("result")
                        val get_qr_code = result.getString("setup_qr_code")
                        setup_qr_code=get_qr_code
                        isOk=true

                    } catch (e: Exception) {
                            Log.e(TAG, "parse json fail!")
                            Log.e(TAG, e.toString())
                            setCurrentStatus("failed")
                    }

                }

                override fun onBytesReceived(bytes: ByteString) {
                    println("Bytes received: ${bytes.hex()}")
                }

            }, ::setCurrentStatus))

            try {
                webSocketClient.connect()
                val jsonMessage = JsonObject().apply {
                    addProperty("message_id", UUID.randomUUID().toString())
                    addProperty("command", APICommand.OPEN_COMMISSIONING_WINDOW.value)

                    val args = JsonObject().apply {
                        addProperty("node_id", device?.nodeId)
                    }
                    add("args", args)
                }
                Log.e(TAG, jsonMessage.toString())
                webSocketClient.sendMessage(jsonMessage.toString())
            }catch (e:Exception)
            {
                Log.e(TAG, "WebSocketClient connect fail!")
                Log.e(TAG, e.toString())
                setCurrentStatus("failed")

            }
        }
    }
    MainScreen(
        navController = navController, paddingContent =
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

//                item {
//                    Row {
//                        Text("桥街于: ", fontWeight = FontWeight.Bold)
//                        Text("网关E1")
//
//                    }
//                }
//                item {
//                    Row {
//                        Text("检测状态: ", fontWeight = FontWeight.Bold)
//                        Text("未检测到行为")
//
//                    }
//                }
//                item {
//                    Row {
//                        Text("设备状态: ", fontWeight = FontWeight.Bold)
//                        Text(device?.deviceStatus.toString())
//                    }
//                }
                if (deviceMap(device?.deviceType.toString()) == "无线调光灯") {
                    item {
                        val coroutineScope = rememberCoroutineScope()
                        val context = LocalContext.current

                        //Device_Switch(device, coroutineScope, context)
                        DeviceBrightness(device, coroutineScope, context)
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
                else if (deviceMap(device?.deviceType.toString()) == "风扇")
                {
                    item { 
                        Fan(device = deviceFind)
                    }
                }
                else if (deviceMap(device?.deviceType.toString()) == "无线彩灯"){
                    item { 
                        Light_Belt(device = deviceFind)
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
                    if (isOk)
                    QRcodeDisplay(qrcode = setup_qr_code )

                }

            }

        }
    }, currentPage = "deviceDetail"
    )
}

fun setCurrentStatus(status:String) : String
{
    return status.toString()
}