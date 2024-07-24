package com.matter.myapplication2.util

val HOST_IPV4= TokenManager.getHostIpv4().toString()//"192.168.98.191"//
val MATTER_SERVER_IPV4=TokenManager.getMatterIpv4().toString() //"192.168.98.191"//
val HOST= "http://$HOST_IPV4:8080"
val MATTER_SERVER= "ws://$MATTER_SERVER_IPV4:5580/ws"
enum class HttpApi(var value: String) {
    BASE_URL(HOST),
    LOGIN_API("${HOST}/api/login"),
    WS_HOST(MATTER_SERVER),
    ADD_DEVICE_API("${HOST}/device/add"),
    LIGHT_OPERATION("${HOST}/device/lightOperation"),
    LIGHT_ADJUST("${HOST}/device/adjustLight"),
    BRIDGE_QRCODE("${HOST}/device/generateQrcode")
}
