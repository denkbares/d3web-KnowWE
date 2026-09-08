# Article lifecycle regression tests

These tests precede the proposed construction/publication refactoring. Production code is unchanged. The contract is
that constructing an article must not affect the live version; publication still happens when queueing, not at commit.
The desired behavior is asserted by the active tests. Contracts that require the planned lifecycle change are marked
with JUnit `@Ignore`, each with a reason describing the current limitation; they remain visible in reports as skipped.

## Baseline

On repository baseline `c55d55ee3`, 8 September 2026 (production code unchanged):

| Test class | Passing | Ignored |
| --- | ---: | ---: |
| `ArticleLifecycleTest` | 5 | 10 ignored |
| `ArticleCompilationLifecycleTest` | 10 | 1 ignored |
| `ArticleRollbackLifecycleTest` | 3 | 3 ignored |
| Existing core tests | 38 | 0 |
| Total | 56 | 14 ignored |

The original 12 ignored tests were enabled on 7 September and failed as assertions, not because of fixture
initialization errors or timeouts; running the core test classes in both alphabetical and reverse alphabetical order
produced the same failing methods. The two additional ignored tests were enabled on 8 September and
also failed as assertions without timeout or fixture setup failures. The two parser fixtures log their injected
exceptions when enabled; those log messages are expected.

The failing contracts cover:

- Construction redirecting live IDs and removing live message tracking before publication.
- Draft IDs and diagnostics becoming globally visible.
- Cleanup of an old article removing the replacement's ID; a first ID request on a retired section registering it again.
- Late cleanup of a deleted `Aa` owner removing the same root ID after a colliding `BB` owner has reused it.
- A failed parser leaving a partial section globally registered.
- A concurrent reader observing a replacement's section before the replacement article has been queued.
- Overlapping unqueued replacement drafts removing the live registry mapping during construction and discard.
- A newly created and replaced article being reported to the compiler as an original removal.
- Rollback failing to restore ID/message tracking, and incorrectly retaining an intermediate newly created article.

Rollback contracts are grouped separately because full transaction rollback repair is not automatically part of the
smaller construction/publication refactoring. The extra compile removal is also an existing behavior to assess, not a
reason to silently expand the production change.

Passing cases protect stable IDs on unchanged replacement, real hash collisions, partial changes, deletion/recreation,
visibility before commit, reentrant events, actual AttachmentManager full-parse/deduplication/removal (including shared
attachment references), and the synthetic clear/restore contribution sequence. The contribution ledger uses a test
compiler and does not validate d3web/ontology semantics. The private headless test still needs to be rerun with its
external fixtures after the production change.

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
