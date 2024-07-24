package com.matter.myapplication2.QRcodeScan

import android.os.Parcelable
import kotlinx.parcelize.Parcelize
import matter.onboardingpayload.DiscoveryCapability
import matter.onboardingpayload.OnboardingPayload

@Parcelize
data class CHIPDeviceInfo(
    val version: Int = 0,
    val vendorId: Int = 0,
    val productId: Int = 0,
    val discriminator: Int = 0,
    val setupPinCode: Long = 0L,
    var commissioningFlow: Int = 0,
    val optionalQrCodeInfoMap: Map<Int, QrCodeInfo> = mapOf(),
    val discoveryCapabilities: MutableSet<DiscoveryCapability> = mutableSetOf(),
    val isShortDiscriminator: Boolean = false,
    val ipAddress: String? = null,
    val port: Int = 5540
) : Parcelable {

    companion object {
        fun fromSetupPayload(setupPayload: OnboardingPayload): CHIPDeviceInfo {
            return CHIPDeviceInfo(
                setupPayload.version,
                setupPayload.vendorId,
                setupPayload.productId,
                setupPayload.getLongDiscriminatorValue(),
                setupPayload.setupPinCode,
                setupPayload.commissioningFlow,
                setupPayload.optionalQRCodeInfo.mapValues { (_, info) ->
                    QrCodeInfo(info.tag, info.type, info.data, info.int32)
                },
                setupPayload.discoveryCapabilities,
                setupPayload.hasShortDiscriminator
            )
        }
    }
}