#!/bin/bash

# ==============================================================
# Tomcat Restart Watchdog (Linux/Docker Version)
# Monitors a flag file and restarts the Docker container when found
# ==============================================================

# --- Configuration ---
# Passen Sie diese Pfade an Ihre Server-Umgebung an!
FLAG_FILE="/knowwe/tomcat-restart/restart.flag"
ALIVE_FILE="/knowwe/tomcat-restart/watchdog.alive"
LOG_FILE="/knowwe/tomcat-restart/watchdog.log"
LOG_MAX_SIZE=1048576 # 1 MB
CONTAINER_NAME="$1" # Name Ihres Docker-Containers

# --- Helper: Logging ---
log() {
    echo "[$(date '+%Y-%m-%d %H:%M:%S')] $1" >> "$LOG_FILE"
}

# Falls kein Name übergeben wurde, Abbruch mit Fehlermeldung
if [ -z "$CONTAINER_NAME" ]; then
    log "Fehler: Kein Container-Name übergeben!"
    exit 1
fi

# --- Helper: Log Rotation ---
if [ -f "$LOG_FILE" ]; then
    size=$(stat -c%s "$LOG_FILE")
    if [ "$size" -gt "$LOG_MAX_SIZE" ]; then
        mv "$LOG_FILE" "${LOG_FILE}.old"
        log "Log rotated: previous log renamed to ${LOG_FILE}.old"
    fi
fi

log "------------------------------------------------------------"
log "Watchdog started as user: $(whoami)"
log "Monitoring flag: $FLAG_FILE"
log "Target container: $CONTAINER_NAME"
log "------------------------------------------------------------"

# --- Main Logic ---
# Prüfen, ob Flag existiert
if [ -f "$FLAG_FILE" ]; then
    log "Flag detected -> attempting to restart container '$CONTAINER_NAME'"
    
    # Docker Container neustarten
    if docker restart "$CONTAINER_NAME" >/dev/null 2>&1; then
        log "Container '$CONTAINER_NAME' restarted successfully."
        
        # Flag löschen
        if rm "$FLAG_FILE"; then
            log "Flag removed successfully."
        else
            log "ERROR removing flag: $?"
        fi
    else
        log "ERROR while restarting container '$CONTAINER_NAME'. Ist der Name korrekt?"
    fi
fi

# Heartbeat aktualisieren
echo "$(date -u +'%Y-%m-%d %H:%M:%SZ')" > "$ALIVE_FILE" || log "ERROR updating alive file"
