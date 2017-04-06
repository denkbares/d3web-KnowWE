package de.knowwe.diaflux.utils;

import java.io.File;
import java.io.IOException;
import java.nio.charset.Charset;

import org.apache.commons.io.IOUtils;

import com.denkbares.strings.Strings;
import com.denkbares.utils.Files;

/**
 * @author Adrian Müller
 * @created 15.01.17
 */
public class GraphvizConnector {


	public String execute(String dotInput) throws IOException {
		String output;
		File unpositionedNodes = File.createTempFile("unpositionedNodes", null);
		File positionedNodes = File.createTempFile("positionedNodes", null);
		Files.writeFile(unpositionedNodes, dotInput);
		try {
			// add position attributes
			// -q suppresses warnings
			// -o defines output file
			// -Tdot defines output type
			// last argument is the input file
			Process p = Runtime.getRuntime().exec("dot -q -o " + positionedNodes.getAbsolutePath()
					+ " -Tdot " + unpositionedNodes.getAbsolutePath());
			p.waitFor();
			if (p.getErrorStream().available() > 0) {
				String err = IOUtils.toString(p.getErrorStream(), Charset.defaultCharset());
				throw new IOException("Graphviz could not process dotfile: " + err);
			}
			output = Strings.readFile(positionedNodes);
		}
		catch (InterruptedException e) {
			throw new IOException("Graphviz has been interrupted: " + e.getLocalizedMessage());
		}
		finally {
			positionedNodes.delete();
			unpositionedNodes.delete();
		}
		return output;
	}

}
