package com.matter.myapplication2.ui

import MainScreen
import TokenManager
import android.util.Log
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Icon
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.navigation.NavHostController
import com.king.ultraswiperefresh.NestedScrollMode
import com.king.ultraswiperefresh.UltraSwipeRefresh
import com.king.ultraswiperefresh.indicator.SwipeRefreshHeader
import com.king.ultraswiperefresh.rememberUltraSwipeRefreshState
import com.matter.myapplication2.DeviceRepository.DeviceRepository
import com.matter.myapplication2.R
import com.matter.myapplication2.navigateSingleTopTo
import com.matter.myapplication2.util.HttpApi
import es.dmoral.toasty.Toasty
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import okhttp3.OkHttpClient
import okhttp3.Request
import okio.IOException
import org.json.JSONException
import org.json.JSONObject


@Composable
fun DeviceList(modifier: Modifier = Modifier, navController: NavHostController) {
    // Device list state


    val deviceListState = remember { mutableStateOf<List<Device>>(emptyList()) }
    val context = LocalContext.current
    val coroutineScope = rememberCoroutineScope()

    val state = rememberUltraSwipeRefreshState()

    if (DeviceRepository.getDevices().isNotEmpty()) {
        deviceListState.value = DeviceRepository.getDevices()
    } else {
        LaunchedEffect(Unit) {
            val devices = getDeviceList()

            if (devices.isEmpty())
            {
                Toasty.error(context, "网络问题", Toasty.LENGTH_SHORT).show()
                TokenManager.clearToken()
                navController.navigateSingleTopTo("login")
            }
            else {
                deviceListState.value = devices
                DeviceRepository.setDevices(devices)
                Toasty.success(context, "Device found", Toasty.LENGTH_SHORT).show()
            }

            Log.e("LaunchedEffect DeviceList", "Device list updated: ${devices.size}")
        }
    }

    if(state.isRefreshing) {
        LaunchedEffect(Unit) {


            val devices = getDeviceList()
            deviceListState.value = devices
            DeviceRepository.setDevices(devices)
            Login(TokenManager.getUsername()!!, TokenManager.getPassword()!!,TokenManager.getHostIpv4()!!)

            if (devices.isEmpty())
                Toasty.error(context, "刷新失败", Toasty.LENGTH_SHORT).show()
            else
                Toasty.success(context, "刷新成功", Toasty.LENGTH_SHORT).show()

            state.isRefreshing = false
        }
    }


    MainScreen(
        modifier = modifier,
        navController = navController,
        paddingContent = { paddingValues ->
            UltraSwipeRefresh(
                state = state,
                onRefresh = { state.isRefreshing = true },
                onLoadMore = {},
                modifier = Modifier.padding(paddingValues),
                headerScrollMode = NestedScrollMode.FixedContent,
                headerMaxOffsetRate = 4f,
                headerIndicator = {
                    SwipeRefreshHeader(
                        state = it,
                        paddingValues = PaddingValues(top = 50.dp)
                    )
                },
                loadMoreEnabled = false
            ) {

                LazyColumn(
                    modifier = Modifier
                        .padding(horizontal = 10.dp)

                ) {

                    items(deviceListState.value) { device ->
                        DeviceItem(device = device, onClick = {
//                            navController.navigate("deviceDetail/${device.deviceId}")
                            navController.navigateSingleTopTo("deviceDetail/${device.deviceId}")
                        })
                    }

                }
            }
        },
        currentPage = "home"
    )


}


suspend fun getDeviceList(): List<Device> = withContext(Dispatchers.IO) {
    val ListOfDevice = mutableListOf<Device>()
    val client = OkHttpClient()

    try {
        val request = Request.Builder()
            .url("${HttpApi.BASE_URL.value}/device/list/1/100")
            .addHeader("Authorization", "${TokenManager.getToken()}")
            .build()

        val response = client.newCall(request).execute()

        if (response.isSuccessful) {
            val responseBody = response.body?.string()
            Log.e("getDeviceList", "Response: $responseBody")
            val jsonObject = responseBody?.let { JSONObject(it) }
            val dataObject = jsonObject?.getJSONObject("data")
            val deviceList = dataObject?.getJSONArray("list")

            deviceList?.let { array ->
                for (i in 0 until array.length()) {
                    val deviceJson = array.getJSONObject(i)
                    val device = Device(
                        deviceId = deviceJson.optString("deviceId"),
                        userId = deviceJson.getInt("userId"),
                        deviceName = deviceJson.getString("deviceName"),
                        deviceType = deviceJson.getString("deviceType"),
                        deviceModel = deviceJson.optString("deviceModel"),
                        deviceManufacturer = deviceJson.getString("deviceManufacturer"),
                        deviceStatus = deviceJson.optString("deviceStatus"),
                        createDate = deviceJson.getString("createDate"),
                        updateDate = deviceJson.getString("updateDate"),
                        location = deviceJson.getString("location"),
                        qrcode = deviceJson.optString("qrcode"),
                        nodeId = deviceJson.optInt("nodeId")
                    )
                    ListOfDevice.add(device)
                }
            }
        } else {
            Log.e("DeviceList", "Failed to fetch devices: ${response.message}")
        }
    } catch (e: IOException) {
        Log.e("DeviceList", "onFailure: ${e.message}")
    } catch (e: JSONException) {
        Log.e("DeviceList", "JSON parsing error: ${e.message}")
    }

    return@withContext ListOfDevice
}


@Composable
fun DeviceItem(device: Device, modifier: Modifier = Modifier, onClick: () -> Unit) {

    val image = when (device.deviceType) {
        "260" -> R.drawable.device_light
        "261" -> R.drawable.device_light_belt
        "8888" -> R.drawable.speed_camera
        "770" -> R.drawable.temperature_sensor
        "44" ->R.drawable.air_condition
        "514" -> R.drawable.window_covering
        else -> R.drawable.device_unknown
    }
    Surface(
        modifier = modifier
            .padding(5.dp)
            .shadow(2.dp, RoundedCornerShape(16.dp))
            .clip(shape = RoundedCornerShape(16.dp))
            .clickable(onClick = onClick),
        color = Color.White // Background color
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp),// Padding inside the item
            verticalAlignment = Alignment.CenterVertically
        ) {
            Icon(
                painter = painterResource(id = image),
                contentDescription = null,
                modifier = Modifier
                    .size(60.dp)
                    .padding(end = 16.dp) // Space between icon and text
            )

            Column(
                modifier = Modifier
                    .weight(1f), // Fill the remaining space,


            ) {

                Text(
                    text = device.deviceName,
                    fontWeight = FontWeight.Bold
                )
                Text(
                    text = deviceMap(device.deviceType),
                )
            }
        }
    }
}

data class Device(
    val deviceId: String?,
    val userId: Int,
    val deviceName: String,
    val deviceType: String,
    val deviceModel: String?,
    val deviceManufacturer: String,
    var deviceStatus: String?,
    val createDate: String,
    val updateDate: String?,
    val location: String,
    val qrcode: String?,
    val nodeId: Int
)

fun deviceMap(deviceType: String): String {

    val deviceTypeInt = deviceType.toInt()
    return when (deviceTypeInt) {
        260 -> "无线调光灯"
        261 -> "无线彩灯"
        0x000F -> "开关"
        0x0106 -> "光照度传感"
        770 -> "温度传感器"
        8888 -> "摄像头"
        44 -> "空气质量传感器"
        514 -> "窗帘"
        else -> "未知设备"
    }

}


