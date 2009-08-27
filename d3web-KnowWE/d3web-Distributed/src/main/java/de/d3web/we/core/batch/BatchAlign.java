package de.d3web.we.core.batch;

import java.io.File;
import java.net.MalformedURLException;

import de.d3web.we.core.DPSEnvironment;


public class BatchAlign {

	public static void main(String[] args) {
		try {
			File file = new File("D:/pkluegl/workspace/KWiki/bin/WEB-INF/resources/webs/KWiki/");
			new DPSEnvironment(file.toURI().toURL());
		} catch (MalformedURLException e) {
			e.printStackTrace();
		}
	}
	
}
