/*
 * Copyright (C) 2013 denkbares GmbH, Germany
 */
package de.knowwe.ontology.tools;

import java.util.LinkedList;
import java.util.List;

import org.eclipse.rdf4j.rio.RDFFormat;
import org.eclipse.rdf4j.rio.RDFParserRegistry;

import de.knowwe.core.Attributes;
import de.knowwe.core.kdom.parsing.Section;
import de.knowwe.core.kdom.parsing.Sections;
import de.knowwe.core.user.UserContext;
import de.knowwe.kdom.defaultMarkup.DefaultMarkupType;
import de.knowwe.ontology.action.OntologyDownloadAction;
import de.knowwe.ontology.compile.OntologyCompiler;
import de.knowwe.ontology.compile.OntologyType;
import de.knowwe.ontology.kdom.OntologyUtils;
import de.knowwe.rdf2go.Rdf2GoCore;
import de.knowwe.tools.DefaultTool;
import de.knowwe.tools.Tool;
import de.knowwe.tools.ToolProvider;
import de.knowwe.util.Icon;

/**
 * @author Sebastian Furth (denkbares GmbH)
 * @created 19.04.2013
 */
public class OntologyDownloadProvider implements ToolProvider {

	public static final String TITLE = "title";

	@Override
	public boolean hasTools(Section<?> section, UserContext userContext) {
		return OntologyUtils.getOntologyCompiler(section) != null;
	}

	@Override
	public Tool[] getTools(Section<?> section, UserContext userContext) {
		List<Tool> tools = new LinkedList<>();
		for (RDFFormat syntax : RDFParserRegistry.getInstance().getKeys()) {
			Tool tool = getDownloadTool(section, syntax);
			if (tool != null) tools.add(tool);
		}
		return tools.toArray(new Tool[0]);
	}

	protected Tool getDownloadTool(Section<?> section, RDFFormat syntax) {

		OntologyCompiler compiler = OntologyUtils.getOntologyCompiler(section);
		if (compiler == null) {
			return null;
		}

		Rdf2GoCore ontology = Rdf2GoCore.getInstance(compiler);

		if (ontology == null || ontology.isEmpty()) {
			return null;
		}

		// get name of ontology
		String ontologyName = DefaultMarkupType.getContent(section).trim();
		if (ontologyName.isEmpty()) {
			ontologyName = "ontology";
		}

		String extension = syntax.getDefaultFileExtension();

		List<Section<OntologyType>> ontologySections = Sections.successors(section.getArticle(), OntologyType.class);
		String jsAction;
		//if there is only one ontology section on this article provide static URL access per article name
		String identifierForThisOntology;
		if (ontologySections.size() == 1) {
			identifierForThisOntology = TITLE + "=" + section.getTitle();
		}
		else {
			identifierForThisOntology = Attributes.SECTION_ID + "=" + section.getID();
		}
		jsAction = "action/OntologyDownloadAction" +
				"?" + identifierForThisOntology +
				"&amp;" + OntologyDownloadAction.PARAM_SYNTAX + "=" + syntax.getDefaultMIMEType() +
				"&amp;" + OntologyDownloadAction.PARAM_FILENAME + "=" + ontologyName + "." + extension + "";
		// assemble download tool
		return new DefaultTool(
				Icon.DOWNLOAD,
				"Download " + syntax.getName().toUpperCase(),
				"Download the entire ontology in " + syntax.getName() + " format for deployment.",
				jsAction,
				Tool.ActionType.HREF,
				Tool.CATEGORY_DOWNLOAD);
	}
}
