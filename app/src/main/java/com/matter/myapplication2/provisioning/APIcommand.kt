import com.google.gson.Gson

data class CommandMessage(
    val message_id: String,
    val command: String,
    val args: Map<String, Any>? = null
)

enum class APICommand(val value: String) {
    START_LISTENING("start_listening"),
    SERVER_DIAGNOSTICS("diagnostics"),
    SERVER_INFO("server_info"),
    GET_NODES("get_nodes"),
    GET_NODE("get_node"),
    COMMISSION_WITH_CODE("commission_with_code"),
    COMMISSION_ON_NETWORK("commission_on_network"),
    SET_WIFI_CREDENTIALS("set_wifi_credentials"),
    SET_THREAD_DATASET("set_thread_dataset"),
    OPEN_COMMISSIONING_WINDOW("open_commissioning_window"),
    DISCOVER("discover"),
    INTERVIEW_NODE("interview_node"),
    DEVICE_COMMAND("device_command"),
    REMOVE_NODE("remove_node"),
    GET_VENDOR_NAMES("get_vendor_names"),
    READ_ATTRIBUTE("read_attribute"),
    WRITE_ATTRIBUTE("write_attribute"),
    PING_NODE("ping_node"),
    GET_NODE_IP_ADDRESSES("get_node_ip_addresses"),
    IMPORT_TEST_NODE("import_test_node")
}
