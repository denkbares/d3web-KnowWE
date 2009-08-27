package de.d3web.textParser.casesTable;

import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.Reader;
import java.net.URL;
import java.util.ArrayList;
import java.util.List;

import de.d3web.textParser.complexRule.Utils;


public class TextParserResource {

	private Reader reader;
	private boolean excelSource = false;
	private boolean read = false;
	private String tableType = null;
	private String data = null;
	private ArrayList<String> dataLines = new ArrayList<String>();
	private String fileName = null;
	private URL url = null;
	private InputStream stream = null;
	private String topicName = null;
	private boolean hastext=false;
	private String text;
	
	


	public String getTopicName() {
		return topicName;
	}


	public void setTopicName(String topicName) {
		this.topicName = topicName;
	}


	public String getFileName() {
		return fileName;
	}

	
	@Override
	public String toString() {
		return fileName;
	}
	public boolean isExcelSource() {
		return excelSource;
	}

	public void setExcelSource(boolean excelSource) {
		this.excelSource = excelSource;
	}
	
	public String getTableType() {
		return this.tableType;
	}
	

	public Reader getReader() {
		return reader;
	}
	
	
	
	private void readReader() {
		int zeichen = 0;
		dataLines = null;
		try {
			dataLines = Utils.readFile(reader);
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		
		String result = "";
		for (int i = 0; i < dataLines.size(); i++) {
			result  += dataLines.get(i)+System.getProperty("line.separator");

		}
		data = result;
	}
	
	public List<String> getDataLines() {
		if(!read) {
			readReader();
			read = true;
		}
		return dataLines;
	}
	
	public String getDataString() {
		if(!read) {
			readReader();
			read = true;
		}
		return data;
		
	}
	

	public TextParserResource(URL url) {
		this.url = url;
		try {
			stream = url.openStream();
			reader = new InputStreamReader(stream);
			
		} catch (IOException e) {
			//System.out.println("Url Resource cannot be opened");
			e.printStackTrace();
		}
	}
	
	public static TextParserResource makeTextParserResource(URL url, boolean excel) {
		TextParserResource r = new TextParserResource(url);
		r.fileName = url.getFile();
		r.setExcelSource(excel);
		
		return r;
	}
	
	public InputStream getStream() {
		return stream;
	}

	public TextParserResource(InputStream stream) {
		this.stream = stream;
		reader = new InputStreamReader(stream);
		
	}
	
	
	public TextParserResource(Reader r) {
		reader = r;
	}
	
	public TextParserResource(String topicName, Reader r) {
		this.topicName = topicName;
		reader = r;
	}
	
	public TextParserResource(Reader r,String s) {
		reader = r;
		this.tableType = s;
	}

	public TextParserResource(String text,String s) {
		this.text=text;
		this.hastext=true;
		this.tableType = s;
	}

	public URL getUrl() {
		return url;
	}


	public boolean hasText() {		
		return this.hastext;
	}
	
	public void hasText(boolean v){
		hastext=v;
	}

	public String getText() {
		
		return text;
	}


	public void setText(String text) {
		this.text=text;		
	}


	
}
