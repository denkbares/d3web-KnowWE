# WAR für die Docker-Testserver hochladen (Profil `upload-war`)

Die Docker-Testserver (z. B. das KnowSEC-Test-Wiki auf brienne) beziehen ihr WAR aus dem generischen
Artifactory-Repository `artifacts-des/knowwe-docker-builds` als `KnowWE-<dist>.war`. Befüllt wird es von
einem Jenkins-Job, der für jede Profilkombination auf dem **Master** `mvn clean package -U -P des-repos,<profile>`
ausführt, das WAR umbenennt und per `jfrog rt u` hochlädt (`os`, `d3web`, `des`, `ssp`, `konap`, `cbx`, `coom`,
`coom-versioning`, `knowsec`, ...).

Das Profil `upload-war` in `KnowWE-App/pom.xml` macht denselben Upload aus Maven heraus, damit ein WAR auch
**manuell und von einem Branch** gebaut und hochgeladen werden kann. Anlass: das KnowSEC-WAR soll vom
Release-Branch `knowsec/release-2026-07` kommen, nicht vom Master; die `knowsec`-Zeile des Jenkins-Jobs
wird dafür auskommentiert, sonst überschreibt der nächste Master-Lauf den Branch-Stand.

## Ablauf für einen Branch-Build

1. Alle Repos auf den Branch, die Closure von `KnowWE-App` lokal bauen (Workspace-Skript mit
   `--target KnowWE-App --profile core-plugins --profile denkbares --profile knowsec`, oder IntelliJ). Nichts
   davon liegt für den Branch auf `repo.denkbares.com`; Remote-SNAPSHOTs gleicher Versionsnummer sind dort
   Master-Stände von früher.
2. Packen und hochladen, **offline**, damit genau diese lokalen Artefakte ins WAR kommen:

   ```bash
   mvn -o clean package -P core-plugins,denkbares,knowsec,upload-war
   ```

   Hochgeladen wird `target/KnowWE-App-<version>.war` (dasselbe WAR wie beim Jenkins-Job) als
   `KnowWE-knowsec.war`. Der Name kommt aus `war.upload.dist`; die Profile `knowsec` und `cbx` setzen ihn, für andere
   Kombinationen `-Dwar.upload.dist=<name>` mitgeben. Ohne die Property bricht `validate` mit einer
   Fehlermeldung ab, statt ein `KnowWE-${war.upload.dist}.war` abzulegen.
3. Zugangsdaten: ein `<server>` in `~/.m2/settings.xml`, dessen Id `war.upload.serverId` entspricht.
   Default ist `des-snapshots-public`, die Id der `distributionManagement`, die Entwickler mit Deploy-Rechten
   ohnehin haben; jede andere Id mit Artifactory-Zugang geht per `-Dwar.upload.serverId=...`. Das Konto
   braucht Schreibrecht auf `artifacts-des/knowwe-docker-builds`.

Ziel-URL (`war.upload.url`, Default `https://repo.denkbares.com/artifacts-des/knowwe-docker-builds`) und
Server sind Properties, ein `file:///...`-Ziel eignet sich zum Ausprobieren ohne Server. Technisch ist es
`wagon-maven-plugin:upload-single` (HTTP `PUT`, wie ihn Artifactory für generische Repositories erwartet);
das Plugin muss einmal online ins lokale Repository geholt worden sein, danach reicht `-o`.

## Was geprüft ist (2026-09-22)

- `mvn -o -P upload-war validate` ohne `war.upload.dist` bricht mit der Meldung des Enforcers ab.
- Die Upload-Execution allein (`org.codehaus.mojo:wagon-maven-plugin:2.0.2:upload-single@upload-war`,
  offline, `file://`-Ziel) legt das 157-MB-WAR als `KnowWE-knowsec.war` ab.
- In der effektiven POM (`help:effective-pom` mit allen vier Profilen) steht `maven-war-plugin` vor
  `wagon-maven-plugin`; innerhalb der Phase `package` läuft damit `war` vor `upload-single`, und
  `war.upload.dist` ist über das Profil `knowsec` auf `knowsec` aufgelöst.
- Der komplette `clean package`-Lauf gegen Artifactory ist **nicht** hier geprüft, sondern Sache des
  Ausführenden mit seinen Zugangsdaten.
