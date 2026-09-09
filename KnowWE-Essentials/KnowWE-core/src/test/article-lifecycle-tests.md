# Article lifecycle regression tests

These tests cover the working-tree construction/publication change. Constructing an article must not affect the live
version; publication still happens when queueing, not at commit. See the [implementation and follow-up plan](../../article-lifecycle-plan.md).

The ID contract was revised with the user on 8 September: unchanged normalized content keeps its namespace on a
recompile, but a content change invalidates **all** IDs. Tests expecting partial-content ID preservation or reuse of a
deleted article's old 32-bit collision slot were deliberately updated for that contract, not disabled to hide failures.
Reservations have been removed. Equivalent early ID requests, private drafts, owner-safe cleanup and rollback
reactivation are now tested actively.

## Baseline

Working-tree results on 9 September 2026, on repository baseline `c55d55ee3`:

| Test class | Passing | Ignored |
| --- | ---: | ---: |
| `ArticleLifecycleTest` | 26 | 0 |
| `ArticleCompilationLifecycleTest` | 10 | 1 ignored |
| `ArticleRollbackLifecycleTest` | 5 | 1 ignored |
| `SectionIndexTest` | 4 | 0 |
| Existing core tests | 38 | 0 |
| Total | 83 | 2 ignored |

All core test classes are run directly through JUnit (83 executed, 85 including ignored methods). The tests with
intentional parser exceptions log those exceptions; these are expected. Before the production change, 56 tests passed
and 14 were ignored after their failures had been verified as assertions rather than initialization/timeouts.

The two remaining disabled contracts concern pre-existing manager bookkeeping and remain outside this change:

- A newly created and replaced article being reported to the compiler as an original removal.
- Rollback incorrectly retaining an intermediate newly created article.

Restoring the original Article now also reactivates its lifecycle and republishes its IDs/diagnostics; those two
rollback tests are active. Full transaction repair is not implied by this localized restoration.

Passing cases include unchanged recompiles, parser-time IDs, normalization, complete invalidation on edits, independent
namespaces for formerly colliding titles, distinct empty siblings, changed parser types and text ranges, deletion/recreation and editing
back to old text, stale-recompile rejection, private diagnostics and late retired writes, visibility before commit,
reentrant events, actual AttachmentManager full-parse/deduplication/shared-reference removal, and the synthetic
clear/restore contribution sequence. The contribution ledger is not a test of d3web/ontology semantics.

The adjacent-module reactor also runs `RecompileActionTest` (1 test) and `HeaderExporterTest` (2 tests) against the
modified core. The latter protects the Word bookmark adapter from parsing opaque IDs as longs; it is not an end-to-end
Word-rendering test. OntologyBridge/CI lifecycle integration and the private headless test remain follow-up work as
explicitly recorded in the plan. No browser focus/update notification changes have been made.

## Running

From the KnowWE repository root:

```bash
mvn -o -pl KnowWE-Essentials/KnowWE-core -am process-test-classes
cd KnowWE-Essentials/KnowWE-core
lifecycle_classpath=$(tr ';' ':' < target/dependencies/output.txt)
java -ea -cp "target/classes:target/test-classes:$lifecycle_classpath" org.junit.runner.JUnitCore \
  de.knowwe.core.ArticleLifecycleTest \
  de.knowwe.core.ArticleCompilationLifecycleTest \
  de.knowwe.core.ArticleRollbackLifecycleTest
```

The direct JUnit runner exits successfully with the current `@Ignore` annotations. To inspect a pending contract,
temporarily remove its annotation and run the affected class with the direct JUnit command above so the fixture Rule is
applied. A normal Maven `test` run also reports the ignored tests, but the current parent POM hardcodes
`<testFailureIgnore>true</testFailureIgnore>`; inspect the test reports rather than relying on Maven's process status
alone.

The fixture uses real lifecycle components and an isolated Environment singleton with a small line parser. Reflection
only bootstraps/restores that singleton and closes private test-owned executors. It does not replace article or ID
behavior. The tests must run serially within a JVM. Existing manager tests now unregister their listeners and close
their executors so that later attachment tests cannot accidentally notify managers belonging to earlier tests.
