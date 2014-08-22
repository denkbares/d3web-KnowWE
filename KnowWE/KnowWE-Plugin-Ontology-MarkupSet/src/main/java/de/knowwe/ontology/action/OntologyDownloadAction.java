/*
 * Copyright (C) 2013 denkbares GmbH, Germany
 */
package de.knowwe.ontology.action;

import java.io.IOException;
import java.io.StringWriter;

import org.ontoware.rdf2go.model.Syntax;

import de.knowwe.core.Attributes;
import de.knowwe.core.action.AbstractAction;
import de.knowwe.core.action.UserActionContext;
import de.knowwe.core.compile.Compilers;
import de.knowwe.core.kdom.parsing.Section;
import de.knowwe.core.kdom.parsing.Sections;
import de.knowwe.rdf2go.Rdf2GoCompiler;
import de.knowwe.rdf2go.Rdf2GoCore;

/**
 * 
 * @author Sebastian Furth (denkbares GmbH)
 * @created 19.04.2013
 */
public class OntologyDownloadAction extends AbstractAction {

	public static final String PARAM_FILENAME = "filename";
	public static final String PARAM_SYNTAX = "syntax";

	@Override
	public void execute(UserActionContext context) throws IOException {

		String filename = context.getParameter(PARAM_FILENAME);
		String secID = context.getParameter(Attributes.SECTION_ID);
		Section<?> section = Sections.get(secID);
		Rdf2GoCompiler compiler = Compilers.getCompiler(section, Rdf2GoCompiler.class);
		Rdf2GoCore rdf2GoCore = compiler.getRdf2GoCore();

		Syntax syntax = Syntax.forName(context.getParameter(PARAM_SYNTAX));

		String mimeType = syntax.getMimeType() + "; charset=UTF-8";
		context.setContentType(mimeType);
		context.setHeader("Content-Disposition", "attachment; filename=\"" + filename + "\"");

		StringWriter writer = new StringWriter();
		rdf2GoCore.writeModel(writer, syntax);
		String content = writer.toString();
        byte[] contentBytes = content.getBytes("UTF-8");
		context.setContentLength(contentBytes.length);

		context.getWriter().write(new String(contentBytes, "UTF-8"));
	}

}
