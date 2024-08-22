package com.matter.myapplication2.ui


import TokenManager
import android.util.Log
import android.widget.Toast
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.IntrinsicSize
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material3.Button
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.material3.TextField
import androidx.compose.material3.TextFieldDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
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
import com.matter.myapplication2.navigateSingleTopTo
import com.matter.myapplication2.util.HttpApi
import es.dmoral.toasty.Toasty
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.withContext
import okhttp3.MediaType.Companion.toMediaTypeOrNull
import okhttp3.OkHttpClient
import okhttp3.Request
import okhttp3.RequestBody.Companion.toRequestBody
import org.json.JSONObject


@OptIn(ExperimentalMaterial3Api::class, ExperimentalCoroutinesApi::class)
@Composable
fun LoginPage(modifier: Modifier = Modifier, navController: NavHostController) {


    val context = LocalContext.current
    var loginClicked by remember { mutableStateOf(false) }


    var usernameInput by remember { mutableStateOf("") }
    var passwordInput by remember { mutableStateOf("") }

    var showTextFields by remember { mutableStateOf(false) }

    var HostIp by remember { mutableStateOf("") }
    var MatterIp by remember { mutableStateOf("") }

    val token = TokenManager.getToken()
    if (!token.isNullOrBlank()) {
        navController.navigateSingleTopTo("home")
    }

    LaunchedEffect(loginClicked) {
        val response = Login(
            usernameInput,
            passwordInput,
            hostIp = HostIp
        )

        if (response.success) {
            response.errorMessage?.let {
                Toasty.success(context, it, Toasty.LENGTH_SHORT).show()
            }
            navController.navigateSingleTopTo("home")
        } else {
            response.errorMessage?.let { Toasty.error(context, it, Toasty.LENGTH_SHORT).show() }
        }
    }




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
                .height(IntrinsicSize.Min)
                .width(IntrinsicSize.Min)
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
                    value = usernameInput, onValueChange = { usernameInput = it },
                    label = {
                        Text(text = stringResource(id = R.string.username))
                    },
                    keyboardOptions = KeyboardOptions.Default.copy(
                        imeAction = ImeAction.Next
                    ),
                    shape = RoundedCornerShape(12.dp),
                    colors = TextFieldDefaults.textFieldColors(
                        focusedIndicatorColor = Color.Transparent, // 去掉底部的横线
                        unfocusedIndicatorColor = Color.Transparent // 去掉底部的横线
                    ),

                    )

                TextField(
                    value = passwordInput, onValueChange = { passwordInput = it },
                    label = {
                        Text(text = stringResource(id = R.string.password))
                    },
                    keyboardOptions = KeyboardOptions.Default.copy(
                        imeAction = ImeAction.Done
                    ),
                    shape = RoundedCornerShape(12.dp),
                    colors = TextFieldDefaults.textFieldColors(
                        focusedIndicatorColor = Color.Transparent, // 去掉底部的横线
                        unfocusedIndicatorColor = Color.Transparent // 去掉底部的横线
                    ),
                )

                Button(onClick = {
                    loginClicked = !loginClicked
                }) {
                    Text(
                        text = "登录",
                        fontSize = 20.sp
                    )
                }


                if (showTextFields) {
                    TextField(
                        value = HostIp,
                        onValueChange = { HostIp = it },
                        label = { Text(text = "HostIp") },
                        keyboardOptions = KeyboardOptions.Default.copy(imeAction = ImeAction.Next),
                        shape = RoundedCornerShape(12.dp),
                        colors = TextFieldDefaults.textFieldColors(
                            focusedIndicatorColor = Color.Transparent,
                            unfocusedIndicatorColor = Color.Transparent
                        )
                    )

                    TextField(
                        value = MatterIp,
                        onValueChange = { MatterIp = it },
                        label = { Text(text = "MatterIp") },
                        keyboardOptions = KeyboardOptions.Default.copy(imeAction = ImeAction.Done),
                        shape = RoundedCornerShape(12.dp),
                        colors = TextFieldDefaults.textFieldColors(
                            focusedIndicatorColor = Color.Transparent,
                            unfocusedIndicatorColor = Color.Transparent
                        )
                    )

                    Button(onClick = {
                        TokenManager.clearHostIpv4()
                        TokenManager.clearMatterIpv4()

                        TokenManager.setHostIpv4(HostIp)
                        TokenManager.setMatterIpv4(MatterIp)
                        Log.e(
                            "Login",
                            "onClick: ${TokenManager.getHostIpv4()} ${TokenManager.getMatterIpv4()}"
                        )
                        Log.e("Login", HttpApi.BASE_URL.value)
                        Toasty.success(context, "保存成功", Toast.LENGTH_SHORT).show()
                    }) {
                        Text(text = "保存")
                    }
                }

                Row(
                    verticalAlignment = Alignment.CenterVertically,
                ){

                    Box(modifier = Modifier.weight(1f))
                    Button(
                        onClick = {
                            showTextFields = !showTextFields
                        }, modifier = Modifier
                            .height(40.dp)
                            .padding(end = 20.dp)
                    ) {
                        Text(
                            text = if (showTextFields) "收起" else "设置网络",
                        )
                    }
                    
                    Button(onClick = {
                        navController.navigateSingleTopTo("register")
                    }) {
                        Text(text = "注册")
                    }
                }
            }


        }

    }

}

suspend fun Login(username: String, password: String, hostIp: String): LoginResult =
    withContext(Dispatchers.IO) {
        val TAG = "Login"
        val json = """
        {
            "username": "$username",
            "password": "$password"
        }
    """
        val mediaType = "application/json; charset=utf-8".toMediaTypeOrNull()
        val requestBody = json.toRequestBody(mediaType)
        val client = OkHttpClient()

        Log.e(TAG, json)
        try {
            val request = Request.Builder()
                .url("http://${hostIp.toString()}:8080/api/login")
                .post(requestBody)
                .build()
            Log.e(TAG, "onResponse: ${request.url}  ")
            val response = client.newCall(request).execute()
            val responseBody = response.body?.string()

            if (response.isSuccessful && !responseBody.isNullOrBlank()) {
                Log.d(TAG, "onResponse: $responseBody")
                val jsonObject = JSONObject(responseBody)
                val token = jsonObject.getJSONObject("data").getString("token")

                TokenManager.setToken(token)
                TokenManager.setUsername(username)
                TokenManager.setPassword(password)

                return@withContext LoginResult(true, "登录成功")
            } else {
                Log.e(TAG, "onResponse: ${responseBody ?: "Empty response body"}")
                return@withContext LoginResult(false, "登录失败，用户名或密码错误")
            }
        } catch (e: Exception) {
            Log.e(TAG, "Login error: ${e.message}")
            return@withContext LoginResult(false, "登录失败，网络错误")
        }
    }

data class LoginResult(val success: Boolean, val errorMessage: String? = null)

