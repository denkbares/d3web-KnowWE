# jspwiki git install:
1. use only pem keys, cause new openssh key format is not supported by JSch
2. add entry to known_hosts: ssh-keyscan -t rsa,ecdsa -p 2222 git.denkbares.com >> .ssh/known_hosts
3. add .ssh/config to use right keys in JSch
* openssh ssh-keygen doesn't support exporting private key to another format, so use: ssh-keygen -m PEM -t rsa -C "mail" -b 4096

# Switch from VersioningFileProvider to GitVersioningFileProvider
1. for all attachments it is necessary to provide a file for the latest version
	* to do this, just copy the file with the highest number out of the \<pageName>-att/<attachment-name>-dir directory to the \<pageName>-att directory
2. then remove all unnecessary version directories (including also attachment-dir directories)
3. create a git repository in your Wiki directory with `git init`
4. add all files to the new git repo with `git add .`
5. commit all of the files: git `commit -m "Initial commit"`
6. now you can configure the GitVersioningFileProvider and GitVersioningAttachmentProvider in your jspwiki-custom.properties

If you already have a remote Git repository for your Wiki site you can just configure jspwiki.gitVersioningFileProvider.remoteGit to point to your remote Git and configure an empty directory for your jspwiki.fileSystemProvider.pageDir and the file provider will clone this repository on initialization
