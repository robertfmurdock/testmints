# Test names as a verbal checksum

A test name is a brief rephrase of the behavior the body makes mechanically complete. It can suggest why the behavior matters when that purpose is recognizable through the subject interface.

> **RoB:** “the test name should be a brief and distinct rephrase of what the test code is doing”
>
> **RoB:** “the test code is a mechanically complete but still brief version of that test name.”
>
> **RoB:** “should ultimate be a sort of verbal checksum that the test code matches the goal (and that should kind of work in both directions)”
>
> **Codex:** “Read the name first and predict what evidence you should find. Read the body first and see whether you’d give it roughly the same name.”

The first read makes the name a prediction. The second makes the body an independent source for the name. Compare the distinguishing behavioral claim in both directions. Whether the chosen evidence would catch a plausible bug is a separate test design question.

## Local context and input order

> **RoB:** “i tend to prefer inputs than outputs in test naming as sort of a clue and precursor to refactoring similar scenarios together”
>
> **RoB:** “no need to use words on something that is always true in all scenarios and should be obvious to the reader.”
>
> **RoB:** “"prefer inputs than outputs" also means ordering, not just only include inputs.”
>
> **Codex:** “The test name spends its words on what varies, usually the input or starting state.”

Leading with the input helps readers compare cases even when the outcome also earns a place in the name. Omit shared information when the enclosing context already supplies it. Keep the details that let a reviewer distinguish neighboring cases.

In [StringDiffTest](../../../libraries/mindiff/src/commonTest/kotlin/com/zegreatrob/mindiff/StringDiffTest.kt), both `WhenStartsSameEndsDifferent` and `WhenMiddleSectionIsDifferent` contain `willIndicateWhereFirstDifferenceOccurs`. The enclosing class supplies the distinguishing input before the method names the outcome. Repeated method names reveal the shared checks and make the potential for a case table visible. In [AssertionsTest](../../../libraries/minassert/src/commonTest/kotlin/com/zegreatrob/minassert/AssertionsTest.kt), `assertNotEqualWorks` leaves both the equal inputs and the expected failure to the reader to discover.

## Candidate skill wording

- Read the name in its enclosing context first. What distinguishes this case, and what would you expect the body to show? Then read the body on its own terms. What would you call this case? Check that both readings point to the same distinguishing behavior. Follow the project’s naming conventions. Consider leading with the input or starting condition when it helps readers compare cases, and let the enclosing context carry what the cases share.
