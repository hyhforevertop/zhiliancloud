package com.matter.myapplication2.ui

import TokenManager
import android.util.Log
import android.widget.Toast
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material3.Button
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.material3.TextField
import androidx.compose.material3.TextFieldDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.navigation.NavHostController
import com.matter.myapplication2.R
import com.matter.myapplication2.util.HttpApi
import com.matter.myapplication2.util.HttpClient
import es.dmoral.toasty.Toasty
import kotlinx.coroutines.launch
import kotlinx.coroutines.suspendCancellableCoroutine
import okhttp3.Call
import okhttp3.Callback
import okhttp3.Response
import org.json.JSONObject
import java.io.IOException
import kotlin.coroutines.resume


@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun LoginPage(modifier: Modifier=Modifier,navController: NavHostController)
{

    val coroutineScope = rememberCoroutineScope()
    val context = LocalContext.current

    var usernameInput by remember { mutableStateOf("") }
    var passwordInput by remember { mutableStateOf("") }




    Column(
        modifier = modifier
            .fillMaxSize()
            .background(
                brush = Brush.verticalGradient(
                    colors = listOf(
                        Color(0xFF79bbff),
                        Color(0xFF4a90e2),
                        Color(0xFF0077CC)
                    ),
                    startY = 0.0f,
                    endY = 1000.0f
                )
            ),
        verticalArrangement = Arrangement.Center, // 内容垂直居中
        horizontalAlignment = Alignment.CenterHorizontally // 内容水平居中
    ) {

        Text(
            text = stringResource(id = R.string.app_title),
            fontSize = 30.sp,
            color = Color.White,
            fontWeight = FontWeight.Bold,
            modifier = Modifier
                .padding(bottom = 20.dp)
                .shadow(4.dp, shape = RoundedCornerShape(8.dp))
                .background(color = Color(0XFF3A9EFD), shape = RoundedCornerShape(8.dp))
                .padding(horizontal = 16.dp, vertical = 8.dp)
        )

        Box(
            modifier = Modifier
                .size(300.dp, 300.dp)
                .shadow(5.dp, RoundedCornerShape(16.dp))
                .clip(shape = RoundedCornerShape(16.dp))
                .background(color = MaterialTheme.colorScheme.surface)
                .padding(16.dp), // 添加内部填充
            contentAlignment = Alignment.Center // 将内容居中
        ) {
            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(horizontal = 16.dp),
                verticalArrangement = Arrangement.spacedBy(16.dp),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                Text(
                    text = stringResource(id = R.string.login),
                    fontSize = MaterialTheme.typography.titleLarge.fontSize,
                    modifier = Modifier.padding(top = 10.dp)
                )

                TextField(
                    value = usernameInput, onValueChange = {usernameInput=it},
                    label = {
                        Text(text = stringResource(id = R.string.username))
                    },
                    keyboardOptions= KeyboardOptions.Default.copy(
                        imeAction = ImeAction.Next
                    ),
                    shape = RoundedCornerShape(12.dp),
                    colors = TextFieldDefaults.textFieldColors(
                        focusedIndicatorColor = Color.Transparent, // 去掉底部的横线
                        unfocusedIndicatorColor = Color.Transparent // 去掉底部的横线
                    ),

                )

                TextField(
                    value = passwordInput, onValueChange = {passwordInput=it},
                    label = {
                        Text(text = stringResource(id = R.string.password))
                    },
                    keyboardOptions= KeyboardOptions.Default.copy(
                        imeAction = ImeAction.Done
                    ),
                    shape = RoundedCornerShape(12.dp),
                    colors = TextFieldDefaults.textFieldColors(
                        focusedIndicatorColor = Color.Transparent, // 去掉底部的横线
                        unfocusedIndicatorColor = Color.Transparent // 去掉底部的横线
                    ),
                )

                Button(onClick = {
                    coroutineScope.launch {
                        val loginResult = login(usernameInput, passwordInput)
                        if (loginResult.success) {
                            // 登录成功逻辑
                            Log.i("Login", "登录成功")
                            // 显示成功消息
                            Toasty.success(context, "登录成功", Toast.LENGTH_SHORT).show()

                            // 导航到主页
                            navController.navigate("home") {
                                popUpTo("home") { inclusive = true }
                                launchSingleTop=true
                            }
                        } else {
                            // 登录失败逻辑
                            Log.e("Login", "登录失败: ${loginResult.errorMessage}")
                            // 显示失败消息
                            Toasty.error(context, "${loginResult.errorMessage}", Toast.LENGTH_SHORT).show()
                        }
                    }

                }) {
                    Text(text = "登录",
                        fontSize = 20.sp)
                }
            }
        }
    }


}


private suspend fun login(username: String, password: String): LoginResult = suspendCancellableCoroutine { continuation->

    val TAG="login"
    val json = """
        {
  "username": "$username",
  "password": "$password"
            }
    """
    try {
        HttpClient.post(
            json = json, url =  HttpApi.LOGIN_API.value, token = "",
            callback = object : Callback {
                override fun onFailure(call: Call, e: IOException) {
                    Log.e(TAG, e.toString())
                    continuation.resume(LoginResult(false, "登录失败，网络错误"))
                }

                override fun onResponse(call: Call, response: Response) {
                    if (response.isSuccessful) {
                        val responseBody = response.body?.string()
                        Log.d(TAG, "onResponse: $responseBody")
                        val jsonObject = responseBody?.let { JSONObject(it) }
                        val token = jsonObject?.getJSONObject("data")?.get("token").toString()
                        // 保存 token
                        TokenManager.setToken(token)
                        TokenManager.setUsername(username)
                        TokenManager.setPassword(password)
                        continuation.resume(LoginResult(true))
                    } else {
                        Log.d(TAG, "onResponse not successful: ${response.body?.string()}")
                        continuation.resume(LoginResult(false, "登录失败，用户名或密码错误"))
                    }
                }

            }
        )
    }
    catch (e: IOException) {
        Log.e(TAG, e.toString())
        continuation.resume(LoginResult(false, "登录失败，网络错误"))
    }


}
data class LoginResult(val success: Boolean, val errorMessage: String? = null)

