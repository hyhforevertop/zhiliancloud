package com.matter.myapplication2.DeviceFunction

import android.content.Context
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.material3.Button
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import com.matter.myapplication2.ui.Device


@Composable
fun Light_Belt(device: Device?, context: Context) {

    var modeSelect by remember { mutableStateOf(0) }



    Column(
        modifier = Modifier,
        verticalArrangement = Arrangement.spacedBy(20.dp),
            horizontalAlignment = androidx.compose.ui.Alignment.CenterHorizontally
    ) {
        Button(onClick = {  }) {
            Text(text = "开关")
        }
        Button(onClick = {
            modeSelect = 1
        }) {
            Text(text = "模式1")
        }
        Button(onClick = {
            modeSelect = 2
        }) {
            Text(text = "模式2")
        }
    }

}

