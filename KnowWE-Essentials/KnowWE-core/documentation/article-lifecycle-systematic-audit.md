# Section-ID and lifecycle consumer audit

9 September 2026; KnowWE baseline `031661ae0`. Review only; no production fixes applied.

## Subsequent fixes awaiting individual review

- A1 (Attachment listener): implemented after the audit. Listener equality uses Section instance identity;
  callbacks check live ownership inside the article registration frame before rebuilding. Four regression tests
  cover edited/unchanged replacements, late unregister versus a successor, and replacement while a callback waits
  for the frame. The first three failed before the fix. Full core suite: 89 passed, 2 previously ignored.
- A2 (CI freeze normalization): implemented 10 September 2026. Accepts legacy 1–8 hex IDs and current
  32-hex IDs without broadening to arbitrary hex lengths. Three regression tests cover link normalization,
  stable header keys across formats/versions, and preservation of other anchors. Two failed before the fix;
  all 30 CI4KE module tests pass afterward.
- A3 (LongOperation registration): implemented 10 September 2026, awaiting review. Atomic ID-map creation replaces
  the Section-instance lock; registration and deduplication share the inner-map monitor. Article lifecycle validation
  and insertion run under the same Article monitor as destroy, without opening a compilation-blocking registration
  frame. Retired/non-current managed owners fail fast; temporary articles remain supported until retired. Read-only
  registration lookup no longer creates map entries. Existing progress still survives unchanged recompiles.
  Four added tests cover obsolete owners, draft/read-only behavior, retirement during operation-ID resolution,
  and concurrent registration. Full core suite: 127 passed, 2 previously ignored.
  This guards registration, not execution: it neither cancels existing operations nor prevents restarting an already
  registered operation that retains old domain objects. Such operations still need their own execution contract.
- A4 (Rerender cleanup): implemented 10 September 2026, awaiting review. Cleanup conditionally removes the exact
  request future, leaving a successor's registration intact. The action's registration/wait/cleanup sequence is
  extracted into a package-private method for a deterministic three-request regression test (no sleeps or wiki
  rendering). The test failed with unconditional removal and passes with owner-conditional removal; it also checks
  successful-request cleanup. All 3 KnowWE-Plugin-Core tests pass. Cancellation still does not interrupt render work.
- A5 (ServiceMateBridge): implemented in KnowWE-SSP on 10 September 2026, awaiting review. A read/write lock
  protects every mapping access; readers receive immutable ID snapshots. Resolution and compilation waits never
  hold that lock. Missing targets/compilers are skipped, candidate compile Sections must match by instance, and
  resolution is rechecked after awaiting compilation. Interrupted waits preserve the interrupt and stop traversal.
  Registration of an already missing Section reports an informative IllegalArgumentException. This is not atomic
  publication across the mapping/Section/compiler registries, nor an active-caller-only contract.
  Two new tests use real registered KB articles to exercise mapping snapshots, equal-ID replacement, deletion and
  concurrent forward/reverse access. These test the mapping API, not a complete ServiceMate domain compilation or
  a controlled replacement during a priority wait. Both pass, as do both XPSReference tests (4 targeted tests).
- A6 (TestStepCostCache): confirmed, implemented and reviewed in KnowWE-SSP on 10 September 2026.
  A test-step-only text edit lost inherited cable costs after changing its Section ID. Independently, a cable-only
  cost edit updated the cache but left the unchanged step's compiled COST property stale. Costs now use semantic
  connector identifiers with source-instance ownership; lookups use the step's current connector references.
  Source changes collect connector identifiers. A KB script at BELOW_DEFAULT invalidates only their current users'
  lazy cached results and calls Compilers.recompileSection with only the cost script (LOWER),
  after source registration at DEFAULT. This also handles removal of the last cost. Retained Section keys are weak,
  including sources inside cached results; explicit removal remains. Cache hits copy only the cached costs, without
  traversing references; unrelated test steps keep their entries. Cache misses resolve identifiers outside the
  monitor, then read all source costs and publish the result together under it. A change counter forces a retry
  after concurrent source updates, invalidation or reset; it does not globally invalidate cached entries.
  Deterministic latch-controlled scenarios cover an update and a removal during term resolution, checking both
  returned and memoized results and that resolution does not hold the cache monitor. This is cache-state safety,
  not an atomic transaction across ontology/article publication; existing cache hits retain the previous batch
  until the refresh script invalidates them. Empty connector buckets
  are pruned by the batch script. Cache maps share a short monitor;
  ontology/compiler calls occur outside it. A replaced KB object
  clears the cache even when a full build retains the compiler. A synthetic integration test covers unchanged and
  edited step replacement, changed connector, cable-only cost edits/removal, late source cleanup, full rebuilds with
  new and retained compilers, and step deletion/recreation. It and SettingsContainerTest pass (2 targeted tests).
- The KnowSEC integration follow-up below remains open.

## Scope and confidence

The broad Java inventory across the local Main repositories found 651 files containing `Sections.get(` or
`.getID(`. This includes unrelated domain IDs and tests; it is not a count of individually verified consumers.
Risk-directed tracing concentrated on KnowWE, KnowWE-DES and KnowWE-SSP: shared registries, ID-keyed caches,
retained Sections/Compilers, delayed callbacks, registration/unregistration, and ID format assumptions.
Supplemental searches covered static Section collections and direct ID equality, including KRONE and KMW.
Java/JS/TS format searches excluded build output and common generated/vendor directories.

This is a systematic source-search and high-risk call-path review, not an exhaustive proof of all plugin behavior.
Unchanged recompiles deliberately reuse addresses, not objects. Content edits invalidate addresses throughout the
article. Neither an existing ID nor a previously successful live check grants an operation a lifecycle lease.

## Findings

### A1 — Attachment callback can restore retired article text (high priority)

`KnowWE-core/.../kdom/basicType/AttachmentType.java:171–189` retains a Section and calls
`registerArticle(article.getTitle(), article.getText())` using its Article, without validating that instance.
`denkbares-Commons/denkbares-Utils/.../events/EventManager.java:139–154` copies listeners before calling them
outside its monitor. Unregistering a listener cannot retract a callback already in that copy.

Sequence: dispatch captures old listener; another operation publishes edited text and unregisters that listener;
the captured callback executes and registers the old text. The retired Article still has its ArticleManager.
This restores stale text in the in-memory article/compiler state; the probe does not demonstrate a wiki-store write.
There is also ID-based listener equality (`AttachmentType:145–158`), so equal-ID generations are not distinct
EventManager keys. A late explicit unregister could remove the successor; normal script destruction consumes its
stored listener, so repeated normal destruction alone is not proof of that additional sequence.

Reproduced with the real ArticleManager and private production callback. Reflection constructs the listener against
a fixture Section; invocation after unregistration models the already-captured callback, not a full threaded event
integration test. Suggested fix: resolve/validate the source under the article registration frame and use current
text, with a defined policy for obsolete listeners. A live check before acquiring the frame is insufficient.

### A2 — CI freeze normalization still assumes short IDs (format regression)

`KnowWE-Plugin-CI4KE/.../dashboard/action/CIFreezeFailedTestsAction.java:331–333` only removes
`#[a-f0-9]{1,8}]`. `normalizeHeader` uses it to match frozen report blocks (call sites at 170, 185, 263, 276, 278).
New 32-hex IDs remain in the key: otherwise equivalent old/new report headers can fail to match after an edit.
Directly reproduced against the production `normalizeLink` method. Extend the accepted alternatives to the old
1–8 hex format and the new 32-hex format, as in the private headless test helper.

### A3 — LongOperation registry mixes ID ownership and instance locks

`KnowWE-core/.../utils/progress/LongOperationUtils.java:134–148` performs get/create/put in a shared ID-keyed map
while synchronizing on the Section instance. Equal-ID predecessor/successor instances use different monitors:
concurrent initial creation can overwrite an inner operation map. The concurrent outer map does not make that
compound operation atomic.

Separately, registration accepts retired Sections. A diagnostic probe registers an operation through the retired
predecessor after an unchanged recompile and retrieves it through the current Section. This is more than progress
retention: delayed rendering can publish new work through an obsolete owner (`LongOperationToolProvider:60,87`).
Some operations retain their Section, e.g. KnowSEC `ImportSourceOperation:86–101`.

The existing lifecycle test deliberately permits *already registered* progress to survive an unchanged recompile.
Preserve that contract; use atomic map creation and separately define/restrict late registration and operation start.
The lost-map race was source-traced, not stress-tested; the retired-registration behavior was reproduced.

### A4 — Rerender cleanup can remove another request's future

`KnowWE-Plugin-Core/.../action/ReRenderContentPartAction.java:141–165`: request B replaces and cancels A's future;
A then executes unconditional `RENDER_FUTURES.remove(key)` in its finally block, removing B's entry. Request C
can no longer find/cancel B, defeating the intended request-waiter limiting. The key is username plus Section ID,
so this also spans unchanged recompiles. Use owner-conditional removal with the exact future.

This is a directly traceable ordering defect, not a newly reproduced threaded test and not specific to the new IDs.
It does not imply `cancel(false)` stops the renderer task itself.

### A5 — ServiceMateBridge lacks the mapping synchronization used by OntologyBridge

`KnowWE-SSP/KnowWE-Plugin-SemanticServiceCore/.../servicemate/ServiceMateBridge.java:39–66,104–109,325–350`
uses an unsynchronized N2MMap, backed by ordinary HashMaps/HashSets. Registration scripts in
`ConceptStoreReference:77,85` and `XPSReference:125,133` mutate it; readers iterate its sets and resolve current IDs.
`export/DANDownloadAction.java:102` reads the bridge before acquiring `blockCompilation` at line 113.
Concurrent lookup and compilation are therefore not excluded by that action's later compilation block.

Risks include inconsistent forward/reverse observations, iteration failure, and null resolution during retirement.
`getServiceMateCompilers` dereferences a resolved markup without a null guard; `getCompilers` passes a potentially
missing Section to `Compilers.getCompilers`, whose implementation dereferences it. This is a source-backed
concurrency/lifecycle risk; no end-to-end ServiceMate timing reproduction was run.

Bring mapping access to a consistent lock/snapshot contract and handle unavailable targets explicitly. Do not hold
the mapping lock while awaiting compilation. The registration scripts consume their stored registration on destroy;
the scan does not establish that an ordinary repeated destroy removes a successor mapping. Rejecting all inactive
compiler callers would be a separate contract change, just as with OntologyBridge.

## Other inspected categories and remaining uncertainty

- OntologyBridge: existing read/write locking and compile-phase cache invalidation remain; the prior real-plugin
  lifecycle tests cover one-sided/combined rebuilds, edits, deletion/recreation and repeated old annotation destroy.
  This scan does not upgrade it to an active-caller-only bridge. See `article-lifecycle-follow-up.md`.
- Section registry and Messages use instance ownership; TermLog removal includes the Section object. PackageManager
  collections use Section objects, not IDs as interchangeable object identities. Existing core tests remain relevant.
- CompilationLocal caches are scoped to compiler/manager and invalidated by lifecycle events; package-name caching
  stores strings/arrays rather than a globally retained Section under an ID. No additional demonstrated defect here.
- InterWikiImportUpdateService stores ID/metadata snapshots and re-resolves/live-checks before using them; attachment
  update bookkeeping is keyed by attachment path, not Section ID. OntologyExporter retains the previously documented
  non-atomic live-check/persistence limitation.
- NonCoverageQuestionsMarkup uses compiler-owned bookkeeping. The TestStepCostCache concern was reproduced by
  a synthetic one-sided-edit integration test and addressed in A6 above; the original target-Section-ID index did
  lose unchanged cable costs after a step-only text edit.
- KnowSEC SessionManager checks live identity in updateSession but cleans up retired entries by semantic identity;
  its batched queue also retains Sections. Full correctness of update/cleanup ordering across ontology generations
  is not established by this scan. It needs a domain integration test before being declared safe.
- SemanticAutocompletion and XMLManager use instance-keyed WeakHashMaps, so equal IDs alone do not alias entries.
  Their unsynchronized access/invalidation is an additional general concurrency concern, not a proved new ID bug.
  COOM session reuse checks model iteration identity; a Section ID is not its version token.
- Local replacement maps route through the hardened Sections.replace path; this does not make arbitrary earlier
  reads or unrelated persistence writes transactional. External clients, private tests and persisted plugin data
  are outside this source audit's completeness claim.

## Validation

Three isolated JUnit diagnostic probes passed against existing local compiled classes: stale attachment callback,
retired LongOperation registration, and CI freeze normalization. These probes assert the observed unsafe behavior,
not the desired fixed behavior. Source is kept in the Codex task workspace as `LifecycleAuditProbeTest.java`, outside
the production repository. No full Maven suite or live wiki was run for this read-only audit. Previously reported
regression suite results belong to the earlier checkpoint, not a fresh execution in this scan.

Recommended follow-up: regression test plus fix for A1 and A2 first; then A3/A4 and the ServiceMate mapping contract.
The TestStepCostCache integration case is now covered by A6; retain KnowSEC as unresolved verification, not assumed safety.
