# 🛠️ Tomcat Restart Watchdog unter Windows

Dieses Setup ermöglicht es, einen als **Windows-Dienst** laufenden Apache Tomcat von **innerhalb einer WebApp** neu zu starten — sicher und kontrolliert.

Die WebApp erstellt eine `restart.flag`-Datei.  
Ein externes PowerShell-Skript (`watchdog.ps1`) überwacht diese Datei und startet den Tomcat-Dienst automatisch neu, sobald sie gefunden wird.

---

## 📂 Verzeichnisstruktur

```
C:\
 └── tomcat-restart\
      ├── watchdog.ps1
      ├── watchdog.log   (wird automatisch erstellt)
      ├── watchdog.alive (wird automatisch erstellt und regelmäßig aktualisiert)
      └── restart.flag   (wird automatisch erstellt/gelöscht)
```

---

## 🧰 1. Geplante Aufgabe (Task) einrichten

Öffne den **Taskplaner** (`taskschd.msc`) → **Aufgabe erstellen...**

### Allgemein
- **Name:** `Tomcat Restart Watchdog`
- **Mit höchsten Privilegien ausführen** ✅
- **Unabhängig von der Benutzeranmeldung ausführen** ✅
- **Konfigurieren für:** deine Windows-Version

### Trigger
- Neu → **Beim Systemstart**  
  *(alternativ/zusätzlich zum Testen: Bei Anmeldung)*

### Aktionen
- Neu → **Programm starten**
  - **Programm/Skript:**  
```
powershell.exe
```
  - **Argumente hinzufügen:**  
```
-WindowStyle Hidden -ExecutionPolicy Bypass -File "C:\tomcat-restart\watchdog.ps1" *> "C:\tomcat-restart\watchdog-console.log" 2>&1
```
- **Starten in (optional):**  
```
C:\tomcat-restart
```

### Bedingungen
- „Nur starten, wenn Computer im Netzbetrieb ist“ → deaktivieren

### Einstellungen
- „Beenden, wenn die Aufgabe länger als ... läuft“ → deaktivieren  
- „Wenn die Aufgabe bereits ausgeführt wird → Keine neue Instanz starten“ → aktivieren

Speichern → Admin-Passwort eingeben.

---

## 🧪 3. Funktionstest

1. **Task starten:**  
   Im Taskplaner → Rechtsklick → „Ausführen“
2. **Flag erzeugen:**  
```cmd
echo test > C:\tomcat-restart\restart.flag
```
Alternativ: Neue leere Datei mit diesem Namen manuell im Windows-Explorer anlegen.

3. **Log prüfen:**  
   Öffne `C:\tomcat-restart\watchdog.log`

Beispielausgabe:
```
[2025-10-17 10:23:14] Watchdog gestartet als Benutzer: SYSTEM
[2025-10-17 10:23:25] Restart-Flag erkannt -> Dienst 'Tomcat9' wird neu gestartet...
[2025-10-17 10:23:32] Dienst 'Tomcat9' erfolgreich neu gestartet.
[2025-10-17 10:23:32] Restart-Flag entfernt.
```

4. **Alive-Datei prüfen:**
    Das Skript schreibt bei jedem Durchlauf (all  ~10 Sek) den aktuellen Zeitstempel nach:
```
C:\tomcat-restart\watchdog.alive
```
Dadurch kann die WebApp zuverlässig erkennen, ob der Watchdog aktiv läuft.

---

## ⚙️ 4. Möglicherweise nötig: Berechtigungen setzen

Tomcat läuft als Windows-Dienst unter dem Benutzer **„Lokaler Dienst“** (`NT-AUTORITÄT\Lokaler Dienst`).  
Damit die WebApp die `restart.flag`-Datei erzeugen darf, müssen Schreibrechte gesetzt werden:

```powershell
icacls "C:\tomcat-restart" /grant "NT-AUTORITÄT\Lokaler Dienst:(OI)(CI)(M)"
```

Alternativ universell:
```
icacls "C:\tomcat-restart" /grant "*S-1-5-19:(OI)(CI)(M)"
```

--- 

Der Neustartfunktionalität sollte nun erfolgreich eingerichtet sein.
