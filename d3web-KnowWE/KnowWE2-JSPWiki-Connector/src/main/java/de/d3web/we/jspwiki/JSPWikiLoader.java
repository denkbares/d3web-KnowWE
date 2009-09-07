package de.d3web.we.jspwiki;


import java.io.File;

import de.d3web.we.javaEnv.KnowWETopicLoader;

public class JSPWikiLoader extends KnowWETopicLoader{
	
	private String dataFolder = "/var/lib/jspwiki";

	public JSPWikiLoader(String path) {
		this.dataFolder = path;
	}
	
	@Override
	public String loadTopic(String web, String topicname) {
		// TODO Auto-generated method stub
		return load(dataFolder,"",topicname);
	}

	@Override
	public File getFile(String web, String topicname) {
		return createFile(dataFolder,"",topicname);
	}

	@Override
	public String getFilePath() {
		// TODO Auto-generated method stub
		return dataFolder;
	}
	
}
