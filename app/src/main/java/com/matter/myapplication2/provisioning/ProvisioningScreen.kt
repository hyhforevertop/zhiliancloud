package com.matter.myapplication2.provisioning


import android.annotation.SuppressLint
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Button
import androidx.compose.material3.LinearProgressIndicator
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableLongStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.navigation.NavController
import com.google.chip.chiptool.ChipClient
import es.dmoral.toasty.Toasty
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.Job
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch


@SuppressLint("DefaultLocale", "SuspiciousIndentation")
@OptIn(ExperimentalCoroutinesApi::class)
@Composable
fun ProvisioningScreen(
    navController: NavController,
) {

    var timeLeft by remember { mutableLongStateOf(180000L) } // 3 minutes in milliseconds
    var isRunning by remember { mutableStateOf(true) }
    val scope = rememberCoroutineScope()
    val context= LocalContext.current

    var provisioningJob by remember { mutableStateOf<Job?>(null) }
    val provisioningTool = remember { WiFiProvisioningTool(context, scope) }

    var currentStatus by remember { mutableStateOf("Initial status") }

    var progress by remember { mutableStateOf(0f) }

    val animatedProgress by animateFloatAsState(targetValue = progress)


    LaunchedEffect(Unit) {
        while (timeLeft > 0 && isRunning) {
            delay(1000L) // Wait for 1 second
            timeLeft -= 1000L // Decrement time
        }
        if (isRunning) {
            onTimeout(navController) // Trigger timeout action
        }
    }

    LaunchedEffect(Unit) {
        while (isRunning) {
            delay(1000L) // 每秒查询一次状态
            currentStatus = provisioningTool.currentStatus
            when(currentStatus)
            {
                "step1"->{
                    progress=0.2f
                }
                "step2"->{
                    progress=0.4f
                }
                "step3"->{
                    progress=0.6f
                }
                "step4"->{
                    progress=0.8f
                }
                "step5"->{
                    progress=1.0f
                    Toasty.success(context,"配网成功",Toasty.LENGTH_LONG).show()
                    delay(3000L)
                    isRunning=false
                    onTimeout(navController)
                }
                "failed"->{
                    Toasty.error(context,"配网失败",Toasty.LENGTH_LONG).show()
                    provisioningJob?.cancel()
                    delay(3000L)
                    isRunning=false
                    onTimeout(navController)
                }
            }
        }
    }


    LaunchedEffect(Unit) {
        provisioningJob = scope.launch {
            provisioningTool.provisionDeviceWithWiFi()
        }
    }


    // Background tasks
    LaunchedEffect(Unit) {
        scope.launch {
            while (isRunning) {
                // Perform background tasks here
                // Example task: Log current time every 30 seconds
                delay(30000L)
                // Log current time or perform other tasks
            }
        }
    }

    // UI elements
    val minutes = (timeLeft / 60000).toInt()
    val seconds = ((timeLeft % 60000) / 1000).toInt()
        Box(
            modifier = Modifier.fillMaxSize(),
            contentAlignment = Alignment.Center
        ) {
            Column(
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                Text(
                    text = currentStatus,
                    fontSize = 40.sp
                )
                Spacer(modifier = Modifier.height(16.dp))
                LinearProgressIndicator(
                    progress = animatedProgress,
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 16.dp)
                        .height(20.dp)
                        .clip(RoundedCornerShape(10.dp))

                )
                Spacer(modifier = Modifier.height(16.dp))
                Text(
                        text = String.format("%02d:%02d", minutes, seconds),
                fontSize = 40.sp
                )
                Spacer(modifier = Modifier.height(16.dp))
                Button(onClick = { isRunning = false
                    provisioningJob?.cancel()
                    ChipClient.getDeviceController(context).close()
                    onTimeout(navController)
                }) {
                    Text("取消")
                }
            }
        }
    // Cleanup logic
    DisposableEffect(Unit) {
        onDispose {
            isRunning = false
        }
    }




}
fun onTimeout(navController: NavController ) {


    // Handle timeout action here
    navController.navigate("home")
    {
        popUpTo("home") {
            inclusive = true
        }
    }

}

