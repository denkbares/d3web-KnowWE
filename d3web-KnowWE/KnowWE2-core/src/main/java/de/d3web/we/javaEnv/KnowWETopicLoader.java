package de.d3web.we.javaEnv;

import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.Reader;
import java.net.MalformedURLException;
import java.net.URL;
import java.net.URLConnection;
import java.nio.charset.Charset;
import java.util.LinkedList;


public abstract class KnowWETopicLoader {
	
	public abstract String loadTopic (String web, String topicname);
	
	public abstract String getFilePath();
	
	public abstract File getFile(String web, String topicname);
	
	protected String load(String dataFolder,String webname,String topicName) {
		
		String filePath = buildFilePath(dataFolder, webname, topicName);
		return loadFile( filePath);
	}
	
	public boolean existsTopic (String webName, String topicName) {
		String filePath = buildFilePath(getFilePath(), webName, topicName);
		File f = new File (filePath);
		return f.exists();
	}

	private String buildFilePath(String dataFolder, String webname,
			String topicName) {
		String filePath = dataFolder + "/";
		if(webname != null && webname.length() > 0 && !webname.equals("default_web")) {
			filePath += webname + "/"; 
		}
		filePath += topicName + ".txt";
		return filePath;
	}
	
	protected File createFile(String dataFolder, String webname, String topicName) {
		String filePath = buildFilePath(dataFolder, webname, topicName);
		return new File( filePath);
	}
	
	private static  InputStream openStream(URL url) throws IOException{
		URLConnection connection = url.openConnection();
		connection.setUseCaches(false);
		return connection.getInputStream();
	}
	

	
	
	public String loadFile( String filePath) {
		URL url = null;
		File f =  new File(filePath);
		try {
			url = f.toURI().toURL();
		} catch (MalformedURLException e1) {
			// TODO Auto-generated catch block
			e1.printStackTrace();
			return null;
		}
		// StringBuffer buffi = new StringBuffer();
		InputStream in = null;
		try {
			in = openStream(url);
		} catch (Exception e) {
			e.printStackTrace();
			return null;
		}


		Reader r = new InputStreamReader(in, Charset.forName("UTF-8"));

		int zeichen = 0;
		java.util.List<Byte> bytes = new LinkedList<Byte>();
		while (true) {

			try {
				zeichen = r.read();
			} catch (IOException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
			if (zeichen == -1)
				break;
			Byte b = Byte.valueOf((byte) zeichen);
			bytes.add(b);
		}

		try {
			r.close();
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		Object[] o = bytes.toArray();
		byte[] byteArray = new byte[o.length];

		for (int i = 0; i < o.length; i++) {
			byteArray[i] = ((Byte) o[i]).byteValue();

		}

		char[] c = new char[byteArray.length];
		for (int i = 0; i < byteArray.length; i++) {
			c[i] = (char) (byteArray[i] & 0xff);
		}
		String text = new String(c);
		
		return text;
	}
}
