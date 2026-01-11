package com.zegreatrob.testmints.plugins

import nl.littlerobots.vcu.plugin.versionSelector

plugins {
    id("nl.littlerobots.version-catalog-update")
}

repositories {
    mavenCentral()
}

versionCatalogUpdate {
    val rejectRegex = "^[0-9.]+[0-9](-RC|-M[0-9]*|-RC[0-9]*.*|-beta.*|-Beta.*|-alpha.*)$".toRegex()
    versionSelector { versionCandidate ->
        !rejectRegex.matches(versionCandidate.candidate.version)
    }
}
