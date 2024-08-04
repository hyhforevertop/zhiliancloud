package com.matter.myapplication2.DeviceFunction

import TokenManager
import android.content.Context
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
import androidx.compose.runtime.LaunchedEffect
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
import es.dmoral.toasty.Toasty
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import okhttp3.MediaType.Companion.toMediaTypeOrNull
import okhttp3.OkHttpClient
import okhttp3.Request
import okhttp3.RequestBody.Companion.toRequestBody

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
fun Device_Brightness(
    device: Device?,
    coroutineScope: CoroutineScope,
    context: Context,
    modifier: Modifier = Modifier
) {
    var brightnessLevel by remember { mutableStateOf(0.5f) }

    var isChecked by remember { mutableStateOf(false) }

    if (isChecked)
        LaunchedEffect(isChecked) {
            val check = adjustBrightness(device, brightnessLevel)
            if (check) {
                Toasty.success(context, "操作成功", Toast.LENGTH_SHORT).show()
            } else {
                Toasty.error(context, "操作失败", Toast.LENGTH_SHORT).show()
            }
            isChecked = false
        }

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
                isChecked=true
            },
            valueRange = 0f..255f,
            steps = 254,
            modifier = Modifier.fillMaxWidth()
        )
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
