package com.matter.myapplication2.provisioning

import APICommand
import TokenManager
import android.annotation.SuppressLint
import android.util.Log
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Button
import androidx.compose.material3.Text
import androidx.compose.material3.TextField
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.unit.dp
import androidx.navigation.NavController
import com.google.gson.JsonObject
import com.matter.myapplication2.ui.setCurrentStatus
import com.matter.myapplication2.util.HttpApi
import com.matter.myapplication2.util.MyWebSocketListener
import com.matter.myapplication2.util.WebSocketClient
import com.matter.myapplication2.util.WebSocketResponseListener
import es.dmoral.toasty.Toasty
import kotlinx.coroutines.delay
import okhttp3.Call
import okhttp3.MediaType.Companion.toMediaTypeOrNull
import okhttp3.OkHttpClient
import okhttp3.Request
import okhttp3.RequestBody.Companion.toRequestBody
import okhttp3.Response
import okio.ByteString
import okio.IOException
import org.json.JSONObject
import java.util.UUID
import java.util.concurrent.TimeUnit


@SuppressLint("SuspiciousIndentation")

@Composable
fun ShareParing(
    modifier: Modifier=Modifier,
    navController: NavController
)
{
    val context= LocalContext.current
    var code by remember { mutableStateOf("") }
    var clicked by remember {
        mutableStateOf(false)
    }

    var check by remember {
        mutableStateOf(false)
    }
    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(horizontal = 20.dp),
        verticalArrangement = androidx.compose.foundation.layout.Arrangement.Center,
        horizontalAlignment = androidx.compose.ui.Alignment.CenterHorizontally
    ) {
        Text(text = "请输入配对码")
        Row (
            modifier = Modifier,
            verticalAlignment = androidx.compose.ui.Alignment.CenterVertically,
            horizontalArrangement = androidx.compose.foundation.layout.Arrangement.Center,
        ) {

            TextField(value = code, onValueChange ={
                code=it
            }, label = {
                Text(text = "配对码")
            } )
            Button(onClick = {clicked=true }) {
                Text(text = "确定")
            }

        }
    }


    if (check)
    {
        LaunchedEffect(key1 = Unit) {
            Toasty.success(context, "配对成功", Toasty.LENGTH_SHORT).show()

            delay(1000)

            navController.navigate("home")
            {
                popUpTo("home") {
                    inclusive = true
                }
            }
        }

    }
   
    if (clicked) {
        LaunchedEffect(key1 = Unit) {

            Toasty.success(context, "配对中", Toasty.LENGTH_SHORT).show()
            val TAG = "ShareParing"
            val WS_URL = HttpApi.WS_HOST.value

            val webSocketClient = WebSocketClient(WS_URL, MyWebSocketListener(object :
                WebSocketResponseListener {
                override fun onMessageReceived(message: String) {
                    Log.e(TAG, "Message received: $message")

                    if (message.contains("result")) {
                        setCurrentStatus("step3")
                        addDevice(message, code, "260", "123",check={
                            check=true
                        })
                    }


                }

                override fun onBytesReceived(bytes: ByteString) {
                    println("Bytes received: ${bytes.hex()}")
                }

            }, ::setCurrentStatus))

            try {
                webSocketClient.connect()
                val jsonMessage = JsonObject().apply {
                    addProperty("message_id", UUID.randomUUID().toString())
                    addProperty("command", APICommand.COMMISSION_WITH_CODE.value)

                    val args = JsonObject().apply {
                        addProperty("code", code)
                        addProperty("network_only", true)

                    }
                    add("args", args)
                }
                Log.e(TAG, jsonMessage.toString())
                webSocketClient.sendMessage(jsonMessage.toString())
            } catch (e: Exception) {
                Log.e(TAG, "WebSocketClient connect fail!")
                Log.e(TAG, e.toString())
                setCurrentStatus("failed")

            }
            clicked=false

            delay(5000)
//            navController.navigate("home")
//            {
//                popUpTo("home") {
//                    inclusive = true
//                }
//            }
        }
    }

}
fun addDevice(message: String, qrcode: String ,dT:String,vP:String,check:()->Unit) {
    setCurrentStatus("step4")
    Log.e("addDevice", message)
    val jsonObject = JSONObject(message)
    jsonObject.put("qrcode", qrcode)
    jsonObject.put("dT", dT)
    jsonObject.put("vP", vP)
    val resultObject = jsonObject.getJSONObject("result")
    resultObject.remove("attributes")
    resultObject.remove("attribute_subscriptions")

    Log.e("addDevice", jsonObject.toString())

    val client = OkHttpClient.Builder()
        .connectTimeout(30, TimeUnit.SECONDS)
        .readTimeout(30, TimeUnit.SECONDS)
        .writeTimeout(30, TimeUnit.SECONDS)
        .build()

    val jsonBody = jsonObject.toString()
    val requestBody = jsonBody.toRequestBody("application/json; charset=utf-8".toMediaTypeOrNull())
    val token = TokenManager.getToken()

    val request = Request.Builder()
        .url(HttpApi.ADD_DEVICE_API.value)
        .addHeader("Authorization", "$token")
        .post(requestBody)
        .build()

    client.newCall(request).enqueue(object : okhttp3.Callback {
        override fun onFailure(call: Call, e: IOException) {
            Log.e(WiFiProvisioningTool.TAG, "Request failed", e)
        }

        override fun onResponse(call: Call, response: Response) {
            response.use {
                if (!response.isSuccessful) throw IOException("Unexpected code $response")
                val responseBody = response.body?.string()
                Log.i(WiFiProvisioningTool.TAG, "Response: $responseBody")
                setCurrentStatus("step5")

                check()
            }
        }
    })
}