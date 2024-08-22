package com.matter.myapplication2.QRcodeScan

import TokenManager
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.width
import androidx.compose.material3.Button
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.navigation.NavController
import kotlinx.coroutines.delay

@Composable
fun PairingSelection(
    modifier: Modifier = Modifier,
    navController: NavController
) {
    var selection by remember { mutableStateOf("") }

    LaunchedEffect(key1 = selection) {
        when (selection) {
            "share" -> {
                TokenManager.setPairingSelection("share")
                navController.navigate("shareParing")
            }

            "normal" -> {
                TokenManager.setPairingSelection("normal")
                navController.navigate("camera")
            }
        }

        delay(2000)


    }
    Row(
        modifier = Modifier.fillMaxSize(),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.Center
    ) {

        Button(onClick = {
            selection = "share"
        }) {
            Text(text = "分享设备")
        }
        Spacer(modifier = Modifier.width(8.dp))
        Button(onClick = {
            selection = "normal"
        }) {
            Text(text = "普通设备")
        }
    }

}