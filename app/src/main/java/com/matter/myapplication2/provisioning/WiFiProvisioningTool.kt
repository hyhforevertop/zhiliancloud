package com.matter.myapplication2.provisioning

import APICommand
import MdnsServiceDiscovery
import TokenManager
import android.app.AlertDialog
import android.bluetooth.BluetoothGatt
import android.content.Context
import android.util.Log
import android.widget.Toast
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import chip.devicecontroller.ChipDeviceController
import chip.devicecontroller.NetworkCredentials
import chip.devicecontroller.NetworkCredentials.WiFiCredentials
import chip.devicecontroller.OpenCommissioningCallback
import com.google.chip.chiptool.ChipClient
import com.google.gson.JsonObject
import com.matter.myapplication2.DeviceRepository.DeviceInfoStorage
import com.matter.myapplication2.QRcodeScan.CHIPDeviceInfo
import com.matter.myapplication2.R
import com.matter.myapplication2.bluetooth.BluetoothManager
import com.matter.myapplication2.util.DeviceIdUtil
import com.matter.myapplication2.util.HttpApi
import com.matter.myapplication2.util.MyWebSocketListener
import com.matter.myapplication2.util.WebSocketClient
import com.matter.myapplication2.util.WebSocketResponseListener
import es.dmoral.toasty.Toasty
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.launch
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

@ExperimentalCoroutinesApi
class WiFiProvisioningTool(private val context: Context, private val scope: CoroutineScope) {
  val mdnsServiceDiscovery = MdnsServiceDiscovery(context)
  private lateinit var deviceInfo: CHIPDeviceInfo
  private var gatt: BluetoothGatt? = null
  private lateinit var wiFiCredentials: WiFiCredentials
  private var deviceController: ChipDeviceController = ChipClient.getDeviceController(context)
  private var dialog: AlertDialog? = null

  private var _currentStatus by mutableStateOf("Idle")
  val currentStatus: String
    get() = _currentStatus

  private fun setCurrentStatus(status: String) {
    _currentStatus = status
    Log.d(TAG, "Current status: $status")
  }


  fun provisionDeviceWithWiFi() {

    wiFiCredentials = WiFiCredentials(TokenManager.getWifiName().toString(), TokenManager.getWifiPassword().toString()) //WiFiCredentials(TokenManager.getWifiName(),TokenManager.getWifiPassword().toString())
    deviceInfo = DeviceInfoStorage.getDeviceInfo(context)!!
    Log.e( "provisionDeviceWithWiFi", TokenManager.getWifiName().toString())
    Log.e( "provisionDeviceWithWiFi", TokenManager.getWifiPassword().toString())
    Log.e("provisionDeviceWithWiFi", deviceInfo.toString())
    Log.e("provisionDeviceWithWiFi", "${wiFiCredentials.ssid} ${wiFiCredentials.password}")
    startConnectingToDevice()

  }

  private fun setAttestationDelegate() {
    deviceController.setDeviceAttestationDelegate(DEVICE_ATTESTATION_FAILED_TIMEOUT) { devicePtr, _, errorCode ->
      Log.i(TAG, "Device attestation errorCode: $errorCode")

      if (errorCode == STATUS_PAIRING_SUCCESS.toLong()) {
        scope.launch { deviceController.continueCommissioning(devicePtr, true) }
        return@setDeviceAttestationDelegate
      }

      scope.launch {
        if (dialog != null && dialog?.isShowing == true) {
          Log.d(TAG, "dialog is already showing")
          return@launch
        }
        dialog = AlertDialog.Builder(context)
          .setPositiveButton("Continue") { _, _ ->
            deviceController.continueCommissioning(devicePtr, true)
          }
          .setNegativeButton("No") { _, _ ->
            deviceController.continueCommissioning(devicePtr, false)
          }
          .setTitle("Device Attestation")
          .setMessage("Device Attestation failed. Continue pairing?")
          .show()
      }
    }
  }

  private fun startConnectingToDevice() {
    if (gatt != null)
    { Log.e("HYH", "gatt is not null")
      return
    }

    scope.launch {
      Log.e("HYH", "startConnectingToDevice")
      val bluetoothManager = BluetoothManager()
      val device = bluetoothManager.getBluetoothDevice(
        context,
        deviceInfo.discriminator,
        deviceInfo.isShortDiscriminator
      ) ?: run {
        showMessage(R.string.rendezvous_over_ble_scanning_failed_text)
        return@launch
      }

      gatt = bluetoothManager.connect(context, device)
      Toasty.success(context, "Pairing", Toast.LENGTH_SHORT).show()
      deviceController.setCompletionListener(ConnectionCallback())

      val deviceId = DeviceIdUtil.getNextAvailableId(context)
      val connId = bluetoothManager.connectionId
      val network = NetworkCredentials.forWiFi(wiFiCredentials)

      setAttestationDelegate()

      deviceController.pairDevice(gatt, connId, deviceId,  deviceInfo.setupPinCode, network)
      DeviceIdUtil.setNextAvailableId(context, deviceId + 1)

      setCurrentStatus("step1")
    }
  }



  private fun showMessage(msgResId: Int, stringArgs: String? = null) {
    val msg = context.getString(msgResId, stringArgs)
    Log.i(TAG, "showMessage:$msg")
    Toast.makeText(context, msg, Toast.LENGTH_SHORT).show()
  }



  inner class ConnectionCallback : GenericChipDeviceListener() {
    override fun onConnectDeviceComplete() {
      Log.d(TAG, "onConnectDeviceComplete")
    }

    override fun onStatusUpdate(status: Int) {
      Log.d(TAG, "Pairing status update: $status")

    }



    override fun onCommissioningComplete(nodeId: Long, errorCode: Long) {
      if (errorCode == STATUS_PAIRING_SUCCESS.toLong()) {
        val lastDevice = DeviceIdUtil.getLastDeviceId(context).toString(10).toLong()
        val deviceIdList = DeviceIdUtil.getCommissionedNodeId(context)
        Log.e("HYH", "$lastDevice, $deviceIdList")

        scope.launch {
          enhancedCommission()
        }
      } else {
        Log.e("onCommissioningComplete", "onCommissioningComplete failed")
      }
      mdnsServiceDiscovery.stopDiscovery()
      DeviceIdUtil.setCommissionedNodeId( context, nodeId)
      ChipClient.getDeviceController(context).close()
    }


    private suspend fun enhancedCommission() {
      val lastDevice = DeviceIdUtil.getLastDeviceId(context).toString(10).toLong()
      val TAG = "EnhancedCommission"
      val testDuration = 180
      val testIteration = 1000
      setCurrentStatus("step2")
      val devicePointer = try {
        ChipClient.getConnectedDevicePointer(context, lastDevice)
      } catch (e: IllegalStateException) {
        Log.d(TAG, "getConnectedDevicePointer exception", e)
        Log.e(TAG, "Get DevicePointer fail!")
        return
      }
      val setupPinCode = (12344523..99999999).random().toLong()//"20202021".toULong().toLong()
      val discriminator = deviceInfo.discriminator.toInt()
      deviceController.openPairingWindowWithPINCallback(
        devicePointer,
        testDuration,
        testIteration.toLong(),
        discriminator,
        setupPinCode,
        object : OpenCommissioningCallback {
          override fun onError(status: Int, deviceId: Long) {
            Log.e(TAG, "OpenCommissioning Fail! \nDevice ID : $deviceId\nErrorCode : $status  ")
          }

          override fun onSuccess(deviceId: Long, manualPairingCode: String?, qrCode: String?) {


            mdnsServiceDiscovery.discoverServices()
            Thread.sleep(5000)
            val dtValue=mdnsServiceDiscovery.getDtValue().toString()
            val vpValue=mdnsServiceDiscovery.getVpValue().toString()


            val WS_URL = HttpApi.WS_HOST.value

            val webSocketClient = WebSocketClient(WS_URL, MyWebSocketListener(object :
              WebSocketResponseListener {
              override fun onMessageReceived(message: String) {
                if (message.contains("result")) {
                  setCurrentStatus("step3")
                  addDevice(message, manualPairingCode.toString(),dtValue,vpValue)
                }

              }

              override fun onBytesReceived(bytes: ByteString) {
                println("Bytes received: ${bytes.hex()}")
              }

            }, ::setCurrentStatus))

            Log.e(TAG, "OpenCommissioning Success! \n Node ID: $deviceId\n\tManual : $manualPairingCode\n\tQRCode : $qrCode \n\t Iteration $testIteration")
            try {
              webSocketClient.connect()
              val jsonMessage = JsonObject().apply {
                addProperty("message_id", UUID.randomUUID().toString())
                addProperty("command", APICommand.COMMISSION_WITH_CODE.value)

                val args = JsonObject().apply {
                  addProperty("code", manualPairingCode.toString())
                  addProperty("network_only",true)
                }
                add("args", args)
              }
              Log.e(TAG, jsonMessage.toString())
              webSocketClient.sendMessage(jsonMessage.toString())
            }catch (e:Exception)
            {
              Log.e(TAG, "WebSocketClient connect fail!")
              Log.e(TAG, e.toString())
              setCurrentStatus("failed")

            }
          }
        }
      )
    }

    private fun addDevice(message: String, qrcode: String ,dT:String,vP:String) {
      setCurrentStatus("step4")

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
          Log.e(TAG, "Request failed", e)
        }

        override fun onResponse(call: Call, response: Response) {
          response.use {
            if (!response.isSuccessful) throw IOException("Unexpected code $response")
            val responseBody = response.body?.string()
            Log.i(TAG, "Response: $responseBody")
            setCurrentStatus("step5")
          }
        }
      })
    }
  }

  companion object {
    private const val TAG = "WiFiProvisioningTool"
    private const val DEVICE_ATTESTATION_FAILED_TIMEOUT = 30
    private const val STATUS_PAIRING_SUCCESS = 0
  }
}
