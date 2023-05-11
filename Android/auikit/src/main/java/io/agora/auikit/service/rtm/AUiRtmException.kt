package io.agora.auikit.service.rtm

class AUiRtmException(
    val code: Int,
    val reason: String,
    val operation: String
) : Throwable(reason)