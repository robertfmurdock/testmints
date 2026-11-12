# Automatic setup context cleanup

## Objective

Close per-test setup contexts that implement Kotlin's multiplatform `AutoCloseable`, in both standard and async mints. The JVM mapping also supports Java's `AutoCloseable` and `Closeable`.

## Decisions

- Close once per invocation after explicit teardown when it runs, before template teardown. Start protecting the context as soon as its provider returns.
- Preserve existing phase behavior and report a close failure alongside any other failure.
- Leave the shared template context under template ownership, including when it is returned unchanged as the test context. This protects cached `beforeAll` resources as well as shared contexts with their own teardown.
- Use the standard interface. Contexts with a `dispose()` API can delegate to it from `close()`. Explicit manual closes receive no special handling.

## Test passes

- Before implementation, the JVM tests in both modules failed on missing close calls and missing combined failure reporting. The shared-context ownership tests passed.
- The first implementation pass exposed a test assumption about exception identity across the coroutine runner. The async setup-failure test now checks the reported message and the close count.
- A second review added explicit teardown failure coverage. The full standard and async suites passed on JVM, Node.js, Wasm/Node.js, and macOS ARM64. Other Native targets were outside the runtime test matrix.

## Shared setup extension

- Built-in shared templates now close the context returned by `sharedSetup` after `sharedTeardown`, even if teardown throws. Close failures are reported alongside test and teardown failures.
- The template that acquires a context owns its close. Nested templates passing through a parent's context leave it open for the parent. Cached `beforeAll` values and their merge path keep their existing lifecycle.
- The ordering test failed before implementation in both JVM suites. After adding coverage for simultaneous shared teardown and close failures, the full suites passed on JVM, Node.js, Wasm/Node.js, and macOS ARM64.

## Merged contexts and suite cleanup

- A fresh closeable result of `mergeContext` now closes after each invocation. A result that is the parent or cached context retains that context's lifetime.
- Suite-end cleanup for `beforeAll` is deferred pending an investigation into `afterAll` support. Custom wrappers own their resources and can use `use` or `try/finally`.
- Added tests for repeated merged-context cleanup and for a cached context used directly. Both modules passed on JVM, Node.js, Wasm/Node.js, and macOS ARM64.

## Test-first follow-up

- Describe new behavior in a Testmints test before changing production code. For changed behavior, run the test against the prior implementation and check the expected failure before implementing it.
- Added standard and async tests for returning the parent from `mergeContext` and for a custom wrapper closing its context when exercise fails.

- Strengthened merged-context evidence with an ID observed in exercise and close for each invocation. Both JVM tests rejected repeated IDs. Compound-failure tests now assert the full exception map, including phase descriptions.
