# jspwiki git install:
1. use only pem keys, cause new openssh key format is not supported by JSch
2. add entry to known_hosts: ssh-keyscan -t rsa,ecdsa -p 2222 git.denkbares.com >> .ssh/known_hosts
3. add .ssh/config to use right keys in JSch
* openssh ssh-keygen doesn't support exporting private key to another format, so use: ssh-keygen -m PEM -t rsa -C "mail" -b 4096
