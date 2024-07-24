package com.matter.myapplication2.QRcodeScan

import android.os.Parcelable
import kotlinx.parcelize.Parcelize
import matter.onboardingpayload.OptionalQRCodeInfoType


@Parcelize
data class QrCodeInfo(
    val tag: Int,
    val type: OptionalQRCodeInfoType,
    val data: String?,
    val intDataValue: Int
) : Parcelable