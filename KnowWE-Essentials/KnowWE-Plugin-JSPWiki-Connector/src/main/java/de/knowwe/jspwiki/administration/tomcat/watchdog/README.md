# 🛠️ Tomcat Restart Watchdog

Mit diesem Setup lässt sich der Tomcat über **„Restart Tomcat“** auf der Administration-Seite des Wikis neu starten. Der Wiki-Admin braucht dafür keinen Zugriff auf den Server.

Die Web-App startet den Tomcat nicht selbst neu, sie darf und kann ihren eigenen Dienst nicht steuern. Sie legt nur eine Datei `restart.flag` an. Ein externes Skript (der Watchdog) überwacht diese Datei, startet den Dienst bzw. Container neu und löscht die Datei danach.

---

## ⚙️ Funktionsweise

```
Wiki (Tomcat-Konto)                         Watchdog (eigenes Konto)
───────────────────                         ────────────────────────
Beim Start: schreibt eigene PID    ──────►  sucht alle ~10 s den Tomcat-Dienst
  nach tomcat.pid                             (über die PID, sonst über den Namen)
                                   ◄──────  schreibt watchdog.alive:
                                              Zeitstempel + „OK …“ / „ERROR …“
30 s nach dem Start: Warnung im             schreibt watchdog.log
  Wiki-Log, wenn ein Neustart nicht
  funktionieren würde
Button „Restart Tomcat“ sichtbar,
  wenn watchdog.alive < 60 s alt,
  der Watchdog den Dienst gefunden hat,
  watchdog.log existiert und
  das Verzeichnis beschreibbar ist
Klick → legt restart.flag an       ──────►  findet restart.flag
  (enthält die PID)                         → startet den Dienst/Container neu
                                            → löscht restart.flag
Wiki prüft nach: Wurde das Flag
  abgeholt? Fährt der Tomcat herunter?
  Wenn nicht → Fehler im Wiki-Log
  (inkl. Ende von watchdog.log) und
  Fehlermeldung im Browser
```

Im Regelfall läuft auf einem Server genau ein Tomcat, und es ist nichts zu konfigurieren: Wiki und Watchdog verwenden `C:\tomcat-restart\` (Windows) bzw. `/knowwe/tomcat-restart/` (Linux/Docker). Beim Start schreibt das Wiki ins Wiki-Log, welches Verzeichnis es verwendet, z. B. `Tomcat restart watchdog directory: C:\tomcat-restart\ (default, -Dknowwe.tomcat.restart.dir is not set)`.

Das Verzeichnis lässt sich auf beiden Seiten ändern (Wiki: System-Property `knowwe.tomcat.restart.dir`, Skript: `-RestartDir` bzw. zweites Argument). Das ist nur für den Sonderfall mehrerer Tomcat-Instanzen auf einem Server vorgesehen, siehe [„Sonderfall: mehrere Tomcat-Instanzen auf einem Server“](#-sonderfall-mehrere-tomcat-instanzen-auf-einem-server).

---

## 🪟 Einrichtung unter Windows

### 1. Skript ablegen und anpassen

`tomcat-restart-watchdog.ps1` als **`C:\tomcat-restart\watchdog.ps1`** ablegen. Das Skript verwendet das Verzeichnis, in dem es liegt.

Ein Dienstname muss **nicht** eingetragen werden. Der Watchdog bestimmt den Dienst bei jedem Durchlauf neu, ein neuer Dienstname nach einem Tomcat-Update braucht also keine Änderung am Skript. Nach folgendem Vorgehen wird der Dienst bestimmt:
1. Der Dienst, der die Prozess-ID aus `restart.flag` bzw. `tomcat.pid` ausführt, auch wenn die JVM als Kindprozess des Dienstes läuft.
2. Sonst der einzige Dienst, dessen Dienstnamen (nicht Anzeigename) auf `Tomcat*` matcht. Das Pattern kann auch mit `-ServicePattern` an der Aufgabe überschrieben werden. Passen mehrere Dienste, der einzige davon, der gerade läuft.

### 2. Geplante Aufgabe einrichten

Öffne den **Taskplaner** (`taskschd.msc`) → **Aufgabe erstellen...**

**Allgemein**
- **Name:** `Tomcat Restart Watchdog`
- **Benutzer:** `SYSTEM` („Benutzer oder Gruppe ändern…“ → `SYSTEM`). SYSTEM darf Dienste beenden und starten und hat lokal Vollzugriff auf `C:\tomcat-restart`. Es sollte **nicht** das Dienstkonto des Tomcat sein, siehe Abschnitt 3.
- **Mit höchsten Privilegien ausführen** ✅
- **Konfigurieren für:** deine Windows-Version

**Trigger**
- Neu → **Beim Systemstart**

**Aktionen**
- Neu → **Programm starten**
  - **Programm/Skript:**
    ```
    powershell.exe
    ```
  - **Argumente hinzufügen:**
    ```
    -NoProfile -WindowStyle Hidden -ExecutionPolicy Bypass -File "C:\tomcat-restart\watchdog.ps1"
    ```
    Optional dahinter `-ServicePattern "KnowSEC*"` (Abschnitt 1). Keine Umleitung wie `*> datei.log` anhängen. Die Aufgabenplanung startet `powershell.exe` ohne Shell, die Umleitung würde nur als Argument an das Skript weitergereicht. Alle Meldungen stehen in `watchdog.log`.
  - **Starten in (optional):**
    ```
    C:\tomcat-restart
    ```

**Bedingungen**
- „Nur starten, wenn Computer im Netzbetrieb ist“ → deaktivieren

**Einstellungen**
- „Beenden, wenn die Aufgabe länger als ... läuft“ → deaktivieren
- „Wenn die Aufgabe bereits ausgeführt wird → Keine neue Instanz starten“ → aktivieren

Speichern. **Nach Änderungen am Skript die Aufgabe beenden und neu starten.** Das Skript läuft in einer Endlosschleife und liest Änderungen sonst nicht ein.

### 3. Berechtigungen

Zwei Konten sind beteiligt:

| Wer | Konto | Braucht |
|---|---|---|
| Tomcat / Wiki | Anmeldekonto des Tomcat-Dienstes, z. B. „Lokaler Dienst“ oder ein AD-Dienstkonto (etwa für den Zugriff auf Netzlaufwerke) | Ändern-Rechte auf `C:\tomcat-restart` (Flag anlegen, Test-Flag anlegen und löschen) |
| Watchdog | SYSTEM (empfohlen) | Dienst beenden/starten, Dateien in `C:\tomcat-restart` lesen, schreiben, löschen. SYSTEM hat beides von Haus aus |

Das Anmeldekonto des Tomcat steht in `services.msc` → Dienst → „Anmelden“. Der Watchdog schreibt es beim Start auch ins `watchdog.log` (`Account=…`).

Schreibrechte für das Tomcat-Konto setzen:

```powershell
# Tomcat läuft als "Lokaler Dienst"
icacls "C:\tomcat-restart" /grant "*S-1-5-19:(OI)(CI)(M)"

# Tomcat läuft unter einem Domänen-Dienstkonto
icacls "C:\tomcat-restart" /grant "DOMÄNE\dienstkonto:(OI)(CI)(M)"
```

**Achtung beim Wechsel des Tomcat-Kontos:** Bekommt der Tomcat-Dienst ein anderes Anmeldekonto, z. B. ein AD-Konto für Netzlaufwerke, muss auch dieses Konto die Schreibrechte bekommen. Die geplante Aufgabe sollte weiter als SYSTEM laufen. Wird sie stattdessen auf das Tomcat-Konto umgestellt, darf sie den Dienst standardmäßig **nicht** beenden. Zusätzlich braucht sie dann das Recht „Anmelden als Stapelverarbeitungsauftrag“ und ein gespeichertes Kennwort, das nicht ablaufen darf.

### 4. Funktionstest

1. **Aufgabe starten:** im Taskplaner → Rechtsklick → „Ausführen“.
2. **Log prüfen:** `C:\tomcat-restart\watchdog.log` sollte etwa so beginnen:
   ```
   [2026-10-01 10:23:14] ------------------------------------------------------------
   [2026-10-01 10:23:14] Watchdog started as user: NT-AUTORITÄT\SYSTEM
   [2026-10-01 10:23:14] Running from: C:\tomcat-restart
   [2026-10-01 10:23:14] Monitoring flag: C:\tomcat-restart\restart.flag
   [2026-10-01 10:23:14] OK Service 'Tomcat90122' (found via process id 4711 from C:\tomcat-restart\tomcat.pid): State=Running, StartMode=Auto, Account=NT AUTHORITY\LocalService, ProcessId=4711
   [2026-10-01 10:23:14] ------------------------------------------------------------
   ```
   Beginnt die Dienstzeile mit `ERROR Tomcat service not found`, hat der Watchdog den Dienst nicht gefunden, der Rest der Zeile nennt den Grund.
3. **Alive-Datei prüfen:** `C:\tomcat-restart\watchdog.alive` muss sich alle ~10 s ändern. Die zweite Zeile ist dieselbe Dienstzeile (`OK …` bzw. `ERROR …`).
4. **Im Wiki:** Auf der Administration-Seite muss „Restart Tomcat“ erscheinen, nicht „Unavailable: Restart Tomcat“. Der Tooltip zeigt die Dienstzeile des Watchdogs. Nach einem Neustart des Wikis steht im Wiki-Log `Tomcat restart watchdog is ready: OK …`. Ein Klick startet den Tomcat neu. Im `watchdog.log` steht dann:
   ```
   [2026-10-01 10:30:02] Flag detected (created by NT-AUTORITÄT\LOKALER DIENST) -> attempting to restart service 'Tomcat90122'
   [2026-10-01 10:30:02] Before restart: OK Service 'Tomcat90122' (found via process id 4711 from C:\tomcat-restart\restart.flag): State=Running, ...
   [2026-10-01 10:30:14] Service 'Tomcat90122' restarted successfully after 12 s.
   [2026-10-01 10:30:14] After restart: OK Service 'Tomcat90122' (...): State=Running, ...
   [2026-10-01 10:30:14] Flag removed successfully.
   ```
   Ohne Wiki lässt sich der Watchdog testen, indem man die Datei von Hand anlegt: `echo test > C:\tomcat-restart\restart.flag`.

### 5. Update auf eine neue Version

Das Watchdog-Skript ist **nicht** im WAR enthalten, es wird als eigene Datei `tomcat-restart-watchdog.ps1` mitgeliefert. Erst das Skript, dann das WAR einspielen: Das neue Skript funktioniert auch mit dem alten Wiki (es findet den Dienst dann über `-ServicePattern`), das neue Wiki mit einem alten Skript meldet dagegen `outdated watchdog script?`.

1. **Skript ersetzen:** Die neue `tomcat-restart-watchdog.ps1` als `C:\tomcat-restart\watchdog.ps1` ablegen. Im Skript nichts mehr eintragen, weder Dienstnamen noch Pfade. Beginnt der Tomcat-Dienst nicht mit `Tomcat`, `-ServicePattern` in den Argumenten der Aufgabe setzen (Abschnitt 1). Frühere Anpassungen im alten Skript (Dienstname, Pfade) entfallen.
2. **Aufgabe neu starten**, sonst läuft das alte Skript weiter. Taskplaner → „Tomcat Restart Watchdog“ → „Beenden“, dann „Ausführen“, oder in einer PowerShell als Administrator:
   ```powershell
   Stop-ScheduledTask "Tomcat Restart Watchdog"; Start-ScheduledTask "Tomcat Restart Watchdog"
   ```
   Die Aufgabe muss als `SYSTEM` mit höchsten Privilegien laufen.
3. **Watchdog prüfen:** Am Ende von `watchdog.log` steht ein neuer Startblock mit `Watchdog started as user: NT-AUTORITÄT\SYSTEM` und `OK Service '…' (…): State=Running, …`. Bei `ERROR Tomcat service not found: …` nennt der Rest der Zeile den Grund.
4. **Schreibrechte erneuern:** Das Wiki legt jetzt auch `tomcat.pid` und den Unterordner `instances\` an, die Rechte des Tomcat-Kontos müssen also für den ganzen Ordner vererbt werden (`(OI)(CI)`, Abschnitt 3):
   ```powershell
   icacls "C:\tomcat-restart" /grant "*S-1-5-19:(OI)(CI)(M)"
   ```
5. **WAR einspielen** wie gewohnt (Tomcat stoppen, WAR ersetzen, Tomcat starten).
6. **Nach dem Start prüfen:** Etwa 30 s nach dem Start steht im Wiki-Log `Tomcat restart watchdog is ready: OK Service '…'`, sonst `Restarting Tomcat via the watchdog will currently not work: …` mit dem Grund. Auf der Administration-Seite steht „Restart Tomcat“ (nicht „Unavailable: …“), der Tooltip nennt den gefundenen Dienst. Optional ein Test-Klick: Nach dem Neustart lädt die Seite von selbst neu und zeigt die Anmeldung, weil die Sitzung mit dem Neustart endet.

Sollten auf dem Server mehrere Tomcat-Instanzen dasselbe `C:\tomcat-restart` verwenden, ist der Button danach mit Begründung gesperrt, siehe [„Sonderfall“](#-sonderfall-mehrere-tomcat-instanzen-auf-einem-server).

---

## 🧩 Sonderfall: mehrere Tomcat-Instanzen auf einem Server

Dieser Abschnitt ist nur relevant, falls auf einem Server mehrere Tomcat-Instanzen mit KnowWE betrieben werden, etwa ein Test- und ein Produktivsystem. Derzeit ist das nicht vorgesehen. Die Beschreibung dient der Vorsorge und erklärt die Schutzmechanismen, die im Wiki dafür vorhanden sind.

### Einrichtung

Jede Tomcat-Instanz benötigt ein eigenes Verzeichnis und eine eigene Watchdog-Aufgabe. Teilen sich mehrere Instanzen ein Verzeichnis, gibt es `tomcat.pid`, `restart.flag` und die Dienstzeile in `watchdog.alive` nur einmal für alle, ein Neustart könnte also die falsche Instanz treffen.

Für eine weitere Instanz, z. B. ein Testsystem:
1. Verzeichnis anlegen, z. B. `C:\tomcat-restart-test`, mit Schreibrechten für das Konto dieses Tomcat-Dienstes (Abschnitt 3 der Windows-Einrichtung).
2. In dieser Instanz die System-Property `-Dknowwe.tomcat.restart.dir=C:\tomcat-restart-test` setzen (siehe unten) und die Instanz neu starten. Die bestehende Instanz behält den Standard `C:\tomcat-restart`.
3. Eine eigene geplante Aufgabe für dieses Verzeichnis anlegen (Abschnitt 2 der Windows-Einrichtung), entweder
   - mit einer unveränderten Kopie des Skripts im neuen Verzeichnis (`C:\tomcat-restart-test\watchdog.ps1` verwendet dann `C:\tomcat-restart-test`), oder
   - mit dem vorhandenen Skript und `-RestartDir "C:\tomcat-restart-test"` in den Argumenten der Aufgabe. Das Verzeichnis muss existieren, sonst beendet sich die Aufgabe sofort (Taskplaner: „Letztes Ausführungsergebnis“ `(0x1)`). Kein `\` vor dem schließenden Anführungszeichen: Windows liest `\"` als Anführungszeichen, ein folgendes `-ServicePattern` wird dann Teil des Verzeichnisses.
4. Bei ähnlich benannten Diensten zusätzlich `-ServicePattern` je Aufgabe setzen, z. B. `TomcatTest*`. Das Muster greift nur als Rückfall, wenn der Dienst nicht über die PID gefunden wird, sollte aber nur den eigenen Dienst treffen.

Das Wiki schreibt beim Start ins Wiki-Log, welches Verzeichnis es verwendet (`… (from -Dknowwe.tomcat.restart.dir)`). Zeigen Wiki und Watchdog auf verschiedene Verzeichnisse, findet das Wiki keinen Heartbeat, und der Button zeigt „Unavailable“ mit dem Verzeichnis, in dem das Wiki gesucht hat.

**System-Property setzen.** Die Property gilt für die gesamte Tomcat-Instanz, also für alle Wikis darin. Diese gehören ohnehin zusammen, weil ein Neustart alle betrifft. Nach dem Setzen die Instanz neu starten.
- Tomcat als Windows-Dienst: in `tomcat9w.exe //ES//<Dienstname>` → Reiter „Java“ → „Java Options“ eine Zeile `-Dknowwe.tomcat.restart.dir=C:\tomcat-restart-test` ergänzen, oder per Kommandozeile:
  ```powershell
  & "C:\…\Tomcat 9.0\bin\tomcat9.exe" //US//<Dienstname> ++JvmOptions "-Dknowwe.tomcat.restart.dir=C:\tomcat-restart-test"
  ```
- Tomcat über `startup.bat`/`catalina.sh`: in `bin\setenv.bat` `set "CATALINA_OPTS=%CATALINA_OPTS% -Dknowwe.tomcat.restart.dir=…"` bzw. in `bin/setenv.sh` `CATALINA_OPTS="$CATALINA_OPTS -Dknowwe.tomcat.restart.dir=…"`.
- Docker: siehe [README-Docker.md](README-Docker.md).

### Erkennung einer Fehlkonfiguration

Die Erkennung ist immer aktiv, auch ohne jede Konfiguration. Sie greift vor allem dann, wenn eine weitere Instanz eingerichtet wird, ohne ihr ein eigenes Verzeichnis zu geben. Dazu legt jedes Wiki beim Start eine Datei `instances\<zufällige ID>.instance` an, mit PID, Rechnername, Startzeit und `catalina.base`. Die ID ist bei jedem Start neu. Die PID taugt nicht als Name, weil sie sich zwischen Rechnern bzw. Containern wiederholt, in Containern ist sie oft sogar immer gleich. Das Wiki aktualisiert die Datei alle 30 s, schreibt sie neu, falls sie fehlt, und löscht sie beim Herunterfahren.

Findet ein Wiki die Datei einer **anderen laufenden** Tomcat-Instanz, sperrt es den Neustart: „Unavailable“ am Button, 503 bei der Aktion, Warnung beim Start. Die Meldung nennt beide Tomcats. Geprüft wird bei jedem Aufruf der Administration-Seite und bei jedem Klick. Der Zustand **heilt** deshalb von selbst: Ist der andere Tomcat weg, ist der Neustart beim nächsten Laden der Seite wieder möglich.

Wann eine andere Datei als „weg“ gilt:
- **Selber Rechner:** sofort, wenn der Prozess mit ihrer PID nicht mehr läuft, etwa bei einem abgeschossenen Tomcat, der seine Datei nicht mehr löschen konnte. Die Datei wird dann gelöscht.
- **Allgemein:** wenn sie seit mehr als 90 s nicht aktualisiert wurde. Das gilt für Tomcats auf anderen Rechnern bzw. in anderen Containern, deren Prozess das Wiki nicht sehen kann. Es gilt auch, wenn die PID eines beendeten Tomcats inzwischen ein anderer Prozess bekommen hat: Die Datei wird nicht mehr aktualisiert, der scheinbare Konflikt hält also höchstens 90 s. Solche Dateien vom selben Rechner werden gelöscht, fremde nach einem Tag.
- **Kein Konflikt** ist eine Datei vom selben Rechner mit der eigenen PID, also eine zweite Web-App im selben Tomcat. Die wird ohnehin mit neu gestartet.

---

## 🔍 Fehlersuche

**Warnung beim Start des Wikis.** 30 s nach dem Start prüft das Wiki, ob ein Neustart funktionieren würde, und schreibt ins Wiki-Log:
- `Tomcat restart watchdog is ready: OK …`: alles in Ordnung.
- `Restarting Tomcat via the watchdog will currently not work: …`: Der Grund steht dahinter, dieselben Gründe wie bei „Unavailable“ unten. So fällt ein Problem nach einem Deployment auf, bevor jemand den Neustart braucht.
- `… it does not report the service state (outdated watchdog script?)`: Der Watchdog läuft, ist aber ein altes Skript ohne Dienstzeile. Neues Skript einspielen und die Aufgabe neu starten.
- `Tomcat restart watchdog is not set up`: Verzeichnis oder `watchdog.alive` fehlen, es ist kein Watchdog eingerichtet.

**Button zeigt „Unavailable: Restart Tomcat“.** Der Tooltip nennt den Grund:
- *Several Tomcats use the restart directory …*: Mehrere laufende Tomcat-Instanzen verwenden dasselbe Verzeichnis, siehe [„Sonderfall“](#-sonderfall-mehrere-tomcat-instanzen-auf-einem-server).
- *The watchdog reports: Tomcat service not found: …*: Der Watchdog findet den Dienst nicht. Dahinter steht, was er versucht hat, z. B. dass kein Dienst auf `-ServicePattern` passt oder mehrere laufen.
- *heartbeat file … does not exist*: Watchdog nicht eingerichtet oder nie gelaufen. Zeigt der Taskplaner als „Letztes Ausführungsergebnis“ `(0x1)`, gibt es das Verzeichnis aus `-RestartDir` nicht.
- *heartbeat file … was last updated N s ago*: Watchdog läuft nicht mehr. Status der geplanten Aufgabe prüfen.
- *watchdog log file … does not exist*: Watchdog nie gestartet oder falsches Verzeichnis.
- *Cannot write to …*: Das Tomcat-Konto hat keine Schreibrechte (Abschnitt 3). Details stehen im Wiki-Log unter `Cannot create test flag`.

**Klick auf „Restart Tomcat“, aber kein Neustart.** Das Wiki zeigt nach 60 bzw. 120 s eine rote Meldung „Tomcat restart failed“. Im Wiki-Log steht ein ERROR von `TomcatServiceRestarter` mit den letzten 20 Zeilen aus `watchdog.log`. Das Log lässt sich über „Download recent logs“ auf der Administration-Seite herunterladen, Serverzugriff ist dafür nicht nötig.

| Meldung im Wiki-Log | Bedeutung | Typische Ursache |
|---|---|---|
| `The watchdog did not process the restart flag within 60 s` | Das Flag wurde nicht abgeholt. Das Wiki löscht es dann selbst, damit später kein unerwarteter Neustart ausgelöst wird | Watchdog hängt oder beobachtet ein anderes Verzeichnis. Docker: `docker restart` fehlgeschlagen, siehe [README-Docker.md](README-Docker.md) |
| `The watchdog removed the restart flag, but Tomcat did not shut down within 120 s` | Der Watchdog hat es versucht, der Tomcat lief aber weiter | Dienst nicht gefunden, fehlende Rechte zum Beenden des Dienstes. Die genaue Meldung steht in den mitgeloggten `watchdog.log`-Zeilen (`… cannot restart: ERROR Tomcat service not found …` bzw. `ERROR while restarting service …`) |
| `restart flag … already exists since N s` | Beim Klick lag schon ein altes Flag | Ein früherer Versuch wurde nicht verarbeitet |
| `but the restart watchdog is not available` | Klick, obwohl der Watchdog nicht verfügbar ist | siehe „Unavailable“ oben |

Häufige Fehler im `watchdog.log`:
- `ERROR Tomcat service not found: …` bzw. `Flag detected …, but cannot restart: …`: Der Dienst passt nicht auf `-ServicePattern`, oder es laufen mehrere passende Dienste. `-ServicePattern` in den Argumenten der Aufgabe anpassen und die Aufgabe neu starten.
- `Service state changed: …`: Der Dienstzustand hat sich geändert, z. B. beim Neustart oder weil der Dienst nach einem Tomcat-Update anders heißt. Nur zur Information.
- `ERROR while restarting service …: … kann nicht geöffnet werden … Zugriff verweigert` (bzw. `Access is denied`): Die Aufgabe läuft nicht als SYSTEM bzw. ohne Rechte auf den Dienst.
- `Flag detected …` ohne folgende Zeile: `Restart-Service` hängt, meist weil der Tomcat nicht herunterfährt. Der Dienst steht dann in `services.msc` auf „Wird beendet“. In dieser Zeit wird auch `watchdog.alive` nicht aktualisiert.

---

## 📝 Änderungen

**2026-09-29** (der Neustart schlug fehl, weil nach einem Tomcat-Update der Dienstname nicht mehr stimmte: Er war im Skript zwar angepasst, die laufende Aufgabe aber nicht neu gestartet)

- **Dienst ohne festen Namen:** Das Wiki schreibt beim Start seine Prozess-ID nach `tomcat.pid` und in `restart.flag`. Der Watchdog findet den Dienst über diese PID, sonst als einzigen (laufenden) Dienst, der auf `$servicePattern` passt (Standard `Tomcat*`). Er bestimmt den Dienst bei jedem Durchlauf neu, ein neuer Dienstname nach einem Tomcat-Update braucht also keine Änderung und keinen Neustart der Aufgabe. `$service` entfällt.
- **Probleme fallen vor dem Klick auf:** Der Watchdog schreibt den Dienstzustand (`OK …`/`ERROR …`) als zweite Zeile in `watchdog.alive` und loggt Änderungen. Das Wiki wertet das aus: 30 s nach dem Start eine Warnung im Wiki-Log, wenn ein Neustart nicht funktionieren würde, „Unavailable“ mit Grund am Button, und im Tooltip des verfügbaren Buttons der gefundene Dienst. Ein altes Watchdog-Skript ohne Dienstzeile wird beim Start gemeldet.
- **Mehrere Tomcats im selben Verzeichnis:** Jedes Wiki registriert sich mit einer Heartbeat-Datei unter `instances/`. Läuft ein anderer Tomcat mit demselben Verzeichnis, ist der Neustart mit Warnung gesperrt, statt womöglich den falschen Tomcat zu treffen.
- **Docker:** Der Heartbeat enthält ebenso den Containerzustand, siehe [README-Docker.md](README-Docker.md).
- **Nach dem Test unter Windows** (Windows 10, PowerShell 5.1, Tomcat 9 als Dienst unter „Lokaler Dienst“):
  - Nach dem Neustart lädt die Seite neu, auch wenn die Sitzung weg ist. Vorher beantwortete der neue Tomcat die Statusabfrage mit 403 und die Seite wartete endlos („waiting for it to come back…“).
  - Ein zweiter Tomcat auf demselben Rechner wird erkannt. Vorher galt er als beendet, weil Java unter Windows Prozesse anderer Dienste nicht öffnen darf, seine Datei unter `instances/` wurde gelöscht.
  - Der Tooltip zeigt den Dienstzustand in einer eigenen Zeile. Neuer Abschnitt „Update auf eine neue Version“.
- **Keine festen Pfade mehr in den Skripten:** Das PowerShell-Skript nutzt das Verzeichnis, in dem es liegt, oder `-RestartDir`. Das Dienstmuster ist der Parameter `-ServicePattern`. Das Docker-Skript nimmt das Verzeichnis als zweites Argument oder aus `RESTART_DIR`. Mehrere Tomcats brauchen damit keine angepassten Kopien des Skripts. Das Wiki loggt beim Start, welches Verzeichnis es nutzt und ob es aus `-Dknowwe.tomcat.restart.dir` kommt. Die README beschreibt, wie man die Property im Windows-Dienst, per `setenv` und im Docker-Container setzt.

**2026-09-28** (nach der Rückmeldung, dass der letzte Neustart nicht funktioniert hat und auch im Log keine hilfreichen Infos zu finden waren)

- **Wiki (`TomcatServiceRestarter`, `AdministrationToolProvider`):**
  - Jeder Neustart-Klick wird mit Benutzer geloggt, ein bereits liegendes Flag mit seinem Alter.
  - Nach dem Klick prüft das Wiki, ob der Watchdog das Flag innerhalb von 60 s abholt und ob der Tomcat danach innerhalb von 120 s herunterfährt. Wenn nicht, schreibt es einen ERROR mit dem Ende von `watchdog.log` ins Wiki-Log. Ein nicht abgeholtes Flag wird entfernt.
  - Neue Aktion `restart-tomcat-status` für den Stand des letzten Neustarts.
  - Ist der Watchdog nicht verfügbar, nennt der Button den Grund im Tooltip. Die Aktion antwortet mit 503 und dem Grund statt mit „Unknown tool type“.
- **Button (JS):** Die Anfrage geht sofort raus, nicht erst nach 12 s. Der Button zeigt den Zustand an, meldet Fehler rot und lädt die Seite erst neu, wenn der neu gestartete Tomcat antwortet (erkennbar an der geänderten JVM-Startzeit). Nach 10 Minuten ohne Rückkehr gibt er auf.
- **`tomcat-restart-watchdog.ps1`:**
  - Loggt beim Start das ausführende Konto sowie Zustand und Anmeldekonto des Dienstes, ein falscher Dienstname fällt sofort auf.
  - Loggt beim Neustart, wer das Flag angelegt hat, den Dienstzustand vor und nach dem Neustart, die Dauer und Fehler vollständig mit Exception, innerem Fehler und ErrorId.
  - Das Log wird als UTF-8 geschrieben, damit das Wiki es lesen kann. Alle Meldungen sind auf Englisch.
- **`docker-restart-watchdog.sh`:** siehe [README-Docker.md](README-Docker.md).
- **README:** Neu geschrieben. Die Aufgabe läuft ausdrücklich als SYSTEM, die wirkungslose Umleitung `*> watchdog-console.log` ist entfernt. Neu sind die Hinweise zu Dienstname, Dateiname, Kontowechsel des Tomcat, `knowwe.tomcat.restart.dir` und der Fehlersuche. Die Beispielausgaben passen jetzt zum Skript. Die Docker-Variante steht jetzt in einer eigenen [README-Docker.md](README-Docker.md).
