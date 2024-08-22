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
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.matter.myapplication2.ui.Device
import com.matter.myapplication2.ui.setCurrentStatus
import com.matter.myapplication2.util.HttpApi
import com.matter.myapplication2.util.MyWebSocketListener
import com.matter.myapplication2.util.WebSocketClient
import com.matter.myapplication2.util.WebSocketResponseListener
import com.patrykandpatrick.vico.compose.axis.vertical.rememberStartAxis
import com.patrykandpatrick.vico.compose.chart.Chart
import com.patrykandpatrick.vico.compose.chart.column.columnChart
import com.patrykandpatrick.vico.compose.chart.line.lineChart
import com.patrykandpatrick.vico.core.chart.scale.AutoScaleUp
import com.patrykandpatrick.vico.core.entry.ChartEntryModelProducer
import com.patrykandpatrick.vico.core.entry.entryOf
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.delay
import kotlinx.coroutines.withContext
import okhttp3.OkHttpClient
import okhttp3.Request
import okio.ByteString
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
    var wetness by remember { mutableIntStateOf(0) }

    val entries1 = mutableListOf<Float>()
    val entries2 = mutableListOf<Float>()
    val entries3 = mutableListOf<Float>()
    val entries4 = mutableListOf<Float>()


    fun getRandomEntries1(data: Float) {
        if (entries1.size >= 20) {
            entries1.clear()
        }
        entries1.add(data)

    }

    fun getRandomEntries2(data: Float) {
        if (entries2.size >= 20) {
            entries2.clear()
        }

        entries2.add(data)
    }

    fun getRandomEntries3(data: Float) {
        if (entries3.size >= 20) {
            entries3.clear()
        }

        entries3.add(data)
    }

    fun getRandomEntries4(data: Float) {
        if (entries4.size >= 20) {
            entries4.clear()
        }

        entries4.add(data)
    }

    var numberData1 = listOf(0f)
    var numberData2 = listOf(0f)
    var numberData3 = listOf(0f)
    var numberData4 = listOf(0f)

    val composedChartEntryModelProducer1 by remember { mutableStateOf(ChartEntryModelProducer()) }
    val composedChartEntryModelProducer2 by remember { mutableStateOf(ChartEntryModelProducer()) }
    val composedChartEntryModelProducer3 by remember { mutableStateOf(ChartEntryModelProducer()) }
    val composedChartEntryModelProducer4 by remember { mutableStateOf(ChartEntryModelProducer()) }

    fun updateChartEntries1(data: Float) {
        getRandomEntries1(data)
        // 将 entries 转换为 List<ChartEntry>
        val chartEntries = entries1.mapIndexed { index, value -> entryOf(index.toFloat(), value) }
        // 传递给 setEntries 的是 List<List<ChartEntry>>
        composedChartEntryModelProducer1.setEntries(listOf(chartEntries))
    }

    fun updateChartEntries2(data: Float) {
        getRandomEntries2(data)
        // 将 entries 转换为 List<ChartEntry>
        val chartEntries = entries2.mapIndexed { index, value -> entryOf(index.toFloat(), value) }
        // 传递给 setEntries 的是 List<List<ChartEntry>>
        composedChartEntryModelProducer2.setEntries(listOf(chartEntries))
    }

    fun updateChartEntries3(data: Float) {
        getRandomEntries3(data)
        // 将 entries 转换为 List<ChartEntry>
        val chartEntries = entries3.mapIndexed { index, value -> entryOf(index.toFloat(), value) }
        // 传递给 setEntries 的是 List<List<ChartEntry>>
        composedChartEntryModelProducer3.setEntries(listOf(chartEntries))
    }

    fun updateChartEntries4(data: Float) {
        getRandomEntries4(data)
        // 将 entries 转换为 List<ChartEntry>
        val chartEntries = entries4.mapIndexed { index, value -> entryOf(index.toFloat(), value) }
        // 传递给 setEntries 的是 List<List<ChartEntry>>
        composedChartEntryModelProducer4.setEntries(listOf(chartEntries))
    }

    LaunchedEffect(Unit) {
        while (true) {

            Co2Quality = getCo2Quality(device)
            TVOCQuality = getTVOCQuality(device)
            temperature = getAirTemperature(device)


            val WS_URL = HttpApi.WS_HOST.value
            val webSocketClient = WebSocketClient(WS_URL, MyWebSocketListener(object :
                WebSocketResponseListener {
                override fun onMessageReceived(message: String) {
                    try {
                        val json = JSONObject(message)

                        // 检查 "result" 是否存在并且非空
                        if (json.has("result") && !json.isNull("result")) {
                            val result = json.getJSONObject("result")

                            // 检查指定的 key 是否存在并且非空
                            val key = "4/1029/0"
                            if (result.has(key) && !result.isNull(key)) {
                                val levelString = result.getString(key)
                                val level = levelString.toFloat()

                                wetness = level.toInt()
                                Log.e("getLevel level", "level: $level")

                                numberData4 = numberData4.drop(1) + wetness.toFloat()
                                updateChartEntries4(wetness.toFloat())
                            } else {
                                Log.e("getLevel", "Key $key does not exist or is null")
                            }
                        } else {
                            Log.e("getLevel", "\"result\" does not exist or is null")
                        }
                    } catch (e: Exception) {
                        Log.e("getLevel", "Error parsing JSON: ${e.message}")

                    }
                }

                override fun onBytesReceived(bytes: ByteString) {
                    println("Bytes received: ${bytes.hex()}")
                }

            }, ::setCurrentStatus))

            try {
                webSocketClient.connect()
                val json = """
                     {
                       "message_id": "e7184b2b1d5b4c808b0834a18b041cfc",
                       "command": "read_attribute",
                       "args": {
                         "node_id": "${device!!.nodeId}",
                         "attribute_path": "4/1029/0"
                       }
                     }
                 """.trimIndent()

                webSocketClient.sendMessage(json)
            } catch (e: IOException) {
                Log.e("getAirQuality", "Error connecting to WebSocket: ${e.message}")

            }

            val formattedTemperature = String.format("%.2f", temperature / 100)
            temperature = formattedTemperature.toDouble()
            Log.e("getAirQuality", "Co2Quality: $Co2Quality")
            Log.e("getAirQuality", "TVOCQuality: $TVOCQuality")
            Log.e("getAirQuality", "temperature: $temperature")
            Log.e("getAirQuality", "wetness: $wetness")
            numberData1 = numberData1.drop(1) + Co2Quality.toFloat()
            numberData2 = numberData2.drop(1) + TVOCQuality.toFloat()
            numberData3 = numberData3.drop(1) + temperature.toFloat()

            updateChartEntries1(Co2Quality.toFloat())
            updateChartEntries2(TVOCQuality.toFloat())
            updateChartEntries3(temperature.toFloat())


            delay(2000)

        }
    }


//    LaunchedEffect(Unit) {
//        while (true) {
//
//            withContext(Dispatchers.IO)
//            {
//                val client = OkHttpClient()
//                val request = Request.Builder()
//                    .url("http://${TokenManager.getHostIpv4()}:5000")
//                    .addHeader("Authorization", "${TokenManager.getToken()}")
//                    .build()
//
//                val response = client.newCall(request).execute()
//                if (response.isSuccessful) {
//                    val responseBody = response.body?.string()
//                    val jsonObject = responseBody?.let { JSONObject(it) }
//                    val dataint = jsonObject?.getInt("data")!!.toFloat()
//                    Log.e("Test", dataint.toString())
//                    numberData1 = numberData1.drop(1) + dataint
//                    numberData2 = numberData2.drop(1) + dataint
//                    numberData3 = numberData3.drop(1) + dataint
//                    Co2Quality = dataint.toInt()
//                    TVOCQuality = dataint.toInt()
//                    temperature = dataint.toDouble()
//                    updateChartEntries1(dataint)
//                    updateChartEntries2(dataint)
//                    updateChartEntries3(dataint)
//
//                }
//            }
//            delay(2000)
//        }
//
//    }

    Column {
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
                text = "${Co2Quality} (ppm)",
                fontWeight = FontWeight.Bold,
                fontSize = 18.sp
            )
        }


        MyLineChart(chartModelProducer = composedChartEntryModelProducer1)


        Row(
            verticalAlignment = Alignment.CenterVertically,
            modifier = Modifier.padding(bottom = 8.dp)
        ) {
            Text(
                text = "挥发性有机化合物: ",
                fontWeight = FontWeight.Bold,
                fontSize = 18.sp
            )
            Spacer(modifier = Modifier.width(8.dp))


            Text(
                text = "${if (TVOCQuality == -1) "获取失败" else TVOCQuality} (ppm)",
                fontWeight = FontWeight.Bold,
                fontSize = 18.sp
            )
        }


        MyLineChart(chartModelProducer = composedChartEntryModelProducer2)



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
        MyColumnChart(chartModelProducer = composedChartEntryModelProducer3)

        Row(
            verticalAlignment = Alignment.CenterVertically,
            modifier = Modifier.padding(bottom = 8.dp)
        ) {
            Text(
                text = "湿度: ",
                fontWeight = FontWeight.Bold,
                fontSize = 18.sp
            )
            Spacer(modifier = Modifier.width(8.dp))

            Text(
                text = "${if (wetness == -1) "获取失败" else wetness/100} (%)",
                fontWeight = FontWeight.Bold,
                fontSize = 18.sp
            )
        }
        MyColumnChart(chartModelProducer = composedChartEntryModelProducer4)
    }
}

@Composable
fun MyLineChart(chartModelProducer: ChartEntryModelProducer) {
    val lineChart = lineChart()
    Chart(
        chart = remember(lineChart) { lineChart },
        chartModelProducer = chartModelProducer,
        startAxis = rememberStartAxis(),
        autoScaleUp = AutoScaleUp.Full,
    )
}

@Composable
fun MyColumnChart(chartModelProducer: ChartEntryModelProducer) {
    val columnChart = columnChart()
    Chart(
        chart = remember(columnChart) { columnChart },
        chartModelProducer = chartModelProducer,
        startAxis = rememberStartAxis(),

        )
}

suspend fun getWetQuality(device: Device?): Int = withContext(Dispatchers.IO) {
    val client = OkHttpClient()

    val url =
        "${HttpApi.AIR_CONDITION_SENSOR_STATUS.value}?nodeId=${device?.nodeId}&targetClusterId=1029"
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
    } catch (e: IOException) {
        Log.e("getAirQuality", "onFailure: ${e.message}")
        return@withContext -1
    }

}

suspend fun getCo2Quality(device: Device?): Int = withContext(Dispatchers.IO) {
    val client = OkHttpClient()

    val url =
        "${HttpApi.AIR_CONDITION_SENSOR_STATUS.value}?nodeId=${device?.nodeId}&targetClusterId=1037"
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
    } catch (e: IOException) {
        Log.e("getAirQuality", "onFailure: ${e.message}")
        return@withContext -1
    }

}

suspend fun getTVOCQuality(device: Device?): Int = withContext(Dispatchers.IO) {
    val client = OkHttpClient()

    val url =
        "${HttpApi.AIR_CONDITION_SENSOR_STATUS.value}?nodeId=${device?.nodeId}&targetClusterId=1070"
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
    } catch (e: IOException) {
        Log.e("getAirQuality", "onFailure: ${e.message}")
        return@withContext -1
    }

}

suspend fun getAirTemperature(device: Device?): Double = withContext(Dispatchers.IO) {
    val client = OkHttpClient()

    val url =
        "${HttpApi.AIR_CONDITION_SENSOR_STATUS.value}?nodeId=${device?.nodeId}&targetClusterId=1026"
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
                return@withContext measuredValue ?: 0.0
            } else {
                // 处理其他状态码，例如 500
                return@withContext -1.0
            }
        } else {
            // 处理请求失败的情况
            return@withContext -1.0
        }
    } catch (e: IOException) {
        Log.e("getAirQuality", "onFailure: ${e.message}")
        return@withContext -1.0
    }
}

suspend fun getSmokeQuality(device: Device?): Int = withContext(Dispatchers.IO) {
    val client = OkHttpClient()

    val url = "test"
    try {
        val request = Request.Builder()
            .url(url)
            .addHeader("Authorization", "${TokenManager.getToken()}")
            .build()

        val response = client.newCall(request).execute()

        if (response.isSuccessful) {
            val responseBody = response.body?.string()
            Log.e("getAirQuality", "onResponse: $responseBody")


        }
    } catch (e: IOException) {
        Log.e("getAirQuality", "onFailure: ${e.message}")

    }


    return@withContext -1
}

