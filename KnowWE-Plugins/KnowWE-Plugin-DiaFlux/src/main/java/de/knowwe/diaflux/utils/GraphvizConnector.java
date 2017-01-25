package de.knowwe.diaflux.utils;

import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.nio.charset.Charset;

import org.apache.commons.io.IOUtils;

import com.denkbares.strings.Strings;

/**
 * @author Adrian Müller
 * @created 15.01.17
 */
public class GraphvizConnector {

	private static final File tmpUnpos = new File("./target/temp/unpositionedNodes");
	private static final File tmpPos = new File("./target/temp/positionedNodes");
	private static final File tmpDir = new File("./target/temp");

	public GraphvizConnector() {
		tmpDir.mkdirs();
	}

	private static void writeOut(String inFile) throws IOException {
		tmpUnpos.createNewFile();
		FileWriter fw = new FileWriter(tmpUnpos, false);
		fw.write(inFile);
		fw.flush();
	}

	public String execute(String dotInput) throws IOException {
		String output;
		try {
			try {
				writeOut(dotInput);
			}
			catch (IOException e) {
				throw new IOException("Writing unpositioned dot failed.", e);
			}
			// add position attributes
			try {
				Process p = Runtime.getRuntime().exec("dot -o " + tmpPos.getAbsolutePath()
						+ " -Tdot " + tmpUnpos.getAbsolutePath());
				p.waitFor();
				if (p.getErrorStream().available() > 0) {
					String err = IOUtils.toString(p.getErrorStream(), Charset.defaultCharset());
					throw new IOException("Graphviz could not process dotfile: " + err);
				}
			}
			catch (IOException e) {
				throw new IOException("Executing graphviz failed. ", e);
			}
			// catch errors
			try {
				output = Strings.readFile(tmpPos);
			}
			catch (IOException e) {
				throw new IOException("Could not read from positioned dot file. " + e);
			}
		}
		catch (InterruptedException e) {
			throw new IOException("Graphviz has been interrupted: " + e.getLocalizedMessage());
		}
		finally {
			tmpPos.delete();
			tmpUnpos.delete();
		}
		return output;
	}

	@Override
	protected void finalize() throws Throwable {
		super.finalize();
		tmpDir.delete();
	}
}
