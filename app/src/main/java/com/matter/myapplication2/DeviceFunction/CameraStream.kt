
import android.annotation.SuppressLint
import android.webkit.WebView
import android.webkit.WebViewClient
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Button
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
import androidx.compose.ui.unit.dp
import androidx.compose.ui.viewinterop.AndroidView
import kotlinx.coroutines.Dispatchers.IO
import kotlinx.coroutines.delay
import kotlinx.coroutines.withContext
import okhttp3.OkHttpClient
import okhttp3.Request

@SuppressLint("SetJavaScriptEnabled")
@Composable
fun WebViewMjpegStream(url: String) {
    var webView: WebView? by remember { mutableStateOf(null) }
    var expanded by remember { mutableStateOf(false) }

    var quality by remember { mutableIntStateOf(0) }


    val htmlContent = """
        <html>
            <body style="margin: 0; padding: 0;">
                <img src="$url" style="width: 100%; height: auto;" />
            </body>
        </html>
    """

    LaunchedEffect(key1 = quality) {
        webView?.stopLoading()
        delay(1000)
        changeQuality(quality)
        webView?.loadDataWithBaseURL(null, htmlContent, "text/html", "UTF-8", null)
    }
    Button(
        onClick = { expanded = !expanded },
        modifier = Modifier
    ) {
        Text(text = "更改分辨率")
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
                loadDataWithBaseURL(null, htmlContent, "text/html", "UTF-8", null)
                webView = this
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

@Composable
fun DisplayMjpegStream() {
    val mjpegUrl = "http://192.168.1.127:81/stream"
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
            .url("http://192.168.1.127/control?var=framesize&val=${quality}")
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

