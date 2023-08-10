package com.zegreatrob.testmints.logs

import io.github.oshai.kotlinlogging.DefaultMessageFormatter
import io.github.oshai.kotlinlogging.Formatter
import io.github.oshai.kotlinlogging.KLoggingEvent
import io.github.oshai.kotlinlogging.KotlinLoggingConfiguration

actual fun platformDefaults() {
    if(KotlinLoggingConfiguration.formatter is DefaultMessageFormatter) {
        KotlinLoggingConfiguration.formatter = DefaultWithParamsFormatter()
    }
}

class DefaultWithParamsFormatter : Formatter {
    private val defaultMessageFormatter = DefaultMessageFormatter()
    override fun formatMessage(loggingEvent: KLoggingEvent): String = listOfNotNull(
        defaultMessageFormatter.formatMessage(loggingEvent).trim(),
        loggingEvent.payload?.toString()?.trim(),
    ).joinToString(" ")
}
