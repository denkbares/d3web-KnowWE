# 🛠️ Tomcat Restart Watchdog

Mit diesem Setup lässt sich der Tomcat über **„Restart Tomcat“** auf der Administration-Seite des Wikis neu
starten. Der Wiki-Admin braucht dafür keinen Zugriff auf den Server.

Die Web-App startet den Tomcat nicht selbst neu, sie darf und kann ihren eigenen Dienst nicht steuern. Sie
legt nur eine Datei `restart.flag` an. Ein externes Skript (der Watchdog) überwacht diese Datei, startet den
Dienst bzw. Container neu und löscht die Datei danach.

Es gibt zwei Varianten:

| Variante | Skript | Neustart per |
|---|---|---|
| Windows, Tomcat als Dienst | `tomcat-restart-watchdog.ps1` | `Restart-Service` |
| Linux, Tomcat im Docker-Container | `docker-restart-watchdog.sh` | `docker restart` |

Diese README beschreibt die Funktionsweise, die Windows-Einrichtung und die Fehlersuche im Wiki. Die
Einrichtung der Docker-Variante steht in [README-Docker.md](README-Docker.md).

---

## ⚙️ Funktionsweise

```
Wiki (Tomcat-Konto)                         Watchdog (eigenes Konto)
───────────────────                         ────────────────────────
Button „Restart Tomcat“ sichtbar,  ◄──────  schreibt alle ~10 s watchdog.alive
  wenn watchdog.alive < 60 s alt,           schreibt watchdog.log
  watchdog.log existiert und
  das Verzeichnis beschreibbar ist
Klick → legt restart.flag an       ──────►  findet restart.flag
                                            → startet Dienst/Container neu
                                            → löscht restart.flag
Wiki prüft nach: Wurde das Flag
  abgeholt? Fährt der Tomcat herunter?
  Wenn nicht → Fehler im Wiki-Log
  (inkl. Ende von watchdog.log) und
  Fehlermeldung im Browser
```

Alle Dateien liegen in einem gemeinsamen Verzeichnis:

```
C:\tomcat-restart\            (Linux/Docker: /knowwe/tomcat-restart/)
 ├── watchdog.ps1             das Skript (Windows)
 ├── watchdog.log             Log des Watchdogs (wird automatisch erstellt)
 ├── watchdog.alive           Heartbeat, alle ~10 s aktualisiert (wird automatisch erstellt)
 ├── restart.flag             Neustart-Anforderung (legt das Wiki an, löscht der Watchdog)
 └── restart-test.flag        kurzzeitig vom Wiki angelegt, um die Schreibrechte zu prüfen
```

Das Wiki liest das Verzeichnis aus der System-Property **`knowwe.tomcat.restart.dir`**. Ohne Property gilt
`C:\tomcat-restart\` (Windows) bzw. `/knowwe/tomcat-restart/` (sonst). Die Pfade in den Skripten sind **fest
eingetragen**. Wer das Verzeichnis ändert, muss deshalb die Property **und** die Pfade im Skript anpassen.

---

## 🪟 Einrichtung unter Windows

### 1. Skript ablegen und anpassen

`tomcat-restart-watchdog.ps1` als **`C:\tomcat-restart\watchdog.ps1`** ablegen. Dann oben im Skript prüfen:

- **`$service`**: der Dienstname des Tomcat, nicht der Anzeigename. Standard ist `Tomcat9`. Den tatsächlichen
  Namen zeigt `Get-Service *tomcat*` oder `services.msc` → Eigenschaften → „Dienstname“.
- die Pfade `$flagPath`, `$alivePath` und `$logFile`, falls nicht `C:\tomcat-restart` verwendet wird.

### 2. Geplante Aufgabe einrichten

Öffne den **Taskplaner** (`taskschd.msc`) → **Aufgabe erstellen...**

**Allgemein**
- **Name:** `Tomcat Restart Watchdog`
- **Benutzer:** `SYSTEM` („Benutzer oder Gruppe ändern…“ → `SYSTEM`). SYSTEM darf Dienste beenden und starten und
  hat lokal Vollzugriff auf `C:\tomcat-restart`. Es sollte **nicht** das Dienstkonto des Tomcat sein, siehe
  Abschnitt 3.
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
    Keine Umleitung wie `*> datei.log` anhängen. Die Aufgabenplanung startet `powershell.exe` ohne Shell, die
    Umleitung würde nur als Argument an das Skript weitergereicht. Alle Meldungen stehen in `watchdog.log`.
  - **Starten in (optional):**
    ```
    C:\tomcat-restart
    ```

**Bedingungen**
- „Nur starten, wenn Computer im Netzbetrieb ist“ → deaktivieren

**Einstellungen**
- „Beenden, wenn die Aufgabe länger als ... läuft“ → deaktivieren
- „Wenn die Aufgabe bereits ausgeführt wird → Keine neue Instanz starten“ → aktivieren

Speichern. Nach Änderungen am Skript die Aufgabe beenden und neu starten, das Skript läuft in einer Endlosschleife.

### 3. Berechtigungen

Zwei Konten sind beteiligt:

| Wer | Konto | Braucht |
|---|---|---|
| Tomcat / Wiki | Anmeldekonto des Tomcat-Dienstes, z. B. „Lokaler Dienst“ oder ein AD-Dienstkonto (etwa für den Zugriff auf Netzlaufwerke) | Ändern-Rechte auf `C:\tomcat-restart` (Flag anlegen, Test-Flag anlegen und löschen) |
| Watchdog | SYSTEM (empfohlen) | Dienst beenden/starten, Dateien in `C:\tomcat-restart` lesen, schreiben, löschen. SYSTEM hat beides von Haus aus |

Das Anmeldekonto des Tomcat steht in `services.msc` → Dienst → „Anmelden“. Der Watchdog schreibt es beim Start
auch ins `watchdog.log` (`Account=…`).

Schreibrechte für das Tomcat-Konto setzen:

```powershell
# Tomcat läuft als "Lokaler Dienst"
icacls "C:\tomcat-restart" /grant "*S-1-5-19:(OI)(CI)(M)"

# Tomcat läuft unter einem Domänen-Dienstkonto
icacls "C:\tomcat-restart" /grant "DOMÄNE\dienstkonto:(OI)(CI)(M)"
```

**Achtung beim Wechsel des Tomcat-Kontos:** Bekommt der Tomcat-Dienst ein anderes Anmeldekonto, z. B. ein
AD-Konto für Netzlaufwerke, muss auch dieses Konto die Schreibrechte bekommen. Die geplante Aufgabe sollte
weiter als SYSTEM laufen. Wird sie stattdessen auf das Tomcat-Konto umgestellt, darf sie den Dienst
standardmäßig **nicht** beenden. Zusätzlich braucht sie dann das Recht „Anmelden als Stapelverarbeitungsauftrag“
und ein gespeichertes Kennwort, das nicht ablaufen darf.

### 4. Funktionstest

1. **Aufgabe starten:** im Taskplaner → Rechtsklick → „Ausführen“.
2. **Log prüfen:** `C:\tomcat-restart\watchdog.log` sollte etwa so beginnen:
   ```
   [2026-10-01 10:23:14] ------------------------------------------------------------
   [2026-10-01 10:23:14] Watchdog started as user: NT-AUTORITÄT\SYSTEM
   [2026-10-01 10:23:14] Running from: C:\tomcat-restart
   [2026-10-01 10:23:14] Monitoring flag: C:\tomcat-restart\restart.flag
   [2026-10-01 10:23:14] Service 'Tomcat9': State=Running, StartMode=Auto, Account=NT AUTHORITY\LocalService, ProcessId=4711
   [2026-10-01 10:23:14] ------------------------------------------------------------
   ```
   Steht dort `Service 'Tomcat9' NOT FOUND`, stimmt `$service` nicht.
3. **Alive-Datei prüfen:** Der Zeitstempel von `C:\tomcat-restart\watchdog.alive` muss sich alle ~10 s ändern.
4. **Im Wiki:** Auf der Administration-Seite muss „Restart Tomcat“ erscheinen, nicht „Unavailable: Restart
   Tomcat“. Ein Klick startet den Tomcat neu. Im `watchdog.log` steht dann:
   ```
   [2026-10-01 10:30:02] Flag detected (created by NT-AUTORITÄT\LOKALER DIENST) -> attempting to restart service 'Tomcat9'
   [2026-10-01 10:30:02] Before restart: Service 'Tomcat9': State=Running, ...
   [2026-10-01 10:30:14] Service 'Tomcat9' restarted successfully after 12 s.
   [2026-10-01 10:30:14] After restart: Service 'Tomcat9': State=Running, ...
   [2026-10-01 10:30:14] Flag removed successfully.
   ```
   Ohne Wiki lässt sich der Watchdog testen, indem man die Datei von Hand anlegt:
   `echo test > C:\tomcat-restart\restart.flag`.

---

## 🔍 Fehlersuche

**Button zeigt „Unavailable: Restart Tomcat“.** Der Tooltip nennt den Grund:
- *heartbeat file … does not exist*: Watchdog nicht eingerichtet oder nie gelaufen.
- *heartbeat file … was last updated N s ago*: Watchdog läuft nicht mehr. Status der geplanten Aufgabe prüfen.
- *watchdog log file … does not exist*: Watchdog nie gestartet oder falsches Verzeichnis.
- *Cannot write to …*: Das Tomcat-Konto hat keine Schreibrechte (Abschnitt 3). Details stehen im Wiki-Log unter
  `Cannot create test flag`.

**Klick auf „Restart Tomcat“, aber kein Neustart.** Das Wiki zeigt nach 60 bzw. 120 s eine rote Meldung „Tomcat
restart failed“. Im Wiki-Log steht ein ERROR von `TomcatServiceRestarter` mit den letzten 20 Zeilen aus
`watchdog.log`. Das Log lässt sich über „Download recent logs“ auf der Administration-Seite herunterladen,
Serverzugriff ist dafür nicht nötig.

| Meldung im Wiki-Log | Bedeutung | Typische Ursache |
|---|---|---|
| `The watchdog did not process the restart flag within 60 s` | Das Flag wurde nicht abgeholt. Das Wiki löscht es dann selbst, damit später kein unerwarteter Neustart ausgelöst wird | Watchdog hängt oder beobachtet ein anderes Verzeichnis. Docker: `docker restart` fehlgeschlagen, siehe [README-Docker.md](README-Docker.md) |
| `The watchdog removed the restart flag, but Tomcat did not shut down within 120 s` | Der Watchdog hat es versucht, der Tomcat lief aber weiter | Falscher Dienstname, fehlende Rechte zum Beenden des Dienstes. Die genaue Meldung steht in den mitgeloggten `watchdog.log`-Zeilen (`ERROR while restarting service …`) |
| `restart flag … already exists since N s` | Beim Klick lag schon ein altes Flag | Ein früherer Versuch wurde nicht verarbeitet |
| `but the restart watchdog is not available` | Klick, obwohl der Watchdog nicht verfügbar ist | siehe „Unavailable“ oben |

Häufige Fehler im `watchdog.log`:
- `Service 'Tomcat9' NOT FOUND`: `$service` an den tatsächlichen Dienstnamen anpassen.
- `ERROR while restarting service …: … kann nicht geöffnet werden … Zugriff verweigert` (bzw. `Access is denied`):
  Die Aufgabe läuft nicht als SYSTEM bzw. ohne Rechte auf den Dienst.
- `Flag detected …` ohne folgende Zeile: `Restart-Service` hängt, meist weil der Tomcat nicht herunterfährt.
  Der Dienst steht dann in `services.msc` auf „Wird beendet“. In dieser Zeit wird auch `watchdog.alive` nicht
  aktualisiert.

---

## 📝 Änderungen

**2026-09-28** (nach der Rückmeldung, dass der letzte Neustart nicht funktioniert hat und auch im Log keine hilfreichen Infos zu finden waren)

- **Wiki (`TomcatServiceRestarter`, `AdministrationToolProvider`):**
  - Jeder Neustart-Klick wird mit Benutzer geloggt, ein bereits liegendes Flag mit seinem Alter.
  - Nach dem Klick prüft das Wiki, ob der Watchdog das Flag innerhalb von 60 s abholt und ob der Tomcat danach
    innerhalb von 120 s herunterfährt. Wenn nicht, schreibt es einen ERROR mit dem Ende von `watchdog.log` ins
    Wiki-Log. Ein nicht abgeholtes Flag wird entfernt.
  - Neue Aktion `restart-tomcat-status` für den Stand des letzten Neustarts.
  - Ist der Watchdog nicht verfügbar, nennt der Button den Grund im Tooltip. Die Aktion antwortet mit 503 und
    dem Grund statt mit „Unknown tool type“.
- **Button (JS):** Die Anfrage geht sofort raus, nicht erst nach 12 s. Der Button zeigt den Zustand an,
  meldet Fehler rot und lädt die Seite erst neu, wenn der neu gestartete Tomcat antwortet (erkennbar an der
  geänderten JVM-Startzeit). Nach 10 Minuten ohne Rückkehr gibt er auf.
- **`tomcat-restart-watchdog.ps1`:**
  - Loggt beim Start das ausführende Konto sowie Zustand und Anmeldekonto des Dienstes, ein falscher
    Dienstname fällt sofort auf.
  - Loggt beim Neustart, wer das Flag angelegt hat, den Dienstzustand vor und nach dem Neustart, die Dauer und
    Fehler vollständig mit Exception, innerem Fehler und ErrorId.
  - Das Log wird als UTF-8 geschrieben, damit das Wiki es lesen kann. Alle Meldungen sind auf Englisch.
- **`docker-restart-watchdog.sh`:** siehe [README-Docker.md](README-Docker.md).
- **README:** Neu geschrieben. Die Aufgabe läuft ausdrücklich als SYSTEM, die wirkungslose Umleitung
  `*> watchdog-console.log` ist entfernt. Neu sind die Hinweise zu Dienstname, Dateiname, Kontowechsel des
  Tomcat, `knowwe.tomcat.restart.dir` und der Fehlersuche. Die Beispielausgaben passen jetzt zum Skript.
  Die Docker-Variante steht jetzt in einer eigenen [README-Docker.md](README-Docker.md).
