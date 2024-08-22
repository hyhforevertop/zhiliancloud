package com.matter.myapplication2.DeviceFunction

import TokenManager
import android.util.Log
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.material3.Slider
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
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


@Composable
fun Fan(
    device: Device?,
    modifier: Modifier = Modifier
) {
    var value by remember { mutableIntStateOf(0) }
    val context = LocalContext.current
    var checked by remember { mutableStateOf(false) }


    if (checked)
    {
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
  "message_id": "adabef62200e49cd85fa5dfe674a8ddb",
  "command": "write_attribute",
  "args": {
    "node_id": ${device!!.nodeId},
    "attribute_path": "2/514/2",
    "value": ${value.toInt()}
  }
}
                """.trimIndent()



                webSocketClient.sendMessage(jsonMessage.toString())

            }
            catch (e: Exception)
            {
                Log.e("changeLightMode", "Error: ${e.message}")
            }
        }

        checked=false
    }

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
                            val key = "2/514/2"
                            if (result.has(key) && !result.isNull(key)) {
                                val levelString = result.getString(key)
                                val level = levelString.toInt()

                                value=level

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
                    "attribute_path": "2/514/2"
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


    Row(
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.spacedBy(10.dp)
    ){
        Text(text = "强度:${value}")
        Slider(
            value=value.toFloat(),
            onValueChange = {
                value = it.toInt()
            },
            valueRange = 0f..100f,
            modifier = Modifier.fillMaxWidth()
            , onValueChangeFinished =
            {
                checked=true
            }
        )

    }
}

suspend fun ChangeFan(device: Device?, value: String):Boolean = withContext(Dispatchers.IO)
{
    val client = OkHttpClient()



    try {
        val url = "${HttpApi.FAN_OPERATION.value}?deviceId=${device!!.deviceId}&value=${value}"
        Log.e("ChangeFan", url)
        val request = Request.Builder()
            .url(
                url
            )
            .header("Authorization", "${TokenManager.getToken()}")
            .post("".toRequestBody())
            .build()

        val response = client.newCall(request).execute()
        Log.e("ChangeFan", response.body!!.string())
        if (response.isSuccessful) {
            Log.e("ChangeFan", response.body!!.string())
            return@withContext true
        } else {
            Log.e("ChangeFan", "Failed to change curtain")
            return@withContext false
        }
    } catch (e: Exception) {
        Log.e("ChangeFan", e.message.toString())
        return@withContext false
    }
}
