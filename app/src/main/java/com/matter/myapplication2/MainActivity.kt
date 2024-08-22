// MainActivity.kt
package com.matter.myapplication2

import TokenManager
import android.net.nsd.NsdManager
import android.os.Bundle
import android.util.Log
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.animation.EnterTransition
import androidx.compose.animation.ExitTransition
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.runtime.Composable
import androidx.core.view.WindowCompat
import androidx.navigation.NavGraph.Companion.findStartDestination
import androidx.navigation.NavHostController
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import com.matter.myapplication2.EnterWIFI.EnterWIFI
import com.matter.myapplication2.QRcodeScan.CameraScreen
import com.matter.myapplication2.QRcodeScan.PairingSelection
import com.matter.myapplication2.provisioning.ProvisioningScreen
import com.matter.myapplication2.provisioning.ShareParing
import com.matter.myapplication2.provisioning.UpdateName
import com.matter.myapplication2.ui.DeviceDetail
import com.matter.myapplication2.ui.DeviceList
import com.matter.myapplication2.ui.LoginPage
import com.matter.myapplication2.ui.ProfilePage
import com.matter.myapplication2.ui.RegisterPage
import com.matter.myapplication2.ui.theme.MyApplication2Theme
import com.matter.myapplication2.util.HttpApi
import com.matter.myapplication2.util.HttpClient
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.suspendCancellableCoroutine
import kotlinx.coroutines.withContext
import okhttp3.Call
import okhttp3.Callback
import okhttp3.Response
import org.json.JSONObject
import java.io.IOException
import kotlin.coroutines.resume


class MainActivity : ComponentActivity() {

    private lateinit var nsdManager: NsdManager

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        TokenManager.init(this)

        // 启动扫描服务
//      val scanServiceIntent = Intent(this, ScanService::class.java)
//      startService(scanServiceIntent)


        setContent {

            MyApplication2Theme {
                val navController = rememberNavController()

                Surface(
                    color = MaterialTheme.colorScheme.background
                ) {

                    AppNavHost(navController = navController)

                    window.statusBarColor = 0
                    WindowCompat.setDecorFitsSystemWindows(window, false)
                    WindowCompat.getInsetsController(
                        window,
                        window.decorView
                    ).isAppearanceLightStatusBars = true
                    //a sd

                }
            }
        }
    }


    override fun onDestroy() {
        super.onDestroy()
        // 在这里添加你需要在关闭时执行的操作
//        stopService(Intent(this, ScanService::class.java)) // 停止扫描服务

        TokenManager.clearHostIpv4()
        TokenManager.clearMatterIpv4()
    }


}


fun NavHostController.navigateSingleTopTo(route: String) =
    this.navigate(route) {
        popUpTo(
            this@navigateSingleTopTo.graph.findStartDestination().id
        ) {
            saveState = true
        }
        launchSingleTop = true
        restoreState = true
    }


@Composable
fun AppNavHost(navController: NavHostController) {
    val token = TokenManager.getToken()

    val startDestination = if (token != null) {
        "home" // 已登录时导航到主页
    } else {
        "login" // 未登录时导航到登录页面
    }

    NavHost(
        navController = navController, startDestination = startDestination,
        enterTransition = { EnterTransition.None },
        exitTransition = { ExitTransition.None },
        popExitTransition = { ExitTransition.None },
        popEnterTransition = { EnterTransition.None }
    ) {
        composable("home") { DeviceList(navController = navController) }
        composable("profile") { ProfilePage(navController = navController) }
        composable("login") { LoginPage(navController = navController) }

        composable("deviceDetail/{deviceId}") { backStackEntry ->
            backStackEntry.arguments?.getString("deviceId")?.let { deviceId ->
                DeviceDetail(deviceId = deviceId, navController = navController)
            }
        }
        composable("camera") { CameraScreen(navController = navController) }
        composable("enterWifi") { EnterWIFI(navController = navController) }
        composable("provisioning") { ProvisioningScreen(navController) }
        composable("updateName/{deviceId}") { backStackEntry ->
            backStackEntry.arguments?.getString("deviceId")?.let { deviceId ->
                UpdateName(deviceId = deviceId, navController = navController)
            }
        }
        composable("paring_selection")
        {
            PairingSelection(navController = navController)
        }
        composable("shareParing")
        {
            ShareParing(navController=navController)
        }
        
        composable("register"){
            RegisterPage(navController = navController)
        }

    }
}


suspend fun refreshToken(currentToken: String): String? {
    return withContext(Dispatchers.IO) {
        suspendCancellableCoroutine<String?> { continuation ->
            HttpClient.post(
                url = HttpApi.LOGIN_API.value,
                json = """{ 
                    "username":"${TokenManager.getUsername()}",
                    "password":"${TokenManager.getPassword()}"
                    }""".trimMargin(),
                token = currentToken,
                callback = object : Callback {
                    override fun onFailure(call: Call, e: IOException) {
                        Log.e(
                            HttpClient.TAG,
                            "网络请求失败 $e ,${TokenManager.getUsername()} ${TokenManager.getPassword()}"
                        )
                        continuation.resume(null) // 网络请求失败，返回 null
                    }

                    override fun onResponse(call: Call, response: Response) {
                        if (response.isSuccessful) {
                            val responseBody = response.body?.string()
                            val jsonObject = responseBody?.let { JSONObject(it) }
                            val token = jsonObject?.getJSONObject("data")?.getString("token")
                            // 更新 token
                            TokenManager.setToken(token ?: "")
                            Log.i(HttpClient.TAG, "刷新 token 成功: $token")
                            continuation.resume(token) // 返回新 token
                        } else {
                            Log.e(HttpClient.TAG, "网络请求失败: ${response.code}")
                            continuation.resume(null) // 响应失败，返回 null
                        }
                    }
                }
            )
        }
    }
}
