package de.d3web.we.webapp.tools;

import java.io.File;
import java.io.FileFilter;

 public class JarFileFilter implements FileFilter{
	
	public boolean accept(File f) {
		return f.getName().endsWith(".jar");
	}
	
}