package com.zegreatrob.testmints.report

import kotlin.native.concurrent.ThreadLocal

@ThreadLocal
object MintReporterConfig : ReporterProvider {
    override var reporter: MintReporter = PlaceholderMintReporter
}
