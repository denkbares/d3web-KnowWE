# Multi-wiki, retired

KnowWE ran an experiment in which one instance served several wiki content folders at once. This document records
what the experiment was, how it worked, why it was retired, what was kept, and where the removed code can be found.
It is the entry point for anyone who wants to revive the idea.

## What it was

One KnowWE instance served a main wiki content folder plus one folder per dependency module. Each folder was its own
git repository, cloned as a sibling of the main content folder by the launcher. The main folder was writable and the
dependency folders were meant to be read-only references, so a module could use terms and packages defined in another
module without copying them.

## How it worked

- **Page names carried a prefix.** A page in a dependency folder was addressed as `<folder>&&<page>`, a page in the
  main folder as the plain page name. `SubWikiUtils` converted between the global name and the local name and held
  the three spellings of the separator (plain, url encoded, html encoded).
- **A router provider dispatched by prefix.** `MultiWikiPageProvider` and `AbstractMultiWikiFileProvider` split the
  global name, picked the folder, and delegated to a per-folder provider instance. There were file, versioning and
  attachment variants, plus the git backed `GitVersioningFileProviderMultiWiki` and
  `GitVersioningAttachmentProviderMultiWiki`.
- **The folder set was discovered by scanning.** `SubWikiInit` listed the subdirectories of the page directory and
  treated each one as a sub-wiki. `jspwiki.mainFolder` named the writable one.
- **The parser, renamer and name resolver were forked.** `JSPWikiMarkupParserMultiWiki`,
  `LinkParsingOperationsMultiWiki`, `DefaultPageRenamerMultiWiki` and `MultiWikiPageNameResolver` taught links,
  renaming and name resolution about the prefix. They lived in the `jspwiki-multiwiki` module of the jspwiki fork.
- **KnowWE-core had a seam for it.** `KnowWESubWikiContext` was threaded through `WikiConnector`, `ArticleManager`
  and `Article` so that KnowWE code could ask for an article in a specific sub-wiki.
- **The versioning plugin worked per repository.** Actions took a `repository` request parameter, iterated over all
  folders, and aggregated results per repository. `dependencies.json` in the main content folder pinned which release
  of each dependency an instance ran against.

## Why it was retired

- **The page namespace was not flat but everything on top of it assumed it was.** Terms, packages, CI dashboards,
  links and the COOM navigation model all key on page names. Two folders could hold the same page name, and the
  answer to that ambiguity was to return nothing or to pick one.
- **Dependency folders were writable in practice.** Write protection was instance wide only, so an edit in a
  dependency folder was written to disk and then ignored by publishing and deleted with the instance. That is a
  silent data loss path.
- **The task and release workflow stayed single repository.** Tasks, merge requests, publishing and cherry picking all
  operated on the main folder while the DTOs pretended to be per repository. Cross-module changes needed two merge
  requests in an order nobody enforced.
- **The cost was a permanent fork.** Parser, renamer, name resolver and four providers had to be kept working against
  every jspwiki API change, and a jspwiki 3 cutover was already in flight.
- **The plumbing had already rotted.** Several registered endpoints of the versioning plugin were no longer reachable
  from the frontend because it had stopped sending the parameters they read.
- **Instance startup scaled with the dependency count.** Every task instance cloned, fetched, switched and pulled
  every dependency synchronously before the wiki booted.

The replacement is one instance per wiki, one content folder, one git repository, plain page names.

## What was kept

The parts of the experiment that are about one git repository rather than about many wikis stayed in the tree, because
the single-wiki git provider is built on them. They are per repository and provider agnostic by construction.

- `org.apache.wiki.providers.git.GitPageHistory`, `GitPageVersion`, `GitRepoIndex`, `GitCommitBatch` and
  `GitCommitBatchRegistry` in `KnowWE-Plugin-JSPWiki-Connector`, with their unit tests, which run against temporary
  repositories and need no wiki.
- `org.apache.wiki.providers.GitVersioningProvider`, the transaction capability that lets bulk saves batch into one
  commit.
- `org.apache.wiki.providers.WikiFileNames`.
- The engine coupled helpers of the deleted providers (user data lookup, comment strategy resolution, event firing,
  page cache eviction), extracted into a shared class of the same package before the deletion.
- `de.knowwe.event.GitCommitEvent`, fired once per repository commit and consumed by the async push listener.

## What was removed and is not coming back on its own

The dependency version capability. An instance could be pointed at a specific release of each dependency module
through `dependencies.json`, and `CloneRepositoryAction`, `RemoveSubWikiAction`, `SwitchAllBranchesAction` and
`GetModuleVersionsAction` exposed that to the UI. All of it is gone, and `dependencies.json` is no longer a runtime
concept. If cross-module reuse with pinned versions is wanted again it needs a different mechanism than sibling wiki
folders, because sibling folders bring back the flat namespace problem and the writable reference problem above.

## Where the removed code lives

The annotated tag `multi-wiki-archive` marks the last working multi-wiki state in `KnowWE`, `KnowWE-DES`, `jspwiki`
and `KnowWE-Orchestration`. In `KnowWE` and `KnowWE-DES` it sits on the merge commit that brought the
`multi-wiki-git` branch to master, so the tagged state is the most advanced version of the experiment, including the
git backed providers. The tagged commits are part of master's history, so nothing can be garbage collected. The tag
message names what was removed after it.

Removed by that follow-up work were the two multi-wiki git providers, the whole `jspwiki-multiwiki` module of the
jspwiki fork, the `KnowWESubWikiContext` seam in KnowWE-core, the sub-wiki awareness of `KnowWE-Plugin-COOM`, the
multi-wiki plumbing and the dependency module actions of `KnowWE-Plugin-Versioning-Git`, and the dependency loading
of the orchestrator.
