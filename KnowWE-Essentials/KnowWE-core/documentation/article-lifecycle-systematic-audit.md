# Section-ID and lifecycle consumer audit

9 September 2026; KnowWE baseline `031661ae0`. Review only; no production fixes applied.

## Subsequent fixes awaiting individual review

- A1 (Attachment listener): implemented after the audit. Listener equality uses Section instance identity;
  callbacks check live ownership inside the article registration frame before rebuilding. Four regression tests
  cover edited/unchanged replacements, late unregister versus a successor, and replacement while a callback waits
  for the frame. The first three failed before the fix. Full core suite: 89 passed, 2 previously ignored.
- A2–A5 and the integration follow-ups below remain unchanged in this step.

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
- NonCoverageQuestionsMarkup uses compiler-owned bookkeeping. TestStepCostCache also belongs to a compiler, but
  indexes costs by the *referenced test step's* Section ID. CableMarkup.CostCompileScript discovers references again
  at destruction rather than retaining its original targets. A test-step-only edit changes the key even if cable
  text is unchanged. Whether dependency recompilation reliably rebuilds all these costs remains an explicit
  follow-up test, not a confirmed failure or a clearance. Exercise a one-sided edit and compare with a full compile.
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
Retain the one-sided TestStepCostCache and KnowSEC integration cases as unresolved verification, not assumed safety.
