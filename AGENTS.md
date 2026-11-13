# Test naming

Default to naming the distinguishing input or starting condition first, then the observable outcome. Prefer `whenExerciseReturnsNullItStillRunsVerify` for the case where a null result is what sets the test apart.

Read the name together with its enclosing class, suite, or case table. If that context already gives the condition, let the method name describe the outcome. Omit details common to neighboring cases when the context makes them clear. Lead with the outcome when no particular input or starting condition distinguishes the case, such as when checking that the exercise result is passed to verify.

## Adding test cases

Add a test case when it exposes a distinct way the implementation can violate the intended behavior and would require an implementation improvement to pass. A representative case can illustrate a simple rule. Avoid adding cases solely to increase coverage.
