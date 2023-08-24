# Mint Logs Plugin

Hey! If you're here, you're probably trying to maximize the value of your tests.

That's good. We like people like you.

For you, we've built the `mint-logs-plugin`.

When applying this plugin to your Gradle project, it will automatically add hooks to the test runner that will log out information as the testmint stage changes.

Expected output will look something like:


`
INFO: [testmints] setup-start {step=setup, state=start, name=com.zegreatrob.testmints.logs.LoggingExampleTest.manualLoggingTester}
setup work happens here
[info] INFO: [testmints] setup-finish {step=setup, state=finish}
[info] INFO: [testmints] exercise-start {step=exercise, state=start}
exercise work happens here
[info] INFO: [testmints] exercise-finish {step=exercise, state=finish}
[info] INFO: [testmints] verify-start {step=verify, state=start, payload=kotlin.Unit}
verify work happens here
[info] INFO: [testmints] verify-finish {step=verify, state=finish}
`

The plugin is built on the [kotlin-logging](https://github.com/oshai/kotlin-logging) library, and as such is configurable by SLF4J, so you can format it however you like.

At this time, only JUnit and Mocha are supported (on JVM and JS, respectively). More to come if there's interest or contributions!

`
    plugins {
        kotlin("multiplatform") version "1.9.10"
        id("com.zegreatrob.testmints.logs.mint-logs") version "10.2.2"
    }
`