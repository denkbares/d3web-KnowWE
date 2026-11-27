package de.knowwe.diaflux.utils;

import java.io.File;
import java.io.IOException;
import java.util.concurrent.TimeUnit;

import com.denkbares.strings.Strings;
import com.denkbares.utils.Files;
import com.denkbares.utils.Stopwatch;

/**
 * @author Adrian Müller
 * @created 15.01.17
 */
public class GraphvizConnector {

	/**
	 * The maximum run time of dot. Flowcharts are small, but a hanging dot must not block the request forever.
	 */
	private static final long TIMEOUT_MILLIS = TimeUnit.MINUTES.toMillis(2);

	private final String dotApp;
	private final long timeoutMillis;

	public GraphvizConnector() {
		this("dot", TIMEOUT_MILLIS);
	}

	/**
	 * Creates a connector running the specified dot executable, killing it if it does not finish within the specified
	 * time. For tests, which have no Graphviz installed and cannot wait for the default timeout.
	 */
	GraphvizConnector(String dotApp, long timeoutMillis) {
		this.dotApp = dotApp;
		this.timeoutMillis = timeoutMillis;
	}

	public String execute(String dotInput) throws IOException {
		String output;
		File unpositionedNodes = File.createTempFile("unpositionedNodes", null);
		File positionedNodes = File.createTempFile("positionedNodes", null);
		File errors = File.createTempFile("graphvizErrors", null);
		Files.writeFile(unpositionedNodes, dotInput);
		try {
			// add position attributes
			// -q suppresses warnings
			// -o defines output file
			// -Tdot defines output type
			// last argument is the input file
			// the error output goes into a file: with a pipe, dot would block as soon as its buffer is full
			Process p = new ProcessBuilder(dotApp, "-q", "-o", positionedNodes.getAbsolutePath(),
					"-Tdot", unpositionedNodes.getAbsolutePath())
					.redirectOutput(ProcessBuilder.Redirect.DISCARD)
					.redirectError(errors)
					.start();
			try {
				if (!p.waitFor(timeoutMillis, TimeUnit.MILLISECONDS)) {
					throw new IOException("Graphviz timed out after " + Stopwatch.getDisplay(timeoutMillis));
				}
			}
			finally {
				// dot must not keep running after a timeout or an interrupt
				if (p.isAlive()) p.destroyForcibly();
			}
			if (errors.length() > 0) {
				throw new IOException("Graphviz could not process dotfile: " + Strings.readFile(errors));
			}
			output = Strings.readFile(positionedNodes);
		}
		catch (InterruptedException e) {
			Thread.currentThread().interrupt();
			throw new IOException("Graphviz has been interrupted: " + e.getLocalizedMessage());
		}
		finally {
			positionedNodes.delete();
			unpositionedNodes.delete();
			errors.delete();
		}
		return output;
	}

}
