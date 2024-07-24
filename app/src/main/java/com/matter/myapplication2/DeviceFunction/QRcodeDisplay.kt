package com.matter.myapplication2.DeviceFunction

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp


@Composable
fun QRcodeDisplay(qrcode: String)
{
    Column(
        modifier = Modifier
            .fillMaxWidth()
            .padding(top = 16.dp),
        verticalArrangement = Arrangement.Center,
        horizontalAlignment = Alignment.CenterHorizontally
    ) {

        Text(text = "Matter设备二维码",
            fontWeight = FontWeight.Bold)
        Spacer(modifier = Modifier.height(16.dp))
        MatterQRCode(value = qrcode)
        Spacer(modifier = Modifier.height(16.dp))
        Text(text = "桥接设备二维码", fontWeight = FontWeight.Bold)
        Spacer(modifier = Modifier.height(16.dp))
        SvgImage()
    }
}