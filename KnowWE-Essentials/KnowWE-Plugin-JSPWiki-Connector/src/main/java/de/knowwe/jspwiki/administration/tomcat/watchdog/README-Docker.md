# 🐳 Tomcat Restart Watchdog für Docker

Die Linux/Docker-Variante des Watchdogs. Läuft der Tomcat im Docker-Container, startet
`docker-restart-watchdog.sh` den **Container** neu, sobald das Wiki `restart.flag` anlegt.

Funktionsweise, Verzeichnisaufbau und die Fehlersuche im Wiki (Button „Unavailable“, Meldungen im Wiki-Log)
sind für beide Varianten gleich und stehen in der [README.md](README.md). Hier geht es nur um das Docker-Spezifische.

---

## ⚙️ Einrichtung

`docker-restart-watchdog.sh` läuft auf dem **Host**, nicht im Container. Es bekommt den Containernamen als
Argument:

```bash
/knowwe/tomcat-restart/docker-restart-watchdog.sh <containername>
```

### Pfade

Oben im Skript sind die Pfade fest eingetragen:

```
FLAG_FILE="/knowwe/tomcat-restart/restart.flag"
ALIVE_FILE="/knowwe/tomcat-restart/watchdog.alive"
LOG_FILE="/knowwe/tomcat-restart/watchdog.log"
```

Das Verzeichnis `/knowwe/tomcat-restart` muss **in den Container gemountet** sein, und zwar unter dem Pfad, den
das Wiki verwendet. Standard ist `/knowwe/tomcat-restart/`, abweichend über `-Dknowwe.tomcat.restart.dir`. Der
Tomcat-Benutzer im Container braucht dort Schreibrechte. Er legt das Flag und die Test-Datei
`restart-test.flag` an, ohne Schreibrechte zeigt das Wiki „Unavailable: Restart Tomcat“.

### Regelmäßiger Aufruf

Das Skript prüft **einmal** auf das Flag, aktualisiert `watchdog.alive` und beendet sich. Es muss deshalb
regelmäßig aufgerufen werden, und zwar **häufiger als alle 60 s**. Sonst meldet das Wiki den Watchdog
zwischendurch als inaktiv, weil `watchdog.alive` älter als 60 s ist.

- Ein Cron-Job im Minutentakt reicht dafür **nicht**.
- Geeignet ist z. B. ein systemd-Timer mit `OnUnitActiveSec=10s` oder eine Schleife:
  ```bash
  while true; do /knowwe/tomcat-restart/docker-restart-watchdog.sh <containername>; sleep 10; done
  ```

Jeder Aufruf schreibt einen Startblock ins Log. Das Log wird ab 1 MB nach `watchdog.log.old` rotiert.

### Berechtigungen

Das aufrufende Konto braucht Rechte für `docker restart` und `docker inspect`, also root oder die Gruppe
`docker`. Außerdem muss es in `/knowwe/tomcat-restart` schreiben und das vom Container angelegte Flag löschen
dürfen.

---

## 🔍 Fehlersuche

Unterschied zu Windows: Schlägt `docker restart` fehl, **bleibt das Flag liegen**, und der nächste Aufruf
versucht es erneut. Nach 60 s entfernt das Wiki das Flag und meldet im Wiki-Log
`The watchdog did not process the restart flag within 60 s`, mit dem Ende von `watchdog.log`.

Meldungen im `watchdog.log`:

| Meldung | Bedeutung |
|---|---|
| `Flag detected -> attempting to restart container '…'` | Flag gefunden, Neustart beginnt |
| `Before restart: State=running, StartedAt=…` | Containerzustand vor dem Neustart. Steht hier eine Fehlermeldung von `docker inspect`, stimmt meist der Containername nicht |
| `Container '…' restarted successfully.` | Neustart erfolgreich, danach `Flag removed successfully.` |
| `ERROR while restarting container '…' (running as …): …` | `docker restart` ist fehlgeschlagen. Dahinter steht die Ausgabe von Docker, z. B. `No such container` (falscher Name) oder `permission denied … docker.sock` (Konto nicht in der Gruppe `docker`) |
| `ERROR removing flag: …` | Neustart war erfolgreich, aber das Flag konnte nicht gelöscht werden. Rechte auf das Verzeichnis prüfen, sonst startet der Container beim nächsten Aufruf erneut |
| `Fehler: Kein Container-Name übergeben!` | Das Skript wurde ohne Argument aufgerufen |

---

## 📝 Änderungen

**2026-09-28**

- Das Skript loggt vor dem Neustart den Containerzustand (`docker inspect`).
- Schlägt `docker restart` fehl, landet die Fehlerausgabe von Docker zusammen mit dem ausführenden Konto im Log.
  Bisher wurde sie verworfen, und es stand nur „Ist der Name korrekt?“ im Log.
- Die Docker-Anleitung steht jetzt in dieser eigenen README statt in der Windows-README.
