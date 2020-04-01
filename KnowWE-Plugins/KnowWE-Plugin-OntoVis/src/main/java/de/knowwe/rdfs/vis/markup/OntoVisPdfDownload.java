/*
 * Copyright (C) 2017 denkbares GmbH. All rights reserved.
 */

package de.knowwe.rdfs.vis.markup;

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;

import javax.servlet.ServletContext;

import com.denkbares.utils.Streams;
import de.knowwe.core.action.AbstractAction;
import de.knowwe.core.action.UserActionContext;
import de.knowwe.core.kdom.parsing.Section;
import de.knowwe.core.kdom.parsing.Sections;
import de.knowwe.kdom.defaultMarkup.DefaultMarkupType;
import de.knowwe.rdfs.vis.util.Utils;
import de.knowwe.visualization.Config;
import de.knowwe.visualization.dot.DOTRenderer;

/**
 * Generates and provides PDF file of a %%OntoVis markup for download.
 *
 * @author Albrecht Striffler (denkbares GmbH)
 * @created 30.06.17
 */
public class OntoVisPdfDownload extends AbstractAction {

	@Override
	public void execute(UserActionContext context) throws IOException {
		ServletContext servletContext = context.getServletContext();
		if (servletContext == null) return; // at wiki startup only

		// find graph name
		Section<?> section = Sections.get(context.getParameter("SectionID"));
		Config config = new Config(Sections.cast(section, DefaultMarkupType.class), context);
		config.setCacheFileID(Utils.getFileID(section, context));
		File dotFile = new File(DOTRenderer.getFilePath(config) + ".dot");
		String name = dotFile.getName().replace(".dot", "");

		File pdf = File.createTempFile(name, "pdf");
		DOTRenderer.convertDot(dotFile, pdf, DOTRenderer.getCommand(config, "pdf", dotFile, pdf));

		context.setContentType(BINARY);
		context.setHeader("Content-Disposition", "attachment;filename=\"" + name + ".pdf\"");

		InputStream fis = new FileInputStream(pdf);
		OutputStream ous = context.getOutputStream();

		Streams.stream(fis, ous);

		// close the Stream
		fis.close();
		ous.close();
	}
}
