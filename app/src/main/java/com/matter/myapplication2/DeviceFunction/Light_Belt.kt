package com.matter.myapplication2.DeviceFunction

import TokenManager
import android.util.Log
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Button
import androidx.compose.material3.Slider
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableFloatStateOf
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.matter.myapplication2.ui.Device
import com.matter.myapplication2.ui.setCurrentStatus
import com.matter.myapplication2.util.HttpApi
import com.matter.myapplication2.util.MyWebSocketListener
import com.matter.myapplication2.util.WebSocketClient
import com.matter.myapplication2.util.WebSocketResponseListener
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.delay
import kotlinx.coroutines.withContext
import okhttp3.OkHttpClient
import okhttp3.Request
import okhttp3.RequestBody.Companion.toRequestBody
import okio.ByteString
import org.json.JSONObject
import java.util.UUID


@Composable
fun Light_Belt(device: Device?) {

    var modeSelect by remember { mutableIntStateOf(0) }
    val context = LocalContext.current
    LaunchedEffect(key1 = modeSelect) {

//        val check = changeLightMode(device, modeSelect)
//        if (check) {
//            Toasty.success(context, "修改成功", Toasty.LENGTH_SHORT).show()
//        } else {
//            Toasty.error(context, "修改失败", Toasty.LENGTH_SHORT).show()
//        }


        val WS_URL = HttpApi.WS_HOST.value
        val webSocketClient = WebSocketClient(WS_URL, MyWebSocketListener(object :
            WebSocketResponseListener {
            override fun onMessageReceived(message: String) {
                Log.e("changeLightMode", "Message received: $message")

            }

            override fun onBytesReceived(bytes: ByteString) {
                println("Bytes received: ${bytes.hex()}")
            }

        }, ::setCurrentStatus))

        try {
            webSocketClient.connect()

            val jsonMessage = """
    {
        "message_id": "${UUID.randomUUID()}",
        "command": "device_command",
        "args": {
            "endpoint_id": 2,
            "node_id": "${device?.nodeId}",
            "payload": {
                "newMode": $modeSelect
            },
            "cluster_id": 80,
            "command_name": "ChangeToMode"
        }
    }
""".trimIndent()

            Log.e("changeLightMode", jsonMessage.toString())
            webSocketClient.sendMessage(jsonMessage.toString())
        } catch (e: Exception) {
            Log.e("changeLightMode", "Error: ${e.message}")
        }
    }

    var brightnessLevel by remember { mutableFloatStateOf(0f) }
    var isChecked by remember { mutableStateOf(false) }


    LaunchedEffect(key1 = Unit) {
        while (true) {
            val WS_URL = HttpApi.WS_HOST.value
            val webSocketClient = WebSocketClient(WS_URL, MyWebSocketListener(object :
                WebSocketResponseListener {
                override fun onMessageReceived(message: String) {
                    Log.e("changeLightMode", "Message received: $message")
                    try {
                        val json = JSONObject(message)

                        // 检查 "result" 是否存在并且非空
                        if (json.has("result") && !json.isNull("result")) {
                            val result = json.getJSONObject("result")

                            // 检查指定的 key 是否存在并且非空
                            val key = "1/8/0"
                            if (result.has(key) && !result.isNull(key)) {
                                val levelString = result.getString(key)
                                val level = levelString.toFloat()

                                brightnessLevel = level
                                Log.e("getLevel level", "level: $level")
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
                    println("Bytes received: ${bytes.hex()}")
                }

            }, ::setCurrentStatus))

            try {
                webSocketClient.connect()

                val jsonMessage = """
                {
                  "message_id": "e7184b2b1d5b4c808b0834a18b041cfc",
                  "command": "read_attribute",
                  "args": {
                    "node_id": ${device!!.nodeId},
                    "attribute_path": "1/8/0"
                  }
                }
            """.trimIndent()

                Log.e("changeLightMode", jsonMessage.toString())
                webSocketClient.sendMessage(jsonMessage.toString())
            } catch (e: Exception) {
                Log.e("changeLightMode", "Error: ${e.message}")
            }

            delay(1000)
        }
    }


    if (isChecked)
        LaunchedEffect(key1 = Unit) {

            val WS_URL = HttpApi.WS_HOST.value
            val webSocketClient = WebSocketClient(WS_URL, MyWebSocketListener(object :
                WebSocketResponseListener {
                override fun onMessageReceived(message: String) {
                    Log.e("changeLightLevel", "Message received: $message")
                }

                override fun onBytesReceived(bytes: ByteString) {
                    println("Bytes received: ${bytes.hex()}")
                }

            }, ::setCurrentStatus))



            try {
                webSocketClient.connect()

                val jsonMessage = """
    {
        "message_id": "${UUID.randomUUID()}",
        "command": "device_command",
        "args": {
            "node_id": "${device?.nodeId}",
            "endpoint_id": 1,
            "cluster_id": 8,
            "command_name": "MoveToLevelWithOnOff",
            "payload": {
                "level": ${brightnessLevel.toInt()},
                "transitionTime": 0,
                "optionsMask": 0,
                "optionsOverride": 0
            }
        }
    }
""".trimIndent()
                val json2 ="""
                    {
                        "message_id": "${UUID.randomUUID()}",
                        "command": "device_command",
                        "args": {
                            "endpoint_id": 2,
                            "node_id": "${device?.nodeId}",
                            "payload": {
                                "newMode": $modeSelect
                            },
                            "cluster_id": 80,
                            "command_name": "ChangeToMode"
                        }
                    }
                """.trimIndent()

                Log.e("changeLightLevel", jsonMessage.toString())
                webSocketClient.sendMessage(jsonMessage)
            }catch (e: Exception)
            {
                Log.e("changeLightMode", "Error: ${e.message}")
            }
            isChecked=false
        }



    Column(
        modifier = Modifier,
        verticalArrangement = Arrangement.spacedBy(20.dp),
        horizontalAlignment = androidx.compose.ui.Alignment.CenterHorizontally
    ) {

        Row(verticalAlignment = Alignment.CenterVertically) {
            Text(
                text = "亮度: ",
                fontWeight = FontWeight.Bold,
                modifier = Modifier.padding(bottom = 8.dp)
            )
            Slider(
                value = brightnessLevel,
                onValueChange = { value ->
                    brightnessLevel = value
                },
                onValueChangeFinished = {
                    isChecked = true
                },
                valueRange = 0f..250f,
                steps = 254,
                modifier = Modifier.fillMaxWidth()
            )
        }
        Row(
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            Button(onClick = { modeSelect = 0 }) {
                Text(text = "模式1")
            }
            Button(onClick = {
                modeSelect = 1
            }) {
                Text(text = "模式2")
            }
            Button(onClick = {
                modeSelect = 2
            }) {
                Text(text = "模式3")
            }
        }

        Row(
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(8.dp)

        ) {

            Button(onClick = { /*TODO*/ }) {
                Text(text = "白色")
            }

            Button(onClick = { /*TODO*/ }) {
                Text(text = "黄色")
            }

            Button(onClick = { /*TODO*/ }) {
                Text(text = "蓝色")
            }
        }

    }

}

suspend fun changeLightMode(device: Device?, mode: Int): Boolean = withContext(Dispatchers.IO) {
    val client = OkHttpClient()
    val request = Request
        .Builder()
        .url(
            "${
                HttpApi.CHANGE_LIGHT_MODE.value
            }?deviceId=${device!!.deviceId}&mode=$mode"
        )
        .header("Authorization", "${TokenManager.getToken()}")
        .post("".toRequestBody())
        .build()
    Log.e(
        "changeLightMode ", "${
            HttpApi.CHANGE_LIGHT_MODE.value
        }?deviceId=${device!!.deviceId}&mode=$mode"
    )
    try {
        val response = client.newCall(request).execute()
        Log.e("changeLightMode ", response.body?.string().toString())
        if (response.isSuccessful) {

            Log.e("changeLightMode", response.body?.string() ?: "null")
            return@withContext true
        } else {

            Log.e("changeLightMode", "Error: ${response.code}")
            return@withContext false

        }
    } catch (e: Exception) {
        Log.e("changeLightMode", "Error: ${e.message}")
        return@withContext false
    }


}