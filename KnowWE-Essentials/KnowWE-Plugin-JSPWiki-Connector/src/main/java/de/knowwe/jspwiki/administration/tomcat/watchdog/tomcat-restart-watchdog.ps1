# ==============================================================
# Tomcat Restart Watchdog
# Monitors a flag file and restarts the Tomcat service when found
# with detailed logging for debugging permission issues
#
# Parameters (both optional):
#   -RestartDir      directory shared with the wiki (-Dknowwe.tomcat.restart.dir of the Tomcat),
#                    default: the directory of this script, e.g. C:\tomcat-restart
#   -ServicePattern  only services matching this name pattern are restarted, default: Tomcat*
# Several Tomcats on one server need one directory and one watchdog task each, e.g.
#   powershell.exe -File "C:\tomcat-restart-test\watchdog.ps1"     (a copy of this script, no changes needed)
#   powershell.exe -File "C:\tomcat-restart\watchdog.ps1" -RestartDir "D:\restart\test" -ServicePattern "TomcatTest*"
# ==============================================================
param(
    [string]$RestartDir = "",
    [string]$ServicePattern = "Tomcat*"
)

# --- Configuration ---
# $PSScriptRoot is not reliably set in parameter defaults of Windows PowerShell 5.1, so the default is resolved here
if (-not $RestartDir) { $RestartDir = $PSScriptRoot }
$RestartDir     = $RestartDir.TrimEnd('\', '/')
$flagPath       = Join-Path $RestartDir "restart.flag"      # Restart request, written by the wiki
$alivePath      = Join-Path $RestartDir "watchdog.alive"    # Heartbeat with the service state, read by the wiki
$pidPath        = Join-Path $RestartDir "tomcat.pid"        # Process id of the wiki, written by the wiki at startup
$logFile        = Join-Path $RestartDir "watchdog.log"      # Log of this script, read by the wiki
$servicePattern = $ServicePattern
$logMaxSize     = 1048576                                   # 1 MB

# The Tomcat service is determined on every run, so a new service name (e.g. after a Tomcat update) needs no change
# here: first the service running the process id the wiki has written (into the flag or tomcat.pid), otherwise the
# only service matching $servicePattern.

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

# --- Helper: Read a process id from a file, either plain or as "pid=<number>" ---
function Read-ProcessId([string]$path) {
    if (-not (Test-Path $path)) { return $null }
    try {
        $content = Get-Content -Path $path -Raw -ErrorAction Stop
        if ($content -match '(\d+)') { return [int]$Matches[1] }
    } catch { }
    return $null
}

# --- Helper: Find the service running the given process id (or its parent, if the JVM runs as child process) ---
function Find-ServiceByProcessId([int]$processId) {
    $svc = Get-CimInstance -ClassName Win32_Service -Filter "ProcessId=$processId" -ErrorAction SilentlyContinue |
        Where-Object { $_.Name -like $servicePattern } | Select-Object -First 1
    if ($svc) { return $svc }
    $proc = Get-CimInstance -ClassName Win32_Process -Filter "ProcessId=$processId" -ErrorAction SilentlyContinue
    if ($proc -and $proc.ParentProcessId) {
        return Get-CimInstance -ClassName Win32_Service -Filter "ProcessId=$($proc.ParentProcessId)" -ErrorAction SilentlyContinue |
            Where-Object { $_.Name -like $servicePattern } | Select-Object -First 1
    }
    return $null
}

# --- Helper: Determine the Tomcat service; returns the service (or $null) and a description of how it was found ---
function Resolve-TomcatService {
    $tried = @()
    foreach ($source in @($flagPath, $pidPath)) {
        $processId = Read-ProcessId $source
        if ($processId) {
            $svc = Find-ServiceByProcessId $processId
            if ($svc) { return @{ Service = $svc; Description = "found via process id $processId from $source" } }
            $tried += "no service '$servicePattern' runs process id $processId from $source"
        }
    }
    try {
        $candidates = @(Get-CimInstance -ClassName Win32_Service -ErrorAction Stop | Where-Object { $_.Name -like $servicePattern })
    } catch {
        return @{ Service = $null; Description = "could not list services: $(Describe-Error $_)" }
    }
    if ($candidates.Count -eq 1) {
        return @{ Service = $candidates[0]; Description = "only service matching '$servicePattern'" }
    }
    # e.g. the service of an old Tomcat version is still installed, but no longer running
    $running = @($candidates | Where-Object { $_.State -eq 'Running' })
    if ($running.Count -eq 1) {
        return @{ Service = $running[0]; Description = "only running service matching '$servicePattern'" }
    }
    if ($candidates.Count -eq 0) {
        $tried += "no service matches '$servicePattern'"
    } else {
        $tried += "several services match '$servicePattern' ($(($candidates | ForEach-Object { $_.Name }) -join ', '))"
    }
    return @{ Service = $null; Description = ($tried -join "; ") }
}

# --- Helper: One line describing the resolved service, starting with OK or ERROR (reported to the wiki) ---
function Describe-TomcatService($resolved) {
    if (-not $resolved.Service) {
        return "ERROR Tomcat service not found: $($resolved.Description)"
    }
    $svc = $resolved.Service
    return "OK Service '$($svc.Name)' ($($resolved.Description)): State=$($svc.State), StartMode=$($svc.StartMode), Account=$($svc.StartName), ProcessId=$($svc.ProcessId)"
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
Log "Service pattern: $servicePattern"
$lastServiceState = Describe-TomcatService (Resolve-TomcatService)
Log $lastServiceState
Log "------------------------------------------------------------"

# --- Main loop ---
while ($true) {
    try {
        $resolved = Resolve-TomcatService
        if (Test-Path $flagPath) {
            $owner = "unknown"
            try { $owner = (Get-Acl $flagPath).Owner } catch { }
            if (-not $resolved.Service) {
                Log "Flag detected (created by $owner), but cannot restart: $(Describe-TomcatService $resolved)"
            } else {
                $name = $resolved.Service.Name
                Log "Flag detected (created by $owner) -> attempting to restart service '$name'"
                Log "Before restart: $(Describe-TomcatService $resolved)"
                $started = Get-Date
                try {
                    Restart-Service -Name $name -Force -ErrorAction Stop
                    Log "Service '$name' restarted successfully after $([int]((Get-Date) - $started).TotalSeconds) s."
                } catch {
                    Log "ERROR while restarting service '$name' after $([int]((Get-Date) - $started).TotalSeconds) s: $(Describe-Error $_)"
                }
                $resolved = Resolve-TomcatService
                Log "After restart: $(Describe-TomcatService $resolved)"
            }
            try {
                # removed even if the restart failed, so the flag does not trigger an unexpected restart later
                Remove-Item $flagPath -Force -ErrorAction Stop
                Log "Flag removed successfully."
            } catch {
                Log "ERROR removing flag: $(Describe-Error $_)"
            }
        }

        # log changes of the service state, e.g. when the service was renamed or removed
        $serviceState = Describe-TomcatService $resolved
        $stateKey = { param($s) $s -replace ', ProcessId=\d+$', '' -replace 'process id \d+', 'process id' }
        if ((& $stateKey $serviceState) -ne (& $stateKey $lastServiceState)) {
            Log "Service state changed: $serviceState"
            $lastServiceState = $serviceState
        }

        # heartbeat: first line time stamp, second line the service state, read by the wiki
        try {
            Set-Content -Path $alivePath -Value @((Get-Date -Format 'u'), $serviceState) -Encoding UTF8 -ErrorAction Stop
        } catch {
            Log "ERROR updating alive file: $(Describe-Error $_)"
        }

    } catch {
        Log "Unexpected error in main loop: $(Describe-Error $_)"
    }
    Start-Sleep -Seconds 10
}
