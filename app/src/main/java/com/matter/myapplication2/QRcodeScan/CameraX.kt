package com.matter.myapplication2.QRcodeScan

import TokenManager
import android.Manifest
import android.util.Log
import android.widget.Toast
import androidx.camera.core.CameraSelector
import androidx.camera.core.ExperimentalGetImage
import androidx.camera.core.ImageAnalysis
import androidx.camera.core.ImageProxy
import androidx.camera.lifecycle.ProcessCameraProvider
import androidx.camera.view.PreviewView
import androidx.compose.foundation.Image
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.IconButton
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalLifecycleOwner
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.viewinterop.AndroidView
import androidx.core.content.ContextCompat
import androidx.navigation.NavHostController
import com.google.accompanist.permissions.ExperimentalPermissionsApi
import com.google.accompanist.permissions.PermissionsRequired
import com.google.accompanist.permissions.rememberMultiplePermissionsState
import com.google.mlkit.vision.barcode.BarcodeScanner
import com.google.mlkit.vision.barcode.BarcodeScanning
import com.google.mlkit.vision.common.InputImage
import com.matter.myapplication2.DeviceRepository.DeviceInfoStorage
import com.matter.myapplication2.R
import com.matter.myapplication2.navigateSingleTopTo
import es.dmoral.toasty.Toasty
import matter.onboardingpayload.OnboardingPayloadParser
import matter.onboardingpayload.UnrecognizedQrCodeException

@Composable
fun CameraX(onBarcodeScanned: (String) -> Unit) {
    val lifecycleOwner = LocalLifecycleOwner.current
    val context = LocalContext.current

    val cameraProviderFuture = remember {
        ProcessCameraProvider.getInstance(context)
    }

    val previewView = remember {
        PreviewView(context).apply {
            id = R.id.previewView
        }
    }
    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(30.dp),
        verticalArrangement = Arrangement.Center,
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Text(
            text = "步骤1", fontSize = 20.sp,
            textAlign = TextAlign.Center,
            modifier = Modifier.padding(top = 100.dp)
        )
        Text(
            text = "请将二维码放在相机的扫描区域",
            fontSize = 20.sp,
            textAlign = TextAlign.Center,
            modifier = Modifier.padding(top = 20.dp)
        )
        AndroidView(
            factory = { previewView },
            modifier = Modifier
                .fillMaxSize()
                .padding(vertical = 100.dp)
        )

    }

    LaunchedEffect(cameraProviderFuture) {
        cameraProviderFuture.addListener({
            val cameraProvider = cameraProviderFuture.get()
            val preview = androidx.camera.core.Preview.Builder().build().also {
                it.setSurfaceProvider(previewView.surfaceProvider)
            }
            val cameraSelector = CameraSelector.DEFAULT_BACK_CAMERA

            val barcodeScanner = BarcodeScanning.getClient()

            val imageAnalysis = ImageAnalysis.Builder().build().also { analysis ->
                analysis.setAnalyzer(ContextCompat.getMainExecutor(context)) { imageProxy ->
                    processImageProxy(barcodeScanner, imageProxy, onBarcodeScanned)
                }
            }

            try {
                cameraProvider.unbindAll()
                cameraProvider.bindToLifecycle(
                    lifecycleOwner,
                    cameraSelector,
                    preview,
                    imageAnalysis
                )
            } catch (e: Exception) {
                Log.e("CameraX", "Use case binding failed", e)
            }
        }, ContextCompat.getMainExecutor(context))
    }
}

@androidx.annotation.OptIn(ExperimentalGetImage::class)
private fun processImageProxy(
    barcodeScanner: BarcodeScanner,
    imageProxy: ImageProxy,
    onBarcodeScanned: (String) -> Unit
) {
    val mediaImage = imageProxy.image
    if (mediaImage != null) {
        val image = InputImage.fromMediaImage(mediaImage, imageProxy.imageInfo.rotationDegrees)
        barcodeScanner.process(image)
            .addOnSuccessListener { barcodes ->
                for (barcode in barcodes) {
                    barcode.rawValue?.let { value ->
                        onBarcodeScanned(value)
                    }
                }
            }
            .addOnFailureListener {
                Log.e("CameraX", "Barcode scanning failed", it)
            }
            .addOnCompleteListener {
                imageProxy.close()
            }
    } else {
        imageProxy.close()
    }
}

@OptIn(ExperimentalPermissionsApi::class)
@Composable
fun CameraScreen(navController: NavHostController) {
    val permissionState = rememberMultiplePermissionsState(
        listOf(
            Manifest.permission.CAMERA,
            Manifest.permission.BLUETOOTH_SCAN,
            Manifest.permission.ACCESS_FINE_LOCATION,
            Manifest.permission.BLUETOOTH,
            Manifest.permission.BLUETOOTH_CONNECT,
        )
    )

    PermissionsRequired(
        multiplePermissionsState = permissionState,
        permissionsNotGrantedContent = {
            Column(
                modifier = Modifier.fillMaxSize()
            ) {
                Row(modifier = Modifier.weight(1f)) {
                }
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(bottom = 16.dp),
                    horizontalArrangement = Arrangement.Center,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    IconButton(onClick = {
                        permissionState.launchMultiplePermissionRequest()
                    }) {
                        Image(
                            painter = painterResource(id = R.drawable.photo_camera),
                            contentDescription = null,
                        )
                    }
                }
            }
        },
        permissionsNotAvailableContent = {
            // 提示用户权限不可用的UI
        }
    ) {
        val context = LocalContext.current
        CameraX { barcode ->
            Log.e("barcode", barcode)
            try {
                val payload = OnboardingPayloadParser().parseQrCode(barcode)

                if (TokenManager.getPairingSelection() == "share") {
                    TokenManager.setQrcodeInfo(barcode)
                    navController.navigateSingleTopTo("shareParing")
                } else {
                    val deviceInfo = CHIPDeviceInfo.fromSetupPayload(payload)
                    DeviceInfoStorage.saveDeviceInfo(context, deviceInfo)
                    Log.e("deviceinfo", deviceInfo.toString())
                    navController.navigateSingleTopTo("enterWifi")
                }
            } catch (ex: UnrecognizedQrCodeException) {
                Log.e("barcode", "Unrecognized QR Code", ex)
                Toasty.error(context, "Unrecognized QR Code", Toast.LENGTH_SHORT).show()
            } catch (ex: Exception) {
                Log.e("barcode", "Exception, $ex")
                Toasty.error(context, "Occur Exception, $ex", Toast.LENGTH_SHORT).show()
            }
        }
    }


}

