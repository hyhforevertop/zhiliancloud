package com.matter.myapplication2.DeviceFunction

import TokenManager
import android.content.Context
import android.os.Handler
import android.os.Looper
import android.util.Log
import android.widget.Toast
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Slider
import androidx.compose.material3.Switch
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.MutableState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.matter.myapplication2.ui.Device
import com.matter.myapplication2.util.HttpApi
import com.matter.myapplication2.util.HttpClient
import es.dmoral.toasty.Toasty
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import okhttp3.Call
import okhttp3.Callback
import okhttp3.Response
import java.io.IOException

@Composable
fun Device_Switch(device: MutableState<Device?>, coroutineScope: CoroutineScope, context: Context) {
    var switchStatus by remember { mutableStateOf(device.value?.deviceStatus == "On") }

    Row(
        modifier = Modifier,
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.spacedBy(20.dp)
    ) {
        Text("开关: ", fontWeight = FontWeight.Bold)
        Switch(
            checked = switchStatus,
            onCheckedChange = { isChecked ->
                switchStatus = isChecked
                device.value = device.value?.copy(deviceStatus = if (isChecked) "On" else "Off")
                coroutineScope.launch {
                    Turn_On_Off_Light(device, context)
                }
            }
        )
    }
}

@Composable
fun Device_Brightness(
    device: MutableState<Device?>,
    coroutineScope: CoroutineScope,
    context: Context,
    modifier: Modifier = Modifier
) {
    var brightnessLevel by remember { mutableStateOf( 0.5f) }

    Row(
        modifier = modifier,
        verticalAlignment = Alignment.CenterVertically
    ) {
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
                coroutineScope.launch {
                    adjustBrightness(device, brightnessLevel, context)
                }
            },
            valueRange = 0f..255f,
            steps = 254,
            modifier = Modifier.fillMaxWidth()
        )
    }
}

suspend fun Turn_On_Off_Light(device: MutableState<Device?>, context: Context) {
    withContext(Dispatchers.IO) {
        val json = ""
        HttpClient.post(
            url = "${HttpApi.LIGHT_OPERATION.value}/${device.value?.deviceId}",
            token = TokenManager.getToken().toString(),
            json = json,
            callback = object : Callback {
                override fun onFailure(call: Call, e: IOException) {
                    Log.e("Turn_On_Off_Light", e.message.toString())
                    Handler(Looper.getMainLooper()).post {
                        Toasty.error(context, "操作失败", Toast.LENGTH_SHORT).show()
                        // 回滚开关状态
                    }
                }

                override fun onResponse(call: Call, response: Response) {
                    val responseBody = response.body?.string()
                    if (response.isSuccessful) {
                        Log.e("Turn_On_Off_Light", responseBody.toString())
                        Handler(Looper.getMainLooper()).post {
                            Toasty.success(context, "操作成功", Toast.LENGTH_SHORT).show()
                        }
                    } else {
                        Log.e("Turn_On_Off_Light", responseBody.toString())
                        Handler(Looper.getMainLooper()).post {
                            Toasty.error(context, "操作失败", Toast.LENGTH_SHORT).show()
                            // 回滚开关状态
                        }
                    }
                }
            }
        )
    }
}


suspend fun adjustBrightness(device: MutableState<Device?>, brightnessLevel: Float, context: Context) {
    withContext(Dispatchers.IO) {
        val json = """
            {
                "deviceId": "${device.value?.deviceId}",
                "level": ${brightnessLevel.toInt()},
                "transitionTime": 1,
                "optionMask": 1,
                "optionOverride": 1
            }
        """
        Log.e("AdjustBrightness", json)
        HttpClient.post(
            url = HttpApi.LIGHT_ADJUST.value,
            token = TokenManager.getToken().toString(),
            json = json,
            callback = object : Callback {
                override fun onFailure(call: Call, e: IOException) {
                    Log.e("AdjustBrightness", e.message.toString())
                    Handler(Looper.getMainLooper()).post {
                        Toasty.error(context, "亮度调节失败", Toast.LENGTH_SHORT).show()
                    }
                }

                override fun onResponse(call: Call, response: Response) {
                    val responseBody = response.body?.string()
                    if (response.isSuccessful) {
                        Log.e("AdjustBrightness", responseBody.toString())
                        Handler(Looper.getMainLooper()).post {
                            Toasty.success(context, "亮度调节成功", Toast.LENGTH_SHORT).show()
                        }
                    } else {
                        Log.e("AdjustBrightness", responseBody.toString())
                        Handler(Looper.getMainLooper()).post {
                            Toasty.error(context, "亮度调节失败", Toast.LENGTH_SHORT).show()
                        }
                    }
                }
            }
        )
    }
}
