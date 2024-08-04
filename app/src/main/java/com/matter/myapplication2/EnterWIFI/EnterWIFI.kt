package com.matter.myapplication2.EnterWIFI

import TokenManager
import android.app.Activity
import android.net.wifi.WifiManager
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
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
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.navigation.NavController

@Composable
fun EnterWIFI(
    modifier: Modifier = Modifier,
    navController: NavController
) {


    val context = LocalContext.current
    var wifiname by remember { mutableStateOf("") }

    LaunchedEffect(Unit) {
        val wifiManager = context.getSystemService(Activity.WIFI_SERVICE) as WifiManager
        val wifiInfo = wifiManager.connectionInfo
        wifiname=wifiInfo.ssid.toString().removePrefix("\"").removeSuffix("\"")
    }

    var wifipassword by remember { mutableStateOf("") }
    Column(
        modifier = modifier.fillMaxSize(),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center
    ) {
        Text(
            text = "步骤2", fontSize = 20.sp,
            textAlign = TextAlign.Center,
            modifier = Modifier
        )
        Text(
            text = "请输入当前WIFI的密码",
            fontSize = 20.sp,
            textAlign = TextAlign.Center,
            modifier = Modifier.padding(top = 20.dp)
        )

        Row(
            modifier = Modifier.padding(20.dp),
            horizontalArrangement = Arrangement.Start,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Text(text = "当前WIFI名称: ")

            TextField(value = wifiname, onValueChange ={
                wifiname=it
            } )
        }

        Row(
            modifier = Modifier.padding(20.dp),
            horizontalArrangement = Arrangement.spacedBy(10.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Text(text = "WIFI密码: ")
            TextField(value =wifipassword, onValueChange = {wifipassword=it},
                label = {
                    Text(text = "WIFI密码")
                })
        }

        Button(onClick = {
            TokenManager.setWifiPassword(wifipassword)
            TokenManager.setWifiName(wifiname)
            navController.navigate("provisioning")
        }) {
            Text(text = "开始配对")
        }
    }


}

