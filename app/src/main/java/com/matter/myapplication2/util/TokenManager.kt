import android.content.Context
import android.content.SharedPreferences

object TokenManager {
    private const val PREF_NAME = "app_prefs"
    private const val KEY_TOKEN = "token"
    private const val KEY_USERNAME = "username"
    private const val KEY_PASSWORD = "password"
    private const val HOST_IPV4 ="HOST_IPV4"
    private const val MATTER_IPV4 ="MATTER_IPV4"
    private const val WIFI_PASSWORD ="WIFI_PASSWORD"
    private const val WIFI_NAME="WIFI_NAME"
    private const val PAIRING_SELECTION="PAIRING_SELECTION"
    private const val QRCODE_INFO="QRCODE_INFO"
    private var sharedPreferences: SharedPreferences? = null

    // 初始化 SharedPreferences
    fun init(context: Context) {
        sharedPreferences = context.getSharedPreferences(PREF_NAME, Context.MODE_PRIVATE)
    }

    fun getQrcodeInfo(): String? {
        return sharedPreferences?.getString(QRCODE_INFO, null)
    }
    fun setQrcodeInfo(info: String) {
        val editor = sharedPreferences?.edit()
        editor?.putString(QRCODE_INFO, info)
        editor?.apply()
    }
    fun clearQrcodeInfo() {
        val editor = sharedPreferences?.edit()
        editor?.remove(QRCODE_INFO)
        editor?.apply()
    }
    fun getPairingSelection(): String? {
        return sharedPreferences?.getString(PAIRING_SELECTION, null)
    }

    fun setPairingSelection(selection: String) {
        val editor = sharedPreferences?.edit()
        editor?.putString(PAIRING_SELECTION, selection)
        editor?.apply()
    }
    fun clearPairingSelection() {
        val editor = sharedPreferences?.edit()
        editor?.remove(PAIRING_SELECTION)
        editor?.apply()
    }
    fun getMatterIpv4(): String? {
        return sharedPreferences?.getString(MATTER_IPV4, null)
    }
    fun setMatterIpv4(ipv4: String) {
        val editor = sharedPreferences?.edit()
        editor?.putString(MATTER_IPV4, ipv4)
        editor?.apply()
    }

    fun clearMatterIpv4() {
         val editor = sharedPreferences?.edit()
        editor?.remove(MATTER_IPV4)
        editor?.apply()
    }


    fun  getHostIpv4(): String? {
        return sharedPreferences?.getString(HOST_IPV4, null)
    }
    fun setHostIpv4(ipv4: String) {
        val editor = sharedPreferences?.edit()
        editor?.putString(HOST_IPV4, ipv4)
        editor?.apply()
    }

    fun clearHostIpv4() {
        val editor = sharedPreferences?.edit()
        editor?.remove(HOST_IPV4)
        editor?.apply()
    }


    // 获取保存的 token
    fun getToken(): String? {
        return sharedPreferences?.getString(KEY_TOKEN, null)
    }

    // 设置 token
    fun setToken(token: String) {
        val editor = sharedPreferences?.edit()
        editor?.putString(KEY_TOKEN, token)
        editor?.apply()
    }


    // 清除保存的 token
    fun clearToken() {
        val editor = sharedPreferences?.edit()
        editor?.remove(KEY_TOKEN)
        editor?.apply()
    }
    // 获取保存的用户名
    fun getUsername(): String? {
        return sharedPreferences?.getString(KEY_USERNAME, null)
    }

    // 设置用户名
    fun setUsername(username: String) {
        val editor = sharedPreferences?.edit()
        editor?.putString(KEY_USERNAME, username)
        editor?.apply()
    }

    // 获取保存的密码
    fun getPassword(): String? {
        return sharedPreferences?.getString(KEY_PASSWORD, null)
    }

    // 设置密码
    fun setPassword(password: String) {
        val editor = sharedPreferences?.edit()
        editor?.putString(KEY_PASSWORD, password)
        editor?.apply()
    }

    // 清除用户名和密码
    fun clearCredentials() {
        val editor = sharedPreferences?.edit()
        editor?.remove(KEY_USERNAME)
        editor?.remove(KEY_PASSWORD)
        editor?.apply()
    }

    fun getWifiPassword(): String? {
        return sharedPreferences?.getString(WIFI_PASSWORD, null)
    }
    fun setWifiPassword(password: String) {
        val editor = sharedPreferences?.edit()
        editor?.putString(WIFI_PASSWORD, password)
        editor?.apply()
    }

    fun getWifiName(): String? {
        return sharedPreferences?.getString(WIFI_NAME, null)
    }
    fun setWifiName(name: String) {
        val editor = sharedPreferences?.edit()
        editor?.putString(WIFI_NAME, name)
        editor?.apply()
    }
}
