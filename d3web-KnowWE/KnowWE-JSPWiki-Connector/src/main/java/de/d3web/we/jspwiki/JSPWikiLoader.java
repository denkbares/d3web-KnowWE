/*
 * Copyright (C) 2009 Chair of Artificial Intelligence and Applied Informatics
 *                    Computer Science VI, University of Wuerzburg
 *
 * This is free software; you can redistribute it and/or modify it
 * under the terms of the GNU Lesser General Public License as
 * published by the Free Software Foundation; either version 3 of
 * the License, or (at your option) any later version.
 *
 * This software is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the GNU
 * Lesser General Public License for more details.
 *
 * You should have received a copy of the GNU Lesser General Public
 * License along with this software; if not, write to the Free
 * Software Foundation, Inc., 51 Franklin St, Fifth Floor, Boston, MA
 * 02110-1301 USA, or see the FSF site: http://www.fsf.org.
 */

package de.d3web.we.jspwiki;


import java.io.File;

import de.d3web.we.core.KnowWETopicLoader;

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
