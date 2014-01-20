/*
 * Copyright (C) 2013 denkbares GmbH, Germany
 */
package de.knowwe.ontology.action;

import de.knowwe.core.Attributes;
import de.knowwe.core.action.AbstractAction;
import de.knowwe.core.action.UserActionContext;
import de.knowwe.rdf2go.Rdf2GoCore;

import java.io.IOException;
import java.io.StringWriter;

/**
 * 
 * @author Sebastian Furth (denkbares GmbH)
 * @created 19.04.2013
 */
public class OntologyDownloadAction extends AbstractAction {

	public static final String PARAM_FILENAME = "filename";

	@Override
	public void execute(UserActionContext context) throws IOException {

		String filename = context.getParameter(PARAM_FILENAME);
		String title = context.getParameter(Attributes.TOPIC);
		String web = context.getParameter(Attributes.WEB);

		String mimetype = "application/rdf+xml; charset=UTF-8";
		context.setContentType(mimetype);
		context.setHeader("Content-Disposition", "attachment; filename=\"" + filename + "\"");

		StringWriter writer = new StringWriter();
		Rdf2GoCore.getInstance(web, title).writeModel(writer);
		String content = writer.toString();
        byte[] contentBytes = content.getBytes("UTF-8");
		context.setContentLength(contentBytes.length);

		context.getWriter().write(new String(contentBytes, "UTF-8"));
	}

}
