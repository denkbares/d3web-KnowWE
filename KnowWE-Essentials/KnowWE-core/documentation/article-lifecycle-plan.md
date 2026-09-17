# Article lifecycle: implementation and follow-up plan

Status: 9 September 2026. User reviewed the core implementation and authorized its commit. The follow-up audits below
are tracked in the [follow-up audit](article-lifecycle-follow-up.md); this core checkpoint does not imply completed
integration verification.

## Agreed contract

- Constructing an Article creates a private draft. It must not destroy the published predecessor or expose IDs and
  diagnostics globally. Publication still happens at queue time, before ArticleRegisteredEvent, not at commit.
- A recompile with identical normalized source inherits the predecessor's ID namespace. An edit, including editing
  back to an earlier text, starts a new namespace and invalidates all previous IDs. Deletion/recreation also starts anew.
- IDs are opaque 32-character hexadecimal name-based UUIDs derived from the namespace, KDOM position, section type,
  offset, depth and text length. The namespace fixes the complete normalized article source, so offset/length identify
  the section text without copying and hashing it again. They are not wiki revision numbers, numeric values, compiler
  identities or authorization tokens.
  Lookup uses the complete string as a map key. No draft-ID reservations or global collision-allocation tables remain.
- Early ID requests remain stable within a draft. Unchanged recompiles make previously requested addresses resolvable
  again by requesting the corresponding new IDs, without assigning old IDs to new sections. Type/structure differences
  can invalidate addresses even if source text is unchanged. The guarantee assumes deterministic parsing.
- A recompile based on an unchanged predecessor that was superseded by an edit before queueing is rejected before
  manager state is changed. Retry with a fresh draft instead of reviving pre-edit IDs.
- Cleanup is owner-checked and retired versions cannot register IDs or diagnostics again. Rollback must reactivate the
  original Article's lifecycle; complete transaction/bookkeeping repair is not included automatically.
- The unused IncrementalSectionizerModule is removed; incremental compilation remains.

## Review sequence

1. **Review this core change first.** Check Article, Section, Messages, DefaultArticleManager and the regression tests.
   There is no atomic snapshot across separate article-map, ID-map and diagnostic queries and no thread-local view.
   The ID format change also requires the Word export's bookmark adapter to treat IDs as opaque strings.
2. **Audit OntologyBridge and analogous internal references explicitly.** Initial findings and executed tests are in
   the follow-up audit; its stated limitations and private integration verification remain.
   - Distinguish equal ID addresses from actual Section/Article/Compiler identity, especially on unchanged recompiles.
   - Trace registration, delayed unregister, cached ID-to-compiler lookups and compiler-priority waiting.
   - Test rebuilding only the ontology while the importing knowledge base stays unchanged, the reverse direction,
     rebuilding both, and an actual content change. Verify dependency-triggered re-registration, not only map locking.
   - Cover old CI workers and late callbacks; new IDs alone do not guarantee correct cancellation or lifecycle ordering.
   - Audit similar ID-indexed caches, sessions, deferred actions, links/export adapters and numeric/length assumptions.
3. **Review stale-browser handling.** Reuse the existing outdated-page notification/focus mechanism where appropriate.
   Focus checks alone are insufficient: stale actions must also be rejected server-side. No focus/UI change is included
   in the current core patch. Old IDs from before deployment will also be invalid after the ID format changes.
4. **Integration verification.** Run the affected RecompileAction, bridge and CI tests with the modified core, add actual
   article/compiler lifecycle integration coverage, and rerun the private CBX headless test with the user's fixtures.
   Synthetic core tests do not prove ontology/d3web semantics or compatibility of all downstream ID consumers.
5. Present follow-up results and remaining risks for user review before further commits.

## Test status

See [the regression-test notes](article-lifecycle-tests.md) for the current executed baseline. The known
new-article multi-replacement rollback and extra compiler-removal bookkeeping cases remain separately disabled with
reasons; their existence is not permission to expand this change silently.

## ID cost evaluation

The 128-bit hash remains for now. A 32-bit hash needs collision allocation/reservations again; an explicit namespace/path
address needs an additional type discriminator to preserve the parser-change contract and has unbounded length (including
for Word bookmarks). A local sequence also introduces a dependency on allocation/request order across parses.
The chosen smaller optimization removes section text from the hashed signature. First-ID cost no longer scales with
section text length; position lookup and digest creation still cost time. Cached getID calls are unchanged. This is not
an application-level performance claim or a completed audit of downstream consumers.

On 9 September, sibling indices were cached at child insertion. Appending maintains the index in constant time;
inserting updates the shifted suffix. Index reads validate the hint against the current parent's list, with a fallback
search for independently changed parent links. This supports the GrammarParser's addChild/setParent sequence and avoids
repeated sibling scans when constructing ID paths. Path construction still walks the ancestors and allocates a list.
