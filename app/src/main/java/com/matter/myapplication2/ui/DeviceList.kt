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
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.unit.dp
import androidx.navigation.NavHostController
import com.king.ultraswiperefresh.NestedScrollMode
import com.king.ultraswiperefresh.UltraSwipeRefresh
import com.king.ultraswiperefresh.indicator.SwipeRefreshHeader
import com.king.ultraswiperefresh.rememberUltraSwipeRefreshState
import com.matter.myapplication2.DeviceRepository.DeviceRepository
import com.matter.myapplication2.R
import com.matter.myapplication2.navigateSingleTopTo
import com.matter.myapplication2.refreshToken
import es.dmoral.toasty.Toasty


@Composable
fun DeviceList(modifier: Modifier = Modifier,navController: NavHostController) {
    // Device list state
    val deviceListState = remember { mutableStateOf<List<Device>>(emptyList()) }
    val context= LocalContext.current

    val state = rememberUltraSwipeRefreshState()

    LaunchedEffect(Unit) {
        val devices = DeviceRepository.getDevices()
        deviceListState.value = devices
        // Refresh token and handle navigation if token is invalid
//        val newToken = withContext(Dispatchers.IO) {
//            refreshToken(TokenManager.getToken().toString())
//        }
//        if (newToken != null) {
//            TokenManager.setToken(newToken)
//        } else {
//            navController.navigate("login") {
//                popUpTo(navController.graph.startDestinationId) {
//                    inclusive = true
//                }
//                launchSingleTop = true
//            }
//            return@LaunchedEffect
//        }

        // Fetch initial device list

        Log.e("LaunchedEffect DeviceList", "Device list updated: ${devices.size}")
    }

    LaunchedEffect(state.isRefreshing) {
        if (state.isRefreshing) {
            // TODO 刷新的逻辑处理，此处的延时只是为了演示效果

            val devices = DeviceRepository.refreshDevices()
            val token = refreshToken(TokenManager.getToken().toString())
            if (token != null) {
                TokenManager.setToken(token)
            }
            if (devices.isNotEmpty()) {
                Toasty.success( context,"刷新成功", Toasty.LENGTH_SHORT).show()
                deviceListState.value = devices
            }
            else
            {
                Toasty.error( context,"刷新失败", Toasty.LENGTH_SHORT).show()
            }
            state.isRefreshing=false
        }
    }

    MainScreen(
        modifier = modifier,
        navController = navController,
        paddingContent = {
                paddingValues->
            UltraSwipeRefresh(state = state,
                onRefresh = { state.isRefreshing=true },
                onLoadMore = {},
                modifier=Modifier.padding(paddingValues),
                headerScrollMode = NestedScrollMode.FixedContent,
                headerMaxOffsetRate = 4f,
                headerIndicator = {
                    SwipeRefreshHeader(state = it,
                        paddingValues = PaddingValues(top = 50.dp)
                    )
                },
                loadMoreEnabled = false
            ) {

                LazyColumn(
                    modifier = Modifier
                        .padding(10.dp)

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


@Composable
fun DeviceItem(device: Device, modifier: Modifier = Modifier, onClick: () -> Unit) {
    
    val image= when(device.deviceType)
    {
        "260"-> R.drawable.device_light
        else-> R.drawable.device_unknown
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
                .padding(16.dp) ,// Padding inside the item
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
    val qrcode: String?
)

fun deviceMap(deviceType: String):String
{
    val deviceTypeInt=deviceType.toInt()
    return when(deviceTypeInt)
    {
        260->"无线调光灯"
        261->"无线彩灯"
        0x000F->"开关"
        0x0106->"光照度传感"
        else->"未知设备"
    }
}


