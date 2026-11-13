---
name: testmints
description: Write, review, and refactor tests in projects that use Testmints, or help adopt Testmints when requested.
---

# Testmints

Use the phases to explain what a test arranges, which behavior it exercises, and how it proves that behavior. Follow the project's runner, assertion library, installed Testmints version, and test conventions. For Kotlin syntax and lifecycle details, read [the Kotlin reference](references/kotlin.md).

## Find the behavior and its owner

- Before rewriting a muddy scenario, map each meaningful assertion to the action it provides evidence for and the subject that owns that behavior. Look for actions hidden in setup or verification and for checkpoints that belong to different transitions.
- For each transition, ask what must already be true, what action changes it, and what observable result proves the change. Arrange and await earlier transitions in setup when they establish the starting state. If two transitions each have their own observable contract, give each its own scenario. Keep several observations of one action together. Keep sequences intact when the ordering or relationship itself is the behavior, such as replay or idempotence.
- Use the narrowest interface that still exercises each contract. A parent test can prove that a child is wired in while the child test proves its detailed behavior. Keep integrated evidence for behavior that depends on the interaction. Move assertions to existing tests where practical and preserve distinct evidence when consolidating.

For example, “times out a request and polls again” can be two scenarios. Start with a pending request, advance to its deadline in exercise, and verify the abort. In the recovery test, arrange a request whose timeout has already settled, advance to the next poll in exercise, and verify that a new request completes. Several effects of a single unmount, such as aborting the pending request and preventing later polling, can stay together.

For example, a test titled “generates a deterministic schema” used `readFile(...)` as its exercise on a prebuilt file. Move content assertions to a scenario that calls the serializer and inspects its output. For determinism, serialize inputs that differ along a dimension the serializer actually receives and compare the results. If delivery is a separate contract, exercise the build or publication path and check the delivered bytes against the expected output. A file read can collect evidence for either scenario.

## Make the phases tell the story

- **Setup** arranges fresh scenario data, collaborators, and resources. Define meaningful values once and carry them through the subject call into verification. Refer to them through their existing owner when practical. Keep a separate pre-action snapshot when the earlier state is part of the evidence.
- **Exercise** visibly calls the subject. Await its completion when it exposes a completion handle, and return the outcome that verification needs. Advancing fake time controls scheduled work and can leave unrelated I/O pending. For expected failure, capture the outcome of the specific call and make unexpected success fail the test.
- **Verify** asserts on observable results, state, or collaborator interactions from that execution. When the subject has no completion handle, use a bounded wait for a live condition. Keep subject calls outside retrying assertions.
- **Teardown** releases resources owned by the scenario. Pair acquisition and cleanup with the supported lifecycle mechanism. Keep shared mock, global, or timer restoration in existing runner hooks.

Prefer helpers that arrange a reusable world or name a stable assertion while leaving registration and the subject action visible. Use named, branch-free case tables for uniform input-to-outcome mappings. Keep fresh mutable collaborators in each case's setup.

## Name the behavior

Read the test name in its enclosing context and predict what the body should show. Then read the body on its own and check whether you’d give it roughly the same name.

A test name can state an observable rule that the body illustrates with a representative case. Leave literal details to the body unless they help distinguish the case.

Follow the project’s naming conventions. Consider leading with the input or starting condition when it helps readers compare cases. Let the enclosing context carry what the cases share.

## Check that the evidence can fail

- **Reviewing:** For tangled tests, describe the proposed scenarios by starting state, action, and observable evidence. Explain where the existing assertions would go. If a plausible broken implementation could still pass, describe it and recommend the smallest change that would make the test catch it.
- **Writing or changing tests:** For a regression, reproduce the known bad behavior at the closest meaningful interface before changing the implementation, then rerun the same test after the fix. For a new or materially changed contract, name a plausible broken implementation and check it against the prior behavior or a small reversible fault where practical. Confirm the intended observable assertion fails, restore any temporary fault, and rerun green.

Isolate the condition the test claims to prove. A throttle test must make the action otherwise eligible so that a separate eligibility rule cannot also suppress it. A late-result cancellation test must deliver the late result and observe what happens. If the real boundary is unavailable, record the limitation and the nearest representative evidence.
