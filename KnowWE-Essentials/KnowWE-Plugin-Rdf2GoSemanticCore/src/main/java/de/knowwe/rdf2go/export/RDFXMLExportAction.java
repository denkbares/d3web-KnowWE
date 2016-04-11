/*
 * Copyright (C) 2011 Chair of Artificial Intelligence and Applied Informatics
 * Computer Science VI, University of Wuerzburg
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
package de.knowwe.rdf2go.export;

import java.io.IOException;
import java.io.StringWriter;

import de.knowwe.core.action.AbstractAction;
import de.knowwe.core.action.UserActionContext;
import de.knowwe.rdf2go.Rdf2GoCore;

/**
 * Action to export the RDF-Ontology in RDF/XML format.
 * 
 * @author Jochen
 * @created 30.01.2012
 */
public class RDFXMLExportAction extends AbstractAction {

	@Override
	public void execute(UserActionContext context) throws IOException {
		String filename = "ontoloy-export";

		String mimetype = "application/rdf+xml; charset=UTF-8";

		context.setContentType(mimetype);
		context.setHeader("Content-Disposition", "attachment; filename=\"" + filename
				+ ".xml\"");
		StringWriter stringWriter = new StringWriter();
		Rdf2GoCore.getInstance().writeModel(stringWriter);
		String string = stringWriter.toString();
		context.setContentLength(string.getBytes().length);

		context.getWriter().write(string);

	}
}
