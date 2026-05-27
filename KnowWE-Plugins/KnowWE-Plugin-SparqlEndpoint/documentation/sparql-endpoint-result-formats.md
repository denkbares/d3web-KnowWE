# SPARQL-Endpoint – Ergebnis-Formate

Über den SPARQL-Endpoint kannst du eine SPARQL-Query an KnowWE schicken und das Ergebnis in
verschiedenen Formaten zurückbekommen (JSON, XML, CSV, Turtle, …).

## So funktioniert es

Du rufst diese URL auf und hängst zwei Dinge an:

```
<url>/action/SparqlEndpointAction?query=<SPARQL>&Accept=<Format>
```

- `query` – deine SPARQL-Query (URL-encoded).
- `Accept` – das gewünschte Format (siehe Tabellen unten).

Optional: `package` (welche Ontologie/Package abgefragt wird) sowie `user` + `password`,
falls du nicht ohnehin in KnowWE eingeloggt bist.

**Zwei kleine Stolperfallen:**

- Der Parameter heißt `Accept`, **nicht mehr** `format` – `format=...` wird ignoriert.
- Ein `+` im Format muss in der URL als `%2B` geschrieben werden
  (z. B. `application%2Fsparql-results%2Bjson`). In den Tabellen ist die fertige Variante dabei.

Statt des Parameters kannst du das Format auch als `Accept`-HTTP-Header mitschicken – praktisch
z. B. bei `curl` (Beispiele unten).

Welches Format möglich ist, hängt vom Query-Typ ab (`SELECT`, `ASK` oder `CONSTRUCT`/`DESCRIBE`).
Gibst du nichts an, kommt das Ergebnis als SPARQL-XML.

---

## SELECT

| Format | Als HTTP-Header (`Accept:`) | Als URL-Parameter |
|---|---|---|
| JSON | `application/sparql-results+json` | `Accept=application%2Fsparql-results%2Bjson` |
| JSON (alias) | `application/json` | `Accept=application%2Fjson` |
| XML *(Default)* | `application/sparql-results+xml` | `Accept=application%2Fsparql-results%2Bxml` |
| CSV | `text/csv` | `Accept=text%2Fcsv` |
| TSV | `text/tab-separated-values` | `Accept=text%2Ftab-separated-values` |
| Binary | `application/x-binary-rdf-results-table` | `Accept=application%2Fx-binary-rdf-results-table` |
| SPARQL-star/JSON | `application/x-sparqlstar-results+json` | `Accept=application%2Fx-sparqlstar-results%2Bjson` |
| SPARQL-star/XML | `application/x-sparqlstar-results+xml` | `Accept=application%2Fx-sparqlstar-results%2Bxml` |
| SPARQL-star/TSV | `application/x-sparqlstar-results+tsv` | `Accept=application%2Fx-sparqlstar-results%2Btsv` |

## ASK

| Format | Als HTTP-Header (`Accept:`) | Als URL-Parameter |
|---|---|---|
| JSON | `application/sparql-results+json` | `Accept=application%2Fsparql-results%2Bjson` |
| XML | `application/sparql-results+xml` | `Accept=application%2Fsparql-results%2Bxml` |
| Text (true/false) | `text/boolean` | `Accept=text%2Fboolean` |

## CONSTRUCT / DESCRIBE (RDF-Graph)

| Format | Als HTTP-Header (`Accept:`) | Als URL-Parameter |
|---|---|---|
| Turtle | `text/turtle` | `Accept=text%2Fturtle` |
| TriG | `application/trig` | `Accept=application%2Ftrig` |
| RDF/XML | `application/rdf+xml` | `Accept=application%2Frdf%2Bxml` |
| N-Triples | `application/n-triples` | `Accept=application%2Fn-triples` |
| Binary RDF | `application/x-binary-rdf` | `Accept=application%2Fx-binary-rdf` |
| Turtle-star | `text/x-turtlestar` | `Accept=text%2Fx-turtlestar` |
| TriG-star | `application/x-trigstar` | `Accept=application%2Fx-trigstar` |

> Die `*-star`-Varianten brauchst du nur, wenn dein Ergebnis RDF-star-Daten enthält –
> für normale Queries kannst du sie ignorieren.

---

## Beispiele

### SELECT als JSON – über die URL (Browser)

```
<url>/action/SparqlEndpointAction?query=SELECT%20%2A%20WHERE%20%7B%20%3Fx%20%3Fy%20%3Fz%20%7D&Accept=application%2Fsparql-results%2Bjson
```

### SELECT als JSON – mit curl

```bash
curl -H "Accept: application/sparql-results+json" \
  "<url>/action/SparqlEndpointAction?query=SELECT%20%2A%20WHERE%20%7B%3Fx%20%3Fy%20%3Fz%7D"
```

### CONSTRUCT als Turtle – mit curl

```bash
curl -H "Accept: text/turtle" \
  "<url>/action/SparqlEndpointAction?query=CONSTRUCT%20%7B%3Fx%20%3Fy%20%3Fz%7D%20WHERE%20%7B%3Fx%20%3Fy%20%3Fz%7D"
```
