

import android.Manifest
import android.annotation.SuppressLint
import android.os.Build
import android.util.Log
import android.webkit.WebView
import android.webkit.WebViewClient
import androidx.annotation.RequiresApi
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.unit.dp
import androidx.compose.ui.viewinterop.AndroidView
import com.google.accompanist.permissions.ExperimentalPermissionsApi
import com.google.accompanist.permissions.rememberPermissionState
import com.matter.myapplication2.util.HttpClient
import com.matter.myapplication2.util.NotificationHandler
import es.dmoral.toasty.Toasty
import kotlinx.coroutines.Dispatchers.IO
import kotlinx.coroutines.delay
import kotlinx.coroutines.withContext
import okhttp3.Call
import okhttp3.Callback
import okhttp3.OkHttpClient
import okhttp3.Request
import okhttp3.Response
import org.json.JSONObject
import java.io.IOException

@RequiresApi(Build.VERSION_CODES.TIRAMISU)
@OptIn(ExperimentalPermissionsApi::class)
@SuppressLint("SetJavaScriptEnabled")
@Composable
fun WebViewMjpegStream(url: String) {
    val context = LocalContext.current

    val postNotificationPermission = rememberPermissionState(permission = Manifest.permission.POST_NOTIFICATIONS)
    val notificationHandler = NotificationHandler(context)
    LaunchedEffect(key1 = true) {
        if (!postNotificationPermission.hasPermission) {
            postNotificationPermission.launchPermissionRequest()
        }
    }

//
//    Column {
//        Button(onClick = {
//            notificationHandler.showSimpleNotification()
//            Log.e("notification", "notification")
//        }) { Text(text = "Simple notification") }
//    }

    var data_length=0

    var webView: WebView? by remember { mutableStateOf(null) }
    var expanded by remember { mutableStateOf(false) }

    var quality by remember { mutableIntStateOf(0) }

    var isFallen by remember { mutableStateOf(false) }

    val htmlContent = """
        <html>
            <body style="margin: 0; padding: 0;">
                <img src="$url" style="width: 100%; height: auto;" />
            </body>
        </html>
    """


//    LaunchedEffect(key1 = quality) {
//        changeQuality(quality)
//        delay(1000)
//     }
//    Button(
//        onClick = { expanded = !expanded },
//        modifier = Modifier
//    ) {
//        Text(text = "更改分辨率")
//    }

    if (isFallen)
        LaunchedEffect(key1 = Unit) {
            notificationHandler.showSimpleNotification()
            Toasty.error(context, "有人跌倒了", Toasty.LENGTH_SHORT).show()
//            val WS_URL = HttpApi.WS_HOST.value
//            val webSocketClient = WebSocketClient(WS_URL, MyWebSocketListener(object :
//                WebSocketResponseListener {
//                override fun onMessageReceived(message: String) {
//                    Log.e("changeLightMode", "Message received: $message")
//
//                }
//
//                override fun onBytesReceived(bytes: ByteString) {
//                    println("Bytes received: ${bytes.hex()}")
//                }
//
//            }, ::setCurrentStatus))
//
//
//            try {
//                webSocketClient.connect()
//
//                val jsonMessage = JsonObject().apply {
//                    addProperty("message_id", UUID.randomUUID().toString())
//                    addProperty("command", "device_command")
//
//                    val payload = JsonObject().apply {
//                        addProperty("level",250)
//                        addProperty("transitionTime", 0)
//                        addProperty("optionsMask", 0)
//                        addProperty("optionsOverride", 0)
//                    }
//
//                    val args = JsonObject().apply {
//                        addProperty("node_id", 185)
//                        addProperty("endpoint_id", 8)
//                        addProperty("cluster_id", 8)
//                        addProperty("command_name", "MoveToLevelWithOnOff")
//                        add("payload", payload)
//                    }
//
//                    add("args", args)
//                }
//
//                Log.e("changeLightMode", jsonMessage.toString())
//                webSocketClient.sendMessage(jsonMessage.toString())
//            } catch (e: Exception) {
//                Log.e("changeLightMode", "Error: ${e.message}")
//            }
            isFallen=false
        }



    LaunchedEffect(key1 = Unit) {
      HttpClient.get(url="http://192.168.1.249:5555", token = "" ,callback = object : Callback {
          override fun onFailure(call: Call, e: IOException) {
              Log.e("notification", "Failed to get data", e)
          }

          override fun onResponse(call: Call, response: Response) {
              response.body?.let {
                  val responseBody = it.string()  // 只调用一次
                  Log.e("notification", responseBody)

                  val json = JSONObject(responseBody)
                  val data = json.getJSONArray("data")
                  data_length = data.length()
              }
          }

      })
    }
    LaunchedEffect(key1 = Unit) {
        while (true)
        {
            delay(500)
            HttpClient.get(url="http://192.168.1.249:5555", token = "" ,callback = object : Callback {
                override fun onFailure(call: Call, e: IOException) {
                    Log.e("notification", "Failed to get data", e)
                }

                override fun onResponse(call: Call, response: Response) {
                    val responseBody = response.body?.string()  // 将响应体内容存储在变量中

                    responseBody?.let {
                        Log.e("notification", it)
                    }

                    val json = responseBody?.let { JSONObject(it) }
                    val data = json?.getJSONArray("data")

                    if (data != null && data.length() > data_length) {
                        isFallen = true
                    }
                    else
                    {
                        isFallen=false
                    }

                    data_length = data?.length() ?: data_length
                }


            })

        }
    }

    if (expanded) {
        Box(
            modifier = Modifier
                .padding(top = 8.dp)
                .fillMaxWidth()
                .background(color = Color.LightGray, shape = RoundedCornerShape(4.dp))
                .padding(8.dp)
        ) {
            Row(
                horizontalArrangement = Arrangement.spacedBy(25.dp)
            ) {
                OptionItem(text = "最低") {
                    expanded = false
                    quality = 3
                }
                OptionItem(text = "低") {
                    expanded = false
                    quality = 6
                }
                OptionItem(text = "高") {
                    expanded = false
                    quality = 9
                }
                OptionItem(text = "最高") {
                    expanded = false
                    quality =12
                }
            }
        }
    }

    AndroidView(
        factory = { context ->
            WebView(context).apply {
                webViewClient = WebViewClient()
                settings.javaScriptEnabled = true
                settings.domStorageEnabled = true
                settings.setSupportZoom(true)
                settings.builtInZoomControls = true
                settings.displayZoomControls = false
                loadUrl(url)
                webView = this
                Log.d("WebView", "Attempting to load URL: $url")
            }

        },
        modifier = Modifier
            .fillMaxWidth()
            .height(300.dp) // Adjust height as needed
    )

    DisposableEffect(Unit) {
        onDispose {
            webView?.destroy()

        }
    }
}

@RequiresApi(Build.VERSION_CODES.TIRAMISU)
@Composable
fun DisplayMjpegStream() {
    val mjpegUrl = "http://192.168.1.249:5000/video_feed"
    WebViewMjpegStream(url = mjpegUrl)
}

@Composable
fun OptionItem(text: String, onClick: () -> Unit) {
    Text(
        text = text,
        modifier = Modifier
            .padding(horizontal = 8.dp, vertical = 4.dp)
            .clickable(onClick = onClick)
    )
}

suspend fun changeQuality(quality: Int) = withContext(IO) {
    val client = OkHttpClient()

    try {
        val request = Request.Builder()
            .url("http://192.168.1.127/control?var=framesize&val=${13}")
            .build()

        val response = client.newCall(request).execute()

        if (response.isSuccessful) {
            println("Quality changed successfully")
        }
        else {
            println("Failed to change quality")
        }
    }
    catch (e: Exception) {
        println("Error: ${e.message}")
    }

}

