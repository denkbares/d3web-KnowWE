Git-Essentials
======

This module contains an API to manage a local git repo programmatically from any kind of java application.
It specifies an interface, that offers to execute any common git operation,
such as `add`, `fetch`, `commit`, `pull`, `push`, `create branch`, `switch brach`, `set-upstream and many more.

Implementations
---------------

For the mentioned interface, different implementations exist:

1) BareGitConnector: Uses CLI process calls to execute git commands. Hence, it requires git installed on the machine,
that the respective application is installed on.
2) JGitConnector: Uses the java open source Git API JGit (https://github.com/eclipse-jgit/jgit). Hence, no git installation
on the server is required. However, in some cases performance seems to be not good.
3) CachingGitConnector: Is a cached wrapper caches the git log history, to be accessible fast after the first cache fill run.
It uses an internal delegate GitConnector to do the actual git calls.
4) JGitBackedGitConnector: This connector internally uses a BareGitConnector and a JGitConnector instance and delegates
the calls to one of those according to what is more beneficial (robust, performant) according to experiences.
