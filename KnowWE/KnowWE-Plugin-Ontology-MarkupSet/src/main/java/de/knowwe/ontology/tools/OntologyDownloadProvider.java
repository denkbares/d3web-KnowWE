/*
 * Copyright (C) 2013 denkbares GmbH, Germany
 */
package de.knowwe.ontology.tools;

import java.util.LinkedList;
import java.util.List;

import org.ontoware.rdf2go.model.Syntax;

import de.knowwe.core.Attributes;
import de.knowwe.core.kdom.parsing.Section;
import de.knowwe.core.user.UserContext;
import de.knowwe.kdom.defaultMarkup.DefaultMarkupType;
import de.knowwe.ontology.action.OntologyDownloadAction;
import de.knowwe.ontology.kdom.OntologyUtils;
import de.knowwe.rdf2go.Rdf2GoCore;
import de.knowwe.tools.DefaultTool;
import de.knowwe.tools.Tool;
import de.knowwe.tools.ToolProvider;
import de.knowwe.util.Icon;

/**
 * 
 * @author Sebastian Furth (denkbares GmbH)
 * @created 19.04.2013
 */
public class OntologyDownloadProvider implements ToolProvider {

	@Override
	public boolean hasTools(Section<?> section, UserContext userContext) {
		return OntologyUtils.getOntologyCompiler(section) != null;
	}

	@Override
	public Tool[] getTools(Section<?> section, UserContext userContext) {
		List<Tool> tools = new LinkedList<>();
		for (Syntax syntax : Syntax.collection()) {
			Tool tool = getDownloadTool(section, syntax);
			if (tool != null) tools.add(tool);
		}
		return tools.toArray(new Tool[tools.size()]);
	}

	protected Tool getDownloadTool(Section<?> section, Syntax syntax) {

		// check if ontology is empty
		Rdf2GoCore ontology = Rdf2GoCore.getInstance(OntologyUtils.getOntologyCompiler(section));
		if (ontology == null || ontology.isEmpty()
				//|| !ontology.getAvailableSyntaxes().contains(syntax)
				) {
			return null;
		}

		// get name of ontology
		String ontologyName = DefaultMarkupType.getContent(section).trim();
		if (ontologyName.isEmpty()) {
			ontologyName = "ontology";
		}

		String extension = syntax.getFilenameExtension();

		// JavaScript action
		String jsAction = "window.location='action/OntologyDownloadAction" +
				"?" + Attributes.TOPIC + "=" + section.getTitle() +
				"&amp;" + Attributes.WEB + "=" + section.getWeb() +
				"&amp;" + Attributes.SECTION_ID + "=" + section.getID() +
				"&amp;" + OntologyDownloadAction.PARAM_SYNTAX + "=" + syntax.getName() +
				"&amp;" + OntologyDownloadAction.PARAM_FILENAME + "=" + ontologyName + extension + "'";

		// assemble download tool
		return new DefaultTool(
				Icon.DOWNLOAD,
				"Download " + syntax.getName().toUpperCase(),
				"Download the entire ontology in " + syntax.getName() + " format for deployment.",
				jsAction);
	}

}
