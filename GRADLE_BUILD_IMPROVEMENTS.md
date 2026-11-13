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
| 5a | Pending | Verify both plugin modules compile generated templates from a clean tree with inferred task dependencies. | — |
| 5b | Pending | Review dashboard producer and consumer APIs. Use outgoing artifacts if supported and run `./gradlew dashboardTestSummary` to check all four inputs. Record any deferral. | — |
| 6a | Pending | Centralize repositories in each build's settings and compare dependency resolution before and after. | — |
| 6b | Pending | Extract shared plugin conventions and verify functional tests, report collection, and generated POMs. | — |
| 6c | Pending | Remove redundant dependencies and simplify catalog aliases. Compare dependency resolution and preserve the isolated ktlint toolchain. | — |
| 7a | Pending | Pin the action processor's Java toolchain. Verify its compilation uses Java 21. | — |
| 7b | Pending | Set explicit build names, task metadata, and UTF-8. Verify included task paths, task help, and publishing configuration. | — |
| 7c | Pending | Switch the wrapper to the binary distribution. Verify the published distribution checksum and wrapper JAR, then run the commit gate through the new wrapper. | — |
