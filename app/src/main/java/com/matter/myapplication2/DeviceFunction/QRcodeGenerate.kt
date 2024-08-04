package com.matter.myapplication2.DeviceFunction

import SvgUtils
import android.graphics.Bitmap
import android.graphics.Color
import android.util.Log
import androidx.compose.foundation.Image
import androidx.compose.foundation.layout.size
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.asImageBitmap
import androidx.compose.ui.graphics.painter.BitmapPainter
import androidx.compose.ui.unit.dp
import com.google.zxing.BarcodeFormat
import com.google.zxing.MultiFormatWriter
import com.google.zxing.common.BitMatrix
import com.matter.myapplication2.util.HttpApi

@Composable
fun MatterQRCode(value: String, size: Int = 148) {
    Log.e("QRCode", "value: $value")
    val bitmap = remember { generateQRCode(value, size, size) }

    if (bitmap != null) {
        Image(
            painter = BitmapPainter(bitmap.asImageBitmap()),
            contentDescription = "QR Code",
            modifier = Modifier.size(size.dp)
        )
    }
}

@Composable
fun SvgImage(modifier: Modifier = Modifier, size: Int = 128) {
    var bitmap by remember { mutableStateOf<Bitmap?>(null) }
    //val url = "http://192.168.14.191:5001/get-svg"
        val url = HttpApi.BRIDGE_QRCODE.value
    LaunchedEffect(url) {
        try {
            SvgUtils.fetchSvg(url, size) { fetchedBitmap ->
                bitmap = fetchedBitmap
            }
        } catch (e: Exception) {
            // Handle error if needed
            Log.e("SvgUtils", "Error fetching SVG: ${e.message}")
            bitmap = null
        }
    }

    bitmap?.let {
        Image(
            painter = BitmapPainter(it.asImageBitmap()),
            contentDescription = "SVG Image",
            modifier = modifier.size(size.dp)
        )
    }
}

fun generateQRCode(text: String, width: Int, height: Int): Bitmap? {
    return try {
        val bitMatrix: BitMatrix = MultiFormatWriter().encode(text, BarcodeFormat.QR_CODE, width, height)
        val bitmap = Bitmap.createBitmap(width, height, Bitmap.Config.RGB_565)
        for (x in 0 until width) {
            for (y in 0 until height) {
                bitmap.setPixel(x, y, if (bitMatrix.get(x, y)) Color.BLACK else Color.WHITE)
            }
        }
        bitmap
    } catch (e: Exception) {
        e.printStackTrace()
        null
    }
}