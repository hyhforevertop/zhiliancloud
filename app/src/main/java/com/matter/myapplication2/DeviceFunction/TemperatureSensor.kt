package com.matter.myapplication2.DeviceFunction

import TokenManager
import android.util.Log
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Row
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableDoubleStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.matter.myapplication2.ui.Device
import com.matter.myapplication2.util.HttpApi
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.delay
import kotlinx.coroutines.withContext
import okhttp3.OkHttpClient
import okhttp3.Request
import org.json.JSONObject
import java.io.IOException


@Composable
fun TemperatureSensor(
    device: Device?,
    coroutineScope: CoroutineScope
) {
    var temperature by remember { mutableDoubleStateOf(0.0) }

    LaunchedEffect(Unit) {
        while (true) {
            temperature = getTemperature(device)
            delay(1000) // 每秒钟刷新一次
        }
    }

    Row(
        modifier = Modifier,
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.spacedBy(20.dp)
    ) {
        Text(
            text = "当前温度指数: ",
            fontWeight = FontWeight.Bold
        )
        Text(text = if(temperature == -0.1) "暂无数据" else temperature.toString())
    }
}

suspend fun getTemperature(device: Device?): Double = withContext(Dispatchers.IO){
    val client = OkHttpClient()

    try {
        val request = Request.Builder()
            .url("${HttpApi.TEMPERATURE_SENSOR_STATUS.value}?nodeId=${device?.nodeId}")
            .addHeader("Authorization", "${TokenManager.getToken()}")
            .build()

        val response = client.newCall(request).execute()

        if (response.isSuccessful) {
            val responseBody = response.body?.string()
            Log.e("getAirQuality", "onResponse: ${responseBody}")
            val jsonObject = responseBody?.let { JSONObject(it) }

            if (response.code == 200) {
                val dataObject = jsonObject?.optJSONObject("data")
                val measuredValue = dataObject?.optDouble("measuredValue")
                return@withContext measuredValue?: -0.1
            } else {
                // 处理其他状态码，例如 500
                return@withContext -0.1
            }
        } else {
            // 处理请求失败的情况
            return@withContext -0.1
        }
    }
    catch (e: IOException)
    {
        Log.e("getTemperature", "onFailure: ${e.message}")
        return@withContext -0.1
    }



}
