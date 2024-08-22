package com.matter.myapplication2.DeviceFunction

import TokenManager
import android.content.Context
import android.util.Log
import android.widget.Toast
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Slider
import androidx.compose.material3.Switch
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateListOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.google.gson.JsonObject
import com.matter.myapplication2.ui.Device
import com.matter.myapplication2.ui.setCurrentStatus
import com.matter.myapplication2.util.HttpApi
import com.matter.myapplication2.util.MyWebSocketListener
import com.matter.myapplication2.util.WebSocketClient
import com.matter.myapplication2.util.WebSocketResponseListener
import es.dmoral.toasty.Toasty
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.delay
import kotlinx.coroutines.withContext
import okhttp3.MediaType.Companion.toMediaTypeOrNull
import okhttp3.OkHttpClient
import okhttp3.Request
import okhttp3.RequestBody.Companion.toRequestBody
import okio.ByteString
import org.json.JSONObject
import java.util.UUID

@Composable
fun Device_Switch(device: Device?, coroutineScope: CoroutineScope, context: Context) {
    var switchStatus by remember { mutableStateOf(device?.deviceStatus == "On") }

    var isChecked by remember { mutableStateOf(false) }

    if (isChecked)
        LaunchedEffect(isChecked) {
            val check = changeDeviceStatus(device)
            if (check) {
                Toasty.success(context, "操作成功", Toast.LENGTH_SHORT).show()
                changeStatus(device)
            } else {
                Toasty.error(context, "操作失败", Toast.LENGTH_SHORT).show()
            }
            isChecked = false
        }

    Row(
        modifier = Modifier,
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.spacedBy(20.dp)
    ) {
        Text("开关: ", fontWeight = FontWeight.Bold)
        Switch(
            checked = switchStatus,
            onCheckedChange = {
                isChecked = true
//                isChecked ->
//                switchStatus = isChecked
//                device.value = device.value?.copy(deviceStatus = if (isChecked) "On" else "Off")
//                coroutineScope.launch {
//                    Turn_On_Off_Light(device, context)
//                }


            }
        )
    }


}

fun changeStatus(device: Device?) {
    if (device?.deviceStatus == "On") {
        device.deviceStatus = "Off"
    } else {
        device?.deviceStatus = "On"
    }
}

@Composable
fun DeviceBrightness(
    device: Device?,
    coroutineScope: CoroutineScope,
    context: Context,
    modifier: Modifier = Modifier
) {
    var brightnessLevels = remember { mutableStateListOf(0.5f, 0.5f, 0.5f, 0.5f, 0.5f, 0.5f, 0.5f, 0.5f) }
    var isChecked by remember { mutableStateOf(false) }
    var selected by remember { mutableStateOf(0) }

    if (isChecked) {
        LaunchedEffect(Unit) {
            val WS_URL = HttpApi.WS_HOST.value
            val webSocketClient = WebSocketClient(
                WS_URL,
                MyWebSocketListener(
                    object : WebSocketResponseListener {
                        override fun onMessageReceived(message: String) {
                            Log.e("changeLightMode", "Message received: $message")
                        }

                        override fun onBytesReceived(bytes: ByteString) {
                            println("Bytes received: ${bytes.hex()}")
                        }
                    },
                    ::setCurrentStatus
                )
            )

            val level = brightnessLevels[selected - 1].toInt()

            try {
                webSocketClient.connect()

                val jsonMessage = JsonObject().apply {
                    addProperty("message_id", UUID.randomUUID().toString())
                    addProperty("command", "device_command")

                    val payload = JsonObject().apply {
                        addProperty("level", level)
                        addProperty("transitionTime", 0)
                        addProperty("optionsMask", 0)
                        addProperty("optionsOverride", 0)
                    }

                    val args = JsonObject().apply {
                        addProperty("node_id", device!!.nodeId)
                        addProperty("endpoint_id", selected)
                        addProperty("cluster_id", 8)
                        addProperty("command_name", "MoveToLevelWithOnOff")
                        add("payload", payload)
                    }

                    add("args", args)
                }

                Log.e("changeLightMode", jsonMessage.toString())
                webSocketClient.sendMessage(jsonMessage.toString())

            } catch (e: Exception) {
                Log.e("changeLightMode", "Error: ${e.message}")
            }

            isChecked = false
        }
    }

    LaunchedEffect(key1 = Unit) {
        while (true) {

            var current = 1
            val WS_URL = HttpApi.WS_HOST.value
            val webSocketClient = WebSocketClient(
                WS_URL,
                MyWebSocketListener(
                    object : WebSocketResponseListener {
                        override fun onMessageReceived(message: String) {
                           // Log.e("getLevel loop", "Message received: $message")
                            try {
                                val json = JSONObject(message)

                                // 检查 "result" 是否存在并且非空
                                if (json.has("result") && !json.isNull("result")) {
                                    val result = json.getJSONObject("result")

                                    // 检查指定的 key 是否存在并且非空
                                    val key = "${current}/8/0"
                                    if (result.has(key) && !result.isNull(key)) {
                                        val levelString = result.getString(key)
                                        val level = levelString.toInt()


                                        brightnessLevels[current-1]=level.toFloat()


                                        current+=1

                                        if (current==9)
                                            current=1

                                        Log.e("getLevel level", "${current} level: $level")
                                    } else {
                                        Log.e("getLevel", "Key $key does not exist or is null")
                                    }
                                } else {
                                    Log.e("getLevel", "\"result\" does not exist or is null")
                                }
                            } catch (e: Exception) {
                                Log.e("getLevel", "Error processing message: ${e.message}")
                            }
                        }



                        override fun onBytesReceived(bytes: ByteString) {
                            //println("Bytes received: ${bytes.hex()}")
                        }
                    },
                    ::setCurrentStatus
                )
            )
            try {

                webSocketClient.connect()

                for (i in 1..8) {

                    val message = """
                    {
                      "message_id": "e7184b2b1d5b4c808b0834a18b041cfc",
                      "command": "read_attribute",
                      "args": {
                        "node_id": ${device!!.nodeId},
                        "attribute_path": "${i}/8/0"
                      }
                    }
                """.trimIndent()

                    webSocketClient.sendMessage(message)
                }
            }
            catch (e: Exception)
            {
                Log.e("getLevel", "Error: ${e.message}")
            }

            delay(1000)
        }
    }

    Column(modifier = modifier) {
        (1..8).forEach { i ->
            val lightName=when(i)
                {
                    1 -> "书房"
                    2 -> "厨房"
                    3 -> "二楼客厅"
                    4 -> "杂物间"
                    5 -> "入口"
                    6 -> "卧室"
                    7 -> "客厅"
                    8 -> "警告灯"
                    else -> ""
                }
            Row(verticalAlignment = Alignment.CenterVertically) {
                Text(
                    text = "${lightName} 亮度: ",
                    fontWeight = FontWeight.Bold,
                    modifier = Modifier.padding(bottom = 8.dp)
                )
                Slider(
                    value = brightnessLevels[i - 1],
                    onValueChange = { value ->
                        brightnessLevels[i - 1] = value
                    },
                    onValueChangeFinished = {
                        isChecked = true
                        selected = i
                    },
                    valueRange = 0f..250f,
                    steps = 254,
                    modifier = Modifier.fillMaxWidth()
                )
            }
        }
    }
}


suspend fun changeDeviceStatus(device: Device?) = withContext(Dispatchers.IO) {

    val json=""
    val mediaType = "application/json; charset=utf-8".toMediaTypeOrNull()
    val requestBody = json.toRequestBody(mediaType)

    val client = OkHttpClient()
    val request = Request.Builder()
        .url("${HttpApi.LIGHT_OPERATION.value}/${device?.deviceId}")
        .addHeader("Authorization", "${TokenManager.getToken()}")
        .post(requestBody)
        .build()
    val response = client.newCall(request).execute()

    if (response.code == 200) {
        Log.e("Turn_On_Off_Light", response.body?.string().toString())
        return@withContext true
    } else {
        Log.e("Turn_On_Off_Light", response.body?.string().toString())
        return@withContext false
    }
}


suspend fun adjustBrightness(device: Device?, brightnessLevel: Float) =
    withContext(Dispatchers.IO) {

        val json = """
            {
                "deviceId": "${device?.deviceId}",
                "level": ${brightnessLevel.toInt()},
                "transitionTime": 1,
                "optionMask": 1,
                "optionOverride": 1
            }
        """
        Log.e("AdjustBrightness", json)

        val mediaType = "application/json; charset=utf-8".toMediaTypeOrNull()
        val requestBody = json.toRequestBody(mediaType)
        val client = OkHttpClient()
        val request = Request.Builder()
            .url(HttpApi.LIGHT_ADJUST.value)
            .addHeader("Authorization", "${TokenManager.getToken()}")
            .post(requestBody)
            .build()
        val response = client.newCall(request).execute()

        return@withContext response.code == 200
    }
