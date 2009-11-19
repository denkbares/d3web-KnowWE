cd /D I:\Programme\Selenium-Grid
start ant launch-hub
start ant -Dport=5554 launch-remote-control
start ant -Dport=5555 launch-remote-control
start ant -Dport=5556 launch-remote-control
start ant -Dport=5557 launch-remote-control