# Lifecycle hardening log

Review date: 2026-09-09  
External proposal: unavailable in this checkout; implementation followed the lifecycle-hardening handoff plan supplied with this task.

## Decisions

| Item | Decision | Notes |
| --- | --- | --- |
| Failure-safe supplied shared-template teardown | Accepted | Standard and async supplied setup/teardown overloads now unwind only after setup has returned a context. |
| Async exercise job settlement before `exerciseFinish` | Accepted | The existing direct-child snapshot contract remains unchanged. |
| New public cleanup API / attempt-owned cleanup | Deferred | Requires a public API and a partial-setup ownership model. |
| Reporter exception isolation policy | Deferred | Needs an observable diagnostics contract. |
| Lifecycle event interface changes | Deferred | Outside the safe compatibility scope. |
| Broader coroutine ownership redesign | Deferred | `GlobalScope` internals and supported-target semantics need a dedicated spike. |

## Characterization baseline

Added the focused lifecycle regressions, then ran:

```
./gradlew :libraries:standard:jvmTest :libraries:async:jvmTest
```

Before the production changes, the compiled baseline reported these expected failures:

| Regression | Baseline result |
| --- | --- |
| Standard supplied nested templates unwind after an escaping exercise exception | Failed: both established shared teardowns were skipped. |
| Standard template cleanup does not mask an escaping exercise exception | Failed: cleanup replaced the exercise exception. |
| Async `exerciseFinish` waits for a tracked `exerciseScope` job | Failed: `exerciseFinish` observed the job as unsettled. |

The async supplied-template ordering regression passed for its ordinary caught exercise failure because async execution already captures that failure before the wrapper returns. The wrappers were nevertheless changed so that any escaping body failure gets the same protected unwind behavior.

## Implementation

- Added internal supplied-template runners for the standard and async built-in shared setup/teardown overloads. They run teardown only after setup establishes a context, unwind nested templates in reverse order, and combine simultaneous body and template-cleanup errors with `CompoundMintTestException` while keeping the body failure first.
- Left custom wrapper overloads unchanged and caller-owned.
- Moved async `exerciseFinish` until after the current `ScopeMint.exerciseScope` child-job snapshot settles, including when the exercise callback throws. Verification and teardown therefore follow the same boundary.

## Final validation

```
./gradlew :libraries:standard:jvmTest :libraries:async:jvmTest
```

Result: **BUILD SUCCESSFUL** (2026-09-09). Focused coverage includes nested teardown ordering, primary-failure preservation, reporter ordering, tracked-job settlement before `exerciseFinish`, and tracked-job settlement before teardown after an exercise failure.

```
./gradlew check
```

Result: **BUILD SUCCESSFUL** (2026-09-09; 598 actionable tasks).
