/*
 * Copyright (C) 2012 denkbares GmbH
 * 
 * This is free software; you can redistribute it and/or modify it under the
 * terms of the GNU Lesser General Public License as published by the Free
 * Software Foundation; either version 3 of the License, or (at your option) any
 * later version.
 * 
 * This software is distributed in the hope that it will be useful, but WITHOUT
 * ANY WARRANTY; without even the implied warranty of MERCHANTABILITY or FITNESS
 * FOR A PARTICULAR PURPOSE. See the GNU Lesser General Public License for more
 * details.
 * 
 * You should have received a copy of the GNU Lesser General Public License
 * along with this software; if not, write to the Free Software Foundation,
 * Inc., 51 Franklin St, Fifth Floor, Boston, MA 02110-1301 USA, or see the FSF
 * site: http://www.fsf.org.
 */
package de.knowwe.rdfs.vis.markup;

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.OutputStream;

import javax.servlet.ServletContext;

import de.knowwe.core.action.AbstractAction;
import de.knowwe.core.action.UserActionContext;
import de.knowwe.core.compile.Compilers;
import de.knowwe.core.kdom.parsing.Section;
import de.knowwe.core.kdom.parsing.Sections;
import de.knowwe.ontology.compile.OntologyCompiler;

/**
 * 
 * @author Johanna Latt
 * @created 08.07.2012
 */
public class OntoVisDisplaySVG extends AbstractAction {

	@Override
	public void execute(UserActionContext context) throws IOException {
		ServletContext servletContext = context.getServletContext();
		if (servletContext == null) return; // at wiki startup only

		String sectionID = context.getParameter("SectionID");
		String realPath = servletContext.getRealPath("");
		String separator = System.getProperty("file.separator");
		String tmpPath = separator + "KnowWEExtension" + separator + "tmp" + separator;

		// find graph name
		Section<?> s = Sections.get(sectionID);
		OntologyCompiler ontoCompiler = Compilers.getCompiler(s, OntologyCompiler.class);

		String textHash = String.valueOf(s.getText().hashCode());
		String compHash = String.valueOf(ontoCompiler.getCompileSection().getTitle().hashCode());

		String path = realPath + tmpPath + "graph_" + textHash + "_" + compHash + ".svg";

		File svg = new File(path);
		FileInputStream fis = new FileInputStream(svg);
		OutputStream ous = context.getOutputStream();
		byte[] readBuffer = new byte[2156];
		int bytesIn = 0;
		while ((bytesIn = fis.read(readBuffer)) != -1) {
			ous.write(readBuffer, 0, bytesIn);
		}
		// close the Stream
		fis.close();
		ous.close();
	}
}