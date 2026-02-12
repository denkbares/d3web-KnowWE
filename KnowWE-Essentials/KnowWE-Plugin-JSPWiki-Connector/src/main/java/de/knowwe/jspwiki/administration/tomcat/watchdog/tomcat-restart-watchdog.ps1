# ==============================================================
# Tomcat Restart Watchdog
# Monitors a flag file and restarts the Tomcat service when found
# with detailed logging for debugging permission issues
# ==============================================================

# --- Configuration ---
$flagPath   = "C:\tomcat-restart\restart.flag"      # Path to flag file
$service    = "Tomcat9"                             # Service name
$logFile    = "C:\tomcat-restart\watchdog.log"      # Path to log file
$logMaxSize = 1048576                               # 1 MB

# --- Helper: Write timestamped log lines ---
function Log([string]$message) {
    $timestamp = (Get-Date -Format 'yyyy-MM-dd HH:mm:ss')
    Add-Content -Path $logFile -Value "[$timestamp] $message"
}

# --- Helper: Rotate log if it gets too large ---
if (Test-Path $logFile) {
    $size = (Get-Item $logFile).Length
    if ($size -gt $logMaxSize) {
        $old = "$logFile.old"
        if (Test-Path $old) { Remove-Item $old -Force }
        Rename-Item $logFile $old
        Log "Log rotated: previous log renamed to $old"
    }
}

# --- Start info ---
Log "------------------------------------------------------------"
Log "Watchdog started as user: $env:USERNAME"
Log "Running from: $PSScriptRoot"
Log "Monitoring flag: $flagPath"
Log "------------------------------------------------------------"

# --- Main loop ---
while ($true) {
    try {
        if (Test-Path $flagPath) {
            Log "Flag detected -> attempting to restart service '$service'"
            try {
                Restart-Service -Name $service -Force -ErrorAction Stop
                Log "Service '$service' restarted successfully."
            } catch {
                Log "ERROR while restarting service: $_"
            }
            try {
                Remove-Item $flagPath -Force
                Log "Flag removed successfully."
            } catch {
                Log "ERROR removing flag: $_"
            }
        }
        # Heartbeat-Datei aktualisieren
        try {
            $alivePath = "C:\tomcat-restart\watchdog.alive"
            Set-Content -Path $alivePath -Value (Get-Date -Format 'u')
        } catch {
            Log "FEHLER beim Aktualisieren der Alive-Datei: $_"
        }

    } catch {
        Log "Unexpected error in main loop: $_"
    }
    Start-Sleep -Seconds 10
}
