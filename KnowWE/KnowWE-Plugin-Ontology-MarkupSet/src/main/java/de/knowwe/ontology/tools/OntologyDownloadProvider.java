/*
 * Copyright (C) 2013 denkbares GmbH, Germany
 */
package de.knowwe.ontology.tools;

import de.knowwe.core.Attributes;
import de.knowwe.core.kdom.parsing.Section;
import de.knowwe.core.user.UserContext;
import de.knowwe.kdom.defaultMarkup.DefaultMarkupType;
import de.knowwe.ontology.action.OntologyDownloadAction;
import de.knowwe.rdf2go.Rdf2GoCore;
import de.knowwe.tools.DefaultTool;
import de.knowwe.tools.Tool;
import de.knowwe.tools.ToolProvider;
import de.knowwe.tools.ToolUtils;

/**
 * 
 * @author Sebastian Furth (denkbares GmbH)
 * @created 19.04.2013
 */
public class OntologyDownloadProvider implements ToolProvider {

	@Override
	public boolean hasTools(Section<?> section, UserContext userContext) {
		Rdf2GoCore ontology = Rdf2GoCore.getInstance(section.getWeb(), section.getTitle());
		return !ontology.isEmpty();
	}

	@Override
	public Tool[] getTools(Section<?> section, UserContext userContext) {
		Tool download = getDownloadTool(section, userContext);
		return ToolUtils.asArray(download);
	}

	protected Tool getDownloadTool(Section<?> section, UserContext userContext) {

		// check if ontology is empty
		Rdf2GoCore ontology = Rdf2GoCore.getInstance(section.getWeb(), section.getTitle());
		if (ontology.isEmpty()) {
			return null;
		}

		// get name of ontology
		String ontologyName = DefaultMarkupType.getContent(section).trim();
		if (ontologyName.isEmpty()) {
			ontologyName = "ontology";
		}

		// JavaScript action
		String jsAction = "window.location='action/OntologyDownloadAction" +
				"?" + Attributes.TOPIC + "=" + section.getTitle() +
				"&amp;" + Attributes.WEB + "=" + section.getWeb() +
				"&amp;" + OntologyDownloadAction.PARAM_FILENAME + "=" + ontologyName + ".rdf'";

		// assemble download tool
		return new DefaultTool(
				"KnowWEExtension/d3web/icon/download16.gif",
				"Download",
				"Download the entire ontology as single file for deployment.",
				jsAction);
	}

}
