package com.matter.myapplication2.ui

import BottomBar
import MainScreen
import TokenManager
import android.annotation.SuppressLint
import android.util.Log
import androidx.compose.foundation.Image
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.material3.VerticalDivider
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.navigation.NavHostController
import com.matter.myapplication2.DeviceRepository.DeviceRepository
import com.matter.myapplication2.R
import com.matter.myapplication2.navigateSingleTopTo
import com.matter.myapplication2.ui.theme.TopbarColor
import com.matter.myapplication2.util.HttpApi
import es.dmoral.toasty.Toasty
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import okhttp3.OkHttpClient
import okhttp3.Request
import okio.IOException
import org.json.JSONException
import org.json.JSONObject


@SuppressLint("SuspiciousIndentation")
@Composable
fun DeviceList(modifier: Modifier = Modifier, navController: NavHostController) {
    // Device list state


    var deviceListState by remember { mutableStateOf<List<Device>>(emptyList()) }
    val context = LocalContext.current
    val coroutineScope = rememberCoroutineScope()


//    if (DeviceRepository.getDevices().isNotEmpty()) {
//        deviceListState = DeviceRepository.getDevices()
//    } else {

    LaunchedEffect(Unit) {
        val devices = getDeviceList()



        deviceListState = devices
        DeviceRepository.setDevices(devices)
        Toasty.success(context, "刷新成功", Toasty.LENGTH_SHORT).show()


        Log.e("LaunchedEffect DeviceList", "Device list updated: ${devices.size}")
    }





    MainScreen(
        modifier = modifier,
        navController = navController,

        paddingContent = { paddingValues ->


            LazyColumn(
                modifier = Modifier
                    .padding(horizontal = 10.dp)
                    .padding(bottom = 60.dp)
                    .padding(top = 95.dp)
            ) {

                items(deviceListState) { device ->
                    DeviceItem(device = device, onClick = {
                        navController.navigateSingleTopTo("deviceDetail/${device.deviceId}")
                    })
                }
            }
        },
        currentPage = "home",
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

    var image = when (device.deviceType) {
        "260" -> R.drawable.device_light
        "269" -> R.drawable.device_light_belt
        "8888" -> R.drawable.speed_camera
        "770" -> R.drawable.temperature_sensor
        "44" -> R.drawable.air_condition
        "514" -> R.drawable.device_fan
        else -> R.drawable.device_unknown
    }
    if (device.deviceName == "human detector") {
        image = R.drawable.human
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
        269 -> "无线彩灯"
        0x000F -> "开关"
        0x0106 -> "光照度传感"
        770 -> "温度传感器"
        8888 -> "摄像头"
        44 -> "空气质量传感器"
        514 -> "风扇"
        123 -> "人体传感器"
        else -> "未知设备"
    }

}

@SuppressLint("UnusedMaterial3ScaffoldPaddingParameter")
@OptIn(ExperimentalMaterial3Api::class)
@Preview(showBackground = true)
@Composable
fun MyPreview(
    modifier: Modifier = Modifier
) {
    Scaffold(
        modifier = modifier.fillMaxSize(),
        topBar = {
            TopAppBar(
                title = {
                    Row {
                        Text(
                            text = "智联云",

                            color = Color.White,

                            )
                        Row(
                            modifier = Modifier.weight(1f)
                        ) {
                            Text(
                                text = "场景1",
                                color = Color.White,
                                modifier = Modifier.padding(start = 80.dp)
                            )
                            Image(
                                painter = painterResource(id = R.drawable.arrow_down_white),
                                contentDescription = null
                            )


                        }
                        Image(
                            painter = painterResource(id = R.drawable.add_circle),
                            contentDescription = null
                        )
                    }
                },
                navigationIcon = { }, // 主页面没有返回按钮

                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = TopbarColor,
                    titleContentColor = MaterialTheme.colorScheme.onSurface,
                    navigationIconContentColor = MaterialTheme.colorScheme.onSurface,
                    actionIconContentColor = MaterialTheme.colorScheme.onSurface
                ), modifier = Modifier
            )
        },
        containerColor = MaterialTheme.colorScheme.background, // 使用主题颜色
        content = { padding ->

            Column(
                modifier = Modifier
                    .padding(padding)
                    .padding(vertical = 10.dp, horizontal = 10.dp),

                ) {

                Column(
                    verticalArrangement = Arrangement.spacedBy(10.dp)
                ) {

                    Row(
                        verticalAlignment = Alignment.CenterVertically
                    ) {

                        Text(
                            text = "日常生活",
                            modifier = Modifier.weight(2f),
                            fontWeight = FontWeight.Bold
                        )
                        Row(
                            modifier = Modifier,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Text(text = "全部", fontWeight = FontWeight.Bold)
                            Image(
                                painter = painterResource(id = R.drawable.arrow_down),
                                contentDescription = null
                            )
                        }
                    }

                    Surface(
                        modifier = modifier
                            .fillMaxWidth()

                            .shadow(2.dp, RoundedCornerShape(16.dp))
                            .clip(shape = RoundedCornerShape(16.dp))
                            .padding(),
                        color = Color.White
                    )
                    {
                        Row(
                            modifier = Modifier
                                .height(
                                    80.dp
                                )
                                .padding(10.dp),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Column(
                                verticalArrangement = Arrangement.spacedBy(5.dp)
                            ) {
                                Text(
                                    modifier=Modifier.padding(end = 10.dp),
                                    text = "回家",
                                    fontWeight = FontWeight.Bold,
                                    fontSize = 20.sp
                                )
                                Text(text = "可开启")
                            }

                            VerticalDivider(
                                modifier =Modifier.padding(start = 10.dp, end = 10.dp),
                            )

                            Row(
                                horizontalArrangement = Arrangement.spacedBy( 10.dp),
                                verticalAlignment = Alignment.CenterVertically,
                            ) {
                                 Image(painter = painterResource(id = R.drawable.device_light), contentDescription = null)
                                Image(painter = painterResource(id = R.drawable.device_fan), contentDescription = null )
                                Image(painter = painterResource(id = R.drawable.music), contentDescription = null )
                            }
                        }

                    }
                    Surface(
                        modifier = modifier
                            .fillMaxWidth()

                            .shadow(2.dp, RoundedCornerShape(16.dp))
                            .clip(shape = RoundedCornerShape(16.dp))
                            .padding(),
                        color = Color.White
                    )
                    {
                        Row(
                            modifier = Modifier
                                .height(
                                    80.dp
                                )
                                .padding(10.dp),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Column(
                                verticalArrangement = Arrangement.spacedBy(5.dp)
                            ) {
                                Text(
                                    modifier=Modifier.padding(end = 10.dp),
                                    text = "睡觉",
                                    fontWeight = FontWeight.Bold,
                                    fontSize = 20.sp
                                )
                                Text(text = "可开启")
                            }

                            VerticalDivider(
                                modifier =Modifier.padding(start = 10.dp, end = 10.dp),
                            )

                            Row(
                                horizontalArrangement = Arrangement.spacedBy( 10.dp),
                                verticalAlignment = Alignment.CenterVertically,
                            ) {
                                Image(painter = painterResource(id = R.drawable.device_light), contentDescription = null)
                                Image(painter = painterResource(id = R.drawable.device_fan), contentDescription = null )
                                Image(painter = painterResource(id = R.drawable.air_condition), contentDescription = null )
                            }
                        }

                    }

                    Surface(
                        modifier = modifier
                            .fillMaxWidth()

                            .shadow(2.dp, RoundedCornerShape(16.dp))
                            .clip(shape = RoundedCornerShape(16.dp))
                            .padding(),
                        color = Color.White
                    )
                    {
                        Row(
                            modifier = Modifier
                                .height(
                                    80.dp
                                )
                                .padding(10.dp),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Column(
                                verticalArrangement = Arrangement.spacedBy(5.dp)
                            ) {
                                Text(
                                    modifier=Modifier.padding(end = 10.dp),
                                    text = "看电影",
                                    fontWeight = FontWeight.Bold,
                                    fontSize = 20.sp
                                )
                                Text(text = "可开启")
                            }

                            VerticalDivider(
                                modifier =Modifier.padding(start = 10.dp, end = 10.dp),
                            )

                            Row(
                                horizontalArrangement = Arrangement.spacedBy( 10.dp),
                                verticalAlignment = Alignment.CenterVertically,
                            ) {
                                Image(painter = painterResource(id = R.drawable.device_light), contentDescription = null)
                                Image(painter = painterResource(id = R.drawable.device_fan), contentDescription = null )
                                Image(painter = painterResource(id = R.drawable.air_condition), contentDescription = null )
                                Image(painter = painterResource(id = R.drawable.live_tv), contentDescription = null )
                            }
                        }

                    }


                }




                Row(
                    modifier = Modifier
                ) {

                    Column {

                    }
                }


            }

        },
        bottomBar = {

            BottomBar(
                modifier = modifier,
                homeIcon = R.drawable.home_selected,
                personIcon = R.drawable.person_common,
                onHomeIconClick = {

                },
                onPersonIconClick = {

                }
            )
        },


        )
}
