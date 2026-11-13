# Gradle build improvements

Status values are Pending, In progress, Blocked, and Verified. A Verified
entry records the checks for the commit that contains it.

## Commit gate

Use Java 21. Run the focused checks for the checkpoint, then run the root
composite build from clean outputs. The two Apple compilation exclusions
match the main CI build.

```sh
./gradlew clean --no-build-cache --no-configuration-cache
./gradlew assemble check --no-build-cache --no-configuration-cache \
  -x :plugins-test:mint-action-test:compileTestKotlinIosArm64 \
  -x :plugins-test:mint-action-test:compileTestKotlinMacosArm64
git diff --check
```

Record the Java and Gradle versions, any `-Pversion`, and the results.
Prefix each commit subject with a SemVer marker based on its release impact.
Use `[none]` for documentation and internal build changes that leave
published behavior unchanged, `[patch]` for backward-compatible fixes,
`[minor]` for backward-compatible features, and `[major]` for breaking
changes. For example, `[none] Give each project one test report collection task`.

Commit the verified change with its tracker update and confirm a clean
worktree before continuing. If a gate fails, record the blocker and leave
the checkpoint open. Publishing verification uses local artifacts and dry
runs. Commits stay local.

## Checkpoints

| ID | Status | Acceptance and focused verification | Result or follow-up |
|---|---|---|---|
| 0 | Verified | Run the commit gate before changing build logic and commit this tracker. | Java 21.0.1 (Corretto), Gradle 9.9.1, no version override. Clean passed in 42s. Assemble and check passed in 4m 30s (858 tasks). |
| 1 | Verified | Give each project report directory one owner. Run `./gradlew collectResults` after the full build and verify reports and test results under the existing CI artifact paths. | Java 21.0.1, Gradle 9.9.1, no version override. Clean passed in 29s, assemble and check in 7m 2s, and collection in 5s. Found 116 test XML files and 357 HTML indexes under the existing artifact root. |
| 2 | Verified | Resolve the Mint Logs hooks archive lazily and wire its extracted output into the JS consumers. Run `./gradlew :plugins:mint-logs-plugin:functionalTest` and confirm the Node and browser cases. | Java 21.0.1, Gradle 9.9.1, no version override. All five functional tests passed. Clean passed in 9s, and assemble and check passed in 3m 33s. |
| 3a | Verified | Configure consumer plugins through plugin and target callbacks. Run the Mint Logs functional suite and the `plugins-test` checks. | Java 21.0.1, Gradle 9.9.1, no version override. Five Mint Logs functional tests passed. Clean passed in 7s, and assemble and check (including plugins-test) passed in 3m 5s. |
| 3b | Verified | Configure POM metadata lazily. Run the representative Maven publication POM generation tasks locally and inspect the generated metadata. | Java 21.0.1, Gradle 9.9.1, no version override. The JVM library, BOM, and both Gradle plugin POMs matched byte-for-byte. Clean passed in 17s, and assemble and check passed in 4m 3s. |
| 4 | Pending | Remove the temporary snapshot pause after November 15, 2026. Dry-run `publish` for snapshot and release versions and inspect the selected tasks. | Date gate remains in effect through November 15. |
| 5a | Verified | Verify both plugin modules compile generated templates from a clean tree with inferred task dependencies. | Both template-copy tasks ran before compilation. Java 21.0.1, Gradle 9.9.1, no version override. Clean and full build passed on retry after the first run found a Java 22 class in convention-plugin validation. |
| 5b | Verified | Review dashboard producer and consumer APIs. Use outgoing artifacts if supported and run `./gradlew dashboardTestSummary` to check all four inputs. Record any deferral. | Four outgoing count artifacts resolved via composite substitution. Standalone summary and clean full build both passed on Java 21.0.1 and Gradle 9.9.1 with no version override. Both summaries matched the 716-test baseline byte-for-byte. |
| 6a | Verified | Centralize repositories in each build's settings and compare dependency resolution before and after. | Representative library and plugin runtime dependency trees matched. Clean and full build passed on Java 21.0.1 and Gradle 9.9.1 with no version override. |
| 6b | Verified | Extract shared plugin conventions and verify functional tests, report collection, and generated POMs. | Java 21.0.1, Gradle 9.9.1, no version override. Three representative POMs matched byte-for-byte. Clean full build including functional tests and subsequent report collection passed. |
| 6c | Verified | Remove redundant dependencies and simplify catalog aliases. Compare dependency resolution and preserve the isolated ktlint toolchain. | Representative JVM runtime and ktlint dependency coordinates matched. Clean and full build passed on Java 21.0.1 and Gradle 9.9.1 with no version override. |
| 7a | Verified | Pin the action processor's Java toolchain. Verify its compilation uses Java 21. | Generated class files report version 65.0. Clean full build passed in 4m 13s on Java 21.0.1 and Gradle 9.9.1 with no version override. |
| 7b | Verified | Set explicit build names, task metadata, and UTF-8. Verify included task paths, task help, and publishing configuration. | Root task help and included plugin and library POM generation with the mac release target passed. Clean full build passed on Java 21.0.1 and Gradle 9.9.1 with no version override. |
| 7c | Verified | Switch the wrapper to the binary distribution. Verify the published distribution checksum and wrapper JAR, then run the commit gate through the new wrapper. | Gradle 9.9.1 binary distribution SHA-256 pinned and wrapper JAR matched the published checksum. Both wrapper runs, task listing, and clean full build passed on Java 21.0.1 with no version override. Wrapper update workflow now requests the binary distribution. |
| 8 | Verified | Validate the wrapper JAR and the exact distribution SHA-256 in the wrapper update workflow before it opens an automerge-eligible PR. Verify that the workflow rejects a mismatched checksum. | Gradle's wrapper-validation action checks the regenerated JAR in CI. The distribution check accepted the official pin and rejected an altered pin locally. Clean and composite assemble/check passed on Java 21.0.1 and Gradle 9.9.1 with no version override. |
| 9 | Verified | Run the main CI assemble and check command twice on Java 21 with the CI version override and Apple target exclusions. Confirm the build passes and the second run reuses the configuration cache before removing the build step's opt-out. Check the release task graphs separately before changing their opt-outs. | Java 21.0.1, Gradle 9.9.1, and -Pversion=12.7.3-SNAPSHOT. Clean passed in 4s. The first assemble/check passed in 4m 29s and stored the cache. The second passed in 2s and reused it. Build cache was disabled for both runs. Removed the main build opt-out. Release opt-outs are retained pending separate verification. The commit gate without a version override passed with clean in 9s and assemble/check in 2m 53s. |
| 10 | Pending | Wire the Karma configuration directory and the plugin build's collected report directories from their producing tasks into their consumers. Verify the Mint Logs browser functional test and collectResults, including the existing CI artifact paths. | The consumer paths and explicit task dependencies are currently configured separately. |
| 11 | Verified | Remove the convention build's explicit Kotlin standard-library declaration if the kotlin-dsl plugin supplies it. Compare the resolved runtime classpath before and after and run the convention build checks. | All 25 resolved runtime module versions matched, including Kotlin stdlib 2.4.21. Convention build checks, clean, and composite assemble/check passed on Java 21.0.1 and Gradle 9.9.1 with no version override. |
| 12 | Pending | Check for a supported Kotlin/JS hook for wiring Mint Logs into the test sync task. Verify whether the action plugin fixture's lint and format tasks still need to schedule KSP when generated sources are excluded. | Verify any changes with the Mint Logs Node functional tests and the fixture's lint and format tasks from clean outputs. |
