# Article lifecycle follow-up audit

9 September 2026, following core checkpoint `16528a2b1`. The user reviewed this follow-up and authorized its commit;
private headless and live old-tab verification remain outstanding.

## Bridge and compilation

`OntologyBridgeLifecycleTest` initializes the actual plugin environment and compiles minimal public Ontology and
KnowledgeBase articles. It verifies forward/reverse compiler identity after ontology-only and KB-only unchanged
recompiles, content edits on either side, and a combined rebuild. It also covers ontology deletion/recreation, KB
deletion, and a repeated destroy callback on the old import annotation. The existing concurrent mapping test passes.
Dependency-triggered package registration updates the mapping in these scenarios; no bridge production change was
needed. This is actual compiler lifecycle coverage, but does not assert private d3web solution relations.

The bridge's compilation-local cache is cleared on compile-phase start/compiler removal. ID equality alone is still
not a compiler identity or a promise that a retained old compiler is usable. We have not reinstated the previously
deferred design that makes the bridge reject every inactive caller: destroy-phase users and cancellation semantics
would need a separate contract. Repeated annotation destruction is protected by consuming its stored registration.

CI's current-queue ownership checks and asynchronous predecessor waiting remain in place. Tests exercise the real
CIBuildManager with controlled execution; they do not run private production CI workloads or prove every consumer
honors interruption.

## Old tabs and server-side edits

KnowWE.js already triggers an update on window focus, except when beforeunload handlers indicate unsaved changes.
The update targets rerender markers. Missing section IDs now produce HTTP 409 from ReRenderContentPartAction, which
the existing shared error handler renders as an outdated-section/reload message rather than a deleted-page message.
The action response has a regression test. Browser interactions were inspected in source, not exercised in a live UI;
pages without rerender markers and tabs with unsaved work do not receive a universal proactive notification.

InstantEdit's load/enable paths already reject missing IDs. The save path delegates to Sections.replace. It previously
resolved IDs before opening the registration frame, allowing a waiting request to retain outdated article grouping.
Resolution now happens after open(): an edit invalidated while waiting is reported missing rather than accepted and
silently omitted from the current article. A coordinated lock-contention test covers this and rejects any unexpected
persistence write. This addresses registration-frame contention, not a full transaction against arbitrary external wiki
persistence writers or reentrant permission callbacks.

## Analogous references

- LongOperationUtils intentionally retains progress by ID across unchanged recompiles. A real article lifecycle test
  verifies retention and cleanup on content change. It does not cancel the underlying operation or prevent all later
  registrations by callers holding retired objects; those remain caller lifecycle responsibilities.
- OntologyExporter removed obsolete timer entries without cancelling their scheduled tasks. Cleanup now cancels the
  timers (regression-tested). Delayed exports check the source Section's live identity before serialization and again
  before persistence. These checks are not atomic with persistence: a rebuild after the last check can still overlap
  the attachment write. A transactional guarantee would require coordinated persistence/registration locking.
- Searches for numeric section-ID parsing in KnowWE and KnowWE-DES found no further adapters beyond the previously
  changed Word bookmark adapter. This is a source search, not proof about external clients or persisted third-party IDs.
- ID-indexed caches are not made universally generation-safe by the new namespace rule. Unchanged recompiles reuse
  addresses deliberately; code retaining Section/Compiler objects must still respect their lifecycle.

## Validation and remaining user verification

All 85 active core tests pass; the two documented pre-existing bookkeeping contracts remain ignored. All seven CI
tests pass, as do both bridge lifecycle/mapping tests, the exporter cancellation test and the rerender conflict test.
The bridge module now declares the existing managed GlobalTestUtils test dependency and dependency-plugin execution
needed by InitPluginManager. Dependency validation passes.

The user is running the private CBX headless test in parallel. Its outcome and a live old-tab/InstantEdit check remain
external validation. Neither the core checkpoint nor this follow-up provides atomic publication across all registries,
full transactional rollback, or a general stale-worker exclusion mechanism.
