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
    
    log "Before restart: $(docker inspect -f 'State={{.State.Status}}, StartedAt={{.State.StartedAt}}' "$CONTAINER_NAME" 2>&1)"

    # Docker Container neustarten
    if output=$(docker restart "$CONTAINER_NAME" 2>&1); then
        log "Container '$CONTAINER_NAME' restarted successfully."
        
        # Flag löschen
        if rm "$FLAG_FILE"; then
            log "Flag removed successfully."
        else
            log "ERROR removing flag: $?"
        fi
    else
        log "ERROR while restarting container '$CONTAINER_NAME' (running as $(whoami)): $output"
    fi
fi

# Heartbeat aktualisieren: erste Zeile Zeitstempel, zweite Zeile Zustand des Containers (OK/ERROR), liest das Wiki
if state=$(docker inspect -f '{{.State.Status}}' "$CONTAINER_NAME" 2>&1); then
    container_state="OK Container '$CONTAINER_NAME': State=$state"
else
    container_state="ERROR Container '$CONTAINER_NAME' not found or not accessible (running as $(whoami)): $state"
fi
printf '%s\n%s\n' "$(date -u +'%Y-%m-%d %H:%M:%SZ')" "$container_state" > "$ALIVE_FILE" || log "ERROR updating alive file"
