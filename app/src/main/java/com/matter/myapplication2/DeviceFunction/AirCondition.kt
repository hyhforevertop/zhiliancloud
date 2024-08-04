package com.matter.myapplication2.DeviceFunction

import TokenManager
import android.util.Log
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableDoubleStateOf
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.matter.myapplication2.ui.Device
import com.matter.myapplication2.util.HttpApi
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.delay
import kotlinx.coroutines.withContext
import okhttp3.OkHttpClient
import okhttp3.Request
import okio.IOException
import org.json.JSONObject


@Composable
fun AirCondition(
    device: Device?,
    coroutineScope: CoroutineScope
) {
    var Co2Quality by remember { mutableIntStateOf(0) }
    var TVOCQuality by remember { mutableIntStateOf(0) }
    var temperature by remember { mutableDoubleStateOf(0.0) }
    LaunchedEffect(Unit) {
        while (true)
        {
            Co2Quality= getCo2Quality(device)
            TVOCQuality = getTVOCQuality(device)
            temperature= getAirTemperature(device)
            val formattedTemperature = String.format("%.2f", temperature / 100)
            temperature=formattedTemperature.toDouble()
            Log.e("getAirQuality", "Co2Quality: $Co2Quality")
            Log.e("getAirQuality", "TVOCQuality: $TVOCQuality")
            Log.e("getAirQuality", "temperature: $temperature")
            delay(100)
        }
    }

    Column() {
        Text(
            text = "空气质量指数",
            fontWeight = FontWeight.Bold,
            fontSize = 20.sp,
            modifier = Modifier.padding(bottom = 16.dp)
        )

        Row(
            verticalAlignment = Alignment.CenterVertically,
            modifier = Modifier.padding(bottom = 8.dp)
        ) {
            Text(
                text = "CO2: ",
                fontWeight = FontWeight.Bold,
                fontSize = 18.sp
            )


            Spacer(modifier = Modifier.width(8.dp)) // Space after the bar

            Text(
                text = "${if (Co2Quality == -1) "获取失败" else Co2Quality} (ppm)",
                fontWeight = FontWeight.Bold,
                fontSize = 18.sp
            )
        }


        Row(
            verticalAlignment = Alignment.CenterVertically,
            modifier = Modifier.padding(bottom = 8.dp)
        ) {
            Text(
                text = "TVOC: ",
                fontWeight = FontWeight.Bold,
                fontSize = 18.sp
            )
            Spacer(modifier = Modifier.width(8.dp))


            Text(
                text = "${if (TVOCQuality == -1) "获取失败" else TVOCQuality} (挥发性有机物)",
                fontWeight = FontWeight.Bold,
                fontSize = 18.sp
            )
        }
        Row(
            verticalAlignment = Alignment.CenterVertically,
            modifier = Modifier.padding(bottom = 8.dp)
        ) {
            Text(
                text = "温度: ",
                fontWeight = FontWeight.Bold,
                fontSize = 18.sp
            )
            Spacer(modifier = Modifier.width(8.dp))

            Text(
                text = "${if (temperature == -1.0) "获取失败" else temperature} (°C)",
                fontWeight = FontWeight.Bold,
                fontSize = 18.sp
            )
        }
    }

}

fun determineColor(airQuality: Int): Color {
    // Add your logic here to determine color based on airQuality
    return when (airQuality) {
        in 0..1000 -> Color.Green
        in 1001..2500 -> Color.Yellow
        else -> Color.Red
    }
}

fun calculateBarLength(airQuality: Int): Float {
    // Scale the length of the bar based on air quality without a maximum value
    return airQuality.toFloat().coerceAtMost(100f)
}
suspend fun getCo2Quality(device: Device?): Int = withContext(Dispatchers.IO){
    val client = OkHttpClient()

    val url ="${HttpApi.AIR_CONDITION_SENSOR_STATUS.value}?nodeId=${device?.nodeId}&targetClusterId=1037"
    Log.e("getAirQuality", "getAirQuality: $url")
    try {
        val request = Request.Builder()
            .url(url)
            .addHeader("Authorization", "${TokenManager.getToken()}")
            .build()

        val response = client.newCall(request).execute()

        if (response.isSuccessful) {
            val responseBody = response.body?.string()
            Log.e("getAirQuality", "onResponse: $responseBody")
            val jsonObject = responseBody?.let { JSONObject(it) }

            if (response.code == 200) {
                val dataObject = jsonObject?.optJSONObject("data")
                val measuredValue = dataObject?.optDouble("measuredValue")
                return@withContext measuredValue?.toInt() ?: 0
            } else {
                // 处理其他状态码，例如 500
                return@withContext -1
            }
        } else {
            // 处理请求失败的情况
            return@withContext -1
        }
    }
    catch (e: IOException)
    {
        Log.e("getAirQuality","onFailure: ${e.message}")
        return@withContext -1
    }

}

suspend fun getTVOCQuality(device: Device?): Int = withContext(Dispatchers.IO){
    val client = OkHttpClient()

    val url ="${HttpApi.AIR_CONDITION_SENSOR_STATUS.value}?nodeId=${device?.nodeId}&targetClusterId=1070"
    Log.e("getAirQuality", "getAirQuality: $url")
    try {
        val request = Request.Builder()
            .url(url)
            .addHeader("Authorization", "${TokenManager.getToken()}")
            .build()

        val response = client.newCall(request).execute()

        if (response.isSuccessful) {
            val responseBody = response.body?.string()
            Log.e("getAirQuality", "onResponse: $responseBody")
            val jsonObject = responseBody?.let { JSONObject(it) }

            if (response.code == 200) {
                val dataObject = jsonObject?.optJSONObject("data")
                val measuredValue = dataObject?.optDouble("measuredValue")
                return@withContext measuredValue?.toInt() ?: 0
            } else {
                // 处理其他状态码，例如 500
                return@withContext -1
            }
        } else {
            // 处理请求失败的情况
            return@withContext -1
        }
    }
    catch (e: IOException)
    {
        Log.e("getAirQuality","onFailure: ${e.message}")
        return@withContext -1
    }

}

suspend fun getAirTemperature(device: Device?): Double = withContext(Dispatchers.IO){
    val client = OkHttpClient()

    val url ="${HttpApi.AIR_CONDITION_SENSOR_STATUS.value}?nodeId=${device?.nodeId}&targetClusterId=1026"
    Log.e("getAirQuality", "getAirQuality: $url")
    try {
        val request = Request.Builder()
            .url(url)
            .addHeader("Authorization", "${TokenManager.getToken()}")
            .build()

        val response = client.newCall(request).execute()

        if (response.isSuccessful) {
            val responseBody = response.body?.string()
            Log.e("getAirQuality", "onResponse: $responseBody")
            val jsonObject = responseBody?.let { JSONObject(it) }

            if (response.code == 200) {
                val dataObject = jsonObject?.optJSONObject("data")
                val measuredValue = dataObject?.optDouble("measuredValue")
                return@withContext measuredValue  ?: 0.0
            } else {
                // 处理其他状态码，例如 500
                return@withContext -1.0
            }
        } else {
            // 处理请求失败的情况
            return@withContext -1.0
        }
    }
    catch (e: IOException)
    {
        Log.e("getAirQuality","onFailure: ${e.message}")
        return@withContext -1.0
    }

}





