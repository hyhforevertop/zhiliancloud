package com.matter.myapplication2.provisioning

import TokenManager
import android.util.Log
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.height
import androidx.compose.material3.Button
import androidx.compose.material3.Text
import androidx.compose.material3.TextField
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.navigation.NavController
import com.matter.myapplication2.DeviceRepository.DeviceRepository
import com.matter.myapplication2.ui.Device
import com.matter.myapplication2.util.HttpApi
import es.dmoral.toasty.Toasty
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.delay
import kotlinx.coroutines.withContext
import okhttp3.OkHttpClient
import okhttp3.Request
import okhttp3.RequestBody.Companion.toRequestBody

@Composable
fun UpdateName(
    modifier: Modifier=Modifier,
    deviceId: String,
    navController: NavController
){
    val context= LocalContext.current
    val deviceFind = DeviceRepository.getDevices().find { it.deviceId == deviceId }
    val device by remember { mutableStateOf(deviceFind) }
    var getDeviceName by remember { mutableStateOf("")  }
    var check by remember { mutableStateOf(false) }

    LaunchedEffect(key1 = check) {
        val isOk=updateDeviceName(device, deviceName = getDeviceName)
        if (isOk) {
            Toasty.success(context, "修改成功", Toasty.LENGTH_SHORT).show()
            delay(1000)
            navController.navigate("home")
            {
                popUpTo("home") {
                    inclusive = true
                }
            }
        }
        else
        {
            Toasty.error(context, "修改失败", Toasty.LENGTH_SHORT).show()
        }
    }


    Box(
        modifier = Modifier.fillMaxSize(),
        contentAlignment = Alignment.Center
    ) {
        Column(
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Text(
                text = "修改当前设备名称",
                fontSize = 40.sp
            )
            Spacer(modifier = Modifier.height(16.dp))
           TextField(value = getDeviceName, onValueChange = {
               getDeviceName=it
           })

            Button(onClick = {  check=true }) {
                Text(text = "确定")
            }
        }
    }



}


suspend fun updateDeviceName(device: Device?, deviceName: String):Boolean= withContext(Dispatchers.IO) {

    val client=OkHttpClient()
    val json="""
        {
            "deviceId": "${device!!.deviceId}",
            "deviceName": "$deviceName",
            "deviceType": ${device!!.deviceType},
            "deviceManufacturer": "${device!!.deviceManufacturer}",
            "location":  "${device!!.location}"
        }
    """.trimIndent()

    try {
        val request =Request.Builder()
            .url(HttpApi.UPDATE_DEVICE.value)
            .put(json.toRequestBody())
            .addHeader("Authorization", "${TokenManager.getToken()}")
            .build()

        val response=client.newCall(request).execute()
        if (response.isSuccessful)
        {
            Log.e("updateDeviceName", "Success")
            return@withContext true
        }
        else
        {
            Log.e("updateDeviceName", "Failed")
            return@withContext false
        }
    }
    catch (e:Exception)
    {
         Log.e( "updateDeviceName",e.message.toString())
        return@withContext false
    }
}