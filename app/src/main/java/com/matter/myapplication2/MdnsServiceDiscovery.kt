import android.content.Context
import android.net.nsd.NsdManager
import android.net.nsd.NsdServiceInfo
import android.util.Log

class MdnsServiceDiscovery(context: Context) {

    private val nsdManager: NsdManager = context.getSystemService(Context.NSD_SERVICE) as NsdManager
    private val discoveryListener = createDiscoveryListener()
    private val TAG = "MdnsServiceDiscovery"
    private val SERVICE_TYPE_UDP = "_matterc._udp."
    private val SERVICE_TYPE_HTTP = "_http._tcp"  // 新增的服务类型

    private var dtValue: String? = null
    private var vpValue: String? = null

    fun discoverServices() {
        nsdManager.discoverServices(SERVICE_TYPE_UDP, NsdManager.PROTOCOL_DNS_SD, discoveryListener)
    }

    fun discoverHttpServices() {
        Log.d(TAG, "Starting HTTP service discovery with type $SERVICE_TYPE_HTTP")
        nsdManager.discoverServices(SERVICE_TYPE_HTTP, NsdManager.PROTOCOL_DNS_SD, discoveryListener)
    }

    fun stopDiscovery() {
        Log.d(TAG, "Stopping service discovery")
        try {
            nsdManager.stopServiceDiscovery(discoveryListener)
        } catch (e: IllegalArgumentException) {
            Log.e(TAG, "Failed to stop service discovery: ${e.message}")
        }
    }

    fun getDtValue(): String? {
        return dtValue
    }

    fun getVpValue(): String? {
        return vpValue
    }

    private fun createDiscoveryListener(): NsdManager.DiscoveryListener {
        return object : NsdManager.DiscoveryListener {

            override fun onDiscoveryStarted(regType: String) {
                Log.d(TAG, "Service discovery started")
            }

            override fun onServiceFound(serviceInfo: NsdServiceInfo) {
                Log.d(TAG, "Service found: $serviceInfo")
                nsdManager.resolveService(serviceInfo, object : NsdManager.ResolveListener {
                    override fun onResolveFailed(serviceInfo: NsdServiceInfo, errorCode: Int) {
                        Log.e(TAG, "Resolve failed: $errorCode")
                    }

                    override fun onServiceResolved(resolvedServiceInfo: NsdServiceInfo) {
                        Log.d(TAG, "Service resolved: $resolvedServiceInfo")
                        extractServiceDetails(resolvedServiceInfo)
                    }
                })
            }

            override fun onServiceLost(serviceInfo: NsdServiceInfo) {
                Log.e(TAG, "Service lost: $serviceInfo")
            }

            override fun onDiscoveryStopped(serviceType: String) {
                Log.i(TAG, "Discovery stopped: $serviceType")
            }

            override fun onStartDiscoveryFailed(serviceType: String, errorCode: Int) {
                Log.e(TAG, "Discovery failed: Error code:$errorCode")
                nsdManager.stopServiceDiscovery(this)
            }

            override fun onStopDiscoveryFailed(serviceType: String, errorCode: Int) {
                Log.e(TAG, "Stop discovery failed: Error code:$errorCode")
                nsdManager.stopServiceDiscovery(this)
            }
        }
    }

    private fun extractServiceDetails(serviceInfo: NsdServiceInfo) {
        Log.d(TAG, "${serviceInfo.serviceName}.${serviceInfo.serviceType}: type TXT, class IN")
        Log.d(TAG, "Name: ${serviceInfo.serviceName}.${serviceInfo.serviceType}")

        val attributes = serviceInfo.attributes
        for ((key, value) in attributes) {
            val valueStr = String(value)
            when (key) {
                "DT" -> {
                    dtValue = valueStr
                    Log.d(TAG, "TXT Length: ${value.size}")
                    Log.d(TAG, "TXT: $key=$valueStr")
                    Log.d(TAG, "Original Bytes: ${value.joinToString("") { "%02X".format(it) }}")
                }
                "VP" -> {
                    vpValue = valueStr
                    Log.d(TAG, "TXT Length: ${value.size}")
                    Log.d(TAG, "TXT: $key=$valueStr")
                    Log.d(TAG, "Original Bytes: ${value.joinToString("") { "%02X".format(it) }}")
                }
            }
        }
    }
}
