package de.knowwe.diaflux.utils;

import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.nio.charset.Charset;

import org.apache.commons.io.IOUtils;
import org.jgrapht.ext.ImportException;

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
			writeOut(dotInput);
			// add position attributes
			Process p = Runtime.getRuntime().exec("dot -o " + tmpPos.getAbsolutePath()
					+ " -Tdot " + tmpUnpos.getAbsolutePath());
			p.waitFor();
			// catch errors
			if (p.getErrorStream().available() > 0) {
				String err = IOUtils.toString(p.getErrorStream(), Charset.defaultCharset());
				throw new IOException("Graphviz could not process dotfile: " + err);
			}
			output = Strings.readFile(tmpPos);
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
