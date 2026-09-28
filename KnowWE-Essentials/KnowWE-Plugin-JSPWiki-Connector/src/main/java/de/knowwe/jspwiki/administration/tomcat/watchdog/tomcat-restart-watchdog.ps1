# ==============================================================
# Tomcat Restart Watchdog
# Monitors a flag file and restarts the Tomcat service when found
# with detailed logging for debugging permission issues
# ==============================================================

# --- Configuration ---
$flagPath   = "C:\tomcat-restart\restart.flag"      # Path to flag file
$alivePath  = "C:\tomcat-restart\watchdog.alive"    # Path to heartbeat file
$service    = "Tomcat9"                             # Service name
$logFile    = "C:\tomcat-restart\watchdog.log"      # Path to log file
$logMaxSize = 1048576                               # 1 MB

# --- Helper: Write timestamped log lines (UTF-8, so the web app can read them) ---
function Log([string]$message) {
    $timestamp = (Get-Date -Format 'yyyy-MM-dd HH:mm:ss')
    Add-Content -Path $logFile -Value "[$timestamp] $message" -Encoding UTF8
}

# --- Helper: Describe an error record including the exception details ---
function Describe-Error($err) {
    $text = "$($err.Exception.GetType().FullName): $($err.Exception.Message)"
    if ($err.Exception.InnerException) {
        $text += " | Inner: $($err.Exception.InnerException.Message)"
    }
    if ($err.FullyQualifiedErrorId) {
        $text += " | ErrorId: $($err.FullyQualifiedErrorId)"
    }
    return $text
}

# --- Helper: Describe the current state and logon account of the service ---
function Describe-Service {
    try {
        $svc = Get-CimInstance -ClassName Win32_Service -Filter "Name='$service'" -ErrorAction Stop
        if (-not $svc) {
            return "Service '$service' NOT FOUND - check the service name configured in this script"
        }
        return "Service '$service': State=$($svc.State), StartMode=$($svc.StartMode), Account=$($svc.StartName), ProcessId=$($svc.ProcessId)"
    } catch {
        return "Could not query service '$service': $(Describe-Error $_)"
    }
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
Log "Watchdog started as user: $([System.Security.Principal.WindowsIdentity]::GetCurrent().Name)"
Log "Running from: $PSScriptRoot"
Log "Monitoring flag: $flagPath"
Log (Describe-Service)
Log "------------------------------------------------------------"

# --- Main loop ---
while ($true) {
    try {
        if (Test-Path $flagPath) {
            $owner = "unknown"
            try { $owner = (Get-Acl $flagPath).Owner } catch { }
            Log "Flag detected (created by $owner) -> attempting to restart service '$service'"
            Log "Before restart: $(Describe-Service)"
            $started = Get-Date
            try {
                Restart-Service -Name $service -Force -ErrorAction Stop
                Log "Service '$service' restarted successfully after $([int]((Get-Date) - $started).TotalSeconds) s."
            } catch {
                Log "ERROR while restarting service after $([int]((Get-Date) - $started).TotalSeconds) s: $(Describe-Error $_)"
            }
            Log "After restart: $(Describe-Service)"
            try {
                # removed even if the restart failed, so the flag does not trigger an unexpected restart later
                Remove-Item $flagPath -Force -ErrorAction Stop
                Log "Flag removed successfully."
            } catch {
                Log "ERROR removing flag: $(Describe-Error $_)"
            }
        }
        # Heartbeat-Datei aktualisieren
        try {
            Set-Content -Path $alivePath -Value (Get-Date -Format 'u') -ErrorAction Stop
        } catch {
            Log "ERROR updating alive file: $(Describe-Error $_)"
        }

    } catch {
        Log "Unexpected error in main loop: $(Describe-Error $_)"
    }
    Start-Sleep -Seconds 10
}
