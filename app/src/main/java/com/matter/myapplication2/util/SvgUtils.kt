
import android.graphics.Bitmap
import android.graphics.Canvas
import android.graphics.Matrix
import android.util.Log
import com.caverock.androidsvg.SVG
import okhttp3.Call
import okhttp3.Callback
import okhttp3.OkHttpClient
import okhttp3.Request
import okhttp3.Response
import java.io.IOException

object SvgUtils {

    private val client = OkHttpClient()

    fun fetchSvg(url: String, targetSize: Int, callback: (Bitmap?) -> Unit) {
        val request = Request.Builder()
            .addHeader("Authorization", "${TokenManager.getToken()}")
            .url(url)
            .build()

        client.newCall(request).enqueue(object : Callback {
            override fun onFailure(call: Call, e: IOException) {
                e.printStackTrace()
                callback(null)
            }

            override fun onResponse(call: Call, response: Response) {
                try {
                    val svgContent = response.body?.string() // Read the response body once

                    Log.e("SvgUtils", "Response: $svgContent")

                    if (svgContent != null) {
                        val svg = SVG.getFromString(svgContent)
                        val bitmap = Bitmap.createBitmap(targetSize, targetSize, Bitmap.Config.ARGB_8888)
                        val canvas = Canvas(bitmap)

                        // Calculate the scale factor to fit the target size
                        val scaleFactor = targetSize / Math.max(svg.documentWidth, svg.documentHeight)
                        val matrix = Matrix()
                        matrix.postScale(scaleFactor, scaleFactor, 0f, 0f)
                        canvas.concat(matrix)

                        svg.renderToCanvas(canvas)
                        callback(bitmap)
                    } else {
                        Log.e("SvgUtils", "SVG content is null")
                        callback(null)
                    }
                } catch (e: Exception) {
                    Log.e("SvgUtils", "Error loading SVG: ${e.message}")
                    callback(null) // Handle error if needed
                }
            }

        })
    }
}
