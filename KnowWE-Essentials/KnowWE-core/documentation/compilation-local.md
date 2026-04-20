# CompilationLocal

Package: `de.knowwe.core.compile`

## Zweck

`CompilationLocal<E>` ist ein **Cache fuer berechnete Werte, der nur waehrend eines
Kompilierungs-Zyklus gueltig ist**. Werte werden lazy ueber einen `Supplier` erzeugt und
ab dem naechsten Kompilierungslauf automatisch verworfen, sodass sie beim ersten Zugriff
neu berechnet werden.

Typische Einsatzfaelle:
- Teure Lookups, die mehrfach pro Compile-Phase in verschiedenen Compile-Skripten
  benoetigt werden (z.B. SPARQL-Query-Ergebnisse, Section-Suchen, Cross-Compiler-Aufloesung)
- Werte, die innerhalb einer Kompilierung stabil sind, sich zwischen Kompilierungen aber
  aendern koennen
- Caching von Default-Konfigurationswerten aus Markup-Annotations

## Cache-Scopes

Es gibt zwei orthogonale Scopes:

| Scope | API | Wann verworfen? |
|---|---|---|
| **Pro Compiler** | `getCached(Compiler, ...)` | Wenn der Compiler entfernt wird ODER seine naechste Compile-Phase startet (`ScriptCompilerCompilePhaseStartEvent`) |
| **Pro CompilerManager** | `getCached(CompilerManager, ...)` | Wenn der CompilerManager die naechste Kompilierung startet (`CompilationStartEvent`) |

Der **Pro-CompilerManager-Scope** ist fuer Compiler-uebergreifende Werte (z.B. globale
Wiki-Daten wie "alle aktuellen RecentChanges") oder Werte, die unabhaengig von einem
einzelnen Compiler bleiben sollen.

## API

### Nur per Compiler-Schluessel

```java
public static <L> L getCached(
    @NotNull Compiler compiler,
    @NotNull Object cacheKey,
    @NotNull Supplier<L> supplier);
```

### Per Compiler + Section (Section wird Teil des Schluessels)

```java
public static <L> L getCached(
    @NotNull Compiler compiler,
    @NotNull Section<?> section,
    @NotNull Object cacheKey,
    @NotNull Supplier<L> supplier);
```

Intern wird `new Pair<>(section, cacheKey)` als kombinierter Key verwendet.

### Per CompilerManager (Compiler-uebergreifend)

```java
public static <L> L getCached(
    @NotNull CompilerManager compilerManager,
    @NotNull Object cacheKey,
    @NotNull Supplier<L> supplier);
```

### Konditionales Caching

```java
public static <L> L getCachedIf(
    @NotNull Compiler compiler,
    @NotNull Object cacheKey,
    @NotNull Supplier<L> supplier,
    @NotNull BooleanSupplier condition);
```

Wenn `condition` `false` liefert: Supplier wird ausgefuehrt, Ergebnis aber NICHT gecached
(es sei denn, der Schluessel wurde frueher schon mal mit `true`-Bedingung gecached — dann
wird das alte gecachte Ergebnis zurueckgegeben). Nuetzlich, wenn der Wert nur unter
bestimmten Bedingungen stabil ist, z.B. nur ausserhalb der laufenden Kompilierung.

### Vorzeitig leeren

```java
removeCache(Compiler, cacheKey);
removeCache(Compiler, Section, cacheKey);
removeCache(CompilerManager, cacheKey);
```

Verwendung wenn man weiss, dass ein gecachter Wert ungueltig wurde, bevor das naechste
Compile-Event ihn ohnehin verwerfen wuerde.

## Lifecycle / Cache-Invalidierung

`CompilationLocal` registriert sich auf folgende Events:

- **`CompilerRemovedEvent`** → der gesamte Cache des entfernten Compilers wird verworfen
- **`ScriptCompilerCompilePhaseStartEvent`** → Cache des Compilers wird zu Beginn jeder
  neuen Compile-Phase verworfen
- **`CompilationStartEvent`** → der CompilerManager-Cache wird verworfen, ausserdem werden
  CompilerCaches fuer Compiler bereinigt, die nicht mehr im CompilerManager registriert sind
- **`ArticleManagerCommitDoneEvent`** (asynchron) → Eintraege mit Section-Schluesseln, deren
  Section nicht mehr "live" ist, werden entfernt (Garbage-Collection fuer veraltete
  Section-Caches)
- **Servlet-Context-Destroyed** → alle Caches werden geleert

## Threading

- Backing-Maps sind `ConcurrentHashMap` → thread-safe
- Pro Cache-Eintrag wird der Supplier ueber **Double-Checked-Locking** ausgefuehrt — d.h.
  bei mehreren Threads, die gleichzeitig auf denselben Schluessel zugreifen, wird der
  Supplier nur einmal aufgerufen
- `volatile` auf der gecachten Variable garantiert Sichtbarkeit ueber Threads

Wichtig: Die Supplier sollten keine Seiteneffekte haben, die mehrfache Ausfuehrung nicht
vertragen — falls ein Thread mit einem teuren Supplier laeuft und ein anderer auf dem
gleichen Schluessel wartet, faellt der zweite auf das berechnete Ergebnis zurueck.

## Verwendungsmuster (aus dem Code)

### 1. Per-Compiler-Lookup eines abgeleiteten Werts

```java
// OntologyMarkup.getDefaultNamespace
return CompilationLocal.getCached(compiler, "defaultNamespace", () -> {
    Section<OntologyMarkup> ontologyTypeSection = compiler.getCompileSection();
    Section<? extends AnnotationContentType> annotationContentSection =
            getAnnotationContentSection(ontologyTypeSection, ANNOTATION_DEFAULT_NAMESPACE);
    if (annotationContentSection == null) return null;
    // ... expensive lookup ...
});
```

Klassischer Fall: Der Default-Namespace eines OntologyCompilers haengt an dessen
Compile-Section, ist innerhalb einer Kompilierung stabil und wird haeufig abgefragt.

### 2. Per-Compiler + Section + Sub-Key

```java
// AbstractPackageCompiler.getCompiledPackages
return CompilationLocal.getCached(this, this.getCompileSection(), this.getClass(),
        () -> getCompileSection().get().getPackagesToCompile(getCompileSection()));

// Spaeter: gezielt invalidieren
public void refreshCompiledPackages() {
    CompilationLocal.removeCache(this, this.getCompileSection(), this.getClass());
}
```

Section + ClassToken bilden zusammen den Schluessel. `removeCache` erlaubt vorzeitiges
Verwerfen, wenn z.B. die Konfiguration extern geaendert wurde.

### 3. Compiler-uebergreifend (Per-CompilerManager)

```java
// RecentChangesUtils.getRecentChangesFromJSPWiki
return CompilationLocal.getCached(
        KnowWEUtils.getDefaultArticleManager().getCompilerManager(),
        "recentChangesFromJSPWiki",
        () -> wikiConnector.getPageManager().getRecentChanges());
```

Das Ergebnis ist nicht an einen einzelnen Compiler gebunden, sondern wiki-global
relevant. Cache-Invalidierung beim naechsten globalen Compile-Start ist genau richtig.

### 4. Konditionales Caching

```java
// DefaultMarkupPackageCompileType.resolvePackagesToCompile
if (!compilerManager.isCompiling()) {
    return CompilationLocal.getCached(
            articleManager.getCompilerManager(),
            "packagesToCompile_" + section.getID(),
            () -> resolvePackagesToCompile(section));
}
else if (isBetweenPackageCompilation(section, compilerManager)) {
    return CompilationLocal.getCached(
            compilerManager,
            "packagesToCompile_during_compilation" + section.getID(),
            () -> resolvePackagesToCompile(section));
}
return resolvePackagesToCompile(section);
```

Hier wird nur in bestimmten Phasen ueberhaupt gecached — wenn die Daten gerade volatile
sein koennten, wird ohne Cache aufgeloest.

### 5. Pro-Section in Compile-Skripten

```java
// TSMCoveringMarkup.isRestrictedToNotBeCompiledBy
return CompilationLocal.getCached(compiler, solutionSection, "isRestrictedToNotBeCompiledBy",
        () -> { /* expensive ancestor/successor traversal */ });
```

Hier ist `solutionSection` der Section-Anteil des Schluessels — die Berechnung passiert
einmal pro Solution, aber nur einmal pro Compile-Lauf.

## Best Practices

1. **CacheKey eindeutig waehlen**: Strings, Class-Token oder Pair sind ueblich. Wenn der
   Wert von zusaetzlichen Parametern abhaengt, in den Key kodieren
   (z.B. `"defaultNamespace_" + namespaceId`).

2. **Section-basierte Schluessel verwenden**, wenn der gecachte Wert an eine konkrete
   Section gebunden ist — der `cleanupStaleSectionCaches`-Mechanismus haelt den Cache
   sauber, falls die Section geloescht wird.

3. **Keine Mutable-State im Supplier-Ergebnis**: Da andere Threads dasselbe Objekt
   bekommen, sollte das Ergebnis immutable sein oder threadsicher behandelt werden.

4. **Nicht missbrauchen fuer permanenten Cache**: `CompilationLocal` ist explizit fuer den
   Lifecycle einer Kompilierung gedacht. Werte, die ueber mehrere Kompilierungen stabil
   sind, gehoeren in einen anderen Cache (oder sollten direkt persistiert werden).

5. **Bei conditional Caching die Bedingung verstehen**: `getCachedIf` cached nicht
   automatisch wieder, sobald die Bedingung wieder `true` wird — wenn der erste Aufruf
   `condition=false` lieferte, wird dauerhaft der Supplier aufgerufen, bis ein
   `condition=true`-Aufruf den Wert ablegt.
