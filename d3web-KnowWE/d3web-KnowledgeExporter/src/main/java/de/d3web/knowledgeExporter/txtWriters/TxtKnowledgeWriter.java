package de.d3web.knowledgeExporter.txtWriters;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.ByteArrayInputStream;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.OutputStreamWriter;
import java.io.UnsupportedEncodingException;

import de.d3web.knowledgeExporter.KnowledgeManager;
import de.d3web.knowledgeExporter.KnowledgeWriter;

public abstract class TxtKnowledgeWriter extends KnowledgeWriter {

	
	protected TxtKnowledgeWriter(KnowledgeManager manager) {
		super(manager);
	}
	
	
	private BufferedReader makeBufferedReader(String text) {
		byte[] byteArray = text.getBytes();
		ByteArrayInputStream in = new ByteArrayInputStream(byteArray);
		InputStreamReader reader = new InputStreamReader(in);
		BufferedReader bufReader = new BufferedReader(reader);
		return bufReader;
	}
	
	private BufferedWriter makeBufferedFileWriter(File output) throws FileNotFoundException, UnsupportedEncodingException {
		FileOutputStream fos = null;
		OutputStreamWriter osw = null;
		BufferedWriter writer = null;
		fos = new FileOutputStream(output);
		osw = new OutputStreamWriter(fos, "UTF-8");
		if (osw != null) {
			writer = new BufferedWriter(osw);
		}
		return writer;
	}
	
	
	private void writeTxtFile(String fileText, File output) throws IOException {
		BufferedReader reader = makeBufferedReader(fileText);
		BufferedWriter writer = makeBufferedFileWriter(output);
		String string = null;
		while ((string = reader.readLine()) != null) {
			writer.write(string + '\n');
			writer.flush();
		}
		reader.close();
		writer.close();
	}
	
	@Override
	public void writeFile(File output) throws IOException {
		writeTxtFile(writeText(), output);
	}
	
	public abstract String writeText();

}
