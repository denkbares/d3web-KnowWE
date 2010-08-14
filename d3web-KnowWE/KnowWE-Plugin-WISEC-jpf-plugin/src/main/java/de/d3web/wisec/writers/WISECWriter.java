package de.d3web.wisec.writers;

import java.io.IOException;
import java.io.Writer;

import de.d3web.wisec.model.WISECModel;

public abstract class WISECWriter {

	protected WISECModel model;
	protected String outputDirectory;

	public WISECWriter(WISECModel model, String outputDirectory) {
		this.model = model;
		this.outputDirectory = outputDirectory;
	}

	public abstract void write() throws IOException;

	protected void writeBreadcrumb(Writer writer) throws IOException {
		writer.append("\n\n[Home | Main]");
	}

}
